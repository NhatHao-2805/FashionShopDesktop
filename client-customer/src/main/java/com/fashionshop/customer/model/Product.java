package com.fashionshop.customer.model;

public class Product {
    private record Variant(String size, String color, int stock) {}

    private java.util.List<Variant> variants;

    public boolean tracksVariants() {
        return variants != null && !variants.isEmpty();
    }

    public int availableStock(String size, String color) {
        if (!tracksVariants()) {
            return stock;
        }
        String selectedSize = size == null ? "" : size;
        String selectedColor = color == null ? "" : color;
        return variants.stream()
                .filter(variant -> variant.size().equals(selectedSize) && variant.color().equals(selectedColor))
                .mapToInt(Variant::stock)
                .findFirst().orElse(0);
    }

    private long id;
    private String name;
    private String description;
    private double price;
    private Long categoryId;
    private String categoryName;
    private String size;
    private String color;
    private int stock;
    private String imageUrl;
    private String status;

    public Product() {
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
