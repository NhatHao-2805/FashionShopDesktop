package com.fashionshop.customer.network;

import com.fashionshop.customer.config.ServerConfig;
import com.fashionshop.customer.service.SessionManager;
import com.google.gson.Gson;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public class ChatRealtimeClient
        implements WebSocket.Listener {

    private final Gson gson =
            new Gson();

    private final ApiClient apiClient =
            new ApiClient();

    private final HttpClient httpClient =
            HttpClient.newHttpClient();

    private final StringBuilder textBuffer =
            new StringBuilder();

    private Consumer<RealtimeEvent> eventHandler;

    private Consumer<Boolean> connectionHandler;

    private volatile WebSocket webSocket;

    private final AtomicBoolean manualClose =
            new AtomicBoolean(false);

    private final AtomicBoolean connecting =
            new AtomicBoolean(false);


    public void setEventHandler(
            Consumer<RealtimeEvent> eventHandler
    ) {

        this.eventHandler =
                eventHandler;
    }


    public void setConnectionHandler(
            Consumer<Boolean> connectionHandler
    ) {

        this.connectionHandler =
                connectionHandler;
    }


    public void connect() {

        if (manualClose.get()) {
            return;
        }

        if (!SessionManager
                .getInstance()
                .isLoggedIn()) {

            return;
        }

        WebSocket current =
                webSocket;

        if (current != null
                && !current.isOutputClosed()) {

            return;
        }

        if (!connecting.compareAndSet(
                false,
                true
        )) {

            return;
        }


        Thread thread =
                new Thread(
                        () -> {

                            try {

                                String ticket =
                                        requestSocketTicket();

                                String url =
                                        createWebSocketUrl(
                                                ticket
                                        );

                                httpClient
                                        .newWebSocketBuilder()
                                        .buildAsync(
                                                URI.create(
                                                        url
                                                ),
                                                this
                                        )
                                        .join();

                            } catch (Exception e) {

                                connecting.set(
                                        false
                                );

                                notifyConnection(
                                        false
                                );

                                scheduleReconnect();
                            }
                        }
                );

        thread.setDaemon(
                true
        );

        thread.start();
    }


    public void close() {

        manualClose.set(
                true
        );

        WebSocket current =
                webSocket;

        webSocket =
                null;

        if (current != null) {

            try {

                current.sendClose(
                        WebSocket.NORMAL_CLOSURE,
                        "Window closed"
                );

            } catch (Exception ignored) {
            }
        }

        notifyConnection(
                false
        );
    }


    public void ping() {

        WebSocket current =
                webSocket;

        if (current == null) {
            return;
        }

        try {

            current.sendText(
                    "PING",
                    true
            );

        } catch (Exception ignored) {
        }
    }


    private String requestSocketTicket()
            throws Exception {

        String json =
                apiClient.post(
                        "/api/protected/chat/socket-ticket",
                        "{}"
                );

        TicketResponse response =
                gson.fromJson(
                        json,
                        TicketResponse.class
                );

        if (response == null
                || !response.success
                || response.data == null
                || response.data.ticket == null
                || response.data.ticket.isBlank()) {

            throw new IllegalStateException(
                    response != null
                            && response.message != null
                            ? response.message
                            : "Không lấy được WebSocket ticket."
            );
        }

        return response.data.ticket;
    }


    private String createWebSocketUrl(
            String ticket
    ) {

        String base =
                ServerConfig.getBaseUrl();

        if (base.startsWith(
                "https://"
        )) {

            base =
                    "wss://"
                            + base.substring(
                            "https://".length()
                    );

        } else if (base.startsWith(
                "http://"
        )) {

            base =
                    "ws://"
                            + base.substring(
                            "http://".length()
                    );
        }

        return base
                + "/ws/chat?ticket="
                + ticket;
    }


    @Override
    public void onOpen(
            WebSocket webSocket
    ) {

        this.webSocket =
                webSocket;

        connecting.set(
                false
        );

        notifyConnection(
                true
        );

        webSocket.request(
                1
        );
    }


    @Override
    public CompletionStage<?> onText(
            WebSocket webSocket,
            CharSequence data,
            boolean last
    ) {

        synchronized (textBuffer) {

            textBuffer.append(
                    data
            );

            if (last) {

                String payload =
                        textBuffer.toString();

                textBuffer.setLength(
                        0
                );

                handlePayload(
                        payload
                );
            }
        }

        webSocket.request(
                1
        );

        return null;
    }


    @Override
    public CompletionStage<?> onClose(
            WebSocket webSocket,
            int statusCode,
            String reason
    ) {

        this.webSocket =
                null;

        connecting.set(
                false
        );

        notifyConnection(
                false
        );

        if (!manualClose.get()) {

            scheduleReconnect();
        }

        return null;
    }


    @Override
    public void onError(
            WebSocket webSocket,
            Throwable error
    ) {

        this.webSocket =
                null;

        connecting.set(
                false
        );

        notifyConnection(
                false
        );

        if (!manualClose.get()) {

            scheduleReconnect();
        }
    }


    private void handlePayload(
            String payload
    ) {

        try {

            RealtimeEvent event =
                    gson.fromJson(
                            payload,
                            RealtimeEvent.class
                    );

            if (event == null
                    || event.type == null) {

                return;
            }

            Consumer<RealtimeEvent> handler =
                    eventHandler;

            if (handler != null) {

                handler.accept(
                        event
                );
            }

        } catch (Exception ignored) {
        }
    }


    private void scheduleReconnect() {

        if (manualClose.get()) {
            return;
        }

        Thread thread =
                new Thread(
                        () -> {

                            try {

                                Thread.sleep(
                                        3000
                                );

                            } catch (InterruptedException e) {

                                Thread.currentThread()
                                        .interrupt();

                                return;
                            }

                            if (!manualClose.get()) {

                                connect();
                            }
                        }
                );

        thread.setDaemon(
                true
        );

        thread.start();
    }


    private void notifyConnection(
            boolean connected
    ) {

        Consumer<Boolean> handler =
                connectionHandler;

        if (handler != null) {

            handler.accept(
                    connected
            );
        }
    }


    private static class TicketResponse {

        boolean success;

        String message;

        TicketData data;
    }


    private static class TicketData {

        String ticket;

        int expiresInSeconds;
    }


    public static class RealtimeEvent {

        private String type;

        private Object data;

        private String message;


        public String getType() {

            return type;
        }


        public Object getData() {

            return data;
        }


        public String getMessage() {

            return message;
        }
    }
}