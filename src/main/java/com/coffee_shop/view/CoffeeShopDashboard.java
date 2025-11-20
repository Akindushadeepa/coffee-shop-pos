package com.coffee_shop.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

public class CoffeeShopDashboard {

    // --- Layout Constants ---
    private static final double SIDEBAR_WIDTH = 240;
    private static final double TOP_BAR_HEIGHT = 60;

    public void start(Stage primaryStage) {

        // 🧩 Root layout uses BorderPane (Left: Sidebar, Center: Main content)
        BorderPane root = new BorderPane();

        // 🧩 Add Sidebar (Navigation)
        VBox sidebar = createSidebar();
        root.setLeft(sidebar);

        // 🧩 Add Main content area (Top Bar + Dashboard cards)
        VBox mainContentArea = new VBox();
        mainContentArea.getChildren().addAll(createTopBar(), createDashboardContent());
        root.setCenter(mainContentArea);

        // 🧩 Setup Scene
        Scene scene = new Scene(root, 1540, 740);
        try {
            String css = getClass().getResource("/dashboard.css").toExternalForm();
            scene.getStylesheets().add(css);
        } catch (Exception e) {
            System.err.println("CSS file not found: " + e.getMessage());
        }

        // 🧩 Configure Stage
        primaryStage.setTitle("Coffee Shop POS - Admin Dashboard");
        primaryStage.setScene(scene);
        

        primaryStage.show();
    }

    /**
     * 🧱 Creates the Sidebar (Left Navigation Panel)
     */
    private VBox createSidebar() {
        VBox sidebar = new VBox(15); // spacing between items
        sidebar.setPrefWidth(SIDEBAR_WIDTH);
        sidebar.getStyleClass().add("sidebar");

        // ☕ Logo/Title at the top
        Label logo = new Label("☕ COFEE");
        logo.setFont(Font.font("Poppins", FontWeight.BOLD, 20));
        logo.getStyleClass().add("logo");
        VBox.setMargin(logo, new Insets(20, 0, 30, 20));

        // 📋 Navigation links
        VBox navLinks = new VBox(5);
        navLinks.getChildren().addAll(
                createNavLink("🏠 Dashboard", true),
                createNavLink("🍩 Menu Management", false),
                createNavLink("📦 Orders", false),
                createNavLink("📊 Sales Reports", false),
                createNavLink("👤 Cashiers", false),
                createNavLink("🛠 Settings", false)
        );

        // 🔻 Spacer pushes logout button to the bottom
        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        // 🚪 Logout Button
        Button logoutButton = new Button("🚪 Logout");
        logoutButton.getStyleClass().add("logout-button");
        logoutButton.setMaxWidth(SIDEBAR_WIDTH - 40);
        VBox.setMargin(logoutButton, new Insets(0, 20, 20, 20));

        sidebar.getChildren().addAll(logo, navLinks, spacer, logoutButton);
        return sidebar;
    }

    /**
     * 🧭 Creates each navigation link/button inside the sidebar
     */
    private Button createNavLink(String text, boolean isActive) {
        Button link = new Button(text);
        link.getStyleClass().add("nav-link");
        if (isActive) link.getStyleClass().add("active");

        link.setMaxWidth(Double.MAX_VALUE);
        link.setAlignment(Pos.CENTER_LEFT);
        link.setPadding(new Insets(10, 20, 10, 20));

        // 🎯 Handle button click navigation
       link.setOnAction(e -> {
    Stage stage = (Stage) link.getScene().getWindow();
    System.out.println("Navigated to: " + text);

    try {
        // 🍩 Menu Management Page
        if (text.contains("Menu Management")) {
            CoffeeShopMenuManagement menuPage = new CoffeeShopMenuManagement();
            menuPage.start(stage);
        }

        // 📦 Orders Page
        else if (text.contains("Orders")) {
            CoffeeShopOrders ordersPage = new CoffeeShopOrders();
            ordersPage.start(stage);
        }

        else if (text.contains("Sales Reports")) {
            CoffeeShopSalesReports reportsPage = new CoffeeShopSalesReports();
            reportsPage.start(stage);
        }

        else if (text.contains("Cashiers")){
            CoffeeShopCashiersMgmt cashiersPage = new CoffeeShopCashiersMgmt();
            cashiersPage.start(stage);
        }
        
        else if (text.contains("Settings")){
            CoffeeShopSettings settingsPage = new CoffeeShopSettings();
            settingsPage.start(stage);
        }
    } catch (Exception ex) {
        ex.printStackTrace();
    }
});

        return link;
    }

    
      //Creates the Top Bar with Page Title, Notification, Admin, and Date/Time
     
