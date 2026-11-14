package com.fashionshop.customer.network;

import com.fashionshop.customer.config.ServerConfig;
import com.fashionshop.customer.service.SessionManager;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.ConnectException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.UUID;

public class ApiClient {

    private static final Duration CONNECT_TIMEOUT =
            Duration.ofSeconds(5);

    private static final Duration REQUEST_TIMEOUT =
            Duration.ofSeconds(30);

    private final HttpClient httpClient;

    public ApiClient() {

        httpClient =
                HttpClient.newBuilder()
                        .connectTimeout(
                                CONNECT_TIMEOUT
                        )
                        .build();
    }

    public String get(
            String endpoint
    ) throws Exception {

        HttpRequest.Builder builder =
                baseRequest(
                        endpoint
                )
                        .GET();

        addAuthorization(
                builder
        );

        return send(
                builder.build()
        );
    }

    public String post(
            String endpoint,
            String jsonBody
    ) throws Exception {

        HttpRequest.Builder builder =
                baseRequest(
                        endpoint
                )
                        .header(
                                "Content-Type",
                                "application/json; charset=UTF-8"
                        )
                        .POST(
                                HttpRequest.BodyPublishers
                                        .ofString(
                                                jsonBody == null
                                                        ? ""
                                                        : jsonBody,
                                                StandardCharsets.UTF_8
                                        )
                        );

        addAuthorization(
                builder
        );

        return send(
                builder.build()
        );
    }

    public String put(
            String endpoint,
            String jsonBody
    ) throws Exception {

        HttpRequest.Builder builder =
                baseRequest(
                        endpoint
                )
                        .header(
                                "Content-Type",
                                "application/json; charset=UTF-8"
                        )
                        .PUT(
                                HttpRequest.BodyPublishers
                                        .ofString(
                                                jsonBody == null
                                                        ? ""
                                                        : jsonBody,
                                                StandardCharsets.UTF_8
                                        )
                        );

        addAuthorization(
                builder
        );

        return send(
                builder.build()
        );
    }

    public String delete(
            String endpoint
    ) throws Exception {

        HttpRequest.Builder builder =
                baseRequest(
                        endpoint
                )
                        .DELETE();

        addAuthorization(
                builder
        );

        return send(
                builder.build()
        );
    }

    public String postMultipartFile(
            String endpoint,
            String fieldName,
            Path file
    ) throws Exception {

        if (file == null
                || !Files.exists(file)
                || !Files.isRegularFile(file)) {

            throw new IllegalArgumentException(
                    "File upload không hợp lệ."
            );
        }

        long fileSize =
                Files.size(
                        file
                );

        if (fileSize <= 0) {

            throw new IllegalArgumentException(
                    "File ảnh đang rỗng."
            );
        }

        if (fileSize > 8L * 1024L * 1024L) {

            throw new IllegalArgumentException(
                    "Ảnh tối đa 8 MB."
            );
        }

        String boundary =
                "----FashionShop"
                        + UUID.randomUUID()
                        .toString()
                        .replace("-", "");

        String fileName =
                file.getFileName()
                        .toString()
                        .replace(
                                "\"",
                                ""
                        );

        String contentType =
                Files.probeContentType(
                        file
                );

        if (contentType == null
                || contentType.isBlank()) {

            contentType =
                    "application/octet-stream";
        }

        ByteArrayOutputStream output =
                new ByteArrayOutputStream();

        String header =
                "--"
                        + boundary
                        + "\r\n"
                        + "Content-Disposition: form-data; name=\""
                        + fieldName
                        + "\"; filename=\""
                        + fileName
                        + "\"\r\n"
                        + "Content-Type: "
                        + contentType
                        + "\r\n\r\n";

        output.write(
                header.getBytes(
                        StandardCharsets.UTF_8
                )
        );

        output.write(
                Files.readAllBytes(
                        file
                )
        );

        String footer =
                "\r\n--"
                        + boundary
                        + "--\r\n";

        output.write(
                footer.getBytes(
                        StandardCharsets.UTF_8
                )
        );

        HttpRequest.Builder builder =
                baseRequest(
                        endpoint
                )
                        .header(
                                "Content-Type",
                                "multipart/form-data; boundary="
                                        + boundary
                        )
                        .POST(
                                HttpRequest.BodyPublishers
                                        .ofByteArray(
                                                output.toByteArray()
                                        )
                        );

        addAuthorization(
                builder
        );

        return send(
                builder.build()
        );
    }

    private HttpRequest.Builder baseRequest(
            String endpoint
    ) {

        return HttpRequest.newBuilder()
                .uri(
                        URI.create(
                                ServerConfig.apiUrl(
                                        endpoint
                                )
                        )
                )
                .timeout(
                        REQUEST_TIMEOUT
                )
                .header(
                        "Accept",
                        "application/json"
                );
    }

    private void addAuthorization(
            HttpRequest.Builder builder
    ) {

        String token =
                SessionManager
                        .getInstance()
                        .getToken();

        if (token != null
                && !token.isBlank()) {

            builder.header(
                    "Authorization",
                    "Bearer " + token
            );
        }
    }

    private String send(
            HttpRequest request
    ) throws Exception {

        try {

            HttpResponse<String> response =
                    httpClient.send(
                            request,
                            HttpResponse.BodyHandlers
                                    .ofString(
                                            StandardCharsets.UTF_8
                                    )
                    );

            String body =
                    response.body();

            if (body == null) {
                body = "";
            }

            if (response.statusCode() >= 500
                    && body.isBlank()) {

                throw new IllegalStateException(
                        "Server gặp lỗi HTTP "
                                + response.statusCode()
                                + "."
                );
            }

            return body;

        } catch (ConnectException e) {

            throw new IllegalStateException(
                    "Không thể kết nối tới server "
                            + ServerConfig.getBaseUrl()
                            + ".",
                    e
            );

        } catch (
                java.net.http.HttpTimeoutException e
        ) {

            throw new IllegalStateException(
                    "Server phản hồi quá lâu.",
                    e
            );

        } catch (IOException e) {

            throw new IllegalStateException(
                    "Lỗi kết nối mạng tới "
                            + ServerConfig.getBaseUrl()
                            + ".",
                    e
            );

        } catch (InterruptedException e) {

            Thread.currentThread()
                    .interrupt();

            throw new IllegalStateException(
                    "Kết nối tới server bị gián đoạn.",
                    e
            );
        }
    }
}