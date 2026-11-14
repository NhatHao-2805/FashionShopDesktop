package com.fashionshop.server.websocket;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.StatusCode;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketError;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;

import java.util.List;
import java.util.Map;

@WebSocket
public class ChatWebSocket {

    private long userId =
            -1;

    private String role;

    private Session session;


    @OnWebSocketConnect
    public void onConnect(
            Session session
    ) {

        this.session =
                session;

        String ticket =
                getFirstParameter(
                        session,
                        "ticket"
                );

        ChatSocketHub.SocketIdentity identity =
                ChatSocketHub
                        .consumeTicket(
                                ticket
                        );

        if (identity == null) {

            try {

                session.close(
                        StatusCode.POLICY_VIOLATION,
                        "Invalid or expired socket ticket"
                );

            } catch (Exception ignored) {
            }

            return;
        }

        this.userId =
                identity.userId();

        this.role =
                identity.role();

        ChatSocketHub.register(
                userId,
                session
        );

        sendText(
                """
                {
                  "type": "CONNECTED",
                  "message": "Realtime chat connected"
                }
                """
        );

        System.out.println(
                "[WS] Connected userId="
                        + userId
                        + ", role="
                        + role
        );
    }


    @OnWebSocketMessage
    public void onMessage(
            String text
    ) {

        /*
         * Tin nhắn nghiệp vụ vẫn gửi qua REST.
         * WebSocket chỉ dùng push realtime.
         *
         * Client có thể gửi PING để giữ kết nối.
         */
        if ("PING".equalsIgnoreCase(
                text == null
                        ? ""
                        : text.trim()
        )) {

            sendText(
                    """
                    {
                      "type": "PONG"
                    }
                    """
            );
        }
    }


    @OnWebSocketClose
    public void onClose(
            int statusCode,
            String reason
    ) {

        if (userId > 0
                && session != null) {

            ChatSocketHub.unregister(
                    userId,
                    session
            );
        }

        System.out.println(
                "[WS] Closed userId="
                        + userId
                        + ", code="
                        + statusCode
                        + ", reason="
                        + reason
        );
    }


    @OnWebSocketError
    public void onError(
            Throwable error
    ) {

        System.err.println(
                "[WS] Error userId="
                        + userId
                        + ": "
                        + (
                        error == null
                                ? "unknown"
                                : error.getMessage()
                )
        );
    }


    private String getFirstParameter(
            Session session,
            String key
    ) {

        if (session == null
                || session.getUpgradeRequest()
                == null) {

            return null;
        }

        Map<String, List<String>> parameters =
                session
                        .getUpgradeRequest()
                        .getParameterMap();

        if (parameters == null) {
            return null;
        }

        List<String> values =
                parameters.get(
                        key
                );

        if (values == null
                || values.isEmpty()) {

            return null;
        }

        return values.get(0);
    }


    private void sendText(
            String text
    ) {

        try {

            if (session != null
                    && session.isOpen()) {

                session
                        .getRemote()
                        .sendString(
                                text
                        );
            }

        } catch (Exception e) {

            System.err.println(
                    "[WS] Send failed: "
                            + e.getMessage()
            );
        }
    }
}