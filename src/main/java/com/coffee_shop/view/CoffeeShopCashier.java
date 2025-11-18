package com.coffee_shop.view;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.util.converter.IntegerStringConverter;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class CoffeeShopCashier {

	public static class CartItem {
		private final SimpleStringProperty name = new SimpleStringProperty();
		private final SimpleIntegerProperty qty = new SimpleIntegerProperty(1);
		private final SimpleDoubleProperty unitPrice = new SimpleDoubleProperty();
		private final SimpleDoubleProperty total = new SimpleDoubleProperty();

		public CartItem(String name, double unitPrice) {
			this.name.set(name);
			this.unitPrice.set(unitPrice);
			updateTotal();
			this.qty.addListener((o, oldV, newV) -> updateTotal());
		}

		public String getName() { return name.get(); }
		public int getQty() { return qty.get(); }
		public double getUnitPrice() { return unitPrice.get(); }
		public double getTotal() { return total.get(); }

		public void setQty(int q) { this.qty.set(q); }
		public void incrementQty() { this.qty.set(this.qty.get() + 1); }

		private void updateTotal() { this.total.set(this.qty.get() * this.unitPrice.get()); }
	}

	public void start(Stage stage) {
		BorderPane root = new BorderPane();
		root.getStyleClass().add("main-content-area");

		// Top bar with title and back button
		HBox topBar = new HBox(10);
		topBar.setPadding(new Insets(10, 20, 10, 20));
		topBar.setAlignment(Pos.CENTER_LEFT);
		topBar.getStyleClass().add("top-bar");

		Label title = new Label("Cashier POS");
		title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");
		title.getStyleClass().add("page-title");

		Button backBtn = new Button("⬅ Back");
		backBtn.setOnAction(e -> {
			try {
				CoffeeShopApp App = new CoffeeShopApp();
				App.start(stage);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		});

		HBox spacer = new HBox();
		HBox.setHgrow(spacer, Priority.ALWAYS);

		topBar.getChildren().addAll(backBtn, title, spacer);

		// Left: menu buttons (loaded from DB)
		VBox menuBox = new VBox(10);
		menuBox.setPadding(new Insets(10));
		menuBox.setPrefWidth(260);
		menuBox.getStyleClass().add("sidebar");

		Label menuLabel = new Label("Menu Items");
		menuLabel.setStyle("-fx-font-weight: bold;");
		menuLabel.getStyleClass().add("card-title");
		menuBox.getChildren().add(menuLabel);

		// Center: cart as TableView with columns Item, Qty, UnitPrice, Total
		VBox centerBox = new VBox(10);
		centerBox.setPadding(new Insets(10));
		Label cartLabel = new Label("Cart");

		TableView<CartItem> cartTable = new TableView<>();
		cartTable.setPrefHeight(400);

		TableColumn<CartItem, String> itemCol = new TableColumn<>("Item");
		itemCol.setCellValueFactory(new PropertyValueFactory<>("name"));
		itemCol.setPrefWidth(240);

		TableColumn<CartItem, Integer> qtyCol = new TableColumn<>("Qty");
		qtyCol.setCellValueFactory(new PropertyValueFactory<>("qty"));
		qtyCol.setPrefWidth(60);
		// Make qty editable
		qtyCol.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));

		TableColumn<CartItem, Double> unitCol = new TableColumn<>("Unit");
		unitCol.setCellValueFactory(new PropertyValueFactory<>("unitPrice"));
		unitCol.setPrefWidth(80);

		TableColumn<CartItem, Double> totalCol = new TableColumn<>("Total");
		totalCol.setCellValueFactory(new PropertyValueFactory<>("total"));
		totalCol.setPrefWidth(100);

		cartTable.getColumns().add(itemCol);
		cartTable.getColumns().add(qtyCol);
		cartTable.getColumns().add(unitCol);
		cartTable.getColumns().add(totalCol);
		cartTable.setEditable(true);
		// grand total property (declared early so handlers can reference it)
		DoubleProperty grandTotal = new SimpleDoubleProperty(0);

		qtyCol.setOnEditCommit(ev -> {
			CartItem ci = ev.getRowValue();
			Integer newQty = ev.getNewValue();
			if (newQty != null && newQty > 0) {
				ci.setQty(newQty);
			}
			cartTable.refresh();
			double sum = 0; for (CartItem c : cartTable.getItems()) sum += c.getTotal();
			grandTotal.set(sum);
		});

		centerBox.getChildren().addAll(cartLabel, cartTable);

		// Right: totals and actions
		VBox rightBox = new VBox(12);
		rightBox.setPadding(new Insets(10));
		rightBox.setPrefWidth(240);
		rightBox.getStyleClass().add("stat-card");

		Label totalLabel = new Label("Total: $0.00");
		// bind listener for grand total now that totalLabel exists
		totalLabel.getStyleClass().add("card-data");
		grandTotal.addListener((obs, oldV, newV) -> totalLabel.setText(String.format("Total: $%.2f", newV.doubleValue())));

		Button removeBtn = new Button("Remove Selected");
		removeBtn.getStyleClass().add("nav-link");
		Button checkoutBtn = new Button("Checkout");
		checkoutBtn.getStyleClass().add("logout-button");

		rightBox.getChildren().addAll(totalLabel, removeBtn, checkoutBtn);

		java.util.function.BiConsumer<String, Double> addItem = (name, price) -> {
			// check if item exists in cart
			CartItem found = null;
			for (CartItem ci : cartTable.getItems()) {
				if (ci.getName().equals(name)) { found = ci; break; }
			}
			if (found != null) {
				found.incrementQty();
				// refresh table
				cartTable.refresh();
			} else {
				cartTable.getItems().add(new CartItem(name, price));
			}
			// recompute grand total
			double sum = 0;
			for (CartItem ci : cartTable.getItems()) sum += ci.getTotal();
			grandTotal.set(sum);
		};

		// Load menu items from local MySQL database `resturant.food`
		// Default XAMPP credentials: user=root, password="" (empty)
		try {
			Class.forName("com.mysql.cj.jdbc.Driver");
			try (java.sql.Connection conn = java.sql.DriverManager.getConnection(
					"jdbc:mysql://localhost:3306/restaurant?serverTimezone=UTC", "root", "")) {
				String sql = "SELECT id, fName, fPrice FROM food";
				try (java.sql.PreparedStatement ps = conn.prepareStatement(sql);
					 java.sql.ResultSet rs = ps.executeQuery()) {
					while (rs.next()) {
						String name = rs.getString("fName");
						double price = rs.getDouble("fPrice");
						Button itemBtn = new Button(String.format("%s - $%.2f", name, price));
						itemBtn.setMaxWidth(Double.MAX_VALUE);
						itemBtn.getStyleClass().add("nav-link");
						itemBtn.setOnAction(ev -> addItem.accept(name, price));
						menuBox.getChildren().add(itemBtn);
					}
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			menuBox.getChildren().add(new Label("Could not load menu from database."));
		}

		removeBtn.setOnAction(e -> {
			CartItem sel = cartTable.getSelectionModel().getSelectedItem();
			if (sel != null) {
				cartTable.getItems().remove(sel);
				double sum = 0; for (CartItem ci : cartTable.getItems()) sum += ci.getTotal();
				grandTotal.set(sum);
			}
		});

		checkoutBtn.setOnAction(e -> {
			if (cartTable.getItems().isEmpty()) {
				Alert a = new Alert(Alert.AlertType.INFORMATION, "Cart is empty.");
				a.showAndWait();
				return;
			}
			Alert a = new Alert(Alert.AlertType.INFORMATION, String.format("Payment received. Total: $%.2f", grandTotal.get()));
			a.showAndWait();
			cartTable.getItems().clear();
			grandTotal.set(0);
		});

		// Compose layout
		BorderPane content = new BorderPane();
		content.setLeft(menuBox);
		content.setCenter(centerBox);
		content.setRight(rightBox);

		root.setTop(topBar);
		root.setCenter(content);

		Scene scene = new Scene(root, 1100, 700);
		try {
			String css = getClass().getResource("/dashboard.css").toExternalForm();
			scene.getStylesheets().add(css);
		} catch (Exception ex) {
			System.err.println("Could not load CSS for cashier: " + ex.getMessage());
		}

		stage.setTitle("Coffee Shop POS - Cashier");
		stage.setScene(scene);
		stage.show();
	}
}
