package com.coffee_shop.view;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.util.converter.IntegerStringConverter;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.Modality;
import javafx.print.PrinterJob;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.io.*;

public class CoffeeShopCashier {

    // Database configuration
    private static final String DB_URL = "jdbc:mysql://localhost:3306/restaurant?serverTimezone=UTC";
    private static final String DB_USER = "root";
    private static final String DB_PASS = "";

    public static class CartItem {
        private final SimpleIntegerProperty foodId = new SimpleIntegerProperty();
        private final SimpleStringProperty name = new SimpleStringProperty();
        private final SimpleIntegerProperty qty = new SimpleIntegerProperty(1);
        private final SimpleDoubleProperty unitPrice = new SimpleDoubleProperty();
        private final SimpleDoubleProperty total = new SimpleDoubleProperty();

        public CartItem(int foodId, String name, double unitPrice) {
            this.foodId.set(foodId);
            this.name.set(name);
            this.unitPrice.set(unitPrice);
            updateTotal();
            this.qty.addListener((o, oldV, newV) -> updateTotal());
        }

        public int getFoodId() { return foodId.get(); }
        public String getName() { return name.get(); }
        public int getQty() { return qty.get(); }
        public double getUnitPrice() { return unitPrice.get(); }
        public double getTotal() { return total.get(); }

        public void setQty(int q) { 
            if (q > 0) {
                this.qty.set(q); 
            }
        }
        
        public void incrementQty() { 
            this.qty.set(this.qty.get() + 1); 
        }

        private void updateTotal() { 
            this.total.set(this.qty.get() * this.unitPrice.get()); 
        }
    }

