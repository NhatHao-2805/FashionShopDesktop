package com.fashionshop.server.model;

import java.math.BigDecimal;

public class OrderItem {


    private String selectedSize;
    private String selectedColor;
    public String getSelectedSize() { return selectedSize; }
    public String getSelectedColor() { return selectedColor; }
    public void setSelectedSize(String value) { selectedSize = value; }
    public void setSelectedColor(String value) { selectedColor = value; }
    private long productId;

    private String productName;

    private int quantity;

    private BigDecimal price;
    private BigDecimal expectedPrice;
    public BigDecimal getExpectedPrice() { return expectedPrice; }
    public void setExpectedPrice(BigDecimal value) { expectedPrice = value; }

    public OrderItem() {
    }

    public long getProductId() {
        return productId;
    }

    public void setProductId(long productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }
}