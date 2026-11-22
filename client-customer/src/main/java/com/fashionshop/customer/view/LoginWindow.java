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

        Label brand = new Label("Fashion Shop");
        brand.getStyleClass().add("brand");

        Label titleLabel = new Label("Đăng nhập");
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

        Label emailLabel = new Label("Email");
        emailLabel.setLabelFor(emailField);
        Label passwordLabel = new Label("Mật khẩu");
        passwordLabel.setLabelFor(passwordField);
        VBox email = new VBox(5, emailLabel, emailField);
        VBox password = new VBox(5, passwordLabel, passwordField);
        VBox root = new VBox(14, brand, titleLabel, email, password, loginButton, progress, messageLabel);
        root.setAlignment(Pos.CENTER_LEFT);
        root.setPadding(new Insets(28, 38, 28, 38));
        messageLabel.managedProperty().bind(messageLabel.textProperty().isNotEmpty());

        Scene scene = new Scene(root, 440, 415);

        stage.setTitle(
                "Fashion Shop - Đăng nhập"
        );

        stage.setResizable(
                false
        );

        scene.getStylesheets().add(LoginWindow.class.getResource("/com/fashionshop/customer/shop.css").toExternalForm());

        stage.setScene(
                scene
        );

        stage.showAndWait();

        return loginSuccess;
    }
}