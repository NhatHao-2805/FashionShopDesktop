package com.fashionshop.customer.view;

import com.fashionshop.customer.service.AdminService;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AdminOrdersWindow {

    private final AdminService adminService =
            new AdminService();

    private final TableView<AdminService.AdminOrder> table =
            new TableView<>();

    private final ComboBox<String> statusFilter =
            new ComboBox<>();

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

    private List<AdminService.AdminOrder> allOrders =
            new ArrayList<>();

    public static void show() {

        new AdminOrdersWindow()
                .open();
    }

    private void open() {

        Stage stage =
                new Stage();

        Label title =
                new Label(
                        "QUẢN LÝ ĐƠN HÀNG"
                );

        title.setStyle(
                "-fx-font-size: 24px;"
                        + "-fx-font-weight: bold;"
        );

        statusFilter.getItems()
                .addAll(
                        "TẤT CẢ",
                        "PENDING",
                        "PROCESSING",
                        "SHIPPING",
                        "DELIVERED",
                        "CANCELLED"
                );

        statusFilter.setValue(
                "TẤT CẢ"
        );

        statusFilter.setOnAction(
                event -> applyFilter()
        );

        Button refresh =
                new Button(
                        "Làm mới"
                );

        refresh.setOnAction(
                event -> loadOrders()
        );

        HBox filters =
                new HBox(
                        10,
                        new Label(
                                "Trạng thái:"
                        ),
                        statusFilter,
                        refresh
                );

        filters.setAlignment(
                Pos.CENTER_LEFT
        );

        configureTable();

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

        HBox bottom =
                new HBox(
                        10,
                        progress,
                        statusLabel
                );

        bottom.setAlignment(
                Pos.CENTER_LEFT
        );

        VBox root =
                new VBox(
                        15,
                        title,
                        filters,
                        table,
                        bottom
                );

        root.setPadding(
                new Insets(20)
        );

        VBox.setVgrow(
                table,
                Priority.ALWAYS
        );

        Scene scene =
                new Scene(
                        root,
                        1250,
                        680
                );

        stage.setTitle(
                "Fashion Shop - Quản lý đơn hàng"
        );

        stage.setScene(
                scene
        );

        stage.show();

        loadOrders();
    }

    private void configureTable() {

        TableColumn<AdminService.AdminOrder, Long> idCol =
                new TableColumn<>(
                        "Mã đơn"
                );

        idCol.setCellValueFactory(
                new PropertyValueFactory<>(
                        "id"
                )
        );

        TableColumn<AdminService.AdminOrder, String> customerCol =
                new TableColumn<>(
                        "Khách hàng"
                );

        customerCol.setCellValueFactory(
                new PropertyValueFactory<>(
                        "customerName"
                )
        );

        TableColumn<AdminService.AdminOrder, String> emailCol =
                new TableColumn<>(
                        "Email"
                );

        emailCol.setCellValueFactory(
                new PropertyValueFactory<>(
                        "customerEmail"
                )
        );

        TableColumn<AdminService.AdminOrder, String> receiverCol =
                new TableColumn<>(
                        "Người nhận"
                );

        receiverCol.setCellValueFactory(
                new PropertyValueFactory<>(
                        "receiverName"
                )
        );

        TableColumn<AdminService.AdminOrder, String> phoneCol =
                new TableColumn<>(
                        "SĐT"
                );

        phoneCol.setCellValueFactory(
                new PropertyValueFactory<>(
                        "phone"
                )
        );

        TableColumn<AdminService.AdminOrder, BigDecimal> totalCol =
                new TableColumn<>(
                        "Tổng tiền"
                );

        totalCol.setCellValueFactory(
                new PropertyValueFactory<>(
                        "totalAmount"
                )
        );

        totalCol.setCellFactory(
                column ->
                        new TableCell<>() {

                            @Override
                            protected void updateItem(
                                    BigDecimal value,
                                    boolean empty
                            ) {

                                super.updateItem(
                                        value,
                                        empty
                                );

                                setText(
                                        empty
                                                || value == null
                                                ? null
                                                : currency.format(
                                                        value
                                                )
                                );
                            }
                        }
        );

        TableColumn<AdminService.AdminOrder, String> statusCol =
                new TableColumn<>(
                        "Trạng thái"
                );

        statusCol.setCellValueFactory(
                new PropertyValueFactory<>(
                        "status"
                )
        );

        statusCol.setCellFactory(
                column ->
                        new TableCell<>() {

                            @Override
                            protected void updateItem(
                                    String value,
                                    boolean empty
                            ) {

                                super.updateItem(
                                        value,
                                        empty
                                );

                                if (empty
                                        || value == null) {

                                    setText(null);

                                } else {

                                    setText(
                                            statusText(
                                                    value
                                            )
                                    );

                                    setStyle(
                                            "-fx-font-weight: bold;"
                                    );
                                }
                            }
                        }
        );

        TableColumn<AdminService.AdminOrder, Void> actionCol =
                new TableColumn<>(
                        "Xử lý"
                );

        actionCol.setPrefWidth(
                160
        );

        actionCol.setCellFactory(
                column ->
                        new TableCell<>() {

                            private final Button actionButton =
                                    new Button();

                            {
                                actionButton.setStyle(
                                        "-fx-background-color: #2563eb;"
                                                + "-fx-text-fill: white;"
                                                + "-fx-font-weight: bold;"
                                );

                                actionButton.setOnAction(
                                        event -> {

                                            AdminService.AdminOrder order =
                                                    getTableView()
                                                            .getItems()
                                                            .get(
                                                                    getIndex()
                                                            );

                                            advanceOrder(
                                                    order
                                            );
                                        }
                                );
                            }

                            @Override
                            protected void updateItem(
                                    Void item,
                                    boolean empty
                            ) {

                                super.updateItem(
                                        item,
                                        empty
                                );

                                if (empty) {

                                    setGraphic(
                                            null
                                    );

                                    return;
                                }

                                AdminService.AdminOrder order =
                                        getTableView()
                                                .getItems()
                                                .get(
                                                        getIndex()
                                                );

                                String next =
                                        nextStatus(
                                                order.getStatus()
                                        );

                                if (next == null) {

                                    setGraphic(
                                            null
                                    );

                                } else {

                                    actionButton.setText(
                                            actionText(
                                                    order.getStatus()
                                            )
                                    );

                                    setGraphic(
                                            actionButton
                                    );
                                }
                            }
                        }
        );

        table.getColumns()
                .setAll(
                        idCol,
                        customerCol,
                        emailCol,
                        receiverCol,
                        phoneCol,
                        totalCol,
                        statusCol,
                        actionCol
                );

        table.setColumnResizePolicy(
                TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN
        );

        table.setPlaceholder(
                new Label(
                        "Chưa có đơn hàng."
                )
        );
    }

    private void loadOrders() {

        setLoading(
                true,
                "Đang tải danh sách đơn hàng..."
        );

        Task<List<AdminService.AdminOrder>> task =
                new Task<>() {

                    @Override
                    protected List<AdminService.AdminOrder> call()
                            throws Exception {

                        return adminService
                                .getOrders();
                    }
                };

        task.setOnSucceeded(
                event -> {

                    allOrders =
                            new ArrayList<>(
                                    task.getValue()
                            );

                    applyFilter();

                    setLoading(
                            false,
                            "Đã tải "
                                    + allOrders.size()
                                    + " đơn hàng."
                    );
                }
        );

        task.setOnFailed(
                event -> {

                    table.getItems()
                            .clear();

                    setLoading(
                            false,
                            "Không thể tải đơn hàng: "
                                    + errorMessage(
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

    private void applyFilter() {

        String selected =
                statusFilter.getValue();

        List<AdminService.AdminOrder> result =
                allOrders.stream()
                        .filter(
                                order ->
                                        selected == null
                                                || "TẤT CẢ".equals(
                                                selected
                                        )
                                                || selected.equalsIgnoreCase(
                                                order.getStatus()
                                        )
                        )
                        .toList();

        table.setItems(
                FXCollections.observableArrayList(
                        result
                )
        );
    }

    private void advanceOrder(
            AdminService.AdminOrder order
    ) {

        String next =
                nextStatus(
                        order.getStatus()
                );

        if (next == null) {
            return;
        }

        Alert confirm =
                new Alert(
                        Alert.AlertType.CONFIRMATION
                );

        confirm.setTitle(
                "Cập nhật đơn hàng"
        );

        confirm.setHeaderText(
                "Đơn hàng #"
                        + order.getId()
        );

        confirm.setContentText(
                "Chuyển trạng thái từ "
                        + statusText(
                        order.getStatus()
                )
                        + " sang "
                        + statusText(
                        next
                )
                        + "?"
        );

        confirm.showAndWait()
                .ifPresent(
                        button -> {

                            if (button
                                    == ButtonType.OK) {

                                updateStatus(
                                        order.getId(),
                                        next
                                );
                            }
                        }
                );
    }

    private void updateStatus(
            long orderId,
            String nextStatus
    ) {

        setLoading(
                true,
                "Đang cập nhật đơn hàng #"
                        + orderId
                        + "..."
        );

        Task<AdminService.AdminOrder> task =
                new Task<>() {

                    @Override
                    protected AdminService.AdminOrder call()
                            throws Exception {

                        return adminService
                                .updateOrderStatus(
                                        orderId,
                                        nextStatus
                                );
                    }
                };

        task.setOnSucceeded(
                event -> {

                    setLoading(
                            false,
                            "Cập nhật đơn hàng #"
                                    + orderId
                                    + " thành công."
                    );

                    loadOrders();
                }
        );

        task.setOnFailed(
                event -> {

                    setLoading(
                            false,
                            "Không thể cập nhật: "
                                    + errorMessage(
                                    task.getException()
                            )
                    );

                    Alert alert =
                            new Alert(
                                    Alert.AlertType.ERROR
                            );

                    alert.setTitle(
                            "Lỗi cập nhật"
                    );

                    alert.setHeaderText(
                            null
                    );

                    alert.setContentText(
                            errorMessage(
                                    task.getException()
                            )
                    );

                    alert.showAndWait();
                }
        );

        Thread thread =
                new Thread(task);

        thread.setDaemon(
                true
        );

        thread.start();
    }

    private String nextStatus(
            String status
    ) {

        if (status == null) {
            return null;
        }

        return switch (
                status.toUpperCase()
        ) {

            case "PENDING" ->
                    "PROCESSING";

            case "PROCESSING" ->
                    "SHIPPING";

            case "SHIPPING" ->
                    "DELIVERED";

            default ->
                    null;
        };
    }

    private String actionText(
            String status
    ) {

        if (status == null) {
            return "";
        }

        return switch (
                status.toUpperCase()
        ) {

            case "PENDING" ->
                    "Duyệt đơn";

            case "PROCESSING" ->
                    "Giao vận chuyển";

            case "SHIPPING" ->
                    "Xác nhận đã giao";

            default ->
                    "";
        };
    }

    private String statusText(
            String status
    ) {

        if (status == null) {
            return "-";
        }

        return switch (
                status.toUpperCase()
        ) {

            case "PENDING" ->
                    "Chờ duyệt";

            case "PROCESSING" ->
                    "Đang xử lý";

            case "SHIPPING" ->
                    "Đang vận chuyển";

            case "DELIVERED" ->
                    "Đã giao";

            case "CANCELLED" ->
                    "Đã hủy";

            default ->
                    status;
        };
    }

    private void setLoading(
            boolean loading,
            String message
    ) {

        Platform.runLater(
                () -> {

                    progress.setVisible(
                            loading
                    );

                    progress.setManaged(
                            loading
                    );

                    statusLabel.setText(
                            message
                    );
                }
        );
    }

    private String errorMessage(
            Throwable throwable
    ) {

        if (throwable == null) {

            return "Lỗi không xác định.";
        }

        return throwable.getMessage()
                == null
                ? throwable
                .getClass()
                .getSimpleName()
                : throwable.getMessage();
    }
}