    private HBox createTopBar() {
        HBox topBar = new HBox(10);
        topBar.setPrefHeight(TOP_BAR_HEIGHT);
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.getStyleClass().add("top-bar");
        topBar.setPadding(new Insets(0, 20, 0, 20));

        Label pageTitle = new Label("Dashboard");
        pageTitle.setFont(Font.font("Poppins", FontWeight.BOLD, 22));
        pageTitle.getStyleClass().add("page-title");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label notificationIcon = new Label("🔔");
        Label adminName = new Label("👤 Admin Name");
        Label dateTime = new Label("⏰ Current Date/Time");

        HBox.setMargin(adminName, new Insets(0, 10, 0, 10));
        topBar.getChildren().addAll(pageTitle, spacer, notificationIcon, adminName, dateTime);

        return topBar;
    }

    
      //Creates Dashboard Content (Statistics + Charts)
     
    private ScrollPane createDashboardContent() {
        VBox content = new VBox(20);
        content.getStyleClass().add("main-content-area");
        content.setPadding(new Insets(20));

        // --- Statistic cards section ---
        GridPane statGrid = new GridPane();
        statGrid.setHgap(20);
        statGrid.setVgap(20);

        ColumnConstraints cc = new ColumnConstraints();
        cc.setPercentWidth(25);
        cc.setHgrow(Priority.ALWAYS);
        statGrid.getColumnConstraints().addAll(cc, cc, cc, cc);

        // Add cards (Top Dashboard Summary)
        statGrid.add(createStatCard("💵 Total Sales (Today)", "$325.00"), 0, 0);
        statGrid.add(createStatCard("📄 Orders Completed", "47"), 1, 0);
        statGrid.add(createStatCard("☕ Best Seller", "Cappuccino"), 2, 0);
        statGrid.add(createStatCard("👥 Active Cashiers", "3"), 3, 0);

        // --- Chart placeholders section ---
        HBox chartsSection = new HBox(20);
        chartsSection.getChildren().addAll(
                createChartPlaceholder("Sales Overview (Chart Placeholder)"),
                createChartPlaceholder("Top 5 Menu Items (Chart Placeholder)")
        );

        // Add both sections to main content
        content.getChildren().addAll(statGrid, chartsSection);

        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.getStyleClass().add("scroll-pane");

        return scrollPane;
    }

    
      //Creates a single Statistic Card (e.g., Total Sales)
     
    private VBox createStatCard(String title, String data) {
        VBox card = new VBox(5);
        card.getStyleClass().add("stat-card");
        card.setPadding(new Insets(20));

        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("card-title");

        Label dataLabel = new Label(data);
        dataLabel.getStyleClass().add("card-data");

        card.getChildren().addAll(titleLabel, dataLabel);
        return card;
    }

    
      //Creates a Placeholder Box for Chart
     
    private VBox createChartPlaceholder(String title) {
        VBox chartBox = new VBox(10);
        chartBox.getStyleClass().add("chart-box");
        chartBox.setPadding(new Insets(15));
        HBox.setHgrow(chartBox, Priority.ALWAYS);

        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("chart-title");

        Region chartRegion = new Region();
        chartRegion.setPrefHeight(300);
        chartRegion.getStyleClass().add("chart-region-placeholder");

        chartBox.getChildren().addAll(titleLabel, chartRegion);
        return chartBox;
    }
}
