package com.fashionshop.customer.view;

import com.fashionshop.customer.service.OrderService;

import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class MyOrdersWindow {

    private final OrderService orderService =
            new OrderService();

    private final VBox ordersBox =
            new VBox(12);

    private final Label statusLabel =
            new Label(
                    "Đang tải đơn hàng..."
            );

    private final ProgressIndicator progressIndicator =
            new ProgressIndicator();

    private final NumberFormat currency =
            NumberFormat.getCurrencyInstance(
                    new Locale(
                            "vi",
                            "VN"
                    )
            );

    private Stage stage;

    public static void show() {

        new MyOrdersWindow()
                .open();
    }

    private void open() {

        stage =
                new Stage();

        Label titleLabel =
                new Label(
                        "ĐƠN HÀNG CỦA TÔI"
                );

        titleLabel.setStyle(
                "-fx-font-size: 24px;"
                        + "-fx-font-weight: bold;"
        );

        Button refreshButton =
                new Button(
                        "Làm mới"
                );

        refreshButton.setOnAction(
                event -> loadOrders()
        );

        HBox titleBox =
                new HBox(
                        12,
                        titleLabel,
                        new RegionSpacer(),
                        refreshButton
                );

        titleBox.setAlignment(
                Pos.CENTER_LEFT
        );

        HBox.setHgrow(
                titleBox.getChildren().get(1),
                Priority.ALWAYS
        );

        ordersBox.setPadding(
                new Insets(5)
        );

        ScrollPane scrollPane =
                new ScrollPane(
                        ordersBox
                );

        scrollPane.setFitToWidth(true);

        scrollPane.setPrefHeight(
                560
        );

        progressIndicator.setPrefSize(
                25,
                25
        );

        HBox statusBox =
                new HBox(
                        10,
                        progressIndicator,
                        statusLabel
                );

        statusBox.setAlignment(
                Pos.CENTER_LEFT
        );

        VBox root =
                new VBox(
                        15,
                        titleBox,
                        scrollPane,
                        statusBox
                );

        root.setPadding(
                new Insets(20)
        );

        Scene scene =
                new Scene(
                        root,
                        820,
                        680
                );

        stage.setTitle(
                "Fashion Shop - Đơn hàng của tôi"
        );

        scene.getStylesheets().add(MyOrdersWindow.class.getResource("/com/fashionshop/customer/shop.css").toExternalForm());

        stage.setScene(scene);
        stage.show();

        loadOrders();
    }

    private void loadOrders() {

        setLoading(
                true,
                "Đang tải đơn hàng..."
        );

        Task<List<OrderService.OrderResult>> task =
                new Task<>() {

                    @Override
                    protected List<OrderService.OrderResult> call()
                            throws Exception {

                        return orderService
                                .getMyOrders();
                    }
                };

        task.setOnSucceeded(event -> {

            setLoading(
                    false,
                    ""
            );

            displayOrders(
                    task.getValue()
            );
        });

        task.setOnFailed(event -> {

            setLoading(
                    false,
                    "Không thể tải đơn hàng: "
                            + getErrorMessage(
                            task.getException()
                    )
            );

            ordersBox
                    .getChildren()
                    .clear();
        });

        Thread thread =
                new Thread(task);

        thread.setDaemon(true);
        thread.start();
    }

    private void displayOrders(
            List<OrderService.OrderResult> orders
    ) {

        ordersBox
                .getChildren()
                .clear();

        if (orders == null
                || orders.isEmpty()) {

            statusLabel.setText(
                    "Bạn chưa có đơn hàng."
            );

            return;
        }

        for (OrderService.OrderResult order :
                orders) {

            ordersBox
                    .getChildren()
                    .add(
                            createOrderCard(
                                    order
                            )
                    );
        }

        statusLabel.setText(
                "Tổng cộng: "
                        + orders.size()
                        + " đơn hàng."
        );
    }

    private VBox createOrderCard(
            OrderService.OrderResult order
    ) {

        Label idLabel =
                new Label(
                        "Đơn hàng #"
                                + order.getId()
                );

        idLabel.setStyle(
                "-fx-font-size: 17px;"
                        + "-fx-font-weight: bold;"
        );

        Label status =
                new Label(
                        getStatusText(
                                order.getStatus()
                        )
                );

        status.setStyle(
                getStatusStyle(
                        order.getStatus()
                )
        );

        HBox heading =
                new HBox(
                        10,
                        idLabel,
                        new RegionSpacer(),
                        status
                );

        HBox.setHgrow(
                heading.getChildren().get(1),
                Priority.ALWAYS
        );

        heading.setAlignment(
                Pos.CENTER_LEFT
        );

        Label totalLabel =
                new Label(
                        "Tổng tiền: "
                                + formatMoney(
                                order.getTotalAmount()
                        )
                );

        totalLabel.setStyle(
                "-fx-font-weight: bold;"
        );

        Label receiver =
                new Label(
                        "Người nhận: "
                                + safe(
                                order.getReceiverName()
                        )
                );

        Label phone =
                new Label(
                        "SĐT: "
                                + safe(
                                order.getPhone()
                        )
                );

        Label address =
                new Label(
                        "Địa chỉ: "
                                + safe(
                                order.getAddress()
                        )
                );

        address.setWrapText(true);

        Label payment =
                new Label(
                        "Thanh toán: "
                                + safe(
                                order.getPaymentMethod()
                        )
                );

        Button detailButton =
                new Button(
                        "Xem chi tiết"
                );

        detailButton.setStyle(
                "-fx-background-color: #c95332;"
                        + "-fx-text-fill: white;"
                        + "-fx-font-weight: bold;"
        );

        detailButton.setOnAction(
                event ->
                        OrderDetailWindow.show(
                                order.getId(),
                                this::loadOrders
                        )
        );

        HBox buttons =
                new HBox(
                        10,
                        detailButton
                );

        VBox card =
                new VBox(
                        7,
                        heading,
                        totalLabel,
                        receiver,
                        phone,
                        address,
                        payment,
                        buttons
                );

        card.setPadding(
                new Insets(15)
        );

        card.setStyle(
                "-fx-border-color: #cccccc;"
                        + "-fx-border-radius: 8;"
                        + "-fx-background-radius: 8;"
                        + "-fx-background-color: white;"
        );

        return card;
    }

    private void setLoading(
            boolean loading,
            String message
    ) {

        progressIndicator.setVisible(
                loading
        );

        progressIndicator.setManaged(
                loading
        );

        statusLabel.setText(
                message
        );
    }

    private String formatMoney(
            BigDecimal amount
    ) {

        return currency.format(
                amount == null
                        ? BigDecimal.ZERO
                        : amount
        );
    }

    private String getStatusText(
            String status
    ) {

        if (status == null) {
            return "Không rõ";
        }

        return switch (
                status.toUpperCase()
        ) {

            case "PENDING" ->
                    "Chờ xử lý";

            case "PROCESSING" ->
                    "Đang xử lý";

            case "SHIPPING" ->
                    "Đang giao";

            case "DELIVERED" ->
                    "Đã giao";

            case "CANCELLED" ->
                    "Đã hủy";

            default ->
                    status;
        };
    }

    private String getStatusStyle(
            String status
    ) {

        String base =
                "-fx-font-weight: bold;"
                        + "-fx-padding: 4 8 4 8;"
                        + "-fx-background-radius: 6;";

        if (status == null) {

            return base
                    + "-fx-background-color: #eeeeee;";
        }

        return switch (
                status.toUpperCase()
        ) {

            case "PENDING" ->
                    base
                            + "-fx-background-color: #fff3cd;";

            case "PROCESSING" ->
                    base
                            + "-fx-background-color: #dbeafe;";

            case "SHIPPING" ->
                    base
                            + "-fx-background-color: #e0f2fe;";

            case "DELIVERED" ->
                    base
                            + "-fx-background-color: #dcfce7;";

            case "CANCELLED" ->
                    base
                            + "-fx-background-color: #fee2e2;";

            default ->
                    base
                            + "-fx-background-color: #eeeeee;";
        };
    }

    private String safe(
            String value
    ) {

        return value == null
                || value.isBlank()
                ? "-"
                : value;
    }

    private String getErrorMessage(
            Throwable throwable
    ) {

        if (throwable == null) {
            return "Lỗi không xác định.";
        }

        if (throwable.getMessage() == null
                || throwable.getMessage().isBlank()) {

            return throwable
                    .getClass()
                    .getSimpleName();
        }

        return throwable.getMessage();
    }

    private static class RegionSpacer
            extends javafx.scene.layout.Region {
    }
}