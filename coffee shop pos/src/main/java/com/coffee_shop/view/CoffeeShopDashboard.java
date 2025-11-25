package com.coffee_shop.view;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.converter.DoubleStringConverter;
import javafx.print.PrinterJob;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.io.File;
import java.nio.file.Files;
import java.nio.charset.StandardCharsets;

public class CoffeeShopDashboard {

    private static final String DB_URL = "jdbc:mysql://localhost:3306/restaurant?serverTimezone=UTC";
    private static final String DB_USER = "root";
    private static final String DB_PASS = "";

    public static class FoodItem {
        private final int id;
        private String name;
        private double price;

        public FoodItem(int id, String name, double price) {
            this.id = id;
            this.name = name;
            this.price = price;
        }

        public int getId() { return id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public double getPrice() { return price; }
        public void setPrice(double price) { this.price = price; }
    }

    private TableView<FoodItem> itemsTable;
    private ObservableList<FoodItem> items = FXCollections.observableArrayList();
    private Stage primaryStage;
    private TextField nameField;
    private TextField priceField;

    public void start(Stage stage) {
        this.primaryStage = stage;
        BorderPane root = new BorderPane();
        root.getStyleClass().add("main-content-area");

        // Top bar with title and logout button
        HBox topBar = createTopBar(stage);

        // Tab pane for different sections
        TabPane tabs = new TabPane();
        tabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        Tab manageTab = new Tab("📦 Manage Items");
        manageTab.setContent(createManageItemsPane());

        Tab salesTab = new Tab("📊 Sales Reports");
        salesTab.setContent(createSalesReportsPane());

        tabs.getTabs().addAll(manageTab, salesTab);

        root.setTop(topBar);
        root.setCenter(tabs);

        Scene scene = new Scene(root, 1100, 750);
        loadCSS(scene);

        stage.setTitle("Coffee Shop - Admin Dashboard");
        stage.setScene(scene);
        stage.show();
    }

    private HBox createTopBar(Stage stage) {
        HBox topBar = new HBox(10);
        topBar.setPadding(new Insets(10, 20, 10, 20));
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.getStyleClass().add("top-bar");

        Button backBtn = new Button("⬅ Logout");
        backBtn.getStyleClass().add("nav-link");
        backBtn.setOnAction(e -> {
            try {
                CoffeeShopApp app = new CoffeeShopApp();
                app.start(stage);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        Label title = new Label("👨‍💼 Admin Dashboard");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");
        title.getStyleClass().add("page-title");

        HBox spacer = new HBox();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        topBar.getChildren().addAll(backBtn, title, spacer);
        return topBar;
    }

    private VBox createManageItemsPane() {
        VBox root = new VBox(15);
        root.setPadding(new Insets(20));

        // Title section
        Label sectionTitle = new Label("Menu Items Management");
        sectionTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        // Add new item form (prominent card)
        VBox addItemCard = createAddItemCard();

        // Items table
        VBox tableSection = createItemsTableSection();

        root.getChildren().addAll(sectionTitle, addItemCard, tableSection);

        // Load initial data
        loadItemsFromDb();

        return root;
    }

    private VBox createAddItemCard() {
        VBox card = new VBox(12);
        card.setPadding(new Insets(20));
        card.getStyleClass().add("stat-card");
        card.setStyle("-fx-background-color: #E3F2FD; -fx-border-color: #2196F3; -fx-border-width: 2px;");

        Label cardTitle = new Label("➕ Add New Menu Item");
        cardTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #1976D2;");

        GridPane form = new GridPane();
        form.setHgap(15);
        form.setVgap(12);
        form.setPadding(new Insets(10, 0, 0, 0));

        // Item Name
        Label nameLabel = new Label("Item Name:");
        nameLabel.setStyle("-fx-font-weight: bold;");
        nameField = new TextField();
        nameField.setPromptText("e.g., Cappuccino, Sandwich");
        nameField.setPrefWidth(300);
        nameField.setStyle("-fx-font-size: 14px; -fx-pref-height: 35px;");

        // Price
        Label priceLabel = new Label("Price (Rs):");
        priceLabel.setStyle("-fx-font-weight: bold;");
        priceField = new TextField();
        priceField.setPromptText("e.g., 350.00");
        priceField.setPrefWidth(150);
        priceField.setStyle("-fx-font-size: 14px; -fx-pref-height: 35px;");

        // Add validation for price field (numbers only)
        priceField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d*(\\.\\d*)?")) {
                priceField.setText(oldVal);
            }
        });

        // Buttons
        Button addBtn = new Button("✓ Add Item");
        addBtn.getStyleClass().add("logout-button");
        addBtn.setStyle("-fx-font-size: 14px; -fx-pref-height: 40px; -fx-pref-width: 140px;");
        addBtn.setOnAction(e -> handleAddItem());

        Button clearBtn = new Button("✗ Clear");
        clearBtn.getStyleClass().add("nav-link");
        clearBtn.setStyle("-fx-font-size: 14px; -fx-pref-height: 40px; -fx-pref-width: 100px;");
        clearBtn.setOnAction(e -> {
            nameField.clear();
            priceField.clear();
            nameField.requestFocus();
        });

        HBox buttonBox = new HBox(10, addBtn, clearBtn);
        buttonBox.setAlignment(Pos.CENTER_LEFT);

        form.add(nameLabel, 0, 0);
        form.add(nameField, 1, 0);
        form.add(priceLabel, 2, 0);
        form.add(priceField, 3, 0);
        form.add(buttonBox, 1, 1, 3, 1);

        // Quick tips
        Label tipsLabel = new Label("💡 Tip: Double-click on table cells below to edit existing items");
        tipsLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #666666; -fx-font-style: italic;");

        card.getChildren().addAll(cardTitle, form, tipsLabel);
        return card;
    }

