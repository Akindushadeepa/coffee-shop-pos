package com.coffee_shop.view;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TextArea;
import javafx.print.PrinterJob;
import javafx.stage.Modality;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.converter.DoubleStringConverter;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.io.File;
import java.nio.file.Files;
import java.nio.charset.StandardCharsets;
import javafx.scene.control.Alert;

public class CoffeeShopDashboard {

	// Change this constant if your database name or credentials differ
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

	public void start(Stage stage) {
		this.primaryStage = stage;
		BorderPane root = new BorderPane();

		HBox topBar = new HBox(10);
		topBar.setPadding(new Insets(10));
		topBar.setAlignment(Pos.CENTER_LEFT);
		Label title = new Label("Admin Dashboard");
		title.setStyle("-fx-font-size:18px; -fx-font-weight:bold;");
		Button backBtn = new Button("⬅ Logout");
		backBtn.setOnAction(e -> {
			try {
				CoffeeShopApp app = new CoffeeShopApp();
				app.start(stage);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		});
		HBox spacer = new HBox();
		HBox.setHgrow(spacer, Priority.ALWAYS);
		topBar.getChildren().addAll(backBtn, title, spacer);

		TabPane tabs = new TabPane();
		Tab manageTab = new Tab("Manage Items");
		manageTab.setClosable(false);
		manageTab.setContent(createManageItemsPane());

		Tab salesTab = new Tab("Sales Reports");
		salesTab.setClosable(false);
		salesTab.setContent(createSalesReportsPane());

		tabs.getTabs().addAll(manageTab, salesTab);

		root.setTop(topBar);
		root.setCenter(tabs);

		Scene scene = new Scene(root, 1000, 700);
		try {
			String css = getClass().getResource("/dashboard.css").toExternalForm();
			scene.getStylesheets().add(css);
		} catch (Exception ex) {
			// ignore if css not found
		}

		stage.setTitle("Coffee Shop - Admin Dashboard");
		stage.setScene(scene);
		stage.show();
	}

	private VBox createManageItemsPane() {
		VBox root = new VBox(10);
		root.setPadding(new Insets(12));

		itemsTable = new TableView<>();
		TableColumn<FoodItem, Integer> idCol = new TableColumn<>("ID");
		idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
		idCol.setPrefWidth(80);

		TableColumn<FoodItem, String> nameCol = new TableColumn<>("Name");
		nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
		nameCol.setPrefWidth(360);
		nameCol.setCellFactory(TextFieldTableCell.forTableColumn());
		nameCol.setOnEditCommit(ev -> {
			FoodItem fi = ev.getRowValue();
			String newName = ev.getNewValue();
			fi.setName(newName);
			updateFoodInDb(fi);
			itemsTable.refresh();
		});

		TableColumn<FoodItem, Double> priceCol = new TableColumn<>("Price");
		priceCol.setCellValueFactory(new PropertyValueFactory<>("price"));
		priceCol.setPrefWidth(160);
		priceCol.setCellFactory(TextFieldTableCell.forTableColumn(new DoubleStringConverter()));
		priceCol.setOnEditCommit(ev -> {
			FoodItem fi = ev.getRowValue();
			Double v = ev.getNewValue();
			if (v != null) {
				fi.setPrice(v);
				updateFoodInDb(fi);
			}
			itemsTable.refresh();
		});

		itemsTable.getColumns().addAll(idCol, nameCol, priceCol);
		itemsTable.setEditable(true);
		itemsTable.setItems(items);

		HBox form = new HBox(8);
		TextField nameField = new TextField();
		nameField.setPromptText("Name");
		TextField priceField = new TextField();
		priceField.setPromptText("Price");
		Button addBtn = new Button("Add Item");
		Button deleteBtn = new Button("Delete Selected");

		addBtn.setOnAction(e -> {
			String name = nameField.getText().trim();
			String priceText = priceField.getText().trim();
			if (name.isEmpty() || priceText.isEmpty()) return;
			try {
				double p = Double.parseDouble(priceText);
				addFoodToDb(name, p);
				nameField.clear(); priceField.clear();
				loadItemsFromDb();
			} catch (NumberFormatException ex) {
				ex.printStackTrace();
			}
		});

		deleteBtn.setOnAction(e -> {
			FoodItem sel = itemsTable.getSelectionModel().getSelectedItem();
			if (sel != null) {
				deleteFoodFromDb(sel.getId());
				loadItemsFromDb();
			}
		});

		form.getChildren().addAll(nameField, priceField, addBtn, deleteBtn);

		root.getChildren().addAll(itemsTable, form);

		// initial load
		loadItemsFromDb();

		return root;
	}

	private VBox createSalesReportsPane() {
		VBox root = new VBox(10);
		root.setPadding(new Insets(12));

		HBox controls = new HBox(8);
		Button daily = new Button("Daily");
		Button weekly = new Button("Weekly");
		Button monthly = new Button("Monthly");

		Label resultLabel = new Label("Select a period to view sales.");

		TableView<java.util.Map<String, Object>> billsTable = new TableView<>();
		TableColumn<java.util.Map<String, Object>, String> bidCol = new TableColumn<>("Bill ID");
		TableColumn<java.util.Map<String, Object>, String> dateCol = new TableColumn<>("Date");
		TableColumn<java.util.Map<String, Object>, String> totCol = new TableColumn<>("Total");
		bidCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(String.valueOf(c.getValue().get("id"))));
		dateCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(String.valueOf(c.getValue().get("bill_date"))));
		totCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(String.valueOf(c.getValue().get("total"))));
		 billsTable.getColumns().addAll(bidCol, dateCol, totCol);

		 // double-click a bill to recreate and show the e-bill
		 billsTable.setOnMouseClicked(ev -> {
			 if (ev.getClickCount() == 2) {
				 java.util.Map<String,Object> sel = billsTable.getSelectionModel().getSelectedItem();
				 if (sel != null) {
					 String billId = String.valueOf(sel.get("id"));
					 showBillPopup(billId);
				 }
			 }
		 });

		controls.getChildren().addAll(daily, weekly, monthly);

		daily.setOnAction(e -> {
			LocalDate today = LocalDate.now();
			LocalDateTime start = today.atStartOfDay();
			LocalDateTime end = today.atTime(LocalTime.MAX);
			List<java.util.Map<String,Object>> rows = queryBillsBetween(start, end);
			double sum = rows.stream().mapToDouble(r -> ((Number)r.get("total")).doubleValue()).sum();
			resultLabel.setText(String.format("Daily Sales: Rs %.2f", sum));
			billsTable.getItems().setAll(rows);
		});

		weekly.setOnAction(e -> {
			LocalDate today = LocalDate.now();
			LocalDateTime start = today.minusDays(6).atStartOfDay();
			LocalDateTime end = today.atTime(LocalTime.MAX);
			List<java.util.Map<String,Object>> rows = queryBillsBetween(start, end);
			double sum = rows.stream().mapToDouble(r -> ((Number)r.get("total")).doubleValue()).sum();
			resultLabel.setText(String.format("Weekly Sales: Rs %.2f", sum));
			billsTable.getItems().setAll(rows);
		});

		monthly.setOnAction(e -> {
			LocalDate today = LocalDate.now();
			LocalDate first = today.withDayOfMonth(1);
			LocalDateTime start = first.atStartOfDay();
			LocalDateTime end = today.atTime(LocalTime.MAX);
			List<java.util.Map<String,Object>> rows = queryBillsBetween(start, end);
			double sum = rows.stream().mapToDouble(r -> ((Number)r.get("total")).doubleValue()).sum();
			resultLabel.setText(String.format("Monthly Sales: Rs %.2f", sum));
			billsTable.getItems().setAll(rows);
		});

		root.getChildren().addAll(controls, resultLabel, billsTable);
		return root;
	}

	// DB helpers
	private Connection getConnection() throws Exception {
		Class.forName("com.mysql.cj.jdbc.Driver");
		return DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
	}

	private void loadItemsFromDb() {
		items.clear();
		try (Connection c = getConnection()) {
			String sql = "SELECT id, fName, fPrice FROM food";
			try (PreparedStatement ps = c.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					items.add(new FoodItem(rs.getInt("id"), rs.getString("fName"), rs.getDouble("fPrice")));
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private void addFoodToDb(String name, double price) {
		try (Connection c = getConnection()) {
			String sql = "INSERT INTO food (fName, fPrice) VALUES (?, ?)";
			try (PreparedStatement ps = c.prepareStatement(sql)) {
				ps.setString(1, name);
				ps.setDouble(2, price);
				ps.executeUpdate();
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private void updateFoodInDb(FoodItem fi) {
		try (Connection c = getConnection()) {
			String sql = "UPDATE food SET fName = ?, fPrice = ? WHERE id = ?";
			try (PreparedStatement ps = c.prepareStatement(sql)) {
				ps.setString(1, fi.getName());
				ps.setDouble(2, fi.getPrice());
				ps.setInt(3, fi.getId());
				ps.executeUpdate();
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private void deleteFoodFromDb(int id) {
		try (Connection c = getConnection()) {
			String sql = "DELETE FROM food WHERE id = ?";
			try (PreparedStatement ps = c.prepareStatement(sql)) {
				ps.setInt(1, id);
				ps.executeUpdate();
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private List<java.util.Map<String,Object>> queryBillsBetween(LocalDateTime start, LocalDateTime end) {
		List<java.util.Map<String,Object>> rows = new ArrayList<>();
		try (Connection c = getConnection()) {
			// Ensure bills table exists (if not, return empty list)
			try (Statement s = c.createStatement()) {
				// no-op; if bills doesn't exist an exception will be thrown and caught below
			}

			String sql = "SELECT id, bill_date, total FROM bills WHERE bill_date BETWEEN ? AND ? ORDER BY bill_date DESC";
			try (PreparedStatement ps = c.prepareStatement(sql)) {
				ps.setObject(1, java.sql.Timestamp.valueOf(start));
				ps.setObject(2, java.sql.Timestamp.valueOf(end));
				try (ResultSet rs = ps.executeQuery()) {
					while (rs.next()) {
						java.util.Map<String,Object> m = new java.util.HashMap<>();
						m.put("id", rs.getString("id"));
						m.put("bill_date", rs.getTimestamp("bill_date").toString());
						m.put("total", rs.getDouble("total"));
						rows.add(m);
					}
				}
			}
		} catch (Exception ex) {
			// if the tables don't exist yet, just return empty result gracefully
			ex.printStackTrace();
		}
		return rows;
	}

	// Show stored e-bill file if available; otherwise fall back to DB reconstruction
	private void showBillPopup(String billId) {
		StringBuilder sb = new StringBuilder();

		// Try to read saved ebill file first (filename: bill_<billId>.txt)
		File dir = new File("ebills");
		File f = new File(dir, "bill_" + billId + ".txt");
		if (f.exists() && f.isFile()) {
			try {
				byte[] bytes = Files.readAllBytes(f.toPath());
				String content = new String(bytes, StandardCharsets.UTF_8);
				sb.append(content);
			} catch (Exception ex) {
				sb.append("Could not read stored e-bill: ").append(ex.getMessage()).append("\n");
				ex.printStackTrace();
			}
		} else {
			// If file missing alert
			Alert a = new Alert(Alert.AlertType.WARNING);
			a.initOwner(primaryStage);
			a.setTitle("E-Bill Not Found");
			a.setHeaderText("Stored e-bill not found");
			a.setContentText("No stored e-bill file was found for bill ID: " + billId + ".\nPlease verify the bill files in the 'ebills' folder.");
			a.showAndWait();
			return;
		}

		Stage billStage = new Stage();
		if (primaryStage != null) billStage.initOwner(primaryStage);
		billStage.initModality(Modality.APPLICATION_MODAL);
		billStage.setTitle("E-Bill - " + billId);

		TextArea ta = new TextArea(sb.toString());
		ta.setEditable(false);
		ta.setWrapText(true);
		ta.setPrefWidth(580);
		ta.setPrefHeight(520);

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
		} catch (Exception ex) {
			// ignore
		}

		billStage.setScene(billScene);
		billStage.show();

		printBtn.setOnAction(ev -> {
			PrinterJob job = PrinterJob.createPrinterJob();
			if (job != null) {
				boolean proceed = job.showPrintDialog(billStage);
				if (proceed) {
					boolean printed = job.printPage(ta);
					if (printed) job.endJob();
				}
			}
		});

		closeBtn.setOnAction(ev -> billStage.close());
	}

}
