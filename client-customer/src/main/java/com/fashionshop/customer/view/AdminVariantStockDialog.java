package com.fashionshop.customer.view;

import com.fashionshop.customer.service.InventoryService;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

final class AdminVariantStockDialog {
    private record Choice(String size, String color) {}
    private record Entry(Choice choice, TextField stock) {}

    private final InventoryService service;
    private final InventoryService.StockItem product;
    private final Runnable onSave;
    private final Label summary = new Label();
    private final OperationStatus status = new OperationStatus();
    private final GridPane quantities = new GridPane();
    private final Button save = new Button("Lưu phân bổ");
    private final Button reload = new Button("Tải lại");
    private final List<Entry> entries = new ArrayList<>();
    private InventoryService.VariantSnapshot snapshot;
    private boolean reloadRequired;

    AdminVariantStockDialog(InventoryService service, InventoryService.StockItem product, Runnable onSave) {
        this.service = service;
        this.product = product;
        this.onSave = onSave;
    }

    void show() {
        quantities.setHgap(24);
        quantities.setVgap(8);
        quantities.setPadding(new Insets(10));

        var scroll = new ScrollPane(quantities);
        scroll.setFitToWidth(true);
        VBox.setVgrow(scroll, Priority.ALWAYS);

        var note = new Label("Chỉ số lượng đã phân bổ mới được bán sau khi lưu. "
                + "Để nhập hoặc xuất tổng tồn, dùng mục Điều chỉnh kho.");
        note.setWrapText(true);
        var actions = new HBox(10, reload, save);
        actions.setAlignment(Pos.CENTER_RIGHT);
        var content = new VBox(10, new Label(product.getProductName()), note, summary, scroll, status, actions);
        content.setPadding(new Insets(18));

        reload.setOnAction(event -> load());
        save.setOnAction(event -> save());
        var scene = new Scene(content, 570, 525);
        scene.getStylesheets().add(AdminVariantStockDialog.class
                .getResource("/com/fashionshop/customer/shop.css").toExternalForm());
        PageNavigation.show("Phân bổ tồn kho", scene);
        PageNavigation.guardBack(scene, () -> !reload.isDisabled());
        load();
    }

    private void load() {
        snapshot = null;
        reloadRequired = false;
        entries.clear();
        quantities.getChildren().clear();
        summary.setText("");
        run("Đang tải tồn theo size và màu…", () -> service.getVariants(product.getProductId()), this::render);
    }

    private void render() {
        Map<Choice, Integer> current = new HashMap<>();
        for (var variant : snapshot.variants()) {
            current.put(new Choice(variant.size(), variant.color()), variant.stock());
        }
        quantities.addRow(0, new Label("Size"), new Label("Màu"), new Label("Số lượng"));
        for (String size : options(snapshot.sizes())) {
            for (String color : options(snapshot.colors())) {
                var choice = new Choice(size, color);
                var input = new TextField(String.valueOf(current.getOrDefault(choice, 0)));
                input.setPrefColumnCount(6);
                input.textProperty().addListener((observable, oldValue, newValue) -> updateSummary());
                entries.add(new Entry(choice, input));
                quantities.addRow(entries.size(), new Label(size.isEmpty() ? "—" : size),
                        new Label(color.isEmpty() ? "—" : color), input);
            }
        }
        updateSummary();
    }

    private void updateSummary() {
        try {
            long allocated = allocations().stream().mapToLong(InventoryService.Allocation::stock).sum();
            long unallocated = snapshot.total() - allocated;
            summary.setText("Tổng tồn: " + snapshot.total() + "  •  Đã phân bổ: " + allocated
                    + (unallocated < 0 ? "  •  Vượt tồn: " + -unallocated : "  •  Chưa phân bổ: " + unallocated));
            save.setDisable(reloadRequired || unallocated < 0);
        } catch (IllegalArgumentException e) {
            summary.setText(e.getMessage());
            save.setDisable(true);
        }
    }

    private List<InventoryService.Allocation> allocations() {
        List<InventoryService.Allocation> values = new ArrayList<>();
        for (var entry : entries) {
            try {
                int stock = Integer.parseInt(entry.stock().getText().trim());
                if (stock < 0) {
                    throw new NumberFormatException();
                }
                values.add(new InventoryService.Allocation(entry.choice().size(), entry.choice().color(), stock));
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Số lượng phải là số nguyên không âm.");
            }
        }
        return values;
    }

    private void save() {
        var expected = snapshot;
        var values = allocations();
        run("Đang lưu phân bổ tồn kho… Vui lòng chờ xác nhận.",
                () -> service.allocate(product.getProductId(), expected, values), () -> {
            PageNavigation.back(save.getScene());
            onSave.run();
        });
    }

    private void run(String message, Callable<InventoryService.VariantSnapshot> operation, Runnable success) {
        save.setDisable(true);
        reload.setDisable(true);
        quantities.setDisable(true);
        status.loading(message);
        var task = new Task<InventoryService.VariantSnapshot>() {
            @Override
            protected InventoryService.VariantSnapshot call() throws Exception {
                return operation.call();
            }
        };
        task.setOnSucceeded(event -> {
            snapshot = task.getValue();
            reload.setDisable(false);
            quantities.setDisable(false);
            status.info("");
            if (PageNavigation.isCurrent(reload.getScene())) {
                success.run();
            }
        });
        task.setOnFailed(event -> {
            reloadRequired = true;
            if (snapshot == null) {
                summary.setText("");
            }
            reload.setDisable(false);
            quantities.setDisable(false);
            status.error("Không hoàn tất thao tác kho: " + task.getException().getMessage()
                    + " Hãy tải lại tồn kho trước khi lưu.", this::load);
        });
        var worker = new Thread(task, "variant-stock-worker");
        worker.setDaemon(true);
        worker.start();
    }

    private List<String> options(String csv) {
        var values = csv == null ? List.<String>of() : Arrays.stream(csv.split(","))
                .map(String::trim).filter(value -> !value.isEmpty()).distinct().toList();
        return values.isEmpty() ? List.of("") : values;
    }
}
