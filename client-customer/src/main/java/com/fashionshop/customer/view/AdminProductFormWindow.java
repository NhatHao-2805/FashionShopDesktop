package com.fashionshop.customer.view;

import com.fashionshop.customer.config.ServerConfig;
import com.fashionshop.customer.model.Category;
import com.fashionshop.customer.model.Product;
import com.fashionshop.customer.service.AdminService;
import com.fashionshop.customer.service.CategoryService;
import com.fashionshop.customer.service.ProductImageService;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.util.List;

public class AdminProductFormWindow {

    private final AdminService adminService =
            new AdminService();

    private final CategoryService categoryService =
            new CategoryService();

    private final ProductImageService imageService =
            new ProductImageService();

    private final Product editingProduct;

    private final Runnable onSaved;

    private final Stage stage =
            new Stage();

    private final TextField nameField =
            new TextField();

    private final TextArea descriptionArea =
            new TextArea();

    private final TextField priceField =
            new TextField();

    private final ComboBox<Category> categoryBox =
            new ComboBox<>();

    private final TextField sizeField =
            new TextField();

    private final TextField colorField =
            new TextField();

    private final TextField stockField =
            new TextField();

    private final TextField imagePathField =
            new TextField();

    private final ComboBox<String> statusBox =
            new ComboBox<>();

    private final Label imageInfoLabel =
            new Label(
                    "Chưa chọn ảnh."
            );

    private final ImageView imagePreview =
            new ImageView();

    private final Label messageLabel =
            new Label();

    private final ProgressIndicator progress =
            new ProgressIndicator();

    private final Button saveButton =
            new Button(
                    "Lưu sản phẩm"
            );

    private final Button chooseImageButton =
            new Button(
                    "Chọn ảnh..."
            );

    private File selectedImageFile;

    private AdminProductFormWindow(
            Product editingProduct,
            Runnable onSaved
    ) {

        this.editingProduct =
                editingProduct;

        this.onSaved =
                onSaved;
    }

    public static void showCreate(
            Runnable onSaved
    ) {

        new AdminProductFormWindow(
                null,
                onSaved
        ).open();
    }

    public static void showEdit(
            Product product,
            Runnable onSaved
    ) {

        new AdminProductFormWindow(
                product,
                onSaved
        ).open();
    }

