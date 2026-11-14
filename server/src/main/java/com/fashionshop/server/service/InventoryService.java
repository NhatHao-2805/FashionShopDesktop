package com.fashionshop.server.service;

import com.fashionshop.server.repository.InventoryRepository;

import java.util.List;
import java.util.Locale;
import java.util.Map;

public class InventoryService {

    private final InventoryRepository repository;

    public InventoryService() {

        this.repository =
                new InventoryRepository();
    }

    public List<Map<String, Object>> getStockOverview() {

        return repository.findStockOverview();
    }

    public List<Map<String, Object>> getTransactions(
            int limit
    ) {

        if (limit <= 0) {

            limit = 100;
        }

        if (limit > 500) {

            limit = 500;
        }

        return repository.findTransactions(
                limit
        );
    }

    public Map<String, Object> adjustStock(
            long productId,
            long userId,
            String type,
            int quantity,
            String reason
    ) {

        if (productId <= 0) {

            throw new IllegalArgumentException(
                    "ID sản phẩm không hợp lệ."
            );
        }

        if (userId <= 0) {

            throw new IllegalArgumentException(
                    "Tài khoản quản trị không hợp lệ."
            );
        }

        if (quantity <= 0) {

            throw new IllegalArgumentException(
                    "Số lượng phải lớn hơn 0."
            );
        }

        if (type == null
                || type.isBlank()) {

            throw new IllegalArgumentException(
                    "Vui lòng chọn loại giao dịch kho."
            );
        }

        String normalizedType =
                type.trim()
                        .toUpperCase(
                                Locale.ROOT
                        );

        if (!normalizedType.equals("IMPORT")
                && !normalizedType.equals("EXPORT")
                && !normalizedType.equals("ADJUST_IN")
                && !normalizedType.equals("ADJUST_OUT")) {

            throw new IllegalArgumentException(
                    "Loại giao dịch kho không hợp lệ."
            );
        }

        if (reason == null
                || reason.isBlank()) {

            throw new IllegalArgumentException(
                    "Vui lòng nhập lý do điều chỉnh kho."
            );
        }

        String normalizedReason =
                reason.trim();

        if (normalizedReason.length() > 500) {

            throw new IllegalArgumentException(
                    "Lý do tối đa 500 ký tự."
            );
        }

        return repository.adjustStock(
                productId,
                userId,
                normalizedType,
                quantity,
                normalizedReason
        );
    }
}