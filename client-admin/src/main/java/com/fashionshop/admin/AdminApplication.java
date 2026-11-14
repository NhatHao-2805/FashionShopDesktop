package com.fashionshop.admin;

import com.fashionshop.admin.service.SessionManager;
import com.fashionshop.admin.view.AdminDashboardWindow;
import com.fashionshop.admin.view.AdminLoginWindow;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;

public class AdminApplication extends Application {

    @Override
    public void start(
            Stage primaryStage
    ) {

        showLogin(
                primaryStage
        );
    }

    private void showLogin(
            Stage stage
    ) {

        AdminLoginWindow loginWindow =
                new AdminLoginWindow();

        boolean success =
                loginWindow.showAndWait();

        if (!success) {

            Platform.exit();

            return;
        }

        if (!SessionManager
                .getInstance()
                .isAdminLoggedIn()) {

            Platform.exit();

            return;
        }

        AdminDashboardWindow dashboard =
                new AdminDashboardWindow(
                        stage,
                        () -> {

                            SessionManager
                                    .getInstance()
                                    .logout();

                            stage.close();

                            showLogin(
                                    new Stage()
                            );
                        }
                );

        dashboard.show();
    }

    public static void main(
            String[] args
    ) {

        launch(args);
    }
}