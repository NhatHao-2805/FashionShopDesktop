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
import javafx.scene.layout.VBox;
import javafx.scene.layout.Region;
import java.util.Comparator;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.TreeSet;
import java.util.stream.Stream;

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

    private final ComboBox<String> sortBox = new ComboBox<>();
    private static final String ALL_COLORS = "Tất cả màu";
    private final ComboBox<String> colorBox = new ComboBox<>();
    private boolean catalogLoaded;
    private boolean catalogLoading;
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
        com.fashionshop.customer.view.PurchaseAccess.setLoginHandler(this::requireCustomerLogin);

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

        loginButton.setOnAction(event -> openLogin());
        ordersButton.setOnAction(event -> { if (requireCustomerLogin()) MyOrdersWindow.show(); });
        chatButton.setText("Chat với shop");
        chatButton.setOnAction(event -> { if (requireCustomerLogin()) CustomerChatWindow.show(); });
        cartButton.setOnAction(event -> { if (requireCustomerLogin()) CartWindow.show(); });
        logoutButton.setOnAction(event -> logout());
        loginButton.getStyleClass().add("header-button");
        ordersButton.getStyleClass().add("header-button");
        chatButton.getStyleClass().add("header-button");
        logoutButton.getStyleClass().add("header-button");
        cartButton.getStyleClass().add("cart-button");
        searchField.setMaxWidth(Double.MAX_VALUE);
        sortBox.getItems().setAll("Mặc định", "Giá: thấp đến cao", "Giá: cao đến thấp", "Tên: A → Z");
        sortBox.getSelectionModel().selectFirst();
        sortBox.setOnAction(event -> filterProducts());
        colorBox.getItems().add(ALL_COLORS);
        colorBox.setValue(ALL_COLORS);
        colorBox.setPrefWidth(150);
        colorBox.setOnAction(event -> filterProducts());
        productFlowPane.setHgap(18);
        productFlowPane.setVgap(26);
        productFlowPane.setPadding(new Insets(4, 0, 20, 0));
        updateCartButtonText();
    }

    private Label label(String text, String style) {
        Label label = new Label(text);
        label.getStyleClass().add(style);
        label.setWrapText(true);
        return label;
    }

    private Region spacer() {
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        return spacer;
    }

    private void showStorefront() {
        Label brand = label("Fashion Shop", "brand");
        userLabel.getStyleClass().add("header-caption");
        HBox navigation = new HBox(6, brand, spacer(), userLabel,
                loginButton, ordersButton, chatButton, logoutButton, cartButton);
        navigation.setAlignment(Pos.CENTER_LEFT);
        navigation.setMaxWidth(1280);
        VBox header = new VBox(navigation);
        header.setAlignment(Pos.CENTER);
        header.getStyleClass().add("shop-header");

        searchField.setPromptText("Tìm theo tên sản phẩm");
        Button searchButton = new Button("Tìm");
        searchButton.getStyleClass().add("soft-button");
        searchButton.setOnAction(event -> filterProducts());
        searchField.setOnAction(event -> filterProducts());
        HBox search = new HBox(8, searchField, searchButton);
        search.getStyleClass().add("search-box");
        HBox.setHgrow(searchField, Priority.ALWAYS);
        search.setPrefWidth(330);

        Button clear = new Button("Xóa bộ lọc");
        clear.getStyleClass().add("soft-button");
        clear.setOnAction(event -> {
            searchField.clear();
            categoryComboBox.getSelectionModel().clearSelection();
            colorBox.setValue(ALL_COLORS);
            sortBox.getSelectionModel().selectFirst();
            filterProducts();
        });
        HBox filters = new HBox(12, label("Danh mục", "muted"), categoryComboBox,
                label("Màu sắc", "muted"), colorBox,
                spacer(), label("Sắp xếp", "muted"), sortBox, clear);
        filters.setAlignment(Pos.CENTER_LEFT);
        filters.getStyleClass().add("filter-bar");
        HBox collectionTitle = new HBox(18, label("Sản phẩm", "section-title"), spacer(), search);
        collectionTitle.setAlignment(Pos.CENTER_LEFT);
        statusLabel.getStyleClass().add("muted");
        VBox content = new VBox(16, collectionTitle, filters, statusLabel, productFlowPane);
        content.getStyleClass().add("store-content");
        content.setMaxWidth(1280);
        VBox wrapper = new VBox(content);
        wrapper.setAlignment(Pos.TOP_CENTER);
        ScrollPane storefrontScroll = new ScrollPane(wrapper);
        storefrontScroll.setFitToWidth(true);
        storefrontScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        BorderPane root = new BorderPane(storefrontScroll);
        root.setTop(header);
        Scene scene = new Scene(root, 1320, 860);
        scene.getStylesheets().add(getClass().getResource("/com/fashionshop/customer/shop.css").toExternalForm());
        primaryStage.setTitle("Fashion Shop");
        primaryStage.setMinWidth(1000);
        primaryStage.setMinHeight(680);
        primaryStage.setScene(scene);
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
cartButton.setVisible(true);

        cartButton.setManaged(true);

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
        if (catalogLoading) return;
        catalogLoading = true;
        catalogLoaded = false;
        productFlowPane.getChildren().clear();

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

                                    var colors = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
                                    allProducts.stream().flatMap(CustomerApplication::productColors)
                                            .forEach(colors::add);
                                    colorBox.getItems().setAll(ALL_COLORS);
                                    colorBox.getItems().addAll(colors);
                                    colorBox.setValue(ALL_COLORS);

                                    catalogLoading = false;
                                    catalogLoaded = true;
                                    filterProducts();
                                }
                        );

                        return null;
                    }
                };

        task.setOnFailed(
                event -> {

                    catalogLoading = false;
                    catalogLoaded = false;
                    statusLabel.setText("Không thể tải sản phẩm từ "
                            + com.fashionshop.customer.config.ServerConfig.getBaseUrl());
                    Button retry = new Button("Thử tải lại");
                    retry.getStyleClass().add("primary-button");
                    retry.setOnAction(e -> loadData());
                    VBox error = new VBox(12,
                            label("Chưa kết nối được cửa hàng", "section-title"),
                            label("Hãy bật MySQL trong XAMPP và chạy server, sau đó nhấn Thử tải lại.", "muted"), retry);
                    error.getStyleClass().add("empty-state");
                    productFlowPane.getChildren().setAll(error);

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

    private static Stream<String> productColors(Product product) {
        String colors = product.getColor();
        return colors == null ? Stream.empty() : Arrays.stream(colors.split(","))
                .map(String::strip)
                .filter(color -> !color.isEmpty());
    }

    private void filterProducts() {
        if (!catalogLoaded) return;

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
        String selectedColor = colorBox.getValue();

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
                                            && categoryMatch
                                            && (selectedColor == null || ALL_COLORS.equals(selectedColor)
                                                    || productColors(product)
                                                    .anyMatch(selectedColor::equalsIgnoreCase));
                                }
                        )
                        .toList();

        List<Product> sorted = new ArrayList<>(filteredProducts);
        String sort = sortBox.getValue();
        if ("Giá: thấp đến cao".equals(sort)) sorted.sort(Comparator.comparingDouble(Product::getPrice));
        else if ("Giá: cao đến thấp".equals(sort)) sorted.sort(Comparator.comparingDouble(Product::getPrice).reversed());
        else if ("Tên: A → Z".equals(sort)) sorted.sort(Comparator.comparing(p -> p.getName() == null ? "" : p.getName(), String.CASE_INSENSITIVE_ORDER));
        displayProducts(sorted);
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

        if (products.isEmpty()) {
            VBox empty = new VBox(10, label("Chưa tìm thấy sản phẩm phù hợp", "section-title"),
                    label("Thử từ khóa khác hoặc xóa bộ lọc để xem thêm sản phẩm.", "muted"));
            empty.getStyleClass().add("empty-state");
            productFlowPane.getChildren().add(empty);
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
