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
import com.fashionshop.customer.view.OperationStatus;
import com.fashionshop.customer.view.PageNavigation;
import com.fashionshop.customer.view.PurchaseAccess;

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

    private final OperationStatus catalogStatus = new OperationStatus();

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
    private final ComboBox<String> sizeBox = new ComboBox<>();
    private final TextField minPriceField = new TextField();
    private final TextField maxPriceField = new TextField();
    private boolean catalogLoaded;
    private boolean catalogLoading;

    private boolean cartListenerRegistered =
            false;

    @Override
    public void start(
            Stage stage
    ) {

        PageNavigation.initialize(stage);
        stage.setMinWidth(1000);
        stage.setMinHeight(680);
        stage.setWidth(1320);
        stage.setHeight(860);

        configureControls();

        showStorefront();

        registerCartListener();

        loadData();
    }

    private void configureControls() {
        PurchaseAccess.setLoginHandler(this::openLogin);

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

        loginButton.setOnAction(event -> openLogin(() -> { }));
        ordersButton.setOnAction(event -> PurchaseAccess.withCustomer(MyOrdersWindow::show));
        chatButton.setText("Chat với shop");
        chatButton.setOnAction(event -> PurchaseAccess.withCustomer(CustomerChatWindow::show));
        cartButton.setOnAction(event -> CartWindow.show());
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
        sizeBox.setPromptText("Tất cả size");
        sizeBox.setPrefWidth(130);
        sizeBox.setOnAction(event -> filterProducts());
        minPriceField.setPromptText("Giá từ");
        maxPriceField.setPromptText("Giá đến");
        minPriceField.setPrefWidth(105);
        maxPriceField.setPrefWidth(105);
        minPriceField.setOnAction(event -> filterProducts());
        maxPriceField.setOnAction(event -> filterProducts());
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
            sizeBox.setValue(null);
            minPriceField.clear();
            maxPriceField.clear();
            sortBox.getSelectionModel().selectFirst();
            filterProducts();
        });
        HBox filters = new HBox(10, label("Danh mục", "muted"), categoryComboBox,
                label("Màu sắc", "muted"), colorBox, label("Size", "muted"), sizeBox,
                label("Giá", "muted"), minPriceField, new Label("–"), maxPriceField,
                spacer(), label("Sắp xếp", "muted"), sortBox, clear);
        filters.setAlignment(Pos.CENTER_LEFT);
        filters.getStyleClass().add("filter-bar");
        HBox collectionTitle = new HBox(18, label("Sản phẩm", "section-title"), spacer(), search);
        collectionTitle.setAlignment(Pos.CENTER_LEFT);
        VBox content = new VBox(16, collectionTitle, filters, catalogStatus, productFlowPane);
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
        PageNavigation.reset("Fashion Shop", scene);
        refreshAuthenticationUi();
    }

    private void openLogin(Runnable customerAction) {
        new LoginWindow().show(() -> {
            if (sessionManager.isLoggedIn() && "ADMIN".equalsIgnoreCase(sessionManager.getCurrentUser().getRole())) {
                showAdminDashboard();
            } else {
                refreshAuthenticationUi();
                if (sessionManager.isCustomer()) {
                    customerAction.run();
                }
            }
        });
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
    }

    private void showAdminDashboard() {

        AdminDashboardWindow dashboard =
                new AdminDashboardWindow(
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

        catalogStatus.loading("Đang tải danh mục sản phẩm…");

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
                                    var sizes = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
                                    allProducts.stream().flatMap(CustomerApplication::productSizes)
                                            .forEach(sizes::add);
                                    sizeBox.getItems().setAll(sizes);
                                    sizeBox.getSelectionModel().clearSelection();

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
                    catalogStatus.error("Không tải được danh mục sản phẩm. Kiểm tra kết nối với máy chủ cửa hàng.",
                            this::loadData);

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

    private static Stream<String> productSizes(Product product) {
        String sizes = product.getSize();
        return sizes == null ? Stream.empty() : Arrays.stream(sizes.split(","))
                .map(String::trim).filter(value -> !value.isEmpty());
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
        String selectedSize = sizeBox.getValue();
        double minPrice = parsePrice(minPriceField.getText(), 0);
        double maxPrice = parsePrice(maxPriceField.getText(), Double.MAX_VALUE);
        final double upperPrice = Math.max(minPrice, maxPrice);

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
                                                    .anyMatch(selectedColor::equalsIgnoreCase))
                                            && (selectedSize == null
                                                    || productSizes(product).anyMatch(selectedSize::equalsIgnoreCase))
                                            && product.getPrice() >= minPrice
                                            && product.getPrice() <= upperPrice;
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

    private static double parsePrice(String value, double fallback) {
        if (value == null || value.isBlank()) return fallback;
        try {
            return Double.parseDouble(value.replace(".", "").replace(",", ".").trim());
        } catch (NumberFormatException ignored) {
            return fallback;
        }
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
            catalogStatus.empty(allProducts.isEmpty()
                    ? "Cửa hàng chưa có sản phẩm."
                    : "Không có sản phẩm phù hợp. Hãy đổi từ khóa hoặc xóa bộ lọc.");
        } else {
            catalogStatus.info("Đang hiển thị " + products.size() + " sản phẩm.");
        }
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
