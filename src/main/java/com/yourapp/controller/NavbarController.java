package com.yourapp.controller;

import com.yourapp.Main;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Contrôleur pour la Sidebar/Navbar.
 * Gère l'état actif des menus et la réduction/extension de la sidebar.
 */
public class NavbarController {

    @FXML private VBox navbarContainer;
    @FXML private HBox logoContainer;
    @FXML private Label appLogoLabel;
    @FXML private HBox footerContainer;
    @FXML private Label collapseIcon;
    @FXML private Label collapseLabel;

    @FXML private HBox dashboardBtn;
    @FXML private HBox auditBtn;
    @FXML private HBox projectsBtn;
    @FXML private HBox historyBtn;
    @FXML private HBox settingsBtn;

    @FXML private Label dashboardIcon;
    @FXML private Label dashboardLabel;
    @FXML private Label auditIcon;
    @FXML private Label auditLabel;
    @FXML private Label projectsIcon;
    @FXML private Label projectsLabel;
    @FXML private Label historyIcon;
    @FXML private Label historyLabel;
    @FXML private Label settingsIcon;
    @FXML private Label settingsLabel;

    @FXML private Circle auditActiveCircle; // Exemple de l'élément de décoration active

    private List<HBox> menuButtons;
    private Map<HBox, String> pageMapping;
    private MainLayoutController mainController;
    private boolean isCollapsed = false;
    private final double WIDE_WIDTH = 250.0;
    private final double COLLAPSED_WIDTH = 60.0;

    @FXML
    public void initialize() {
        menuButtons = new ArrayList<>();
        menuButtons.add(dashboardBtn);
        menuButtons.add(auditBtn);
        menuButtons.add(projectsBtn);
        menuButtons.add(historyBtn);
        menuButtons.add(settingsBtn);

        // Association des boutons aux chemins FXML (à charger par le MainLayoutController)
        pageMapping = new HashMap<>();
        pageMapping.put(dashboardBtn, "DashboardView.fxml");
        pageMapping.put(auditBtn, "AuditView.fxml");
        pageMapping.put(projectsBtn, "ProjectsView.fxml"); // Doit exister pour fonctionner
        pageMapping.put(historyBtn, "HistoryView.fxml");   // Doit exister pour fonctionner
        pageMapping.put(settingsBtn, "SettingsView.fxml"); // Doit exister pour fonctionner

        // Initialisation de l'état (Audit est actif par défaut dans FXML)
        setActiveButton(auditBtn);

        // Gérer le cas où les labels et le logo doivent être masqués au départ si le FXML n'est pas utilisé
        // Ici, on part du principe que la version large est l'état initial.
        // On rend les éléments invisibles si on veut cacher au départ, mais ici on garde visible.
    }

    /**
     * Définit le contrôleur principal pour la communication inter-contrôleurs.
     * @param controller Le MainLayoutController
     */
    public void setMainController(MainLayoutController controller) {
        this.mainController = controller;
    }

    /**
     * Gère la navigation lors du clic sur un élément de menu.
     * @param event L'événement de souris.
     */
    @FXML
    private void handleNavigation(MouseEvent event) {
        HBox clickedButton = (HBox) event.getSource();
        setActiveButton(clickedButton);

        String fxmlName = pageMapping.get(clickedButton);
        if (mainController != null && fxmlName != null) {
            mainController.loadPage(fxmlName);
        }
    }

