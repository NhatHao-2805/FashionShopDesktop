package com.fashionshop.customer.view;

import com.fashionshop.customer.config.ServerConfig;
import com.fashionshop.customer.model.Product;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import java.text.NumberFormat;
import java.util.Locale;

public class ProductCard extends VBox {
    private static final int IMAGE_WIDTH = 212;
    private static final int IMAGE_HEIGHT = 246;

    public ProductCard(Product product) {
        getStyleClass().add("product-card");
        setPrefWidth(IMAGE_WIDTH);
        setMinWidth(IMAGE_WIDTH);
        setMaxWidth(IMAGE_WIDTH);
        Label placeholder = text("Chưa có ảnh", "product-meta");
        ImageView imageView = new ImageView();
        imageView.setFitWidth(IMAGE_WIDTH);
        imageView.setFitHeight(IMAGE_HEIGHT);
        imageView.setPreserveRatio(true);
        imageView.setSmooth(true);
        StackPane imageContainer = new StackPane(placeholder, imageView);
        imageContainer.getStyleClass().add("product-image");
        imageContainer.setMinSize(IMAGE_WIDTH, IMAGE_HEIGHT);
        imageContainer.setPrefSize(IMAGE_WIDTH, IMAGE_HEIGHT);
        imageContainer.setMaxSize(IMAGE_WIDTH, IMAGE_HEIGHT);
        imageContainer.setAlignment(Pos.CENTER);
        String imageUrl = ServerConfig.resourceUrl(product.getImageUrl());
        if (imageUrl != null) {
            try {
                Image image = new Image(imageUrl, IMAGE_WIDTH * 2, IMAGE_HEIGHT * 2, true, true, true);
                imageView.setImage(image);
                placeholder.visibleProperty().bind(image.progressProperty().lessThan(1).or(image.errorProperty()));
                image.progressProperty().addListener((observable, previous, progress) -> cropImage(imageView, image));
                cropImage(imageView, image);
            } catch (IllegalArgumentException ignored) {
                placeholder.setText("Ảnh chưa khả dụng");
            }
        }
        Label name = text(value(product.getName()), "product-name");
        name.setWrapText(true);
        name.setMinHeight(36);
        name.setPrefHeight(36);
        Label category = text(value(product.getCategoryName()), "product-category");
        Label price = text(NumberFormat.getCurrencyInstance(Locale.forLanguageTag("vi-VN")).format(product.getPrice()), "product-price");
        Label variants = text("Size " + value(product.getSize()) + "  •  " + value(product.getColor()), "product-meta");
        variants.setWrapText(true);
        variants.setMinHeight(32);
        Label stock = text(product.getStock() > 0 ? "Còn " + product.getStock() + " sản phẩm" : "Tạm hết hàng",
                product.getStock() > 0 ? "stock" : "sold-out");
        Button detail = new Button("Xem chi tiết");
        detail.getStyleClass().add("soft-button");
        detail.setMaxWidth(Double.MAX_VALUE);
        detail.setOnAction(event -> ProductDetailWindow.show(product));
        VBox information = new VBox(5, category, name, price, variants, stock, detail);
        information.getStyleClass().add("product-information");
        getChildren().addAll(imageContainer, information);
    }

    private static void cropImage(ImageView view, Image image) {
        double width = image.getWidth();
        double height = image.getHeight();
        if (width <= 0 || height <= 0 || image.isError()) {
            return;
        }

        double ratio = (double) IMAGE_WIDTH / IMAGE_HEIGHT;
        double cropWidth = Math.min(width, height * ratio);
        double cropHeight = Math.min(height, width / ratio);
        view.setViewport(new Rectangle2D((width - cropWidth) / 2,
                (height - cropHeight) / 2, cropWidth, cropHeight));
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
