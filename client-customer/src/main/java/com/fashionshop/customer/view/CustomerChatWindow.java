package com.fashionshop.customer.view;

import com.fashionshop.customer.model.User;
import com.fashionshop.customer.network.ChatRealtimeClient;
import com.fashionshop.customer.service.ChatService;
import com.fashionshop.customer.service.SessionManager;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.util.Duration;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class CustomerChatWindow {

    private final ChatService chatService =
            new ChatService();

    private final ChatRealtimeClient realtimeClient =
            new ChatRealtimeClient();

    private final SessionManager sessionManager =
            SessionManager.getInstance();

    private final VBox messageBox =
            new VBox(10);

    private final ScrollPane messageScroll =
            new ScrollPane(messageBox);

    private final TextArea messageInput =
            new TextArea();

    private final Label statusLabel =
            new Label();

    private final Label adminLabel =
            new Label(
                    "Đang kết nối với nhân viên hỗ trợ..."
            );

    private final Button sendButton =
            new Button("Gửi");

    private Timeline refreshTimeline;

    private boolean loading =
            false;

    private long lastRenderedMessageId =
            -1;


    public static void show() {

        new CustomerChatWindow()
                .open();
    }


    private void open() {

        User currentUser =
                sessionManager.getCurrentUser();

        if (currentUser == null) {

            Alert alert =
                    new Alert(
                            Alert.AlertType.WARNING
                    );

            alert.setTitle(
                    "Chat với Shop"
            );

            alert.setHeaderText(
                    "Bạn chưa đăng nhập"
            );

            alert.setContentText(
                    "Vui lòng đăng nhập trước khi sử dụng chat."
            );

            alert.showAndWait();

            return;
        }


        BorderPane root =
                new BorderPane();

        root.setStyle(
                "-fx-background-color: #f3f4f6;"
        );


        // =====================================================
        // HEADER
        // =====================================================

        Label title =
                new Label(
                        "💬 Chat với Fashion Shop"
                );

        title.setStyle(
                "-fx-font-size: 22px;"
                        + "-fx-font-weight: bold;"
                        + "-fx-text-fill: #111827;"
        );

        adminLabel.setStyle(
                "-fx-text-fill: #6b7280;"
        );

        VBox heading =
                new VBox(
                        4,
                        title,
                        adminLabel
                );

        heading.setPadding(
                new Insets(16)
        );

        heading.setStyle(
                "-fx-background-color: white;"
                        + "-fx-border-color: transparent transparent #e5e7eb transparent;"
        );

        root.setTop(
                heading
        );


        // =====================================================
        // MESSAGE AREA
        // =====================================================

        messageBox.setPadding(
                new Insets(16)
        );

        messageBox.setFillWidth(
                true
        );

        messageScroll.setFitToWidth(
                true
        );

        messageScroll.setStyle(
                "-fx-background-color: transparent;"
                        + "-fx-background: #f3f4f6;"
        );

        root.setCenter(
                messageScroll
        );


        // =====================================================
        // INPUT
        // =====================================================

        messageInput.setPromptText(
                "Nhập câu hỏi về sản phẩm, đơn hàng hoặc khiếu nại..."
        );

        messageInput.setWrapText(
                true
        );

        messageInput.setPrefRowCount(
                2
        );

        sendButton.setStyle(
                "-fx-background-color: #2563eb;"
                        + "-fx-text-fill: white;"
                        + "-fx-font-weight: bold;"
                        + "-fx-background-radius: 8;"
        );

        sendButton.setPrefWidth(
                90
        );

        sendButton.setOnAction(
                event -> sendMessage()
        );

        messageInput.setOnKeyPressed(
                event -> {

                    if (event.isControlDown()
                            && event.getCode()
                            == javafx.scene.input.KeyCode.ENTER) {

                        sendMessage();
                    }
                }
        );

        HBox inputRow =
                new HBox(
                        10,
                        messageInput,
                        sendButton
                );

        HBox.setHgrow(
                messageInput,
                Priority.ALWAYS
        );

        inputRow.setAlignment(
                Pos.CENTER
        );

        VBox bottom =
                new VBox(
                        6,
                        inputRow,
                        statusLabel
                );

        bottom.setPadding(
                new Insets(14)
        );

        bottom.setStyle(
                "-fx-background-color: white;"
                        + "-fx-border-color: #e5e7eb transparent transparent transparent;"
        );

        root.setBottom(
                bottom
        );


        Scene scene =
                new Scene(
                        root,
                        720,
                        620
                );

        PageNavigation.show("Chat với shop", scene, this::stopRefresh);


        

        configureRealtime();



        loadConversationInfo();

        refreshMessages();


        refreshTimeline =
                new Timeline(
                        new KeyFrame(
                                Duration.seconds(10),
                                event -> refreshMessages()
                        )
                );

        refreshTimeline.setCycleCount(
                Timeline.INDEFINITE
        );

        refreshTimeline.play();
    }


    private void configureRealtime() {

        realtimeClient.setConnectionHandler(
                connected ->
                        Platform.runLater(
                                () -> {

                                    if (connected) {

                                        statusLabel.setText(
                                                "Realtime đã kết nối."
                                        );

                                        statusLabel.setStyle(
                                                "-fx-text-fill: #16a34a;"
                                        );

                                    } else {

                                        statusLabel.setText(
                                                "Đang dùng chế độ dự phòng..."
                                        );

                                        statusLabel.setStyle(
                                                "-fx-text-fill: #d97706;"
                                        );
                                    }
                                }
                        )
        );


        realtimeClient.setEventHandler(
                event -> {

                    if (event == null
                            || event.getType() == null) {

                        return;
                    }

                    if ("CHAT_MESSAGE".equalsIgnoreCase(
                            event.getType()
                    )
                            || "CHAT_REFRESH".equalsIgnoreCase(
                            event.getType()
                    )) {

                        Platform.runLater(
                                () -> {

                                    lastRenderedMessageId =
                                            -1;

                                    refreshMessages();
                                }
                        );
                    }
                }
        );


        realtimeClient.connect();
    }

    private void loadConversationInfo() {

        Task<ChatService.Conversation> task =
                new Task<>() {

                    @Override
                    protected ChatService.Conversation call()
                            throws Exception {

                        return chatService
                                .getCustomerConversation();
                    }
                };

        task.setOnSucceeded(
                event -> {

                    ChatService.Conversation conversation =
                            task.getValue();

                    if (conversation != null
                            && conversation.getAdminName() != null) {

                        adminLabel.setText(
                                "Đang chat với: "
                                        + conversation.getAdminName()
                        );
                    }
                }
        );

        Thread thread =
                new Thread(task);

        thread.setDaemon(
                true
        );

        thread.start();
    }


    private void refreshMessages() {

        if (loading) {
            return;
        }

        loading =
                true;

        Task<List<ChatService.ChatMessage>> task =
                new Task<>() {

                    @Override
                    protected List<ChatService.ChatMessage> call()
                            throws Exception {

                        return chatService
                                .getCustomerMessages();
                    }
                };

        task.setOnSucceeded(
                event -> {

                    loading =
                            false;

                    displayMessages(
                            task.getValue()
                    );

                    statusLabel.setText(
                            "Đã kết nối với shop."
                    );

                    statusLabel.setStyle(
                            "-fx-text-fill: #16a34a;"
                    );
                }
        );

        task.setOnFailed(
                event -> {

                    loading =
                            false;

                    Throwable error =
                            task.getException();

                    statusLabel.setText(
                            error == null
                                    || error.getMessage() == null
                                    ? "Không thể cập nhật tin nhắn."
                                    : error.getMessage()
                    );

                    statusLabel.setStyle(
                            "-fx-text-fill: #dc2626;"
                    );
                }
        );

        Thread thread =
                new Thread(task);

        thread.setDaemon(
                true
        );

        thread.start();
    }


    private void displayMessages(
            List<ChatService.ChatMessage> messages
    ) {

        if (messages == null) {
            return;
        }

        long newestId =
                messages.isEmpty()
                        ? 0
                        : messages
                        .get(
                                messages.size() - 1
                        )
                        .getId();

        if (newestId == lastRenderedMessageId) {
            return;
        }

        lastRenderedMessageId =
                newestId;

        messageBox
                .getChildren()
                .clear();

        User currentUser =
                sessionManager.getCurrentUser();

        long currentUserId =
                currentUser == null
                        ? -1
                        : currentUser.getId();

        if (messages.isEmpty()) {

            Label empty =
                    new Label(
                            "Chưa có tin nhắn.\nHãy gửi câu hỏi đầu tiên cho shop."
                    );

            empty.setStyle(
                    "-fx-text-fill: #6b7280;"
                            + "-fx-font-size: 14px;"
            );

            messageBox
                    .getChildren()
                    .add(
                            empty
                    );

            return;
        }


        for (
                ChatService.ChatMessage message
                : messages
        ) {

            boolean mine =
                    message.getSenderId()
                            == currentUserId;

            VBox bubble =
                    createBubble(
                            message,
                            mine
                    );

            HBox row =
                    new HBox(
                            bubble
                    );

            row.setAlignment(
                    mine
                            ? Pos.CENTER_RIGHT
                            : Pos.CENTER_LEFT
            );

            messageBox
                    .getChildren()
                    .add(
                            row
                    );
        }


        Platform.runLater(
                () -> messageScroll
                        .setVvalue(
                                1.0
                        )
        );
    }


    private VBox createBubble(
            ChatService.ChatMessage message,
            boolean mine
    ) {

        Label sender =
                new Label(
                        mine
                                ? "Bạn"
                                : (
                                message.getSenderName()
                                        == null
                                        ? "Fashion Shop"
                                        : message.getSenderName()
                        )
                );

        sender.setStyle(
                "-fx-font-size: 11px;"
                        + "-fx-font-weight: bold;"
                        + "-fx-text-fill: "
                        + (
                        mine
                                ? "#dbeafe"
                                : "#6b7280"
                )
                        + ";"
        );


        Label content =
                new Label(
                        message.getMessage()
                );

        content.setWrapText(
                true
        );

        content.setMaxWidth(
                430
        );

        content.setStyle(
                "-fx-font-size: 14px;"
                        + "-fx-text-fill: "
                        + (
                        mine
                                ? "white"
                                : "#111827"
                )
                        + ";"
        );


        Label time =
                new Label(
                        formatTime(
                                message.getCreatedAt()
                        )
                );

        time.setStyle(
                "-fx-font-size: 10px;"
                        + "-fx-text-fill: "
                        + (
                        mine
                                ? "#dbeafe"
                                : "#9ca3af"
                )
                        + ";"
        );


        VBox bubble =
                new VBox(
                        4,
                        sender,
                        content,
                        time
                );

        bubble.setPadding(
                new Insets(
                        9,
                        12,
                        9,
                        12
                )
        );

        bubble.setMaxWidth(
                470
        );

        bubble.setStyle(
                mine
                        ? "-fx-background-color: #2563eb;"
                        + "-fx-background-radius: 14;"
                        : "-fx-background-color: white;"
                        + "-fx-border-color: #e5e7eb;"
                        + "-fx-border-radius: 14;"
                        + "-fx-background-radius: 14;"
        );

        return bubble;
    }


    private void sendMessage() {

        String text =
                messageInput.getText();

        if (text == null
                || text.trim().isEmpty()) {

            return;
        }

        String message =
                text.trim();

        sendButton.setDisable(
                true
        );

        statusLabel.setText(
                "Đang gửi..."
        );

        Task<ChatService.ChatMessage> task =
                new Task<>() {

                    @Override
                    protected ChatService.ChatMessage call()
                            throws Exception {

                        return chatService
                                .sendCustomerMessage(
                                        message
                                );
                    }
                };

        task.setOnSucceeded(
                event -> {

                    sendButton.setDisable(
                            false
                    );

                    messageInput.clear();

                    lastRenderedMessageId =
                            -1;

                    refreshMessages();
                }
        );

        task.setOnFailed(
                event -> {

                    sendButton.setDisable(
                            false
                    );

                    Throwable error =
                            task.getException();

                    statusLabel.setText(
                            error == null
                                    || error.getMessage() == null
                                    ? "Không gửi được tin nhắn."
                                    : error.getMessage()
                    );

                    statusLabel.setStyle(
                            "-fx-text-fill: #dc2626;"
                    );
                }
        );

        Thread thread =
                new Thread(task);

        thread.setDaemon(
                true
        );

        thread.start();
    }


    private String formatTime(
            String value
    ) {

        if (value == null
                || value.isBlank()) {

            return "";
        }

        try {

            LocalDateTime dateTime =
                    LocalDateTime.parse(
                            value
                    );

            return dateTime.format(
                    DateTimeFormatter.ofPattern(
                            "dd/MM/yyyy HH:mm"
                    )
            );

        } catch (Exception e) {

            return value
                    .replace(
                            "T",
                            " "
                    );
        }
    }


    private void stopRefresh() {

        

        realtimeClient.close();
if (refreshTimeline != null) {

            refreshTimeline.stop();
        }
    }
}