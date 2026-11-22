package com.fashionshop.server.controller;

import com.fashionshop.server.security.JwtAuthentication;
import com.fashionshop.server.repository.VariantStockRepository;
import com.fashionshop.server.service.InventoryService;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;

import java.sql.SQLException;
import java.util.List;

import static spark.Spark.get;
import static spark.Spark.post;

public class InventoryController {

    private final InventoryService service;

    private final Gson gson;
    private final VariantStockRepository variants = new VariantStockRepository();

    public InventoryController() {

        this.service =
                new InventoryService();

        this.gson =
                new Gson();
    }

    public void registerRoutes() {

        String variantsPath = "/api/admin/inventory/:productId/variants";
        get(variantsPath, (request, response) -> {
            response.type("application/json; charset=UTF-8");
            try {
                return success("Đã tải tồn kho size/màu.",
                        variants.get(Long.parseLong(request.params(":productId"))));
            } catch (IllegalArgumentException e) {
                response.status(400);
                return error(e.getMessage());
            } catch (SQLException e) {
                response.status(500);
                return error("Không thể tải tồn kho size/màu.");
            }
        });
        post(variantsPath, (request, response) -> {
            response.type("application/json; charset=UTF-8");
            try {
                var body = gson.fromJson(request.body(), AllocationRequest.class);
                if (body == null) {
                    throw new IllegalArgumentException("Dữ liệu phân bổ không hợp lệ.");
                }
                return success("Đã lưu phân bổ tồn kho.", variants.allocate(
                        Long.parseLong(request.params(":productId")), body.expected(), body.allocations()));
            } catch (JsonParseException e) {
                response.status(400);
                return error("Dữ liệu phân bổ không hợp lệ.");
            } catch (IllegalArgumentException e) {
                response.status(400);
                return error(e.getMessage());
            } catch (SQLException e) {
                response.status(500);
                return error("Không thể lưu phân bổ tồn kho.");
            }
        });

        get(
                "/api/admin/inventory",
                (request, response) -> {

                    response.type(
                            "application/json; charset=UTF-8"
                    );

                    try {

                        return success(
                                "Lấy dữ liệu kho thành công.",
                                service.getStockOverview()
                        );

                    } catch (Exception e) {

                        e.printStackTrace();

                        response.status(500);

                        return error(
                                "Không thể tải dữ liệu kho."
                        );
                    }
                }
        );

        get(
                "/api/admin/inventory/transactions",
                (request, response) -> {

                    response.type(
                            "application/json; charset=UTF-8"
                    );

                    try {

                        int limit =
                                parseLimit(
                                        request.queryParams(
                                                "limit"
                                        )
                                );

                        return success(
                                "Lấy lịch sử kho thành công.",
                                service.getTransactions(
                                        limit
                                )
                        );

                    } catch (
                            IllegalArgumentException e
                    ) {

                        response.status(400);

                        return error(
                                e.getMessage()
                        );

                    } catch (Exception e) {

                        e.printStackTrace();

                        response.status(500);

                        return error(
                                "Không thể tải lịch sử kho."
                        );
                    }
                }
        );

        post(
                "/api/admin/inventory/adjust",
                (request, response) -> {

                    response.type(
                            "application/json; charset=UTF-8"
                    );

                    try {

                        JwtAuthentication authentication =
                                request.attribute(
                                        "auth"
                                );

                        if (authentication == null) {

                            response.status(401);

                            return error(
                                    "Chưa xác thực tài khoản."
                            );
                        }

                        AdjustmentRequest body =
                                gson.fromJson(
                                        request.body(),
                                        AdjustmentRequest.class
                                );

                        if (body == null) {

                            throw new IllegalArgumentException(
                                    "Request không hợp lệ."
                            );
                        }

                        Object result =
                                service.adjustStock(
                                        body.productId,
                                        authentication.getUserId(),
                                        body.type,
                                        body.quantity,
                                        body.reason
                                );

                        return success(
                                "Điều chỉnh tồn kho thành công.",
                                result
                        );

                    } catch (
                            IllegalArgumentException e
                    ) {

                        response.status(400);

                        return error(
                                e.getMessage()
                        );

                    } catch (Exception e) {

                        e.printStackTrace();

                        response.status(500);

                        return error(
                                "Không thể điều chỉnh tồn kho."
                        );
                    }
                }
        );
    }

    private int parseLimit(
            String value
    ) {

        if (value == null
                || value.isBlank()) {

            return 100;
        }

        try {

            return Integer.parseInt(
                    value
            );

        } catch (NumberFormatException e) {

            throw new IllegalArgumentException(
                    "Limit không hợp lệ."
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

    private static class AdjustmentRequest {

        long productId;

        String type;

        int quantity;

        String reason;
    }

    private record AllocationRequest(VariantStockRepository.Snapshot expected,
                                     List<VariantStockRepository.Allocation> allocations) {}

    private static class ApiResponse {

        boolean success;

        String message;

        Object data;
    }
}
