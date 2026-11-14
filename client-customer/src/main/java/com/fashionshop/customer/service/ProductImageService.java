package com.fashionshop.customer.service;

import com.fashionshop.customer.network.ApiClient;
import com.google.gson.Gson;

import java.io.File;

public class ProductImageService {

    private final ApiClient apiClient =
            new ApiClient();

    private final Gson gson =
            new Gson();

    public String upload(
            File file
    ) throws Exception {

        if (file == null) {

            throw new IllegalArgumentException(
                    "Vui lòng chọn ảnh sản phẩm."
            );
        }

        String json =
                apiClient.postMultipartFile(
                        "/api/admin/uploads/product-image",
                        "file",
                        file.toPath()
                );

        UploadResponse response =
                gson.fromJson(
                        json,
                        UploadResponse.class
                );

        if (response == null) {

            throw new IllegalStateException(
                    "Server không trả dữ liệu upload."
            );
        }

        if (!response.success) {

            throw new IllegalArgumentException(
                    response.message == null
                            ? "Không thể upload ảnh."
                            : response.message
            );
        }

        if (response.data == null
                || response.data.imagePath == null
                || response.data.imagePath.isBlank()) {

            throw new IllegalStateException(
                    "Server không trả đường dẫn ảnh."
            );
        }

        return response.data.imagePath;
    }

    private static class UploadResponse {

        boolean success;

        String message;

        UploadData data;
    }

    private static class UploadData {

        String imagePath;

        String fileName;

        String originalFileName;

        long size;

        int width;

        int height;
    }
}