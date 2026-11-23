package com.fashionshop.server;

import com.fashionshop.server.controller.OrderController;
import com.fashionshop.server.controller.AdminController;
import com.fashionshop.server.controller.AdminProductController;
import com.fashionshop.server.controller.ProductImageUploadController;
import com.fashionshop.server.controller.InventoryController;
import com.fashionshop.server.config.AppConfig;
import com.fashionshop.server.controller.AuthController;
import com.fashionshop.server.controller.ChatController;
import com.fashionshop.server.controller.CategoryController;
import com.fashionshop.server.controller.ProductController;
import com.fashionshop.server.controller.TestAuthController;
import com.fashionshop.server.database.DatabaseTest;
import com.fashionshop.server.security.AuthMiddleware;
import com.fashionshop.server.websocket.ChatWebSocket;

import static spark.Spark.*;

public class ServerApplication {

    public static void main(String[] args) {

        printBanner();

        try {

            // =========================================================
            // 1. ĐỌC CẤU HÌNH SERVER
            // =========================================================

            String host =
                    AppConfig.getWithEnvironmentOverride("server.host", "FASHION_HTTP_HOST");

            int httpPort =
                    Integer.parseInt(AppConfig.getWithEnvironmentOverride("server.http.port", "FASHION_HTTP_PORT"));

            int websocketPort =
                    AppConfig.getInt("server.websocket.port");

            System.out.println("[CONFIG]");
            System.out.println(
                    "Server host       : " + host
            );
            System.out.println(
                    "HTTP port         : " + httpPort
            );
            System.out.println(
                    "WebSocket port    : " + websocketPort
            );
            System.out.println();


            // =========================================================
            // 2. KIỂM TRA DATABASE
            // =========================================================

            System.out.println("[DATABASE]");
            System.out.println(
                    "Đang kiểm tra kết nối MySQL/MariaDB..."
            );

            DatabaseTest.run();

            System.out.println();


            // =========================================================
            // 3. CẤU HÌNH HTTP SERVER
            // =========================================================

           ipAddress(host);
port(httpPort);

staticFiles.externalLocation(
        java.nio.file.Paths.get("uploads").toAbsolutePath().normalize().toString()
);


            
webSocket(
        "/ws/chat",
        ChatWebSocket.class
);

System.out.println(
        "[WS] Endpoint registered: /ws/chat"
);
// =========================================================
            // 4. CORS
            // =========================================================

            enableCors();


            // =========================================================
            // 5. JSON RESPONSE MẶC ĐỊNH
            // =========================================================

            before((request, response) -> {

                response.type(
                        "application/json; charset=UTF-8"
                );
            });


            // =========================================================
            // 6. CONTROLLERS
            // =========================================================

            AuthController authController =
                    new AuthController();

            authController.registerRoutes();

            ChatController chatController =
                    new ChatController();

            chatController.registerRoutes();




            CategoryController categoryController =
                    new CategoryController();

            categoryController.registerRoutes();


            ProductController productController =
                    new ProductController();

            productController.registerRoutes();

            OrderController orderController =
        new OrderController();

orderController.registerRoutes();

            AdminController adminController =
                    new AdminController();

            adminController.registerRoutes();

            AdminProductController adminProductController =
                    new AdminProductController();

            adminProductController.registerRoutes();

            ProductImageUploadController productImageUploadController =
                    new ProductImageUploadController();

            productImageUploadController.registerRoutes();

            InventoryController inventoryController =
                    new InventoryController();

            inventoryController.registerRoutes();


            TestAuthController testAuthController =
                    new TestAuthController();

            testAuthController.registerRoutes();




            // =========================================================
            // 7. AUTHENTICATION MIDDLEWARE
            // =========================================================

            AuthMiddleware authMiddleware =
                    new AuthMiddleware();

            authMiddleware.requireAuthentication();
            authMiddleware.requireAdmin();


            // =========================================================
            // 8. HIỂN THỊ DANH SÁCH ROUTES
            // =========================================================

            System.out.println("[ROUTES]");

            System.out.println(
                    "POST /api/auth/login"
            );

            System.out.println(
                    "GET  /api/categories"
            );

            System.out.println(
                    "GET  /api/categories/:id"
            );

            System.out.println(
                    "GET  /api/products"
            );

            System.out.println(
                    "GET  /api/products/:id"
            );

            System.out.println(
        "POST /api/protected/orders"
            );

            // =========================================================
            // 9. HEALTH API
            // =========================================================

            registerHealthRoutes(httpPort);


            // =========================================================
            // 10. XỬ LÝ 404
            // =========================================================

            notFound((request, response) -> {

                response.status(404);

                response.type(
                        "application/json; charset=UTF-8"
                );

                return """
                        {
                          "success": false,
                          "message": "API endpoint not found",
                          "data": null
                        }
                        """;
            });


            // =========================================================
            // 11. XỬ LÝ EXCEPTION TOÀN CỤC
            // =========================================================

            exception(
                    Exception.class,
                    (exception, request, response) -> {

                        exception.printStackTrace();

                        response.status(500);

                        response.type(
                                "application/json; charset=UTF-8"
                        );

                        response.body("""
                                {
                                  "success": false,
                                  "message": "Internal server error",
                                  "data": null
                                }
                                """);
                    }
            );


            // =========================================================
            // 12. CHỜ SERVER KHỞI ĐỘNG
            // =========================================================

            awaitInitialization();


            // =========================================================
            // 13. HIỂN THỊ THÔNG TIN SERVER
            // =========================================================

            printServerInfo(
                    host,
                    httpPort,
                    websocketPort
            );

        } catch (Exception e) {

            System.err.println();

            System.err.println(
                    "=============================================="
            );

            System.err.println(
                    "       FASHION SHOP SERVER FAILED"
            );

            System.err.println(
                    "=============================================="
            );

            System.err.println(
                    "Lỗi: " + e.getMessage()
            );

            e.printStackTrace();

            System.err.println(
                    "=============================================="
            );

            System.exit(1);
        }
    }


