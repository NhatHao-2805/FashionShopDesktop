package com.fashionshop.customer.view;

import com.fashionshop.customer.service.SessionManager;
import javafx.scene.control.Alert;

import java.util.function.Consumer;

/** Runs customer actions after any required sign-in has completed. */
public final class PurchaseAccess {
    private static Consumer<Runnable> loginHandler;

    private PurchaseAccess() { }

    public static void setLoginHandler(Consumer<Runnable> handler) {
        loginHandler = handler;
    }

    public static void withCustomer(Runnable action) {
        SessionManager session = SessionManager.getInstance();
        if (session.isCustomer()) {
            action.run();
        } else if (!session.isLoggedIn() && loginHandler != null) {
            loginHandler.accept(action);
        } else {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.initOwner(PageNavigation.window());
            alert.setHeaderText(null);
            alert.setContentText("Vui lòng dùng tài khoản khách hàng để mua sắm.");
            alert.showAndWait();
        }
    }
}
