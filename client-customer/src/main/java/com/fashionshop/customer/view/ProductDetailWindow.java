package com.fashionshop.customer.view;

import com.fashionshop.customer.config.ServerConfig;
import com.fashionshop.customer.model.Product;
import com.fashionshop.customer.service.CartService;

import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Priority;

import java.text.NumberFormat;
import java.util.Locale;

public class ProductDetailWindow {

    public static void show(
            Product product
    ) {

        ImageView imageView =
                new ImageView();

        imageView.setFitWidth(430);
        imageView.setFitHeight(430);
        imageView.setPreserveRatio(true);
        imageView.setSmooth(true);

        Label imageStatus = new Label("Chưa có ảnh sản phẩm");
        imageStatus.getStyleClass().add("muted");
        StackPane imagePane = new StackPane(imageView, imageStatus);
        imagePane.getStyleClass().add("product-image");
        imagePane.setMinSize(430, 430);
        imagePane.setPrefSize(430, 430);
        imagePane.setMaxSize(430, 430);

        String imageUrl = ServerConfig.resourceUrl(product.getImageUrl());
        if (imageUrl != null) {
            try {
                Image image = new Image(imageUrl, 700, 700, true, true, true);
                imageView.setImage(image);
                imageStatus.textProperty().bind(Bindings.when(image.errorProperty())
                        .then("Không tải được ảnh sản phẩm")
                        .otherwise("Đang tải ảnh…"));
                imageStatus.visibleProperty().bind(
                        image.errorProperty().or(image.progressProperty().lessThan(1)));
            } catch (IllegalArgumentException ignored) {
                imageStatus.setText("Không tải được ảnh sản phẩm");
            }
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
                        + "-fx-text-fill: #345348;"
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

        javafx.scene.control.ComboBox<String> sizeChoice = new javafx.scene.control.ComboBox<>();
        javafx.scene.control.ComboBox<String> colorChoice = new javafx.scene.control.ComboBox<>();
        for (String value : (product.getSize() == null ? "" : product.getSize()).split(","))
            if (!value.isBlank()) sizeChoice.getItems().add(value.trim());
        for (String value : (product.getColor() == null ? "" : product.getColor()).split(","))
            if (!value.isBlank()) colorChoice.getItems().add(value.trim());
        sizeChoice.setPromptText("Chọn size");
        colorChoice.setPromptText("Chọn màu");
        if (sizeChoice.getItems().size() == 1) sizeChoice.getSelectionModel().selectFirst();
        if (colorChoice.getItems().size() == 1) colorChoice.getSelectionModel().selectFirst();

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
                "-fx-background-color: #345348;"
                        + "-fx-text-fill: white;"
                        + "-fx-font-weight: bold;"
                        + "-fx-background-radius: 6;"
                        + "-fx-cursor: hand;"
        );

        Runnable refreshStock = () -> {
            String size = sizeChoice.getValue(), color = colorChoice.getValue();
            boolean selected = (size != null || sizeChoice.getItems().isEmpty())
                    && (color != null || colorChoice.getItems().isEmpty());
            int available = product.availableStock(size, color);
            boolean canBuy = selected && available > 0;
            stockLabel.setText(selected ? "Còn lại: " + available : "Chọn size và màu để xem tồn kho");
            int quantity = Math.min(quantitySpinner.getValue(), Math.max(1, available));
            quantitySpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(
                    1, Math.max(1, available), quantity));
            quantitySpinner.setDisable(!canBuy);
            addToCartButton.setDisable(!canBuy);
            addToCartButton.setText(selected && available == 0 ? "Tạm hết hàng" : "Thêm vào giỏ hàng");
        };
        sizeChoice.valueProperty().addListener((observable, oldValue, newValue) -> refreshStock.run());
        colorChoice.valueProperty().addListener((observable, oldValue, newValue) -> refreshStock.run());
        refreshStock.run();
        addToCartButton.setOnAction(event -> PurchaseAccess.withCustomer(() -> {

            int quantity =
                    quantitySpinner.getValue();

            try {
                if ((!sizeChoice.getItems().isEmpty() && sizeChoice.getValue() == null)
                        || (!colorChoice.getItems().isEmpty() && colorChoice.getValue() == null)) {
                    throw new IllegalArgumentException("Vui lòng chọn size và màu.");
                }
                CartService.getInstance().addProduct(product, quantity, sizeChoice.getValue(), colorChoice.getValue());
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
        }));

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
                        sizeLabel, sizeChoice,
                        colorLabel, colorChoice,
                        stockLabel,
                        quantityBox,
                        buttonBox
                );

        infoBox.setPadding(new Insets(28, 34, 28, 34));
        infoBox.setPrefWidth(540);
        infoBox.setMaxWidth(540);
        infoBox.getStyleClass().add("product-detail-info");

        HBox productCard = new HBox(34, imagePane, infoBox);
        productCard.setPadding(new Insets(28));
        productCard.setMaxSize(1060, 520);
        productCard.getStyleClass().add("product-detail-card");
        HBox.setHgrow(infoBox, Priority.NEVER);

        BorderPane root =
                new BorderPane();

        StackPane content = new StackPane(productCard);
        content.setPadding(new Insets(28));
        root.setCenter(content);

        Scene scene =
                new Scene(
                        root,
                        1120,
                        680
                );

        scene.getStylesheets().add(ProductDetailWindow.class.getResource("/com/fashionshop/customer/shop.css").toExternalForm());
        PageNavigation.show("Chi tiết sản phẩm", scene);
        PageNavigation.guardBack(scene, () -> true);
    }
}
