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

        if (user == null) {

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
                        + "-fx-text-fill: #d32f2f;"
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
                "-fx-background-color: #d32f2f;"
                        + "-fx-text-fill: white;"
                        + "-fx-font-weight: bold;"
                        + "-fx-background-radius: 6;"
        );

        orderButton.setOnAction(event -> {

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

            orderButton.setDisable(true);

            try {

                OrderService.OrderResult result =
                        orderService.createOrder(
                                receiverName,
                                phone,
                                address,
                                note,
                                cartService.getItems()
                        );

                cartService.clear();

                Alert success =
                        new Alert(
                                Alert.AlertType.INFORMATION
                        );

                success.setTitle(
                        "Đặt hàng thành công"
                );

                success.setHeaderText(
                        "Đơn hàng #"
                                + result.getId()
                );

                success.setContentText(
                        "Trạng thái: "
                                + result.getStatus()
                                + "\nTổng tiền: "
                                + currency.format(
                                result.getTotalAmount()
                        )
                );

                success.showAndWait();

                stage.close();

            } catch (Exception e) {

                messageLabel.setText(
                        e.getMessage()
                );

                orderButton.setDisable(false);
            }
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

        stage.setScene(scene);

        stage.showAndWait();
    }
}