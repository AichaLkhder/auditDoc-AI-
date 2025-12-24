package com.yourapp.services_UI;

import com.itextpdf.text.Document;
import com.yourapp.dto.AuditResponseDto;
import com.yourapp.model.Audit;
import com.yourapp.model.AuditReport;
import com.yourapp.DAO.AuditReportRepository;
import com.yourapp.DAO.AuditRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfWriter;
import org.apache.poi.xwpf.usermodel.*;

/**
 * Service pour la génération de rapports d'audit avec sauvegarde en BDD
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ReportService {

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final AuditReportRepository reportRepository;
    private final AuditRepository auditRepository;

    /**
     * Générer un rapport texte ET le sauvegarder en BDD
     */
    @Transactional
    public void generateAndSaveTextReport(AuditResponseDto auditDto, File file) throws IOException {
        log.info("📄 Génération du rapport texte pour l'audit {}", auditDto.getId());

        // Générer le contenu du rapport
        String reportContent = generateTextReportContent(auditDto);

        // Écrire dans le fichier
        try (FileWriter writer = new FileWriter(file)) {
            writer.write(reportContent);
        }

        // Sauvegarder en BDD
        saveReportToDatabase(auditDto, file.getAbsolutePath(), reportContent, "TXT");

        log.info("✅ Rapport texte généré et sauvegardé: {}", file.getAbsolutePath());
    }

    /**
     * Générer un rapport HTML ET le sauvegarder en BDD
     */
    @Transactional
    public void generateAndSaveHtmlReport(AuditResponseDto auditDto, File file) throws IOException {
        log.info("🌐 Génération du rapport HTML pour l'audit {}", auditDto.getId());

        // Générer le contenu HTML
        String htmlContent = generateHtmlReportContent(auditDto);

        // Écrire dans le fichier
        try (FileWriter writer = new FileWriter(file)) {
            writer.write(htmlContent);
        }

        // Créer un résumé pour la BDD (sans le HTML complet)
        String summary = generateSummaryForDatabase(auditDto);

        // Sauvegarder en BDD
        saveReportToDatabase(auditDto, file.getAbsolutePath(), summary, "HTML");

        log.info("✅ Rapport HTML généré et sauvegardé: {}", file.getAbsolutePath());
    }

    /**
     * Générer un rapport PDF ET le sauvegarder en BDD
     */
    @Transactional
    public void generateAndSavePdfReport(AuditResponseDto auditDto, File file) throws IOException {
        log.info("📕 Génération du rapport PDF pour l'audit {}", auditDto.getId());

        try {
            Document document = new Document();
            PdfWriter.getInstance(document, new FileOutputStream(file));
            document.open();

            // Titre
            Font titleFont = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD);
            Paragraph title = new Paragraph("Rapport d'Audit Documentaire", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(20);
            document.add(title);

            // Informations générales
            Font headerFont = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD);
            Font contentFont = new Font(Font.FontFamily.HELVETICA, 10, Font.NORMAL);

            document.add(new Paragraph("Informations Générales:", headerFont));
            document.add(new Paragraph("Projet: " + auditDto.getProjectName(), contentFont));
            document.add(new Paragraph("Modèle: " + auditDto.getModelName(), contentFont));
            document.add(new Paragraph("Date: " + LocalDateTime.now().format(DATE_FORMATTER), contentFont));
            document.add(new Paragraph("Statut: " + auditDto.getStatus(), contentFont));

            int docsCount = auditDto.getDocuments() != null ? auditDto.getDocuments().size() : 0;
            int issuesCount = auditDto.getIssues() != null ? auditDto.getIssues().size() : 0;

            document.add(new Paragraph("Documents analysés: " + docsCount, contentFont));
            document.add(new Paragraph("Problèmes détectés: " + issuesCount, contentFont));
            document.add(new Paragraph("Score: " + calculateScore(auditDto) + "/100", contentFont));

            document.add(new Paragraph("\n"));

            // Problèmes détectés
            if (issuesCount > 0) {
                document.add(new Paragraph("Problèmes Identifiés:", headerFont));
                int i = 1;
                for (var issue : auditDto.getIssues()) {
                    document.add(new Paragraph(i + ". " + issue.getIssueType(), contentFont));
                    if (issue.getLocation() != null && !issue.getLocation().isEmpty()) {
                        document.add(new Paragraph("   Localisation: " + issue.getLocation(), contentFont));
                    }
                    document.add(new Paragraph("   Description: " + issue.getDescription(), contentFont));
                    if (issue.getSuggestion() != null && !issue.getSuggestion().isEmpty()) {
                        document.add(new Paragraph("   Recommandation: " + issue.getSuggestion(), contentFont));
                    }
                    document.add(new Paragraph(" "));
                    i++;
                }
            } else {
                document.add(new Paragraph("✅ Aucun problème détecté", contentFont));
            }

            // Pied de page
            document.add(new Paragraph("\n\n"));
            Paragraph footer = new Paragraph(
                    "Généré par AuditDoc AI • " + LocalDateTime.now().format(DATE_FORMATTER),
                    new Font(Font.FontFamily.HELVETICA, 8, Font.ITALIC)
            );
            footer.setAlignment(Element.ALIGN_CENTER);
            document.add(footer);

            document.close();

            // Sauvegarder en BDD
            String summary = generateSummaryForDatabase(auditDto);
            saveReportToDatabase(auditDto, file.getAbsolutePath(), summary, "PDF");

            log.info("✅ Rapport PDF généré et sauvegardé: {}", file.getAbsolutePath());

        } catch (DocumentException e) {
            log.error("❌ Erreur lors de la génération du PDF", e);
            throw new IOException("Erreur lors de la génération du PDF", e);
        }
    }

    /**
     * Générer un rapport Word ET le sauvegarder en BDD
     */
    @Transactional
    public void generateAndSaveWordReport(AuditResponseDto auditDto, File file) throws IOException {
        log.info("📝 Génération du rapport Word pour l'audit {}", auditDto.getId());

        try (XWPFDocument document = new XWPFDocument()) {

            // Titre
            XWPFParagraph titleParagraph = document.createParagraph();
            titleParagraph.setAlignment(ParagraphAlignment.CENTER);
            XWPFRun titleRun = titleParagraph.createRun();
            titleRun.setText("Rapport d'Audit Documentaire");
            titleRun.setBold(true);
            titleRun.setFontSize(16);
            titleRun.addBreak();
            titleRun.setText(" ");
            titleRun.addBreak();

            // Informations générales
            XWPFParagraph infoParagraph = document.createParagraph();
            XWPFRun infoRun = infoParagraph.createRun();
            infoRun.setText("Informations Générales");
            infoRun.setBold(true);
            infoRun.setFontSize(12);
            infoRun.addBreak();

            int docsCount = auditDto.getDocuments() != null ? auditDto.getDocuments().size() : 0;
            int issuesCount = auditDto.getIssues() != null ? auditDto.getIssues().size() : 0;

            infoRun.setText("• Projet: " + auditDto.getProjectName());
            infoRun.addBreak();
            infoRun.setText("• Modèle: " + auditDto.getModelName());
            infoRun.addBreak();
            infoRun.setText("• Date: " + LocalDateTime.now().format(DATE_FORMATTER));
            infoRun.addBreak();
            infoRun.setText("• Statut: " + auditDto.getStatus());
            infoRun.addBreak();
            infoRun.setText("• Documents analysés: " + docsCount);
            infoRun.addBreak();
            infoRun.setText("• Problèmes détectés: " + issuesCount);
            infoRun.addBreak();
            infoRun.setText("• Score: " + calculateScore(auditDto) + "/100");
            infoRun.addBreak();
            infoRun.addBreak();

            // Problèmes détectés
            if (issuesCount > 0) {
                XWPFParagraph issuesTitleParagraph = document.createParagraph();
                XWPFRun issuesTitleRun = issuesTitleParagraph.createRun();
                issuesTitleRun.setText("Problèmes Identifiés");
                issuesTitleRun.setBold(true);
                issuesTitleRun.setFontSize(12);
                issuesTitleRun.addBreak();

                int i = 1;
                for (var issue : auditDto.getIssues()) {
                    XWPFParagraph issueParagraph = document.createParagraph();
                    XWPFRun issueRun = issueParagraph.createRun();
                    issueRun.setText(i + ". " + issue.getIssueType());
                    issueRun.setBold(true);
                    issueRun.addBreak();

                    if (issue.getLocation() != null && !issue.getLocation().isEmpty()) {
                        issueRun.setText("   Localisation: " + issue.getLocation());
                        issueRun.addBreak();
                    }

                    issueRun.setText("   Description: " + issue.getDescription());
                    issueRun.addBreak();

                    if (issue.getSuggestion() != null && !issue.getSuggestion().isEmpty()) {
                        issueRun.setText("   Recommandation: " + issue.getSuggestion());
                        issueRun.addBreak();
                    }

                    issueRun.addBreak();
                    i++;
                }
            } else {
                XWPFParagraph noIssuesParagraph = document.createParagraph();
                XWPFRun noIssuesRun = noIssuesParagraph.createRun();
                noIssuesRun.setText("✅ Aucun problème détecté");
                noIssuesRun.setColor("008000");
                noIssuesRun.addBreak();
            }

            // Pied de page
            XWPFParagraph footerParagraph = document.createParagraph();
            footerParagraph.setAlignment(ParagraphAlignment.CENTER);
            XWPFRun footerRun = footerParagraph.createRun();
            footerRun.setText("Généré par AuditDoc AI • " + LocalDateTime.now().format(DATE_FORMATTER));
            footerRun.setItalic(true);
            footerRun.setFontSize(8);

            // Sauvegarder le document
            try (FileOutputStream out = new FileOutputStream(file)) {
                document.write(out);
            }

            // Sauvegarder en BDD
            String summary = generateSummaryForDatabase(auditDto);
            saveReportToDatabase(auditDto, file.getAbsolutePath(), summary, "DOCX");

            log.info("✅ Rapport Word généré et sauvegardé: {}", file.getAbsolutePath());

        } catch (Exception e) {
            log.error("❌ Erreur lors de la génération du document Word", e);
            throw new IOException("Erreur lors de la génération du document Word", e);
        }
    }

    /**
     * Sauvegarder le rapport dans la base de données
     */
    @Transactional
    public void saveReportToDatabase(AuditResponseDto auditDto, String filePath,
                                     String summary, String format) {
        log.info("💾 Sauvegarde du rapport en BDD pour l'audit {}", auditDto.getId());

        // Récupérer l'entité Audit depuis la BDD
        Audit audit = auditRepository.findById(auditDto.getId())
                .orElseThrow(() -> new RuntimeException("Audit introuvable: " + auditDto.getId()));

        // Créer l'entité AuditReport
        AuditReport report = new AuditReport();
        report.setAudit(audit);
        report.setReportPath(filePath);
        report.setReportSummary(summary);
        report.setScore(calculateScore(auditDto));
        report.setProblemsCount(auditDto.getIssues() != null ? auditDto.getIssues().size() : 0);

        // Sauvegarder en BDD
        reportRepository.save(report);

        log.info("✅ Rapport sauvegardé en BDD avec ID: {}", report.getId());
    }

    /**
     * Calculer le score de conformité
     */
    private Integer calculateScore(AuditResponseDto audit) {
        int totalIssues = audit.getIssues() != null ? audit.getIssues().size() : 0;

        if (totalIssues == 0) {
            return 100; // Parfait
        } else if (totalIssues <= 3) {
            return 80; // Bon
        } else if (totalIssues <= 7) {
            return 60; // Moyen
        } else {
            return 40; // À améliorer
        }
    }

    /**
     * Générer le contenu du rapport texte
     */
    private String generateTextReportContent(AuditResponseDto audit) {
        StringBuilder report = new StringBuilder();

        appendHeader(report, audit);
        appendSummary(report, audit);
        appendDocuments(report, audit);
        appendProblems(report, audit);
        appendRecommendations(report, audit);
        appendFooter(report);

        return report.toString();
    }

    /**
     * Générer un résumé pour la base de données
     */
    private String generateSummaryForDatabase(AuditResponseDto audit) {
        int docsCount = audit.getDocuments() != null ? audit.getDocuments().size() : 0;
        int issuesCount = audit.getIssues() != null ? audit.getIssues().size() : 0;

        return String.format(
                "Rapport d'audit généré pour le projet '%s'. " +
                        "%d document(s) analysé(s), %d problème(s) détecté(s). " +
                        "Date: %s",
                audit.getProjectName(),
                docsCount,
                issuesCount,
                LocalDateTime.now().format(DATE_FORMATTER)
        );
    }

    /**
     * Générer le nom de fichier par défaut
     */
    public String getDefaultFileName(AuditResponseDto audit, String extension) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String projectName = audit.getProjectName().replaceAll("[^a-zA-Z0-9]", "_");
        return String.format("Rapport_%s_%d_%s.%s", projectName, audit.getId(), timestamp, extension);
    }

    // ========== Méthodes de génération de contenu ==========

    private void appendHeader(StringBuilder report, AuditResponseDto audit) {
        report.append("═══════════════════════════════════════════════════════════\n");
        report.append("           RAPPORT D'AUDIT DOCUMENTAIRE\n");
        report.append("═══════════════════════════════════════════════════════════\n\n");
    }

    private void appendSummary(StringBuilder report, AuditResponseDto audit) {
        report.append("📊 INFORMATIONS GÉNÉRALES\n");
        report.append("─────────────────────────────────────────────────────────\n");
        report.append("Projet        : ").append(audit.getProjectName()).append("\n");
        report.append("Modèle        : ").append(audit.getModelName()).append("\n");
        report.append("Date          : ").append(LocalDateTime.now().format(DATE_FORMATTER)).append("\n");
        report.append("Statut        : ").append(audit.getStatus()).append("\n");
        report.append("Documents     : ").append(audit.getDocuments() != null ? audit.getDocuments().size() : 0).append("\n");
        report.append("Problèmes     : ").append(audit.getIssues() != null ? audit.getIssues().size() : 0).append("\n");
        report.append("Score         : ").append(calculateScore(audit)).append("/100\n\n");
    }

    private void appendDocuments(StringBuilder report, AuditResponseDto audit) {
        if (audit.getDocuments() != null && !audit.getDocuments().isEmpty()) {
            report.append("📄 DOCUMENTS ANALYSÉS\n");
            report.append("─────────────────────────────────────────────────────────\n");
            for (var doc : audit.getDocuments()) {
                report.append("• ").append(doc.getFileName()).append("\n");
                report.append("  Statut: ").append(doc.getStatus()).append("\n");
                if (doc.getIssuesCount() != null) {
                    report.append("  Problèmes: ").append(doc.getIssuesCount()).append("\n");
                }
                report.append("\n");
            }
        }
    }

    private void appendProblems(StringBuilder report, AuditResponseDto audit) {
        if (audit.getIssues() != null && !audit.getIssues().isEmpty()) {
            report.append("\n🔴 PROBLÈMES IDENTIFIÉS\n");
            report.append("═══════════════════════════════════════════════════════════\n\n");

            int problemNumber = 1;
            for (var issue : audit.getIssues()) {
                report.append("PROBLÈME #").append(problemNumber++).append("\n");
                report.append("─────────────────────────────────────────────────────────\n");
                report.append("Type          : ").append(issue.getIssueType() != null ? issue.getIssueType() : "Non spécifié").append("\n");

                if (issue.getLocation() != null && !issue.getLocation().isEmpty()) {
                    report.append("Localisation  : ").append(issue.getLocation()).append("\n");
                }

                if (issue.getDocumentName() != null) {
                    report.append("Document      : ").append(issue.getDocumentName()).append("\n");
                }

                report.append("\nDescription:\n");
                report.append(issue.getDescription() != null ? issue.getDescription() : "Aucune description").append("\n");

                report.append("\n\n");
            }
        } else {
            report.append("\n✅ AUCUN PROBLÈME DÉTECTÉ\n");
            report.append("─────────────────────────────────────────────────────────\n");
            report.append("Tous les documents analysés sont conformes.\n\n");
        }
    }

    private void appendRecommendations(StringBuilder report, AuditResponseDto audit) {
        report.append("\n💡 RECOMMANDATIONS\n");
        report.append("═══════════════════════════════════════════════════════════\n\n");

        if (audit.getIssues() != null && !audit.getIssues().isEmpty()) {
            int recoNumber = 1;
            boolean hasRecommendations = false;

            for (var issue : audit.getIssues()) {
                if (issue.getSuggestion() != null && !issue.getSuggestion().isEmpty()) {
                    hasRecommendations = true;
                    report.append("RECOMMANDATION #").append(recoNumber++).append("\n");
                    report.append("─────────────────────────────────────────────────────────\n");
                    report.append("Concernant    : ").append(issue.getIssueType()).append("\n");
                    report.append("\nSuggestion:\n");
                    report.append(issue.getSuggestion()).append("\n\n\n");
                }
            }

            if (!hasRecommendations) {
                report.append("Aucune recommandation spécifique.\n\n");
            }
        } else {
            report.append("Aucune recommandation nécessaire. Continuez votre excellent travail!\n\n");
        }
    }

    private void appendFooter(StringBuilder report) {
        report.append("\n═══════════════════════════════════════════════════════════\n");
        report.append("           Généré par AuditDoc AI\n");
        report.append("           ").append(LocalDateTime.now().format(DATE_FORMATTER)).append("\n");
        report.append("═══════════════════════════════════════════════════════════\n");
    }

    /**
     * Générer le contenu HTML
     */
    private String generateHtmlReportContent(AuditResponseDto audit) {
        StringBuilder html = new StringBuilder();

        html.append("<!DOCTYPE html>\n<html lang='fr'>\n<head>\n");
        html.append("    <meta charset='UTF-8'>\n");
        html.append("    <meta name='viewport' content='width=device-width, initial-scale=1.0'>\n");
        html.append("    <title>Rapport d'Audit - ").append(escapeHtml(audit.getProjectName())).append("</title>\n");
        html.append("    <style>").append(getHtmlStyles()).append("</style>\n");
        html.append("</head>\n<body>\n");
        html.append("    <div class='container'>\n");

        // En-tête
        html.append("        <div class='header'>\n");
        html.append("            <h1>📊 Rapport d'Audit Documentaire</h1>\n");
        html.append("            <p class='date'>Généré le ").append(LocalDateTime.now().format(DATE_FORMATTER)).append("</p>\n");
        html.append("        </div>\n");

        // Score
        int score = calculateScore(audit);
        String scoreClass = score >= 80 ? "score-good" : (score >= 60 ? "score-medium" : "score-bad");
        html.append("        <div class='score-box ").append(scoreClass).append("'>\n");
        html.append("            <h2>Score de Conformité</h2>\n");
        html.append("            <div class='score'>").append(score).append("<span>/100</span></div>\n");
        html.append("        </div>\n");

        // Résumé
        html.append("        <div class='summary'>\n");
        html.append("            <h2>Résumé</h2>\n");
        html.append("            <div class='info-grid'>\n");
        html.append("                <div class='info-item'><strong>Projet:</strong> ").append(escapeHtml(audit.getProjectName())).append("</div>\n");
        html.append("                <div class='info-item'><strong>Modèle:</strong> ").append(escapeHtml(audit.getModelName())).append("</div>\n");
        html.append("                <div class='info-item'><strong>Statut:</strong> <span class='badge ").append(audit.getStatus().toLowerCase()).append("'>").append(audit.getStatus()).append("</span></div>\n");
        html.append("                <div class='info-item'><strong>Documents:</strong> ").append(audit.getDocuments() != null ? audit.getDocuments().size() : 0).append("</div>\n");
        html.append("                <div class='info-item'><strong>Problèmes:</strong> <span class='badge-danger'>").append(audit.getIssues() != null ? audit.getIssues().size() : 0).append("</span></div>\n");
        html.append("            </div>\n");
        html.append("        </div>\n");

        // Problèmes
        if (audit.getIssues() != null && !audit.getIssues().isEmpty()) {
            html.append("        <div class='section'>\n");
            html.append("            <h2>🔴 Problèmes Identifiés</h2>\n");

            int problemNumber = 1;
            for (var issue : audit.getIssues()) {
                html.append("            <div class='problem-card'>\n");
                html.append("                <div class='problem-header'>\n");
                html.append("                    <span class='problem-number'>#").append(problemNumber++).append("</span>\n");
                html.append("                    <span class='problem-type'>").append(escapeHtml(issue.getIssueType())).append("</span>\n");
                html.append("                </div>\n");

                if (issue.getLocation() != null && !issue.getLocation().isEmpty()) {
                    html.append("                <div class='problem-location'>📍 ").append(escapeHtml(issue.getLocation())).append("</div>\n");
                }

                html.append("                <div class='problem-description'>").append(escapeHtml(issue.getDescription())).append("</div>\n");
                html.append("            </div>\n");
            }

            html.append("        </div>\n");
        }

        // Recommandations
        if (audit.getIssues() != null && !audit.getIssues().isEmpty()) {
            boolean hasRecommendations = audit.getIssues().stream()
                    .anyMatch(i -> i.getSuggestion() != null && !i.getSuggestion().isEmpty());

            if (hasRecommendations) {
                html.append("        <div class='section'>\n");
                html.append("            <h2>💡 Recommandations</h2>\n");

                int recoNumber = 1;
                for (var issue : audit.getIssues()) {
                    if (issue.getSuggestion() != null && !issue.getSuggestion().isEmpty()) {
                        html.append("            <div class='recommendation-card'>\n");
                        html.append("                <div class='recommendation-header'>\n");
                        html.append("                    <span class='recommendation-number'>#").append(recoNumber++).append("</span>\n");
                        html.append("                    <span class='recommendation-title'>").append(escapeHtml(issue.getIssueType())).append("</span>\n");
                        html.append("                </div>\n");
                        html.append("                <div class='recommendation-text'>").append(escapeHtml(issue.getSuggestion())).append("</div>\n");
                        html.append("            </div>\n");
                    }
                }

                html.append("        </div>\n");
            }
        }

        // Pied de page
        html.append("        <div class='footer'>\n");
        html.append("            <p>Généré par <strong>AuditDoc AI</strong> - ").append(LocalDateTime.now().format(DATE_FORMATTER)).append("</p>\n");
        html.append("        </div>\n");

        html.append("    </div>\n</body>\n</html>\n");

        return html.toString();
    }

    private String getHtmlStyles() {
        return """
            * { margin: 0; padding: 0; box-sizing: border-box; }
            body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; background: #f3f4f6; padding: 20px; }
            .container { max-width: 1000px; margin: 0 auto; background: white; padding: 40px; border-radius: 12px; box-shadow: 0 4px 6px rgba(0,0,0,0.1); }
            .header { text-align: center; margin-bottom: 30px; border-bottom: 3px solid #1E88E5; padding-bottom: 20px; }
            .header h1 { color: #1f2937; font-size: 32px; margin-bottom: 10px; }
            .date { color: #6b7280; font-size: 14px; }
            .score-box { text-align: center; padding: 30px; border-radius: 12px; margin-bottom: 30px; }
            .score-good { background: linear-gradient(135deg, #10b981, #059669); color: white; }
            .score-medium { background: linear-gradient(135deg, #f59e0b, #d97706); color: white; }
            .score-bad { background: linear-gradient(135deg, #ef4444, #dc2626); color: white; }
            .score { font-size: 64px; font-weight: 700; }
            .score span { font-size: 32px; opacity: 0.8; }
            .summary { background: #f0f9ff; padding: 20px; border-radius: 10px; margin-bottom: 30px; border-left: 4px solid #1E88E5; }
            .summary h2 { color: #1f2937; margin-bottom: 15px; }
            .info-grid { display: grid; grid-template-columns: repeat(2, 1fr); gap: 12px; }
            .info-item { padding: 10px; background: white; border-radius: 6px; }
            .badge { padding: 4px 12px; border-radius: 12px; font-size: 12px; font-weight: 600; }
            .badge.completed { background: #d1fae5; color: #065f46; }
            .badge-danger { background: #fee2e2; color: #dc2626; padding: 4px 12px; border-radius: 12px; font-size: 12px; font-weight: 600; }
            .section { margin-bottom: 40px; }
            .section h2 { color: #1f2937; margin-bottom: 20px; font-size: 24px; }
            .problem-card { background: #fef2f2; border: 1px solid #fca5a5; border-radius: 10px; padding: 20px; margin-bottom: 15px; }
            .problem-header { display: flex; align-items: center; gap: 10px; margin-bottom: 10px; }
            .problem-number { background: #dc2626; color: white; padding: 4px 10px; border-radius: 50%; font-weight: 600; font-size: 12px; }
            .problem-type { color: #dc2626; font-weight: 700; font-size: 16px; }
            .problem-location { color: #6b7280; font-size: 13px; margin-bottom: 10px; font-style: italic; }
            .problem-description { color: #374151; line-height: 1.6; }
            .recommendation-card { background: #f0fdf4; border: 1px solid #86efac; border-radius: 10px; padding: 20px; margin-bottom: 15px; }
            .recommendation-header { display: flex; align-items: center; gap: 10px; margin-bottom: 10px; }
            .recommendation-number { background: #059669; color: white; padding: 4px 10px; border-radius: 50%; font-weight: 600; font-size: 12px; }
            .recommendation-title { color: #059669; font-weight: 700; font-size: 16px; }
            .recommendation-text { color: #065f46; line-height: 1.6; }
            .footer { text-align: center; margin-top: 50px; padding-top: 20px; border-top: 2px solid #e5e7eb; color: #6b7280; }
            @media print { body { background: white; padding: 0; } .container { box-shadow: none; } }
        """;
    }

    private String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}