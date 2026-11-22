package com.fashionshop.admin;

import com.fashionshop.customer.service.SessionManager;
import com.fashionshop.customer.view.AdminDashboardWindow;
import com.fashionshop.customer.view.LoginWindow;
import com.fashionshop.customer.view.PageNavigation;
import javafx.application.Application;
import javafx.stage.Stage;

public class AdminApplication extends Application {

    @Override
    public void start(Stage primaryStage) {
        PageNavigation.initialize(primaryStage);
        primaryStage.setMinWidth(1000);
        primaryStage.setMinHeight(680);
        primaryStage.setWidth(1320);
        primaryStage.setHeight(860);
        showLogin();
    }

    private void showLogin() {
        new LoginWindow(true).show(() -> new AdminDashboardWindow(() -> {
            SessionManager.getInstance().logout();
            showLogin();
        }).show());
    }

    public static void main(String[] args) {
        launch(args);
    }
}
