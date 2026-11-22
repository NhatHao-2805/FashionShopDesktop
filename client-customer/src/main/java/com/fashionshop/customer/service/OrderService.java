package com.fashionshop.customer.service;

import com.fashionshop.customer.model.CartItem;
import com.fashionshop.customer.network.ApiClient;
import com.google.gson.Gson;
import com.google.gson.JsonParseException;

import java.math.BigDecimal;
import java.net.ConnectException;
import java.net.http.HttpConnectTimeoutException;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class OrderService {

    private final ApiClient apiClient;
    private final Gson gson;

    public OrderService() {

        this.apiClient =
                new ApiClient();

        this.gson =
                new Gson();
    }

    public OrderResult createOrder(
            String receiverName,
            String phone,
            String address,
            String note,
            List<CartItem> cartItems
    ) throws Exception {

        SessionManager.getInstance().requireCustomer();
        if (cartItems == null || cartItems.isEmpty()) {
            throw new IllegalArgumentException("Giỏ hàng đang trống.");
        }
        for (CartItem item : cartItems) {
            if (item == null || item.getProduct() == null || item.getQuantity() <= 0
                    || item.getQuantity() > item.getProduct().getStock()) {
                throw new IllegalArgumentException("Số lượng sản phẩm không hợp lệ. Vui lòng kiểm tra giỏ hàng.");
            }
        }

        CreateOrderRequest request =
                new CreateOrderRequest();

        request.receiverName =
                receiverName;

        request.phone =
                phone;

        request.address =
                address;

        request.note =
                note;

        request.paymentMethod =
                "COD";

        request.items =
                new ArrayList<>();

        for (CartItem cartItem :
                cartItems) {

            ItemRequest item =
                    new ItemRequest();

            item.productId =
                    cartItem
                            .getProduct()
                            .getId();

            item.quantity =
                    cartItem.getQuantity();

            item.expectedPrice = BigDecimal.valueOf(cartItem.getProduct().getPrice());
            item.selectedSize = cartItem.getSelectedSize();
            item.selectedColor = cartItem.getSelectedColor();
            request.items.add(
                    item
            );
        }

        String jsonRequest =
                gson.toJson(request);

        HttpResponse<String> reply;
        try {
            reply = apiClient.postResponse("/api/protected/orders", jsonRequest);
        } catch (Exception e) {
            if (e.getCause() instanceof ConnectException
                    || e.getCause() instanceof HttpConnectTimeoutException) {
                throw e;
            }
            throw new UnconfirmedOrderException(e);
        }

        if (reply.statusCode() >= 500) {
            throw new UnconfirmedOrderException(null);
        }
        ApiResponse response;
        try {
            response = gson.fromJson(reply.body(), ApiResponse.class);
        } catch (JsonParseException e) {
            throw new UnconfirmedOrderException(e);
        }
        if (reply.statusCode() >= 400 && reply.statusCode() < 500) {
            if (response != null && response.message != null && !response.message.isBlank()) {
                throw new IllegalStateException(response.message);
            }
            throw new IllegalStateException("Máy chủ từ chối đặt hàng (HTTP " + reply.statusCode() + ").");
        }
        if (reply.statusCode() < 200 || reply.statusCode() >= 300 || response == null
                || !response.success || response.data == null || response.data.getId() <= 0) {
            throw new UnconfirmedOrderException(null);
        }
        return response.data;
    }

    public static final class UnconfirmedOrderException extends Exception {
        private UnconfirmedOrderException(Throwable cause) {
            super("Chưa xác nhận được kết quả đặt hàng. Hãy kiểm tra Đơn hàng của tôi trước khi đặt lại.", cause);
        }
    }

    public List<OrderResult> getMyOrders()
            throws Exception {

        String jsonResponse =
                apiClient.get(
                        "/api/protected/orders"
                );

        OrderListResponse response =
                gson.fromJson(
                        jsonResponse,
                        OrderListResponse.class
                );

        if (response == null) {

            throw new IllegalStateException(
                    "Server không trả dữ liệu."
            );
        }

        if (!response.success) {

            throw new IllegalArgumentException(
                    response.message != null
                            ? response.message
                            : "Không thể lấy đơn hàng."
            );
        }

        if (response.data == null) {

            return Collections.emptyList();
        }

        return Arrays.asList(
                response.data
        );
    }

    public OrderResult getOrderDetail(
            long orderId
    ) throws Exception {

        if (orderId <= 0) {

            throw new IllegalArgumentException(
                    "ID đơn hàng không hợp lệ."
            );
        }

        String jsonResponse =
                apiClient.get(
                        "/api/protected/orders/"
                                + orderId
                );

        ApiResponse response =
                gson.fromJson(
                        jsonResponse,
                        ApiResponse.class
                );

        return requireData(
                response,
                "Không thể lấy chi tiết đơn hàng."
        );
    }

    public OrderResult cancelOrder(
            long orderId
    ) throws Exception {

        if (orderId <= 0) {

            throw new IllegalArgumentException(
                    "ID đơn hàng không hợp lệ."
            );
        }

        String jsonResponse =
                apiClient.put(
                        "/api/protected/orders/"
                                + orderId
                                + "/cancel",
                        "{}"
                );

        ApiResponse response =
                gson.fromJson(
                        jsonResponse,
                        ApiResponse.class
                );

        return requireData(
                response,
                "Không thể hủy đơn hàng."
        );
    }

    private OrderResult requireData(
            ApiResponse response,
            String fallbackMessage
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
                            : fallbackMessage
            );
        }

        if (response.data == null) {

            throw new IllegalStateException(
                    "Server không trả thông tin đơn hàng."
            );
        }

        return response.data;
    }

    private static class CreateOrderRequest {

        String receiverName;
        String phone;
        String address;
        String note;
        String paymentMethod;

        List<ItemRequest> items;
    }

    private static class ItemRequest {
        BigDecimal expectedPrice;

        String selectedSize;
        String selectedColor;
        long productId;
        int quantity;
    }

    private static class ApiResponse {

        boolean success;
        String message;
        OrderResult data;
    }

    private static class OrderListResponse {

        boolean success;
        String message;
        OrderResult[] data;
    }

    public static class OrderResult {

        private long id;
        private long userId;

        private BigDecimal totalAmount;

        private String receiverName;
        private String phone;
        private String address;
        private String note;

        private String paymentMethod;
        private String status;

        private List<OrderItemResult> items;

        public long getId() {
            return id;
        }

        public long getUserId() {
            return userId;
        }

        public BigDecimal getTotalAmount() {
            return totalAmount;
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

        public List<OrderItemResult> getItems() {

            if (items == null) {
                return Collections.emptyList();
            }

            return items;
        }
    }

    public static class OrderItemResult {

    private String selectedSize;
    private String selectedColor;
    public String getSelectedSize() { return selectedSize; }
    public String getSelectedColor() { return selectedColor; }
    public void setSelectedSize(String value) { selectedSize = value; }
    public void setSelectedColor(String value) { selectedColor = value; }


        private long productId;

        private String productName;

        private int quantity;

        private BigDecimal price;

        public long getProductId() {
            return productId;
        }

        public String getProductName() {
            return productName + " | " + java.util.Objects.toString(selectedSize, "—") + " / " + java.util.Objects.toString(selectedColor, "—");
        }

        public int getQuantity() {
            return quantity;
        }

        public BigDecimal getPrice() {
            return price;
        }

        public BigDecimal getSubtotal() {

            if (price == null) {
                return BigDecimal.ZERO;
            }

            return price.multiply(
                    BigDecimal.valueOf(
                            quantity
                    )
            );
        }
    }
}
