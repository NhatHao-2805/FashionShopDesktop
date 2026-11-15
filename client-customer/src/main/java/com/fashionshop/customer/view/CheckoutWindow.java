package com.fashionshop.customer.view;

import com.fashionshop.customer.model.User;
import com.fashionshop.customer.service.CartService;
import com.fashionshop.customer.service.OrderService;
import com.fashionshop.customer.service.SessionManager;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.text.NumberFormat;
import java.util.Locale;

public class CheckoutWindow {

    private final CartService cartService =
            CartService.getInstance();

    private final SessionManager sessionManager =
            SessionManager.getInstance();

    private final OrderService orderService =
            new OrderService();

    public static void show() {
        if (!PurchaseAccess.requireCustomer()) return;
        new CheckoutWindow().open();
    }

    private void open() {

        if (cartService.getItems().isEmpty()) {

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

        User user =
                sessionManager.getCurrentUser();

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

        Stage stage =
                new Stage();

        stage.initModality(
                Modality.APPLICATION_MODAL
        );

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

        NumberFormat currency =
                NumberFormat.getCurrencyInstance(
                        new Locale("vi", "VN")
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
                        + "-fx-text-fill: #bd4c30;"
        );

        Label messageLabel =
                new Label();

        messageLabel.setWrapText(true);

        Button orderButton =
                new Button("Xác nhận đặt hàng");

        orderButton.setMaxWidth(
                Double.MAX_VALUE
        );

        orderButton.setStyle(
                "-fx-background-color: #bd4c30;"
                        + "-fx-text-fill: white;"
                        + "-fx-font-weight: bold;"
                        + "-fx-background-radius: 6;"
        );

        orderButton.setOnAction(event -> {
            if (!sessionManager.isCustomer() || sessionManager.getCurrentUser().getId() != user.getId()) {
                messageLabel.setText("Phiên đăng nhập đã thay đổi. Vui lòng mở lại giỏ hàng.");
                orderButton.setDisable(true);
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

                messageLabel.setText(
                        "Vui lòng nhập đầy đủ tên, số điện thoại và địa chỉ."
                );

                return;
            }

            try {
                com.fashionshop.customer.service.ShippingValidation.validate(receiverName, phone, address, note);
            } catch (IllegalArgumentException e) {
                messageLabel.setText(e.getMessage());
                return;
            }
            orderButton.setDisable(true);
            messageLabel.setText("Đang kiểm tra giá và tồn kho mới nhất...");
            var snapshot = cartService.getItems().stream().map(item ->
                    new com.fashionshop.customer.model.CartItem(item.getProduct(), item.getQuantity(),
                            item.getSelectedSize(), item.getSelectedColor())).toList();
            javafx.concurrent.Task<com.fashionshop.customer.service.CheckoutReview.Result> reviewTask = new javafx.concurrent.Task<>() {
                @Override protected com.fashionshop.customer.service.CheckoutReview.Result call() {
                    return com.fashionshop.customer.service.CheckoutReview.review(snapshot,
                            new com.fashionshop.customer.service.ProductService().getAllProducts());
                }
            };
            reviewTask.setOnFailed(e -> {
                messageLabel.setText(reviewTask.getException().getMessage());
                orderButton.setDisable(false);
            });
            reviewTask.setOnSucceeded(e -> {
                if (!stage.isShowing() || !sameCustomer(user.getId())) {
                    messageLabel.setText("Phiên đăng nhập đã thay đổi. Vui lòng mở lại giỏ hàng.");
                    return;
                }
                var review = reviewTask.getValue();
                totalLabel.setText("Tổng thanh toán: " + currency.format(review.total()));
                if (!review.changes().isEmpty()) {
                    Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                    confirm.initOwner(stage);
                    confirm.setHeaderText("Giá hoặc tồn kho đã thay đổi");
                    confirm.setContentText(String.join("\n", review.changes())
                            + "\nTổng mới: " + currency.format(review.total()) + "\nBạn đồng ý đặt hàng theo thông tin mới?");
                    if (confirm.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) {
                        messageLabel.setText("Chưa đặt hàng. Bạn có thể kiểm tra lại giỏ hàng.");
                        orderButton.setDisable(false);
                        return;
                    }
                }
                if (!sameCustomer(user.getId()) || !stage.isShowing()) return;
                cartService.getItems().setAll(review.items());
                messageLabel.setText("Đang tạo đơn hàng...");
                javafx.concurrent.Task<OrderService.OrderResult> submit = new javafx.concurrent.Task<>() {
                    @Override protected OrderService.OrderResult call() throws Exception {
                        return orderService.createOrder(receiverName, phone, address, note, review.items());
                    }
                };
                submit.setOnFailed(error -> {
                    messageLabel.setText(submit.getException().getMessage());
                    orderButton.setDisable(false);
                });
                submit.setOnSucceeded(done -> {
                    if (sameCustomer(user.getId())) cartService.clear();
                    var result = submit.getValue();
                    Alert success = new Alert(Alert.AlertType.INFORMATION);
                    success.setHeaderText("Đã tạo đơn #" + result.getId());
                    success.setContentText("Tổng tiền: " + currency.format(result.getTotalAmount()));
                    success.showAndWait();
                    stage.close();
                });
                startTask(submit);
            });
            startTask(reviewTask);
        });

        Button cancelButton =
                new Button("Hủy");

        cancelButton.setMaxWidth(
                Double.MAX_VALUE
        );

        cancelButton.setOnAction(
                event -> stage.close()
        );

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
                        messageLabel
                );

        root.setPadding(
                new Insets(25)
        );

        Scene scene =
                new Scene(
                        root,
                        500,
                        620
                );

        stage.setTitle(
                "Fashion Shop - Đặt hàng"
        );

        scene.getStylesheets().add(CheckoutWindow.class.getResource("/com/fashionshop/customer/shop.css").toExternalForm());

        stage.setScene(scene);

        stage.showAndWait();
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