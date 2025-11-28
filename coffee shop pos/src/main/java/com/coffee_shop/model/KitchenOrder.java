package com.coffee_shop.model;

import java.time.LocalDateTime;

public class KitchenOrder {
    private final int id;
    private final String billId;
    private final int foodId;
    private final String name;
    private final int qty;
    private final String tableNumber;
    private final String status;
    private final LocalDateTime createdAt;

    public KitchenOrder(int id, String billId, int foodId, String name, int qty, String tableNumber, String status, LocalDateTime createdAt) {
        this.id = id;
        this.billId = billId;
        this.foodId = foodId;
        this.name = name;
        this.qty = qty;
        this.tableNumber = tableNumber;
        this.status = status;
        this.createdAt = createdAt;
    }

    public int getId() { return id; }
    public String getBillId() { return billId; }
    public int getFoodId() { return foodId; }
    public String getName() { return name; }
    public int getQty() { return qty; }
    public String getTableNumber() { return tableNumber; }
    public String getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
