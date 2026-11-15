package com.fashionshop.customer.service;

import com.fashionshop.customer.model.Product;
import com.fashionshop.customer.network.ApiClient;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;

public class ProductService {

    private final ApiClient apiClient;
    private final Gson gson;

    public ProductService() {
        this.apiClient = new ApiClient();
        this.gson = new Gson();
    }

    public List<Product> getAllProducts() {

        try {

            String json =
                    apiClient.get("/api/products");

            Type responseType =
                    new TypeToken<ApiResponse<List<Product>>>() {
                    }.getType();

            ApiResponse<List<Product>> response =
                    gson.fromJson(
                            json,
                            responseType
                    );

            if (response != null
                    && response.success
                    && response.data != null) {

                return response.data;
            }

        } catch (Exception e) {

            throw new IllegalStateException("Không thể tải sản phẩm từ server. Hãy kiểm tra server và MySQL.", e);
        }

        throw new IllegalStateException("Server không trả về danh sách sản phẩm hợp lệ.");
    }

    private static class ApiResponse<T> {

        boolean success;
        String message;
        T data;
    }
}