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
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextField;
import javafx.print.PrinterJob;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.DriverManager;
import javafx.stage.Modality;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.control.TableRow;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class CoffeeShopCashier {
	private String currentCashier = "CASHIER"; // default, set using setCurrentCashier
	public void setCurrentCashier(String username) { this.currentCashier = username; }

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
					// earlier pref width moved to rightBox declaration
		// Left: menu buttons (loaded from DB)
		VBox menuBox = new VBox(10);
		menuBox.setPadding(new Insets(10));
		menuBox.setPrefWidth(300);
		// Make menuBox stretch full height of the content area
		menuBox.setPrefHeight(Double.MAX_VALUE);
		menuBox.getStyleClass().add("sidebar");

		Label menuLabel = new Label("Menu Items");
		menuLabel.setStyle("-fx-font-weight: bold;");
		menuLabel.getStyleClass().addAll("card-title", "menu-header");
		// center the label (wrap in HBox for reliable centering)
		javafx.scene.layout.HBox menuHeaderBox = new javafx.scene.layout.HBox(menuLabel);
		menuHeaderBox.setAlignment(Pos.CENTER);
		menuHeaderBox.setMaxWidth(Double.MAX_VALUE);
		// Search box + scrollable flow of menu items
		TextField menuSearch = new TextField();
		menuSearch.setPromptText("Search menu...");
		menuSearch.getStyleClass().add("login-field");
		// Vertical stacked menu so it looks like a handwritten menu list
		javafx.scene.layout.VBox flow = new javafx.scene.layout.VBox(10);
		flow.setPadding(new Insets(8));
		javafx.scene.control.ScrollPane scrollMenu = new javafx.scene.control.ScrollPane(flow);
		scrollMenu.setFitToWidth(true);
		// Allow scrollMenu to expand and fill the left column vertically
		scrollMenu.setFitToWidth(true);
		scrollMenu.setFitToHeight(true);
		scrollMenu.setMaxHeight(Double.MAX_VALUE);
		VBox.setVgrow(scrollMenu, Priority.ALWAYS);
		java.util.List<Button> menuButtons = new java.util.ArrayList<>();
		menuBox.getChildren().addAll(menuHeaderBox, menuSearch, scrollMenu);

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
		unitCol.setPrefWidth(90);
		// align numbers to the right
		unitCol.setStyle("-fx-alignment: CENTER-RIGHT;");

		TableColumn<CartItem, Double> totalCol = new TableColumn<>("Total");
		totalCol.setCellValueFactory(new PropertyValueFactory<>("total"));
		totalCol.setPrefWidth(120);
		// align numbers to the right
		totalCol.setStyle("-fx-alignment: CENTER-RIGHT;");

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

		// Pending bills for this cashier
		Label pendingLabel = new Label("Pending Orders (Kitchen)");
		TableView<java.util.Map<String, Object>> pendingTable = new TableView<>();
		TableColumn<java.util.Map<String, Object>, String> pbIdCol = new TableColumn<>("Bill ID");
		pbIdCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(String.valueOf(c.getValue().get("id"))));
		TableColumn<java.util.Map<String, Object>, String> pbTableCol = new TableColumn<>("Table");
		pbTableCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(String.valueOf(c.getValue().get("table_number"))));
		TableColumn<java.util.Map<String, Object>, String> pbPending = new TableColumn<>("Pending Items");
		pbPending.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(String.valueOf(c.getValue().get("pending"))));
		TableColumn<java.util.Map<String, Object>, String> pbTotal = new TableColumn<>("Total");
		pbTotal.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(String.valueOf(c.getValue().get("total"))));
		pendingTable.getColumns().addAll(java.util.Arrays.asList(pbIdCol, pbTableCol, pbPending, pbTotal));
		pendingTable.setPrefHeight(140);
		Button refreshPendingBtn = new Button("Refresh");
		refreshPendingBtn.setOnAction(ev -> loadPendingBillsForCashier(pendingTable));
		// move pending table to the right panel instead of center
		// double click a pending bill to show its items
		pendingTable.setRowFactory(tv -> {
			TableRow<java.util.Map<String,Object>> row = new TableRow<>();
			row.setOnMouseClicked(ev -> {
				if (!row.isEmpty() && ev.getClickCount() == 2) {
					java.util.Map<String,Object> item = row.getItem();
					String billId = String.valueOf(item.get("id"));
					// query items for bill
					StringBuilder sb = new StringBuilder();
					String sql = "SELECT bi.qty, bi.unit_price, f.fName FROM bill_items bi JOIN food f ON bi.food_id = f.id WHERE bi.bill_id = ?";
					try (Connection c = DriverManager.getConnection("jdbc:mysql://localhost:3306/restaurant?serverTimezone=UTC", "root", "")) {
						try (PreparedStatement ps = c.prepareStatement(sql)) {
							ps.setString(1, billId);
							try (ResultSet rs = ps.executeQuery()) {
								while (rs.next()) {
									sb.append(rs.getString("fName")).append(" - qty: ").append(rs.getInt("qty")).append("\n");
								}
							}
						}
					} catch (Exception ex) { ex.printStackTrace(); sb.append("Could not load items: "+ex.getMessage()); }
					Alert a = new Alert(Alert.AlertType.INFORMATION, sb.toString(), ButtonType.OK);
					a.setTitle("Bill Items: " + billId);
					a.setHeaderText("Items for bill " + billId);
					a.showAndWait();
				}
			});
			return row;
		});
		// initial load
		loadPendingBillsForCashier(pendingTable);
		// auto refresh for pending bills
		Timeline pendRefresh = new Timeline(new KeyFrame(javafx.util.Duration.seconds(3), ev -> loadPendingBillsForCashier(pendingTable)));
		pendRefresh.setCycleCount(Timeline.INDEFINITE);
		pendRefresh.play();

		// Right: totals and actions
		VBox rightBox = new VBox(12);
		rightBox.setPadding(new Insets(10));
		rightBox.setPrefWidth(460);
		rightBox.getStyleClass().add("stat-card");

		Label totalLabel = new Label("Total: Rs0.00");
		// bind listener for grand total now that totalLabel exists
		totalLabel.getStyleClass().add("card-data");
		grandTotal.addListener((obs, oldV, newV) -> totalLabel.setText(String.format("Total: Rs%.2f", newV.doubleValue())));

		Button removeBtn = new Button("Remove Selected");
		// Use a specific dark button style for remove action
		removeBtn.getStyleClass().add("remove-button");
		Button checkoutBtn = new Button("💳 Checkout");
		checkoutBtn.getStyleClass().add("pay-button");

		// Add Table number input for this bill
		Label tableLabel = new Label("Table #");
		TextField tableField = new TextField();
		tableField.setPromptText("Table #");
		tableField.setPrefWidth(60);

		// align total label to the right inside the rightBox
		javafx.scene.layout.HBox totalBox = new javafx.scene.layout.HBox(totalLabel);
		totalBox.setAlignment(Pos.CENTER_RIGHT);
		totalBox.setMaxWidth(Double.MAX_VALUE);
		javafx.scene.layout.HBox.setHgrow(totalBox, Priority.ALWAYS);
		rightBox.getChildren().addAll(tableLabel, tableField, totalBox, removeBtn, checkoutBtn);
		// add pending orders table below the actions on the right column
		pendingLabel.getStyleClass().add("card-title");
		pendingLabel.setMinWidth(Region.USE_PREF_SIZE);
		pendingTable.setPrefHeight(160);
		pendingTable.setMaxHeight(220);
		javafx.scene.layout.VBox.setVgrow(pendingTable, Priority.ALWAYS);
		centerBox.getChildren().addAll(pendingLabel, pendingTable, refreshPendingBtn);

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
					// Ensure `bills` has the required columns (migration for older DBs)
					try {
						com.coffee_shop.model.DbMigration.ensureColumnExists(conn, "bills", "cashier", "VARCHAR(100) DEFAULT NULL");
						com.coffee_shop.model.DbMigration.ensureColumnExists(conn, "bills", "table_number", "VARCHAR(20) DEFAULT NULL");
						com.coffee_shop.model.DbMigration.ensureColumnExists(conn, "kitchen_orders", "table_number", "VARCHAR(20) DEFAULT NULL");
					} catch (Exception ex) {
						ex.printStackTrace();
					}
				String sql = "SELECT id, fName, fPrice FROM food";
				try (java.sql.PreparedStatement ps = conn.prepareStatement(sql);
					 java.sql.ResultSet rs = ps.executeQuery()) {
					while (rs.next()) {
						int foodId = rs.getInt("id");
						String name = rs.getString("fName");
						double price = rs.getDouble("fPrice");
						// Create a more polished tile with optional image, name and price
						// Create full-width tile button aligned horizontally
						Button itemBtn = new Button();
						itemBtn.setPrefHeight(64);
						itemBtn.setMaxWidth(Double.MAX_VALUE); // fill parent's width
						itemBtn.getStyleClass().add("item-card");
						// Tile layout: icon on the left, text name+price to the right
						javafx.scene.layout.HBox tileBox = new javafx.scene.layout.HBox(12);
						tileBox.setAlignment(Pos.CENTER_LEFT);
						tileBox.setPadding(new Insets(6, 12, 6, 12));
						// small coffee icon for aesthetic

						javafx.scene.control.Label iconLabel = new javafx.scene.control.Label("☕");
						iconLabel.getStyleClass().add("item-icon");

						// optionally load an image from item_images (name-based or id-based)
						javafx.scene.image.ImageView iv = null;
						try {
							java.io.File imgFile = new java.io.File("item_images" + java.io.File.separator + name.replaceAll("[^a-zA-Z0-9_\\- ]", "_") + ".jpg");
							if (!imgFile.exists()) imgFile = new java.io.File("item_images" + java.io.File.separator + name.replaceAll("[^a-zA-Z0-9_\\- ]", "_") + ".png");
								if (imgFile.exists()) {
									javafx.scene.image.Image img = new javafx.scene.image.Image(imgFile.toURI().toString(), 64, 40, true, true);
									iv = new javafx.scene.image.ImageView(img);
									iv.getStyleClass().add("item-image");
								}
						} catch (Exception ex) { /* ignore image load errors */ }
						javafx.scene.layout.VBox textBox = new javafx.scene.layout.VBox(2);
						textBox.setAlignment(Pos.CENTER_LEFT);
						javafx.scene.control.Label nameLbl = new javafx.scene.control.Label(name);
						nameLbl.getStyleClass().add("item-name");
						javafx.scene.control.Label priceLbl = new javafx.scene.control.Label(String.format("Rs %.2f", price));
						priceLbl.getStyleClass().add("item-price");
						textBox.getChildren().addAll(nameLbl, priceLbl);
						// Add image (if loaded) to the left of text, otherwise use icon
						if (iv != null) {
							tileBox.getChildren().addAll(iv, textBox);
						} else {
							tileBox.getChildren().addAll(iconLabel, textBox);
						}
						itemBtn.setGraphic(tileBox);
						// store the name for search filter (text nodes might not be picked up)
						itemBtn.setUserData(name.toLowerCase());
						final int fId = foodId;
						final String fName = name;
						final double fPrice = price;
						itemBtn.setOnAction(ev -> addItem.accept(
							new java.util.AbstractMap.SimpleEntry<>(fId, 
								new java.util.AbstractMap.SimpleEntry<>(fName, fPrice))
						));
						// ensure the button grows horizontally to fill the width of the ScrollPane's content
						HBox.setHgrow(itemBtn, Priority.ALWAYS);
						itemBtn.setMaxWidth(Double.MAX_VALUE);
						flow.getChildren().add(itemBtn);
						menuButtons.add(itemBtn);
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

		// filter menu items when typing in search box
		menuSearch.setOnKeyReleased(ev -> {
			String q = menuSearch.getText();
			if (q == null) q = "";
			q = q.trim().toLowerCase();
			for (Button b : menuButtons) {
				String txt = (b.getUserData() != null) ? b.getUserData().toString().toLowerCase() : b.getText().toLowerCase();
				boolean matches = txt.contains(q);
				b.setVisible(matches);
				b.setManaged(matches);
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
			String tno = tableField.getText().trim();
			if (tno != null && !tno.isEmpty()) sb.append("Table: ").append(tno).append("\n");
			sb.append("Cashier: ").append(this.currentCashier).append("\n");
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
					String createBills = "CREATE TABLE IF NOT EXISTS bills (id VARCHAR(64) PRIMARY KEY, bill_date DATETIME, total DOUBLE, cashier VARCHAR(100), table_number VARCHAR(20))";
					String createItems = "CREATE TABLE IF NOT EXISTS bill_items (id INT AUTO_INCREMENT PRIMARY KEY, bill_id VARCHAR(64), food_id INT, qty INT, unit_price DOUBLE, total DOUBLE, FOREIGN KEY(food_id) REFERENCES food(id), INDEX(bill_id))";
					try (java.sql.Statement s = conn.createStatement()) {
						s.execute(createBills);
						s.execute(createItems);
					}
					String insertBill = "INSERT INTO bills (id, bill_date, total, cashier, table_number) VALUES (?, NOW(), ?, ?, ?)";
					try (java.sql.PreparedStatement ps = conn.prepareStatement(insertBill)) {
						ps.setString(1, billIdFull);
						ps.setDouble(2, grandTotal.get());
						ps.setString(3, this.currentCashier);
						ps.setString(4, tableField.getText().trim());
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
						// --- START: send items to kitchen_orders ---
						try {
							com.coffee_shop.model.KitchenOrdersRepository kitchenRepo = new com.coffee_shop.model.KitchenOrdersRepository();
							java.util.List<com.coffee_shop.model.KitchenOrdersRepository.OrderItem> items = new java.util.ArrayList<>();
							for (CartItem ci : cartTable.getItems()) {
								items.add(new com.coffee_shop.model.KitchenOrdersRepository.OrderItem(ci.getFoodId(), ci.getName(), ci.getQty()));
							}
							try {
								kitchenRepo.insertKitchenOrders(billIdFull, items, tableField.getText().trim());
								System.out.println("Kitchen orders sent for bill: " + billIdFull);
							} catch (Exception bun) {
								// If the kitchen write fails, we log but do not stop the bill
								System.err.println("Could not send kitchen orders for bill " + billIdFull + ": " + bun.getMessage());
								bun.printStackTrace();
							}
						} catch (Exception kitchenEx) {
							kitchenEx.printStackTrace();
						}
// --- END: send items to kitchen_orders ---

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
				// clear cart after closing popup (we keep a pending list in the cashier dashboard)
				cartTable.getItems().clear();
				grandTotal.set(0);
			});
		});

		// Compose layout: use HBox as main content (menu | center | right) so rightBox sits flush to the window's right edge
		javafx.scene.layout.HBox content = new javafx.scene.layout.HBox();
		content.getChildren().addAll(menuBox, centerBox, rightBox);
		content.setSpacing(0);
		// Allow centerBox to expand and push the rightBox to the edge
		javafx.scene.layout.HBox.setHgrow(centerBox, javafx.scene.layout.Priority.ALWAYS);

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

	private void loadPendingBillsForCashier(javafx.scene.control.TableView<java.util.Map<String, Object>> pendingTable) {
		java.util.List<java.util.Map<String, Object>> rows = new java.util.ArrayList<>();
		String sql = "SELECT b.id, b.total, b.table_number, SUM(CASE WHEN ko.status = 'PENDING' THEN 1 ELSE 0 END) as pending FROM bills b JOIN kitchen_orders ko ON ko.bill_id=b.id WHERE b.cashier = ? GROUP BY b.id, b.total, b.table_number HAVING pending > 0";
		try (Connection c = DriverManager.getConnection("jdbc:mysql://localhost:3306/restaurant?serverTimezone=UTC", "root", "")) {
			try (PreparedStatement ps = c.prepareStatement(sql)) {
				ps.setString(1, this.currentCashier);
				try (ResultSet rs = ps.executeQuery()) {
					while (rs.next()) {
						java.util.Map<String, Object> m = new java.util.HashMap<>();
						m.put("id", rs.getString("id"));
						m.put("total", rs.getDouble("total"));
						m.put("table_number", rs.getString("table_number"));
						m.put("pending", rs.getInt("pending"));
						rows.add(m);
					}
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		pendingTable.getItems().setAll(rows);
	}
}
