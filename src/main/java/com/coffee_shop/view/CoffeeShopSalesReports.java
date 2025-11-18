package com.coffee_shop.view;

import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class CoffeeShopSalesReports {
     public void start(Stage stage) {

        // 🧩 Layout setup
        VBox root = new VBox(20);
        root.setStyle("-fx-alignment: center; -fx-padding: 40;");

        // 📦 Page Title
        Label title = new Label("📦 Orders Management Page");

        // ⬅ Back Button
        Button backButton = new Button("⬅ Back to Dashboard");
        backButton.setOnAction(e -> {
            try {
                CoffeeShopDashboard dashboard = new CoffeeShopDashboard();
                dashboard.start(stage);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        // 🧱 Add elements
        root.getChildren().addAll(title, backButton);

        // 🎬 Create and show scene
        Scene scene = new Scene(root, 1540, 740);
        stage.setScene(scene);
        stage.setTitle("Coffee Shop POS - Orders");
        stage.show();
    }
}
