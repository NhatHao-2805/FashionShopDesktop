package com.fashionshop.admin.view;

import com.fashionshop.admin.model.User;
import com.fashionshop.admin.service.AuthService;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class AdminLoginWindow {

    private final AuthService authService =
            new AuthService();

    private boolean loginSuccess =
            false;

    public boolean showAndWait() {

        Stage stage =
                new Stage();

        stage.initModality(
                Modality.APPLICATION_MODAL
        );

        Label logo =
                new Label(
                        "FASHION SHOP"
                );

        logo.setStyle(
                "-fx-font-size: 30px;"
                        + "-fx-font-weight: bold;"
                        + "-fx-text-fill: #111827;"
        );

        Label subtitle =
                new Label(
                        "Hệ thống quản trị cửa hàng"
                );

        subtitle.setStyle(
                "-fx-font-size: 14px;"
                        + "-fx-text-fill: #6b7280;"
        );

        Label adminLabel =
                new Label(
                        "ADMIN LOGIN"
                );

        adminLabel.setStyle(
                "-fx-font-size: 18px;"
                        + "-fx-font-weight: bold;"
                        + "-fx-text-fill: #2563eb;"
        );

        TextField emailField =
                new TextField();

        emailField.setPromptText(
                "Email quản trị"
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
                22,
                22
        );

        progress.setVisible(
                false
        );

        progress.setManaged(
                false
        );

        Button loginButton =
                new Button(
                        "ĐĂNG NHẬP QUẢN TRỊ"
                );

        loginButton.setMaxWidth(
                Double.MAX_VALUE
        );

        loginButton.setPrefHeight(
                44
        );

        loginButton.setStyle(
                "-fx-background-color: #2563eb;"
                        + "-fx-text-fill: white;"
                        + "-fx-font-weight: bold;"
                        + "-fx-background-radius: 8;"
                        + "-fx-cursor: hand;"
        );

        Runnable doLogin =
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

                    messageLabel.setText(
                            "Đang xác thực..."
                    );

                    Thread thread =
                            new Thread(
                                    () -> {

                                        try {

                                            User user =
                                                    authService.login(
                                                            email,
                                                            password
                                                    );

                                            javafx.application.Platform
                                                    .runLater(
                                                            () -> {

                                                                loginSuccess =
                                                                        true;

                                                                messageLabel.setStyle(
                                                                        "-fx-text-fill: #16a34a;"
                                                                );

                                                                messageLabel.setText(
                                                                        "Xin chào "
                                                                                + user.getName()
                                                                );

                                                                stage.close();
                                                            }
                                                    );

                                        } catch (Exception e) {

                                            javafx.application.Platform
                                                    .runLater(
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
                event -> doLogin.run()
        );

        passwordField.setOnAction(
                event -> doLogin.run()
        );

        VBox form =
                new VBox(
                        15,
                        adminLabel,
                        emailField,
                        passwordField,
                        loginButton,
                        progress,
                        messageLabel
                );

        form.setPadding(
                new Insets(30)
        );

        form.setMaxWidth(
                420
        );

        form.setStyle(
                "-fx-background-color: white;"
                        + "-fx-background-radius: 14;"
                        + "-fx-border-color: #e5e7eb;"
                        + "-fx-border-radius: 14;"
        );

        VBox header =
                new VBox(
                        5,
                        logo,
                        subtitle
                );

        header.setAlignment(
                Pos.CENTER
        );

        VBox root =
                new VBox(
                        25,
                        header,
                        form
                );

        root.setAlignment(
                Pos.CENTER
        );

        root.setPadding(
                new Insets(40)
        );

        root.setStyle(
                "-fx-background-color: #f3f4f6;"
        );

        Scene scene =
                new Scene(
                        root,
                        650,
                        560
                );

        stage.setTitle(
                "Fashion Shop - Admin Login"
        );

        stage.setResizable(
                false
        );

        stage.setScene(
                scene
        );

        stage.showAndWait();

        return loginSuccess;
    }
}