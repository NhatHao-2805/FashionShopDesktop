package com.fashionshop.customer;

import com.fashionshop.customer.model.Category;
import com.fashionshop.customer.model.Product;
import com.fashionshop.customer.model.User;
import com.fashionshop.customer.service.CartService;
import com.fashionshop.customer.service.CategoryService;
import com.fashionshop.customer.service.ProductService;
import com.fashionshop.customer.service.SessionManager;
import com.fashionshop.customer.view.AdminDashboardWindow;
import com.fashionshop.customer.view.CartWindow;
import com.fashionshop.customer.view.CustomerChatWindow;
import com.fashionshop.customer.view.LoginWindow;
import com.fashionshop.customer.view.MyOrdersWindow;
import com.fashionshop.customer.view.ProductCard;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;

public class CustomerApplication extends Application {

    private final ProductService productService =
            new ProductService();

    private final CategoryService categoryService =
            new CategoryService();

    private final CartService cartService =
            CartService.getInstance();

    private final SessionManager sessionManager =
            SessionManager.getInstance();

    private final FlowPane productFlowPane =
            new FlowPane();

    private final ComboBox<Category> categoryComboBox =
            new ComboBox<>();

    private final TextField searchField =
            new TextField();

    private final Label statusLabel =
            new Label("Đang tải dữ liệu...");

    private final Label userLabel =
            new Label();

    private final Button loginButton =
            new Button("Đăng nhập");

    private final Button ordersButton =
            new Button("Đơn hàng");

    
    private final Button chatButton =
            new Button("💬 Chat");
private final Button cartButton =
            new Button();

    private final Button logoutButton =
            new Button("Đăng xuất");

    private List<Product> allProducts =
            new ArrayList<>();

    private Stage primaryStage;

    private boolean cartListenerRegistered =
            false;

    @Override
    public void start(
            Stage stage
    ) {

        this.primaryStage =
                stage;

        configureControls();

        showStorefront();

        registerCartListener();

        loadData();
    }

    private void configureControls() {

        searchField.setPromptText(
                "Tìm kiếm sản phẩm..."
        );

        searchField.setPrefWidth(
                250
        );

        searchField.textProperty()
                .addListener(
                        (observable, oldValue, newValue) ->
                                filterProducts()
                );

        categoryComboBox.setPromptText(
                "Tất cả"
        );

        categoryComboBox.setPrefWidth(
                160
        );

        categoryComboBox.setOnAction(
                event -> filterProducts()
        );

        loginButton.setStyle(
                "-fx-background-color: #2563eb;"
                        + "-fx-text-fill: white;"
                        + "-fx-font-weight: bold;"
                        + "-fx-background-radius: 8;"
        );

        loginButton.setOnAction(
                event -> openLogin()
        );

        ordersButton.setStyle(
                "-fx-background-color: #1976d2;"
                        + "-fx-text-fill: white;"
                        + "-fx-font-weight: bold;"
                        + "-fx-background-radius: 8;"
        );

        ordersButton.setOnAction(
                event -> {

                    if (!requireCustomerLogin()) {
                        return;
                    }

                    MyOrdersWindow.show();
                }
        );
        // =========================================================
        // CHAT WITH SHOP
        // =========================================================

        chatButton.setStyle(
                "-fx-background-color: #7c3aed;"
                        + "-fx-text-fill: white;"
                        + "-fx-font-weight: bold;"
                        + "-fx-background-radius: 8;"
        );

        chatButton.setTooltip(
                new javafx.scene.control.Tooltip(
                        "Chat với shop"
                )
        );

        chatButton.setOnAction(
                event -> {

                    if (!requireCustomerLogin()) {
                        return;
                    }

                    CustomerChatWindow.show();
                }
        );


        cartButton.setStyle(
                "-fx-background-color: #d32f2f;"
                        + "-fx-text-fill: white;"
                        + "-fx-font-weight: bold;"
                        + "-fx-background-radius: 8;"
        );

        cartButton.setOnAction(
                event -> {

                    if (!requireCustomerLogin()) {
                        return;
                    }

                    CartWindow.show();
                }
        );

        logoutButton.setStyle(
                "-fx-background-color: #374151;"
                        + "-fx-text-fill: white;"
                        + "-fx-font-weight: bold;"
                        + "-fx-background-radius: 8;"
        );

        logoutButton.setOnAction(
                event -> logout()
        );

        productFlowPane.setHgap(
                15
        );

        productFlowPane.setVgap(
                15
        );

        productFlowPane.setPadding(
                new Insets(10)
        );

        updateCartButtonText();
    }

