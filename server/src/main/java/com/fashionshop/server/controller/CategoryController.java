package com.fashionshop.server.controller;

import com.fashionshop.server.model.Category;
import com.fashionshop.server.repository.CategoryRepository;
import com.google.gson.Gson;

import java.util.List;

import static spark.Spark.get;

public class CategoryController {

    private final CategoryRepository categoryRepository;
    private final Gson gson;

    public CategoryController() {
        this.categoryRepository = new CategoryRepository();
        this.gson = new Gson();
    }

    public void registerRoutes() {

        // =========================================================
        // GET ALL ACTIVE CATEGORIES
        // =========================================================

        get("/api/categories", (request, response) -> {

            response.type("application/json; charset=UTF-8");

            try {

                List<Category> categories =
                        categoryRepository.findAllActive();

                response.status(200);

                return success(
                        "Lấy danh sách danh mục thành công.",
                        categories
                );

            } catch (Exception e) {

                e.printStackTrace();

                response.status(500);

                return error(
                        "Không thể lấy danh sách danh mục."
                );
            }
        });

        // =========================================================
        // GET CATEGORY BY ID
        // =========================================================

        get("/api/categories/:id", (request, response) -> {

            response.type("application/json; charset=UTF-8");

            try {

                long categoryId;

                try {

                    categoryId =
                            Long.parseLong(
                                    request.params(":id")
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

                Category category =
                        categoryRepository.findById(
                                categoryId
                        );

                if (category == null) {

                    response.status(404);

                    return error(
                            "Không tìm thấy danh mục."
                    );
                }

                response.status(200);

                return success(
                        "Lấy thông tin danh mục thành công.",
                        category
                );

            } catch (Exception e) {

                e.printStackTrace();

                response.status(500);

                return error(
                        "Không thể lấy thông tin danh mục."
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