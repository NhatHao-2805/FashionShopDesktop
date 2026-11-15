package com.fashionshop.server.security;

import com.fashionshop.server.model.User;

/** Standalone regression checks; throws AssertionError on failure. */
public class CurrentAccountAuthenticationTest {
    @org.junit.jupiter.api.Test
    void accountChangesTakeEffect() { main(new String[0]); }

    public static void main(String[] args) {
        User user = new User();
        user.setId(7);
        user.setEmail("demo@example.test");
        user.setRole("ADMIN");
        user.setStatus("ACTIVE");
        int[] reads = {0};
        var auth = new CurrentAccountAuthentication(id -> { reads[0]++; return user; });
        check(auth.resolve(7).isAdmin(), "Active admin rejected");
        user.setRole("CUSTOMER");
        check(!auth.resolve(7).isAdmin() && auth.resolve(7).isCustomer(), "Old admin permission retained");
        user.setEmail("updated@example.test");
        check(auth.resolve(7).getEmail().equals("updated@example.test"), "Stale email retained");
        user.setStatus("LOCKED");
        denied(() -> auth.resolve(7));
        user.setStatus(null);
        denied(() -> auth.resolve(7));
        user.setStatus("ACTIVE");
        user.setRole("UNKNOWN");
        denied(() -> auth.resolve(7));
        denied(() -> new CurrentAccountAuthentication(id -> null).resolve(7));
        denied(() -> auth.resolve(0));
        check(reads[0] == 7, "Account not reloaded per request");
        try {
            new CurrentAccountAuthentication(id -> { throw new IllegalStateException("DB offline"); }).resolve(7);
            throw new AssertionError("Database failure permitted access");
        } catch (IllegalStateException expected) { }
        System.out.println("PASS: current role/email, blocked/deleted accounts, unknown roles, invalid IDs and DB failure.");
    }
    private static void denied(Runnable action) {
        try { action.run(); throw new AssertionError("Access should be denied"); }
        catch (SecurityException expected) { }
    }
    private static void check(boolean valid, String message) {
        if (!valid) throw new AssertionError(message);
    }
}