    private void showStorefront() {

        Label titleLabel =
                new Label("FASHION SHOP");

        titleLabel.setStyle(
                "-fx-font-size: 28px;"
                        + "-fx-font-weight: bold;"
                        + "-fx-text-fill: #111827;"
        );

        Label categoryLabel =
                new Label("Danh mục:");

        HBox spacer =
                new HBox();

        HBox.setHgrow(
                spacer,
                Priority.ALWAYS
        );

        HBox topBox =
                new HBox(
                        12,
                        titleLabel,
                        spacer,
                        searchField,
                        categoryLabel,
                        categoryComboBox,
                        userLabel,
                        loginButton,
                        ordersButton,
                        chatButton,
                        cartButton,
                        logoutButton
                );

        topBox.setAlignment(
                Pos.CENTER_LEFT
        );

        topBox.setPadding(
                new Insets(
                        14
                )
        );

        topBox.setStyle(
                "-fx-background-color: white;"
                        + "-fx-border-color: transparent transparent #e5e7eb transparent;"
        );

        ScrollPane scrollPane =
                new ScrollPane(
                        productFlowPane
                );

        scrollPane.setFitToWidth(
                true
        );

        scrollPane.setStyle(
                "-fx-background-color: #f9fafb;"
                        + "-fx-background: #f9fafb;"
        );

        BorderPane root =
                new BorderPane();

        root.setStyle(
                "-fx-background-color: #f9fafb;"
        );

        root.setTop(
                topBox
        );

        root.setCenter(
                scrollPane
        );

        root.setBottom(
                statusLabel
        );

        BorderPane.setMargin(
                statusLabel,
                new Insets(
                        12
                )
        );

        Scene scene =
                new Scene(
                        root,
                        1400,
                        760
                );

        primaryStage.setTitle(
                "Fashion Shop"
        );

        primaryStage.setScene(
                scene
        );

        primaryStage.show();

        refreshAuthenticationUi();
    }

    private void openLogin() {

        LoginWindow loginWindow =
                new LoginWindow();

        boolean loginSuccess =
                loginWindow.showAndWait();

        if (!loginSuccess) {
            return;
        }

        User user =
                sessionManager.getCurrentUser();

        if (user == null) {
            return;
        }

        if ("ADMIN".equalsIgnoreCase(
                user.getRole()
        )) {

            showAdminDashboard();

            return;
        }

        refreshAuthenticationUi();
    }

    private boolean requireCustomerLogin() {

        User user =
                sessionManager.getCurrentUser();

        if (user == null) {

            openLogin();

            user =
                    sessionManager.getCurrentUser();
        }

        if (user == null) {
            return false;
        }

        if ("ADMIN".equalsIgnoreCase(
                user.getRole()
        )) {

            showAdminDashboard();

            return false;
        }

        return "CUSTOMER".equalsIgnoreCase(
                user.getRole()
        );
    }

    private void refreshAuthenticationUi() {

        User user =
                sessionManager.getCurrentUser();

        boolean loggedIn =
                user != null;

        boolean customer =
                loggedIn
                        && "CUSTOMER".equalsIgnoreCase(
                        user.getRole()
                );

        loginButton.setVisible(
                !loggedIn
        );

        loginButton.setManaged(
                !loggedIn
        );

        logoutButton.setVisible(
                loggedIn
        );

        logoutButton.setManaged(
                loggedIn
        );

        ordersButton.setVisible(
                customer
        );

        ordersButton.setManaged(
                customer
        );

        
        chatButton.setVisible(
                customer
        );

        chatButton.setManaged(
                customer
        );
cartButton.setVisible(
                customer
        );

        cartButton.setManaged(
                customer
        );

        if (loggedIn) {

            userLabel.setText(
                    "Xin chào, "
                            + user.getName()
            );

        } else {

            userLabel.setText(
                    "Khách"
            );
        }
    }

