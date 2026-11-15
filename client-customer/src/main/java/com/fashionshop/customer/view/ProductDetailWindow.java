package com.fashionshop.customer.view;

import com.fashionshop.customer.model.Product;
import com.fashionshop.customer.service.CartService;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.text.NumberFormat;
import java.util.Locale;

public class ProductDetailWindow {

    private static final String IMAGE_BASE_URL =
            "http://localhost:8080/";

    public static void show(
            Product product
    ) {

        Stage stage =
                new Stage();

        ImageView imageView =
                new ImageView();

        imageView.setFitWidth(350);
        imageView.setFitHeight(350);
        imageView.setPreserveRatio(true);
        imageView.setSmooth(true);

        if (product.getImageUrl() != null
                && !product.getImageUrl().isBlank()) {

            Image image =
                    new Image(
                            IMAGE_BASE_URL + product.getImageUrl(),
                            true
                    );

            imageView.setImage(image);
        }

        Label nameLabel =
                new Label(product.getName());

        nameLabel.setStyle(
                "-fx-font-size: 24px;"
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
                "-fx-font-size: 20px;"
                        + "-fx-font-weight: bold;"
                        + "-fx-text-fill: #bd4c30;"
        );

        Label descriptionLabel =
                new Label(product.getDescription());

        descriptionLabel.setWrapText(true);

        Label categoryLabel =
                new Label(
                        "Danh mục: "
                                + product.getCategoryName()
                );

        Label sizeLabel =
                new Label(
                        "Size: " + product.getSize()
                );

        Label colorLabel =
                new Label(
                        "Màu: " + product.getColor()
                );

        Label stockLabel =
                new Label(
                        "Còn lại: " + product.getStock()
                );

        int maxQuantity =
                Math.max(1, product.getStock());

        Spinner<Integer> quantitySpinner =
                new Spinner<>(
                        1,
                        maxQuantity,
                        1
                );

        quantitySpinner.setEditable(false);
        quantitySpinner.setDisable(product.getStock() <= 0);

        Label quantityLabel =
                new Label("Số lượng:");

        HBox quantityBox =
                new HBox(
                        10,
                        quantityLabel,
                        quantitySpinner
                );

        Button addToCartButton =
                new Button("Thêm vào giỏ hàng");

        addToCartButton.setStyle(
                "-fx-background-color: #c95332;"
                        + "-fx-text-fill: white;"
                        + "-fx-font-weight: bold;"
                        + "-fx-background-radius: 6;"
                        + "-fx-cursor: hand;"
        );

        addToCartButton.setDisable(product.getStock() <= 0);
        if (product.getStock() <= 0) addToCartButton.setText("Tạm hết hàng");
        addToCartButton.setOnAction(event -> {
            if (!PurchaseAccess.requireCustomer()) return;

            int quantity =
                    quantitySpinner.getValue();

            try {
                CartService.getInstance().addProduct(product, quantity);
            } catch (IllegalArgumentException | IllegalStateException e) {
                Alert error = new Alert(Alert.AlertType.WARNING);
                error.setHeaderText(null);
                error.setContentText(e.getMessage());
                error.showAndWait();
                return;
            }

            Alert alert =
                    new Alert(
                            Alert.AlertType.INFORMATION
                    );

            alert.setTitle("Giỏ hàng");
            alert.setHeaderText(null);
            alert.setContentText(
                    "Đã thêm "
                            + quantity
                            + " x "
                            + product.getName()
                            + " vào giỏ hàng."
            );

            alert.showAndWait();
        });

        Button cartButton =
                new Button("Xem giỏ hàng");

        cartButton.setOnAction(event ->
                CartWindow.show()
        );

        HBox buttonBox =
                new HBox(
                        10,
                        addToCartButton,
                        cartButton
                );

        VBox infoBox =
                new VBox(
                        12,
                        nameLabel,
                        priceLabel,
                        descriptionLabel,
                        categoryLabel,
                        sizeLabel,
                        colorLabel,
                        stockLabel,
                        quantityBox,
                        buttonBox
                );

        infoBox.setPadding(new Insets(20));

        BorderPane root =
                new BorderPane();

        root.setPadding(new Insets(20));
        root.setLeft(imageView);
        root.setCenter(infoBox);

        Scene scene =
                new Scene(
                        root,
                        850,
                        520
                );

        stage.setTitle("Chi tiết sản phẩm");
        scene.getStylesheets().add(ProductDetailWindow.class.getResource("/com/fashionshop/customer/shop.css").toExternalForm());
        stage.setScene(scene);
        stage.show();
    }
}