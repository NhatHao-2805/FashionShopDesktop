package com.fashionshop.customer.view;

import com.fashionshop.customer.service.InventoryService;

import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class AdminInventoryAdjustDialog {

    private final InventoryService inventoryService =
            new InventoryService();

    private final InventoryService.StockItem item;

    private final Runnable onSuccess;


    private final ComboBox<TransactionType> typeBox =
            new ComboBox<>();

    private final Spinner<Integer> quantitySpinner =
            new Spinner<>(
                    1,
                    999999,
                    1
            );

    private final TextArea reasonArea =
            new TextArea();

    private final Label previewLabel =
            new Label();

    private final OperationStatus status = new OperationStatus();

    private final Button saveButton =
            new Button(
                    "Xác nhận"
            );

    public AdminInventoryAdjustDialog(
            InventoryService.StockItem item,
            Runnable onSuccess
    ) {

        this.item =
                item;

        this.onSuccess =
                onSuccess;
    }

    public void show() {

        Label product =
                new Label(
                        item.getProductName()
                                + "  •  Tồn hiện tại: "
                                + item.getStock()
                );

        product.setStyle(
                "-fx-font-size: 15px;"
                        + "-fx-text-fill: #374151;"
                        + "-fx-font-weight: bold;"
        );

        typeBox.getItems()
                .addAll(
                        TransactionType.values()
                );

        typeBox.setValue(
                TransactionType.IMPORT
        );

        typeBox.setMaxWidth(
                Double.MAX_VALUE
        );

        quantitySpinner.setEditable(
                true
        );

        quantitySpinner.setMaxWidth(
                Double.MAX_VALUE
        );

        reasonArea.setPromptText(
                "Ví dụ: Nhập lô hàng mới từ nhà cung cấp..."
        );

        reasonArea.setWrapText(
                true
        );

        reasonArea.setPrefRowCount(
                4
        );

        typeBox.setOnAction(
                event -> updatePreview()
        );

        quantitySpinner.valueProperty()
                .addListener(
                        (obs, oldValue, newValue) ->
                                updatePreview()
                );

        previewLabel.setWrapText(
                true
        );

        previewLabel.setStyle(
                "-fx-background-color: #e3edf4;"
                        + "-fx-text-fill: #294b61;"
                        + "-fx-font-weight: bold;"
                        + "-fx-padding: 10;"
                        + "-fx-background-radius: 8;"
        );

        updatePreview();

        GridPane form =
                new GridPane();

        form.setHgap(
                12
        );

        form.setVgap(
                12
        );

        form.add(
                new Label(
                        "Loại giao dịch:"
                ),
                0,
                0
        );

        form.add(
                typeBox,
                1,
                0
        );

        form.add(
                new Label(
                        "Số lượng:"
                ),
                0,
                1
        );

        form.add(
                quantitySpinner,
                1,
                1
        );

        form.add(
                new Label(
                        "Lý do:"
                ),
                0,
                2
        );

        form.add(
                reasonArea,
                1,
                2
        );

        saveButton.setStyle(
                "-fx-background-color: #294b61;"
                        + "-fx-text-fill: white;"
                        + "-fx-font-weight: bold;"
                        + "-fx-background-radius: 8;"
        );

        saveButton.setOnAction(
                event -> submit()
        );

        Button cancel =
                new Button(
                        "Hủy"
                );

        cancel.setOnAction(
                event -> PageNavigation.back(saveButton.getScene())
        );
        cancel.disableProperty().bind(saveButton.disableProperty());

        HBox buttons =
                new HBox(
                        10,
                        saveButton,
                        cancel
                );

        VBox root =
                new VBox(
                        14,
                        product,
                        new Separator(),
                        form,
                        previewLabel,
                        buttons,
                        status
                );

        root.setPadding(
                new Insets(24)
        );

        Scene scene =
                new Scene(
                        root,
                        560,
                        480
                );

        PageNavigation.show("Điều chỉnh kho", scene);
        PageNavigation.guardBack(scene, () -> !saveButton.isDisabled());
    }

    private void updatePreview() {

        TransactionType type =
                typeBox.getValue();

        int quantity =
                quantitySpinner.getValue();

        int after =
                type != null
                        && type.increase
                        ? item.getStock() + quantity
                        : item.getStock() - quantity;

        previewLabel.setText(
                "Tồn trước: "
                        + item.getStock()
                        + "   →   Tồn sau: "
                        + after
        );

        if (after < 0) {

            previewLabel.setStyle(
                    "-fx-background-color: #fef2f2;"
                            + "-fx-text-fill: #dc2626;"
                            + "-fx-font-weight: bold;"
                            + "-fx-padding: 10;"
                            + "-fx-background-radius: 8;"
            );

        } else {

            previewLabel.setStyle(
                    "-fx-background-color: #e3edf4;"
                            + "-fx-text-fill: #294b61;"
                            + "-fx-font-weight: bold;"
                            + "-fx-padding: 10;"
                            + "-fx-background-radius: 8;"
            );
        }
    }

    private void submit() {

        TransactionType type =
                typeBox.getValue();

        String reason =
                reasonArea.getText()
                        == null
                        ? ""
                        : reasonArea
                        .getText()
                        .trim();

        if (type == null) {

            showError(
                    "Vui lòng chọn loại giao dịch."
            );

            return;
        }

        if (reason.isBlank()) {

            showError(
                    "Vui lòng nhập lý do điều chỉnh kho."
            );

            return;
        }

        int quantity =
                quantitySpinner.getValue();

        int after =
                type.increase
                        ? item.getStock() + quantity
                        : item.getStock() - quantity;

        if (after < 0) {

            showError(
                    "Không thể xuất nhiều hơn tồn kho hiện tại."
            );

            return;
        }

        setLoading(
                true
        );

        Task<InventoryService.AdjustmentResult> task =
                new Task<>() {

                    @Override
                    protected InventoryService.AdjustmentResult call()
                            throws Exception {

                        return inventoryService
                                .adjustStock(
                                        item.getProductId(),
                                        type.apiValue,
                                        quantity,
                                        reason
                                );
                    }
                };

        task.setOnSucceeded(
                event -> {

                    setLoading(
                            false
                    );

                    InventoryService.AdjustmentResult result =
                            task.getValue();

                    Alert success =
                            new Alert(
                                    Alert.AlertType.INFORMATION
                            );

                    success.setTitle(
                            "Fashion Shop"
                    );

                    success.setHeaderText(
                            "Cập nhật kho thành công"
                    );

                    success.setContentText(
                            result.getProductName()
                                    + "\nTồn trước: "
                                    + result.getStockBefore()
                                    + "\nTồn sau: "
                                    + result.getStockAfter()
                    );

                    success.showAndWait();

                    PageNavigation.back(saveButton.getScene());
                    if (onSuccess != null) {
                        onSuccess.run();
                    }
                }
        );

        task.setOnFailed(
                event -> {

                    setLoading(
                            false
                    );

                    Throwable error =
                            task.getException();

                    showError(
                            error == null
                                    ? "Không thể điều chỉnh kho."
                                    : error.getMessage()
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

    private void setLoading(boolean loading) {
        saveButton.setDisable(loading);
        typeBox.setDisable(loading);
        quantitySpinner.setDisable(loading);
        reasonArea.setDisable(loading);
        if (loading) {
            status.loading("Đang cập nhật kho… Nút xác nhận tạm khóa đến khi có phản hồi.");
        } else {
            status.info("");
        }
    }

    private void showError(String message) {
        status.error(message == null ? "Không thể cập nhật kho." : message, null);
    }

    private enum TransactionType {

        IMPORT(
                "Nhập hàng",
                "IMPORT",
                true
        ),

        EXPORT(
                "Xuất hàng",
                "EXPORT",
                false
        ),

        ADJUST_IN(
                "Điều chỉnh tăng",
                "ADJUST_IN",
                true
        ),

        ADJUST_OUT(
                "Điều chỉnh giảm",
                "ADJUST_OUT",
                false
        );

        private final String displayName;

        private final String apiValue;

        private final boolean increase;

        TransactionType(
                String displayName,
                String apiValue,
                boolean increase
        ) {

            this.displayName =
                    displayName;

            this.apiValue =
                    apiValue;

            this.increase =
                    increase;
        }

        @Override
        public String toString() {
            return displayName;
        }
    }
}
