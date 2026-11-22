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

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;

public class OrderDetailWindow {

    private final OrderService orderService =
            new OrderService();

    private final long orderId;

    private final Runnable onChanged;

    private final VBox contentBox =
            new VBox(10);

    private final OperationStatus status = new OperationStatus();
    private boolean cancelling;

    private final NumberFormat currency =
            NumberFormat.getCurrencyInstance(
                    new Locale(
                            "vi",
                            "VN"
                    )
            );


    private OrderService.OrderResult currentOrder;

    private OrderDetailWindow(
            long orderId,
            Runnable onChanged
    ) {

        this.orderId =
                orderId;

        this.onChanged =
                onChanged;
    }

    public static void show(
            long orderId,
            Runnable onChanged
    ) {

        new OrderDetailWindow(
                orderId,
                onChanged
        ).open();
    }

    private void open() {

        Label titleLabel =
                new Label(
                        "CHI TIẾT ĐƠN HÀNG #"
                                + orderId
                );

        titleLabel.setStyle(
                "-fx-font-size: 22px;"
                        + "-fx-font-weight: bold;"
        );

        ScrollPane scrollPane =
                new ScrollPane(
                        contentBox
                );

        scrollPane.setFitToWidth(true);

        Button closeButton =
                new Button(
                        "Đóng"
                );

        closeButton.setOnAction(
                event -> PageNavigation.back(closeButton.getScene())
        );

        VBox.setVgrow(scrollPane, Priority.ALWAYS);
        closeButton.disableProperty().bind(status.busyProperty());

        VBox root =
                new VBox(
                        15,
                        titleLabel,
                        status,
                        scrollPane,
                        closeButton
                );

        root.setPadding(
                new Insets(20)
        );

        Scene scene =
                new Scene(
                        root,
                        650,
                        650
                );

        scene.getStylesheets().add(OrderDetailWindow.class.getResource("/com/fashionshop/customer/shop.css").toExternalForm());

        PageNavigation.show("Chi tiết đơn hàng", scene);
        PageNavigation.guardBack(scene, () -> !cancelling);

        loadOrder();
    }

    private void loadOrder() {

        if (status.isBusy()) return;
        status.loading("Đang tải chi tiết đơn hàng…");

        Task<OrderService.OrderResult> task =
                new Task<>() {

                    @Override
                    protected OrderService.OrderResult call()
                            throws Exception {

                        return orderService
                                .getOrderDetail(
                                        orderId
                                );
                    }
                };

        task.setOnSucceeded(event -> {

            currentOrder =
                    task.getValue();

            status.info("");

            displayOrder(
                    currentOrder
            );
        });

        task.setOnFailed(event -> {
            contentBox.getChildren().clear();
            status.error("Không tải được chi tiết đơn: " + getErrorMessage(task.getException()), this::loadOrder);
        });

        Thread thread =
                new Thread(task);

        thread.setDaemon(true);
        thread.start();
    }

