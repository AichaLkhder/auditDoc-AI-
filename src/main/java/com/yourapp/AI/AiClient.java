package com.yourapp.AI;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;

import java.net.SocketTimeoutException;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class AiClient {

    @Value("${ai.provider:ollama}")
    private String provider;

    @Value("${ai.ollama.base-url:http://localhost:11434}")
    private String ollamaBaseUrl;

    @Value("${ai.ollama.api-path:/api/generate}")
    private String ollamaApiPath;

    @Value("${ai.api.url:}")
    private String apiUrl;

    @Value("${ai.api.key:}")
    private String apiKey;

    @Value("${ai.model:llama3}")
    private String model;

    @Value("${ai.max-tokens:2000}")
    private Integer maxTokens;

    @Value("${ai.temperature:0.7}")
    private Double temperature;

    @Value("${ai.simulation.mode:auto}")
    private String simulationMode;

    @Value("${ai.ollama.timeout:300000}")
    private int timeoutMs;

    @Value("${ai.retry.max-attempts:3}")
    private int maxRetryAttempts;

    @Value("${ai.retry.backoff-delay:1000}")
    private int retryBackoffDelay;

    private final RestTemplate restTemplate;
    private boolean forceSimulation = false;

    public AiClient() {
        // Configuration du RestTemplate avec timeout personnalisé
        this.restTemplate = createRestTemplateWithTimeout();
    }

    /**
     * Crée un RestTemplate avec des timeouts configurés
     */
    private RestTemplate createRestTemplateWithTimeout() {
        org.springframework.http.client.SimpleClientHttpRequestFactory factory =
                new org.springframework.http.client.SimpleClientHttpRequestFactory();

        factory.setConnectTimeout(Duration.ofMillis(timeoutMs));
        factory.setReadTimeout(Duration.ofMillis(timeoutMs));

        return new RestTemplate(factory);
    }

    /**
     * Point d'entrée principal pour envoyer une requête à l'IA
     */
    public String sendRequest(String prompt) {
        log.info("📤 Envoi requête IA | provider={} | model={}", provider, model);

        if (shouldSimulate()) {
            log.info("🎭 Mode simulation activé");
            return simulateAiResponse(prompt);
        }

        // Tentatives avec retry
        for (int attempt = 1; attempt <= maxRetryAttempts; attempt++) {
            try {
                String result = switch (provider.toLowerCase()) {
                    case "openai" -> callOpenAi(prompt);
                    case "ollama" -> callOllama(prompt);
                    case "gemini" -> callGemini(prompt);
                    default -> throw new IllegalStateException("Provider IA inconnu: " + provider);
                };

                if (result != null && !result.trim().isEmpty()) {
                    log.info("✅ Réponse IA reçue avec succès (tentative {}/{})", attempt, maxRetryAttempts);
                    return result;
                }

            } catch (ResourceAccessException e) {
                if (e.getCause() instanceof SocketTimeoutException) {
                    log.warn("⏱️ Timeout lors de la tentative {} sur {}", attempt, maxRetryAttempts);
                    if (attempt < maxRetryAttempts) {
                        sleepWithBackoff(attempt);
                        continue;
                    }
                }
                throw new RuntimeException("Erreur de connexion à l'API IA: " + e.getMessage(), e);
            } catch (HttpClientErrorException | HttpServerErrorException e) {
                log.error("❌ Erreur HTTP {} lors de la tentative {}: {}",
                        e.getStatusCode(), attempt, e.getMessage());
                if (attempt < maxRetryAttempts) {
                    sleepWithBackoff(attempt);
                    continue;
                }
                throw new RuntimeException("Erreur API IA: " + e.getStatusCode() + " - " + e.getMessage(), e);
            } catch (Exception e) {
                log.error("❌ Erreur inattendue lors de la tentative {}: {}", attempt, e.getMessage());
                if (attempt < maxRetryAttempts) {
                    sleepWithBackoff(attempt);
                    continue;
                }

                // En mode auto, basculer en simulation après tous les retry échoués
                if ("auto".equalsIgnoreCase(simulationMode)) {
                    log.warn("🔄 Basculement automatique en mode SIMULATION après {} échecs.", maxRetryAttempts);
                    forceSimulation = true;
                    return simulateAiResponse(prompt);
                }
                throw new RuntimeException("Erreur lors de l'appel à l'API IA: " + e.getMessage(), e);
            }
        }

        throw new RuntimeException("Toutes les tentatives ont échoué");
    }

    /**
     * Délai exponentiel avec backoff pour les retry
     */
    private void sleepWithBackoff(int attempt) {
        try {
            long delay = retryBackoffDelay * (long) Math.pow(2, attempt - 1);
            log.info("⏳ Attente de {} ms avant la tentative suivante", delay);
            Thread.sleep(Math.min(delay, 10000)); // Max 10 secondes
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interruption pendant le backoff", e);
        }
    }

    // =============================
    // GOOGLE GEMINI (AI Studio)
    // =============================
    private String callGemini(String prompt) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("Clé API Gemini manquante dans la configuration.");
        }

        // L'API Google requiert la clé en paramètre d'URL
        String urlWithKey = apiUrl + "?key=" + apiKey;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Construction du corps spécifique à Gemini (v1 / v1beta)
        Map<String, Object> body = new HashMap<>();

        // Structure : contents -> parts -> text
        Map<String, Object> textPart = new HashMap<>();
        textPart.put("text", prompt);

        Map<String, Object> content = new HashMap<>();
        content.put("parts", List.of(textPart));

        body.put("contents", List.of(content));

        // Configuration optionnelle
        Map<String, Object> generationConfig = new HashMap<>();
        generationConfig.put("temperature", temperature);
        generationConfig.put("maxOutputTokens", maxTokens);
        body.put("generationConfig", generationConfig);

        return extractGeminiResponse(send(urlWithKey, body, headers));
    }

    @SuppressWarnings("unchecked")
    private String extractGeminiResponse(Map<String, Object> response) {
        try {
            List<Map<String, Object>> candidates = (List<Map<String, Object>>) response.get("candidates");
            if (candidates == null || candidates.isEmpty()) {
                throw new RuntimeException("Aucun candidat trouvé dans la réponse Gemini.");
            }

            Map<String, Object> firstCandidate = candidates.get(0);
            Map<String, Object> content = (Map<String, Object>) firstCandidate.get("content");
            List<Map<String, Object>> parts = (List<Map<String, Object>>) content.get("parts");

            return (String) parts.get(0).get("text");
        } catch (Exception e) {
            log.error("❌ Erreur lors de l'extraction de la réponse Gemini. Structure reçue: {}", response);
            throw new RuntimeException("Format de réponse Gemini invalide", e);
        }
    }

    // =============================
    // OPENAI
    // =============================
    private String callOpenAi(String prompt) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("Clé API OpenAI manquante dans la configuration.");
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        Map<String, Object> body = new HashMap<>();
        body.put("model", model);
        body.put("messages", List.of(Map.of("role", "user", "content", prompt)));
        body.put("max_tokens", maxTokens);
        body.put("temperature", temperature);

        return extractOpenAiResponse(send(apiUrl, body, headers));
    }

    @SuppressWarnings("unchecked")
    private String extractOpenAiResponse(Map<String, Object> response) {
        try {
            List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
            if (choices == null || choices.isEmpty()) {
                throw new RuntimeException("Aucun choix dans la réponse OpenAI");
            }

            Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
            if (message == null) {
                throw new RuntimeException("Format de réponse OpenAI invalide: message manquant");
            }

            return (String) message.get("content");
        } catch (Exception e) {
            log.error("❌ Erreur lors de l'extraction de la réponse OpenAI. Structure reçue: {}", response);
            throw new RuntimeException("Format de réponse OpenAI invalide", e);
        }
    }

    // =============================
    // OLLAMA (Local)
    // =============================
    private String callOllama(String prompt) {
        String fullUrl = ollamaBaseUrl + ollamaApiPath;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> body = new HashMap<>();
        body.put("model", model);
        body.put("prompt", prompt);
        body.put("stream", false);
        body.put("options", Map.of(
                "temperature", temperature,
                "num_predict", maxTokens
        ));

        log.debug("🌐 Appel Ollama à: {}", fullUrl);
        log.debug("📝 Prompt: {}", prompt.substring(0, Math.min(200, prompt.length())) + "...");

        try {
            return extractOllamaResponse(send(fullUrl, body, headers));
        } catch (ResourceAccessException e) {
            log.error("🔌 Impossible de se connecter à Ollama. Vérifiez que le service est démarré à {}", ollamaBaseUrl);
            throw new RuntimeException("Ollama n'est pas démarré ou inaccessible", e);
        }
    }

    @SuppressWarnings("unchecked")
    private String extractOllamaResponse(Map<String, Object> response) {
        try {
            String responseText = (String) response.get("response");
            if (responseText == null || responseText.trim().isEmpty()) {
                throw new RuntimeException("Réponse Ollama vide");
            }

            log.debug("📨 Réponse Ollama reçue: {} caractères", responseText.length());
            return responseText;
        } catch (Exception e) {
            log.error("❌ Erreur lors de l'extraction de la réponse Ollama. Structure reçue: {}", response);
            throw new RuntimeException("Format de réponse Ollama invalide", e);
        }
    }

    // =============================
    // COMMUNICATION HTTP
    // =============================
    private Map<String, Object> send(String url, Map<String, Object> body, HttpHeaders headers) {
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
        log.debug("🌐 Envoi HTTP POST à: {}", url);

        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, entity, Map.class);

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("Réponse API invalide, code HTTP : " + response.getStatusCode());
        }

        if (response.getBody() == null) {
            throw new RuntimeException("Réponse API vide");
        }

        return response.getBody();
    }

    // =============================
    // MODE SIMULATION / STATUS
    // =============================
    private boolean shouldSimulate() {
        return "enabled".equalsIgnoreCase(simulationMode) || forceSimulation;
    }

    private String simulateAiResponse(String prompt) {
        log.warn("🎭 MODE SIMULATION : Génération d'une réponse de test");

        // Simuler un délai d'analyse
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        return """
        {
          "issues": [
            {
              "issueType": "Conformité",
              "description": "Document analysé en mode simulation. Ceci est une réponse de test pour vérifier le fonctionnement du système.",
              "pageNumber": 1,
              "paragraphNumber": 1,
              "suggestion": "Pour une analyse réelle, vérifiez la connexion à l'API IA et désactivez le mode simulation."
            },
            {
              "issueType": "Structure",
              "description": "Format du document non vérifié en mode simulation.",
              "pageNumber": 1,
              "paragraphNumber": 2,
              "suggestion": "Assurez-vous que le document respecte les normes requises."
            }
          ]
        }
        """;
    }

    /**
     * Test de connexion à l'API IA
     */
    public boolean testConnection() {
        try {
            String testPrompt = "Test de connexion. Réponds simplement par 'OK'.";
            String response = sendRequest(testPrompt);
            return response != null && response.contains("OK");
        } catch (Exception e) {
            log.error("❌ Test de connexion IA échoué: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Obtenir le statut de l'API IA
     */
    public Map<String, Object> getStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("provider", provider);
        status.put("model", model);
        status.put("simulationMode", simulationMode);
        status.put("forceSimulation", forceSimulation);

        try {
            status.put("connected", testConnection());
        } catch (Exception e) {
            status.put("connected", false);
            status.put("error", e.getMessage());
        }

        return status;
    }

    /**
     * Réinitialiser le mode simulation
     */
    public void resetSimulation() {
        forceSimulation = false;
        log.info("🔄 Mode simulation réinitialisé");
    }
}