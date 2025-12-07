package com.coffee_shop.view;

import com.coffee_shop.model.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.layout.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.converter.DoubleStringConverter;

import java.io.*;
import java.nio.file.*;
import java.sql.*;
import java.time.*;
import java.util.*;

public class CoffeeShopDashboard {

    private static final String DB_URL = "jdbc:mysql://localhost:3306/restaurant?serverTimezone=UTC";
    private static final String DB_USER = "root";
    private static final String DB_PASS = "";
    private static final String IMAGES_DIR = "item_images";

    public static class FoodItem {
        private final int id;
        private String name;
        private double price;
        private String imagePath;
        private boolean available;
        private String category;

        public FoodItem(int id, String name, double price, String imagePath, boolean available, String category) {
            this.id = id;
            this.name = name;
            this.price = price;
            this.imagePath = imagePath;
            this.available = available;
            this.category = category;
        }

        public int getId() { return id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public double getPrice() { return price; }
        public void setPrice(double price) { this.price = price; }
        public String getImagePath() { return imagePath; }
        public void setImagePath(String imagePath) { this.imagePath = imagePath; }
        public boolean isAvailable() { return available; }
        public void setAvailable(boolean available) { this.available = available; }
        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }
    }

    public static class UserRow {
        private String username;
        private String password;
        private String role;

