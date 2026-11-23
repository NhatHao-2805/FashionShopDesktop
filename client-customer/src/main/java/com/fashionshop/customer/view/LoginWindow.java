package com.fashionshop.customer.view;

import com.fashionshop.customer.model.User;
import com.fashionshop.customer.service.AuthService;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class LoginWindow {

    private final AuthService authService =
            new AuthService();

    private final boolean adminOnly;

    public LoginWindow() {
        this(false);
    }

    public LoginWindow(boolean adminOnly) {
        this.adminOnly = adminOnly;
    }

    public void show(Runnable onSuccess) {

        Label brand = new Label("Fashion Shop");
        brand.getStyleClass().add("brand");

        Label titleLabel = new Label(adminOnly ? "Đăng nhập quản trị" : "Đăng nhập");
        titleLabel.getStyleClass().add("section-title");

        TextField emailField =
                new TextField();

        emailField.setPromptText(
                "Email"
        );

        emailField.setPrefHeight(
                42
        );

        PasswordField passwordField =
                new PasswordField();

        passwordField.setPromptText(
                "Mật khẩu"
        );

        passwordField.setPrefHeight(
                42
        );

        Label messageLabel =
                new Label();

        messageLabel.setWrapText(
                true
        );

        messageLabel.setStyle(
                "-fx-text-fill: #dc2626;"
        );

        ProgressIndicator progress =
                new ProgressIndicator();

        progress.setPrefSize(
                24,
                24
        );

        progress.setVisible(
                false
        );

        progress.setManaged(
                false
        );

        Button loginButton =
                new Button(
                        "Đăng nhập"
                );

        loginButton.setMaxWidth(
                Double.MAX_VALUE
        );

        loginButton.setPrefHeight(
                44
        );

        loginButton.getStyleClass().add("primary-button");

        Button registerButton = new Button("Tạo tài khoản khách hàng");
        registerButton.setMaxWidth(Double.MAX_VALUE);
        registerButton.setVisible(!adminOnly);
        registerButton.setManaged(!adminOnly);
        registerButton.setOnAction(event -> {
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setTitle("Đăng ký tài khoản");
            TextField name = new TextField(); name.setPromptText("Họ tên");
            TextField phone = new TextField(); phone.setPromptText("Số điện thoại (không bắt buộc)");
            PasswordField confirm = new PasswordField(); confirm.setPromptText("Nhập lại mật khẩu");
            VBox form = new VBox(9, new Label("Họ tên"), name, new Label("Số điện thoại"), phone,
                    new Label("Email"), new TextField(emailField.getText()), new Label("Mật khẩu"), new PasswordField());
            TextField emailRegister = (TextField) form.getChildren().get(5);
            PasswordField password = (PasswordField) form.getChildren().get(7);
            form.getChildren().addAll(new Label("Xác nhận mật khẩu"), confirm);
            dialog.getDialogPane().setContent(form); dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
            dialog.setResultConverter(button -> {
                if (button != ButtonType.OK) return button;
                try {
                    if (!password.getText().equals(confirm.getText())) throw new IllegalArgumentException("Mật khẩu xác nhận không khớp.");
                    authService.register(name.getText(), emailRegister.getText(), password.getText(), phone.getText());
                    messageLabel.setStyle("-fx-text-fill: #16a34a;"); messageLabel.setText("Đăng ký thành công, hãy đăng nhập.");
                } catch (Exception error) { messageLabel.setStyle("-fx-text-fill: #dc2626;"); messageLabel.setText(error.getMessage()); }
                return button;
            });
            dialog.showAndWait();
        });

        Runnable loginAction =
                () -> {

                    String email =
                            emailField
                                    .getText()
                                    .trim();

                    String password =
                            passwordField
                                    .getText();

                    if (email.isBlank()
                            || password.isBlank()) {

                        messageLabel.setText(
                                "Vui lòng nhập email và mật khẩu."
                        );

                        return;
                    }

                    loginButton.setDisable(
                            true
                    );

                    emailField.setDisable(
                            true
                    );

                    passwordField.setDisable(
                            true
                    );

                    progress.setVisible(
                            true
                    );

                    progress.setManaged(
                            true
                    );

                    messageLabel.setStyle(
                            "-fx-text-fill: #6b7280;"
                    );

                    messageLabel.setText(
                            "Đang đăng nhập..."
                    );

                    Thread thread =
                            new Thread(
                                    () -> {

                                        try {

                                            User user =
                                                    adminOnly
                                                            ? authService.loginAdmin(email, password)
                                                            : authService.login(email, password);

                                            Platform.runLater(
                                                    () -> {

                                                        messageLabel.setStyle(
                                                                "-fx-text-fill: #16a34a;"
                                                        );

                                                        messageLabel.setText(
                                                                "Xin chào "
                                                                        + user.getName()
                                                        );

                                                        loginButton.setDisable(false);
                                                        PageNavigation.back(loginButton.getScene());
                                                        onSuccess.run();
                                                    }
                                            );

                                        } catch (Exception e) {

                                            Platform.runLater(
                                                    () -> {

                                                        loginButton.setDisable(
                                                                false
                                                        );

                                                        emailField.setDisable(
                                                                false
                                                        );

                                                        passwordField.setDisable(
                                                                false
                                                        );

                                                        progress.setVisible(
                                                                false
                                                        );

                                                        progress.setManaged(
                                                                false
                                                        );

                                                        messageLabel.setStyle(
                                                                "-fx-text-fill: #dc2626;"
                                                        );

                                                        messageLabel.setText(
                                                                e.getMessage()
                                                        );
                                                    }
                                            );
                                        }
                                    }
                            );

                    thread.setDaemon(
                            true
                    );

                    thread.start();
                };

        loginButton.setOnAction(
                event -> loginAction.run()
        );

        passwordField.setOnAction(
                event -> loginAction.run()
        );

        Label emailLabel = new Label("Email");
        emailLabel.setLabelFor(emailField);
        Label passwordLabel = new Label("Mật khẩu");
        passwordLabel.setLabelFor(passwordField);
        VBox email = new VBox(5, emailLabel, emailField);
        VBox password = new VBox(5, passwordLabel, passwordField);
        VBox root = new VBox(14, brand, titleLabel, email, password, loginButton, registerButton, progress, messageLabel);
        root.setAlignment(Pos.CENTER_LEFT);
        root.setPadding(new Insets(28, 38, 28, 38));
        messageLabel.managedProperty().bind(messageLabel.textProperty().isNotEmpty());

        root.setMaxSize(440, Region.USE_PREF_SIZE);
        Scene scene = new Scene(new StackPane(root), 1000, 720);
        scene.getStylesheets().add(LoginWindow.class.getResource("/com/fashionshop/customer/shop.css").toExternalForm());
        if (adminOnly) {
            PageNavigation.reset("Đăng nhập quản trị", scene);
        } else {
            PageNavigation.show("Đăng nhập", scene);
        }
        PageNavigation.guardBack(scene, () -> !loginButton.isDisabled());
    }
}
