package com.fashionshop.customer.model;

public class CartItem {


    private String selectedSize;
    private String selectedColor;
    public String getSelectedSize() { return selectedSize; }
    public String getSelectedColor() { return selectedColor; }
    public void setSelectedSize(String value) { selectedSize = value; }
    public void setSelectedColor(String value) { selectedColor = value; }
    private Product product;
    private int quantity;

    public CartItem(
            Product product,
            int quantity
    ) {

        this.product = product;
        this.quantity = quantity;
    }

    public CartItem(Product product, int quantity, String size, String color) {
        this(product, quantity);
        selectedSize = size;
        selectedColor = color;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(
            Product product
    ) {

        this.product = product;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(
            int quantity
    ) {

        this.quantity = quantity;
    }

    public double getSubtotal() {

        return product.getPrice()
                * quantity;
    }
}