    /**
     * Change le style du bouton actif.
     * @param activeButton Le bouton HBox qui devient actif.
     */
    private void setActiveButton(HBox activeButton) {
        // Rétablir tous les boutons à l'état inactif
        for (HBox button : menuButtons) {
            button.getStyleClass().remove("menu-item-active");
            button.getStyleClass().add("menu-item");

            // Rétablir les labels et icônes à l'état inactif (CSS)
            ((Label) button.getChildren().get(1)).getStyleClass().setAll("menu-label"); // Label
            ((Label) button.getChildren().get(0)).getStyleClass().setAll("menu-icon");  // Icone

            // Masquer l'indicateur d'actif (le cercle est seulement dans Audit pour l'exemple)
            if (button.getChildren().size() > 2) {
                button.getChildren().get(0).setVisible(false); // Cache le Circle dans la structure de l'AuditView
            }
        }

        // Définir le bouton cliqué comme actif
        activeButton.getStyleClass().remove("menu-item");
        activeButton.getStyleClass().add("menu-item-active");

        // Mettre à jour les styles pour les labels/icônes actifs
        if (activeButton.getId().equals("auditBtn")) {
            // Cas spécial pour Audit qui a le cercle en plus. On assume l'ordre: Circle, Icon, Label
            activeButton.getChildren().get(0).setVisible(true); // Afficher le Circle
            ((Label) activeButton.getChildren().get(1)).getStyleClass().setAll("menu-icon-active"); // Icone
            ((Label) activeButton.getChildren().get(2)).getStyleClass().setAll("menu-label-active"); // Label
        } else {
            // Cas général: Icon, Label
            ((Label) activeButton.getChildren().get(0)).getStyleClass().setAll("menu-icon-active"); // Icone
            ((Label) activeButton.getChildren().get(1)).getStyleClass().setAll("menu-label-active"); // Label
        }

    }

    /**
     * Bascule entre l'état réduit et l'état étendu de la sidebar.
     */
    @FXML
    private void toggleSidebar() {
        isCollapsed = !isCollapsed;

        if (isCollapsed) {
            // Réduire
            collapseIcon.setText(">"); // Changer l'icône
            collapseLabel.setVisible(false);
            appLogoLabel.setVisible(false);
            // Cacher tous les labels de menu (sauf les icônes)
            menuButtons.forEach(btn -> {
                if (btn.getChildren().size() > 1) {
                    btn.getChildren().get(1).setVisible(false); // Cache le Label (position 1 ou 2)
                    if(btn.getChildren().size() > 2){ // Cas spécial Audit
                        btn.getChildren().get(2).setVisible(false);
                    }
                }
                btn.setPadding(new javafx.geometry.Insets(12)); // Ajuster le padding pour l'icône seule
            });
            // Adapter les conteneurs
            logoContainer.setAlignment(javafx.geometry.Pos.CENTER);
            footerContainer.setAlignment(javafx.geometry.Pos.CENTER);
            footerContainer.setPadding(new javafx.geometry.Insets(15, 0, 15, 0)); // Padding centré

            // Animation de la réduction
            animateSidebarWidth(COLLAPSED_WIDTH);

        } else {
            // Étendre
            collapseIcon.setText("<"); // Rétablir l'icône
            collapseLabel.setVisible(true);
            appLogoLabel.setVisible(true);
            // Afficher tous les labels de menu
            menuButtons.forEach(btn -> {
                if (btn.getChildren().size() > 1) {
                    btn.getChildren().get(1).setVisible(true); // Affiche le Label
                    if(btn.getChildren().size() > 2){ // Cas spécial Audit
                        btn.getChildren().get(2).setVisible(true);
                    }
                }
                btn.setPadding(new javafx.geometry.Insets(12, 15, 12, 15)); // Rétablir le padding initial
            });
            // Rétablir les conteneurs
            logoContainer.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
            footerContainer.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
            footerContainer.setPadding(new javafx.geometry.Insets(15, 0, 15, 20)); // Padding initial

            // Animation de l'extension
            animateSidebarWidth(WIDE_WIDTH);
        }
    }

    /**
     * Applique une animation de transition de largeur.
     * @param targetWidth La largeur cible de la sidebar.
     */
    private void animateSidebarWidth(double targetWidth) {
        Timeline timeline = new Timeline();
        KeyValue kv = new KeyValue(navbarContainer.prefWidthProperty(), targetWidth);
        KeyFrame kf = new KeyFrame(Duration.millis(300), kv); // Animation de 300ms
        timeline.getKeyFrames().add(kf);
        timeline.play();
    }
}