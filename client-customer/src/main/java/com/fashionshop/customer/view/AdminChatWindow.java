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
import javafx.stage.Stage;
import javafx.util.Duration;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class AdminChatWindow {

    private final ChatService chatService =
            new ChatService();

    private final ChatRealtimeClient realtimeClient =
            new ChatRealtimeClient();

    private final SessionManager sessionManager =
            SessionManager.getInstance();

    private final ListView<ChatService.Conversation>
            conversationList =
            new ListView<>();

    private final VBox messageBox =
            new VBox(10);

    private final ScrollPane messageScroll =
            new ScrollPane(
                    messageBox
            );

    private final Label customerTitle =
            new Label(
                    "Chọn khách hàng để trò chuyện"
            );

    private final Label customerInfo =
            new Label();

    private final Label statusLabel =
            new Label();

    private final TextArea messageInput =
            new TextArea();

    private final Button sendButton =
            new Button(
                    "Gửi"
            );

    private Timeline refreshTimeline;

    private boolean loadingConversations =
            false;

    private boolean loadingMessages =
            false;

    private long selectedConversationId =
            -1;

    private long lastRenderedMessageId =
            -1;


    public static void show() {

        new AdminChatWindow()
                .open();
    }


    private void open() {

        User user =
                sessionManager.getCurrentUser();

        if (user == null
                || !"ADMIN".equalsIgnoreCase(
                user.getRole()
        )) {

            Alert alert =
                    new Alert(
                            Alert.AlertType.ERROR
                    );

            alert.setHeaderText(
                    "Không có quyền truy cập"
            );

            alert.setContentText(
                    "Chỉ tài khoản ADMIN được sử dụng chức năng này."
            );

            alert.showAndWait();

            return;
        }


        Stage stage =
                new Stage();

        BorderPane root =
                new BorderPane();

        root.setStyle(
                "-fx-background-color: #f3f4f6;"
        );


        // =====================================================
        // LEFT SIDE - CONVERSATIONS
        // =====================================================

        Label inboxTitle =
                new Label(
                        "💬 Tin nhắn khách hàng"
                );

        inboxTitle.setStyle(
                "-fx-font-size: 20px;"
                        + "-fx-font-weight: bold;"
        );


        Label inboxSubtitle =
                new Label(
                        "Chọn khách hàng để xem hội thoại"
                );

        inboxSubtitle.setStyle(
                "-fx-text-fill: #6b7280;"
        );


        conversationList.setPrefWidth(
                320
        );


        conversationList.setCellFactory(
                listView ->
                        new ListCell<>() {

                            @Override
                            protected void updateItem(
                                    ChatService.Conversation item,
                                    boolean empty
                            ) {

                                super.updateItem(
                                        item,
                                        empty
                                );

                                if (empty
                                        || item == null) {

                                    setText(
                                            null
                                    );

                                    setGraphic(
                                            null
                                    );

                                    return;
                                }


                                Label name =
                                        new Label(
                                                item.getCustomerName()
                                                        == null
                                                        ? "Khách hàng #"
                                                        + item.getCustomerId()
                                                        : item.getCustomerName()
                                        );

                                name.setStyle(
                                        "-fx-font-weight: bold;"
                                );


                                String previewText =
                                        item.getLastMessage();

                                if (previewText == null
                                        || previewText.isBlank()) {

                                    previewText =
                                            "Chưa có tin nhắn";
                                }

                                if (previewText.length()
                                        > 35) {

                                    previewText =
                                            previewText.substring(
                                                    0,
                                                    35
                                            )
                                                    + "...";
                                }


                                Label preview =
                                        new Label(
                                                previewText
                                        );

                                preview.setStyle(
                                        "-fx-text-fill: #6b7280;"
                                                + "-fx-font-size: 12px;"
                                );


                                VBox info =
                                        new VBox(
                                                3,
                                                name,
                                                preview
                                        );


                                Region spacer =
                                        new Region();

                                HBox.setHgrow(
                                        spacer,
                                        Priority.ALWAYS
                                );


                                Label unread =
                                        new Label();

                                if (item.getUnreadCount()
                                        > 0) {

                                    unread.setText(
                                            String.valueOf(
                                                    item.getUnreadCount()
                                            )
                                    );

                                    unread.setStyle(
                                            "-fx-background-color: #dc2626;"
                                                    + "-fx-text-fill: white;"
                                                    + "-fx-font-weight: bold;"
                                                    + "-fx-background-radius: 20;"
                                                    + "-fx-padding: 3 8 3 8;"
                                    );

                                } else {

                                    unread.setVisible(
                                            false
                                    );

                                    unread.setManaged(
                                            false
                                    );
                                }


                                HBox row =
                                        new HBox(
                                                8,
                                                info,
                                                spacer,
                                                unread
                                        );

                                row.setAlignment(
                                        Pos.CENTER_LEFT
                                );

                                row.setPadding(
                                        new Insets(
                                                7
                                        )
                                );

                                setGraphic(
                                        row
                                );
                            }
                        }
        );


        conversationList
                .getSelectionModel()
                .selectedItemProperty()
                .addListener(
                        (
                                observable,
                                oldConversation,
                                newConversation
                        ) -> {

                            if (newConversation == null) {
                                return;
                            }

                            selectedConversationId =
                                    newConversation.getId();

                            lastRenderedMessageId =
                                    -1;

                            customerTitle.setText(
                                    newConversation
                                            .getCustomerName()
                                            == null
                                            ? "Khách hàng #"
                                            + newConversation
                                            .getCustomerId()
                                            : newConversation
                                            .getCustomerName()
                            );

                            customerInfo.setText(
                                    newConversation
                                            .getCustomerEmail()
                                            == null
                                            ? ""
                                            : newConversation
                                            .getCustomerEmail()
                            );

                            refreshMessages();
                        }
                );


        VBox left =
                new VBox(
                        8,
                        inboxTitle,
                        inboxSubtitle,
                        new Separator(),
                        conversationList
                );

        VBox.setVgrow(
                conversationList,
                Priority.ALWAYS
        );

        left.setPrefWidth(
                330
        );

        left.setPadding(
                new Insets(
                        16
                )
        );

        left.setStyle(
                "-fx-background-color: white;"
                        + "-fx-border-color: transparent #e5e7eb transparent transparent;"
        );

        root.setLeft(
                left
        );


        // =====================================================
        // CENTER CHAT AREA
        // =====================================================

        customerTitle.setStyle(
                "-fx-font-size: 20px;"
                        + "-fx-font-weight: bold;"
        );

        customerInfo.setStyle(
                "-fx-text-fill: #6b7280;"
        );

        VBox chatHeader =
                new VBox(
                        3,
                        customerTitle,
                        customerInfo
                );

        chatHeader.setPadding(
                new Insets(
                        15
                )
        );

        chatHeader.setStyle(
                "-fx-background-color: white;"
                        + "-fx-border-color: transparent transparent #e5e7eb transparent;"
        );


        messageBox.setPadding(
                new Insets(
                        18
                )
        );

        messageScroll.setFitToWidth(
                true
        );

        messageScroll.setStyle(
                "-fx-background-color: transparent;"
                        + "-fx-background: #f3f4f6;"
        );


        messageInput.setPromptText(
                "Nhập nội dung trả lời khách hàng..."
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


        VBox inputArea =
                new VBox(
                        6,
                        inputRow,
                        statusLabel
                );

        inputArea.setPadding(
                new Insets(
                        14
                )
        );

        inputArea.setStyle(
                "-fx-background-color: white;"
                        + "-fx-border-color: #e5e7eb transparent transparent transparent;"
        );


        BorderPane chatPane =
                new BorderPane();

        chatPane.setTop(
                chatHeader
        );

        chatPane.setCenter(
                messageScroll
        );

        chatPane.setBottom(
                inputArea
        );


        root.setCenter(
                chatPane
        );


        Scene scene =
                new Scene(
                        root,
                        1100,
                        700
                );

        stage.setTitle(
                "Fashion Shop Admin - Tin nhắn khách hàng"
        );

        stage.setScene(
                scene
        );

        stage.show();


        

        configureRealtime();
stage.setOnHidden(
                event -> stopRefresh()
        );


        refreshConversations();


        refreshTimeline =
                new Timeline(
                        new KeyFrame(
                                Duration.seconds(10),
                                event -> {

                                    refreshConversations();

                                    if (selectedConversationId
                                            > 0) {

                                        refreshMessages();
                                    }
                                }
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

                                    refreshConversations();

                                    if (selectedConversationId > 0) {

                                        lastRenderedMessageId =
                                                -1;

                                        refreshMessages();
                                    }
                                }
                        );
                    }
                }
        );


        realtimeClient.connect();
    }

    private void refreshConversations() {

        if (loadingConversations) {
            return;
        }

        loadingConversations =
                true;

        long previouslySelected =
                selectedConversationId;


        Task<List<ChatService.Conversation>> task =
                new Task<>() {

                    @Override
                    protected List<ChatService.Conversation> call()
                            throws Exception {

                        return chatService
                                .getAdminConversations();
                    }
                };


        task.setOnSucceeded(
                event -> {

                    loadingConversations =
                            false;

                    List<ChatService.Conversation> conversations =
                            task.getValue();

                    conversationList
                            .getItems()
                            .setAll(
                                    conversations
                            );


                    ChatService.Conversation toSelect =
                            null;

                    for (
                            ChatService.Conversation conversation
                            : conversations
                    ) {

                        if (conversation.getId()
                                == previouslySelected) {

                            toSelect =
                                    conversation;

                            break;
                        }
                    }


                    if (toSelect != null) {

                        conversationList
                                .getSelectionModel()
                                .select(
                                        toSelect
                                );

                    } else if (
                            previouslySelected <= 0
                                    && !conversations.isEmpty()
                    ) {

                        conversationList
                                .getSelectionModel()
                                .select(
                                        0
                                );
                    }


                    if (conversations.isEmpty()) {

                        statusLabel.setText(
                                "Chưa có cuộc trò chuyện nào."
                        );
                    }
                }
        );


        task.setOnFailed(
                event -> {

                    loadingConversations =
                            false;

                    Throwable error =
                            task.getException();

                    statusLabel.setText(
                            error == null
                                    || error.getMessage() == null
                                    ? "Không thể tải danh sách chat."
                                    : error.getMessage()
                    );

                    statusLabel.setStyle(
                            "-fx-text-fill: #dc2626;"
                    );
                }
        );


        Thread thread =
                new Thread(
                        task
                );

        thread.setDaemon(
                true
        );

        thread.start();
    }


    private void refreshMessages() {

        if (selectedConversationId <= 0
                || loadingMessages) {

            return;
        }

        loadingMessages =
                true;

        long conversationId =
                selectedConversationId;


        Task<List<ChatService.ChatMessage>> task =
                new Task<>() {

                    @Override
                    protected List<ChatService.ChatMessage> call()
                            throws Exception {

                        return chatService
                                .getAdminMessages(
                                        conversationId
                                );
                    }
                };


        task.setOnSucceeded(
                event -> {

                    loadingMessages =
                            false;

                    if (conversationId
                            != selectedConversationId) {

                        return;
                    }

                    displayMessages(
                            task.getValue()
                    );

                    statusLabel.setText(
                            "Đã kết nối."
                    );

                    statusLabel.setStyle(
                            "-fx-text-fill: #16a34a;"
                    );
                }
        );


        task.setOnFailed(
                event -> {

                    loadingMessages =
                            false;

                    Throwable error =
                            task.getException();

                    statusLabel.setText(
                            error == null
                                    || error.getMessage() == null
                                    ? "Không tải được tin nhắn."
                                    : error.getMessage()
                    );

                    statusLabel.setStyle(
                            "-fx-text-fill: #dc2626;"
                    );
                }
        );


        Thread thread =
                new Thread(
                        task
                );

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

        if (newestId
                == lastRenderedMessageId) {

            return;
        }

        lastRenderedMessageId =
                newestId;


        messageBox
                .getChildren()
                .clear();


        if (messages.isEmpty()) {

            Label empty =
                    new Label(
                            "Chưa có tin nhắn trong cuộc trò chuyện này."
                    );

            empty.setStyle(
                    "-fx-text-fill: #6b7280;"
            );

            messageBox
                    .getChildren()
                    .add(
                            empty
                    );

            return;
        }


        User admin =
                sessionManager.getCurrentUser();

        long adminId =
                admin == null
                        ? -1
                        : admin.getId();


        for (
                ChatService.ChatMessage message
                : messages
        ) {

            boolean mine =
                    message.getSenderId()
                            == adminId;

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
                                ? "Admin"
                                : (
                                message.getSenderName()
                                        == null
                                        ? "Khách hàng"
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
                500
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
                530
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

        if (selectedConversationId <= 0) {

            statusLabel.setText(
                    "Vui lòng chọn khách hàng."
            );

            return;
        }


        String text =
                messageInput.getText();

        if (text == null
                || text.trim().isEmpty()) {

            return;
        }


        String message =
                text.trim();

        long conversationId =
                selectedConversationId;


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
                                .sendAdminMessage(
                                        conversationId,
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

                    refreshConversations();
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
                new Thread(
                        task
                );

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
                            "dd/MM HH:mm"
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