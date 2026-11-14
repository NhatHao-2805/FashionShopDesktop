package com.fashionshop.customer.view;

import com.fashionshop.customer.service.InventoryService;

import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;

public class AdminInventoryWindow {

    private final InventoryService inventoryService =
            new InventoryService();

    private final TableView<InventoryService.StockItem> stockTable =
            new TableView<>();

    private final TableView<InventoryService.InventoryTransaction> historyTable =
            new TableView<>();

    private final TextField searchField =
            new TextField();

    private final CheckBox lowStockOnly =
            new CheckBox(
                    "Chỉ sản phẩm sắp hết"
            );

    private final Label summaryLabel =
            new Label();

    private final Label statusLabel =
            new Label();

    private final ProgressIndicator progress =
            new ProgressIndicator();

    private List<InventoryService.StockItem> allStock =
            new ArrayList<>();

    public static void show() {

        new AdminInventoryWindow()
                .open();
    }

    private void open() {

        Stage stage =
                new Stage();

        Label title =
                new Label(
                        "QUẢN LÝ KHO HÀNG"
                );

        title.setStyle(
                "-fx-font-size: 26px;"
                        + "-fx-font-weight: bold;"
        );

        Label subtitle =
                new Label(
                        "Theo dõi tồn kho và toàn bộ lịch sử nhập / xuất."
                );

        subtitle.setStyle(
                "-fx-text-fill: #6b7280;"
        );

        searchField.setPromptText(
                "Tìm tên sản phẩm / danh mục..."
        );

        searchField.setPrefWidth(
                300
        );

        searchField.textProperty()
                .addListener(
                        (obs, oldValue, newValue) ->
                                applyFilter()
                );

        lowStockOnly.setOnAction(
                event -> applyFilter()
        );

        Button refresh =
                new Button(
                        "Làm mới"
                );

        refresh.setStyle(
                "-fx-background-color: #2563eb;"
                        + "-fx-text-fill: white;"
                        + "-fx-font-weight: bold;"
        );

        refresh.setOnAction(
                event -> loadAll()
        );

        Region spacer =
                new Region();

        HBox.setHgrow(
                spacer,
                Priority.ALWAYS
        );

        HBox toolbar =
                new HBox(
                        12,
                        searchField,
                        lowStockOnly,
                        spacer,
                        refresh
                );

        toolbar.setAlignment(
                Pos.CENTER_LEFT
        );

        configureStockTable();

        configureHistoryTable();

        Label stockTitle =
                sectionTitle(
                        "Tồn kho hiện tại"
                );

        Label historyTitle =
                sectionTitle(
                        "Lịch sử giao dịch kho"
                );

        VBox stockBox =
                new VBox(
                        8,
                        stockTitle,
                        stockTable
                );

        VBox historyBox =
                new VBox(
                        8,
                        historyTitle,
                        historyTable
                );

        VBox.setVgrow(
                stockTable,
                Priority.ALWAYS
        );

        VBox.setVgrow(
                historyTable,
                Priority.ALWAYS
        );

        SplitPane splitPane =
                new SplitPane(
                        stockBox,
                        historyBox
                );

        splitPane.setOrientation(
                javafx.geometry.Orientation.VERTICAL
        );

        splitPane.setDividerPositions(
                0.52
        );

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
                        14,
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
                        subtitle,
                        new Separator(),
                        toolbar,
                        splitPane,
                        footer
                );

        root.setPadding(
                new Insets(22)
        );

        root.setStyle(
                "-fx-background-color: #f9fafb;"
        );

        VBox.setVgrow(
                splitPane,
                Priority.ALWAYS
        );

        Scene scene =
                new Scene(
                        root,
                        1400,
                        820
                );

        stage.setTitle(
                "Fashion Shop - Kho hàng"
        );

        stage.setScene(
                scene
        );

        stage.show();

