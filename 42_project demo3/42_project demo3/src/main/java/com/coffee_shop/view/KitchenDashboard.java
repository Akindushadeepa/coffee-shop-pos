package com.coffee_shop.view;

import com.coffee_shop.model.KitchenOrder;
import com.coffee_shop.model.KitchenOrdersRepository;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.TreeTableView;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeItem;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.control.TreeTableRow;
import java.util.Optional;
import javafx.stage.Stage;

import java.util.List;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class KitchenDashboard extends Application {

    private final KitchenOrdersRepository repo = new KitchenOrdersRepository();
    private final TreeTableView<KitchenOrderRow> treeTable = new TreeTableView<>();
    // remember seen order ids so we can detect new orders
    private final Set<Integer> lastSeenOrderIds = new HashSet<>();

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

        TreeTableColumn<KitchenOrderRow, String> tableCol = new TreeTableColumn<>("Table");
        tableCol.setCellValueFactory((TreeTableColumn.CellDataFeatures<KitchenOrderRow, String> param) -> new javafx.beans.property.ReadOnlyStringWrapper(param.getValue().getValue().getTableNumber()));
        tableCol.setPrefWidth(80);

        treeTable.getColumns().addAll(idCol, billCol, tableCol, nameCol, qtyCol, timeCol, statusCol);
        treeTable.setColumnResizePolicy(TreeTableView.CONSTRAINED_RESIZE_POLICY);

        Button refreshBtn = new Button("Refresh");
        Button completeBtn = new Button("Mark Completed");

        refreshBtn.setOnAction(e -> loadPending());
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
                loadPending();
            } catch (Exception ex) {
                ex.printStackTrace();
                Alert a = new Alert(Alert.AlertType.ERROR, "Could not update order: " + ex.getMessage(), ButtonType.OK);
                a.showAndWait();
            }
        });

        // Double click a row to mark completed (quick action)
        treeTable.setRowFactory(tv -> {
            TreeTableRow<KitchenOrderRow> row = new TreeTableRow<>();
            row.setOnMouseClicked(event -> {
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
                            loadPending();
                        } catch (Exception ex) {
                            ex.printStackTrace();
                            Alert err = new Alert(Alert.AlertType.ERROR, "Could not mark completed: " + ex.getMessage(), ButtonType.OK);
                            err.showAndWait();
                        }
                    }
                }
            });
            return row;
        });

        HBox controls = new HBox(10, refreshBtn, completeBtn);
        controls.setAlignment(Pos.CENTER);
        controls.setPadding(new Insets(10));

        BorderPane root = new BorderPane();
        root.setTop(new Label("Pending Kitchen Orders"));
        root.setCenter(treeTable);
        root.setBottom(controls);
        BorderPane.setMargin(treeTable, new Insets(10));

        Scene scene = new Scene(root, 800, 480);
        primaryStage.setScene(scene);
        primaryStage.show();
        // Set up auto-refresh: reload pending orders every 3 seconds
        Timeline refreshTimeline = new Timeline(new KeyFrame(Duration.seconds(3), ev -> loadPending()));
        refreshTimeline.setCycleCount(Timeline.INDEFINITE);
        refreshTimeline.play();

        // Stop the timeline when the stage is closed
        primaryStage.setOnCloseRequest(ev -> refreshTimeline.stop());
        loadPending();
    }

    private void loadPending() {
        try {
            List<KitchenOrder> pending = repo.getPendingOrders();
            // Build grouped tree by bill id
            TreeItem<KitchenOrderRow> rootItem = new TreeItem<>(new KitchenOrderRow(0, "ROOT", "", 0, "", "", ""));
            rootItem.setExpanded(true);
            // collect ids and detect new ones
            Set<Integer> currentIds = new HashSet<>();
            // group by bill
            java.util.Map<String, java.util.List<KitchenOrder>> grouped = pending.stream().collect(Collectors.groupingBy(KitchenOrder::getBillId, java.util.LinkedHashMap::new, Collectors.toList()));
            for (java.util.Map.Entry<String, java.util.List<KitchenOrder>> e : grouped.entrySet()) {
                String billId = e.getKey();
                java.util.List<KitchenOrder> items = e.getValue();
                int totalQty = items.stream().mapToInt(KitchenOrder::getQty).sum();
                String tableNum = items.get(0).getTableNumber();
                String billLabel = "Bill: " + billId + (tableNum == null || tableNum.isEmpty() ? "" : " (Table: " + tableNum + ")");
                TreeItem<KitchenOrderRow> billNode = new TreeItem<>(new KitchenOrderRow(0, billId, billLabel, totalQty, tableNum, "PENDING", items.get(0).getCreatedAt().toString()));
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
            // if we had previous ids and new ones appear, show a brief non-blocking popup
            Set<Integer> newIds = new HashSet<>(currentIds);
            newIds.removeAll(lastSeenOrderIds);
            if (!lastSeenOrderIds.isEmpty() && !newIds.isEmpty()) {
                // build a summary text for new orders
                List<KitchenOrder> newOrders = pending.stream().filter(k -> newIds.contains(k.getId())).collect(Collectors.toList());
                showNewOrdersNotification(newOrders);
            }
            lastSeenOrderIds.clear();
            lastSeenOrderIds.addAll(currentIds);
        } catch (Exception ex) {
            ex.printStackTrace();
            Alert a = new Alert(Alert.AlertType.ERROR, "Could not load orders: " + ex.getMessage(), ButtonType.OK);
            a.showAndWait();
        }
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
