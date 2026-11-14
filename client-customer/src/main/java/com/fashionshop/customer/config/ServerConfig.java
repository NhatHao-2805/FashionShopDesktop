package com.fashionshop.customer.config;

public final class ServerConfig {

    public static final String PROPERTY_NAME =
            "fashion.server.url";

    public static final String ENV_NAME =
            "FASHION_SERVER_URL";

    private static final String DEFAULT_URL =
            "http://localhost:8080";

    private static final String BASE_URL =
            resolveBaseUrl();

    private ServerConfig() {
    }

    public static String getBaseUrl() {

        return BASE_URL;
    }

    public static String apiUrl(
            String endpoint
    ) {

        if (endpoint == null
                || endpoint.isBlank()) {

            return BASE_URL;
        }

        String normalizedEndpoint =
                endpoint.startsWith("/")
                        ? endpoint
                        : "/" + endpoint;

        return BASE_URL
                + normalizedEndpoint;
    }

    public static String resourceUrl(
            String path
    ) {

        if (path == null
                || path.isBlank()) {

            return null;
        }

        String value =
                path.trim();

        if (value.startsWith(
                "http://"
        )
                || value.startsWith(
                "https://"
        )) {

            /*
             * Trường hợp dữ liệu cũ đang lưu URL localhost:
             * thay host bằng server hiện tại để client máy khác
             * vẫn tải được tài nguyên.
             */
            if (!BASE_URL.equals(
                    DEFAULT_URL
            )) {

                value =
                        value.replace(
                                "http://localhost:8080",
                                BASE_URL
                        );

                value =
                        value.replace(
                                "http://127.0.0.1:8080",
                                BASE_URL
                        );
            }

            return value;
        }

        return apiUrl(
                value
        );
    }

    public static boolean isRemoteServer() {

        return !DEFAULT_URL.equals(
                BASE_URL
        );
    }

    private static String resolveBaseUrl() {

        String systemProperty =
                System.getProperty(
                        PROPERTY_NAME
                );

        if (systemProperty != null
                && !systemProperty
                .isBlank()) {

            return normalize(
                    systemProperty
            );
        }

        String environment =
                System.getenv(
                        ENV_NAME
                );

        if (environment != null
                && !environment
                .isBlank()) {

            return normalize(
                    environment
            );
        }

        return DEFAULT_URL;
    }

    private static String normalize(
            String value
    ) {

        String normalized =
                value.trim();

        while (normalized.endsWith(
                "/"
        )) {

            normalized =
                    normalized.substring(
                            0,
                            normalized.length() - 1
                    );
        }

        if (!normalized.startsWith(
                "http://"
        )
                && !normalized.startsWith(
                "https://"
        )) {

            normalized =
                    "http://"
                            + normalized;
        }

        return normalized;
    }
}