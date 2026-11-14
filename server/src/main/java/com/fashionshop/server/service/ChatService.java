package com.fashionshop.server.service;

import com.fashionshop.server.repository.ChatRepository;
import com.fashionshop.server.websocket.ChatSocketHub;

import java.util.List;
import java.util.Map;

public class ChatService {

    private final ChatRepository repository =
            new ChatRepository();

    public Map<String, Object> getCustomerConversation(
            long customerId
    ) {

        return repository
                .getOrCreateCustomerConversation(
                        customerId
                );
    }

    public List<Map<String, Object>> getCustomerMessages(
            long customerId
    ) {

        return repository.findCustomerMessages(
                customerId
        );
    }

    public Map<String, Object> sendCustomerMessage(
            long customerId,
            String message
    ) {

        Map<String, Object> result =
                repository.sendCustomerMessage(
                        customerId,
                        validateMessage(
                                message
                        )
                );

        ChatSocketHub.pushChatMessage(
                result
        );

        return result;
    }

    public List<Map<String, Object>> getAdminConversations(
            long adminId
    ) {

        return repository
                .findAdminConversations(
                        adminId
                );
    }

    public List<Map<String, Object>> getAdminMessages(
            long adminId,
            long conversationId
    ) {

        validateConversationId(
                conversationId
        );

        return repository
                .findAdminMessages(
                        adminId,
                        conversationId
                );
    }

    public Map<String, Object> sendAdminMessage(
            long adminId,
            long conversationId,
            String message
    ) {

        validateConversationId(
                conversationId
        );

        Map<String, Object> result =
                repository
                        .sendAdminMessage(
                                adminId,
                                conversationId,
                                validateMessage(
                                        message
                                )
                        );

        ChatSocketHub.pushChatMessage(
                result
        );

        return result;
    }

    public Map<String, Object> getUnreadCount(
            long userId
    ) {

        return Map.of(
                "count",
                repository.countUnreadForUser(
                        userId
                )
        );
    }

    private String validateMessage(
            String value
    ) {

        if (value == null) {

            throw new IllegalArgumentException(
                    "Tin nhắn không được để trống."
            );
        }

        String message =
                value.trim();

        if (message.isEmpty()) {

            throw new IllegalArgumentException(
                    "Tin nhắn không được để trống."
            );
        }

        if (message.length() > 2000) {

            throw new IllegalArgumentException(
                    "Tin nhắn tối đa 2000 ký tự."
            );
        }

        return message;
    }

    private void validateConversationId(
            long conversationId
    ) {

        if (conversationId <= 0) {

            throw new IllegalArgumentException(
                    "ID hội thoại không hợp lệ."
            );
        }
    }
}