package com.fashionshop.customer.service;

import com.fashionshop.customer.model.Category;
import com.fashionshop.customer.network.ApiClient;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;

public class CategoryService {

    private final ApiClient apiClient;
    private final Gson gson;

    public CategoryService() {
        this.apiClient = new ApiClient();
        this.gson = new Gson();
    }

    public List<Category> getAllCategories() {

        try {

            String json =
                    apiClient.get("/api/categories");

            Type responseType =
                    new TypeToken<ApiResponse<List<Category>>>() {
                    }.getType();

            ApiResponse<List<Category>> response =
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

            e.printStackTrace();
        }

        return Collections.emptyList();
    }

    private static class ApiResponse<T> {

        boolean success;
        String message;
        T data;
    }
}