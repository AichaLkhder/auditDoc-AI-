package com.yourapp;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
//import org.apache.catalina.core.ApplicationContext;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

public class Main extends Application {

    private static ConfigurableApplicationContext springContext;

    @Override
    public void init() {
        SpringApplication app = new SpringApplication(AuditDocAiApplication.class);
        app.setWebApplicationType(org.springframework.boot.WebApplicationType.NONE);
        springContext = app.run();
    }

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(
                Main.class.getResource("/views/fxml/signup.fxml")
        );
        loader.setControllerFactory(param ->
                springContext.getBean(param)
        );

        Parent root = loader.load();

        stage.setScene(new Scene(root, 800, 600));
        stage.setTitle("AuditDoc AI - Connexion");
        stage.show();
    }

    @Override
    public void stop() {springContext.close();}

    public static ConfigurableApplicationContext getSpringContext() {
        return springContext;
    }
}