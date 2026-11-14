package com.fashionshop.server.service;

import com.fashionshop.server.model.Product;
import com.fashionshop.server.repository.AdminProductRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;

public class AdminProductService {

    private final AdminProductRepository repository;

    public AdminProductService() {

        this.repository =
                new AdminProductRepository();
    }

    public List<Product> getProducts() {

        return repository.findAll();
    }

    public Product createProduct(
            Product product
    ) {

        validateProduct(
                product
        );

        product.setStatus(
                normalizeStatus(
                        product.getStatus()
                )
        );

        return repository.create(
                product
        );
    }

    public Product updateProduct(
            long productId,
            Product product
    ) {

        if (productId <= 0) {

            throw new IllegalArgumentException(
                    "ID sản phẩm không hợp lệ."
            );
        }

        Product existing =
                repository.findById(
                        productId
                );

        if (existing == null) {

            throw new IllegalArgumentException(
                    "Không tìm thấy sản phẩm."
            );
        }

        product.setId(
                productId
        );

        validateProduct(
                product
        );

        product.setStatus(
                normalizeStatus(
                        product.getStatus()
                )
        );

        Product updated =
                repository.update(
                        product
                );

        if (updated == null) {

            throw new IllegalStateException(
                    "Không thể cập nhật sản phẩm."
            );
        }

        return updated;
    }

    public Product changeStatus(
            long productId,
            String status
    ) {

        if (productId <= 0) {

            throw new IllegalArgumentException(
                    "ID sản phẩm không hợp lệ."
            );
        }

        if (repository.findById(
                productId
        ) == null) {

            throw new IllegalArgumentException(
                    "Không tìm thấy sản phẩm."
            );
        }

        Product updated =
                repository.updateStatus(
                        productId,
                        normalizeStatus(
                                status
                        )
                );

        if (updated == null) {

            throw new IllegalStateException(
                    "Không thể cập nhật trạng thái sản phẩm."
            );
        }

        return updated;
    }

    private void validateProduct(
            Product product
    ) {

        if (product == null) {

            throw new IllegalArgumentException(
                    "Dữ liệu sản phẩm không hợp lệ."
            );
        }

        if (product.getName() == null
                || product.getName()
                .isBlank()) {

            throw new IllegalArgumentException(
                    "Tên sản phẩm không được để trống."
            );
        }

        product.setName(
                product.getName()
                        .trim()
        );

        if (product.getPrice() == null
                || product.getPrice()
                .compareTo(
                        BigDecimal.ZERO
                ) < 0) {

            throw new IllegalArgumentException(
                    "Giá sản phẩm không hợp lệ."
            );
        }

        if (product.getStock() < 0) {

            throw new IllegalArgumentException(
                    "Tồn kho không được âm."
            );
        }

        if (product.getCategoryId() > 0
                && !repository.categoryExists(
                product.getCategoryId()
        )) {

            throw new IllegalArgumentException(
                    "Danh mục sản phẩm không tồn tại."
            );
        }

        product.setDescription(
                clean(
                        product.getDescription()
                )
        );

        product.setSize(
                clean(
                        product.getSize()
                )
        );

        product.setColor(
                clean(
                        product.getColor()
                )
        );

        product.setImageUrl(
                clean(
                        product.getImageUrl()
                )
        );
    }

    private String normalizeStatus(
            String status
    ) {

        if (status == null
                || status.isBlank()) {

            return "ACTIVE";
        }

        String normalized =
                status.trim()
                        .toUpperCase(
                                Locale.ROOT
                        );

        if (!"ACTIVE".equals(
                normalized
        )
                && !"INACTIVE".equals(
                normalized
        )) {

            throw new IllegalArgumentException(
                    "Trạng thái sản phẩm chỉ được ACTIVE hoặc INACTIVE."
            );
        }

        return normalized;
    }

    private String clean(
            String value
    ) {

        if (value == null) {
            return null;
        }

        String cleaned =
                value.trim();

        return cleaned.isEmpty()
                ? null
                : cleaned;
    }
}