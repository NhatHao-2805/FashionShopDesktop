package com.fashionshop.admin.view;

import com.fashionshop.admin.model.User;
import com.fashionshop.admin.service.AdminService;
import com.fashionshop.admin.service.SessionManager;

import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;

public class AdminDashboardWindow {

    private final Stage stage;

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

    public AdminDashboardWindow(
            Stage stage,
            Runnable logoutAction
    ) {

        this.stage =
                stage;

        this.logoutAction =
                logoutAction;
    }

    public void show() {

        BorderPane root =
                new BorderPane();

        root.setStyle(
                "-fx-background-color: #f3f4f6;"
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

        stage.setTitle(
                "Fashion Shop - Admin Dashboard"
        );

        stage.setMinWidth(
                1100
        );

        stage.setMinHeight(
                700
        );

        stage.setScene(
                scene
        );

        stage.show();

        loadDashboard();
    }

    private VBox createSidebar() {

        Label brand =
                new Label(
                        "FASHION\nSHOP"
                );

        brand.setStyle(
                "-fx-font-size: 24px;"
                        + "-fx-font-weight: bold;"
                        + "-fx-text-fill: white;"
        );

        Label adminText =
                new Label(
                        "ADMIN PANEL"
                );

        adminText.setStyle(
                "-fx-text-fill: #93c5fd;"
                        + "-fx-font-weight: bold;"
        );

        Button dashboardButton =
                menuButton(
                        "Tổng quan"
                );

        dashboardButton.setStyle(
                activeMenuStyle()
        );

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

        Button reportsButton =
                menuButton(
                        "Thống kê"
                );

        ordersButton.setOnAction(
                event ->
                        showComingSoon(
                                "Quản lý đơn hàng"
                        )
        );

        productsButton.setOnAction(
                event ->
                        showComingSoon(
                                "Quản lý sản phẩm"
                        )
        );

        customersButton.setOnAction(
                event ->
                        showComingSoon(
                                "Quản lý khách hàng"
                        )
        );

        inventoryButton.setOnAction(
                event ->
                        showComingSoon(
                                "Quản lý kho"
                        )
        );

        reportsButton.setOnAction(
                event ->
                        showComingSoon(
                                "Báo cáo thống kê"
                        )
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
                        + "-fx-cursor: hand;"
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
                            "Bạn có muốn đăng xuất khỏi hệ thống quản trị?"
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
                        adminText,
                        new Separator(),
                        dashboardButton,
                        ordersButton,
                        productsButton,
                        customersButton,
                        inventoryButton,
                        reportsButton,
                        spacer,
                        logoutButton
                );

        sidebar.setPadding(
                new Insets(
                        25
                )
        );

        sidebar.setPrefWidth(
                230
        );

        sidebar.setStyle(
                "-fx-background-color: #111827;"
        );

        return sidebar;
    }

    private HBox createTopBar() {

        Label title =
                new Label(
                        "Tổng quan hệ thống"
                );

        title.setStyle(
                "-fx-font-size: 22px;"
                        + "-fx-font-weight: bold;"
                        + "-fx-text-fill: #111827;"
        );

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

        String adminName =
                user == null
                        ? "ADMIN"
                        : user.getName();

        Label userLabel =
                new Label(
                        "Quản trị viên: "
                                + adminName
                );

        userLabel.setStyle(
                "-fx-font-weight: bold;"
                        + "-fx-text-fill: #374151;"
        );

        Button refreshButton =
                new Button(
                        "Làm mới"
                );

        refreshButton.setStyle(
                "-fx-background-color: #2563eb;"
                        + "-fx-text-fill: white;"
                        + "-fx-font-weight: bold;"
                        + "-fx-background-radius: 7;"
                        + "-fx-cursor: hand;"
        );

        refreshButton.setOnAction(
                event -> loadDashboard()
        );

        HBox topBar =
                new HBox(
                        15,
                        title,
                        spacer,
                        userLabel,
                        refreshButton
                );

        topBar.setAlignment(
                Pos.CENTER_LEFT
        );

        topBar.setPadding(
                new Insets(
                        18,
                        25,
                        18,
                        25
                )
        );

        topBar.setStyle(
                "-fx-background-color: white;"
                        + "-fx-border-color: transparent transparent #e5e7eb transparent;"
        );

        return topBar;
    }

    private VBox createDashboardContent() {

        Label heading =
                new Label(
                        "Dashboard"
                );

        heading.setStyle(
                "-fx-font-size: 28px;"
                        + "-fx-font-weight: bold;"
                        + "-fx-text-fill: #111827;"
        );

        Label description =
                new Label(
                        "Theo dõi tình hình hoạt động của Fashion Shop."
                );

        description.setStyle(
                "-fx-text-fill: #6b7280;"
        );

        statsGrid.setHgap(
                18
        );

        statsGrid.setVgap(
                18
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
                        8,
                        heading,
                        description,
                        new Separator(),
                        statsGrid,
                        statusBox
                );

        content.setPadding(
                new Insets(
                        25
                )
        );

        ScrollPane scrollPane =
                new ScrollPane(
                        content
                );

        scrollPane.setFitToWidth(
                true
        );

        scrollPane.setStyle(
                "-fx-background: #f3f4f6;"
                        + "-fx-background-color: #f3f4f6;"
        );

        VBox wrapper =
                new VBox(
                        scrollPane
                );

        VBox.setVgrow(
                scrollPane,
                Priority.ALWAYS
        );

        return wrapper;
    }

    private void loadDashboard() {

        progress.setVisible(
                true
        );

        progress.setManaged(
                true
        );

        statusLabel.setText(
                "Đang tải dữ liệu..."
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

                    statsGrid
                            .getChildren()
                            .clear();

                    statusLabel.setText(
                            "Không thể tải dashboard: "
                                    + getErrorMessage(
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
                "Tổng đơn hàng",
                String.valueOf(
                        stats.getTotalOrders()
                ),
                "Toàn bộ đơn hàng"
        );

        addCard(
                1,
                0,
                "Chờ duyệt",
                String.valueOf(
                        stats.getPendingOrders()
                ),
                "Đơn mới cần xử lý"
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
                "Đang vận chuyển",
                String.valueOf(
                        stats.getShippingOrders()
                ),
                "Đơn đang giao"
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
                "Đơn bị hủy"
        );

        addCard(
                0,
                2,
                "Khách hàng",
                String.valueOf(
                        stats.getTotalCustomers()
                ),
                "Tài khoản CUSTOMER"
        );

        addCard(
                1,
                2,
                "Sản phẩm",
                String.valueOf(
                        stats.getTotalProducts()
                ),
                "Sản phẩm trong hệ thống"
        );

        addCard(
                2,
                2,
                "Tổng tồn kho",
                String.valueOf(
                        stats.getTotalStock()
                ),
                "Số lượng sản phẩm còn lại"
        );

        addCard(
                0,
                3,
                "Sắp hết hàng",
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
                createStatCard(
                        title,
                        value,
                        description
                );

        statsGrid.add(
                card,
                column,
                row
        );

        GridPane.setHgrow(
                card,
                Priority.ALWAYS
        );
    }

    private VBox createStatCard(
            String title,
            String value,
            String description
    ) {

        Label titleLabel =
                new Label(
                        title
                );

        titleLabel.setStyle(
                "-fx-text-fill: #6b7280;"
                        + "-fx-font-size: 14px;"
                        + "-fx-font-weight: bold;"
        );

        Label valueLabel =
                new Label(
                        value
                );

        valueLabel.setWrapText(
                true
        );

        valueLabel.setStyle(
                "-fx-text-fill: #111827;"
                        + "-fx-font-size: 26px;"
                        + "-fx-font-weight: bold;"
        );

        Label descriptionLabel =
                new Label(
                        description
                );

        descriptionLabel.setWrapText(
                true
        );

        descriptionLabel.setStyle(
                "-fx-text-fill: #9ca3af;"
                        + "-fx-font-size: 12px;"
        );

        VBox card =
                new VBox(
                        8,
                        titleLabel,
                        valueLabel,
                        descriptionLabel
                );

        card.setPadding(
                new Insets(
                        20
                )
        );

        card.setPrefWidth(
                280
        );

        card.setMinHeight(
                130
        );

        card.setStyle(
                "-fx-background-color: white;"
                        + "-fx-background-radius: 12;"
                        + "-fx-border-color: #e5e7eb;"
                        + "-fx-border-radius: 12;"
        );

        return card;
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
                normalMenuStyle()
        );

        return button;
    }

    private String normalMenuStyle() {

        return "-fx-background-color: transparent;"
                + "-fx-text-fill: #d1d5db;"
                + "-fx-font-size: 14px;"
                + "-fx-font-weight: bold;"
                + "-fx-cursor: hand;"
                + "-fx-background-radius: 8;";
    }

    private String activeMenuStyle() {

        return "-fx-background-color: #2563eb;"
                + "-fx-text-fill: white;"
                + "-fx-font-size: 14px;"
                + "-fx-font-weight: bold;"
                + "-fx-cursor: hand;"
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

    private String getErrorMessage(
            Throwable throwable
    ) {

        if (throwable == null) {

            return "Lỗi không xác định.";
        }

        if (throwable.getMessage() == null
                || throwable.getMessage()
                .isBlank()) {

            return throwable
                    .getClass()
                    .getSimpleName();
        }

        return throwable.getMessage();
    }

    private void showComingSoon(
            String feature
    ) {

        Alert alert =
                new Alert(
                        Alert.AlertType.INFORMATION
                );

        alert.setTitle(
                "Fashion Shop Admin"
        );

        alert.setHeaderText(
                feature
        );

        alert.setContentText(
                "Chức năng này sẽ được hoàn thiện ở batch tiếp theo."
        );

        alert.showAndWait();
    }
}