package com.coffee_shop.view;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class CoffeeShopApp extends Application {
    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Coffee Shop Management System");
        
        // Create login page
        VBox loginPage = createLoginPage();
        
        Scene scene = new Scene(loginPage, 500, 400);
        try {
            String css = getClass().getResource("/dashboard.css").toExternalForm();
            scene.getStylesheets().add(css);
        } catch (Exception e) {
            System.err.println("CSS file not found: " + e.getMessage());
        }
        primaryStage.setScene(scene);
        primaryStage.show();
    }
    
    private VBox createLoginPage() {
        VBox loginContainer = new VBox();
        loginContainer.setStyle("-fx-background-color: #F8F9FA;");
        loginContainer.setAlignment(Pos.CENTER);
        loginContainer.setSpacing(20);
        loginContainer.setPadding(new Insets(40));
        
        // Title Label
        Label titleLabel = new Label("Coffee Shop Login");
        titleLabel.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: #3E2723;");
        
        // Username Label and TextField
        Label usernameLabel = new Label("Username:");
        usernameLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #333333; -fx-font-weight: bold;");
        
        TextField usernameField = new TextField();
        usernameField.setPromptText("Enter your username");
        usernameField.setPrefHeight(40);
        usernameField.setStyle(
            "-fx-padding: 10px; " +
            "-fx-font-size: 14px; " +
            "-fx-border-radius: 5px; " +
            "-fx-border-color: #DDD; " +
            "-fx-border-width: 1px; " +
            "-fx-background-color: #FFFFFF;"
        );
        
        // Password Label and PasswordField
        Label passwordLabel = new Label("Password:");
        passwordLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #333333; -fx-font-weight: bold;");
        
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Enter your password");
        passwordField.setPrefHeight(40);
        passwordField.setStyle(
            "-fx-padding: 10px; " +
            "-fx-font-size: 14px; " +
            "-fx-border-radius: 5px; " +
            "-fx-border-color: #DDD; " +
            "-fx-border-width: 1px; " +
            "-fx-background-color: #FFFFFF;"
        );
        
        // Login Button
        Button loginButton = new Button("Login");
        loginButton.setPrefHeight(45);
        loginButton.setPrefWidth(150);
        loginButton.setStyle(
            "-fx-background-color: #6F4E37; " +
            "-fx-text-fill: #FFFFFF; " +
            "-fx-font-size: 14px; " +
            "-fx-font-weight: bold; " +
            "-fx-background-radius: 5px; " +
            "-fx-cursor: hand; " +
            "-fx-padding: 10px 20px;"
        );
        
        // Login Button Hover Effect
        loginButton.setOnMouseEntered(event -> 
            loginButton.setStyle(
                "-fx-background-color: #5a4130; " +
                "-fx-text-fill: #FFFFFF; " +
                "-fx-font-size: 14px; " +
                "-fx-font-weight: bold; " +
                "-fx-background-radius: 5px; " +
                "-fx-cursor: hand; " +
                "-fx-padding: 10px 20px;"
            )
        );
        
        loginButton.setOnMouseExited(event -> 
            loginButton.setStyle(
                "-fx-background-color: #6F4E37; " +
                "-fx-text-fill: #FFFFFF; " +
                "-fx-font-size: 14px; " +
                "-fx-font-weight: bold; " +
                "-fx-background-radius: 5px; " +
                "-fx-cursor: hand; " +
                "-fx-padding: 10px 20px;"
            )
        );
        
        // Login Button Action
        loginButton.setOnAction(event -> {
            String username = usernameField.getText();
            String password = passwordField.getText();
            
            if (username.isEmpty() || password.isEmpty()) {
                System.out.println("Please fill in all fields");
            } else if (username.equals("admin") && password.equals("admin")) {
                System.out.println("Admin login successful");
                try {
                    CoffeeShopDashboard dashboard = new CoffeeShopDashboard();
                    dashboard.start((Stage) loginButton.getScene().getWindow());
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            } else if (username.equals("cashier") && password.equals("12345")) {
                System.out.println("Cashier login successful");
                try {
                    CoffeeShopCashier cashier = new CoffeeShopCashier();
                    cashier.start((Stage) loginButton.getScene().getWindow());
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            } else {
                System.out.println("Invalid username or password");
                Alert a = new Alert(Alert.AlertType.ERROR);
                a.setTitle("Invalid Credentials");
                a.setHeaderText("Login Failed");
                a.setContentText("The username or password you entered is incorrect.");
                a.showAndWait();
            }
        });
        
        // Add all elements to container
        loginContainer.getChildren().addAll(
            titleLabel,
            usernameLabel,
            usernameField,
            passwordLabel,
            passwordField,
            loginButton
        );
        
        return loginContainer;
    }

    public static void main(String[] args) {
        launch(args);
    }
    
}
