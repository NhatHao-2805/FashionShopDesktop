package com.fashionshop.customer.view;

import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.function.BooleanSupplier;

public final class PageNavigation {
    private static final String STYLESHEET =
            PageNavigation.class.getResource("/com/fashionshop/customer/shop.css").toExternalForm();
    private static final Deque<Page> history = new ArrayDeque<>();
    private static Stage stage;

    private PageNavigation() { }

    public static void initialize(Stage mainStage) {
        stage = mainStage;
        stage.addEventHandler(WindowEvent.WINDOW_HIDDEN, event -> clearHistory());
    }

    public static Stage window() {
        return stage;
    }

    public static void reset(String title, Scene scene) {
        clearHistory();
        show(title, scene);
    }

    public static void show(String title, Scene scene) {
        show(title, scene, () -> { });
    }

    public static void show(String title, Scene scene, Runnable onClose) {
        if (!scene.getStylesheets().contains(STYLESHEET)) {
            scene.getStylesheets().add(STYLESHEET);
        }
        Page page = new Page(title, scene, onClose);
        if (!history.isEmpty()) {
            Button back = new Button("← Quay lại");
            back.getStyleClass().addAll("soft-button", "page-back");
            back.setOnAction(event -> back(scene));
            Label heading = new Label(title);
            heading.getStyleClass().add("page-heading");
            HBox bar = new HBox(16, back, heading);
            bar.setAlignment(Pos.CENTER_LEFT);
            bar.getStyleClass().add("page-navigation");
            Parent content = scene.getRoot();
            BorderPane frame = new BorderPane();
            scene.setRoot(frame);
            frame.setTop(bar);
            frame.setCenter(content);
            scene.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
                if (event.isAltDown() && event.getCode() == KeyCode.LEFT) {
                    back(scene);
                    event.consume();
                }
            });
        }
        history.push(page);
        display(page);
    }

    public static void guardBack(Scene scene, BooleanSupplier canGoBack) {
        if (isCurrent(scene)) {
            history.peek().canGoBack = canGoBack;
        }
    }

    public static boolean isCurrent(Scene scene) {
        return scene != null && !history.isEmpty() && stage.getScene() == scene;
    }

    public static void back(Scene scene) {
        if (history.size() > 1 && isCurrent(scene) && history.peek().canGoBack.getAsBoolean()) {
            history.pop().onClose.run();
            display(history.peek());
        }
    }

    private static void clearHistory() {
        while (!history.isEmpty()) {
            history.pop().onClose.run();
        }
    }

    private static void display(Page page) {
        stage.setTitle(page.title);
        stage.setScene(page.scene);
        if (!stage.isShowing()) {
            stage.show();
        }
    }

    private static final class Page {
        private final String title;
        private final Scene scene;
        private final Runnable onClose;
        private BooleanSupplier canGoBack = () -> true;

        private Page(String title, Scene scene, Runnable onClose) {
            this.title = title;
            this.scene = scene;
            this.onClose = onClose;
        }
    }
}
