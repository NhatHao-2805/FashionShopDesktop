package com.fashionshop.server.controller;

import com.fashionshop.server.model.Product;
import com.fashionshop.server.service.AdminProductService;

import com.google.gson.Gson;

import static spark.Spark.get;
import static spark.Spark.post;
import static spark.Spark.put;

public class AdminProductController {

    private final AdminProductService service;

    private final Gson gson;

    public AdminProductController() {

        this.service =
                new AdminProductService();

        this.gson =
                new Gson();
    }

    public void registerRoutes() {

        get(
                "/api/admin/products",
                (request, response) -> {

                    response.type(
                            "application/json; charset=UTF-8"
                    );

                    try {

                        return success(
                                "Lấy danh sách sản phẩm thành công.",
                                service.getProducts()
                        );

                    } catch (Exception e) {

                        e.printStackTrace();

                        response.status(
                                500
                        );

                        return error(
                                "Không thể tải danh sách sản phẩm."
                        );
                    }
                }
        );

        post(
                "/api/admin/products",
                (request, response) -> {

                    response.type(
                            "application/json; charset=UTF-8"
                    );

                    try {

                        Product product =
                                gson.fromJson(
                                        request.body(),
                                        Product.class
                                );

                        Product created =
                                service.createProduct(
                                        product
                                );

                        response.status(
                                201
                        );

                        return success(
                                "Thêm sản phẩm thành công.",
                                created
                        );

                    } catch (
                            IllegalArgumentException e
                    ) {

                        response.status(
                                400
                        );

                        return error(
                                e.getMessage()
                        );

                    } catch (Exception e) {

                        e.printStackTrace();

                        response.status(
                                500
                        );

                        return error(
                                "Không thể thêm sản phẩm."
                        );
                    }
                }
        );

        put(
                "/api/admin/products/:id",
                (request, response) -> {

                    response.type(
                            "application/json; charset=UTF-8"
                    );

                    try {

                        long productId =
                                parseId(
                                        request.params(
                                                ":id"
                                        )
                                );

                        Product product =
                                gson.fromJson(
                                        request.body(),
                                        Product.class
                                );

                        Product updated =
                                service.updateProduct(
                                        productId,
                                        product
                                );

                        return success(
                                "Cập nhật sản phẩm thành công.",
                                updated
                        );

                    } catch (
                            IllegalArgumentException e
                    ) {

                        response.status(
                                400
                        );

                        return error(
                                e.getMessage()
                        );

                    } catch (Exception e) {

                        e.printStackTrace();

                        response.status(
                                500
                        );

                        return error(
                                "Không thể cập nhật sản phẩm."
                        );
                    }
                }
        );

        put(
                "/api/admin/products/:id/status",
                (request, response) -> {

                    response.type(
                            "application/json; charset=UTF-8"
                    );

                    try {

                        long productId =
                                parseId(
                                        request.params(
                                                ":id"
                                        )
                                );

                        StatusRequest body =
                                gson.fromJson(
                                        request.body(),
                                        StatusRequest.class
                                );

                        if (body == null) {

                            throw new IllegalArgumentException(
                                    "Request không hợp lệ."
                            );
                        }

                        Product updated =
                                service.changeStatus(
                                        productId,
                                        body.status
                                );

                        return success(
                                "Cập nhật trạng thái sản phẩm thành công.",
                                updated
                        );

                    } catch (
                            IllegalArgumentException e
                    ) {

                        response.status(
                                400
                        );

                        return error(
                                e.getMessage()
                        );

                    } catch (Exception e) {

                        e.printStackTrace();

                        response.status(
                                500
                        );

                        return error(
                                "Không thể cập nhật trạng thái sản phẩm."
                        );
                    }
                }
        );
    }

    private long parseId(
            String value
    ) {

        try {

            long id =
                    Long.parseLong(
                            value
                    );

            if (id <= 0) {

                throw new NumberFormatException();
            }

            return id;

        } catch (Exception e) {

            throw new IllegalArgumentException(
                    "ID sản phẩm không hợp lệ."
            );
        }
    }

    private String success(
            String message,
            Object data
    ) {

        ApiResponse response =
                new ApiResponse();

        response.success = true;
        response.message = message;
        response.data = data;

        return gson.toJson(
                response
        );
    }

    private String error(
            String message
    ) {

        ApiResponse response =
                new ApiResponse();

        response.success = false;
        response.message = message;
        response.data = null;

        return gson.toJson(
                response
        );
    }

    private static class StatusRequest {

        String status;
    }

    private static class ApiResponse {

        boolean success;
        String message;
        Object data;
    }
}