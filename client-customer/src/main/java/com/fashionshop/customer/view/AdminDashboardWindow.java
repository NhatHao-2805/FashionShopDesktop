package com.fashionshop.customer.view;

import com.fashionshop.customer.model.User;
import com.fashionshop.customer.service.AdminService;
import com.fashionshop.customer.service.SessionManager;

import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;

public class AdminDashboardWindow {

    private final Runnable logoutAction;

    private final AdminService adminService =
            new AdminService();

    private final GridPane statsGrid =
            new GridPane();

    private final Label statusLabel =
            new Label();

    private final ProgressIndicator progress =
            new ProgressIndicator();

    private final NumberFormat currency =
            NumberFormat.getCurrencyInstance(
                    new Locale(
                            "vi",
                            "VN"
                    )
            );

    public AdminDashboardWindow(Runnable logoutAction) {

        this.logoutAction =
                logoutAction;
    }

    public void show() {

        BorderPane root =
                new BorderPane();

        root.setStyle(
                "-fx-background-color: #dce6ee;"
        );

        root.setLeft(
                createSidebar()
        );

        root.setTop(
                createTopBar()
        );

        root.setCenter(
                createDashboardContent()
        );

        Scene scene =
                new Scene(
                        root,
                        1400,
                        820
                );

        PageNavigation.reset("Quản trị Fashion Shop", scene);

        loadDashboard();
    }

    private VBox createSidebar() {

        Label brand =
                new Label(
                        "Fashion Shop"
                );

        brand.setStyle(
                "-fx-font-size: 24px;"
                        + "-fx-font-weight: bold;"
                        + "-fx-text-fill: white;"
        );

        Label role =
                new Label(
                        "Quản trị cửa hàng"
                );

        role.setStyle(
                "-fx-text-fill: #d6e4ee;"
                        + "-fx-font-weight: bold;"
        );

        Button dashboardButton =
                menuButton(
                        "Tổng quan"
                );

        dashboardButton.setStyle(
                activeStyle()
        );
        dashboardButton.setOnAction(event -> loadDashboard());

        Button ordersButton =
                menuButton(
                        "Đơn hàng"
                );

        Button productsButton =
                menuButton(
                        "Sản phẩm"
                );

        Button customersButton =
                menuButton(
                        "Khách hàng"
                );

        Button inventoryButton =
                menuButton(
                        "Kho hàng"
                );
        Button chatButton =
                menuButton(
                        "💬 Tin nhắn"
                );


        ordersButton.setOnAction(
                event -> AdminOrdersWindow.show()
        );

        productsButton.setOnAction(
                event -> AdminProductsWindow.show()
        );

        customersButton.setOnAction(
                event -> AdminCustomersWindow.show()
        );

        inventoryButton.setOnAction(
                event -> AdminInventoryWindow.show()
        );
        chatButton.setOnAction(
                event -> AdminChatWindow.show()
        );


        Region spacer =
                new Region();

        VBox.setVgrow(
                spacer,
                Priority.ALWAYS
        );

        Button logoutButton =
                menuButton(
                        "Đăng xuất"
                );

        logoutButton.setStyle(
                "-fx-background-color: #dc2626;"
                        + "-fx-text-fill: white;"
                        + "-fx-font-weight: bold;"
                        + "-fx-background-radius: 8;"
        );

        logoutButton.setOnAction(
                event -> {

                    Alert confirm =
                            new Alert(
                                    Alert.AlertType.CONFIRMATION
                            );

                    confirm.setTitle(
                            "Đăng xuất"
                    );

                    confirm.setHeaderText(
                            "Đăng xuất khỏi tài khoản quản trị?"
                    );

                    confirm.showAndWait()
                            .ifPresent(
                                    button -> {

                                        if (button
                                                == ButtonType.OK) {

                                            logoutAction.run();
                                        }
                                    }
                            );
                }
        );

        VBox sidebar =
                new VBox(
                        12,
                        brand,
                        role,
                        new Separator(),
                        dashboardButton,
                        ordersButton,
                        productsButton,
                        customersButton,
                        inventoryButton,
                        chatButton,
                        spacer,
                        logoutButton
                );

        sidebar.setPrefWidth(
                230
        );

        sidebar.setPadding(
                new Insets(25)
        );

        sidebar.setStyle(
                "-fx-background-color: #314a5d;"
        );

        return sidebar;
    }

    private HBox createTopBar() {

        Label title =
                new Label(
                        "Tổng quan hệ thống"
                );

        title.getStyleClass().add("admin-page-title");

        Region spacer =
                new Region();

        HBox.setHgrow(
                spacer,
                Priority.ALWAYS
        );

        User user =
                SessionManager
                        .getInstance()
                        .getCurrentUser();

        Label admin =
                new Label(
                        "Quản trị viên: "
                                + (
                                user == null
                                        ? "ADMIN"
                                        : user.getName()
                        )
                );

        admin.setStyle(
                "-fx-font-weight: bold;"
        );

        Button refresh =
                new Button(
                        "Làm mới"
                );

        refresh.setStyle(
                "-fx-background-color: #294b61;"
                        + "-fx-text-fill: white;"
                        + "-fx-font-weight: bold;"
        );

        refresh.setOnAction(
                event -> loadDashboard()
        );

        HBox bar =
                new HBox(
                        12,
                        title,
                        spacer,
                        admin,
                        refresh
                );

        bar.setAlignment(
                Pos.CENTER_LEFT
        );

        bar.setPadding(
                new Insets(18)
        );

        bar.setStyle(
                "-fx-background-color: #edf3f7;"
        );

        return bar;
    }

