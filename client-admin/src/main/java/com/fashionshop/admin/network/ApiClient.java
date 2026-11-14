package com.fashionshop.admin.network;

import com.fashionshop.admin.service.SessionManager;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

public class ApiClient {

    private static final String BASE_URL =
            "http://localhost:8080";

    private final HttpClient httpClient;

    public ApiClient() {

        this.httpClient =
                HttpClient.newHttpClient();
    }

    public String get(
            String endpoint
    ) throws IOException, InterruptedException {

        HttpRequest.Builder builder =
                HttpRequest.newBuilder()
                        .uri(
                                URI.create(
                                        BASE_URL
                                                + endpoint
                                )
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
    ) throws IOException, InterruptedException {

        HttpRequest.Builder builder =
                HttpRequest.newBuilder()
                        .uri(
                                URI.create(
                                        BASE_URL
                                                + endpoint
                                )
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
    ) throws IOException, InterruptedException {

        HttpRequest.Builder builder =
                HttpRequest.newBuilder()
                        .uri(
                                URI.create(
                                        BASE_URL
                                                + endpoint
                                )
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

    private String send(
            HttpRequest request
    ) throws IOException, InterruptedException {

        HttpResponse<String> response =
                httpClient.send(
                        request,
                        HttpResponse.BodyHandlers
                                .ofString(
                                        StandardCharsets.UTF_8
                                )
                );

        return response.body();
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
}