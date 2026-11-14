package com.fashionshop.customer.view;

import com.fashionshop.customer.model.User;
import com.fashionshop.customer.service.AuthService;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class LoginWindow {

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

        Label brand =
                new Label(
                        "FASHION SHOP"
                );

        brand.setStyle(
                "-fx-font-size: 28px;"
                        + "-fx-font-weight: bold;"
                        + "-fx-text-fill: #111827;"
        );

        Label titleLabel =
                new Label(
                        "ĐĂNG NHẬP"
                );

        titleLabel.setStyle(
                "-fx-font-size: 20px;"
                        + "-fx-font-weight: bold;"
                        + "-fx-text-fill: #2563eb;"
        );

        Label description =
                new Label(
                        "Khách hàng và quản trị viên sử dụng chung một màn hình đăng nhập."
                );

        description.setWrapText(
                true
        );

        description.setStyle(
                "-fx-text-fill: #6b7280;"
        );

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

        loginButton.setStyle(
                "-fx-background-color: #2563eb;"
                        + "-fx-text-fill: white;"
                        + "-fx-font-weight: bold;"
                        + "-fx-background-radius: 8;"
        );

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
                                                    authService.login(
                                                            email,
                                                            password
                                                    );

                                            Platform.runLater(
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

        VBox root =
                new VBox(
                        14,
                        brand,
                        titleLabel,
                        description,
                        emailField,
                        passwordField,
                        loginButton,
                        progress,
                        messageLabel
                );

        root.setAlignment(
                Pos.CENTER
        );

        root.setPadding(
                new Insets(35)
        );

        root.setStyle(
                "-fx-background-color: white;"
        );

        Scene scene =
                new Scene(
                        root,
                        500,
                        430
                );

        stage.setTitle(
                "Fashion Shop - Đăng nhập"
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