    private void open() {

        stage.initModality(
                Modality.APPLICATION_MODAL
        );

        boolean editing =
                editingProduct != null;

        Label title =
                new Label(
                        editing
                                ? "SỬA SẢN PHẨM"
                                : "THÊM SẢN PHẨM"
                );

        title.setStyle(
                "-fx-font-size: 24px;"
                        + "-fx-font-weight: bold;"
                        + "-fx-text-fill: #111827;"
        );

        Label subtitle =
                new Label(
                        editing
                                ? "Cập nhật thông tin sản phẩm."
                                : "Tạo sản phẩm mới và upload ảnh trực tiếp lên server."
                );

        subtitle.setStyle(
                "-fx-text-fill: #6b7280;"
        );

        configureFields();

        GridPane form =
                new GridPane();

        form.setHgap(
                14
        );

        form.setVgap(
                14
        );

        form.add(
                label("Tên sản phẩm"),
                0,
                0
        );

        form.add(
                nameField,
                1,
                0
        );

        form.add(
                label("Danh mục"),
                0,
                1
        );

        form.add(
                categoryBox,
                1,
                1
        );

        form.add(
                label("Giá bán"),
                0,
                2
        );

        form.add(
                priceField,
                1,
                2
        );

        form.add(
                label(
                        editing
                                ? "Tồn kho hiện tại"
                                : "Tồn kho ban đầu"
                ),
                0,
                3
        );

        form.add(
                stockField,
                1,
                3
        );

        form.add(
                label("Size"),
                0,
                4
        );

        form.add(
                sizeField,
                1,
                4
        );

        form.add(
                label("Màu sắc"),
                0,
                5
        );

        form.add(
                colorField,
                1,
                5
        );

        VBox imageBox =
                createImageBox();

        form.add(
                label("Ảnh sản phẩm"),
                0,
                6
        );

        form.add(
                imageBox,
                1,
                6
        );

        form.add(
                label("Trạng thái"),
                0,
                7
        );

        form.add(
                statusBox,
                1,
                7
        );

        form.add(
                label("Mô tả"),
                0,
                8
        );

        form.add(
                descriptionArea,
                1,
                8
        );

        GridPane.setHgrow(
                nameField,
                Priority.ALWAYS
        );

        GridPane.setHgrow(
                categoryBox,
                Priority.ALWAYS
        );

        GridPane.setHgrow(
                priceField,
                Priority.ALWAYS
        );

        GridPane.setHgrow(
                sizeField,
                Priority.ALWAYS
        );

        GridPane.setHgrow(
                colorField,
                Priority.ALWAYS
        );

        GridPane.setHgrow(
                imageBox,
                Priority.ALWAYS
        );

        GridPane.setHgrow(
                statusBox,
                Priority.ALWAYS
        );

        GridPane.setHgrow(
                descriptionArea,
                Priority.ALWAYS
        );

        saveButton.setPrefHeight(
                42
        );

        saveButton.setStyle(
                "-fx-background-color: #2563eb;"
                        + "-fx-text-fill: white;"
                        + "-fx-font-weight: bold;"
                        + "-fx-background-radius: 8;"
        );

        saveButton.setOnAction(
                event -> save()
        );

        Button cancelButton =
                new Button(
                        "Hủy"
                );

        cancelButton.setPrefHeight(
                42
        );

        cancelButton.setStyle(
                "-fx-background-color: #e5e7eb;"
                        + "-fx-text-fill: #111827;"
                        + "-fx-font-weight: bold;"
                        + "-fx-background-radius: 8;"
        );

        cancelButton.setOnAction(
                event -> stage.close()
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

        messageLabel.setWrapText(
                true
        );

        HBox buttons =
                new HBox(
                        10,
                        saveButton,
                        cancelButton,
                        progress
                );

        buttons.setAlignment(
                Pos.CENTER_LEFT
        );

        VBox content =
                new VBox(
                        10,
                        title,
                        subtitle,
                        new Separator(),
                        form,
                        buttons,
                        messageLabel
                );

        content.setPadding(
                new Insets(24)
        );

        content.setStyle(
                "-fx-background-color: white;"
        );

        ScrollPane scrollPane =
                new ScrollPane(
                        content
                );

        scrollPane.setFitToWidth(
                true
        );

        scrollPane.setStyle(
                "-fx-background: white;"
                        + "-fx-background-color: white;"
        );

        Scene scene =
                new Scene(
                        scrollPane,
                        720,
                        820
                );

        stage.setTitle(
                editing
                        ? "Fashion Shop - Sửa sản phẩm"
                        : "Fashion Shop - Thêm sản phẩm"
        );

        stage.setScene(
                scene
        );

        loadCategories();

        stage.showAndWait();
    }

    private VBox createImageBox() {

        imagePreview.setFitWidth(
                230
        );

        imagePreview.setFitHeight(
                230
        );

        imagePreview.setPreserveRatio(
                true
        );

        imagePreview.setSmooth(
                true
        );

        chooseImageButton.setStyle(
                "-fx-background-color: #111827;"
                        + "-fx-text-fill: white;"
                        + "-fx-font-weight: bold;"
                        + "-fx-background-radius: 8;"
        );

        chooseImageButton.setOnAction(
                event -> chooseImage()
        );

        imagePathField.setEditable(
                false
        );

        imagePathField.setPromptText(
                "Đường dẫn ảnh trên server sẽ tự sinh."
        );

        imageInfoLabel.setStyle(
                "-fx-text-fill: #6b7280;"
        );

        VBox previewContainer =
                new VBox(
                        imagePreview
                );

        previewContainer.setAlignment(
                Pos.CENTER
        );

        previewContainer.setPrefSize(
                250,
                250
        );

        previewContainer.setStyle(
                "-fx-background-color: #f3f4f6;"
                        + "-fx-border-color: #d1d5db;"
                        + "-fx-border-radius: 8;"
                        + "-fx-background-radius: 8;"
                        + "-fx-padding: 10;"
        );

        HBox actionBox =
                new HBox(
                        10,
                        chooseImageButton,
                        imageInfoLabel
                );

        actionBox.setAlignment(
                Pos.CENTER_LEFT
        );

        return new VBox(
                8,
                previewContainer,
                actionBox,
                imagePathField
        );
    }

    private void configureFields() {

        nameField.setPromptText(
                "Ví dụ: Áo sơ mi Oxford"
        );

        priceField.setPromptText(
                "Ví dụ: 350000"
        );

        stockField.setPromptText(
                "Số lượng tồn"
        );

        sizeField.setPromptText(
                "Ví dụ: S,M,L,XL"
        );

        colorField.setPromptText(
                "Ví dụ: Đen,Trắng,Xanh"
        );

        descriptionArea.setPromptText(
                "Mô tả sản phẩm..."
        );

        descriptionArea.setPrefRowCount(
                4
        );

        descriptionArea.setWrapText(
                true
        );

        statusBox.getItems()
                .addAll(
                        "ACTIVE",
                        "INACTIVE"
                );

        statusBox.setValue(
                "ACTIVE"
        );

        if (editingProduct != null) {

            nameField.setText(
                    safe(
                            editingProduct.getName()
                    )
            );

            descriptionArea.setText(
                    safe(
                            editingProduct.getDescription()
                    )
            );

            priceField.setText(
                    String.valueOf(
                            editingProduct.getPrice()
                    )
            );

            stockField.setText(
                    String.valueOf(
                            editingProduct.getStock()
                    )
            );

            /*
             * Sau khi sản phẩm đã tạo,
             * tồn kho phải chỉnh bằng module Kho hàng
             * để có inventory transaction.
             */
            stockField.setDisable(
                    true
            );

            stockField.setTooltip(
                    new Tooltip(
                            "Hãy sử dụng mục Kho hàng để thay đổi tồn kho."
                    )
            );

            sizeField.setText(
                    safe(
                            editingProduct.getSize()
                    )
            );

            colorField.setText(
                    safe(
                            editingProduct.getColor()
                    )
            );

            String currentImage =
                    editingProduct.getImageUrl();

            imagePathField.setText(
                    safe(
                            currentImage
                    )
            );

            if (currentImage != null
                    && !currentImage.isBlank()) {

                showRemotePreview(
                        currentImage
                );

                imageInfoLabel.setText(
                        "Ảnh hiện tại"
                );
            }

            if (editingProduct.getStatus()
                    != null
                    && !editingProduct
                    .getStatus()
                    .isBlank()) {

                statusBox.setValue(
                        editingProduct.getStatus()
                                .toUpperCase()
                );
            }
        }
    }

    private void chooseImage() {

        FileChooser chooser =
                new FileChooser();

        chooser.setTitle(
                "Chọn ảnh sản phẩm"
        );

        chooser.getExtensionFilters()
                .add(
                        new FileChooser.ExtensionFilter(
                                "Ảnh JPG / PNG",
                                "*.jpg",
                                "*.jpeg",
                                "*.png"
                        )
                );

        File file =
                chooser.showOpenDialog(
                        stage
                );

        if (file == null) {
            return;
        }

        if (file.length()
                > 8L * 1024L * 1024L) {

            showError(
                    "Ảnh tối đa 8 MB."
            );

            return;
        }

        selectedImageFile =
                file;

        imageInfoLabel.setText(
                file.getName()
        );

        imagePathField.setText(
                "Sẽ upload khi bấm Lưu sản phẩm"
        );

        try {

            Image image =
                    new Image(
                            file.toURI()
                                    .toString(),
                            230,
                            230,
                            true,
                            true
                    );

            imagePreview.setImage(
                    image
            );

        } catch (Exception e) {

            selectedImageFile =
                    null;

            showError(
                    "Không thể đọc ảnh được chọn."
            );
        }
    }

    private void showRemotePreview(
            String imagePath
    ) {

        try {

            String url =
                    ServerConfig.resourceUrl(
                            imagePath
                    );

            if (url == null) {
                return;
            }

            Image image =
                    new Image(
                            url,
                            230,
                            230,
                            true,
                            true,
                            true
                    );

            imagePreview.setImage(
                    image
            );

        } catch (Exception ignored) {
        }
    }

    private void loadCategories() {

        categoryBox.setDisable(
                true
        );

        Task<List<Category>> task =
                new Task<>() {

                    @Override
                    protected List<Category> call() {

                        return categoryService
                                .getAllCategories();
                    }
                };

        task.setOnSucceeded(
                event -> {

                    List<Category> categories =
                            task.getValue();

                    categoryBox.getItems()
                            .setAll(
                                    categories
                            );

                    if (editingProduct != null
                            && editingProduct
                            .getCategoryId()
                            != null) {

                        for (Category category :
                                categories) {

                            if (category.getId()
                                    == editingProduct
                                    .getCategoryId()) {

                                categoryBox.setValue(
                                        category
                                );

                                break;
                            }
                        }
                    }

                    categoryBox.setDisable(
                            false
                    );
                }
        );

        task.setOnFailed(
                event -> {

                    categoryBox.setDisable(
                            false
                    );

                    showError(
                            "Không tải được danh mục."
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

    private void save() {

        try {

            String name =
                    nameField
                            .getText()
                            .trim();

            if (name.isBlank()) {

                throw new IllegalArgumentException(
                        "Vui lòng nhập tên sản phẩm."
                );
            }

            double price;

            try {

                price =
                        Double.parseDouble(
                                priceField
                                        .getText()
                                        .trim()
                        );

            } catch (Exception e) {

                throw new IllegalArgumentException(
                        "Giá sản phẩm không hợp lệ."
                );
            }

            if (price < 0) {

                throw new IllegalArgumentException(
                        "Giá sản phẩm không được âm."
                );
            }

            int stock;

            try {

                stock =
                        Integer.parseInt(
                                stockField
                                        .getText()
                                        .trim()
                        );

            } catch (Exception e) {

                throw new IllegalArgumentException(
                        "Tồn kho phải là số nguyên."
                );
            }

            if (stock < 0) {

                throw new IllegalArgumentException(
                        "Tồn kho không được âm."
                );
            }

            if (editingProduct == null
                    && selectedImageFile == null) {

                throw new IllegalArgumentException(
                        "Vui lòng chọn ảnh cho sản phẩm mới."
                );
            }

            Category category =
                    categoryBox.getValue();

            Product product =
                    new Product();

            if (editingProduct != null) {

                product.setId(
                        editingProduct.getId()
                );
            }

            product.setName(
                    name
            );

            product.setDescription(
                    emptyToNull(
                            descriptionArea.getText()
                    )
            );

            product.setPrice(
                    price
            );

            product.setCategoryId(
                    category == null
                            ? null
                            : category.getId()
            );

            product.setSize(
                    emptyToNull(
                            sizeField.getText()
                    )
            );

            product.setColor(
                    emptyToNull(
                            colorField.getText()
                    )
            );

            product.setStock(
                    stock
            );

            if (editingProduct != null) {

                product.setImageUrl(
                        editingProduct.getImageUrl()
                );
            }

            product.setStatus(
                    statusBox.getValue()
            );

            saveProduct(
                    product
            );

        } catch (
                IllegalArgumentException e
        ) {

            showError(
                    e.getMessage()
            );
        }
    }

    private void saveProduct(
            Product product
    ) {

        setLoading(
                true
        );

        Task<Product> task =
                new Task<>() {

                    @Override
                    protected Product call()
                            throws Exception {

                        if (selectedImageFile != null) {

                            updateMessage(
                                    "Đang upload ảnh..."
                            );

                            String uploadedImagePath =
                                    imageService.upload(
                                            selectedImageFile
                                    );

                            product.setImageUrl(
                                    uploadedImagePath
                            );
                        }

                        updateMessage(
                                "Đang lưu sản phẩm..."
                        );

                        if (editingProduct == null) {

                            return adminService
                                    .createProduct(
                                            product
                                    );
                        }

                        return adminService
                                .updateProduct(
                                        product
                                );
                    }
                };

        task.setOnSucceeded(
                event -> {

                    setLoading(
                            false
                    );

                    Product saved =
                            task.getValue();

                    Alert alert =
                            new Alert(
                                    Alert.AlertType.INFORMATION
                            );

                    alert.setTitle(
                            "Fashion Shop"
                    );

                    alert.setHeaderText(
                            editingProduct == null
                                    ? "Thêm sản phẩm thành công"
                                    : "Cập nhật sản phẩm thành công"
                    );

                    alert.setContentText(
                            saved == null
                                    ? "Dữ liệu đã được lưu."
                                    : saved.getName()
                    );

                    alert.showAndWait();

                    if (onSaved != null) {

                        onSaved.run();
                    }

                    stage.close();
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
                                    ? "Không thể lưu sản phẩm."
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

    private void setLoading(
            boolean loading
    ) {

        Platform.runLater(
                () -> {

                    saveButton.setDisable(
                            loading
                    );

                    chooseImageButton.setDisable(
                            loading
                    );

                    progress.setVisible(
                            loading
                    );

                    progress.setManaged(
                            loading
                    );

                    if (!loading) {

                        messageLabel.setText(
                                ""
                        );
                    }
                }
        );
    }

    private void updateMessage(
            String message
    ) {

        Platform.runLater(
                () -> {

                    messageLabel.setStyle(
                            "-fx-text-fill: #6b7280;"
                    );

                    messageLabel.setText(
                            message
                    );
                }
        );
    }

    private void showError(
            String message
    ) {

        Platform.runLater(
                () -> {

                    messageLabel.setStyle(
                            "-fx-text-fill: #dc2626;"
                    );

                    messageLabel.setText(
                            message == null
                                    ? "Đã xảy ra lỗi."
                                    : message
                    );
                }
        );
    }

    private Label label(
            String text
    ) {

        Label label =
                new Label(
                        text
                );

        label.setStyle(
                "-fx-font-weight: bold;"
                        + "-fx-text-fill: #374151;"
        );

        return label;
    }

    private String safe(
            String value
    ) {

        return value == null
                ? ""
                : value;
    }

    private String emptyToNull(
            String value
    ) {

        if (value == null) {
            return null;
        }

        String text =
                value.trim();

        return text.isEmpty()
                ? null
                : text;
    }
}