package com.coffee_shop.view;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import com.coffee_shop.model.Users;
import com.coffee_shop.model.Admin;
import com.coffee_shop.model.Cashier;
import com.coffee_shop.model.UsersRepository;

public class CoffeeShopApp extends Application {
    private UsersRepository usersRepository;
    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Coffee Shop Management System");
        // initialize repository (creates `users` table if necessary)
        usersRepository = new UsersRepository();
        try {
            if (!usersRepository.hasAnyUsers()) {
                // Only insert sample users when table is empty
                usersRepository.addUser(new Admin("admin1", "admin1"));
                usersRepository.addUser(new Admin("admin2", "admin2"));
                usersRepository.addUser(new Cashier("cashier1", "cashier1"));
                usersRepository.addUser(new Cashier("cashier2", "cashier2"));
            }
        } catch (Exception e) {
            System.err.println("Warning: could not populate sample users: " + e.getMessage());
        }

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
    
    private void openManageUsersWindow() {
        Stage dialog = new Stage();
        dialog.setTitle("Manage Users");
        dialog.initOwner(null);

        VBox box = new VBox(10);
        box.setPadding(new javafx.geometry.Insets(12));

        Label info = new Label("Enter one user per line in the form: username,password,ROLE\nROLE should be ADMIN or CASHIER. Example:\nadmin1,admin1,ADMIN\ncashier1,cashier1,CASHIER");
        info.setWrapText(true);

        TextArea area = new TextArea();
        area.setPrefColumnCount(40);
        area.setPrefRowCount(12);

        // populate with current users
        try {
            java.util.List<Users> existing = usersRepository.getAllUsers();
            StringBuilder sb = new StringBuilder();
            for (Users u : existing) {
                sb.append(u.getUsername()).append(',').append(u.getPassword()).append(',').append(u.getRole()).append('\n');
            }
            area.setText(sb.toString());
        } catch (Exception ex) {
            area.setText("# Could not load existing users: " + ex.getMessage());
        }

        Button save = new Button("Save and Replace All Users");
        Button cancel = new Button("Cancel");

        save.setOnAction(evt -> {
            String text = area.getText();
            String[] lines = text.split("\\r?\\n");
            java.util.List<Users> newUsers = new java.util.ArrayList<>();
            for (String line : lines) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;
                String[] parts = line.split(",");
                if (parts.length < 2) continue;
                String u = parts[0].trim();
                String p = parts[1].trim();
                String role = parts.length >= 3 ? parts[2].trim() : "CASHIER";
                if ("ADMIN".equalsIgnoreCase(role)) {
                    newUsers.add(new Admin(u, p));
                } else if ("CASHIER".equalsIgnoreCase(role)) {
                    newUsers.add(new Cashier(u, p));
                } else {
                    newUsers.add(new Users(u, p) {
                        @Override
                        public String getRole() {
                            return role;
                        }
                    });
                }
            }

            try {
                usersRepository.replaceUsers(newUsers);
                javafx.scene.control.Alert a = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
                a.setTitle("Success");
                a.setHeaderText("Users updated");
                a.setContentText("All users replaced successfully.");
                a.showAndWait();
                dialog.close();
            } catch (Exception e) {
                e.printStackTrace();
                javafx.scene.control.Alert a = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
                a.setTitle("Error");
                a.setHeaderText("Could not update users");
                a.setContentText(e.getMessage());
                a.showAndWait();
            }
        });

        cancel.setOnAction(evt -> dialog.close());

        HBox btns = new HBox(10, save, cancel);
        btns.setAlignment(Pos.CENTER_RIGHT);

        box.getChildren().addAll(info, area, btns);

        Scene s = new Scene(box);
        dialog.setScene(s);
        dialog.initModality(javafx.stage.Modality.APPLICATION_MODAL);
        dialog.showAndWait();
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
                Alert a = new Alert(Alert.AlertType.WARNING);
                a.setTitle("Missing Fields");
                a.setHeaderText("Incomplete Form");
                a.setContentText("Please enter both username and password.");
                a.showAndWait();
                return;
            }

            Users user = null;
            try {
                user = usersRepository.authenticate(username, password);
            } catch (Exception ex) {
                ex.printStackTrace();
                Alert a = new Alert(Alert.AlertType.ERROR);
                a.setTitle("Database Error");
                a.setHeaderText("Authentication Failed");
                a.setContentText("Could not check credentials: " + ex.getMessage());
                a.showAndWait();
                return;
            }

            if (user == null) {
                System.out.println("Invalid username or password");
                Alert a = new Alert(Alert.AlertType.ERROR);
                a.setTitle("Invalid Credentials");
                a.setHeaderText("Login Failed");
                a.setContentText("The username or password you entered is incorrect.");
                a.showAndWait();
                return;
            }

            if (user instanceof Admin) {
                System.out.println("Admin login successful: " + user.getUsername());
                try {
                    CoffeeShopDashboard dashboard = new CoffeeShopDashboard();
                    dashboard.start((Stage) loginButton.getScene().getWindow());
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            } else if (user instanceof Cashier) {
                System.out.println("Cashier login successful: " + user.getUsername());
                try {
                    CoffeeShopCashier cashier = new CoffeeShopCashier();
                    cashier.start((Stage) loginButton.getScene().getWindow());
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            } else {
                Alert a = new Alert(Alert.AlertType.INFORMATION);
                a.setTitle("Unknown Role");
                a.setHeaderText("Login Succeeded");
                a.setContentText("Logged in as: " + user.getRole());
                a.showAndWait();
            }
        });
        
        // Add all elements to container
        HBox buttons = new HBox(10, loginButton);
        buttons.setAlignment(Pos.CENTER);

        loginContainer.getChildren().addAll(
            titleLabel,
            usernameLabel,
            usernameField,
            passwordLabel,
            passwordField,
            buttons
        );
        
        return loginContainer;
    }

    public static void main(String[] args) {
        launch(args);
    }
    
}
