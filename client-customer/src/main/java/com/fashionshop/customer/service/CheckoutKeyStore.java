package com.fashionshop.customer.service;

import com.fashionshop.customer.config.ServerConfig;

import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

/** Stores a request identifier only. Shipping fields and cart contents stay in memory. */
public final class CheckoutKeyStore {
    private CheckoutKeyStore() { }

    public static String pendingKey(long customerId) {
        return preferences().get(accountKey(customerId), null);
    }

    public static String remember(long customerId) throws BackingStoreException {
        String pending = pendingKey(customerId);
        if (pending == null) {
            pending = UUID.randomUUID().toString();
            preferences().put(accountKey(customerId), pending);
        }
        preferences().flush();
        return pending;
    }

    public static void clear(long customerId, String requestId) throws BackingStoreException {
        if (requestId.equals(pendingKey(customerId))) {
            preferences().remove(accountKey(customerId));
            preferences().flush();
        }
    }

    private static Preferences preferences() {
        return Preferences.userNodeForPackage(CheckoutKeyStore.class).node("checkout");
    }

    private static String accountKey(long customerId) {
        String scope = ServerConfig.getBaseUrl() + "#" + customerId;
        return UUID.nameUUIDFromBytes(scope.getBytes(StandardCharsets.UTF_8)).toString();
    }
}