    public void start(Stage stage) {
        BorderPane root = new BorderPane();
        root.getStyleClass().add("main-content-area");

        // Top bar
        HBox topBar = createTopBar(stage);
        
        // Main content area
        BorderPane content = new BorderPane();
        
        // Left: Menu items from database
        VBox menuBox = createMenuPanel();
        
        // Center: Shopping cart table
        VBox centerBox = new VBox(10);
        centerBox.setPadding(new Insets(10));
        
        Label cartLabel = new Label("Shopping Cart");
        cartLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        
        TableView<CartItem> cartTable = createCartTable();
        
        // Grand total tracking
        DoubleProperty grandTotal = new SimpleDoubleProperty(0);
        
        centerBox.getChildren().addAll(cartLabel, cartTable);
        
        // Right: Totals and actions
        VBox rightBox = createActionsPanel(cartTable, grandTotal, stage);
        
        // Setup cart table editing
        setupCartTableEditing(cartTable, grandTotal);
        
        // Load menu items from database
        loadMenuItems(menuBox, cartTable, grandTotal);
        
        // Layout assembly
        content.setLeft(menuBox);
        content.setCenter(centerBox);
        content.setRight(rightBox);
        
        root.setTop(topBar);
        root.setCenter(content);

        Scene scene = new Scene(root, 1200, 750);
        loadCSS(scene);

        stage.setTitle("Coffee Shop POS - Cashier");
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
                showError("Navigation Error", "Could not return to login page.");
            }
        });

        Label title = new Label("☕ Cashier Point of Sale");
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold;");
        title.getStyleClass().add("page-title");

        HBox spacer = new HBox();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        topBar.getChildren().addAll(backBtn, title, spacer);
        return topBar;
    }

    private VBox createMenuPanel() {
        VBox menuBox = new VBox(10);
        menuBox.setPadding(new Insets(10));
        menuBox.setPrefWidth(280);
        menuBox.getStyleClass().add("sidebar");

        Label menuLabel = new Label("📋 Menu Items");
        menuLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: white;");
        menuBox.getChildren().add(menuLabel);

        return menuBox;
    }

    private TableView<CartItem> createCartTable() {
        TableView<CartItem> cartTable = new TableView<>();
        cartTable.setPrefHeight(500);
        cartTable.setEditable(true);

        TableColumn<CartItem, String> itemCol = new TableColumn<>("Item");
        itemCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        itemCol.setPrefWidth(280);

        TableColumn<CartItem, Integer> qtyCol = new TableColumn<>("Qty");
        qtyCol.setCellValueFactory(new PropertyValueFactory<>("qty"));
        qtyCol.setPrefWidth(80);
        qtyCol.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));

        TableColumn<CartItem, Double> unitCol = new TableColumn<>("Unit Price");
        unitCol.setCellValueFactory(new PropertyValueFactory<>("unitPrice"));
        unitCol.setPrefWidth(120);

        TableColumn<CartItem, Double> totalCol = new TableColumn<>("Total");
        totalCol.setCellValueFactory(new PropertyValueFactory<>("total"));
        totalCol.setPrefWidth(120);

        cartTable.getColumns().addAll(itemCol, qtyCol, unitCol, totalCol);
        return cartTable;
    }

    private VBox createActionsPanel(TableView<CartItem> cartTable, DoubleProperty grandTotal, Stage stage) {
        VBox rightBox = new VBox(15);
        rightBox.setPadding(new Insets(15));
        rightBox.setPrefWidth(260);
        rightBox.getStyleClass().add("stat-card");

        Label totalLabel = new Label("Total: Rs 0.00");
        totalLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");
        totalLabel.getStyleClass().add("card-data");

        grandTotal.addListener((obs, oldV, newV) -> 
            totalLabel.setText(String.format("Total: Rs %.2f", newV.doubleValue()))
        );

        Button removeBtn = new Button("🗑 Remove Selected");
        removeBtn.setMaxWidth(Double.MAX_VALUE);
        removeBtn.getStyleClass().add("nav-link");
        removeBtn.setOnAction(e -> {
            CartItem selected = cartTable.getSelectionModel().getSelectedItem();
            if (selected != null) {
                cartTable.getItems().remove(selected);
                recalculateTotal(cartTable, grandTotal);
            } else {
                showInfo("No Selection", "Please select an item to remove.");
            }
        });

        Button clearBtn = new Button("🧹 Clear Cart");
        clearBtn.setMaxWidth(Double.MAX_VALUE);
        clearBtn.getStyleClass().add("nav-link");
        clearBtn.setOnAction(e -> {
            if (!cartTable.getItems().isEmpty()) {
                Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                confirm.setTitle("Clear Cart");
                confirm.setHeaderText("Are you sure?");
                confirm.setContentText("This will remove all items from the cart.");
                confirm.showAndWait().ifPresent(response -> {
                    if (response == ButtonType.OK) {
                        cartTable.getItems().clear();
                        grandTotal.set(0);
                    }
                });
            }
        });

        Button checkoutBtn = new Button("💳 Checkout");
        checkoutBtn.setMaxWidth(Double.MAX_VALUE);
        checkoutBtn.getStyleClass().add("logout-button");
        checkoutBtn.setStyle("-fx-font-size: 16px; -fx-pref-height: 50px;");
        checkoutBtn.setOnAction(e -> processCheckout(cartTable, grandTotal, stage));

        rightBox.getChildren().addAll(
            new Label("Order Summary"),
            new Separator(),
            totalLabel,
            new Separator(),
            removeBtn,
            clearBtn,
            new Separator(),
            checkoutBtn
        );

        return rightBox;
    }

    private void setupCartTableEditing(TableView<CartItem> cartTable, DoubleProperty grandTotal) {
        TableColumn<CartItem, Integer> qtyCol = (TableColumn<CartItem, Integer>) cartTable.getColumns().get(1);
        qtyCol.setOnEditCommit(event -> {
            CartItem item = event.getRowValue();
            Integer newQty = event.getNewValue();
            if (newQty != null && newQty > 0) {
                item.setQty(newQty);
            } else {
                showError("Invalid Quantity", "Quantity must be greater than 0.");
                item.setQty(1);
            }
            cartTable.refresh();
            recalculateTotal(cartTable, grandTotal);
        });
    }

    private void loadMenuItems(VBox menuBox, TableView<CartItem> cartTable, DoubleProperty grandTotal) {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {
            String sql = "SELECT id, fName, fPrice FROM food ORDER BY fName";
            try (PreparedStatement ps = conn.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                
                while (rs.next()) {
                    int foodId = rs.getInt("id");
                    String name = rs.getString("fName");
                    double price = rs.getDouble("fPrice");
                    
                    Button itemBtn = new Button(String.format("%s\nRs %.2f", name, price));
                    itemBtn.setMaxWidth(Double.MAX_VALUE);
                    itemBtn.setMinHeight(50);
                    itemBtn.getStyleClass().add("nav-link");
                    itemBtn.setStyle("-fx-alignment: center;");
                    
                    itemBtn.setOnAction(e -> addItemToCart(cartTable, grandTotal, foodId, name, price));
                    menuBox.getChildren().add(itemBtn);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            showError("Database Error", "Could not load menu items from database.");
        }
    }

    private void addItemToCart(TableView<CartItem> cartTable, DoubleProperty grandTotal, 
                               int foodId, String name, double price) {
        // Check if item already exists in cart
        CartItem existingItem = null;
        for (CartItem item : cartTable.getItems()) {
            if (item.getFoodId() == foodId) {
                existingItem = item;
                break;
            }
        }

        if (existingItem != null) {
            existingItem.incrementQty();
            cartTable.refresh();
        } else {
            cartTable.getItems().add(new CartItem(foodId, name, price));
        }

        recalculateTotal(cartTable, grandTotal);
    }

    private void recalculateTotal(TableView<CartItem> cartTable, DoubleProperty grandTotal) {
        double sum = cartTable.getItems().stream()
                              .mapToDouble(CartItem::getTotal)
                              .sum();
        grandTotal.set(sum);
    }

    private void processCheckout(TableView<CartItem> cartTable, DoubleProperty grandTotal, Stage stage) {
        if (cartTable.getItems().isEmpty()) {
            showInfo("Empty Cart", "Please add items to cart before checkout.");
            return;
        }

        // Generate bill ID
        String billId = generateBillId();
        
        // Save to database
        boolean saved = saveBillToDatabase(billId, cartTable, grandTotal.get());
        
        if (!saved) {
            showError("Checkout Failed", "Could not save bill to database.");
            return;
        }

        // Generate bill text
        String billText = generateBillText(billId, cartTable, grandTotal.get());
        
        // Save to file
        saveBillToFile(billId, billText);
        
        // Show bill popup
        showBillPopup(stage, billId, billText, cartTable, grandTotal);
    }

    private String generateBillId() {
        String datePrefix = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyMMdd"));
        int sequence = 1;
        
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {
            String sql = "SELECT id FROM bills WHERE id LIKE ? ORDER BY id DESC LIMIT 1";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, datePrefix + "_%");
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        String lastId = rs.getString("id");
                        int idx = lastId.lastIndexOf('_');
                        if (idx >= 0) {
                            try {
                                sequence = Integer.parseInt(lastId.substring(idx + 1)) + 1;
                            } catch (NumberFormatException e) {
                                sequence = 1;
                            }
                        }
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        
        return String.format("%s_%d", datePrefix, sequence);
    }

    private boolean saveBillToDatabase(String billId, TableView<CartItem> cartTable, double total) {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {
            conn.setAutoCommit(false);
            
            try {
                // Insert bill
                String billSql = "INSERT INTO bills (id, bill_date, total) VALUES (?, NOW(), ?)";
                try (PreparedStatement ps = conn.prepareStatement(billSql)) {
                    ps.setString(1, billId);
                    ps.setDouble(2, total);
                    ps.executeUpdate();
                }
                
                // Insert bill items
                String itemSql = "INSERT INTO bill_items (bill_id, food_id, qty, unit_price, total) VALUES (?, ?, ?, ?, ?)";
                try (PreparedStatement ps = conn.prepareStatement(itemSql)) {
                    for (CartItem item : cartTable.getItems()) {
                        ps.setString(1, billId);
                        ps.setInt(2, item.getFoodId());
                        ps.setInt(3, item.getQty());
                        ps.setDouble(4, item.getUnitPrice());
                        ps.setDouble(5, item.getTotal());
                        ps.addBatch();
                    }
                    ps.executeBatch();
                }
                
                conn.commit();
                return true;
            } catch (Exception e) {
                conn.rollback();
                throw e;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    private String generateBillText(String billId, TableView<CartItem> cartTable, double total) {
        StringBuilder sb = new StringBuilder();
        sb.append("═══════════════════════════════════════════════\n");
        sb.append("           COFFEE SHOP E-BILL\n");
        sb.append("═══════════════════════════════════════════════\n");
        sb.append("Bill ID: ").append(billId).append("\n");
        sb.append("Date: ").append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))).append("\n");
        sb.append("───────────────────────────────────────────────\n");
        sb.append(String.format("%-30s %5s %10s %10s\n", "Item", "Qty", "Unit", "Total"));
        sb.append("───────────────────────────────────────────────\n");
        
        for (CartItem item : cartTable.getItems()) {
            sb.append(String.format("%-30s %5d %10.2f %10.2f\n", 
                item.getName(), item.getQty(), item.getUnitPrice(), item.getTotal()));
        }
        
        sb.append("═══════════════════════════════════════════════\n");
        sb.append(String.format("%52s %.2f\n", "GRAND TOTAL: Rs", total));
        sb.append("═══════════════════════════════════════════════\n");
        sb.append("\n         Thank you for your purchase!\n");
        sb.append("            Please visit again!\n");
        sb.append("═══════════════════════════════════════════════\n");
        
        return sb.toString();
    }

    private void saveBillToFile(String billId, String billText) {
        try {
            File dir = new File("ebills");
            if (!dir.exists()) dir.mkdirs();
            
            File billFile = new File(dir, "bill_" + billId + ".txt");
            try (PrintWriter pw = new PrintWriter(new OutputStreamWriter(
                    new FileOutputStream(billFile), "UTF-8"))) {
                pw.print(billText);
            }
            
            billFile.setWritable(false);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void showBillPopup(Stage owner, String billId, String billText, 
                               TableView<CartItem> cartTable, DoubleProperty grandTotal) {
        Stage billStage = new Stage();
        billStage.initOwner(owner);
        billStage.initModality(Modality.APPLICATION_MODAL);
        billStage.setTitle("E-Bill - " + billId);

        TextArea billArea = new TextArea(billText);
        billArea.setEditable(false);
        billArea.setWrapText(true);
        billArea.setPrefSize(600, 500);
        billArea.setStyle("-fx-font-family: 'Courier New', monospace; -fx-font-size: 13px;");

        Button printBtn = new Button("🖨 Print");
        printBtn.getStyleClass().add("logout-button");
        printBtn.setOnAction(e -> {
            PrinterJob job = PrinterJob.createPrinterJob();
            if (job != null && job.showPrintDialog(billStage)) {
                if (job.printPage(billArea)) {
                    job.endJob();
                    showInfo("Print Success", "Bill printed successfully!");
                }
            }
        });

        Button closeBtn = new Button("✓ Close");
        closeBtn.getStyleClass().add("nav-link");
        closeBtn.setOnAction(e -> {
            billStage.close();
            cartTable.getItems().clear();
            grandTotal.set(0);
        });

        HBox buttonBox = new HBox(10, printBtn, closeBtn);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        buttonBox.setPadding(new Insets(10));

        VBox root = new VBox(10, billArea, buttonBox);
        root.setPadding(new Insets(15));

        Scene scene = new Scene(root);
        loadCSS(scene);
        billStage.setScene(scene);
        billStage.show();
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
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}