package com.fashionshop.customer.view;

import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;

/** Shared feedback for background reads, forms and actions. */
public final class OperationStatus extends HBox {
    private final Label message = new Label();
    private final ProgressIndicator progress = new ProgressIndicator();
    private final Button retry = new Button("Thử lại");
    private final ReadOnlyBooleanWrapper busy = new ReadOnlyBooleanWrapper();

    public OperationStatus() {
        super(10);
        setAlignment(Pos.CENTER_LEFT);
        getStyleClass().add("operation-status");
        getStylesheets().add(getClass().getResource("/com/fashionshop/customer/shop.css").toExternalForm());
        message.setWrapText(true);
        message.setMaxWidth(Double.MAX_VALUE);
        setHgrow(message, Priority.ALWAYS);
        progress.setMinSize(18, 18);
        progress.setMaxSize(18, 18);
        progress.visibleProperty().bind(busy);
        progress.managedProperty().bind(busy);
        retry.setMinWidth(Region.USE_PREF_SIZE);
        retry.managedProperty().bind(retry.visibleProperty());
        visibleProperty().bind(message.textProperty().isNotEmpty());
        managedProperty().bind(visibleProperty());
        getChildren().addAll(progress, message, retry);
        info("");
    }

    public ReadOnlyBooleanProperty busyProperty() {
        return busy.getReadOnlyProperty();
    }

    public boolean isBusy() {
        return busy.get();
    }

    public void loading(String text) {
        display("loading", text, null);
    }

    public void info(String text) {
        display("info", text, null);
    }

    public void empty(String text) {
        display("empty", text, null);
    }

    public void success(String text) {
        display("success", text, null);
    }

    public void error(String text, Runnable tryAgain) {
        display("error", text, tryAgain);
    }

    private void display(String state, String text, Runnable tryAgain) {
        getStyleClass().setAll("operation-status", "operation-" + state);
        message.setText(text == null || text.isBlank() ? "" : text);
        busy.set("loading".equals(state));
        retry.setVisible(tryAgain != null);
        retry.setOnAction(tryAgain == null ? null : event -> tryAgain.run());
    }
}
