package com.yourapp.controller;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;

import java.util.Arrays;
import java.util.List;

/**
 * Controller for Sidebar.fxml.
 */
public class SidebarController {

    // Référence au contrôleur principal pour charger les pages
    private MainLayoutController mainLayoutController;

    // POINT 4: Sidebar VBox et éléments de menu
    @FXML
    private VBox sidebarVBox;
    @FXML
    private Label toggleIcon;
    @FXML
    private Label toggleLabel;

    @FXML
    private HBox dashboardItem;
    @FXML
    private HBox auditItem;
    @FXML
    private HBox projectsItem;
    @FXML
    private HBox historyItem;
    @FXML
    private HBox settingsItem;

    // POINT 1: Éléments spécifiques à l'Audit (pour changement de couleur)
    @FXML
    private Label auditLabel;
    @FXML
    private Label auditIcon;
    @FXML
    private Circle auditDot;

    private List<HBox> menuItems;
    private HBox activeItem;
    private boolean isReduced = false;

    // Noms des pages FXML correspondantes pour le chargement
    private final String DASHBOARD_PAGE = "Dashboard";
    private final String AUDIT_PAGE = "Audit";
    private final String PROJECTS_PAGE = "Projets";
    private final String HISTORY_PAGE = "Historique";
    private final String SETTINGS_PAGE = "Parametres"; // Assurez-vous que le nom du fichier FXML est correct

    public void setMainLayoutController(MainLayoutController controller) {
        this.mainLayoutController = controller;
    }

    @FXML
    public void initialize() {
        menuItems = Arrays.asList(dashboardItem, auditItem, projectsItem, historyItem, settingsItem);
        // Au démarrage, l'Audit est actif par défaut (selon votre FXML initial)
        activeItem = auditItem;
        setActiveStyle(auditItem);
    }

    /**
     * Gère le clic sur un élément de menu (pour chargement de page et style actif).
     */
    @FXML
    private void handleMenuItemClick(MouseEvent event) {
        HBox clickedItem = (HBox) event.getSource();

        if (clickedItem != activeItem) {
            // Retire l'ancien style actif
            setInactiveStyle(activeItem);

            // Applique le nouveau style actif
            activeItem = clickedItem;
            setActiveStyle(activeItem);

            // Détermine le nom de la page à charger
            String pageName = getPageName(clickedItem);
            if (mainLayoutController != null && pageName != null) {
                mainLayoutController.loadPage(pageName);
            }
        }
    }

    private String getPageName(HBox item) {
        if (item == dashboardItem) return DASHBOARD_PAGE;
        if (item == auditItem) return AUDIT_PAGE;
        if (item == projectsItem) return PROJECTS_PAGE;
        if (item == historyItem) return HISTORY_PAGE;
        if (item == settingsItem) return SETTINGS_PAGE;
        return null;
    }

    /**
     * Applique le style actif à un élément de menu (menu-item-active).
     * @param item
     */
    private void setActiveStyle(HBox item) {
        // Enlève le style "menu-item" (s'il existe) et ajoute "menu-item-active"
        item.getStyleClass().remove("menu-item");
        item.getStyleClass().add("menu-item-active");

        // POINT 1 : Change le style du label/icône actif (couleur/poids)
        // La gestion des labels se fait par CSS : .menu-item-active .menu-label-inactive devient .menu-label-active
        // On doit retirer le style par défaut des labels et appliquer le style actif.
        for (Node child : item.getChildren()) {
            if (child instanceof Label) {
                Label label = (Label) child;
                if (label.getStyleClass().contains("menu-label")) {
                    label.getStyleClass().remove("menu-label");
                    label.getStyleClass().add("menu-label-active-custom"); // Style personnalisé pour le texte
                } else if (label.getStyleClass().contains("menu-icon")) {
                    label.getStyleClass().remove("menu-icon");
                    label.getStyleClass().add("menu-icon-active");
                }
            }
            // POINT 1 : Rend le cercle de l'audit visible s'il est actif
            if (item == auditItem && child instanceof Circle) {
                child.setVisible(true);
            }
        }
    }

    /**
     * Applique le style inactif à un élément de menu (menu-item).
     * @param item
     */
    private void setInactiveStyle(HBox item) {
        // Enlève le style "menu-item-active" et ajoute "menu-item"
        item.getStyleClass().remove("menu-item-active");
        item.getStyleClass().add("menu-item");

        // POINT 1 : Remet le style par défaut (couleur/poids)
        for (Node child : item.getChildren()) {
            if (child instanceof Label) {
                Label label = (Label) child;
                if (label.getStyleClass().contains("menu-label-active-custom")) {
                    label.getStyleClass().remove("menu-label-active-custom");
                    label.getStyleClass().add("menu-label"); // Style par défaut du label
                } else if (label.getStyleClass().contains("menu-icon-active")) {
                    label.getStyleClass().remove("menu-icon-active");
                    label.getStyleClass().add("menu-icon"); // Style par défaut de l'icône
                }
            }
            // POINT 1 : Cache le cercle de l'audit s'il n'est plus actif
            if (item == auditItem && child instanceof Circle) {
                child.setVisible(false);
            }
        }
    }


    /**
     * POINT 4: Gère la réduction/expansion de la sidebar.
     */
    @FXML
    private void handleToggleSidebar() {
        isReduced = !isReduced;

        if (isReduced) {
            // Réduire
            sidebarVBox.setPrefWidth(60.0);
            toggleIcon.setText(">"); // Change l'icône
            toggleLabel.setVisible(false); // Cacher le label "Réduire"
        } else {
            // Étendre
            sidebarVBox.setPrefWidth(250.0);
            toggleIcon.setText("<"); // Change l'icône
            toggleLabel.setVisible(true); // Afficher le label "Réduire"
        }

        // POINT 4: Cacher/Afficher les labels de menu sans toucher aux styles de conteneur
        for (HBox item : menuItems) {
            // Le label est le deuxième enfant de HBox (index 1)
            if (item.getChildren().size() > 1) {
                Node labelNode = item.getChildren().get(item.getChildren().size() - 1); // Le dernier est le label
                if (labelNode instanceof Label) {
                    labelNode.setVisible(!isReduced);
                    // Règle d'opacité dans CSS pour s'assurer que le texte est bien invisible
                    labelNode.getStyleClass().add(isReduced ? "hidden-on-shrink" : "visible-on-shrink");
                }
            }
        }
    }
}