    private VBox createItemsTableSection() {
        VBox section = new VBox(10);

        // Section header with item count
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);

        Label tableTitle = new Label("Current Menu Items");
        tableTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        Label itemCount = new Label();
        itemCount.setStyle("-fx-font-size: 14px; -fx-text-fill: #666666;");
        items.addListener((javafx.collections.ListChangeListener.Change<? extends FoodItem> c) -> {
            itemCount.setText("(" + items.size() + " items)");
        });

        Button refreshBtn = new Button("🔄 Refresh");
        refreshBtn.getStyleClass().add("nav-link");
        refreshBtn.setOnAction(e -> loadItemsFromDb());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        header.getChildren().addAll(tableTitle, itemCount, spacer, refreshBtn);

        // Create table
        itemsTable = new TableView<>();
        itemsTable.setEditable(true);
        itemsTable.setPrefHeight(400);

        // ID Column
        TableColumn<FoodItem, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        idCol.setPrefWidth(80);
        idCol.setStyle("-fx-alignment: CENTER;");

        // Name Column (editable)
        TableColumn<FoodItem, String> nameCol = new TableColumn<>("Item Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        nameCol.setPrefWidth(400);
        nameCol.setCellFactory(TextFieldTableCell.forTableColumn());
        nameCol.setOnEditCommit(event -> {
            FoodItem item = event.getRowValue();
            String newName = event.getNewValue();
            if (newName != null && !newName.trim().isEmpty()) {
                item.setName(newName.trim());
                updateFoodInDb(item);
                showInfo("Updated", "Item name updated successfully!");
            } else {
                showError("Invalid Name", "Item name cannot be empty!");
                loadItemsFromDb();
            }
        });

        // Price Column (editable)
        TableColumn<FoodItem, Double> priceCol = new TableColumn<>("Price (Rs)");
        priceCol.setCellValueFactory(new PropertyValueFactory<>("price"));
        priceCol.setPrefWidth(150);
        priceCol.setStyle("-fx-alignment: CENTER-RIGHT;");
        priceCol.setCellFactory(TextFieldTableCell.forTableColumn(new DoubleStringConverter()));
        priceCol.setOnEditCommit(event -> {
            FoodItem item = event.getRowValue();
            Double newPrice = event.getNewValue();
            if (newPrice != null && newPrice > 0) {
                item.setPrice(newPrice);
                updateFoodInDb(item);
                showInfo("Updated", "Price updated successfully!");
            } else {
                showError("Invalid Price", "Price must be greater than 0!");
                loadItemsFromDb();
            }
        });

        // Actions Column
        TableColumn<FoodItem, Void> actionsCol = new TableColumn<>("Actions");
        actionsCol.setPrefWidth(150);
        actionsCol.setCellFactory(col -> new TableCell<FoodItem, Void>() {
            private final Button deleteBtn = new Button("🗑 Delete");

            {
                deleteBtn.getStyleClass().add("nav-link");
                deleteBtn.setStyle("-fx-background-color: #f44336; -fx-text-fill: white;");
                deleteBtn.setOnAction(e -> {
                    FoodItem item = getTableView().getItems().get(getIndex());
                    handleDeleteItem(item);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : deleteBtn);
                setAlignment(Pos.CENTER);
            }
        });

        itemsTable.getColumns().addAll(idCol, nameCol, priceCol, actionsCol);
        itemsTable.setItems(items);

        section.getChildren().addAll(header, itemsTable);
        return section;
    }

    private void handleAddItem() {
        String name = nameField.getText().trim();
        String priceText = priceField.getText().trim();

        // Validation
        if (name.isEmpty()) {
            showError("Validation Error", "Please enter item name!");
            nameField.requestFocus();
            return;
        }

        if (priceText.isEmpty()) {
            showError("Validation Error", "Please enter price!");
            priceField.requestFocus();
            return;
        }

        try {
            double price = Double.parseDouble(priceText);
            if (price <= 0) {
                showError("Validation Error", "Price must be greater than 0!");
                priceField.requestFocus();
                return;
            }

            // Add to database
            boolean success = addFoodToDb(name, price);
            if (success) {
                showSuccess("Success!", "Item '" + name + "' added successfully!");
                nameField.clear();
                priceField.clear();
                nameField.requestFocus();
                loadItemsFromDb();
            } else {
                showError("Error", "Failed to add item to database!");
            }

        } catch (NumberFormatException ex) {
            showError("Invalid Price", "Please enter a valid number for price!");
            priceField.requestFocus();
        }
    }

    private void handleDeleteItem(FoodItem item) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Delete");
        confirm.setHeaderText("Delete Item: " + item.getName());
        confirm.setContentText("Are you sure you want to delete this item?\nThis action cannot be undone.");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                boolean success = deleteFoodFromDb(item.getId());
                if (success) {
                    showInfo("Deleted", "Item deleted successfully!");
                    loadItemsFromDb();
                } else {
                    showError("Error", "Failed to delete item!");
                }
            }
        });
    }

    private VBox createSalesReportsPane() {
        VBox root = new VBox(15);
        root.setPadding(new Insets(20));

        Label sectionTitle = new Label("Sales Reports");
        sectionTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        // Period selection buttons
        HBox controls = new HBox(12);
        Button dailyBtn = new Button("📅 Today");
        Button weeklyBtn = new Button("📅 This Week");
        Button monthlyBtn = new Button("📅 This Month");

        dailyBtn.getStyleClass().add("logout-button");
        weeklyBtn.getStyleClass().add("logout-button");
        monthlyBtn.getStyleClass().add("logout-button");

        dailyBtn.setStyle("-fx-pref-width: 120px; -fx-pref-height: 40px;");
        weeklyBtn.setStyle("-fx-pref-width: 120px; -fx-pref-height: 40px;");
        monthlyBtn.setStyle("-fx-pref-width: 120px; -fx-pref-height: 40px;");

        controls.getChildren().addAll(dailyBtn, weeklyBtn, monthlyBtn);

        // Results display
        Label resultLabel = new Label("Select a period to view sales reports");
        resultLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        // Bills table
        TableView<java.util.Map<String, Object>> billsTable = new TableView<>();
        billsTable.setPrefHeight(400);

        TableColumn<java.util.Map<String, Object>, String> idCol = new TableColumn<>("Bill ID");
        idCol.setPrefWidth(150);
        idCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
            String.valueOf(c.getValue().get("id"))));

        TableColumn<java.util.Map<String, Object>, String> dateCol = new TableColumn<>("Date & Time");
        dateCol.setPrefWidth(200);
        dateCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
            String.valueOf(c.getValue().get("bill_date"))));

        TableColumn<java.util.Map<String, Object>, String> totalCol = new TableColumn<>("Total (Rs)");
        totalCol.setPrefWidth(150);
        totalCol.setStyle("-fx-alignment: CENTER-RIGHT;");
        totalCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
            String.format("%.2f", c.getValue().get("total"))));

        billsTable.getColumns().addAll(idCol, dateCol, totalCol);

        // Double-click to view bill
        billsTable.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                java.util.Map<String, Object> selected = billsTable.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    String billId = String.valueOf(selected.get("id"));
                    showBillPopup(billId);
                }
            }
        });

        Label tipLabel = new Label("💡 Double-click on any bill to view details");
        tipLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #666666; -fx-font-style: italic;");

        // Button actions
        dailyBtn.setOnAction(e -> {
            LocalDate today = LocalDate.now();
            LocalDateTime start = today.atStartOfDay();
            LocalDateTime end = today.atTime(LocalTime.MAX);
            List<java.util.Map<String, Object>> bills = queryBillsBetween(start, end);
            double sum = bills.stream().mapToDouble(r -> ((Number) r.get("total")).doubleValue()).sum();
            resultLabel.setText(String.format("Today's Sales: Rs %.2f (%d bills)", sum, bills.size()));
            billsTable.getItems().setAll(bills);
        });

        weeklyBtn.setOnAction(e -> {
            LocalDate today = LocalDate.now();
            LocalDateTime start = today.minusDays(6).atStartOfDay();
            LocalDateTime end = today.atTime(LocalTime.MAX);
            List<java.util.Map<String, Object>> bills = queryBillsBetween(start, end);
            double sum = bills.stream().mapToDouble(r -> ((Number) r.get("total")).doubleValue()).sum();
            resultLabel.setText(String.format("Last 7 Days Sales: Rs %.2f (%d bills)", sum, bills.size()));
            billsTable.getItems().setAll(bills);
        });

        monthlyBtn.setOnAction(e -> {
            LocalDate today = LocalDate.now();
            LocalDate first = today.withDayOfMonth(1);
            LocalDateTime start = first.atStartOfDay();
            LocalDateTime end = today.atTime(LocalTime.MAX);
            List<java.util.Map<String, Object>> bills = queryBillsBetween(start, end);
            double sum = bills.stream().mapToDouble(r -> ((Number) r.get("total")).doubleValue()).sum();
            resultLabel.setText(String.format("This Month's Sales: Rs %.2f (%d bills)", sum, bills.size()));
            billsTable.getItems().setAll(bills);
        });

        root.getChildren().addAll(sectionTitle, controls, resultLabel, billsTable, tipLabel);
        return root;
    }

    // Database Methods

    private Connection getConnection() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            return DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
        } catch (ClassNotFoundException e) {
            throw new SQLException("MySQL Driver not found!", e);
        }
    }

    private void loadItemsFromDb() {
        items.clear();
        try (Connection conn = getConnection()) {
            String sql = "SELECT id, fName, fPrice FROM food ORDER BY fName";
            try (PreparedStatement ps = conn.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    items.add(new FoodItem(
                        rs.getInt("id"),
                        rs.getString("fName"),
                        rs.getDouble("fPrice")
                    ));
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            showError("Database Error", "Could not load items: " + ex.getMessage());
        }
    }

    private boolean addFoodToDb(String name, double price) {
        try (Connection conn = getConnection()) {
            String sql = "INSERT INTO food (fName, fPrice) VALUES (?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, name);
                ps.setDouble(2, price);
                int rows = ps.executeUpdate();
                return rows > 0;
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            showError("Database Error", "Could not add item: " + ex.getMessage());
            return false;
        }
    }

    private void updateFoodInDb(FoodItem item) {
        try (Connection conn = getConnection()) {
            String sql = "UPDATE food SET fName = ?, fPrice = ? WHERE id = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, item.getName());
                ps.setDouble(2, item.getPrice());
                ps.setInt(3, item.getId());
                ps.executeUpdate();
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            showError("Database Error", "Could not update item: " + ex.getMessage());
        }
    }

    private boolean deleteFoodFromDb(int id) {
        try (Connection conn = getConnection()) {
            String sql = "DELETE FROM food WHERE id = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, id);
                int rows = ps.executeUpdate();
                return rows > 0;
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            showError("Database Error", "Could not delete item: " + ex.getMessage());
            return false;
        }
    }

    private List<java.util.Map<String, Object>> queryBillsBetween(LocalDateTime start, LocalDateTime end) {
        List<java.util.Map<String, Object>> bills = new ArrayList<>();
        try (Connection conn = getConnection()) {
            String sql = "SELECT id, bill_date, total FROM bills WHERE bill_date BETWEEN ? AND ? ORDER BY bill_date DESC";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setTimestamp(1, Timestamp.valueOf(start));
                ps.setTimestamp(2, Timestamp.valueOf(end));
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        java.util.Map<String, Object> bill = new java.util.HashMap<>();
                        bill.put("id", rs.getString("id"));
                        bill.put("bill_date", rs.getTimestamp("bill_date").toString());
                        bill.put("total", rs.getDouble("total"));
                        bills.add(bill);
                    }
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return bills;
    }

    private void showBillPopup(String billId) {
        File billFile = new File("ebills/bill_" + billId + ".txt");
        if (!billFile.exists()) {
            showError("Bill Not Found", "E-bill file not found for: " + billId);
            return;
        }

        try {
            String content = new String(Files.readAllBytes(billFile.toPath()), StandardCharsets.UTF_8);

            Stage popup = new Stage();
            popup.initOwner(primaryStage);
            popup.initModality(Modality.APPLICATION_MODAL);
            popup.setTitle("E-Bill: " + billId);

            TextArea textArea = new TextArea(content);
            textArea.setEditable(false);
            textArea.setWrapText(true);
            textArea.setPrefSize(600, 500);
            textArea.setStyle("-fx-font-family: 'Courier New', monospace; -fx-font-size: 13px;");

            Button printBtn = new Button("🖨 Print");
            printBtn.getStyleClass().add("logout-button");
            printBtn.setOnAction(e -> {
                PrinterJob job = PrinterJob.createPrinterJob();
                if (job != null && job.showPrintDialog(popup)) {
                    if (job.printPage(textArea)) {
                        job.endJob();
                    }
                }
            });

            Button closeBtn = new Button("Close");
            closeBtn.getStyleClass().add("nav-link");
            closeBtn.setOnAction(e -> popup.close());

            HBox buttons = new HBox(10, printBtn, closeBtn);
            buttons.setAlignment(Pos.CENTER_RIGHT);
            buttons.setPadding(new Insets(10));

            VBox layout = new VBox(10, textArea, buttons);
            layout.setPadding(new Insets(15));

            Scene scene = new Scene(layout);
            loadCSS(scene);
            popup.setScene(scene);
            popup.show();

        } catch (Exception ex) {
            ex.printStackTrace();
            showError("Error", "Could not read bill file: " + ex.getMessage());
        }
    }

    private void loadCSS(Scene scene) {
        try {
            String css = getClass().getResource("/dashboard.css").toExternalForm();
            scene.getStylesheets().add(css);
        } catch (Exception ex) {
            System.err.println("CSS not found: " + ex.getMessage());
        }
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.initOwner(primaryStage);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.initOwner(primaryStage);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showSuccess(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.initOwner(primaryStage);
        alert.setTitle(title);
        alert.setHeaderText("✓ " + title);
        alert.setContentText(message);
        alert.showAndWait();
    }
}