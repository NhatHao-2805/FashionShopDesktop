package com.fashionshop.customer.view;

import com.fashionshop.customer.model.Product;

import javafx.geometry.Insets;
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

    private static final String IMAGE_BASE_URL =
            "http://localhost:8080/";

    public ProductCard(Product product) {

        setSpacing(10);
        setPadding(new Insets(15));

        setPrefWidth(230);
        setMinWidth(230);
        setMaxWidth(230);

        setStyle(
                "-fx-background-color: white;"
                        + "-fx-border-color: #dddddd;"
                        + "-fx-border-radius: 10;"
                        + "-fx-background-radius: 10;"
                        + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 8, 0, 0, 2);"
        );

        ImageView imageView = new ImageView();

        imageView.setFitWidth(200);
        imageView.setFitHeight(180);
        imageView.setPreserveRatio(true);
        imageView.setSmooth(true);

        StackPane imageContainer =
                new StackPane(imageView);

        imageContainer.setPrefSize(200, 180);
        imageContainer.setMinSize(200, 180);
        imageContainer.setMaxSize(200, 180);
        imageContainer.setAlignment(Pos.CENTER);

        imageContainer.setStyle(
                "-fx-background-color: #f5f5f5;"
                        + "-fx-background-radius: 8;"
        );

        if (product.getImageUrl() != null
                && !product.getImageUrl().isBlank()) {

            Image image =
                    new Image(
                            IMAGE_BASE_URL + product.getImageUrl(),
                            200,
                            180,
                            true,
                            true,
                            true
                    );

            imageView.setImage(image);
        }

        Label nameLabel =
                new Label(product.getName());

        nameLabel.setWrapText(true);

        nameLabel.setStyle(
                "-fx-font-size: 16px;"
                        + "-fx-font-weight: bold;"
        );

        NumberFormat currency =
                NumberFormat.getCurrencyInstance(
                        new Locale("vi", "VN")
                );

        Label priceLabel =
                new Label(
                        currency.format(product.getPrice())
                );

        priceLabel.setStyle(
                "-fx-font-size: 16px;"
                        + "-fx-font-weight: bold;"
                        + "-fx-text-fill: #d32f2f;"
        );

        Label categoryLabel =
                new Label(
                        "Danh mục: "
                                + product.getCategoryName()
                );

        Label sizeLabel =
                new Label(
                        "Size: "
                                + product.getSize()
                );

        Label colorLabel =
                new Label(
                        "Màu: "
                                + product.getColor()
                );

        Label stockLabel =
                new Label(
                        "Còn lại: "
                                + product.getStock()
                );

        Button detailButton =
                new Button("Xem chi tiết");

        detailButton.setMaxWidth(
                Double.MAX_VALUE
        );

        detailButton.setStyle(
                "-fx-background-color: #222222;"
                        + "-fx-text-fill: white;"
                        + "-fx-font-weight: bold;"
                        + "-fx-background-radius: 6;"
                        + "-fx-cursor: hand;"
        );

        detailButton.setOnAction(
                event ->
                        ProductDetailWindow.show(product)
        );

        getChildren().addAll(
                imageContainer,
                nameLabel,
                priceLabel,
                categoryLabel,
                sizeLabel,
                colorLabel,
                stockLabel,
                detailButton
        );
    }
}