    private VBox createDashboardContent() {

        statsGrid.setHgap(
                16
        );

        statsGrid.setVgap(
                16
        );

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

        HBox statusBox =
                new HBox(
                        10,
                        progress,
                        statusLabel
                );

        statusBox.setAlignment(
                Pos.CENTER_LEFT
        );

        VBox content =
                new VBox(
                        10,
                        statsGrid,
                        statusBox
                );

        content.setPadding(
                new Insets(25)
        );

        return content;
    }

    private void loadDashboard() {

        progress.setVisible(
                true
        );

        progress.setManaged(
                true
        );

        statusLabel.setText(
                "Đang tải dashboard..."
        );

        Task<AdminService.DashboardStats> task =
                new Task<>() {

                    @Override
                    protected AdminService.DashboardStats call()
                            throws Exception {

                        return adminService
                                .getDashboardStats();
                    }
                };

        task.setOnSucceeded(
                event -> {

                    progress.setVisible(
                            false
                    );

                    progress.setManaged(
                            false
                    );

                    displayStats(
                            task.getValue()
                    );

                    statusLabel.setText(
                            "Dữ liệu đã được cập nhật."
                    );
                }
        );

        task.setOnFailed(
                event -> {

                    progress.setVisible(
                            false
                    );

                    progress.setManaged(
                            false
                    );

                    statusLabel.setText(
                            "Không thể tải dashboard: "
                                    + getMessage(
                                    task.getException()
                            )
                    );
                }
        );

        Thread thread =
                new Thread(task);

        thread.setDaemon(
                true
        );

        thread.start();
    }

    private void displayStats(
            AdminService.DashboardStats stats
    ) {

        statsGrid
                .getChildren()
                .clear();

        addCard(
                0,
                0,
                "Tổng đơn",
                String.valueOf(
                        stats.getTotalOrders()
                ),
                "Tất cả đơn hàng"
        );

        addCard(
                1,
                0,
                "Chờ duyệt",
                String.valueOf(
                        stats.getPendingOrders()
                ),
                "Đơn mới"
        );

        addCard(
                2,
                0,
                "Đang xử lý",
                String.valueOf(
                        stats.getProcessingOrders()
                ),
                "Shop đang chuẩn bị"
        );

        addCard(
                0,
                1,
                "Đang giao",
                String.valueOf(
                        stats.getShippingOrders()
                ),
                "Đơn đang vận chuyển"
        );

        addCard(
                1,
                1,
                "Đã giao",
                String.valueOf(
                        stats.getDeliveredOrders()
                ),
                "Đơn hoàn tất"
        );

        addCard(
                2,
                1,
                "Đã hủy",
                String.valueOf(
                        stats.getCancelledOrders()
                ),
                "Đơn đã hủy"
        );

        addCard(
                0,
                2,
                "Khách hàng",
                String.valueOf(
                        stats.getTotalCustomers()
                ),
                "Tài khoản khách"
        );

        addCard(
                1,
                2,
                "Sản phẩm",
                String.valueOf(
                        stats.getTotalProducts()
                ),
                "Sản phẩm hệ thống"
        );

        addCard(
                2,
                2,
                "Tồn kho",
                String.valueOf(
                        stats.getTotalStock()
                ),
                "Tổng số lượng còn lại"
        );

        addCard(
                0,
                3,
                "Sắp hết",
                String.valueOf(
                        stats.getLowStockProducts()
                ),
                "Sản phẩm tồn ≤ 5"
        );

        addCard(
                1,
                3,
                "Doanh thu",
                formatMoney(
                        stats.getDeliveredRevenue()
                ),
                "Chỉ tính đơn đã giao"
        );
    }

    private void addCard(
            int column,
            int row,
            String title,
            String value,
            String description
    ) {

        VBox card =
                new VBox(
                        8
                );

        Label titleLabel =
                new Label(
                        title
                );

        titleLabel.setStyle(
                "-fx-text-fill: #41596c;"
                        + "-fx-font-weight: bold;"
        );

        Label valueLabel =
                new Label(
                        value
                );

        valueLabel.setStyle(
                "-fx-font-size: 25px;"
                        + "-fx-font-weight: bold;"
        );

        Label descriptionLabel =
                new Label(
                        description
                );

        descriptionLabel.setStyle(
                "-fx-text-fill: #9ca3af;"
        );

        card.getChildren()
                .addAll(
                        titleLabel,
                        valueLabel,
                        descriptionLabel
                );

        card.setPadding(
                new Insets(18)
        );

        card.setPrefWidth(
                280
        );

        card.setStyle(
                "-fx-background-color: #edf3f7;"
                        + "-fx-background-radius: 12;"
                        + "-fx-border-color: #adbfce;"
                        + "-fx-border-radius: 12;"
        );

        statsGrid.add(
                card,
                column,
                row
        );
    }

    private Button menuButton(
            String text
    ) {

        Button button =
                new Button(
                        text
                );

        button.setMaxWidth(
                Double.MAX_VALUE
        );

        button.setAlignment(
                Pos.CENTER_LEFT
        );

        button.setPrefHeight(
                42
        );

        button.setStyle(
                "-fx-background-color: transparent;"
                        + "-fx-text-fill: #e9f2f8;"
                        + "-fx-font-weight: bold;"
        );

        return button;
    }

    private String activeStyle() {

        return "-fx-background-color: #294b61;"
                + "-fx-text-fill: white;"
                + "-fx-font-weight: bold;"
                + "-fx-background-radius: 8;";
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

    private String getMessage(
            Throwable throwable
    ) {

        if (throwable == null) {

            return "Lỗi không xác định.";
        }

        if (throwable.getMessage()
                == null
                || throwable
                .getMessage()
                .isBlank()) {

            return throwable
                    .getClass()
                    .getSimpleName();
        }

        return throwable.getMessage();
    }

}
