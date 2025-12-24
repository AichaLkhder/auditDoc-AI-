package com.yourapp.controller;

import com.yourapp.model.Notification;
import com.yourapp.model.User;
import com.yourapp.services.NotificationService;
import com.yourapp.utils.SessionManager;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Component
public class TopbarController {

    @FXML private ImageView topLogo;
    @FXML private Label topAppName;
    @FXML private Label userName;
    @FXML private Label userRole;
    @FXML private Circle notificationBadge;
    @FXML private Button btnNotifications;
    @FXML private StackPane notificationWrapper;
    @FXML private Button btnNewAudit;
    @FXML private Circle avatarCircle;
    @FXML private Label avatarText;
    @FXML private Button btnLanguage;
    @FXML private Button btnTheme;

    private final NotificationService notificationService = new NotificationService();

    @Autowired
    private ApplicationContext springContext;

    private MainLayoutController mainController;

    @FXML
    public void initialize() {
        // Load logo
        try {
            Image img = new Image(getClass().getResource("/views/icons/logo-audit.png").toExternalForm());
            if (topLogo != null) {
                topLogo.setImage(img);
                topLogo.setFitWidth(98);
                topLogo.setFitHeight(98);
                topLogo.setPreserveRatio(true);
                topLogo.setSmooth(true);
            }
        } catch (Exception ex) {
            System.err.println("TopbarController: logo not found -> " + ex.getMessage());
        }

        // Load current user from session
        loadCurrentUser();

        // Notifications
        updateNotificationBadge();

        // Fix button rendering - ensure no ellipsis
        Platform.runLater(() -> {
            if (btnNotifications != null) {
                btnNotifications.setMnemonicParsing(false);
                btnNotifications.setEllipsisString("");
            }
            if (btnLanguage != null) {
                btnLanguage.setMnemonicParsing(false);
                btnLanguage.setEllipsisString("");
            }
            if (btnTheme != null) {
                btnTheme.setMnemonicParsing(false);
                btnTheme.setEllipsisString("");
            }
            if (btnNewAudit != null) {
                btnNewAudit.setMnemonicParsing(false);
            }
        });

        // Actions
        btnNotifications.setOnAction(e -> showNotificationPopup());
        btnNewAudit.setOnAction(e -> loadAuditPage());
        btnLanguage.setOnAction(e -> showLanguageMenu());
        btnTheme.setOnAction(e -> toggleTheme());

        // User menu - clickable on all elements
        userName.setOnMouseClicked(e -> showUserMenu());
        userRole.setOnMouseClicked(e -> showUserMenu());
        avatarCircle.setOnMouseClicked(e -> showUserMenu());

        // Logo and app name - clickable to return to dashboard
        topLogo.setOnMouseClicked(e -> loadDashboard());
        topAppName.setOnMouseClicked(e -> loadDashboard());
        topLogo.setStyle("-fx-cursor: hand;");
        topAppName.setStyle("-fx-cursor: hand;");
    }

    /**
     * Load the current logged-in user from SessionManager
     */
    private void loadCurrentUser() {
        User currentUser = SessionManager.getInstance().getCurrentUser();

        if (currentUser != null) {
            userName.setText(currentUser.getFullName());
            userRole.setText(currentUser.getRole());

            // Set avatar color based on user role
            updateAvatarColor(currentUser.getRole());

            System.out.println("✅ Topbar loaded for user: " + currentUser.getFullName());
        } else {
            userName.setText("Guest");
            userRole.setText("Not logged in");
            avatarCircle.setFill(javafx.scene.paint.Color.GRAY);
            System.err.println("⚠️ No user logged in - SessionManager returned null");
        }
    }

    /**
     * Update avatar color based on user role
     */
    private void updateAvatarColor(String role) {
        if (role == null) {
            avatarCircle.setFill(javafx.scene.paint.Color.GRAY);
            return;
        }

        switch (role.toUpperCase()) {
            case "ADMIN":
            case "ADMINISTRATEUR":
                avatarCircle.setFill(javafx.scene.paint.Color.web("#dc3545"));
                break;
            case "MANAGER":
                avatarCircle.setFill(javafx.scene.paint.Color.web("#ffc107"));
                break;
            case "USER":
            default:
                avatarCircle.setFill(javafx.scene.paint.Color.web("#007bff"));
                break;
        }
    }

    public void setMainController(MainLayoutController mainController) {
        this.mainController = mainController;
    }

    private void loadAuditPage() {
        if (mainController != null) {
            mainController.loadView("Audit.fxml");
            mainController.updateSidebarActive("audit");
        } else {
            System.err.println("MainController is not set in TopbarController");
        }
    }

