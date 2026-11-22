package com.fashionshop.customer.view;

import com.fashionshop.customer.service.AdminService;

import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.List;

public class AdminCustomersWindow {

    private final AdminService adminService =
            new AdminService();

    private final TableView<AdminService.Customer> table =
            new TableView<>();

    private final Label statusLabel =
            new Label();

    public static void show() {

        new AdminCustomersWindow()
                .open();
    }

    private void open() {

        Button refresh =
                new Button(
                        "Làm mới"
                );

        refresh.setOnAction(
                event -> loadCustomers()
        );

        configureTable();

        VBox root =
                new VBox(
                        15,
                        refresh,
                        table,
                        statusLabel
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
                        950,
                        600
                );

        PageNavigation.show("Khách hàng", scene);

        loadCustomers();
    }

    private void configureTable() {

        TableColumn<AdminService.Customer, Long> id =
                new TableColumn<>(
                        "ID"
                );

        id.setCellValueFactory(
                new PropertyValueFactory<>(
                        "id"
                )
        );

        TableColumn<AdminService.Customer, String> name =
                new TableColumn<>(
                        "Họ tên"
                );

        name.setCellValueFactory(
                new PropertyValueFactory<>(
                        "name"
                )
        );

        TableColumn<AdminService.Customer, String> email =
                new TableColumn<>(
                        "Email"
                );

        email.setCellValueFactory(
                new PropertyValueFactory<>(
                        "email"
                )
        );

        TableColumn<AdminService.Customer, String> phone =
                new TableColumn<>(
                        "Số điện thoại"
                );

        phone.setCellValueFactory(
                new PropertyValueFactory<>(
                        "phone"
                )
        );

        TableColumn<AdminService.Customer, String> status =
                new TableColumn<>(
                        "Trạng thái"
                );

        status.setCellValueFactory(
                new PropertyValueFactory<>(
                        "status"
                )
        );

        table.getColumns()
                .setAll(
                        id,
                        name,
                        email,
                        phone,
                        status
                );

        table.setColumnResizePolicy(
                TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN
        );
    }

    private void loadCustomers() {

        statusLabel.setText(
                "Đang tải khách hàng..."
        );

        Task<List<AdminService.Customer>> task =
                new Task<>() {

                    @Override
                    protected List<AdminService.Customer> call()
                            throws Exception {

                        return adminService
                                .getCustomers();
                    }
                };

        task.setOnSucceeded(
                event -> {

                    table.setItems(
                            FXCollections.observableArrayList(
                                    task.getValue()
                            )
                    );

                    statusLabel.setText(
                            "Tổng cộng: "
                                    + task.getValue()
                                    .size()
                                    + " khách hàng."
                    );
                }
        );

        task.setOnFailed(
                event -> {

                    table.getItems()
                            .clear();

                    statusLabel.setText(
                            "Không thể tải khách hàng: "
                                    + (
                                    task.getException()
                                            == null
                                            ? "Lỗi không xác định."
                                            : task.getException()
                                            .getMessage()
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
}