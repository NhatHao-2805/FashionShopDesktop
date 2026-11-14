package com.fashionshop.server.websocket;

import com.google.gson.Gson;
import org.eclipse.jetty.websocket.api.Session;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

public final class ChatSocketHub {

    private static final long TICKET_LIFETIME_MS =
            60_000L;

    private static final Gson GSON =
            new Gson();

    private static final Map<String, Ticket> TICKETS =
            new ConcurrentHashMap<>();

    private static final Map<Long, CopyOnWriteArraySet<Session>>
            USER_SESSIONS =
            new ConcurrentHashMap<>();


    private ChatSocketHub() {
    }


    // =========================================================
    // SHORT-LIVED SOCKET TICKETS
    // =========================================================

    public static String issueTicket(
            long userId,
            String role
    ) {

        cleanupExpiredTickets();

        String ticket =
                UUID.randomUUID()
                        .toString()
                        .replace(
                                "-",
                                ""
                        );

        long expiresAt =
                System.currentTimeMillis()
                        + TICKET_LIFETIME_MS;

        TICKETS.put(
                ticket,
                new Ticket(
                        userId,
                        role,
                        expiresAt
                )
        );

        return ticket;
    }


    public static SocketIdentity consumeTicket(
            String ticketValue
    ) {

        if (ticketValue == null
                || ticketValue.isBlank()) {

            return null;
        }

        Ticket ticket =
                TICKETS.remove(
                        ticketValue
                );

        if (ticket == null) {

            return null;
        }

        if (ticket.expiresAt()
                < System.currentTimeMillis()) {

            return null;
        }

        return new SocketIdentity(
                ticket.userId(),
                ticket.role()
        );
    }


    // =========================================================
    // SESSION MANAGEMENT
    // =========================================================

    public static void register(
            long userId,
            Session session
    ) {

        USER_SESSIONS
                .computeIfAbsent(
                        userId,
                        ignored ->
                                new CopyOnWriteArraySet<>()
                )
                .add(
                        session
                );

        System.out.println(
                "[WS] User "
                        + userId
                        + " connected. Connections="
                        + USER_SESSIONS
                        .get(userId)
                        .size()
        );
    }


    public static void unregister(
            long userId,
            Session session
    ) {

        CopyOnWriteArraySet<Session> sessions =
                USER_SESSIONS.get(
                        userId
                );

        if (sessions == null) {
            return;
        }

        sessions.remove(
                session
        );

        if (sessions.isEmpty()) {

            USER_SESSIONS.remove(
                    userId
            );
        }

        System.out.println(
                "[WS] User "
                        + userId
                        + " disconnected."
        );
    }


    // =========================================================
    // REALTIME EVENTS
    // =========================================================

    public static void pushChatMessage(
            Map<String, Object> message
    ) {

        if (message == null) {
            return;
        }

        Object receiverValue =
                message.get(
                        "receiverId"
                );

        if (!(receiverValue
                instanceof Number receiverNumber)) {

            return;
        }

        long receiverId =
                receiverNumber.longValue();

        Map<String, Object> event =
                Map.of(
                        "type",
                        "CHAT_MESSAGE",
                        "data",
                        message
                );

        pushToUser(
                receiverId,
                GSON.toJson(
                        event
                )
        );
    }


    public static void pushRefresh(
            long userId
    ) {

        Map<String, Object> event =
                Map.of(
                        "type",
                        "CHAT_REFRESH"
                );

        pushToUser(
                userId,
                GSON.toJson(
                        event
                )
        );
    }


    private static void pushToUser(
            long userId,
            String payload
    ) {

        CopyOnWriteArraySet<Session> sessions =
                USER_SESSIONS.get(
                        userId
                );

        if (sessions == null
                || sessions.isEmpty()) {

            return;
        }

        for (Session session :
                List.copyOf(sessions)) {

            try {

                if (session != null
                        && session.isOpen()) {

                    session
                            .getRemote()
                            .sendString(
                                    payload
                            );

                } else {

                    sessions.remove(
                            session
                    );
                }

            } catch (Exception e) {

                sessions.remove(
                        session
                );

                System.err.println(
                        "[WS] Push failed for user "
                                + userId
                                + ": "
                                + e.getMessage()
                );
            }
        }

        if (sessions.isEmpty()) {

            USER_SESSIONS.remove(
                    userId
            );
        }
    }


    private static void cleanupExpiredTickets() {

        long now =
                System.currentTimeMillis();

        TICKETS
                .entrySet()
                .removeIf(
                        entry ->
                                entry
                                        .getValue()
                                        .expiresAt()
                                        < now
                );
    }


    private record Ticket(
            long userId,
            String role,
            long expiresAt
    ) {
    }


    public record SocketIdentity(
            long userId,
            String role
    ) {
    }
}