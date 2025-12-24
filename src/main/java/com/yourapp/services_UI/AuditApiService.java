package com.yourapp.services_UI;

import com.yourapp.dto.AuditCreateRequestDto;
import com.yourapp.dto.AuditResponseDto;
import com.yourapp.dto.AuditIssueDto;
import com.yourapp.services.AuditService;
import com.yourapp.services.AuditIssueService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Service UI pour les opérations d'audit
 * Sert d'intermédiaire entre le contrôleur JavaFX et le service backend
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuditApiService {

    // Injection directe des services backend
    private final AuditService auditService;
    private final AuditIssueService auditIssueService;

    /**
     * Créer un nouvel audit (appel direct au service)
     */
    public AuditResponseDto createAudit(AuditCreateRequestDto request) {
        log.info("Création d'un audit pour le projet ID: {}", request.getProjectId());

        try {
            AuditResponseDto audit = auditService.createAudit(request);
            log.info("✅ Audit créé avec succès. ID: {}", audit.getId());
            return audit;
        } catch (Exception e) {
            log.error("❌ Erreur lors de la création de l'audit", e);
            throw new RuntimeException("Impossible de créer l'audit: " + e.getMessage(), e);
        }
    }

    /**
     * Lancer l'analyse d'un audit
     */
    public AuditResponseDto startAnalysis(Long auditId) {
        log.info("Démarrage de l'analyse pour l'audit ID: {}", auditId);

        try {
            AuditResponseDto audit = auditService.startAnalysis(auditId);
            log.info("✅ Analyse lancée avec succès pour l'audit: {}", auditId);
            return audit;
        } catch (Exception e) {
            log.error("❌ Erreur lors du démarrage de l'analyse", e);
            throw new RuntimeException("Impossible de démarrer l'analyse: " + e.getMessage(), e);
        }
    }

    /**
     * Récupérer un audit par son ID
     */
    public AuditResponseDto getAuditById(Long auditId) {
        log.info("Récupération de l'audit ID: {}", auditId);

        try {
            AuditResponseDto audit = auditService.getAuditById(auditId);
            if (audit != null) {
                log.info("✅ Audit récupéré: Status = {}", audit.getStatus());
            } else {
                log.warn("⚠️ Audit non trouvé: {}", auditId);
            }
            return audit;
        } catch (Exception e) {
            log.error("❌ Erreur lors de la récupération de l'audit", e);
            throw new RuntimeException("Impossible de récupérer l'audit: " + e.getMessage(), e);
        }
    }

    /**
     * 🔥 NOUVELLE MÉTHODE: Récupérer les issues d'un audit
     */
    public List<AuditIssueDto> getIssuesByAudit(Long auditId) {
        log.info("📊 Récupération des issues pour l'audit ID: {}", auditId);

        try {
            List<AuditIssueDto> issues = auditIssueService.getIssuesByAudit(auditId);
            log.info("✅ {} issues récupérées pour l'audit {}", issues.size(), auditId);
            return issues;
        } catch (Exception e) {
            log.error("❌ Erreur lors de la récupération des issues", e);
            throw new RuntimeException("Impossible de récupérer les issues: " + e.getMessage(), e);
        }
    }

    /**
     * Récupérer tous les audits d'un projet
     */
    public List<AuditResponseDto> getAuditsByProject(Long projectId) {
        log.info("Récupération des audits du projet ID: {}", projectId);

        try {
            List<AuditResponseDto> audits = auditService.getAuditsByProject(projectId);
            log.info("✅ {} audits récupérés pour le projet", audits.size());
            return audits;
        } catch (Exception e) {
            log.error("❌ Erreur lors de la récupération des audits du projet", e);
            throw new RuntimeException("Impossible de récupérer les audits", e);
        }
    }

    /**
     * Supprimer un audit
     */
    public boolean deleteAudit(Long auditId) {
        log.info("Suppression de l'audit ID: {}", auditId);

        try {
            auditService.deleteAudit(auditId);
            log.info("✅ Audit supprimé avec succès");
            return true;
        } catch (Exception e) {
            log.error("❌ Erreur lors de la suppression de l'audit", e);
            return false;
        }
    }

    /**
     * Récupérer les statistiques d'un audit
     */
    public Map<String, Object> getAuditStatistics(Long auditId) {
        log.info("Récupération des statistiques de l'audit ID: {}", auditId);

        try {
            Map<String, Object> stats = auditService.getAuditStatistics(auditId);
            log.info("✅ Statistiques récupérées pour l'audit");
            return stats;
        } catch (Exception e) {
            log.error("❌ Erreur lors de la récupération des statistiques", e);
            return Map.of("error", e.getMessage());
        }
    }

    /**
     * Polling pour vérifier le statut d'un audit en cours
     * Version avec délai augmenté pour l'analyse IA longue
     */
    public AuditResponseDto pollAuditStatus(Long auditId) {
        return pollAuditStatus(auditId, 120, 2); // 120 tentatives × 2s = 4 minutes
    }

    /**
     * Polling pour vérifier le statut d'un audit en cours (version paramétrable)
     */
    public AuditResponseDto pollAuditStatus(Long auditId, int maxAttempts, int intervalSeconds) {
        log.info("🔍 Polling du statut de l'audit ID: {} (maxAttempts={}, intervalSeconds={})",
                auditId, maxAttempts, intervalSeconds);

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                AuditResponseDto audit = getAuditById(auditId);

                if (audit == null) {
                    log.warn("⚠️ Audit {} non trouvé lors du polling", auditId);
                    Thread.sleep(intervalSeconds * 1000L);
                    continue;
                }

                String status = audit.getStatus();
                log.debug("⏳ Tentative {}/{}: Statut actuel = {}", attempt, maxAttempts, status);

                // Vérifier si l'analyse est terminée
                if ("COMPLETED".equals(status) || "FAILED".equals(status) || "ERROR".equals(status)) {
                    log.info("✅ Audit {} terminé avec le statut: {}", auditId, status);

                    // Récupérer les issues seulement si l'audit est COMPLETED
                    if ("COMPLETED".equals(status)) {
                        try {
                            List<AuditIssueDto> issues = getIssuesByAudit(auditId);
                            audit.setIssues(issues);
                            log.info("📊 {} issues ajoutées à la réponse", issues.size());
                        } catch (Exception e) {
                            log.warn("⚠️ Impossible de récupérer les issues pour l'audit {}", auditId, e);
                        }
                    }

                    return audit;
                }

                // Si l'analyse est toujours en cours, attendre avant de réessayer
                if (attempt < maxAttempts) {
                    Thread.sleep(intervalSeconds * 1000L);
                }

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.error("❌ Polling interrompu pour l'audit {}", auditId, e);
                throw new RuntimeException("Polling interrompu", e);
            } catch (Exception e) {
                log.warn("⚠️ Erreur lors du polling (tentative {}): {}", attempt, e.getMessage());

                // Attendre avant de réessayer en cas d'erreur
                try {
                    Thread.sleep(intervalSeconds * 1000L);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Polling interrompu", ie);
                }
            }
        }

        log.warn("⏱️ Timeout: L'audit {} n'a pas terminé dans le délai imparti ({} secondes)",
                auditId, maxAttempts * intervalSeconds);

        // Récupérer l'état actuel avant de lancer l'exception
        try {
            AuditResponseDto audit = getAuditById(auditId);
            if (audit != null) {
                log.info("📊 Statut final: {} - Problèmes détectés: {}",
                        audit.getStatus(), audit.getProblemsCount());
            }
        } catch (Exception e) {
            log.warn("⚠️ Impossible de récupérer le statut final", e);
        }

        throw new RuntimeException(String.format(
                "Timeout: L'audit n'a pas terminé dans le délai imparti (%d secondes). " +
                        "L'analyse IA est peut-être en cours, veuillez patienter.",
                maxAttempts * intervalSeconds));
    }

    /**
     * Version améliorée du polling avec gestion d'erreur et retry
     */
    public AuditResponseDto pollAuditStatusWithRetry(Long auditId) {
        int maxRetries = 3;
        for (int retry = 1; retry <= maxRetries; retry++) {
            try {
                return pollAuditStatus(auditId);
            } catch (Exception e) {
                log.warn("⚠️ Échec du polling (tentative {}/{}): {}", retry, maxRetries, e.getMessage());

                if (retry == maxRetries) {
                    throw e;
                }

                try {
                    Thread.sleep(5000); // Attendre 5s avant de réessayer
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Polling interrompu", ie);
                }
            }
        }

        throw new RuntimeException("Toutes les tentatives de polling ont échoué");
    }
}