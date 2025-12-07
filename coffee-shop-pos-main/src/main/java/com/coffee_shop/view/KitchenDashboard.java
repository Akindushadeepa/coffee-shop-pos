package com.coffee_shop.view;

import com.coffee_shop.model.KitchenOrder;
import com.coffee_shop.model.KitchenOrdersRepository;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.concurrent.Task;
import javafx.scene.control.TreeTableView;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeItem;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Priority;
import javafx.scene.control.TreeTableRow;
import java.util.Optional;
import javafx.stage.Stage;

import java.util.List;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class KitchenDashboard extends Application {

    private final KitchenOrdersRepository repo = new KitchenOrdersRepository();
    private String currentStatusFilter = "PENDING"; // ALL, PENDING, COMPLETED
    private final TreeTableView<KitchenOrderRow> treeTable = new TreeTableView<>();
    // remember seen order ids so we can detect new orders
    private final Set<Integer> lastSeenOrderIds = new HashSet<>();
    private final Set<Integer> newlyArrivedOrderIds = new HashSet<>();
    private final Set<Integer> pickedOrderIds = new HashSet<>();

    // instance controls we need to access from other methods
    private TextField searchField;
    // legacy simple label removed; using structured detail pane instead
    private Label detailBillLabel;
    private Label detailsMetaLabel;
    private ListView<String> detailsList;
    private Button btnMarkItemCompleted;
    private Button btnMarkBillCompleted;
    private Button refreshBtn;
    private ToggleButton allBtn;
    private ToggleButton pendingBtn;
    private ToggleButton completedBtn;

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Kitchen Dashboard");

        TreeTableColumn<KitchenOrderRow, Integer> idCol = new TreeTableColumn<>("ID");
        idCol.setCellValueFactory((TreeTableColumn.CellDataFeatures<KitchenOrderRow, Integer> param) -> new javafx.beans.property.ReadOnlyObjectWrapper<>(param.getValue().getValue().getId()));
        idCol.setPrefWidth(60);

        TreeTableColumn<KitchenOrderRow, String> billCol = new TreeTableColumn<>("Bill ID");
        billCol.setCellValueFactory((TreeTableColumn.CellDataFeatures<KitchenOrderRow, String> param) -> new javafx.beans.property.ReadOnlyStringWrapper(param.getValue().getValue().getBillId()));
        billCol.setPrefWidth(140);

        TreeTableColumn<KitchenOrderRow, String> nameCol = new TreeTableColumn<>("Item");
        nameCol.setCellValueFactory((TreeTableColumn.CellDataFeatures<KitchenOrderRow, String> param) -> new javafx.beans.property.ReadOnlyStringWrapper(param.getValue().getValue().getName()));
        nameCol.setPrefWidth(240);

        TreeTableColumn<KitchenOrderRow, Integer> qtyCol = new TreeTableColumn<>("Qty");
        qtyCol.setCellValueFactory((TreeTableColumn.CellDataFeatures<KitchenOrderRow, Integer> param) -> new javafx.beans.property.ReadOnlyObjectWrapper<>(param.getValue().getValue().getQty()));
        qtyCol.setPrefWidth(80);

        TreeTableColumn<KitchenOrderRow, String> timeCol = new TreeTableColumn<>("Time");
        timeCol.setCellValueFactory((TreeTableColumn.CellDataFeatures<KitchenOrderRow, String> param) -> new javafx.beans.property.ReadOnlyStringWrapper(param.getValue().getValue().getCreatedAt()));
        timeCol.setPrefWidth(160);

        // Status column to show PENDING/COMPLETED
        TreeTableColumn<KitchenOrderRow, String> statusCol = new TreeTableColumn<>("Status");
        statusCol.setCellValueFactory((TreeTableColumn.CellDataFeatures<KitchenOrderRow, String> param) -> new javafx.beans.property.ReadOnlyStringWrapper(param.getValue().getValue().getStatus()));
        statusCol.setPrefWidth(120);
        statusCol.setCellFactory(col -> new javafx.scene.control.TreeTableCell<KitchenOrderRow, String>() {
            private final Label badge = new Label();
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    badge.setText(status);
                    badge.getStyleClass().clear();
                    if (status.equalsIgnoreCase("PENDING")) {
                        badge.getStyleClass().add("status-pending");
                    } else if (status.equalsIgnoreCase("COMPLETED")) {
                        badge.getStyleClass().add("status-completed");
                    } else {
                        badge.getStyleClass().add("status-mixed");
                    }
                    setGraphic(badge);
                    setText(null);
                }
            }
        });

        TreeTableColumn<KitchenOrderRow, String> tableCol = new TreeTableColumn<>("Table");
        tableCol.setCellValueFactory((TreeTableColumn.CellDataFeatures<KitchenOrderRow, String> param) -> new javafx.beans.property.ReadOnlyStringWrapper(param.getValue().getValue().getTableNumber()));
        tableCol.setPrefWidth(80);

        treeTable.getColumns().addAll(idCol, billCol, tableCol, nameCol, qtyCol, timeCol, statusCol);
        treeTable.setColumnResizePolicy(TreeTableView.CONSTRAINED_RESIZE_POLICY);

        Button refreshBtn = new Button("🔄 Refresh");
        Button completeBtn = new Button("✅ Mark Completed");
        refreshBtn.getStyleClass().add("filter-button");
        completeBtn.getStyleClass().add("filter-button");
        Button clearFilterBtn = new Button("✖ Clear");
        ToggleGroup filterGroup = new ToggleGroup();
        // convert to instance fields
        allBtn = new ToggleButton("All (0)");
        pendingBtn = new ToggleButton("Pending (0)");
        completedBtn = new ToggleButton("Completed (0)");
        allBtn.setToggleGroup(filterGroup);
        pendingBtn.setToggleGroup(filterGroup);
        completedBtn.setToggleGroup(filterGroup);
        allBtn.getStyleClass().add("filter-button");
        pendingBtn.getStyleClass().add("filter-button");
        completedBtn.getStyleClass().add("filter-button");
        // initial counts
        updateFilterCountsAsync();
        allBtn.setSelected(true);
        searchField = new TextField();
        searchField.setPromptText("Search Bill ID or Item Name...");
        // use async reload so UI is not blocked by DB IO
        refreshBtn.setOnAction(e -> reloadOrdersAsync(currentStatusFilter, searchField.getText()));
        this.refreshBtn = refreshBtn;
        completeBtn.setOnAction(e -> {
            TreeItem<KitchenOrderRow> selItem = treeTable.getSelectionModel().getSelectedItem();
                if (selItem == null) {
                Alert a = new Alert(Alert.AlertType.INFORMATION, "Select an order to mark completed.", ButtonType.OK);
                a.showAndWait();
                return;
            }
                try {
                if (selItem != null && !selItem.isLeaf()) {
                    // parent selected: mark all children
                    for (TreeItem<KitchenOrderRow> child : selItem.getChildren()) {
                        repo.markOrderCompleted(child.getValue().getId());
                    }
                } else if (selItem != null) {
                    repo.markOrderCompleted(selItem.getValue().getId());
                }
                reloadOrdersAsync(currentStatusFilter, searchField.getText());
            } catch (Exception ex) {
                ex.printStackTrace();
                Alert a = new Alert(Alert.AlertType.ERROR, "Could not update order: " + ex.getMessage(), ButtonType.OK);
                a.showAndWait();
            }
        });

        // Double click a row to mark completed (quick action)
        treeTable.setRowFactory(tv -> {
            TreeTableRow<KitchenOrderRow> row = new TreeTableRow<>();
            // style based on status
            row.itemProperty().addListener((obs, oldItem, newItem) -> {
                row.getStyleClass().removeAll("row-pending", "row-completed", "new-order", "row-picked", "row-mixed");
                if (newItem != null) {
                    if ("PENDING".equalsIgnoreCase(newItem.getStatus())) {
                        row.getStyleClass().add("row-pending");
                    } else if ("COMPLETED".equalsIgnoreCase(newItem.getStatus())) {
                        row.getStyleClass().add("row-completed");
                    } else {
                        row.getStyleClass().add("row-mixed");
                    }
                    try {
                        if (newlyArrivedOrderIds.contains(newItem.getId())) {
                            row.getStyleClass().add("new-order");
                        }
                    // apply picked style
                    if (pickedOrderIds.contains(newItem.getId())) {
                        row.getStyleClass().add("row-picked");
                    }
                    } catch (Exception ignored) {}
                }
            });
            row.setOnMouseClicked(event -> {
                // ensure single-click selects and shows details *and* toggles picked state
                if (!row.isEmpty() && event.getClickCount() == 1) {
                    TreeItem<KitchenOrderRow> sel = row.getTreeItem();
                    treeTable.getSelectionModel().select(sel);
                    showDetailsForItem(sel);
                    pickSingle(sel);
                }
                if (!row.isEmpty() && event.getClickCount() == 2) {
                    TreeItem<KitchenOrderRow> clickedItem = treeTable.getTreeItem(row.getIndex());
                    KitchenOrderRow clicked = clickedItem.getValue();
                    Alert a = new Alert(Alert.AlertType.CONFIRMATION, "Mark order " + clicked.getId() + " as completed?", ButtonType.YES, ButtonType.NO);
                    Optional<ButtonType> res = a.showAndWait();
                    if (res.isPresent() && res.get() == ButtonType.YES) {
                        try {
                            if (!clickedItem.isLeaf()) {
                                // parent: mark all children
                                for (TreeItem<KitchenOrderRow> ch : clickedItem.getChildren()) repo.markOrderCompleted(ch.getValue().getId());
                            } else {
                                repo.markOrderCompleted(clicked.getId());
                            }
                            reloadOrdersAsync(currentStatusFilter, searchField.getText());
                        } catch (Exception ex) {
                            ex.printStackTrace();
                            Alert err = new Alert(Alert.AlertType.ERROR, "Could not mark completed: " + ex.getMessage(), ButtonType.OK);
                            err.showAndWait();
                        }
                    }
                }
            });
            // Add a context menu for quick actions
            ContextMenu cm = new ContextMenu();
            MenuItem mark = new MenuItem("Mark Completed");
            mark.setOnAction(e -> {
                TreeItem<KitchenOrderRow> sel = row.getTreeItem();
                if (sel != null) {
                    try {
                        if (!sel.isLeaf()) {
                            for (TreeItem<KitchenOrderRow> child : sel.getChildren()) repo.markOrderCompleted(child.getValue().getId());
                        } else {
                            repo.markOrderCompleted(sel.getValue().getId());
                        }
                        reloadOrdersAsync(currentStatusFilter, searchField.getText());
                    } catch (Exception ex) { ex.printStackTrace(); }
                }
            });
                MenuItem view = new MenuItem("View Details");
            view.setOnAction(e -> {
                TreeItem<KitchenOrderRow> sel = row.getTreeItem();
                if (sel != null) {
                        // focus selection and show full details on the right
                        treeTable.getSelectionModel().select(sel);
                        showDetailsForItem(sel);
                        pickSingle(sel);
                        // no modal popup; right-side panel is the main details view
                }
            });
            cm.getItems().addAll(view, mark);
            row.setContextMenu(cm);
            return row;
        });

        // Filter and search handling
        clearFilterBtn.setOnAction(ev -> { allBtn.setSelected(true); currentStatusFilter = "ALL"; searchField.clear(); reloadOrdersAsync(currentStatusFilter, null); });
        pendingBtn.setOnAction(ev -> { currentStatusFilter = "PENDING"; reloadOrdersAsync("PENDING", searchField.getText()); });
        completedBtn.setOnAction(ev -> { currentStatusFilter = "COMPLETED"; reloadOrdersAsync("COMPLETED", searchField.getText()); });
        allBtn.setOnAction(ev -> { currentStatusFilter = "ALL"; reloadOrdersAsync(currentStatusFilter, searchField.getText()); });
        searchField.setOnAction(ev -> {
            String status = allBtn.isSelected() ? "ALL" : (pendingBtn.isSelected() ? "PENDING" : (completedBtn.isSelected() ? "COMPLETED" : "ALL"));
            reloadOrdersAsync(status, searchField.getText());
        });

        

        HBox controls = new HBox(10, refreshBtn, completeBtn, allBtn, pendingBtn, completedBtn, clearFilterBtn, searchField);
        controls.setAlignment(Pos.CENTER_LEFT);
        controls.setPadding(new Insets(10));

        BorderPane root = new BorderPane();
        root.getStyleClass().add("main-content-area");

        HBox topBar = new HBox(10);
        topBar.getStyleClass().add("top-bar");
        Label title = new Label("🍳 Kitchen Dashboard");
        title.getStyleClass().add("page-title");
        Label pendingCounter = new Label("Pending: 0");
        pendingCounter.setId("pendingCount");
        HBox spacer = new HBox();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        topBar.setPadding(new Insets(10));
        topBar.getChildren().addAll(title, spacer, pendingCounter);

        VBox leftPane = new VBox(10);
        leftPane.setPadding(new Insets(10));
        leftPane.getChildren().addAll(controls, treeTable);
        VBox.setVgrow(treeTable, Priority.ALWAYS);

        VBox rightPane = new VBox(10);
        rightPane.setPadding(new Insets(10));
        rightPane.getStyleClass().add("stat-card");
        Label detailsTitle = new Label("Order Details");
        detailsTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        detailBillLabel = new Label("Select an order to see details");
        detailBillLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
        detailsMetaLabel = new Label("");
        detailsMetaLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #555;");
        detailsList = new ListView<>();
        detailsList.setPrefHeight(200);
        btnMarkItemCompleted = new Button("Mark Item Completed");
        btnMarkBillCompleted = new Button("Mark Bill Completed");
        btnMarkItemCompleted.setOnAction(e -> {
            TreeItem<KitchenOrderRow> sel = treeTable.getSelectionModel().getSelectedItem();
            if (sel != null && sel.isLeaf()) {
                try {
                    repo.markOrderCompleted(sel.getValue().getId());
                    reloadOrdersAsync(currentStatusFilter, searchField.getText());
                    updateFilterCountsAsync();
                } catch (Exception ex) {
                    ex.printStackTrace();
                    Alert err = new Alert(Alert.AlertType.ERROR, "Could not mark completed: " + ex.getMessage(), ButtonType.OK);
                    err.showAndWait();
                }
            }
        });
        btnMarkBillCompleted.setOnAction(e -> {
            TreeItem<KitchenOrderRow> sel = treeTable.getSelectionModel().getSelectedItem();
            if (sel != null && !sel.isLeaf()) {
                try {
                    for (TreeItem<KitchenOrderRow> ch : sel.getChildren()) repo.markOrderCompleted(ch.getValue().getId());
                    reloadOrdersAsync(currentStatusFilter, searchField.getText());
                    updateFilterCountsAsync();
                } catch (Exception ex) {
                    ex.printStackTrace();
                    Alert err = new Alert(Alert.AlertType.ERROR, "Could not mark completed: " + ex.getMessage(), ButtonType.OK);
                    err.showAndWait();
                }
            }
        });
        HBox detailsButtons = new HBox(10, btnMarkItemCompleted, btnMarkBillCompleted);
        detailsButtons.setAlignment(Pos.CENTER_RIGHT);
        btnMarkItemCompleted.setDisable(true);
        btnMarkBillCompleted.setDisable(true);
        rightPane.getChildren().addAll(detailsTitle, detailBillLabel, detailsMetaLabel, detailsList, detailsButtons);

        // update structured details on selection changes
        treeTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            showDetailsForItem(newSel);
        });

        // make the right pane a fixed width and left pane take remaining
        rightPane.setMinWidth(360);
        rightPane.setMaxWidth(420);
        HBox.setHgrow(leftPane, Priority.ALWAYS);
        HBox content = new HBox(12, leftPane, rightPane);
        content.setPadding(new Insets(10));

        root.setTop(topBar);
        root.setCenter(content);

        Scene scene = new Scene(root, 1000, 600);
        try { scene.getStylesheets().add(getClass().getResource("/dashboard.css").toExternalForm()); } catch (Exception ex) { System.err.println("CSS not found"); }
        primaryStage.setScene(scene);
        primaryStage.show();
        // Set up auto-refresh: reload current view orders every 3 seconds (async)
        Timeline refreshTimeline = new Timeline(new KeyFrame(Duration.seconds(3), ev -> reloadOrdersAsync(currentStatusFilter, searchField.getText())));
        refreshTimeline.setCycleCount(Timeline.INDEFINITE);
        refreshTimeline.play();

        // Stop the timeline when the stage is closed
        primaryStage.setOnCloseRequest(ev -> refreshTimeline.stop());
        reloadOrdersAsync(currentStatusFilter);
    }

    

    private void loadOrders(String status) {
        try {
            List<KitchenOrder> pending;
            if ("ALL".equalsIgnoreCase(status)) pending = repo.getAllOrders();
            else pending = repo.getOrdersByStatus(status);
            updateTreeFromOrders(pending, status);
        } catch (Exception ex) {
            ex.printStackTrace();
            Alert a = new Alert(Alert.AlertType.ERROR, "Could not load orders: " + ex.getMessage(), ButtonType.OK);
            a.showAndWait();
        }
    }

    private void updateTreeFromOrders(List<KitchenOrder> orders, String status) {
        try {
            // Build grouped tree by bill id
            TreeItem<KitchenOrderRow> rootItem = new TreeItem<>(new KitchenOrderRow(0, "ROOT", "", 0, "", "", ""));
            rootItem.setExpanded(true);
            Set<Integer> currentIds = new HashSet<>();
            java.util.Map<String, java.util.List<KitchenOrder>> grouped = orders.stream().collect(Collectors.groupingBy(KitchenOrder::getBillId, java.util.LinkedHashMap::new, Collectors.toList()));
            for (java.util.Map.Entry<String, java.util.List<KitchenOrder>> e : grouped.entrySet()) {
                String billId = e.getKey();
                java.util.List<KitchenOrder> items = e.getValue();
                int totalQty = items.stream().mapToInt(KitchenOrder::getQty).sum();
                String tableNum = items.get(0).getTableNumber();
                String billLabel = "Bill: " + billId + (tableNum == null || tableNum.isEmpty() ? "" : " (Table: " + tableNum + ")");
                String groupStatus = items.stream().map(KitchenOrder::getStatus).distinct().count() == 1 ? items.get(0).getStatus() : "MIXED";
                TreeItem<KitchenOrderRow> billNode = new TreeItem<>(new KitchenOrderRow(0, billId, billLabel, totalQty, tableNum, groupStatus, items.get(0).getCreatedAt().toString()));
                billNode.setExpanded(true);
                for (KitchenOrder ko : items) {
                    TreeItem<KitchenOrderRow> leaf = new TreeItem<>(new KitchenOrderRow(ko.getId(), ko.getBillId(), ko.getName(), ko.getQty(), ko.getTableNumber(), ko.getStatus(), ko.getCreatedAt().toString()));
                    billNode.getChildren().add(leaf);
                    currentIds.add(ko.getId());
                }
                rootItem.getChildren().add(billNode);
            }
            treeTable.setRoot(rootItem);
            treeTable.setShowRoot(false);
            Set<Integer> newIds = new HashSet<>(currentIds);
            newIds.removeAll(lastSeenOrderIds);
            if (!lastSeenOrderIds.isEmpty() && !newIds.isEmpty()) {
                List<KitchenOrder> newOrders = orders.stream().filter(k -> newIds.contains(k.getId())).collect(Collectors.toList());
                showNewOrdersNotification(newOrders);
            }
            lastSeenOrderIds.clear();
            lastSeenOrderIds.addAll(currentIds);
            // remove picked ids that are no longer in the current view (e.g., completed)
            pickedOrderIds.retainAll(currentIds);
            showCount(status, currentIds.size());
            newlyArrivedOrderIds.clear();
            newlyArrivedOrderIds.addAll(newIds);
            if (!newlyArrivedOrderIds.isEmpty()) {
                Timeline t = new Timeline(new KeyFrame(Duration.seconds(4), e -> { newlyArrivedOrderIds.clear(); treeTable.refresh(); }));
                t.setCycleCount(1);
                t.play();
            }
            // update the filter button counts
            updateFilterCountsAsync();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void reloadOrdersAsync(String status) {
        reloadOrdersAsync(status, null);
    }

    private void reloadOrdersAsync(String status, String searchTerm) {
        // disable refresh while working
        if (refreshBtn != null) {
            refreshBtn.setDisable(true);
            refreshBtn.setText("Refreshing...");
        }
        Task<java.util.List<KitchenOrder>> task = new Task<java.util.List<KitchenOrder>>() {
            @Override
            protected java.util.List<KitchenOrder> call() throws Exception {
                if ("ALL".equalsIgnoreCase(status)) return repo.getAllOrders();
                return repo.getOrdersByStatus(status);
            }
        };
        task.setOnSucceeded(e -> {
            java.util.List<KitchenOrder> orders = task.getValue();
            updateTreeFromOrders(orders, status);
            if (searchTerm != null && !searchTerm.trim().isEmpty()) applyLocalSearch(searchTerm);
            if (refreshBtn != null) {
                refreshBtn.setDisable(false);
                refreshBtn.setText("🔄 Refresh");
            }
        });
        task.setOnFailed(e -> {
            if (refreshBtn != null) {
                refreshBtn.setDisable(false);
                refreshBtn.setText("🔄 Refresh");
            }
            task.getException().printStackTrace();
            javafx.application.Platform.runLater(() -> {
                Alert a = new Alert(Alert.AlertType.ERROR, "Could not refresh orders: " + task.getException().getMessage(), ButtonType.OK);
                a.showAndWait();
            });
        });
        new Thread(task).start();
    }

    private void applyLocalSearch(String term) {
        String t = term.trim().toLowerCase();
        TreeItem<KitchenOrderRow> root = treeTable.getRoot();
        if (root == null) return;
        TreeItem<KitchenOrderRow> newRoot = new TreeItem<>(root.getValue());
        for (TreeItem<KitchenOrderRow> bill : root.getChildren()) {
            TreeItem<KitchenOrderRow> copyBill = new TreeItem<>(bill.getValue());
            for (TreeItem<KitchenOrderRow> child : bill.getChildren()) {
                boolean matchesTerm = child.getValue().getName().toLowerCase().contains(t) || child.getValue().getBillId().toLowerCase().contains(t);
                if (matchesTerm) copyBill.getChildren().add(new TreeItem<>(child.getValue()));
            }
            if (!copyBill.getChildren().isEmpty()) newRoot.getChildren().add(copyBill);
        }
        treeTable.setRoot(newRoot);
        treeTable.setShowRoot(false);
    }

    // Deprecated: use pickSingle to pick a single item or group

    private void pickSingle(TreeItem<KitchenOrderRow> sel) {
        if (sel == null) return;
        // clear any previous picks then pick only this selection (leaf or group)
        pickedOrderIds.clear();
        if (sel.isLeaf()) {
            pickedOrderIds.add(sel.getValue().getId());
        } else {
            for (TreeItem<KitchenOrderRow> ch : sel.getChildren()) {
                pickedOrderIds.add(ch.getValue().getId());
            }
        }
        treeTable.refresh();
    }

    private void showDetailsForItem(TreeItem<KitchenOrderRow> sel) {
        if (sel == null) return;
        detailsList.getItems().clear();
        btnMarkItemCompleted.setDisable(true);
        btnMarkBillCompleted.setDisable(true);
        KitchenOrderRow r = sel.getValue();
        if (r == null) return;
            if (!sel.isLeaf()) {
            detailBillLabel.setText("Bill: " + r.getBillId());
            int total = sel.getChildren().size();
            int pickedCnt = 0;
            for (TreeItem<KitchenOrderRow> ch : sel.getChildren()) if (pickedOrderIds.contains(ch.getValue().getId())) pickedCnt++;
            String pickedText = pickedCnt == 0 ? "" : ("  •  Picked: " + pickedCnt + "/" + total);
            detailsMetaLabel.setText("Table: " + (r.getTableNumber() == null ? "-" : r.getTableNumber()) + "  •  Total Qty: " + r.getQty() + pickedText);
            for (TreeItem<KitchenOrderRow> ch : sel.getChildren()) {
                KitchenOrderRow it = ch.getValue();
                detailsList.getItems().add(String.format("%s  x%d  [%s]", it.getName(), it.getQty(), it.getStatus()));
            }
            btnMarkBillCompleted.setDisable(false);
        } else {
            detailBillLabel.setText("Bill: " + r.getBillId() + " — Item: " + r.getName());
            detailsMetaLabel.setText("Table: " + (r.getTableNumber() == null ? "-" : r.getTableNumber()) + "  •  Qty: " + r.getQty() + "  •  Status: " + r.getStatus() + (pickedOrderIds.contains(r.getId()) ? "  •  Picked" : ""));
            detailsList.getItems().add(String.format("%s  x%d  [%s]", r.getName(), r.getQty(), r.getStatus()));
            btnMarkItemCompleted.setDisable(false);
        }
    }

    private void updateFilterCountsAsync() {
        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                // expensive DB work on background
                int pendingCount = repo.getOrdersByStatus("PENDING").size();
                int completedCount = repo.getOrdersByStatus("COMPLETED").size();
                int totalCount = repo.getAllOrders().size();
                javafx.application.Platform.runLater(() -> {
                    try {
                        if (pendingBtn != null) pendingBtn.setText("Pending (" + pendingCount + ")");
                        if (completedBtn != null) completedBtn.setText("Completed (" + completedCount + ")");
                        if (allBtn != null) allBtn.setText("All (" + totalCount + ")");
                    } catch (Exception ex) { /* ignore */ }
                });
                return null;
            }
        };
        new Thread(task).start();
    }

    

    private void showCount(String status, int count) {
        try {
            Scene s = treeTable.getScene();
            if (s == null) return;
            Label label = (Label) s.lookup("#pendingCount");
            if (label == null) return;
            if ("PENDING".equalsIgnoreCase(status)) {
                label.setText("Pending: " + count);
            } else if ("COMPLETED".equalsIgnoreCase(status)) {
                label.setText("Completed: " + count);
            } else {
                label.setText("Total: " + count);
            }
        } catch (Exception ex) { /* ignore */ }
    }

    

    private void showNewOrdersNotification(List<KitchenOrder> newOrders) {
        try {
            // Build a short message with bill IDs and first line per order
                String summary = newOrders.stream()
                    .map(o -> String.format("%s t%s (x%d)", o.getBillId(), o.getTableNumber(), o.getQty()))
                    .collect(Collectors.joining(", "));

            javafx.stage.Stage popup = new javafx.stage.Stage();
            popup.setAlwaysOnTop(true);
            popup.initOwner(null);
            popup.initStyle(javafx.stage.StageStyle.UTILITY);
            VBox box = new VBox(8);
            box.setPadding(new javafx.geometry.Insets(10));
            Label header = new Label("New kitchen orders") ;
            header.setStyle("-fx-font-weight:bold; -fx-font-size:14px;");
            Label msg = new Label(summary);
            Button close = new Button("Close");
            close.setOnAction(ev -> popup.close());
            box.getChildren().addAll(header, msg, close);
            Scene s = new Scene(box);
            popup.setScene(s);
            // show non-blocking, auto close after 3 seconds
            popup.show();
            javafx.animation.Timeline t = new javafx.animation.Timeline(new javafx.animation.KeyFrame(javafx.util.Duration.seconds(3), e -> popup.close()));
            t.setCycleCount(1);
            t.play();
            // beep to get attention (optional)
            try { java.awt.Toolkit.getDefaultToolkit().beep(); } catch (Exception ex) {}
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // small inner DTO for table
    public static class KitchenOrderRow {
        private final int id;
        private final String billId;
        private final String name;
        private final int qty;
        private final String tableNumber;
        private final String status;
        private final String createdAt;

        public KitchenOrderRow(int id, String billId, String name, int qty, String tableNumber, String status, String createdAt) {
            this.id = id;
            this.billId = billId;
            this.name = name;
            this.qty = qty;
            this.tableNumber = tableNumber;
            this.status = status;
            this.createdAt = createdAt;
        }
        public int getId() { return id; }
        public String getBillId() { return billId; }
        public String getName() { return name; }
        public int getQty() { return qty; }
        public String getTableNumber() { return tableNumber; }
        public String getStatus() { return status; }
        public String getCreatedAt() { return createdAt; }
    }
}
