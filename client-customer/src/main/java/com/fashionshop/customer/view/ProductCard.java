package com.fashionshop.customer.view;

import com.fashionshop.customer.config.ServerConfig;
import com.fashionshop.customer.model.Product;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import java.text.NumberFormat;
import java.util.Locale;

public class ProductCard extends VBox {
    public ProductCard(Product product) {
        getStyleClass().add("product-card");
        setPrefWidth(216);
        setMinWidth(216);
        setMaxWidth(216);
        Label placeholder = text("FASHION SHOP", "product-category");
        ImageView imageView = new ImageView();
        imageView.setFitWidth(190);
        imageView.setFitHeight(210);
        imageView.setPreserveRatio(true);
        imageView.setSmooth(true);
        StackPane imageContainer = new StackPane(placeholder, imageView);
        imageContainer.getStyleClass().add("product-image");
        imageContainer.setMinSize(190, 220);
        imageContainer.setPrefSize(190, 220);
        imageContainer.setMaxSize(190, 220);
        imageContainer.setAlignment(Pos.CENTER);
        String imageUrl = ServerConfig.resourceUrl(product.getImageUrl());
        if (imageUrl != null) {
            try {
                Image image = new Image(imageUrl, 190, 210, true, true, true);
                imageView.setImage(image);
                placeholder.visibleProperty().bind(image.progressProperty().lessThan(1).or(image.errorProperty()));
            } catch (IllegalArgumentException ignored) {
                placeholder.setText("Ảnh chưa khả dụng");
            }
        }
        Label name = text(value(product.getName()), "product-name");
        name.setWrapText(true);
        name.setMinHeight(42);
        name.setPrefHeight(42);
        Label category = text(value(product.getCategoryName()).toUpperCase(Locale.forLanguageTag("vi")), "product-category");
        Label price = text(NumberFormat.getCurrencyInstance(Locale.forLanguageTag("vi-VN")).format(product.getPrice()), "product-price");
        Label variants = text("Size " + value(product.getSize()) + "  •  " + value(product.getColor()), "product-meta");
        variants.setWrapText(true);
        variants.setMinHeight(32);
        Label stock = text(product.getStock() > 0 ? "Còn " + product.getStock() + " sản phẩm" : "Tạm hết hàng",
                product.getStock() > 0 ? "stock" : "sold-out");
        Button detail = new Button("Khám phá sản phẩm  →");
        detail.getStyleClass().add("soft-button");
        detail.setMaxWidth(Double.MAX_VALUE);
        detail.setOnAction(event -> ProductDetailWindow.show(product));
        getChildren().addAll(imageContainer, category, name, price, variants, stock, detail);
    }

    private static Label text(String value, String style) {
        Label label = new Label(value);
        label.getStyleClass().add(style);
        return label;
    }

    private static String value(String value) {
        return value == null || value.isBlank() ? "—" : value;
    }
}
