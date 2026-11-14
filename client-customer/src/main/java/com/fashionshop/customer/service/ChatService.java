package com.fashionshop.customer.service;

import com.fashionshop.customer.network.ApiClient;
import com.google.gson.Gson;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ChatService {

    private final ApiClient apiClient =
            new ApiClient();

    private final Gson gson =
            new Gson();


    // =========================================================
    // CUSTOMER
    // =========================================================

    public Conversation getCustomerConversation()
            throws Exception {

        String json =
                apiClient.get(
                        "/api/protected/chat/conversation"
                );

        ConversationResponse response =
                gson.fromJson(
                        json,
                        ConversationResponse.class
                );

        validate(
                response,
                "Không thể tải cuộc trò chuyện."
        );

        return response.data;
    }


    public List<ChatMessage> getCustomerMessages()
            throws Exception {

        String json =
                apiClient.get(
                        "/api/protected/chat/messages"
                );

        MessagesResponse response =
                gson.fromJson(
                        json,
                        MessagesResponse.class
                );

        validate(
                response,
                "Không thể tải tin nhắn."
        );

        if (response.data == null) {
            return Collections.emptyList();
        }

        return Arrays.asList(
                response.data
        );
    }


    public ChatMessage sendCustomerMessage(
            String message
    ) throws Exception {

        SendRequest request =
                new SendRequest();

        request.message =
                message;

        String json =
                apiClient.post(
                        "/api/protected/chat/messages",
                        gson.toJson(
                                request
                        )
                );

        MessageResponse response =
                gson.fromJson(
                        json,
                        MessageResponse.class
                );

        validate(
                response,
                "Không thể gửi tin nhắn."
        );

        return response.data;
    }


    // =========================================================
    // ADMIN
    // =========================================================

    public List<Conversation> getAdminConversations()
            throws Exception {

        String json =
                apiClient.get(
                        "/api/admin/chat/conversations"
                );

        ConversationsResponse response =
                gson.fromJson(
                        json,
                        ConversationsResponse.class
                );

        validate(
                response,
                "Không thể tải danh sách hội thoại."
        );

        if (response.data == null) {
            return Collections.emptyList();
        }

        return Arrays.asList(
                response.data
        );
    }


    public List<ChatMessage> getAdminMessages(
            long conversationId
    ) throws Exception {

        String json =
                apiClient.get(
                        "/api/admin/chat/conversations/"
                                + conversationId
                                + "/messages"
                );

        MessagesResponse response =
                gson.fromJson(
                        json,
                        MessagesResponse.class
                );

        validate(
                response,
                "Không thể tải tin nhắn."
        );

        if (response.data == null) {
            return Collections.emptyList();
        }

        return Arrays.asList(
                response.data
        );
    }


    public ChatMessage sendAdminMessage(
            long conversationId,
            String message
    ) throws Exception {

        SendRequest request =
                new SendRequest();

        request.message =
                message;

        String json =
                apiClient.post(
                        "/api/admin/chat/conversations/"
                                + conversationId
                                + "/messages",
                        gson.toJson(
                                request
                        )
                );

        MessageResponse response =
                gson.fromJson(
                        json,
                        MessageResponse.class
                );

        validate(
                response,
                "Không thể gửi tin nhắn."
        );

        return response.data;
    }


    // =========================================================
    // VALIDATION
    // =========================================================

    private void validate(
            BaseResponse response,
            String fallback
    ) {

        if (response == null) {

            throw new IllegalArgumentException(
                    fallback
            );
        }

        if (!response.success) {

            throw new IllegalArgumentException(
                    response.message == null
                            || response.message.isBlank()
                            ? fallback
                            : response.message
            );
        }
    }


    // =========================================================
    // RESPONSE CLASSES
    // =========================================================

    private static class BaseResponse {

        boolean success;
        String message;
    }


    private static class ConversationResponse
            extends BaseResponse {

        Conversation data;
    }


    private static class ConversationsResponse
            extends BaseResponse {

        Conversation[] data;
    }


    private static class MessageResponse
            extends BaseResponse {

        ChatMessage data;
    }


    private static class MessagesResponse
            extends BaseResponse {

        ChatMessage[] data;
    }


    private static class SendRequest {

        String message;
    }


    // =========================================================
    // DTO
    // =========================================================

    public static class Conversation {

        private long id;

        private long customerId;

        private String customerName;

        private String customerEmail;

        private long adminId;

        private String adminName;

        private String lastMessage;

        private String lastMessageAt;

        private String updatedAt;

        private int unreadCount;


        public long getId() {

            return id;
        }


        public long getCustomerId() {

            return customerId;
        }


        public String getCustomerName() {

            return customerName;
        }


        public String getCustomerEmail() {

            return customerEmail;
        }


        public long getAdminId() {

            return adminId;
        }


        public String getAdminName() {

            return adminName;
        }


        public String getLastMessage() {

            return lastMessage;
        }


        public String getLastMessageAt() {

            return lastMessageAt;
        }


        public String getUpdatedAt() {

            return updatedAt;
        }


        public int getUnreadCount() {

            return unreadCount;
        }


        @Override
        public String toString() {

            if (customerName != null
                    && !customerName.isBlank()) {

                return customerName;
            }

            return "Khách hàng #" + customerId;
        }
    }


    public static class ChatMessage {

        private long id;

        private long conversationId;

        private long senderId;

        private String senderName;

        private long receiverId;

        private String receiverName;

        private String message;

        private String messageType;

        private boolean read;

        private String createdAt;


        public long getId() {

            return id;
        }


        public long getConversationId() {

            return conversationId;
        }


        public long getSenderId() {

            return senderId;
        }


        public String getSenderName() {

            return senderName;
        }


        public long getReceiverId() {

            return receiverId;
        }


        public String getReceiverName() {

            return receiverName;
        }


        public String getMessage() {

            return message;
        }


        public String getMessageType() {

            return messageType;
        }


        public boolean isRead() {

            return read;
        }


        public String getCreatedAt() {

            return createdAt;
        }
    }
}