package com.fashionshop.server.controller;

import com.fashionshop.server.security.JwtAuthentication;
import com.fashionshop.server.service.ChatService;
import com.fashionshop.server.websocket.ChatSocketHub;
import com.google.gson.Gson;

import static spark.Spark.get;
import static spark.Spark.post;

public class ChatController {

    private final ChatService service =
            new ChatService();

    private final Gson gson =
            new Gson();

    public void registerRoutes() {

        // =========================================================
        // CUSTOMER
        // =========================================================

        get(
                "/api/protected/chat/conversation",
                (request, response) -> {

                    response.type(
                            "application/json; charset=UTF-8"
                    );

                    try {

                        JwtAuthentication auth =
                                requireCustomer(
                                        request.attribute(
                                                "authentication"
                                        )
                                );

                        return success(
                                "Lấy hội thoại thành công.",
                                service.getCustomerConversation(
                                        auth.getUserId()
                                )
                        );

                    } catch (SecurityException e) {

                        response.status(403);
                        return error(e.getMessage());

                    } catch (Exception e) {

                        e.printStackTrace();
                        response.status(500);

                        return error(
                                "Không thể tải hội thoại."
                        );
                    }
                }
        );

        get(
                "/api/protected/chat/messages",
                (request, response) -> {

                    response.type(
                            "application/json; charset=UTF-8"
                    );

                    try {

                        JwtAuthentication auth =
                                requireCustomer(
                                        request.attribute(
                                                "authentication"
                                        )
                                );

                        return success(
                                "Lấy tin nhắn thành công.",
                                service.getCustomerMessages(
                                        auth.getUserId()
                                )
                        );

                    } catch (SecurityException e) {

                        response.status(403);
                        return error(e.getMessage());

                    } catch (Exception e) {

                        e.printStackTrace();
                        response.status(500);

                        return error(
                                "Không thể tải tin nhắn."
                        );
                    }
                }
        );

        post(
                "/api/protected/chat/messages",
                (request, response) -> {

                    response.type(
                            "application/json; charset=UTF-8"
                    );

                    try {

                        JwtAuthentication auth =
                                requireCustomer(
                                        request.attribute(
                                                "authentication"
                                        )
                                );

                        SendMessageRequest body =
                                gson.fromJson(
                                        request.body(),
                                        SendMessageRequest.class
                                );

                        if (body == null) {

                            throw new IllegalArgumentException(
                                    "Request không hợp lệ."
                            );
                        }

                        response.status(201);

                        return success(
                                "Gửi tin nhắn thành công.",
                                service.sendCustomerMessage(
                                        auth.getUserId(),
                                        body.message
                                )
                        );

                    } catch (
                            IllegalArgumentException e
                    ) {

                        response.status(400);
                        return error(e.getMessage());

                    } catch (SecurityException e) {

                        response.status(403);
                        return error(e.getMessage());

                    } catch (Exception e) {

                        e.printStackTrace();
                        response.status(500);

                        return error(
                                "Không thể gửi tin nhắn."
                        );
                    }
                }
        );


        // =========================================================
        // ADMIN
        // =========================================================

        get(
                "/api/admin/chat/conversations",
                (request, response) -> {

                    response.type(
                            "application/json; charset=UTF-8"
                    );

                    try {

                        JwtAuthentication auth =
                                requireAdmin(
                                        request.attribute(
                                                "authentication"
                                        )
                                );

                        return success(
                                "Lấy danh sách hội thoại thành công.",
                                service.getAdminConversations(
                                        auth.getUserId()
                                )
                        );

                    } catch (SecurityException e) {

                        response.status(403);
                        return error(e.getMessage());

                    } catch (Exception e) {

                        e.printStackTrace();
                        response.status(500);

                        return error(
                                "Không thể tải hội thoại."
                        );
                    }
                }
        );

        get(
                "/api/admin/chat/conversations/:id/messages",
                (request, response) -> {

                    response.type(
                            "application/json; charset=UTF-8"
                    );

                    try {

                        JwtAuthentication auth =
                                requireAdmin(
                                        request.attribute(
                                                "authentication"
                                        )
                                );

                        long conversationId =
                                parseId(
                                        request.params(":id")
                                );

                        return success(
                                "Lấy tin nhắn thành công.",
                                service.getAdminMessages(
                                        auth.getUserId(),
                                        conversationId
                                )
                        );

                    } catch (
                            IllegalArgumentException e
                    ) {

                        response.status(400);
                        return error(e.getMessage());

                    } catch (SecurityException e) {

                        response.status(403);
                        return error(e.getMessage());

                    } catch (Exception e) {

                        e.printStackTrace();
                        response.status(500);

                        return error(
                                "Không thể tải tin nhắn."
                        );
                    }
                }
        );

        post(
                "/api/admin/chat/conversations/:id/messages",
                (request, response) -> {

                    response.type(
                            "application/json; charset=UTF-8"
                    );

                    try {

                        JwtAuthentication auth =
                                requireAdmin(
                                        request.attribute(
                                                "authentication"
                                        )
                                );

                        long conversationId =
                                parseId(
                                        request.params(":id")
                                );

                        SendMessageRequest body =
                                gson.fromJson(
                                        request.body(),
                                        SendMessageRequest.class
                                );

                        if (body == null) {

                            throw new IllegalArgumentException(
                                    "Request không hợp lệ."
                            );
                        }

                        response.status(201);

                        return success(
                                "Gửi tin nhắn thành công.",
                                service.sendAdminMessage(
                                        auth.getUserId(),
                                        conversationId,
                                        body.message
                                )
                        );

                    } catch (
                            IllegalArgumentException e
                    ) {

                        response.status(400);
                        return error(e.getMessage());

                    } catch (SecurityException e) {

                        response.status(403);
                        return error(e.getMessage());

                    } catch (Exception e) {

                        e.printStackTrace();
                        response.status(500);

                        return error(
                                "Không thể gửi tin nhắn."
                        );
                    }
                }
        );

                post(
                "/api/protected/chat/socket-ticket",
                (request, response) -> {

                    response.type(
                            "application/json; charset=UTF-8"
                    );

                    try {

                        JwtAuthentication auth =
                                requireAuthenticated(
                                        request.attribute(
                                                "authentication"
                                        )
                                );

                        String role =
                                auth.isAdmin()
                                        ? "ADMIN"
                                        : (
                                        auth.isCustomer()
                                                ? "CUSTOMER"
                                                : "USER"
                                );

                        String ticket =
                                ChatSocketHub.issueTicket(
                                        auth.getUserId(),
                                        role
                                );

                        return success(
                                "Tạo WebSocket ticket thành công.",
                                java.util.Map.of(
                                        "ticket",
                                        ticket,
                                        "expiresInSeconds",
                                        60
                                )
                        );

                    } catch (SecurityException e) {

                        response.status(403);

                        return error(
                                e.getMessage()
                        );

                    } catch (Exception e) {

                        e.printStackTrace();

                        response.status(500);

                        return error(
                                "Không thể tạo WebSocket ticket."
                        );
                    }
                }
        );

get(
                "/api/protected/chat/unread-count",
                (request, response) -> {

                    response.type(
                            "application/json; charset=UTF-8"
                    );

                    try {

                        JwtAuthentication auth =
                                requireAuthenticated(
                                        request.attribute(
                                                "authentication"
                                        )
                                );

                        return success(
                                "Lấy số tin chưa đọc thành công.",
                                service.getUnreadCount(
                                        auth.getUserId()
                                )
                        );

                    } catch (SecurityException e) {

                        response.status(403);
                        return error(e.getMessage());

                    } catch (Exception e) {

                        e.printStackTrace();
                        response.status(500);

                        return error(
                                "Không thể tải số tin chưa đọc."
                        );
                    }
                }
        );
    }

