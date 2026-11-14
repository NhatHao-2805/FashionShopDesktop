package com.fashionshop.server.controller;

import com.fashionshop.server.model.Order;
import com.fashionshop.server.model.OrderItem;
import com.fashionshop.server.security.JwtAuthentication;
import com.fashionshop.server.service.OrderService;
import com.google.gson.Gson;

import java.util.List;

import static spark.Spark.get;
import static spark.Spark.post;
import static spark.Spark.put;

public class OrderController {

    private final OrderService orderService;
    private final Gson gson;

    public OrderController() {

        this.orderService =
                new OrderService();

        this.gson =
                new Gson();
    }

    public void registerRoutes() {

        // =====================================================
        // GET /api/protected/orders
        // =====================================================

        get(
                "/api/protected/orders",
                (request, response) -> {

                    response.type(
                            "application/json; charset=UTF-8"
                    );

                    try {

                        JwtAuthentication authentication =
                                requireCustomer(
                                        request.attribute(
                                                "authentication"
                                        )
                                );

                        List<Order> orders =
                                orderService.getOrdersByUser(
                                        authentication.getUserId()
                                );

                        response.status(200);

                        return success(
                                "Lấy danh sách đơn hàng thành công.",
                                orders
                        );

                    } catch (IllegalArgumentException e) {

                        response.status(400);

                        return error(
                                e.getMessage()
                        );

                    } catch (SecurityException e) {

                        response.status(403);

                        return error(
                                e.getMessage()
                        );

                    } catch (Exception e) {

                        e.printStackTrace();

                        response.status(500);

                        return error(
                                "Không thể lấy danh sách đơn hàng."
                        );
                    }
                }
        );


        // =====================================================
        // GET /api/protected/orders/:id
        // =====================================================

        get(
                "/api/protected/orders/:id",
                (request, response) -> {

                    response.type(
                            "application/json; charset=UTF-8"
                    );

                    try {

                        JwtAuthentication authentication =
                                requireCustomer(
                                        request.attribute(
                                                "authentication"
                                        )
                                );

                        long orderId =
                                parseOrderId(
                                        request.params(":id")
                                );

                        Order order =
                                orderService.getOrderDetail(
                                        authentication.getUserId(),
                                        orderId
                                );

                        response.status(200);

                        return success(
                                "Lấy chi tiết đơn hàng thành công.",
                                order
                        );

                    } catch (IllegalArgumentException e) {

                        response.status(400);

                        return error(
                                e.getMessage()
                        );

                    } catch (SecurityException e) {

                        response.status(403);

                        return error(
                                e.getMessage()
                        );

                    } catch (Exception e) {

                        e.printStackTrace();

                        response.status(500);

                        return error(
                                "Không thể lấy chi tiết đơn hàng."
                        );
                    }
                }
        );


        // =====================================================
        // POST /api/protected/orders
        // =====================================================

        post(
                "/api/protected/orders",
                (request, response) -> {

                    response.type(
                            "application/json; charset=UTF-8"
                    );

                    try {

                        JwtAuthentication authentication =
                                requireCustomer(
                                        request.attribute(
                                                "authentication"
                                        )
                                );

                        CreateOrderRequest body =
                                gson.fromJson(
                                        request.body(),
                                        CreateOrderRequest.class
                                );

                        if (body == null) {

                            response.status(400);

                            return error(
                                    "Request không hợp lệ."
                            );
                        }

                        Order order =
                                orderService.createOrder(
                                        authentication.getUserId(),
                                        body.receiverName,
                                        body.phone,
                                        body.address,
                                        body.note,
                                        body.paymentMethod,
                                        body.items
                                );

                        response.status(201);

                        return success(
                                "Đặt hàng thành công.",
                                order
                        );

                    } catch (IllegalArgumentException e) {

                        response.status(400);

                        return error(
                                e.getMessage()
                        );

                    } catch (SecurityException e) {

                        response.status(403);

                        return error(
                                e.getMessage()
                        );

                    } catch (Exception e) {

                        e.printStackTrace();

                        response.status(500);

                        return error(
                                "Không thể tạo đơn hàng."
                        );
                    }
                }
        );


        // =====================================================
        // PUT /api/protected/orders/:id/cancel
        // =====================================================

        put(
                "/api/protected/orders/:id/cancel",
                (request, response) -> {

                    response.type(
                            "application/json; charset=UTF-8"
                    );

                    try {

                        JwtAuthentication authentication =
                                requireCustomer(
                                        request.attribute(
                                                "authentication"
                                        )
                                );

                        long orderId =
                                parseOrderId(
                                        request.params(":id")
                                );

                        Order order =
                                orderService.cancelOrder(
                                        authentication.getUserId(),
                                        orderId
                                );

                        response.status(200);

                        return success(
                                "Hủy đơn hàng thành công.",
                                order
                        );

                    } catch (IllegalArgumentException e) {

                        response.status(400);

                        return error(
                                e.getMessage()
                        );

                    } catch (SecurityException e) {

                        response.status(403);

                        return error(
                                e.getMessage()
                        );

                    } catch (Exception e) {

                        e.printStackTrace();

                        response.status(500);

                        return error(
                                "Không thể hủy đơn hàng."
                        );
                    }
                }
        );
    }

    private JwtAuthentication requireCustomer(
            JwtAuthentication authentication
    ) {

        if (authentication == null) {

            throw new SecurityException(
                    "Unauthorized."
            );
        }

        if (!authentication.isCustomer()) {

            throw new SecurityException(
                    "Chỉ tài khoản CUSTOMER được sử dụng chức năng này."
            );
        }

        return authentication;
    }

    private long parseOrderId(
            String value
    ) {

        try {

            long orderId =
                    Long.parseLong(value);

            if (orderId <= 0) {

                throw new NumberFormatException();
            }

            return orderId;

        } catch (Exception e) {

            throw new IllegalArgumentException(
                    "ID đơn hàng không hợp lệ."
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

        return gson.toJson(response);
    }

    private String error(
            String message
    ) {

        ApiResponse response =
                new ApiResponse();

        response.success = false;
        response.message = message;
        response.data = null;

        return gson.toJson(response);
    }

    private static class CreateOrderRequest {

        String receiverName;
        String phone;
        String address;
        String note;
        String paymentMethod;

        List<OrderItem> items;
    }

    private static class ApiResponse {

        boolean success;
        String message;
        Object data;
    }
}