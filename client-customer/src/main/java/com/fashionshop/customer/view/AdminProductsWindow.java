package com.fashionshop.customer.view;

import com.fashionshop.customer.model.Product;
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

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AdminProductsWindow {

    private final AdminService adminService =
            new AdminService();

    private final TableView<Product> table =
            new TableView<>();

    private final TextField searchField =
            new TextField();

    private final ComboBox<String> statusFilter =
            new ComboBox<>();

    private final Label summaryLabel =
            new Label();

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

    private List<Product> allProducts =
            new ArrayList<>();

    public static void show() {

        new AdminProductsWindow()
                .open();
    }

    private void open() {

        Stage stage =
                new Stage();

        Label title =
                new Label(
                        "QUẢN LÝ SẢN PHẨM"
                );

        title.setStyle(
                "-fx-font-size: 26px;"
                        + "-fx-font-weight: bold;"
                        + "-fx-text-fill: #111827;"
        );

        Label description =
                new Label(
                        "Quản lý thông tin, giá bán và trạng thái sản phẩm."
                );

        description.setStyle(
                "-fx-text-fill: #6b7280;"
        );

        searchField.setPromptText(
                "Tìm tên hoặc danh mục..."
        );

        searchField.setPrefWidth(
                280
        );

        searchField.textProperty()
                .addListener(
                        (observable, oldValue, newValue) ->
                                applyFilter()
                );

        statusFilter.getItems()
                .addAll(
                        "TẤT CẢ",
                        "ACTIVE",
                        "INACTIVE"
                );

        statusFilter.setValue(
                "TẤT CẢ"
        );

        statusFilter.setOnAction(
                event -> applyFilter()
        );

        Button addButton =
                new Button(
                        "+ Thêm sản phẩm"
                );

        addButton.setPrefHeight(
                38
        );

        addButton.setStyle(
                "-fx-background-color: #16a34a;"
                        + "-fx-text-fill: white;"
                        + "-fx-font-weight: bold;"
                        + "-fx-background-radius: 8;"
        );

        addButton.setOnAction(
                event ->
                        AdminProductFormWindow
                                .showCreate(
                                        this::loadProducts
                                )
        );

        Button refreshButton =
                new Button(
                        "Làm mới"
                );

        refreshButton.setPrefHeight(
                38
        );

        refreshButton.setStyle(
                "-fx-background-color: #2563eb;"
                        + "-fx-text-fill: white;"
                        + "-fx-font-weight: bold;"
                        + "-fx-background-radius: 8;"
        );

        refreshButton.setOnAction(
                event -> loadProducts()
        );

        HBox spacer =
                new HBox();

        HBox.setHgrow(
                spacer,
                Priority.ALWAYS
        );

        HBox toolbar =
                new HBox(
                        10,
                        searchField,
                        statusFilter,
                        spacer,
                        addButton,
                        refreshButton
                );

        toolbar.setAlignment(
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

        HBox footer =
                new HBox(
                        15,
                        progress,
                        statusLabel,
                        summaryLabel
                );

        footer.setAlignment(
                Pos.CENTER_LEFT
        );

        VBox root =
                new VBox(
                        8,
                        title,
                        description,
                        new Separator(),
                        toolbar,
                        table,
                        footer
                );

        root.setPadding(
                new Insets(22)
        );

        root.setStyle(
                "-fx-background-color: #f9fafb;"
        );

        VBox.setVgrow(
                table,
                Priority.ALWAYS
        );

        Scene scene =
                new Scene(
                        root,
                        1400,
                        720
                );

        stage.setTitle(
                "Fashion Shop - Quản lý sản phẩm"
        );

        stage.setScene(
                scene
        );

        stage.show();

        loadProducts();
    }

    private void configureTable() {

        TableColumn<Product, Long> idCol =
                new TableColumn<>(
                        "ID"
                );

        idCol.setCellValueFactory(
                new PropertyValueFactory<>(
                        "id"
                )
        );

        idCol.setPrefWidth(
                65
        );

        TableColumn<Product, String> nameCol =
                new TableColumn<>(
                        "Tên sản phẩm"
                );

        nameCol.setCellValueFactory(
                new PropertyValueFactory<>(
                        "name"
                )
        );

        nameCol.setPrefWidth(
                230
        );

        TableColumn<Product, String> categoryCol =
                new TableColumn<>(
                        "Danh mục"
                );

        categoryCol.setCellValueFactory(
                new PropertyValueFactory<>(
                        "categoryName"
                )
        );

        TableColumn<Product, Double> priceCol =
                new TableColumn<>(
                        "Giá"
                );

        priceCol.setCellValueFactory(
                new PropertyValueFactory<>(
                        "price"
                )
        );

        priceCol.setCellFactory(
                column ->
                        new TableCell<>() {

                            @Override
                            protected void updateItem(
                                    Double value,
                                    boolean empty
                            ) {

                                super.updateItem(
                                        value,
                                        empty
                                );

                                if (empty
                                        || value == null) {

                                    setText(
                                            null
                                    );

                                } else {

                                    setText(
                                            currency.format(
                                                    value
                                            )
                                    );
                                }
                            }
                        }
        );

        TableColumn<Product, String> sizeCol =
                new TableColumn<>(
                        "Size"
                );

        sizeCol.setCellValueFactory(
                new PropertyValueFactory<>(
                        "size"
                )
        );

        TableColumn<Product, String> colorCol =
                new TableColumn<>(
                        "Màu"
                );

        colorCol.setCellValueFactory(
                new PropertyValueFactory<>(
                        "color"
                )
        );

        TableColumn<Product, Integer> stockCol =
                new TableColumn<>(
                        "Tồn kho"
                );

        stockCol.setCellValueFactory(
                new PropertyValueFactory<>(
                        "stock"
                )
        );

        stockCol.setCellFactory(
                column ->
                        new TableCell<>() {

                            @Override
                            protected void updateItem(
                                    Integer value,
                                    boolean empty
                            ) {

                                super.updateItem(
                                        value,
                                        empty
                                );

                                if (empty
                                        || value == null) {

                                    setText(
                                            null
                                    );

                                    setStyle(
                                            ""
                                    );

                                    return;
                                }

                                setText(
                                        String.valueOf(
                                                value
                                        )
                                );

                                if (value <= 5) {

                                    setStyle(
                                            "-fx-text-fill: #dc2626;"
                                                    + "-fx-font-weight: bold;"
                                    );

                                } else {

                                    setStyle(
                                            "-fx-text-fill: #166534;"
                                                    + "-fx-font-weight: bold;"
                                    );
                                }
                            }
                        }
        );

        TableColumn<Product, String> statusCol =
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

                                    setText(
                                            null
                                    );

                                    setStyle(
                                            ""
                                    );

                                    return;
                                }

                                if ("ACTIVE"
                                        .equalsIgnoreCase(
                                                value
                                        )) {

                                    setText(
                                            "Đang bán"
                                    );

                                    setStyle(
                                            "-fx-text-fill: #15803d;"
                                                    + "-fx-font-weight: bold;"
                                    );

                                } else {

                                    setText(
                                            "Đã ẩn"
                                    );

                                    setStyle(
                                            "-fx-text-fill: #9ca3af;"
                                                    + "-fx-font-weight: bold;"
                                    );
                                }
                            }
                        }
        );

        TableColumn<Product, Void> actionCol =
                new TableColumn<>(
                        "Thao tác"
                );

        actionCol.setPrefWidth(
                220
        );

        actionCol.setCellFactory(
                column ->
                        new TableCell<>() {

                            private final Button editButton =
                                    new Button(
                                            "Sửa"
                                    );

                            private final Button statusButton =
                                    new Button();

                            private final HBox box =
                                    new HBox(
                                            7,
                                            editButton,
                                            statusButton
                                    );

                            {

                                box.setAlignment(
                                        Pos.CENTER_LEFT
                                );

                                editButton.setStyle(
                                        "-fx-background-color: #2563eb;"
                                                + "-fx-text-fill: white;"
                                                + "-fx-font-weight: bold;"
                                );

                                editButton.setOnAction(
                                        event -> {

                                            Product product =
                                                    currentProduct();

                                            if (product != null) {

                                                AdminProductFormWindow
                                                        .showEdit(
                                                                product,
                                                                AdminProductsWindow
                                                                        .this
                                                                        ::loadProducts
                                                        );
                                            }
                                        }
                                );

                                statusButton.setOnAction(
                                        event -> {

                                            Product product =
                                                    currentProduct();

                                            if (product != null) {

                                                toggleStatus(
                                                        product
                                                );
                                            }
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

                                Product product =
                                        currentProduct();

                                if (product == null) {

                                    setGraphic(
                                            null
                                    );

                                    return;
                                }

                                boolean active =
                                        "ACTIVE"
                                                .equalsIgnoreCase(
                                                        product.getStatus()
                                                );

                                statusButton.setText(
                                        active
                                                ? "Ẩn"
                                                : "Kích hoạt"
                                );

                                statusButton.setStyle(
                                        active
                                                ? "-fx-background-color: #dc2626;"
                                                + "-fx-text-fill: white;"
                                                + "-fx-font-weight: bold;"
                                                : "-fx-background-color: #16a34a;"
                                                + "-fx-text-fill: white;"
                                                + "-fx-font-weight: bold;"
                                );

                                setGraphic(
                                        box
                                );
                            }

                            private Product currentProduct() {

                                int index =
                                        getIndex();

                                if (index < 0
                                        || index >= getTableView()
                                        .getItems()
                                        .size()) {

                                    return null;
                                }

                                return getTableView()
                                        .getItems()
                                        .get(index);
                            }
                        }
        );

        table.getColumns()
                .setAll(
                        idCol,
                        nameCol,
                        categoryCol,
                        priceCol,
                        sizeCol,
                        colorCol,
                        stockCol,
                        statusCol,
                        actionCol
                );

        table.setColumnResizePolicy(
                TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN
        );

        table.setPlaceholder(
                new Label(
                        "Không có sản phẩm phù hợp."
                )
        );
    }

    private void loadProducts() {

        setLoading(
                true,
                "Đang tải danh sách sản phẩm..."
        );

        Task<List<Product>> task =
                new Task<>() {

                    @Override
                    protected List<Product> call()
                            throws Exception {

                        return adminService
                                .getProducts();
                    }
                };

        task.setOnSucceeded(
                event -> {

                    allProducts =
                            new ArrayList<>(
                                    task.getValue()
                            );

                    applyFilter();

                    setLoading(
                            false,
                            "Dữ liệu đã được cập nhật."
                    );
                }
        );

        task.setOnFailed(
                event -> {

                    allProducts.clear();

                    table.getItems()
                            .clear();

                    setLoading(
                            false,
                            "Không thể tải sản phẩm: "
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

        String keyword =
                searchField.getText()
                        == null
                        ? ""
                        : searchField
                        .getText()
                        .trim()
                        .toLowerCase();

        String selectedStatus =
                statusFilter.getValue();

        List<Product> result =
                allProducts.stream()
                        .filter(
                                product -> {

                                    String name =
                                            product.getName()
                                                    == null
                                                    ? ""
                                                    : product
                                                    .getName()
                                                    .toLowerCase();

                                    String category =
                                            product.getCategoryName()
                                                    == null
                                                    ? ""
                                                    : product
                                                    .getCategoryName()
                                                    .toLowerCase();

                                    boolean keywordMatch =
                                            keyword.isEmpty()
                                                    || name.contains(
                                                    keyword
                                            )
                                                    || category.contains(
                                                    keyword
                                            );

                                    boolean statusMatch =
                                            selectedStatus == null
                                                    || "TẤT CẢ"
                                                    .equals(
                                                            selectedStatus
                                                    )
                                                    || selectedStatus
                                                    .equalsIgnoreCase(
                                                            product
                                                            .getStatus()
                                                    );

                                    return keywordMatch
                                            && statusMatch;
                                }
                        )
                        .toList();

        table.setItems(
                FXCollections.observableArrayList(
                        result
                )
        );

        long active =
                allProducts.stream()
                        .filter(
                                product ->
                                        "ACTIVE"
                                                .equalsIgnoreCase(
                                                        product.getStatus()
                                                )
                        )
                        .count();

        long lowStock =
                allProducts.stream()
                        .filter(
                                product ->
                                        "ACTIVE"
                                                .equalsIgnoreCase(
                                                        product.getStatus()
                                                )
                                                && product.getStock()
                                                <= 5
                        )
                        .count();

        summaryLabel.setText(
                "Hiển thị "
                        + result.size()
                        + "/"
                        + allProducts.size()
                        + " | Đang bán: "
                        + active
                        + " | Sắp hết: "
                        + lowStock
        );
    }

    private void toggleStatus(
            Product product
    ) {

        boolean active =
                "ACTIVE"
                        .equalsIgnoreCase(
                                product.getStatus()
                        );

        String nextStatus =
                active
                        ? "INACTIVE"
                        : "ACTIVE";

        Alert confirm =
                new Alert(
                        Alert.AlertType.CONFIRMATION
                );

        confirm.setTitle(
                "Cập nhật sản phẩm"
        );

        confirm.setHeaderText(
                product.getName()
        );

        confirm.setContentText(
                active
                        ? "Ẩn sản phẩm này khỏi cửa hàng?"
                        : "Kích hoạt lại sản phẩm này?"
        );

        confirm.showAndWait()
                .ifPresent(
                        button -> {

                            if (button
                                    == ButtonType.OK) {

                                changeStatus(
                                        product,
                                        nextStatus
                                );
                            }
                        }
                );
    }

    private void changeStatus(
            Product product,
            String status
    ) {

        setLoading(
                true,
                "Đang cập nhật sản phẩm #"
                        + product.getId()
                        + "..."
        );

        Task<Product> task =
                new Task<>() {

                    @Override
                    protected Product call()
                            throws Exception {

                        return adminService
                                .changeProductStatus(
                                        product.getId(),
                                        status
                                );
                    }
                };

        task.setOnSucceeded(
                event -> {

                    setLoading(
                            false,
                            "Cập nhật trạng thái thành công."
                    );

                    loadProducts();
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

                    Alert error =
                            new Alert(
                                    Alert.AlertType.ERROR
                            );

                    error.setTitle(
                            "Lỗi"
                    );

                    error.setHeaderText(
                            "Không thể cập nhật sản phẩm"
                    );

                    error.setContentText(
                            errorMessage(
                                    task.getException()
                            )
                    );

                    error.showAndWait();
                }
        );

        Thread thread =
                new Thread(task);

        thread.setDaemon(
                true
        );

        thread.start();
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
            Throwable error
    ) {

        if (error == null) {

            return "Lỗi không xác định.";
        }

        if (error.getMessage()
                == null
                || error.getMessage()
                .isBlank()) {

            return error.getClass()
                    .getSimpleName();
        }

        return error.getMessage();
    }
}