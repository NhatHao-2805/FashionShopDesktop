package com.fashionshop.server.controller;

import com.fashionshop.server.service.AdminService;
import com.google.gson.Gson;

import java.util.Map;

import static spark.Spark.get;
import static spark.Spark.put;

public class AdminController {

    private final AdminService adminService;
    private final Gson gson;

    public AdminController() {

        this.adminService =
                new AdminService();

        this.gson =
                new Gson();
    }

    public void registerRoutes() {

        get(
                "/api/admin/dashboard",
                (request, response) -> {

                    response.type(
                            "application/json; charset=UTF-8"
                    );

                    try {

                        return success(
                                "Lấy thống kê thành công.",
                                adminService
                                        .getDashboardStats()
                        );

                    } catch (Exception e) {

                        e.printStackTrace();

                        response.status(500);

                        return error(
                                "Không thể tải thống kê."
                        );
                    }
                }
        );

        get(
                "/api/admin/orders",
                (request, response) -> {

                    response.type(
                            "application/json; charset=UTF-8"
                    );

                    try {

                        return success(
                                "Lấy danh sách đơn hàng thành công.",
                                adminService
                                        .getOrders()
                        );

                    } catch (Exception e) {

                        e.printStackTrace();

                        response.status(500);

                        return error(
                                "Không thể tải danh sách đơn hàng."
                        );
                    }
                }
        );

        get(
                "/api/admin/customers",
                (request, response) -> {

                    response.type(
                            "application/json; charset=UTF-8"
                    );

                    try {

                        return success(
                                "Lấy danh sách khách hàng thành công.",
                                adminService
                                        .getCustomers()
                        );

                    } catch (Exception e) {

                        e.printStackTrace();

                        response.status(500);

                        return error(
                                "Không thể tải danh sách khách hàng."
                        );
                    }
                }
        );

        put(
                "/api/admin/orders/:id/status",
                (request, response) -> {

                    response.type(
                            "application/json; charset=UTF-8"
                    );

                    try {

                        long orderId =
                                Long.parseLong(
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

                            response.status(400);

                            return error(
                                    "Request không hợp lệ."
                            );
                        }

                        Map<String, Object> order =
                                adminService
                                        .updateOrderStatus(
                                                orderId,
                                                body.status
                                        );

                        return success(
                                "Cập nhật trạng thái đơn hàng thành công.",
                                order
                        );

                    } catch (
                            IllegalArgumentException e
                    ) {

                        response.status(400);

                        return error(
                                e.getMessage()
                        );

                    } catch (
                            IllegalStateException e
                    ) {

                        response.status(409);

                        return error(
                                e.getMessage()
                        );

                    } catch (Exception e) {

                        e.printStackTrace();

                        response.status(500);

                        return error(
                                "Không thể cập nhật đơn hàng."
                        );
                    }
                }
        );
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