    private void displayOrder(
            OrderService.OrderResult order
    ) {

        contentBox
                .getChildren()
                .clear();

        if (order == null) {

            this.status.empty("Không tìm thấy đơn hàng.");

            return;
        }

        Label status =
                new Label(
                        "Trạng thái: "
                                + getStatusText(
                                order.getStatus()
                        )
                );

        status.setStyle(
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
                        "Số điện thoại: "
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

        Label note =
                new Label(
                        "Ghi chú: "
                                + safe(
                                order.getNote()
                        )
                );

        note.setWrapText(true);

        Label productsTitle =
                new Label(
                        "SẢN PHẨM"
                );

        productsTitle.setStyle(
                "-fx-font-size: 16px;"
                        + "-fx-font-weight: bold;"
        );

        contentBox
                .getChildren()
                .addAll(
                        status,
                        receiver,
                        phone,
                        address,
                        payment,
                        note,
                        new Separator(),
                        productsTitle
                );

        if (order.getItems().isEmpty()) {

            contentBox
                    .getChildren()
                    .add(
                            new Label(
                                    "Không có thông tin sản phẩm."
                            )
                    );

        } else {

            for (
                    OrderService.OrderItemResult item :
                    order.getItems()
            ) {

                contentBox
                        .getChildren()
                        .add(
                                createItemCard(
                                        item
                                )
                        );
            }
        }

        Label total =
                new Label(
                        "TỔNG TIỀN: "
                                + formatMoney(
                                order.getTotalAmount()
                        )
                );

        total.setStyle(
                "-fx-font-size: 17px;"
                        + "-fx-font-weight: bold;"
        );

        contentBox
                .getChildren()
                .add(
                        new Separator()
                );

        contentBox
                .getChildren()
                .add(
                        total
                );

        if ("PENDING".equalsIgnoreCase(
                order.getStatus()
        )) {

            Button cancelButton =
                    new Button(
                            "Hủy đơn hàng"
                    );

            cancelButton.setStyle(
                    "-fx-background-color: #9a3b34;"
                            + "-fx-text-fill: white;"
                            + "-fx-font-weight: bold;"
            );

            cancelButton.disableProperty().bind(this.status.busyProperty());
            cancelButton.setOnAction(
                    event ->
                            confirmCancel()
            );

            contentBox
                    .getChildren()
                    .add(
                            cancelButton
                    );
        }
    }

    private VBox createItemCard(
            OrderService.OrderItemResult item
    ) {

        String name =
                item.getProductName();

        if (name == null
                || name.isBlank()) {

            name =
                    "Sản phẩm #"
                            + item.getProductId();
        }

        Label nameLabel =
                new Label(name);

        nameLabel.setStyle(
                "-fx-font-weight: bold;"
        );

        Label quantityLabel =
                new Label(
                        "Số lượng: "
                                + item.getQuantity()
                );

        Label priceLabel =
                new Label(
                        "Đơn giá: "
                                + formatMoney(
                                item.getPrice()
                        )
                );

        Label subtotalLabel =
                new Label(
                        "Thành tiền: "
                                + formatMoney(
                                item.getSubtotal()
                        )
                );

        VBox card =
                new VBox(
                        4,
                        nameLabel,
                        quantityLabel,
                        priceLabel,
                        subtotalLabel
                );

        card.setPadding(
                new Insets(10)
        );

        card.setStyle(
                "-fx-border-color: #dddddd;"
                        + "-fx-border-radius: 6;"
                        + "-fx-background-radius: 6;"
                        + "-fx-background-color: #fafafa;"
        );

        return card;
    }

    private void confirmCancel() {

        if (currentOrder == null) {
            return;
        }

        Alert confirm =
                new Alert(
                        Alert.AlertType.CONFIRMATION
                );

        confirm.setTitle(
                "Xác nhận hủy đơn"
        );

        confirm.setHeaderText(
                "Bạn có chắc muốn hủy đơn hàng #"
                        + orderId
                        + "?"
        );

        confirm.setContentText(
                "Sản phẩm sẽ được hoàn lại tồn kho."
        );

        confirm.showAndWait()
                .ifPresent(button -> {

                    if (button
                            == ButtonType.OK) {

                        cancelOrder();
                    }
                });
    }

    private void cancelOrder() {

        if (status.isBusy()) return;
        cancelling = true;
        status.loading("Đang hủy đơn hàng… Vui lòng chờ xác nhận.");

        Task<OrderService.OrderResult> task =
                new Task<>() {

                    @Override
                    protected OrderService.OrderResult call()
                            throws Exception {

                        return orderService
                                .cancelOrder(
                                        orderId
                                );
                    }
                };

        task.setOnSucceeded(event -> {
            cancelling = false;
            currentOrder = task.getValue();
            displayOrder(currentOrder);
            status.success("Đã hủy đơn hàng #" + orderId + ".");
            if (onChanged != null) onChanged.run();
        });

        task.setOnFailed(event -> {
            cancelling = false;
            contentBox.getChildren().clear();
            status.error("Không xác nhận được việc hủy đơn. Hãy tải lại để kiểm tra trạng thái. "
                    + getErrorMessage(task.getException()), () -> {
                if (onChanged != null) onChanged.run();
                loadOrder();
            });
        });

        Thread thread =
                new Thread(task);

        thread.setDaemon(true);
        thread.start();
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

    private String safe(
            String value
    ) {

        return value == null
                || value.isBlank()
                ? "-"
                : value;
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
}