    private JwtAuthentication requireAuthenticated(
            JwtAuthentication auth
    ) {

        if (auth == null) {

            throw new SecurityException(
                    "Unauthorized."
            );
        }

        return auth;
    }

    private JwtAuthentication requireCustomer(
            JwtAuthentication auth
    ) {

        requireAuthenticated(
                auth
        );

        if (!auth.isCustomer()) {

            throw new SecurityException(
                    "Chỉ CUSTOMER được sử dụng chức năng này."
            );
        }

        return auth;
    }

    private JwtAuthentication requireAdmin(
            JwtAuthentication auth
    ) {

        requireAuthenticated(
                auth
        );

        if (!auth.isAdmin()) {

            throw new SecurityException(
                    "Chỉ ADMIN được sử dụng chức năng này."
            );
        }

        return auth;
    }

    private long parseId(
            String value
    ) {

        try {

            long id =
                    Long.parseLong(
                            value
                    );

            if (id <= 0) {
                throw new NumberFormatException();
            }

            return id;

        } catch (Exception e) {

            throw new IllegalArgumentException(
                    "ID hội thoại không hợp lệ."
            );
        }
    }

    private String success(
            String message,
            Object data
    ) {

        ApiResponse response =
                new ApiResponse();

        response.success = true;
        response.message = message;
        response.data = data;

        return gson.toJson(
                response
        );
    }

    private String error(
            String message
    ) {

        ApiResponse response =
                new ApiResponse();

        response.success = false;
        response.message = message;
        response.data = null;

        return gson.toJson(
                response
        );
    }

    private static class SendMessageRequest {

        String message;
    }

    private static class ApiResponse {

        boolean success;
        String message;
        Object data;
    }
}