    private void logout() {

        sessionManager.logout();

        cartService.clear();

        refreshAuthenticationUi();

        statusLabel.setText(
                "Bạn đã đăng xuất. Bạn vẫn có thể xem sản phẩm."
        );
    }

    private void showAdminDashboard() {

        AdminDashboardWindow dashboard =
                new AdminDashboardWindow(
                        primaryStage,
                        () -> {

                            sessionManager.logout();

                            cartService.clear();

                            showStorefront();

                            loadData();
                        }
                );

        dashboard.show();
    }

    private void registerCartListener() {

        if (cartListenerRegistered) {
            return;
        }

        cartService.getItems()
                .addListener(
                        (ListChangeListener<Object>) change ->
                                Platform.runLater(
                                        this::updateCartButtonText
                                )
                );

        cartListenerRegistered =
                true;
    }

    private void loadData() {

        statusLabel.setText(
                "Đang tải dữ liệu..."
        );

        Task<Void> task =
                new Task<>() {

                    @Override
                    protected Void call() {

                        allProducts =
                                productService
                                        .getAllProducts();

                        List<Category> categories =
                                categoryService
                                        .getAllCategories();

                        Platform.runLater(
                                () -> {

                                    categoryComboBox
                                            .getItems()
                                            .setAll(
                                                    categories
                                            );

                                    displayProducts(
                                            allProducts
                                    );
                                }
                        );

                        return null;
                    }
                };

        task.setOnFailed(
                event -> {

                    statusLabel.setText(
                            "Không thể tải dữ liệu từ server."
                    );

                    if (task.getException()
                            != null) {

                        task.getException()
                                .printStackTrace();
                    }
                }
        );

        Thread thread =
                new Thread(task);

        thread.setDaemon(
                true
        );

        thread.start();
    }

    private void filterProducts() {

        String keyword =
                searchField.getText()
                        == null
                        ? ""
                        : searchField
                        .getText()
                        .trim()
                        .toLowerCase();

        Category selectedCategory =
                categoryComboBox.getValue();

        List<Product> filteredProducts =
                allProducts.stream()
                        .filter(
                                product -> {

                                    String productName =
                                            product.getName()
                                                    == null
                                                    ? ""
                                                    : product
                                                    .getName()
                                                    .toLowerCase();

                                    String description =
                                            product.getDescription()
                                                    == null
                                                    ? ""
                                                    : product
                                                    .getDescription()
                                                    .toLowerCase();

                                    boolean keywordMatch =
                                            keyword.isEmpty()
                                                    || productName
                                                    .contains(
                                                            keyword
                                                    )
                                                    || description
                                                    .contains(
                                                            keyword
                                                    );

                                    boolean categoryMatch =
                                            selectedCategory
                                                    == null
                                                    || (
                                                    product.getCategoryId()
                                                            != null
                                                            && product
                                                            .getCategoryId()
                                                            .longValue()
                                                            == selectedCategory
                                                            .getId()
                                            );

                                    return keywordMatch
                                            && categoryMatch;
                                }
                        )
                        .toList();

        displayProducts(
                filteredProducts
        );
    }

    private void displayProducts(
            List<Product> products
    ) {

        productFlowPane
                .getChildren()
                .clear();

        for (Product product :
                products) {

            ProductCard card =
                    new ProductCard(
                            product
                    );

            productFlowPane
                    .getChildren()
                    .add(
                            card
                    );
        }

        statusLabel.setText(
                "Đang hiển thị "
                        + products.size()
                        + " sản phẩm."
        );
    }

    private void updateCartButtonText() {

        cartButton.setText(
                "Giỏ hàng ("
                        + cartService
                        .getTotalQuantity()
                        + ")"
        );
    }

    public static void main(
            String[] args
    ) {

        launch(args);
    }
}