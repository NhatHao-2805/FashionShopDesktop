package com.fashionshop.customer.service;

import com.fashionshop.customer.config.ServerConfig;
import com.fashionshop.customer.network.ApiClient;

public class ServerConnectionService {

    private final ApiClient apiClient =
            new ApiClient();

    public ConnectionResult checkConnection() {

        try {

            String response =
                    apiClient.get(
                            "/api/health"
                    );

            if (response == null
                    || response.isBlank()) {

                return new ConnectionResult(
                        false,
                        "Server có phản hồi nhưng dữ liệu rỗng.",
                        ServerConfig.getBaseUrl()
                );
            }

            return new ConnectionResult(
                    true,
                    "Kết nối server thành công.",
                    ServerConfig.getBaseUrl()
            );

        } catch (Exception e) {

            return new ConnectionResult(
                    false,
                    e.getMessage(),
                    ServerConfig.getBaseUrl()
            );
        }
    }

    public static class ConnectionResult {

        private final boolean connected;

        private final String message;

        private final String serverUrl;

        public ConnectionResult(
                boolean connected,
                String message,
                String serverUrl
        ) {

            this.connected =
                    connected;

            this.message =
                    message;

            this.serverUrl =
                    serverUrl;
        }

        public boolean isConnected() {
            return connected;
        }

        public String getMessage() {
            return message;
        }

        public String getServerUrl() {
            return serverUrl;
        }
    }
}