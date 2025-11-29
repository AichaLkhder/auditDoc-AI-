package com.yourapp.controller;

import javafx.fxml.FXML;
import javafx.scene.shape.Circle;

public class TopbarController {

    @FXML private Circle notificationDot;

    // Variable boolean demandée
    private boolean notificationActive = true;

    @FXML
    public void initialize() {
        updateNotificationVisibility();

        // Example: If needed, you could expose a method to change the state
        // setNotificationActive(true);
    }

    /**
     * Met à jour la visibilité du point de notification en fonction de la variable `notificationActive`.
     */
    private void updateNotificationVisibility() {
        notificationDot.setVisible(notificationActive);
    }

    /**
     * Définit l'état de la notification et met à jour l'affichage.
     * @param active true pour afficher le point, false pour le cacher.
     */
    public void setNotificationActive(boolean active) {
        this.notificationActive = active;
        updateNotificationVisibility();
    }
}