    private void loadDashboard() {
        if (mainController != null) {
            mainController.loadView("Dashboard.fxml");
            mainController.updateSidebarActive("dashboard");
        } else {
            System.err.println("MainController is not set in TopbarController");
        }
    }

    private void updateNotificationBadge() {
        int unread = notificationService.countUnread();
        notificationBadge.setVisible(unread > 0);
    }

    private void showNotificationPopup() {
        ContextMenu menu = new ContextMenu();
        List<Notification> all = notificationService.getAll();

        if (all.isEmpty()) {
            MenuItem noNotif = new MenuItem("Aucune notification");
            noNotif.setDisable(true);
            menu.getItems().add(noNotif);
        } else {
            for (Notification n : all) {
                MenuItem item = new MenuItem(n.getMessage());
                if (!n.isRead()) {
                    item.setStyle("-fx-font-weight: bold;");
                }
                menu.getItems().add(item);
                item.setOnAction(ev -> {
                    n.markAsRead();
                    updateNotificationBadge();
                });
            }
        }

        menu.show(btnNotifications, javafx.geometry.Side.BOTTOM, 0, 10);
    }

    private void showLanguageMenu() {
        ContextMenu menu = new ContextMenu();

        MenuItem french = new MenuItem("Français");
        MenuItem english = new MenuItem("English");
        MenuItem arabic = new MenuItem("العربية");

        french.setOnAction(e -> changeLanguage("fr"));
        english.setOnAction(e -> changeLanguage("en"));
        arabic.setOnAction(e -> changeLanguage("ar"));

        menu.getItems().addAll(french, english, arabic);
        menu.show(btnLanguage, javafx.geometry.Side.BOTTOM, 0, 10);
    }

    private void changeLanguage(String lang) {
        System.out.println("Changement de langue: " + lang);
        // TODO: Add language change logic
    }

    private void toggleTheme() {
        System.out.println("Toggle theme (light/dark)");
        // TODO: Add theme change logic
        if (btnTheme.getText().equals("☀")) {
            btnTheme.setText("🌙");
        } else {
            btnTheme.setText("☀");
        }
    }

    private void showUserMenu() {
        ContextMenu menu = new ContextMenu();

        MenuItem profile = new MenuItem("Profil utilisateur");
        MenuItem help = new MenuItem("Aide");
        MenuItem logout = new MenuItem("Se déconnecter");

        profile.setOnAction(e -> loadInCenter("/fxml/ProfileView.fxml"));
        help.setOnAction(e -> loadInCenter("/fxml/HelpView.fxml"));
        logout.setOnAction(e -> handleLogout());

        menu.getItems().addAll(profile, help, logout);
        menu.show(userName, javafx.geometry.Side.BOTTOM, 0, 10);
    }

    /**
     * Handle user logout
     */
    private void handleLogout() {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirmation");
        confirmAlert.setHeaderText("Déconnexion");
        confirmAlert.setContentText("Êtes-vous sûr de vouloir vous déconnecter?");

        Optional<ButtonType> result = confirmAlert.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            SessionManager.getInstance().logout();
            System.out.println("✅ User logged out successfully");
            redirectToLogin();
        }
    }

    /**
     * Redirect to login page
     */
    private void redirectToLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/fxml/login.fxml"));
            loader.setControllerFactory(springContext::getBean);
            Parent root = loader.load();

            Stage stage = (Stage) userName.getScene().getWindow();
            stage.getScene().setRoot(root);
            stage.setMaximized(true);
            stage.setTitle("AuditDoc AI - Connexion");

            System.out.println("✅ Redirected to login page");

        } catch (Exception e) {
            System.err.println("❌ Error redirecting to login: " + e.getMessage());
            e.printStackTrace();

            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText("Erreur de déconnexion");
            alert.setContentText("Impossible de charger la page de connexion.");
            alert.showAndWait();
        }
    }

    private void loadInCenter(String path) {
        try {
            Node loaded = loadFXML(path);
            if (loaded != null) setCenterOfBorderPane(loaded);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Node loadFXML(String resourcePath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(resourcePath));
            return loader.load();
        } catch (IOException | NullPointerException e) {
            System.err.println("Error loading: " + resourcePath);
            e.printStackTrace();
            return null;
        }
    }

    private void setCenterOfBorderPane(Node node) {
        Node current = btnNewAudit;
        while (current != null) {
            if (current instanceof BorderPane) {
                ((BorderPane) current).setCenter(node);
                return;
            }
            current = current.getParent();
        }

        try {
            Node root = btnNewAudit.getScene().getRoot();
            if (root instanceof BorderPane) {
                ((BorderPane) root).setCenter(node);
            }
        } catch (Exception ignored) {}
    }
}