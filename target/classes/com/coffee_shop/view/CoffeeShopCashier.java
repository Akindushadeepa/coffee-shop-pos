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
import javafx.scene.control.TextArea;
import javafx.print.PrinterJob;
import javafx.stage.Modality;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class CoffeeShopCashier {

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

		Label totalLabel = new Label("Total: Rs0.00");
		// bind listener for grand total now that totalLabel exists
		totalLabel.getStyleClass().add("card-data");
		grandTotal.addListener((obs, oldV, newV) -> totalLabel.setText(String.format("Total: Rs%.2f", newV.doubleValue())));

		Button removeBtn = new Button("Remove Selected");
		removeBtn.getStyleClass().add("nav-link");
		Button checkoutBtn = new Button("Checkout");
		checkoutBtn.getStyleClass().add("logout-button");

		rightBox.getChildren().addAll(totalLabel, removeBtn, checkoutBtn);

		// addItem now takes foodId, name, and price
		java.util.function.Consumer<java.util.AbstractMap.SimpleEntry<Integer, java.util.AbstractMap.SimpleEntry<String, Double>>> addItem = 
			(entry) -> {
			int foodId = entry.getKey();
			String name = entry.getValue().getKey();
			double price = entry.getValue().getValue();
			// check if item exists in cart
			CartItem found = null;
			for (CartItem ci : cartTable.getItems()) {
				if (ci.getFoodId() == foodId) { found = ci; break; }
			}
			if (found != null) {
				found.incrementQty();
				// refresh table
				cartTable.refresh();
			} else {
				cartTable.getItems().add(new CartItem(foodId, name, price));
			}
			// recompute grand total
			double sum = 0;
			for (CartItem ci : cartTable.getItems()) sum += ci.getTotal();
			grandTotal.set(sum);
		};

		// Load menu items from local MySQL database `restaurant.food`
		// Default XAMPP credentials: user=root, password=""
		try {
			Class.forName("com.mysql.cj.jdbc.Driver");
			try (java.sql.Connection conn = java.sql.DriverManager.getConnection(
					"jdbc:mysql://localhost:3306/restaurant?serverTimezone=UTC", "root", "")) {
				String sql = "SELECT id, fName, fPrice FROM food";
				try (java.sql.PreparedStatement ps = conn.prepareStatement(sql);
					 java.sql.ResultSet rs = ps.executeQuery()) {
					while (rs.next()) {
						int foodId = rs.getInt("id");
						String name = rs.getString("fName");
						double price = rs.getDouble("fPrice");
						Button itemBtn = new Button(String.format("%s - Rs%.2f", name, price));
						itemBtn.setMaxWidth(Double.MAX_VALUE);
						itemBtn.getStyleClass().add("nav-link");
						final int fId = foodId;
						final String fName = name;
						final double fPrice = price;
						itemBtn.setOnAction(ev -> addItem.accept(
							new java.util.AbstractMap.SimpleEntry<>(fId, 
								new java.util.AbstractMap.SimpleEntry<>(fName, fPrice))
						));
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

			// Ensure totals are up-to-date
			double finalTotal = 0;
			for (CartItem ci : cartTable.getItems()) finalTotal += ci.getTotal();
			grandTotal.set(finalTotal);

			// Build bill text
			StringBuilder sb = new StringBuilder();
			sb.append("COFFEE SHOP - E-BILL\n");
			sb.append("Date: ").append(java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))).append("\n");
			sb.append("-----------------------------------------------\n");
			sb.append(String.format("%-30s %5s %10s %10s\n", "Item", "Qty", "Unit", "Total"));
			sb.append("-----------------------------------------------\n");
			for (CartItem ci : cartTable.getItems()) {
				sb.append(String.format("%-30s %5d %10.2f %10.2f\n", ci.getName(), ci.getQty(), ci.getUnitPrice(), ci.getTotal()));
			}
			sb.append("-----------------------------------------------\n");
			sb.append(String.format("%47s %10.2f\n", "GRAND TOTAL:", grandTotal.get()));
			sb.append("\nThank you for your purchase!\n");

			// Save to file using bill id (six-digit date) and sequence if needed
			String billIdSix = java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyMMdd"));
			java.io.File dir = new java.io.File("ebills");
			if (!dir.exists()) dir.mkdirs();
			// determine sequence number for today's bills
			int seq = 1;
			// Prefer using the bills table to determine the next sequence to avoid relying on files.
			try {
				Class.forName("com.mysql.cj.jdbc.Driver");
				try (java.sql.Connection _conn = java.sql.DriverManager.getConnection(
						"jdbc:mysql://localhost:3306/restaurant?serverTimezone=UTC", "root", "")) {
					String q = "SELECT id FROM bills WHERE id LIKE ?";
					try (java.sql.PreparedStatement ps = _conn.prepareStatement(q)) {
						ps.setString(1, billIdSix + "_%");
						try (java.sql.ResultSet rs = ps.executeQuery()) {
							int max = 0;
							while (rs.next()) {
								String id = rs.getString("id");
								int idx = id.lastIndexOf('_');
								if (idx >= 0 && idx + 1 < id.length()) {
									try {
										int n = Integer.parseInt(id.substring(idx + 1));
										if (n > max) max = n;
									} catch (NumberFormatException ex) {
										// ignore malformed ids
									}
								}
							}
							seq = max + 1;
						}
					}
				}
			} catch (Exception ex) {
				// fallback to file count if DB check fails
				java.io.File[] matches = dir.listFiles((d, name) -> name.startsWith("bill_" + billIdSix + "_") && name.endsWith(".txt"));
				if (matches != null) seq = matches.length + 1;
			}
			String billIdFull = String.format("%s_%d", billIdSix, seq);
			java.io.File out = new java.io.File(dir, String.format("bill_%s_%d.txt", billIdSix, seq));

			// Persist bill and items to the database (create tables if needed)
			try {
				Class.forName("com.mysql.cj.jdbc.Driver");
				try (java.sql.Connection conn = java.sql.DriverManager.getConnection(
						"jdbc:mysql://localhost:3306/restaurant?serverTimezone=UTC", "root", "")) {
/* 
					String createBills = "CREATE TABLE IF NOT EXISTS bills (id VARCHAR(64) PRIMARY KEY, bill_date DATETIME, total DOUBLE)";
					String createItems = "CREATE TABLE IF NOT EXISTS bill_items (id INT AUTO_INCREMENT PRIMARY KEY, bill_id VARCHAR(64), food_id INT, qty INT, unit_price DOUBLE, total DOUBLE, FOREIGN KEY(food_id) REFERENCES food(id), INDEX(bill_id))";
					try (java.sql.Statement s = conn.createStatement()) {
						s.execute(createBills);
						s.execute(createItems);
					}
*/
					String insertBill = "INSERT INTO bills (id, bill_date, total) VALUES (?, NOW(), ?)";
					try (java.sql.PreparedStatement ps = conn.prepareStatement(insertBill)) {
						ps.setString(1, billIdFull);
						ps.setDouble(2, grandTotal.get());
						ps.executeUpdate();
					}

					String insertItem = "INSERT INTO bill_items (bill_id, food_id, qty, unit_price, total) VALUES (?, ?, ?, ?, ?)";
					try (java.sql.PreparedStatement psi = conn.prepareStatement(insertItem)) {
						for (CartItem ci : cartTable.getItems()) {
							psi.setString(1, billIdFull);
							psi.setInt(2, ci.getFoodId());
							psi.setInt(3, ci.getQty());
							psi.setDouble(4, ci.getUnitPrice());
							psi.setDouble(5, ci.getTotal());
							psi.addBatch();
						}
						psi.executeBatch();
					} 
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}

			try (java.io.PrintWriter pw = new java.io.PrintWriter(new java.io.OutputStreamWriter(new java.io.FileOutputStream(out), java.nio.charset.StandardCharsets.UTF_8))) {
				pw.println("BILL ID: " + billIdFull);
				pw.print(sb.toString());
				pw.flush();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			// Make the generated bill file read-only
			try {
				out.setWritable(false);
			} catch (Exception ex) {
				// ignore if unable to change permissions on some platforms
			}

			// Show bill in popup with Print button
			Stage billStage = new Stage();
			billStage.initOwner(stage);
			billStage.initModality(Modality.APPLICATION_MODAL);
			billStage.setTitle("E-Bill - " + billIdSix + "_" + seq);

			// show bill id at top of popup as well
			String popupText = "BILL ID: " + billIdSix + "_" + seq + "\n\n" + sb.toString();
			TextArea ta = new TextArea(popupText);
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
				// ignore css loading for popup
			}

			billStage.setScene(billScene);
			billStage.show();

			printBtn.setOnAction(ev -> {
				PrinterJob job = PrinterJob.createPrinterJob();
				if (job != null) {
					boolean proceed = job.showPrintDialog(stage);
					if (proceed) {
						boolean printed = job.printPage(ta);
						if (printed) job.endJob();
					}
				}
			});

			closeBtn.setOnAction(ev -> {
				billStage.close();
				// clear cart after closing popup
				cartTable.getItems().clear();
				grandTotal.set(0);
			});
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