        public UserRow(String username, String password, String role) {
            this.username = username;
            this.password = password;
            this.role = role;
        }

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }
    }

    private TableView<FoodItem> itemsTable;
    private ObservableList<FoodItem> items = FXCollections.observableArrayList();
    private TableView<UserRow> usersTable;
    private ObservableList<UserRow> users = FXCollections.observableArrayList();
    private UsersRepository usersRepo = new UsersRepository();
    private Stage primaryStage;
    private TextField nameField, priceField;
    private ComboBox<String> categoryCombo;
    private CheckBox availableCheck;
    private Label imagePreviewLabel;
    private String selectedImagePath = null;

    public void start(Stage stage) {
        this.primaryStage = stage;
        new File(IMAGES_DIR).mkdirs();
        
        BorderPane root = new BorderPane();
        root.getStyleClass().add("main-content-area");

        HBox topBar = createTopBar(stage);
        TabPane tabs = new TabPane();
        tabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        Tab manageTab = new Tab("📦 Manage Items");
        manageTab.setContent(createManageItemsPane());

        Tab salesTab = new Tab("📊 Sales Reports");
        salesTab.setContent(createSalesReportsPane());

        Tab usersTab = new Tab("👥 Manage Users");
        usersTab.setContent(createManageUsersPane());

        tabs.getTabs().addAll(manageTab, salesTab, usersTab);

        root.setTop(topBar);
        root.setCenter(tabs);

        Scene scene = new Scene(root, 1200, 800);
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
                new CoffeeShopApp().start(stage);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        Label title = new Label("👨‍💼 Admin Dashboard");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        HBox spacer = new HBox();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        topBar.getChildren().addAll(backBtn, title, spacer);
        return topBar;
    }

    private VBox createManageItemsPane() {
        VBox root = new VBox(15);
        root.setPadding(new Insets(20));

        Label sectionTitle = new Label("Menu Items Management");
        sectionTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        VBox addItemCard = createAddItemCard();
        VBox tableSection = createItemsTableSection();

        root.getChildren().addAll(sectionTitle, addItemCard, tableSection);
        loadItemsFromDb();

        return root;
    }

    private VBox createAddItemCard() {
        VBox card = new VBox(15);
        card.setPadding(new Insets(20));
        card.getStyleClass().add("stat-card");
        card.setStyle("-fx-background-color: #E3F2FD; -fx-border-color: #2196F3; -fx-border-width: 2px;");

        Label cardTitle = new Label("➕ Add New Menu Item");
        cardTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #1976D2;");

        GridPane form = new GridPane();
        form.setHgap(15);
        form.setVgap(12);

        Label nameLabel = new Label("Item Name:");
        nameField = new TextField();
        nameField.setPromptText("e.g., Cappuccino");

        Label catLabel = new Label("Category:");
        categoryCombo = new ComboBox<>();
        categoryCombo.getItems().addAll("Hot Coffee", "Cold Coffee", "Hot Drinks", "Bakery", "Food", "Dessert");
        categoryCombo.setValue("Hot Coffee");

        Label priceLabel = new Label("Price:");
        priceField = new TextField();
        priceField.setPromptText("350.00");

        availableCheck = new CheckBox("Available");
        availableCheck.setSelected(true);

        Button uploadBtn = new Button("📁 Image");
        uploadBtn.setOnAction(e -> chooseImage());
        
        imagePreviewLabel = new Label("No image");
        
        Button addBtn = new Button("✓ Add");
        addBtn.setOnAction(e -> handleAddItem());

        Button clearBtn = new Button("✗ Clear");
        clearBtn.setOnAction(e -> clearForm());

        form.add(nameLabel, 0, 0);
        form.add(nameField, 1, 0);
        form.add(catLabel, 2, 0);
        form.add(categoryCombo, 3, 0);
        form.add(priceLabel, 0, 1);
        form.add(priceField, 1, 1);
        form.add(availableCheck, 2, 1);
        form.add(uploadBtn, 0, 2);
        form.add(imagePreviewLabel, 1, 2);
        form.add(addBtn, 0, 3);
        form.add(clearBtn, 1, 3);

        card.getChildren().addAll(cardTitle, form);
        return card;
    }

    private void chooseImage() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Select Image");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg"));
        File file = fc.showOpenDialog(primaryStage);
        if (file != null) {
            try {
                String fileName = System.currentTimeMillis() + "_" + file.getName();
                Path target = Paths.get(IMAGES_DIR, fileName);
                Files.copy(file.toPath(), target, StandardCopyOption.REPLACE_EXISTING);
                selectedImagePath = fileName;
                imagePreviewLabel.setText("✓ " + file.getName());
            } catch (IOException ex) {
                showError("Error", "Could not save image");
            }
        }
    }

    private void clearForm() {
        nameField.clear();
        priceField.clear();
        categoryCombo.setValue("Hot Coffee");
        availableCheck.setSelected(true);
        selectedImagePath = null;
        imagePreviewLabel.setText("No image");
    }

    private VBox createItemsTableSection() {
        VBox section = new VBox(10);

        itemsTable = new TableView<>();
        itemsTable.setEditable(true);

        TableColumn<FoodItem, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        idCol.setPrefWidth(50);

        TableColumn<FoodItem, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        nameCol.setPrefWidth(200);

        TableColumn<FoodItem, String> catCol = new TableColumn<>("Category");
        catCol.setCellValueFactory(new PropertyValueFactory<>("category"));
        catCol.setPrefWidth(120);

        TableColumn<FoodItem, Double> priceCol = new TableColumn<>("Price");
        priceCol.setCellValueFactory(new PropertyValueFactory<>("price"));
        priceCol.setPrefWidth(100);

        TableColumn<FoodItem, Boolean> availCol = new TableColumn<>("Available");
        availCol.setCellValueFactory(new PropertyValueFactory<>("available"));
        availCol.setPrefWidth(80);

        TableColumn<FoodItem, Void> actionsCol = new TableColumn<>("Actions");
        actionsCol.setPrefWidth(150);
        actionsCol.setCellFactory(col -> new TableCell<FoodItem, Void>() {
            private final Button deleteBtn = new Button("🗑 Delete");
            {
                deleteBtn.setOnAction(e -> {
                    FoodItem item = getTableView().getItems().get(getIndex());
                    handleDeleteItem(item);
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : deleteBtn);
            }
        });

        itemsTable.getColumns().addAll(idCol, nameCol, catCol, priceCol, availCol, actionsCol);
        itemsTable.setItems(items);

        Button refreshBtn = new Button("🔄 Refresh");
        refreshBtn.setOnAction(e -> loadItemsFromDb());

        section.getChildren().addAll(refreshBtn, itemsTable);
        return section;
    }

    private void handleAddItem() {
        String name = nameField.getText().trim();
        String priceText = priceField.getText().trim();

        if (name.isEmpty() || priceText.isEmpty()) {
            showError("Error", "Fill all fields");
            return;
        }

        try {
            double price = Double.parseDouble(priceText);
            if (addFoodToDb(name, price, selectedImagePath, categoryCombo.getValue(), availableCheck.isSelected())) {
                showSuccess("Success", "Item added!");
                clearForm();
                loadItemsFromDb();
            }
        } catch (NumberFormatException ex) {
            showError("Error", "Invalid price");
        }
    }

    private void handleDeleteItem(FoodItem item) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Delete");
        confirm.setContentText("Delete " + item.getName() + "?");
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                deleteFoodFromDb(item.getId());
                loadItemsFromDb();
            }
        });
    }

    private VBox createManageUsersPane() {
        VBox root = new VBox(15);
        root.setPadding(new Insets(20));

        Label title = new Label("User Management");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        usersTable = new TableView<>();
        usersTable.setEditable(true);

        TableColumn<UserRow, String> usernameCol = new TableColumn<>("Username");
        usernameCol.setCellValueFactory(new PropertyValueFactory<>("username"));
        usernameCol.setPrefWidth(250);
        usernameCol.setCellFactory(TextFieldTableCell.forTableColumn());
        usernameCol.setOnEditCommit(e -> e.getRowValue().setUsername(e.getNewValue()));

        TableColumn<UserRow, String> passwordCol = new TableColumn<>("Password");
        passwordCol.setCellValueFactory(new PropertyValueFactory<>("password"));
        passwordCol.setPrefWidth(250);
        passwordCol.setCellFactory(TextFieldTableCell.forTableColumn());
        passwordCol.setOnEditCommit(e -> e.getRowValue().setPassword(e.getNewValue()));

        TableColumn<UserRow, String> roleCol = new TableColumn<>("Role");
        roleCol.setCellValueFactory(new PropertyValueFactory<>("role"));
        roleCol.setPrefWidth(200);
        roleCol.setCellFactory(ComboBoxTableCell.forTableColumn("ADMIN", "CASHIER", "KITCHEN"));
        roleCol.setOnEditCommit(e -> e.getRowValue().setRole(e.getNewValue()));

        usersTable.getColumns().addAll(usernameCol, passwordCol, roleCol);
        usersTable.setItems(users);

        HBox buttons = new HBox(10);
        
        Button addBtn = new Button("➕ Add");
        addBtn.setOnAction(e -> addNewUser());

        Button deleteBtn = new Button("🗑 Delete");
        deleteBtn.setOnAction(e -> deleteSelectedUser());

        Button refreshBtn = new Button("🔄 Refresh");
        refreshBtn.setOnAction(e -> loadUsersFromDb());

        Button saveBtn = new Button("💾 Save All");
        saveBtn.setOnAction(e -> saveAllUsers());

        buttons.getChildren().addAll(addBtn, deleteBtn, refreshBtn, saveBtn);

        root.getChildren().addAll(title, usersTable, buttons);
        loadUsersFromDb();

        return root;
    }

    private void addNewUser() {
        Dialog<UserRow> dialog = new Dialog<>();
        dialog.setTitle("Add User");

        ButtonType addType = new ButtonType("Add", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        TextField usernameField = new TextField();
        PasswordField passwordField = new PasswordField();
        ComboBox<String> roleCombo = new ComboBox<>();
        roleCombo.getItems().addAll("ADMIN", "CASHIER", "KITCHEN");
        roleCombo.setValue("CASHIER");

        grid.add(new Label("Username:"), 0, 0);
        grid.add(usernameField, 1, 0);
        grid.add(new Label("Password:"), 0, 1);
        grid.add(passwordField, 1, 1);
        grid.add(new Label("Role:"), 0, 2);
        grid.add(roleCombo, 1, 2);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(btn -> {
            if (btn == addType) {
                return new UserRow(usernameField.getText(), passwordField.getText(), roleCombo.getValue());
            }
            return null;
        });

        dialog.showAndWait().ifPresent(user -> {
            users.add(user);
            showInfo("Added", "User added to table");
        });
    }

    private void deleteSelectedUser() {
        UserRow selected = usersTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            users.remove(selected);
            showInfo("Removed", "User removed from table");
        }
    }

    private void loadUsersFromDb() {
        users.clear();
        try {
            List<Users> userList = usersRepo.getAllUsers();
            for (Users u : userList) {
                users.add(new UserRow(u.getUsername(), u.getPassword(), u.getRole()));
            }
        } catch (SQLException ex) {
            showError("Error", "Could not load users");
        }
    }

    private void saveAllUsers() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm");
        confirm.setContentText("Replace all users in database?");
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    List<Users> usersList = new ArrayList<>();
                    for (UserRow row : users) {
                        Users user;
                        switch (row.getRole().toUpperCase()) {
                            case "ADMIN":
                                user = new Admin(row.getUsername(), row.getPassword());
                                break;
                            case "KITCHEN":
                                user = new Kitchen(row.getUsername(), row.getPassword());
                                break;
                            default:
                                user = new Cashier(row.getUsername(), row.getPassword());
                        }
                        usersList.add(user);
                    }
                    usersRepo.replaceUsers(usersList);
                    showSuccess("Success", "Users saved!");
                    loadUsersFromDb();
                } catch (SQLException ex) {
                    showError("Error", "Could not save users");
                }
            }
        });
    }

    private VBox createSalesReportsPane() {
        VBox root = new VBox(15);
        root.setPadding(new Insets(20));

        Label title = new Label("Sales Reports");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        HBox controls = new HBox(10);
        Button dailyBtn = new Button("📅 Today");
        Button weeklyBtn = new Button("📅 Week");
        Button monthlyBtn = new Button("📅 Month");
        controls.getChildren().addAll(dailyBtn, weeklyBtn, monthlyBtn);

        Label resultLabel = new Label("Select period");

        TableView<Map<String, Object>> billsTable = new TableView<>();

        TableColumn<Map<String, Object>, String> idCol = new TableColumn<>("Bill ID");
        idCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(String.valueOf(c.getValue().get("id"))));

        TableColumn<Map<String, Object>, String> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(String.valueOf(c.getValue().get("bill_date"))));

        TableColumn<Map<String, Object>, String> totalCol = new TableColumn<>("Total");
        totalCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(String.format("%.2f", c.getValue().get("total"))));

        billsTable.getColumns().addAll(idCol, dateCol, totalCol);

        // Open bill file on double-click of a row (bill id)
        billsTable.setRowFactory(tv -> {
            TableRow<Map<String, Object>> row = new TableRow<>();
            row.setOnMouseClicked(e -> {
                if (e.getClickCount() == 2 && !row.isEmpty()) {
                    Map<String, Object> rowData = row.getItem();
                    String id = String.valueOf(rowData.get("id"));
                    showBillFromFile(id);
                }
            });
            return row;
        });

        dailyBtn.setOnAction(e -> loadSales(billsTable, resultLabel, "daily"));
        weeklyBtn.setOnAction(e -> loadSales(billsTable, resultLabel, "weekly"));
        monthlyBtn.setOnAction(e -> loadSales(billsTable, resultLabel, "monthly"));

        root.getChildren().addAll(title, controls, resultLabel, billsTable);
        return root;
    }

    // Diagnostic removed: test DB button and helper were temporary for debugging

    private void loadSales(TableView<Map<String, Object>> table, Label label, String period) {
        LocalDateTime start, end;
        LocalDate today = LocalDate.now();
        
        if ("daily".equals(period)) {
            start = today.atStartOfDay();
            end = today.atTime(LocalTime.MAX);
        } else if ("weekly".equals(period)) {
            start = today.minusDays(6).atStartOfDay();
            end = today.atTime(LocalTime.MAX);
        } else {
            start = today.withDayOfMonth(1).atStartOfDay();
            end = today.atTime(LocalTime.MAX);
        }

        List<Map<String, Object>> bills = queryBills(start, end);
        double total = bills.stream().mapToDouble(b -> ((Number) b.get("total")).doubleValue()).sum();
        String periodLabel = period.substring(0,1).toUpperCase() + period.substring(1);
        label.setText(String.format("%s: Rs %.2f (%d bills)", periodLabel, total, bills.size()));
        table.getItems().setAll(bills);
    }

    private Connection getConnection() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            return DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
        } catch (ClassNotFoundException e) {
            throw new SQLException("MySQL Driver not found!");
        }
    }

    private void loadItemsFromDb() {
        items.clear();
        try (Connection conn = getConnection()) {
            String sql = "SELECT id, fName, fPrice, image_path, available, category FROM food ORDER BY fName";
            try (PreparedStatement ps = conn.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    items.add(new FoodItem(
                        rs.getInt("id"),
                        rs.getString("fName"),
                        rs.getDouble("fPrice"),
                        rs.getString("image_path"),
                        rs.getBoolean("available"),
                        rs.getString("category")
                    ));
                }
            }
        } catch (SQLException ex) {
            showError("Error", "Could not load items");
        }
    }

    private boolean addFoodToDb(String name, double price, String imagePath, String category, boolean available) {
        try (Connection conn = getConnection()) {
            String sql = "INSERT INTO food (fName, fPrice, image_path, category, available) VALUES (?, ?, ?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, name);
                ps.setDouble(2, price);
                ps.setString(3, imagePath);
                ps.setString(4, category);
                ps.setBoolean(5, available);
                return ps.executeUpdate() > 0;
            }
        } catch (SQLException ex) {
            showError("Error", "Could not add item");
            return false;
        }
    }

    private void deleteFoodFromDb(int id) {
        try (Connection conn = getConnection()) {
            String sql = "DELETE FROM food WHERE id=?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, id);
                ps.executeUpdate();
            }
        } catch (SQLException ex) {
            showError("Error", "Could not delete");
        }
    }

    private List<Map<String, Object>> queryBills(LocalDateTime start, LocalDateTime end) {
        List<Map<String, Object>> bills = new ArrayList<>();
        try (Connection conn = getConnection()) {
            // Use DATE(bill_date) comparison to avoid timezone/precision issues with DATETIME/TIMESTAMP
            String sql = "SELECT id, bill_date, total FROM bills WHERE DATE(bill_date) BETWEEN ? AND ? ORDER BY bill_date DESC";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                java.sql.Date sd = java.sql.Date.valueOf(start.toLocalDate());
                java.sql.Date ed = java.sql.Date.valueOf(end.toLocalDate());
                ps.setDate(1, sd);
                ps.setDate(2, ed);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        Map<String, Object> bill = new HashMap<>();
                        bill.put("id", rs.getString("id"));
                        Timestamp t = rs.getTimestamp("bill_date");
                        bill.put("bill_date", (t == null ? "" : t.toString()));
                        bill.put("total", rs.getDouble("total"));
                        bills.add(bill);
                    }
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            showError("Database Error", "Could not load bills: " + ex.getMessage());
        }
        return bills;
    }

    private void showBillFromFile(String billId) {
        String fileName = "bill_" + billId + ".txt";
        List<Path> candidates = Arrays.asList(
                Paths.get("ebills", fileName),
                Paths.get("..", "ebills", fileName),
                Paths.get("target", "classes", "ebills", fileName),
                Paths.get("src", "main", "resources", "ebills", fileName)
        );

        Path found = null;
        for (Path p : candidates) {
            if (Files.exists(p)) { found = p; break; }
        }

        if (found == null) {
            showError("File Not Found", "Could not find bill file: " + fileName);
            return;
        }

        try {
            String content = String.join("\n", Files.readAllLines(found));

            // Build content with BILL ID header to match cashier UI
            String popupText = "BILL ID: " + billId + "\n\n" + content;

            TextArea ta = new TextArea(popupText);
            ta.setEditable(false);
            ta.setWrapText(true);
            ta.setPrefWidth(580);
            ta.setPrefHeight(520);

            Stage billStage = new Stage();
            billStage.initOwner(primaryStage);
            billStage.initModality(Modality.APPLICATION_MODAL);
            billStage.setTitle("E-Bill - " + billId);

            Button printBtn = new Button("Print");
            Button closeBtn = new Button("Close");

            HBox buttonsBox = new HBox(10, printBtn, closeBtn);
            buttonsBox.setAlignment(Pos.CENTER_RIGHT);

            VBox billRoot = new VBox(10, ta, buttonsBox);
            billRoot.setPadding(new Insets(10));

            Scene billScene = new Scene(billRoot, 600, 600);
            try {
                String css = getClass().getResource("/dashboard.css").toExternalForm();
                billScene.getStylesheets().add(css);
            } catch (Exception ignored) {}

            printBtn.setOnAction(ev -> {
                javafx.print.PrinterJob job = javafx.print.PrinterJob.createPrinterJob();
                if (job != null) {
                    boolean proceed = job.showPrintDialog(primaryStage);
                    if (proceed) {
                        boolean printed = job.printPage(ta);
                        if (printed) job.endJob();
                    }
                }
            });

            closeBtn.setOnAction(ev -> billStage.close());

            billStage.setScene(billScene);
            billStage.showAndWait();
        } catch (IOException ex) {
            showError("Read Error", "Could not read file: " + ex.getMessage());
        }
    }

    private void loadCSS(Scene scene) {
        try {
            String css = getClass().getResource("/dashboard.css").toExternalForm();
            scene.getStylesheets().add(css);
        } catch (Exception ex) {
            System.err.println("CSS not found");
        }
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showSuccess(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
}