package com.fashionshop.customer.view;

import com.fashionshop.customer.model.CartItem;
import com.fashionshop.customer.service.CartService;

import javafx.collections.ListChangeListener;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.text.NumberFormat;
import java.util.Locale;

public class CartWindow {

    private final CartService cartService =
            CartService.getInstance();

    private final VBox itemsBox =
            new VBox(10);

    private final Label totalQuantityLabel =
            new Label();

    private final Label totalPriceLabel =
            new Label();

    private final NumberFormat currency =
            NumberFormat.getCurrencyInstance(
                    new Locale("vi", "VN")
            );

    public static void show() {
        if (!PurchaseAccess.requireCustomer()) return;
        new CartWindow().open();
    }

    private void open() {

        Stage stage =
                new Stage();

        Label titleLabel =
                new Label("GIỎ HÀNG");

        titleLabel.setStyle(
                "-fx-font-size: 24px;"
                        + "-fx-font-weight: bold;"
        );

        itemsBox.setPadding(new Insets(10));

        ScrollPane scrollPane =
                new ScrollPane(itemsBox);

        scrollPane.setFitToWidth(true);

        totalQuantityLabel.setStyle(
                "-fx-font-size: 16px;"
                        + "-fx-font-weight: bold;"
        );

        totalPriceLabel.setStyle(
                "-fx-font-size: 18px;"
                        + "-fx-font-weight: bold;"
                        + "-fx-text-fill: #bd4c30;"
        );

Button checkoutButton =
        new Button("Đặt hàng");

checkoutButton.setStyle(
        "-fx-background-color: #bd4c30;"
                + "-fx-text-fill: white;"
                + "-fx-font-weight: bold;"
);

checkoutButton.setOnAction(event ->
        CheckoutWindow.show()
);

        Button clearButton =
                new Button("Xóa toàn bộ");

        clearButton.setStyle(
                "-fx-background-color: #444444;"
                        + "-fx-text-fill: white;"
                        + "-fx-font-weight: bold;"
        );

        clearButton.setOnAction(event -> {
            cartService.clear();
            refresh();
        });

        Button closeButton =
                new Button("Đóng");

        closeButton.setOnAction(event ->
                stage.close()
        );

        HBox leftBottomBox =
                new HBox(
                        20,
                        totalQuantityLabel,
                        totalPriceLabel
                );

        HBox spacer =
                new HBox();
        HBox.setHgrow(spacer, Priority.ALWAYS);

      HBox rightBottomBox =
        new HBox(
                10,
                checkoutButton,
                clearButton,
                closeButton
        );

        HBox bottomBox =
                new HBox(
                        20,
                        leftBottomBox,
                        spacer,
                        rightBottomBox
                );

        bottomBox.setAlignment(Pos.CENTER_LEFT);
        bottomBox.setPadding(new Insets(15, 0, 0, 0));

        BorderPane root =
                new BorderPane();

        root.setPadding(new Insets(20));

        root.setTop(titleLabel);
        root.setCenter(scrollPane);
        root.setBottom(bottomBox);

        BorderPane.setMargin(
                titleLabel,
                new Insets(0, 0, 15, 0)
        );

        Scene scene =
                new Scene(
                        root,
                        760,
                        520
                );

        stage.setTitle("Giỏ hàng");
        scene.getStylesheets().add(CartWindow.class.getResource("/com/fashionshop/customer/shop.css").toExternalForm());
        stage.setScene(scene);

        long customerId = com.fashionshop.customer.service.SessionManager.getInstance().getCurrentUser().getId();
        ListChangeListener<CartItem> listener = change -> {
            Runnable update = () -> {
                var session = com.fashionshop.customer.service.SessionManager.getInstance();
                if (!session.isCustomer() || session.getCurrentUser().getId() != customerId) stage.close();
                else refresh();
            };
            if (javafx.application.Platform.isFxApplicationThread()) update.run();
            else javafx.application.Platform.runLater(update);
        };
        cartService.getItems().addListener(listener);
        stage.setOnHidden(event -> cartService.getItems().removeListener(listener));

        refresh();

        stage.show();
    }

    private void refresh() {

        itemsBox.getChildren().clear();

        if (cartService.getItems().isEmpty()) {

            Label emptyLabel =
                    new Label("Giỏ hàng đang trống.");

            emptyLabel.setStyle(
                    "-fx-font-size: 16px;"
                            + "-fx-text-fill: #666666;"
            );

            itemsBox.getChildren().add(emptyLabel);

        } else {

            for (CartItem item : cartService.getItems()) {

                Label nameLabel =
                        new Label(
                                item.getProduct().getName() + " | " + java.util.Objects.toString(item.getSelectedSize(), "—") + " / " + java.util.Objects.toString(item.getSelectedColor(), "—")
                        );

                nameLabel.setStyle(
                        "-fx-font-size: 15px;"
                                + "-fx-font-weight: bold;"
                );

                Label infoLabel =
                        new Label(
                                "Số lượng: "
                                        + item.getQuantity()
                                        + " | Đơn giá: "
                                        + currency.format(
                                        item.getProduct().getPrice()
                                )
                                        + " | Thành tiền: "
                                        + currency.format(
                                        item.getSubtotal()
                                )
                        );

                Button removeButton =
                        new Button("Xóa");

                removeButton.setOnAction(event -> {
                    cartService.removeItem(item);
                    refresh();
                });

                VBox textBox =
                        new VBox(
                                5,
                                nameLabel,
                                infoLabel
                        );

                HBox spacer =
                        new HBox();
                HBox.setHgrow(spacer, Priority.ALWAYS);

                HBox row =
                        new HBox(
                                15,
                                textBox,
                                spacer,
                                removeButton
                        );

                row.setAlignment(Pos.CENTER_LEFT);
                row.setPadding(new Insets(12));

                row.setStyle(
                        "-fx-background-color: white;"
                                + "-fx-border-color: #dddddd;"
                                + "-fx-border-radius: 8;"
                                + "-fx-background-radius: 8;"
                );

                itemsBox.getChildren().add(row);
            }
        }

        totalQuantityLabel.setText(
                "Tổng số lượng: "
                        + cartService.getTotalQuantity()
        );

        totalPriceLabel.setText(
                "Tổng tiền: "
                        + currency.format(
                        cartService.getTotalPrice()
                )
        );
    }
}