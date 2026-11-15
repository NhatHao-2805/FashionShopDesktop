package com.fashionshop.customer.view;

import com.fashionshop.customer.service.SessionManager;
import javafx.scene.control.Alert;
import java.util.function.BooleanSupplier;

/** Shared entry check for every customer purchasing window. */
public final class PurchaseAccess {
    private static BooleanSupplier loginHandler;

    private PurchaseAccess() { }

    public static void setLoginHandler(BooleanSupplier handler) {
        loginHandler = handler;
    }

    public static boolean requireCustomer() {
        SessionManager session = SessionManager.getInstance();
        if (session.isCustomer()) return true;
        if (!session.isLoggedIn()) {
            if (loginHandler != null) loginHandler.getAsBoolean();
            else new LoginWindow().showAndWait();
            // Canceling login must never continue a pending purchase.
            if (!session.isLoggedIn()) return false;
        }
        if (session.isCustomer()) return true;
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setHeaderText(null);
        alert.setContentText("Vui lòng dùng tài khoản khách hàng để mua sắm. Tài khoản quản trị chỉ dùng để quản lý cửa hàng.");
        alert.showAndWait();
        return false;
    }
}