        loadAll();
    }

    private void configureStockTable() {

        TableColumn<InventoryService.StockItem, Long> id =
                new TableColumn<>(
                        "ID"
                );

        id.setCellValueFactory(
                new PropertyValueFactory<>(
                        "productId"
                )
        );

        id.setPrefWidth(
                70
        );

        TableColumn<InventoryService.StockItem, String> name =
                new TableColumn<>(
                        "Sản phẩm"
                );

        name.setCellValueFactory(
                new PropertyValueFactory<>(
                        "productName"
                )
        );

        name.setPrefWidth(
                270
        );

        TableColumn<InventoryService.StockItem, String> category =
                new TableColumn<>(
                        "Danh mục"
                );

        category.setCellValueFactory(
                new PropertyValueFactory<>(
                        "categoryName"
                )
        );

        TableColumn<InventoryService.StockItem, Integer> stock =
                new TableColumn<>(
                        "Tồn kho"
                );

        stock.setCellValueFactory(
                new PropertyValueFactory<>(
                        "stock"
                )
        );

        stock.setCellFactory(
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

                                    setText(null);
                                    setStyle("");

                                    return;
                                }

                                setText(
                                        String.valueOf(
                                                value
                                        )
                                );

                                if (value <= 0) {

                                    setStyle(
                                            "-fx-text-fill: #b91c1c;"
                                                    + "-fx-font-weight: bold;"
                                                    + "-fx-background-color: #fee2e2;"
                                    );

                                } else if (value <= 5) {

                                    setStyle(
                                            "-fx-text-fill: #c2410c;"
                                                    + "-fx-font-weight: bold;"
                                                    + "-fx-background-color: #ffedd5;"
                                    );

                                } else {

                                    setStyle(
                                            "-fx-text-fill: #15803d;"
                                                    + "-fx-font-weight: bold;"
                                    );
                                }
                            }
                        }
        );

        TableColumn<InventoryService.StockItem, String> status =
                new TableColumn<>(
                        "Trạng thái"
                );

        status.setCellValueFactory(
                new PropertyValueFactory<>(
                        "status"
                )
        );

        TableColumn<InventoryService.StockItem, Void> action =
                new TableColumn<>(
                        "Thao tác"
                );

        action.setPrefWidth(
                170
        );

        action.setCellFactory(
                column ->
                        new TableCell<>() {

                            private final Button button =
                                    new Button(
                                            "Điều chỉnh kho"
                                    );

                            {

                                button.setStyle(
                                        "-fx-background-color: #2563eb;"
                                                + "-fx-text-fill: white;"
                                                + "-fx-font-weight: bold;"
                                );

                                button.setOnAction(
                                        event -> {

                                            int index =
                                                    getIndex();

                                            if (index < 0
                                                    || index >= getTableView()
                                                    .getItems()
                                                    .size()) {

                                                return;
                                            }

                                            InventoryService.StockItem item =
                                                    getTableView()
                                                            .getItems()
                                                            .get(index);

                                            new AdminInventoryAdjustDialog(
                                                    item,
                                                    AdminInventoryWindow
                                                            .this
                                                            ::loadAll
                                            ).showAndWait();
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

                                setGraphic(
                                        empty
                                                ? null
                                                : button
                                );
                            }
                        }
        );

        stockTable.getColumns()
                .setAll(
                        id,
                        name,
                        category,
                        stock,
                        status,
                        action
                );

        stockTable.setColumnResizePolicy(
                TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN
        );

        stockTable.setPlaceholder(
                new Label(
                        "Không có dữ liệu kho."
                )
        );
    }

    private void configureHistoryTable() {

        TableColumn<InventoryService.InventoryTransaction, Long> id =
                new TableColumn<>(
                        "#"
                );

        id.setCellValueFactory(
                new PropertyValueFactory<>(
                        "id"
                )
        );

        TableColumn<InventoryService.InventoryTransaction, String> product =
                new TableColumn<>(
                        "Sản phẩm"
                );

        product.setCellValueFactory(
                new PropertyValueFactory<>(
                        "productName"
                )
        );

        product.setPrefWidth(
                210
        );

        TableColumn<InventoryService.InventoryTransaction, String> type =
                new TableColumn<>(
                        "Loại"
                );

        type.setCellValueFactory(
                new PropertyValueFactory<>(
                        "type"
                )
        );

        type.setCellFactory(
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

                                setText(
                                        empty
                                                ? null
                                                : typeText(value)
                                );
                            }
                        }
        );

        TableColumn<InventoryService.InventoryTransaction, Integer> quantity =
                new TableColumn<>(
                        "SL"
                );

        quantity.setCellValueFactory(
                new PropertyValueFactory<>(
                        "quantity"
                )
        );

        TableColumn<InventoryService.InventoryTransaction, Integer> before =
                new TableColumn<>(
                        "Trước"
                );

        before.setCellValueFactory(
                new PropertyValueFactory<>(
                        "stockBefore"
                )
        );

        TableColumn<InventoryService.InventoryTransaction, Integer> after =
                new TableColumn<>(
                        "Sau"
                );

        after.setCellValueFactory(
                new PropertyValueFactory<>(
                        "stockAfter"
                )
        );

        TableColumn<InventoryService.InventoryTransaction, String> user =
                new TableColumn<>(
                        "Người thao tác"
                );

        user.setCellValueFactory(
                new PropertyValueFactory<>(
                        "userName"
                )
        );

        TableColumn<InventoryService.InventoryTransaction, String> reason =
                new TableColumn<>(
                        "Lý do"
                );

        reason.setCellValueFactory(
                new PropertyValueFactory<>(
                        "reason"
                )
        );

        reason.setPrefWidth(
                300
        );

        TableColumn<InventoryService.InventoryTransaction, String> created =
                new TableColumn<>(
                        "Thời gian"
                );

        created.setCellValueFactory(
                new PropertyValueFactory<>(
                        "createdAt"
                )
        );

        historyTable.getColumns()
                .setAll(
                        id,
                        product,
                        type,
                        quantity,
                        before,
                        after,
                        user,
                        reason,
                        created
                );

        historyTable.setColumnResizePolicy(
                TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN
        );

        historyTable.setPlaceholder(
                new Label(
                        "Chưa có lịch sử giao dịch kho."
                )
        );
    }

    private void loadAll() {

        setLoading(
                true,
                "Đang tải dữ liệu kho..."
        );

        Task<InventoryData> task =
                new Task<>() {

                    @Override
                    protected InventoryData call()
                            throws Exception {

                        List<InventoryService.StockItem> stock =
                                inventoryService
                                        .getStockOverview();

                        List<InventoryService.InventoryTransaction> history =
                                inventoryService
                                        .getTransactions(
                                                200
                                        );

                        return new InventoryData(
                                stock,
                                history
                        );
                    }
                };

        task.setOnSucceeded(
                event -> {

                    InventoryData data =
                            task.getValue();

                    allStock =
                            new ArrayList<>(
                                    data.stock
                            );

                    historyTable.setItems(
                            FXCollections
                                    .observableArrayList(
                                            data.history
                                    )
                    );

                    applyFilter();

                    setLoading(
                            false,
                            "Dữ liệu kho đã được cập nhật."
                    );
                }
        );

        task.setOnFailed(
                event -> {

                    setLoading(
                            false,
                            "Không thể tải dữ liệu kho: "
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

        List<InventoryService.StockItem> result =
                allStock.stream()
                        .filter(
                                item -> {

                                    String product =
                                            item.getProductName()
                                                    == null
                                                    ? ""
                                                    : item
                                                    .getProductName()
                                                    .toLowerCase();

                                    String category =
                                            item.getCategoryName()
                                                    == null
                                                    ? ""
                                                    : item
                                                    .getCategoryName()
                                                    .toLowerCase();

                                    boolean keywordMatch =
                                            keyword.isEmpty()
                                                    || product.contains(
                                                    keyword
                                            )
                                                    || category.contains(
                                                    keyword
                                            );

                                    boolean lowMatch =
                                            !lowStockOnly
                                                    .isSelected()
                                                    || item.getStock()
                                                    <= 5;

                                    return keywordMatch
                                            && lowMatch;
                                }
                        )
                        .toList();

        stockTable.setItems(
                FXCollections.observableArrayList(
                        result
                )
        );

        long outOfStock =
                allStock.stream()
                        .filter(
                                item ->
                                        item.getStock() <= 0
                        )
                        .count();

        long low =
                allStock.stream()
                        .filter(
                                item ->
                                        item.getStock() > 0
                                                && item.getStock() <= 5
                        )
                        .count();

        int total =
                allStock.stream()
                        .mapToInt(
                                InventoryService.StockItem::getStock
                        )
                        .sum();

        summaryLabel.setText(
                "Sản phẩm: "
                        + allStock.size()
                        + "  |  Tổng tồn: "
                        + total
                        + "  |  Sắp hết: "
                        + low
                        + "  |  Hết hàng: "
                        + outOfStock
        );
    }

    private Label sectionTitle(
            String text
    ) {

        Label label =
                new Label(
                        text
                );

        label.setStyle(
                "-fx-font-size: 18px;"
                        + "-fx-font-weight: bold;"
        );

        return label;
    }

    private String typeText(
            String type
    ) {

        if (type == null) {
            return "-";
        }

        return switch (
                type.toUpperCase()
        ) {

            case "IMPORT" ->
                    "Nhập hàng";

            case "EXPORT" ->
                    "Xuất hàng";

            case "ADJUST_IN" ->
                    "Điều chỉnh +";

            case "ADJUST_OUT" ->
                    "Điều chỉnh -";

            case "SALE" ->
                    "Bán hàng";

            case "ORDER_CANCEL" ->
                    "Hoàn đơn";

            default ->
                    type;
        };
    }

    private void setLoading(
            boolean loading,
            String message
    ) {

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

    private record InventoryData(
            List<InventoryService.StockItem> stock,
            List<InventoryService.InventoryTransaction> history
    ) {
    }
}