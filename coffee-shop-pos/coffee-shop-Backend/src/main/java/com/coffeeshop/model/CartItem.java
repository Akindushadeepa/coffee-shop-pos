package com.coffeeshop.model;

public class CartItem {
    private Long productId;
    private String productName;
    private Double price;
    private Integer quantity;
    private Double subtotal;

    // Constructors
    public CartItem() {
    }

    public CartItem(Long productId, String productName, Double price, Integer quantity, Double subtotal) {
        this.productId = productId;
        this.productName = productName;
        this.price = price;
        this.quantity = quantity;
        this.subtotal = subtotal;
    }

    // Getters
    public Long getProductId() {
        return productId;
    }

    public String getProductName() {
        return productName;
    }

    public Double getPrice() {
        return price;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public Double getSubtotal() {
        return subtotal;
    }

    // Setters
    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public void setSubtotal(Double subtotal) {
        this.subtotal = subtotal;
    }

    // Methods
    public void calculateSubtotal() {
        this.subtotal = this.price * this.quantity;
    }
}