    // =============================================================
    // HEALTH ROUTES
    // =============================================================

    private static void registerHealthRoutes(
            int httpPort
    ) {

        get("/api/health", (request, response) -> {

            response.status(200);

            response.type(
                    "application/json; charset=UTF-8"
            );

            return """
                    {
                      "success": true,
                      "message": "Fashion Shop Server is running",
                      "data": {
                        "service": "fashion-shop-server",
                        "status": "UP",
                        "variant_stock": true,
                        "checkout_idempotency": true,
                        "http_port": %d
                      }
                    }
                    """.formatted(httpPort);
        });


        get("/api/health/db", (request, response) -> {

            response.status(200);

            response.type(
                    "application/json; charset=UTF-8"
            );

            boolean databaseConnected;

            try {

                databaseConnected =
                        com.fashionshop.server.database
                                .DatabaseConnection
                                .testConnection();

            } catch (Exception ignored) {

                databaseConnected = false;
            }


            if (databaseConnected) {
                boolean checkoutSchemaReady;
                try (var connection = com.fashionshop.server.database.DatabaseConnection.getConnection();
                     var statement = connection.createStatement();
                     var rows = statement.executeQuery("SELECT 1 FROM order_requests LIMIT 1")) {
                    checkoutSchemaReady = true;
                } catch (java.sql.SQLException ignored) {
                    checkoutSchemaReady = false;
                }

                return """
                        {
                          "success": true,
                          "message": "Database connection is working",
                          "data": {
                            "database": "fashion_shop",
                            "status": "UP",
                            "checkout_idempotency": %s
                          }
                        }
                        """.formatted(checkoutSchemaReady);

            } else {

                response.status(503);

                return """
                        {
                          "success": false,
                          "message": "Database connection failed",
                          "data": {
                            "database": "fashion_shop",
                            "status": "DOWN"
                          }
                        }
                        """;
            }
        });


        System.out.println(
                "GET  /api/health"
        );

        System.out.println(
                "GET  /api/health/db"
        );
    }


    // =============================================================
    // CORS
    // =============================================================

    private static void enableCors() {

        options("/*", (request, response) -> {

            String accessControlRequestHeaders =
                    request.headers(
                            "Access-Control-Request-Headers"
                    );

            if (
                    accessControlRequestHeaders != null
            ) {

                response.header(
                        "Access-Control-Allow-Headers",
                        accessControlRequestHeaders
                );
            }


            String accessControlRequestMethod =
                    request.headers(
                            "Access-Control-Request-Method"
                    );

            if (
                    accessControlRequestMethod != null
            ) {

                response.header(
                        "Access-Control-Allow-Methods",
                        accessControlRequestMethod
                );
            }

            return "";
        });


        before((request, response) -> {

            response.header(
                    "Access-Control-Allow-Origin",
                    "*"
            );

            response.header(
                    "Access-Control-Allow-Methods",
                    "GET,POST,PUT,DELETE,OPTIONS"
            );

            response.header(
                    "Access-Control-Allow-Headers",
                    "Content-Type,Authorization"
            );

            response.header(
                    "Access-Control-Allow-Credentials",
                    "false"
            );
        });
    }


    // =============================================================
    // STARTUP BANNER
    // =============================================================

    private static void printBanner() {

        System.out.println();

        System.out.println(
                "=================================================="
        );

        System.out.println(
                "          FASHION SHOP JAVA SERVER"
        );

        System.out.println(
                "=================================================="
        );

        System.out.println(
                "  Java Server - HTTP REST - WebSocket - MySQL"
        );

        System.out.println(
                "=================================================="
        );

        System.out.println();
    }


    // =============================================================
    // SERVER INFO
    // =============================================================

    private static void printServerInfo(
            String host,
            int httpPort,
            int websocketPort
    ) {

        System.out.println();

        System.out.println(
                "=================================================="
        );

        System.out.println(
                "          FASHION SHOP SERVER READY"
        );

        System.out.println(
                "=================================================="
        );


        System.out.println(
                "HTTP Server    : http://"
                        + host
                        + ":"
                        + httpPort
        );

        System.out.println(
                "Health API     : http://localhost:"
                        + httpPort
                        + "/api/health"
        );

        System.out.println(
                "DB Health      : http://localhost:"
                        + httpPort
                        + "/api/health/db"
        );

        System.out.println(
                "Login API      : http://localhost:"
                        + httpPort
                        + "/api/auth/login"
        );

        System.out.println(
                "Category API   : http://localhost:"
                        + httpPort
                        + "/api/categories"
        );

        System.out.println(
                "Product API    : http://localhost:"
                        + httpPort
                        + "/api/products"
        );
        System.out.println(
                "WebSocket      : ws://"
                        + host
                        + ":"
                        + httpPort
                        + "/ws/chat"
        );
System.out.println(
                "Database       : fashion_shop"
        );

        System.out.println(
                "HTTP Status    : RUNNING"
        );

        System.out.println(
                "WebSocket      : RUNNING"
        );

        System.out.println(
                "=================================================="
        );

        System.out.println();
    }
}
