package com.fashionshop.server.service;

import com.fashionshop.server.repository.AdminRepository;

import java.util.List;
import java.util.Locale;
import java.util.Map;

public class AdminService {

    private final AdminRepository adminRepository;

    public AdminService() {

        this.adminRepository =
                new AdminRepository();
    }

    public Map<String, Object>
    getDashboardStats() {

        return adminRepository
                .getDashboardStats();
    }

    public List<Map<String, Object>>
    getOrders() {

        return adminRepository
                .findAllOrders();
    }

    public List<Map<String, Object>>
    getCustomers() {

        return adminRepository
                .findCustomers();
    }

    public Map<String, Object>
    updateOrderStatus(
            long orderId,
            String newStatus
    ) {

        if (orderId <= 0) {

            throw new IllegalArgumentException(
                    "ID đơn hàng không hợp lệ."
            );
        }

        if (newStatus == null
                || newStatus.isBlank()) {

            throw new IllegalArgumentException(
                    "Trạng thái mới không hợp lệ."
            );
        }

        String normalized =
                newStatus.trim()
                        .toUpperCase(
                                Locale.ROOT
                        );

        String currentStatus =
                adminRepository
                        .findOrderStatus(
                                orderId
                        );

        if (currentStatus == null) {

            throw new IllegalArgumentException(
                    "Không tìm thấy đơn hàng."
            );
        }

        validateTransition(
                currentStatus,
                normalized
        );

        boolean updated =
                adminRepository
                        .updateOrderStatus(
                                orderId,
                                currentStatus,
                                normalized
                        );

        if (!updated) {

            throw new IllegalStateException(
                    "Đơn hàng vừa thay đổi. Hãy tải lại dữ liệu."
            );
        }

        Map<String, Object> order =
                adminRepository
                        .findOrderById(
                                orderId
                        );

        if (order == null) {

            throw new IllegalStateException(
                    "Không thể tải lại đơn hàng."
            );
        }

        return order;
    }

    private void validateTransition(
            String currentStatus,
            String newStatus
    ) {

        String current =
                currentStatus.toUpperCase(
                        Locale.ROOT
                );

        if (current.equals(newStatus)) {

            throw new IllegalArgumentException(
                    "Đơn hàng đã ở trạng thái này."
            );
        }

        boolean valid =
                switch (current) {

                    case "PENDING" ->
                            "PROCESSING".equals(
                                    newStatus
                            );

                    case "PROCESSING" ->
                            "SHIPPING".equals(
                                    newStatus
                            );

                    case "SHIPPING" ->
                            "DELIVERED".equals(
                                    newStatus
                            );

                    default ->
                            false;
                };

        if (!valid) {

            throw new IllegalArgumentException(
                    "Không thể chuyển trạng thái từ "
                            + current
                            + " sang "
                            + newStatus
                            + "."
            );
        }
    }
}