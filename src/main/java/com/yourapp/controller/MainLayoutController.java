package com.yourapp.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.layout.VBox;

import java.io.IOException;

/**
 * Contrôleur principal du MainLayout.fxml.
 * Gère le chargement dynamique des pages dans le conteneur central (pageContainer).
 */
public class MainLayoutController {

    @FXML private VBox pageContainer;

    // Injection du contrôleur de la Navbar via fx:include
    @FXML private NavbarController navbarController;

    @FXML
    public void initialize() {
        // Le contrôleur de la Navbar a besoin de connaître le contrôleur principal
        // pour demander le chargement d'une nouvelle page
        if (navbarController != null) {
            navbarController.setMainController(this);
        }

        // Charger la page 'Audit' par défaut au démarrage
        loadPage("AuditView.fxml");
    }

    /**
     * Charge un nouveau fichier FXML dans le conteneur de page central.
     * @param fxmlFileName Le nom du fichier FXML à charger (ex: "AuditView.fxml").
     */
    public void loadPage(String fxmlFileName) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/views/fxml/" + fxmlFileName)
            );
            Node pageNode = loader.load();

            // S'assurer que le contenu est bien chargé dans le conteneur
            pageContainer.getChildren().setAll(pageNode);

            // Optionnel: Réinitialiser le ScrollPane au début de la page
            // pageScrollPane.setVvalue(0.0);

            System.out.println("Page chargée : " + fxmlFileName);

        } catch (IOException e) {
            System.err.println("Erreur de chargement de la page FXML : " + fxmlFileName);
            e.printStackTrace();
            // Optionnel: Afficher un message d'erreur dans le conteneur
        }
    }
}