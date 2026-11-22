package com.fashionshop.customer.service;

import com.fashionshop.customer.network.ApiClient;
import com.google.gson.Gson;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class InventoryService {

    private final ApiClient apiClient =
            new ApiClient();

    private final Gson gson =
            new Gson();

    public record Variant(long id, String size, String color, int stock) {}
    public record Allocation(String size, String color, int stock) {}
    public record VariantSnapshot(int total, String sizes, String colors, List<Variant> variants) {}
    private record AllocationRequest(VariantSnapshot expected, List<Allocation> allocations) {}

    public VariantSnapshot getVariants(long productId) throws Exception {
        return readVariants(apiClient.get("/api/admin/inventory/" + productId + "/variants"));
    }

    public VariantSnapshot allocate(long productId, VariantSnapshot expected, List<Allocation> allocations)
            throws Exception {
        return readVariants(apiClient.post("/api/admin/inventory/" + productId + "/variants",
                gson.toJson(new AllocationRequest(expected, allocations))));
    }

    private VariantSnapshot readVariants(String json) {
        var response = gson.fromJson(json, VariantResponse.class);
        validate(response, "Không thể tải tồn kho size/màu.");
        return response.data;
    }

    public List<StockItem> getStockOverview()
            throws Exception {

        String json =
                apiClient.get(
                        "/api/admin/inventory"
                );

        StockResponse response =
                gson.fromJson(
                        json,
                        StockResponse.class
                );

        validate(
                response,
                "Không thể tải dữ liệu kho."
        );

        if (response.data == null) {
            return Collections.emptyList();
        }

        return Arrays.asList(
                response.data
        );
    }

    public List<InventoryTransaction> getTransactions(
            int limit
    ) throws Exception {

        String json =
                apiClient.get(
                        "/api/admin/inventory/transactions?limit="
                                + limit
                );

        TransactionResponse response =
                gson.fromJson(
                        json,
                        TransactionResponse.class
                );

        validate(
                response,
                "Không thể tải lịch sử kho."
        );

        if (response.data == null) {
            return Collections.emptyList();
        }

        return Arrays.asList(
                response.data
        );
    }

    public AdjustmentResult adjustStock(
            long productId,
            String type,
            int quantity,
            String reason
    ) throws Exception {

        AdjustmentRequest request =
                new AdjustmentRequest();

        request.productId =
                productId;

        request.type =
                type;

        request.quantity =
                quantity;

        request.reason =
                reason;

        String json =
                apiClient.post(
                        "/api/admin/inventory/adjust",
                        gson.toJson(
                                request
                        )
                );

        AdjustmentResponse response =
                gson.fromJson(
                        json,
                        AdjustmentResponse.class
                );

        validate(
                response,
                "Không thể điều chỉnh kho."
        );

        if (response.data == null) {

            throw new IllegalStateException(
                    "Server không trả dữ liệu giao dịch kho."
            );
        }

        return response.data;
    }

    private void validate(
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
                    response.message == null
                            ? fallback
                            : response.message
            );
        }
    }

    private static class BaseResponse {

        boolean success;
        String message;
    }

    private static class VariantResponse extends BaseResponse {
        VariantSnapshot data;
    }

    private static class StockResponse
            extends BaseResponse {

        StockItem[] data;
    }

    private static class TransactionResponse
            extends BaseResponse {

        InventoryTransaction[] data;
    }

    private static class AdjustmentResponse
            extends BaseResponse {

        AdjustmentResult data;
    }

    private static class AdjustmentRequest {

        long productId;
        String type;
        int quantity;
        String reason;
    }

    public static class StockItem {

        private long productId;

        private String productName;

        private Long categoryId;

        private String categoryName;

        private int stock;

        private String status;

        private boolean lowStock;

        public long getProductId() {
            return productId;
        }

        public String getProductName() {
            return productName;
        }

        public Long getCategoryId() {
            return categoryId;
        }

        public String getCategoryName() {
            return categoryName;
        }

        public int getStock() {
            return stock;
        }

        public String getStatus() {
            return status;
        }

        public boolean isLowStock() {
            return lowStock;
        }
    }

    public static class InventoryTransaction {

        private long id;

        private long productId;

        private String productName;

        private Long userId;

        private String userName;

        private String type;

        private int quantity;

        private int stockBefore;

        private int stockAfter;

        private String reason;

        private String createdAt;

        public long getId() {
            return id;
        }

        public long getProductId() {
            return productId;
        }

        public String getProductName() {
            return productName;
        }

        public Long getUserId() {
            return userId;
        }

        public String getUserName() {
            return userName;
        }

        public String getType() {
            return type;
        }

        public int getQuantity() {
            return quantity;
        }

        public int getStockBefore() {
            return stockBefore;
        }

        public int getStockAfter() {
            return stockAfter;
        }

        public String getReason() {
            return reason;
        }

        public String getCreatedAt() {
            return createdAt;
        }
    }

    public static class AdjustmentResult {

        private long transactionId;

        private long productId;

        private String productName;

        private String type;

        private int quantity;

        private int stockBefore;

        private int stockAfter;

        private String reason;

        public long getTransactionId() {
            return transactionId;
        }

        public long getProductId() {
            return productId;
        }

        public String getProductName() {
            return productName;
        }

        public String getType() {
            return type;
        }

        public int getQuantity() {
            return quantity;
        }

        public int getStockBefore() {
            return stockBefore;
        }

        public int getStockAfter() {
            return stockAfter;
        }

        public String getReason() {
            return reason;
        }
    }
}
