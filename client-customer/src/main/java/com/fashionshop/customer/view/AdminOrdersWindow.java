package com.fashionshop.customer.view;

import com.fashionshop.customer.service.AdminService;

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

    private final OperationStatus status = new OperationStatus();
    private boolean updatingOrder;
    private boolean ordersLoaded;

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

        refresh.disableProperty().bind(status.busyProperty());
        statusFilter.disableProperty().bind(status.busyProperty());
        table.disableProperty().bind(status.busyProperty());
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

        VBox root =
                new VBox(
                        15,
                        filters,
                        status,
                        table
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

        PageNavigation.show("Quản lý đơn hàng", scene);
        PageNavigation.guardBack(scene, () -> !updatingOrder);

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
                                        "-fx-background-color: #294b61;"
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

        table.setPlaceholder(new Label());
    }

    private void loadOrders() {
        loadOrders(null);
    }

    private void loadOrders(String updatedOrder) {
        if (status.isBusy()) return;
        var position = TablePosition.capture(table, AdminService.AdminOrder::getId);
        ordersLoaded = false;
        status.loading("Đang tải danh sách đơn hàng…");

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

                    ordersLoaded = true;
                    status.info("");
                    applyFilter();
                    position.restore(table, AdminService.AdminOrder::getId);
                    if (updatedOrder != null) status.success(updatedOrder);
                }
        );

        task.setOnFailed(event -> {
            allOrders.clear();
            table.getItems().clear();
            String prefix = updatedOrder == null ? "" : updatedOrder + " ";
            status.error(prefix + "Không tải được danh sách đơn hàng: "
                    + errorMessage(task.getException()), () -> loadOrders(updatedOrder));
        });

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
        if (ordersLoaded && !status.isBusy()) {
            if (result.isEmpty()) {
                status.empty(allOrders.isEmpty() ? "Chưa có đơn hàng trong cửa hàng."
                        : "Không có đơn hàng ở trạng thái đã chọn.");
            } else {
                status.info("Đang hiển thị " + result.size() + " đơn hàng.");
            }
        }
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

        if (status.isBusy()) return;
        updatingOrder = true;
        status.loading("Đang cập nhật đơn hàng #" + orderId + "… Vui lòng chờ xác nhận.");

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

        task.setOnSucceeded(event -> {
            updatingOrder = false;
            status.info("");
            loadOrders("Đã cập nhật đơn hàng #" + orderId + ".");
        });

        task.setOnFailed(event -> {
            updatingOrder = false;
            ordersLoaded = false;
            allOrders.clear();
            table.getItems().clear();
            status.error("Không xác nhận được cập nhật đơn #" + orderId
                    + ". Hãy tải lại danh sách để kiểm tra trạng thái. "
                    + errorMessage(task.getException()), this::loadOrders);
        });

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
