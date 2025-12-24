package com.yourapp.utils;

import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;
import java.util.Map;

/**
 * Utilitaire pour tester la validité de votre clé API OpenAI
 * Exécutez ce fichier pour vérifier si votre clé fonctionne
 */
public class TestOpenAIKey {

    public static void main(String[] args) {
        // 🔑 VOTRE CLÉ API ICI
        String apiKey = "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";
        String apiUrl = "https://api.openai.com/v1/chat/completions";

        System.out.println("========================================");
        System.out.println("🔍 TEST DE LA CLÉ API OPENAI");
        System.out.println("========================================");
        System.out.println("🔑 Clé API: " + apiKey.substring(0, 20) + "...");
        System.out.println("🌐 URL: " + apiUrl);
        System.out.println("========================================\n");

        try {
            RestTemplate restTemplate = new RestTemplate();

            // Créer la requête
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);

            Map<String, Object> requestBody = Map.of(
                    "model", "gpt-3.5-turbo",
                    "messages", new Object[]{
                            Map.of("role", "user", "content", "Dis juste 'OK'")
                    },
                    "max_tokens", 10
            );

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            System.out.println("📤 Envoi de la requête de test...\n");

            // Envoyer la requête
            ResponseEntity<Map> response = restTemplate.exchange(
                    apiUrl,
                    HttpMethod.POST,
                    entity,
                    Map.class
            );

            // Analyser la réponse
            if (response.getStatusCode() == HttpStatus.OK) {
                System.out.println("✅ SUCCÈS ! Votre clé API fonctionne correctement");
                System.out.println("📊 Réponse reçue: " + response.getBody());
                System.out.println("\n✨ Vous pouvez utiliser cette clé dans votre application");
            }

        } catch (org.springframework.web.client.HttpClientErrorException.Unauthorized e) {
            System.err.println("❌ ERREUR 401 - CLÉ API INVALIDE");
            System.err.println("🔴 Votre clé API est invalide ou a expiré");
            System.err.println("\n📝 SOLUTIONS :");
            System.err.println("1. Allez sur https://platform.openai.com/api-keys");
            System.err.println("2. Créez une NOUVELLE clé API");
            System.err.println("3. Remplacez l'ancienne clé dans application.yml");
            System.err.println("4. Assurez-vous d'avoir des crédits sur votre compte OpenAI");

        } catch (org.springframework.web.client.HttpClientErrorException.TooManyRequests e) {
            System.err.println("❌ ERREUR 429 - QUOTA DÉPASSÉ");
            System.err.println("🔴 Vous avez dépassé votre quota ou limite de taux");
            System.err.println("\n📝 SOLUTIONS :");
            System.err.println("1. Vérifiez votre usage sur https://platform.openai.com/usage");
            System.err.println("2. Ajoutez des crédits à votre compte");
            System.err.println("3. Attendez quelques minutes avant de réessayer");

        } catch (Exception e) {
            System.err.println("❌ ERREUR : " + e.getMessage());
            e.printStackTrace();
        }

        System.out.println("\n========================================");
    }
}