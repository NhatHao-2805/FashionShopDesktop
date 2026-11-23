package com.fashionshop.customer.view;

import com.fashionshop.customer.model.CartItem;
import com.fashionshop.customer.model.User;
import com.fashionshop.customer.service.CartService;
import com.fashionshop.customer.service.CheckoutKeyStore;
import com.fashionshop.customer.service.OrderService;
import com.fashionshop.customer.service.SessionManager;

import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.prefs.BackingStoreException;

public class CheckoutWindow {
    private record PendingOrder(OrderService.PreparedOrder request, Map<CartItem, Integer> purchased) { }
    private static final Map<Long, PendingOrder> PENDING_ORDERS = new HashMap<>();
    private final NumberFormat currency = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("vi-VN"));

    private final CartService cartService =
            CartService.getInstance();

    private final SessionManager sessionManager =
            SessionManager.getInstance();

    private final OrderService orderService =
            new OrderService();

    public static void show() {
        PurchaseAccess.withCustomer(() -> new CheckoutWindow().open());
    }

    private void open() {
        User user = sessionManager.getCurrentUser();
        PendingOrder pending = PENDING_ORDERS.get(user.getId());
        String remembered;
        try {
            remembered = CheckoutKeyStore.pendingKey(user.getId());
        } catch (SecurityException failure) {
            var alert = new Alert(Alert.AlertType.ERROR, "Không đọc được mã đặt hàng đã lưu trên máy.");
            alert.initOwner(PageNavigation.window());
            alert.showAndWait();
            return;
        }
        if (cartService.getItems().isEmpty() && pending == null && remembered == null) {

            Alert alert =
                    new Alert(
                            Alert.AlertType.WARNING
                    );

            alert.setHeaderText(null);
            alert.setContentText(
                    "Giỏ hàng đang trống."
            );

            alert.showAndWait();

            return;
        }

        if (!sessionManager.isCustomer()) {

            Alert alert =
                    new Alert(
                            Alert.AlertType.ERROR
                    );

            alert.setHeaderText(null);
            alert.setContentText(
                    "Bạn chưa đăng nhập."
            );

            alert.showAndWait();

            return;
        }

        Label titleLabel =
                new Label("THÔNG TIN ĐẶT HÀNG");

        titleLabel.setStyle(
                "-fx-font-size: 24px;"
                        + "-fx-font-weight: bold;"
        );

        TextField nameField =
                new TextField();

        nameField.setPromptText(
                "Tên người nhận"
        );

        nameField.setText(
                user.getName() == null
                        ? ""
                        : user.getName()
        );

        TextField phoneField =
                new TextField();

        phoneField.setPromptText(
                "Số điện thoại"
        );

        phoneField.setText(
                user.getPhone() == null
                        ? ""
                        : user.getPhone()
        );

        TextArea addressArea =
                new TextArea();

        addressArea.setPromptText(
                "Địa chỉ nhận hàng"
        );

        addressArea.setPrefRowCount(3);

        TextArea noteArea =
                new TextArea();

        noteArea.setPromptText(
                "Ghi chú"
        );

        noteArea.setPrefRowCount(2);

        Label paymentLabel =
                new Label(
                        "Phương thức thanh toán: COD"
                );

        Label totalLabel =
                new Label(
                        "Tổng thanh toán: "
                                + currency.format(
                                cartService.getTotalPrice()
                        )
                );

        totalLabel.setStyle(
                "-fx-font-size: 18px;"
                        + "-fx-font-weight: bold;"
                        + "-fx-text-fill: #345348;"
        );

        OperationStatus status = new OperationStatus();

        Button orderButton =
                new Button("Xác nhận đặt hàng");

        orderButton.setMaxWidth(
                Double.MAX_VALUE
        );

        orderButton.setStyle(
                "-fx-background-color: #345348;"
                        + "-fx-text-fill: white;"
                        + "-fx-font-weight: bold;"
                        + "-fx-background-radius: 6;"
        );

        var submitting = new SimpleBooleanProperty(false);
        Button checkOrders = new Button("Kiểm tra đơn hàng của tôi");
        checkOrders.setVisible(false);
        checkOrders.managedProperty().bind(checkOrders.visibleProperty());
        checkOrders.setOnAction(event -> MyOrdersWindow.show());
        checkOrders.disableProperty().bind(submitting);
        var lockFields = submitting.or(checkOrders.visibleProperty());
        nameField.disableProperty().bind(lockFields);
        phoneField.disableProperty().bind(lockFields);
        addressArea.disableProperty().bind(lockFields);
        noteArea.disableProperty().bind(lockFields);
        orderButton.disableProperty().bind(submitting.or(
                Bindings.isEmpty(cartService.getItems()).and(checkOrders.visibleProperty().not())));
        if (pending != null) showPending(status, orderButton, checkOrders);

        orderButton.setOnAction(event -> {
            if (!sessionManager.isCustomer() || sessionManager.getCurrentUser().getId() != user.getId()) {
                status.info("Phiên đăng nhập đã thay đổi. Vui lòng mở lại giỏ hàng.");
                return;
            }

            PendingOrder previous = PENDING_ORDERS.get(user.getId());
            if (previous != null) {
                submit(user.getId(), previous, status, submitting, orderButton, checkOrders);
                return;
            }

            String receiverName =
                    nameField.getText().trim();

            String phone =
                    phoneField.getText().trim();

            String address =
                    addressArea.getText().trim();

            String note =
                    noteArea.getText().trim();

            if (receiverName.isBlank()
                    || phone.isBlank()
                    || address.isBlank()) {

                status.info(
                        "Vui lòng nhập đầy đủ tên, số điện thoại và địa chỉ."
                );

                return;
            }

            try {
                com.fashionshop.customer.service.ShippingValidation.validate(receiverName, phone, address, note);
            } catch (IllegalArgumentException e) {
                status.error(e.getMessage(), null);
                return;
            }
            submitting.set(true);
            status.loading("Đang kiểm tra giá và tồn kho mới nhất… Nút đặt hàng sẽ mở lại nếu cần bạn chỉnh sửa.");
            Map<CartItem, Integer> purchased = new HashMap<>();
            var snapshot = cartService.getItems().stream().map(item -> {
                purchased.put(item, item.getQuantity());
                return new CartItem(item.getProduct(), item.getQuantity(), item.getSelectedSize(), item.getSelectedColor());
            }).toList();
            javafx.concurrent.Task<com.fashionshop.customer.service.CheckoutReview.Result> reviewTask = new javafx.concurrent.Task<>() {
                @Override protected com.fashionshop.customer.service.CheckoutReview.Result call() {
                    return com.fashionshop.customer.service.CheckoutReview.review(snapshot,
                            new com.fashionshop.customer.service.ProductService().getAllProducts());
                }
            };
            reviewTask.setOnFailed(e -> {
                status.error(reviewTask.getException().getMessage(), null);
                submitting.set(false);
            });
            reviewTask.setOnSucceeded(e -> {
                if (!PageNavigation.isCurrent(orderButton.getScene()) || !sameCustomer(user.getId())) {
                    status.info("Phiên đăng nhập đã thay đổi. Vui lòng mở lại giỏ hàng.");
                    submitting.set(false);
                    return;
                }
                var review = reviewTask.getValue();
                totalLabel.setText("Tổng thanh toán: " + currency.format(review.total()));
                if (!review.changes().isEmpty()) {
                    Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                    confirm.initOwner(PageNavigation.window());
                    confirm.setHeaderText("Giá hoặc tồn kho đã thay đổi");
                    confirm.setContentText(String.join("\n", review.changes())
                            + "\nTổng mới: " + currency.format(review.total()) + "\nBạn đồng ý đặt hàng theo thông tin mới?");
                    if (confirm.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) {
                        status.info("Chưa đặt hàng. Bạn có thể kiểm tra lại giỏ hàng.");
                        submitting.set(false);
                        return;
                    }
                }
                if (!sameCustomer(user.getId()) || !PageNavigation.isCurrent(orderButton.getScene())) {
                    submitting.set(false);
                    return;
                }
                try {
                    String requestId = CheckoutKeyStore.remember(user.getId());
                    var request = orderService.prepareOrder(requestId, receiverName, phone, address, note, review.items());
                    var submission = new PendingOrder(request, Map.copyOf(purchased));
                    PENDING_ORDERS.put(user.getId(), submission);
                    submit(user.getId(), submission, status, submitting, orderButton, checkOrders);
                } catch (IllegalArgumentException failure) {
                    status.error(failure.getMessage(), null);
                    submitting.set(false);
                } catch (BackingStoreException | SecurityException failure) {
                    status.error("Không lưu được mã yêu cầu trên máy. Đơn hàng chưa được gửi.", null);
                    submitting.set(false);
                }
            });
            startTask(reviewTask);
        });

        Button cancelButton =
                new Button("Hủy");

        cancelButton.setMaxWidth(
                Double.MAX_VALUE
        );

        cancelButton.setOnAction(event -> PageNavigation.back(cancelButton.getScene()));
        cancelButton.disableProperty().bind(submitting);

        VBox root =
                new VBox(
                        12,
                        titleLabel,
                        new Label("Người nhận:"),
                        nameField,
                        new Label("Số điện thoại:"),
                        phoneField,
                        new Label("Địa chỉ:"),
                        addressArea,
                        new Label("Ghi chú:"),
                        noteArea,
                        paymentLabel,
                        totalLabel,
                        orderButton,
                        cancelButton,
                        status,
                        checkOrders
                );

        root.setPadding(
                new Insets(25)
        );

        root.setMaxWidth(560);
        StackPane form = new StackPane(root);
        ScrollPane scroll = new ScrollPane(form);
        scroll.setFitToWidth(true);
        Scene scene =
                new Scene(
                        scroll,
                        500,
                        620
                );

        scene.getStylesheets().add(CheckoutWindow.class.getResource("/com/fashionshop/customer/shop.css").toExternalForm());

        PageNavigation.show("Đặt hàng", scene);
        PageNavigation.guardBack(scene, () -> !submitting.get());
        if (pending == null && remembered != null) {
            recover(user.getId(), remembered, status, submitting, orderButton);
        }
    }
    private void submit(long userId, PendingOrder pending, OperationStatus status,
                        SimpleBooleanProperty submitting, Button orderButton, Button checkOrders) {
        boolean retry = checkOrders.isVisible();
        submitting.set(true);
        status.loading("Đang gửi yêu cầu đặt hàng… Vui lòng chờ xác nhận.");
        Task<OrderService.OrderResult> task = new Task<>() {
            @Override
            protected OrderService.OrderResult call() throws Exception {
                if (!sameCustomer(userId)) throw new IllegalStateException("Tài khoản đã thay đổi.");
                return orderService.submitOrder(pending.request());
            }
        };
        task.setOnFailed(event -> {
            Throwable failure = task.getException();
            if (failure instanceof OrderService.UnconfirmedOrderException
                    || (retry && !(failure instanceof OrderService.RejectedOrderException))) {
                showPending(status, orderButton, checkOrders);
            } else {
                PENDING_ORDERS.remove(userId, pending);
                orderButton.setText("Xác nhận đặt hàng");
                checkOrders.setVisible(false);
            }
            status.error(failure.getMessage(), failure instanceof OrderService.CheckoutConflict
                    ? () -> recover(userId, pending.request().requestId(), status, submitting, orderButton) : null);
            submitting.set(false);
        });
        task.setOnSucceeded(event -> {
            PENDING_ORDERS.remove(userId, pending);
            var order = task.getValue();
            if (sameCustomer(userId) && !"CANCELLED".equals(order.getStatus())) {
                cartService.confirmPurchase(pending.purchased());
            }
            confirm(userId, pending.request().requestId(), order, status, submitting, orderButton);
        });
        startTask(task);
    }

    private void showPending(OperationStatus status, Button orderButton, Button checkOrders) {
        status.info("Gửi lại sẽ dùng nguyên thông tin và mã của yêu cầu đang chờ xác nhận.");
        orderButton.setText("Gửi lại yêu cầu cũ");
        checkOrders.setVisible(true);
    }

    private void recover(long userId, String requestId, OperationStatus status,
                         SimpleBooleanProperty submitting, Button orderButton) {
        submitting.set(true);
        status.loading("Đang kiểm tra yêu cầu đặt hàng trước đó…");
        Task<OrderService.OrderResult> lookup = new Task<>() {
            @Override
            protected OrderService.OrderResult call() throws Exception {
                return orderService.findOrderForRequest(requestId);
            }
        };
        lookup.setOnSucceeded(event -> {
            var order = lookup.getValue();
            if (order == null) {
                status.info("Chưa tìm thấy đơn đã lưu. Lần đặt tiếp theo sẽ sử dụng cùng mã yêu cầu.");
                submitting.set(false);
            } else {
                PENDING_ORDERS.remove(userId);
                confirm(userId, requestId, order, status, submitting, orderButton);
            }
        });
        lookup.setOnFailed(event -> {
            status.error("Chưa kiểm tra được yêu cầu trước. " + lookup.getException().getMessage(),
                    () -> recover(userId, requestId, status, submitting, orderButton));
            submitting.set(false);
        });
        startTask(lookup);
    }

    private void confirm(long userId, String requestId, OrderService.OrderResult order,
                         OperationStatus status, SimpleBooleanProperty submitting, Button orderButton) {
        if (PageNavigation.isCurrent(orderButton.getScene()) && sameCustomer(userId)) {
            boolean cancelled = "CANCELLED".equals(order.getStatus());
            status.success("Đã tìm thấy đơn #" + order.getId() + ".");
            Alert success = new Alert(Alert.AlertType.INFORMATION);
            success.setHeaderText((cancelled ? "Đơn đã hủy #" : "Đã xác nhận đơn #") + order.getId());
            success.setContentText(cancelled ? "Giỏ hàng vẫn được giữ."
                    : "Tổng tiền: " + currency.format(order.getTotalAmount()));
            success.initOwner(PageNavigation.window());
            if (success.showAndWait().orElse(null) == ButtonType.OK) {
                try {
                    CheckoutKeyStore.clear(userId, requestId);
                } catch (BackingStoreException | SecurityException error) {
                    // A stored key can safely recover the same order again.
                }
            }
        }
        submitting.set(false);
        PageNavigation.back(orderButton.getScene());
    }

    private boolean sameCustomer(long id) {
        return sessionManager.isCustomer() && sessionManager.getCurrentUser().getId() == id;
    }
    private void startTask(javafx.concurrent.Task<?> task) {
        Thread worker = new Thread(task, "checkout-worker");
        worker.setDaemon(true);
        worker.start();
    }

}
