package com.fashionshop.server.controller;

import com.fashionshop.server.model.Product;
import com.fashionshop.server.repository.ProductRepository;
import com.google.gson.Gson;

import java.util.List;

import static spark.Spark.get;

public class ProductController {

    private final ProductRepository productRepository;
    private final Gson gson;

    public ProductController() {
        this.productRepository = new ProductRepository();
        this.gson = new Gson();
    }

    public void registerRoutes() {

        // =========================================================
        // GET PRODUCTS
        // =========================================================

        get("/api/products", (request, response) -> {

            response.type("application/json; charset=UTF-8");

            try {

                String keyword =
                        request.queryParams("keyword");

                String categoryIdParam =
                        request.queryParams("categoryId");

                Long categoryId = null;

                if (
                        categoryIdParam != null
                                && !categoryIdParam.isBlank()
                ) {

                    try {

                        categoryId =
                                Long.parseLong(
                                        categoryIdParam
                                );

                    } catch (NumberFormatException e) {

                        response.status(400);

                        return error(
                                "Category ID không hợp lệ."
                        );
                    }

                    if (categoryId <= 0) {

                        response.status(400);

                        return error(
                                "Category ID không hợp lệ."
                        );
                    }
                }

                List<Product> products;

                boolean hasKeyword =
                        keyword != null
                                && !keyword.isBlank();

                boolean hasCategory =
                        categoryId != null;

                if (!hasKeyword && !hasCategory) {

                    products =
                            productRepository.findAllActive();

                } else {

                    products =
                            productRepository.search(
                                    keyword,
                                    categoryId
                            );
                }

                response.status(200);

                return success(
                        "Lấy danh sách sản phẩm thành công.",
                        products
                );

            } catch (Exception e) {

                e.printStackTrace();

                response.status(500);

                return error(
                        "Không thể lấy danh sách sản phẩm."
                );
            }
        });

        // =========================================================
        // GET PRODUCT BY ID
        // =========================================================

        get("/api/products/:id", (request, response) -> {

            response.type("application/json; charset=UTF-8");

            try {

                long productId;

                try {

                    productId =
                            Long.parseLong(
                                    request.params(":id")
                            );

                } catch (NumberFormatException e) {

                    response.status(400);

                    return error(
                            "Product ID không hợp lệ."
                    );
                }

                if (productId <= 0) {

                    response.status(400);

                    return error(
                            "Product ID không hợp lệ."
                    );
                }

                Product product =
                        productRepository.findById(
                                productId
                        );

                if (product == null) {

                    response.status(404);

                    return error(
                            "Không tìm thấy sản phẩm."
                    );
                }

                response.status(200);

                return success(
                        "Lấy thông tin sản phẩm thành công.",
                        product
                );

            } catch (Exception e) {

                e.printStackTrace();

                response.status(500);

                return error(
                        "Không thể lấy thông tin sản phẩm."
                );
            }
        });
    }

    // =============================================================
    // SUCCESS RESPONSE
    // =============================================================

    private String success(
            String message,
            Object data
    ) {

        ApiResponse response =
                new ApiResponse();

        response.success = true;
        response.message = message;
        response.data = data;

        return gson.toJson(response);
    }

    // =============================================================
    // ERROR RESPONSE
    // =============================================================

    private String error(String message) {

        ApiResponse response =
                new ApiResponse();

        response.success = false;
        response.message = message;
        response.data = null;

        return gson.toJson(response);
    }

    // =============================================================
    // API RESPONSE
    // =============================================================

    private static class ApiResponse {
        boolean success;
        String message;
        Object data;
    }
}