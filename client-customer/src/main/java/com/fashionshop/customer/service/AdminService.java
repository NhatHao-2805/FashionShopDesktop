package com.fashionshop.customer.service;

import com.fashionshop.customer.model.Product;
import com.fashionshop.customer.network.ApiClient;

import com.google.gson.Gson;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class AdminService {

    private final ApiClient apiClient =
            new ApiClient();

    private final Gson gson =
            new Gson();

    // ============================================================
    // DASHBOARD
    // ============================================================

    public DashboardStats getDashboardStats()
            throws Exception {

        String json =
                apiClient.get(
                        "/api/admin/dashboard"
                );

        DashboardResponse response =
                gson.fromJson(
                        json,
                        DashboardResponse.class
                );

        validateResponse(
                response,
                "Không thể tải dashboard."
        );

        if (response.data == null) {

            throw new IllegalStateException(
                    "Dữ liệu dashboard không hợp lệ."
            );
        }

        return response.data;
    }


    // ============================================================
    // ORDERS
    // ============================================================

    public List<AdminOrder> getOrders()
            throws Exception {

        String json =
                apiClient.get(
                        "/api/admin/orders"
                );

        OrdersResponse response =
                gson.fromJson(
                        json,
                        OrdersResponse.class
                );

        validateResponse(
                response,
                "Không thể tải danh sách đơn hàng."
        );

        if (response.data == null) {

            return Collections.emptyList();
        }

        return Arrays.asList(
                response.data
        );
    }

    public AdminOrder updateOrderStatus(
            long orderId,
            String status
    ) throws Exception {

        StatusRequest request =
                new StatusRequest();

        request.status =
                status;

        String json =
                apiClient.put(
                        "/api/admin/orders/"
                                + orderId
                                + "/status",
                        gson.toJson(
                                request
                        )
                );

        OrderResponse response =
                gson.fromJson(
                        json,
                        OrderResponse.class
                );

        validateResponse(
                response,
                "Không thể cập nhật trạng thái đơn hàng."
        );

        if (response.data == null) {

            throw new IllegalStateException(
                    "Server không trả thông tin đơn hàng."
            );
        }

        return response.data;
    }


    // ============================================================
    // CUSTOMERS
    // ============================================================

    public List<Customer> getCustomers()
            throws Exception {

        String json =
                apiClient.get(
                        "/api/admin/customers"
                );

        CustomersResponse response =
                gson.fromJson(
                        json,
                        CustomersResponse.class
                );

        validateResponse(
                response,
                "Không thể tải danh sách khách hàng."
        );

        if (response.data == null) {

            return Collections.emptyList();
        }

        return Arrays.asList(
                response.data
        );
    }


    // ============================================================
    // PRODUCTS
    // ============================================================

    public List<Product> getProducts()
            throws Exception {

        String json =
                apiClient.get(
                        "/api/admin/products"
                );

        ProductsResponse response =
                gson.fromJson(
                        json,
                        ProductsResponse.class
                );

        validateResponse(
                response,
                "Không thể tải danh sách sản phẩm."
        );

        if (response.data == null) {

            return Collections.emptyList();
        }

        return Arrays.asList(
                response.data
        );
    }

    public Product createProduct(
            Product product
    ) throws Exception {

        String json =
                apiClient.post(
                        "/api/admin/products",
                        gson.toJson(
                                product
                        )
                );

        ProductResponse response =
                gson.fromJson(
                        json,
                        ProductResponse.class
                );

        validateResponse(
                response,
                "Không thể thêm sản phẩm."
        );

        if (response.data == null) {

            throw new IllegalStateException(
                    "Server không trả sản phẩm vừa tạo."
            );
        }

        return response.data;
    }

    public Product updateProduct(
            Product product
    ) throws Exception {

        if (product == null
                || product.getId() <= 0) {

            throw new IllegalArgumentException(
                    "Sản phẩm không hợp lệ."
            );
        }

        String json =
                apiClient.put(
                        "/api/admin/products/"
                                + product.getId(),
                        gson.toJson(
                                product
                        )
                );

        ProductResponse response =
                gson.fromJson(
                        json,
                        ProductResponse.class
                );

        validateResponse(
                response,
                "Không thể cập nhật sản phẩm."
        );

        if (response.data == null) {

            throw new IllegalStateException(
                    "Server không trả dữ liệu sản phẩm."
            );
        }

        return response.data;
    }

    public Product changeProductStatus(
            long productId,
            String status
    ) throws Exception {

        StatusRequest request =
                new StatusRequest();

        request.status =
                status;

        String json =
                apiClient.put(
                        "/api/admin/products/"
                                + productId
                                + "/status",
                        gson.toJson(
                                request
                        )
                );

        ProductResponse response =
                gson.fromJson(
                        json,
                        ProductResponse.class
                );

        validateResponse(
                response,
                "Không thể cập nhật trạng thái sản phẩm."
        );

        if (response.data == null) {

            throw new IllegalStateException(
                    "Server không trả dữ liệu sản phẩm."
            );
        }

        return response.data;
    }


    // ============================================================
    // RESPONSE VALIDATION
    // ============================================================

    private void validateResponse(
            BaseResponse response,
            String fallback
    ) {

        if (response == null) {

            throw new IllegalStateException(
                    "Server không trả dữ liệu."
            );
        }

        if (!response.success) {

            throw new IllegalArgumentException(
                    response.message != null
                            ? response.message
                            : fallback
            );
        }
    }


    // ============================================================
    // RESPONSE TYPES
    // ============================================================

    private static class BaseResponse {

        boolean success;

        String message;
    }

    private static class DashboardResponse
            extends BaseResponse {

        DashboardStats data;
    }

    private static class OrdersResponse
            extends BaseResponse {

        AdminOrder[] data;
    }

    private static class OrderResponse
            extends BaseResponse {

        AdminOrder data;
    }

    private static class CustomersResponse
            extends BaseResponse {

        Customer[] data;
    }

    private static class ProductsResponse
            extends BaseResponse {

        Product[] data;
    }

    private static class ProductResponse
            extends BaseResponse {

        Product data;
    }

    private static class StatusRequest {

        String status;
    }


    // ============================================================
    // DASHBOARD MODEL
    // ============================================================

    public static class DashboardStats {

        private long totalCustomers;
        private long totalProducts;
        private long totalStock;
        private long lowStockProducts;

        private long totalOrders;
        private long pendingOrders;
        private long processingOrders;
        private long shippingOrders;
        private long deliveredOrders;
        private long cancelledOrders;

        private BigDecimal deliveredRevenue;

        public long getTotalCustomers() {
            return totalCustomers;
        }

        public long getTotalProducts() {
            return totalProducts;
        }

        public long getTotalStock() {
            return totalStock;
        }

        public long getLowStockProducts() {
            return lowStockProducts;
        }

        public long getTotalOrders() {
            return totalOrders;
        }

        public long getPendingOrders() {
            return pendingOrders;
        }

        public long getProcessingOrders() {
            return processingOrders;
        }

        public long getShippingOrders() {
            return shippingOrders;
        }

        public long getDeliveredOrders() {
            return deliveredOrders;
        }

        public long getCancelledOrders() {
            return cancelledOrders;
        }

        public BigDecimal getDeliveredRevenue() {

            return deliveredRevenue == null
                    ? BigDecimal.ZERO
                    : deliveredRevenue;
        }
    }


    // ============================================================
    // ORDER MODEL
    // ============================================================

    public static class AdminOrder {

        private long id;
        private long userId;

        private String customerName;
        private String customerEmail;

        private BigDecimal totalAmount;

        private String receiverName;
        private String phone;
        private String address;
        private String note;

        private String paymentMethod;
        private String status;

        public long getId() {
            return id;
        }

        public long getUserId() {
            return userId;
        }

        public String getCustomerName() {
            return customerName;
        }

        public String getCustomerEmail() {
            return customerEmail;
        }

        public BigDecimal getTotalAmount() {

            return totalAmount == null
                    ? BigDecimal.ZERO
                    : totalAmount;
        }

        public String getReceiverName() {
            return receiverName;
        }

        public String getPhone() {
            return phone;
        }

        public String getAddress() {
            return address;
        }

        public String getNote() {
            return note;
        }

        public String getPaymentMethod() {
            return paymentMethod;
        }

        public String getStatus() {
            return status;
        }
    }


    // ============================================================
    // CUSTOMER MODEL
    // ============================================================

    public static class Customer {

        private long id;

        private String name;
        private String email;
        private String phone;
        private String status;

        public long getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public String getEmail() {
            return email;
        }

        public String getPhone() {
            return phone;
        }

        public String getStatus() {
            return status;
        }
    }
}