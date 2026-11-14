package com.fashionshop.server.controller;

import com.google.gson.Gson;

import javax.imageio.ImageIO;
import javax.servlet.MultipartConfigElement;
import javax.servlet.http.Part;

import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import static spark.Spark.post;

public class ProductImageUploadController {

    private static final long MAX_FILE_SIZE =
            8L * 1024L * 1024L;

    private static final long MAX_REQUEST_SIZE =
            10L * 1024L * 1024L;

    private final Gson gson =
            new Gson();

    public void registerRoutes() {

        post(
                "/api/admin/uploads/product-image",
                (request, response) -> {

                    response.type(
                            "application/json; charset=UTF-8"
                    );

                    Part filePart =
                            null;

                    Path savedFile =
                            null;

                    try {

                        Path uploadDirectory =
                                Paths.get(
                                        "uploads",
                                        "products"
                                )
                                        .toAbsolutePath()
                                        .normalize();

                        Path tempDirectory =
                                Paths.get(
                                        "uploads",
                                        "tmp"
                                )
                                        .toAbsolutePath()
                                        .normalize();

                        Files.createDirectories(
                                uploadDirectory
                        );

                        Files.createDirectories(
                                tempDirectory
                        );

                        MultipartConfigElement multipartConfig =
                                new MultipartConfigElement(
                                        tempDirectory.toString(),
                                        MAX_FILE_SIZE,
                                        MAX_REQUEST_SIZE,
                                        1024 * 1024
                                );

                        request.raw()
                                .setAttribute(
                                        "org.eclipse.jetty.multipartConfig",
                                        multipartConfig
                                );

                        filePart =
                                request.raw()
                                        .getPart(
                                                "file"
                                        );

                        if (filePart == null
                                || filePart.getSize() <= 0) {

                            throw new IllegalArgumentException(
                                    "Vui lòng chọn ảnh sản phẩm."
                            );
                        }

                        if (filePart.getSize()
                                > MAX_FILE_SIZE) {

                            throw new IllegalArgumentException(
                                    "Ảnh tối đa 8 MB."
                            );
                        }

                        String originalFileName =
                                filePart.getSubmittedFileName();

                        if (originalFileName == null
                                || originalFileName.isBlank()) {

                            throw new IllegalArgumentException(
                                    "Tên file ảnh không hợp lệ."
                            );
                        }

                        String extension =
                                extensionOf(
                                        originalFileName
                                );

                        if (!extension.equals("jpg")
                                && !extension.equals("jpeg")
                                && !extension.equals("png")) {

                            throw new IllegalArgumentException(
                                    "Chỉ hỗ trợ ảnh JPG, JPEG hoặc PNG."
                            );
                        }

                        String contentType =
                                filePart.getContentType();

                        if (contentType == null
                                || !contentType
                                .toLowerCase(Locale.ROOT)
                                .startsWith("image/")) {

                            throw new IllegalArgumentException(
                                    "File được chọn không phải ảnh hợp lệ."
                            );
                        }

                        String safeExtension =
                                extension.equals("jpeg")
                                        ? "jpg"
                                        : extension;

                        String generatedFileName =
                                System.currentTimeMillis()
                                        + "_"
                                        + UUID.randomUUID()
                                        .toString()
                                        .replace("-", "")
                                        + "."
                                        + safeExtension;

                        savedFile =
                                uploadDirectory
                                        .resolve(
                                                generatedFileName
                                        )
                                        .normalize();

                        if (!savedFile.startsWith(
                                uploadDirectory
                        )) {

                            throw new IllegalArgumentException(
                                    "Đường dẫn file không hợp lệ."
                            );
                        }

                        try (
                                InputStream inputStream =
                                        filePart.getInputStream()
                        ) {

                            Files.copy(
                                    inputStream,
                                    savedFile,
                                    StandardCopyOption.REPLACE_EXISTING
                            );
                        }

                        BufferedImage image =
                                ImageIO.read(
                                        savedFile.toFile()
                                );

                        if (image == null) {

                            Files.deleteIfExists(
                                    savedFile
                            );

                            throw new IllegalArgumentException(
                                    "Nội dung file không phải ảnh JPG/PNG hợp lệ."
                            );
                        }

                        if (image.getWidth() <= 0
                                || image.getHeight() <= 0) {

                            Files.deleteIfExists(
                                    savedFile
                            );

                            throw new IllegalArgumentException(
                                    "Kích thước ảnh không hợp lệ."
                            );
                        }

                        String imagePath =
                                "/products/"
                                        + generatedFileName;

                        Map<String, Object> data =
                                new LinkedHashMap<>();

                        data.put(
                                "imagePath",
                                imagePath
                        );

                        data.put(
                                "fileName",
                                generatedFileName
                        );

                        data.put(
                                "originalFileName",
                                originalFileName
                        );

                        data.put(
                                "size",
                                filePart.getSize()
                        );

                        data.put(
                                "width",
                                image.getWidth()
                        );

                        data.put(
                                "height",
                                image.getHeight()
                        );

                        response.status(
                                201
                        );

                        return success(
                                "Upload ảnh sản phẩm thành công.",
                                data
                        );

                    } catch (
                            IllegalArgumentException e
                    ) {

                        if (savedFile != null) {

                            try {

                                Files.deleteIfExists(
                                        savedFile
                                );

                            } catch (Exception ignored) {
                            }
                        }

                        response.status(
                                400
                        );

                        return error(
                                e.getMessage()
                        );

                    } catch (Exception e) {

                        e.printStackTrace();

                        if (savedFile != null) {

                            try {

                                Files.deleteIfExists(
                                        savedFile
                                );

                            } catch (Exception ignored) {
                            }
                        }

                        response.status(
                                500
                        );

                        return error(
                                "Không thể upload ảnh sản phẩm."
                        );

                    } finally {

                        if (filePart != null) {

                            try {

                                filePart.delete();

                            } catch (Exception ignored) {
                            }
                        }
                    }
                }
        );
    }

    private String extensionOf(
            String fileName
    ) {

        int index =
                fileName.lastIndexOf(
                        '.'
                );

        if (index < 0
                || index >= fileName.length() - 1) {

            return "";
        }

        return fileName
                .substring(
                        index + 1
                )
                .toLowerCase(
                        Locale.ROOT
                );
    }

    private String success(
            String message,
            Object data
    ) {

        ApiResponse response =
                new ApiResponse();

        response.success =
                true;

        response.message =
                message;

        response.data =
                data;

        return gson.toJson(
                response
        );
    }

    private String error(
            String message
    ) {

        ApiResponse response =
                new ApiResponse();

        response.success =
                false;

        response.message =
                message;

        response.data =
                null;

        return gson.toJson(
                response
        );
    }

    private static class ApiResponse {

        boolean success;

        String message;

        Object data;
    }
}