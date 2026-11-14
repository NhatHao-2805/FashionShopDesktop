package com.fashionshop.admin.service;

import com.fashionshop.admin.network.ApiClient;

import com.google.gson.Gson;

import java.math.BigDecimal;

public class AdminService {

    private final ApiClient apiClient;

    private final Gson gson;

    public AdminService() {

        this.apiClient =
                new ApiClient();

        this.gson =
                new Gson();
    }

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

        if (response == null) {

            throw new IllegalStateException(
                    "Server không trả dữ liệu."
            );
        }

        if (!response.success) {

            throw new IllegalArgumentException(
                    response.message != null
                            ? response.message
                            : "Không thể tải dashboard."
            );
        }

        if (response.data == null) {

            throw new IllegalStateException(
                    "Dữ liệu dashboard không hợp lệ."
            );
        }

        return response.data;
    }

    private static class DashboardResponse {

        boolean success;

        String message;

        DashboardStats data;
    }

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
}