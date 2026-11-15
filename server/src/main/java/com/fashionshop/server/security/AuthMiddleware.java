package com.fashionshop.server.security;

import io.jsonwebtoken.Claims;
import spark.Request;
import spark.Response;

import static spark.Spark.before;

public class AuthMiddleware {

    private final JwtService jwtService;
    private final CurrentAccountAuthentication currentAccount = new CurrentAccountAuthentication();

    public AuthMiddleware() {
        this.jwtService = new JwtService();
    }

    public void requireAuthentication() {

        before(
                "/api/protected/*",
                (request, response) ->
                        authenticate(
                                request,
                                response
                        )
        );

        before(
                "/api/admin/*",
                (request, response) ->
                        authenticate(
                                request,
                                response
                        )
        );
    }

    public void requireAdmin() {

        before(
                "/api/admin/*",
                (request, response) -> {

                    JwtAuthentication authentication =
                            request.attribute(
                                    "authentication"
                            );

                    if (authentication == null) {

                        halt(
                                401,
                                """
                                {
                                  "success": false,
                                  "message": "Unauthorized",
                                  "data": null
                                }
                                """
                        );
                    }

                    if (!authentication.isAdmin()) {

                        halt(
                                403,
                                """
                                {
                                  "success": false,
                                  "message": "Forbidden: ADMIN role required",
                                  "data": null
                                }
                                """
                        );
                    }
                }
        );
    }

    private void authenticate(
            Request request,
            Response response
    ) {

        String authorization =
                request.headers(
                        "Authorization"
                );

        if (authorization == null
                || authorization.isBlank()) {

            halt(
                    401,
                    """
                    {
                      "success": false,
                      "message": "Unauthorized: missing Authorization header",
                      "data": null
                    }
                    """
            );
        }

        if (!authorization.startsWith(
                "Bearer "
        )) {

            halt(
                    401,
                    """
                    {
                      "success": false,
                      "message": "Unauthorized: invalid Authorization header",
                      "data": null
                    }
                    """
            );
        }

        String token =
                authorization
                        .substring(7)
                        .trim();

        long userId;
        try {
            Claims claims = jwtService.parseToken(token);
            userId = Long.parseLong(claims.getSubject());
            if (userId <= 0) throw new IllegalArgumentException("Invalid subject");
        } catch (Exception e) {
            halt(401, """
                    {"success":false,"message":"Token không hợp lệ hoặc đã hết hạn.","data":null}
                    """);
            return;
        }

        JwtAuthentication authentication;
        try {
            // Do not reuse role/email embedded in an older JWT.
            authentication = currentAccount.resolve(userId);
        } catch (SecurityException e) {
            halt(401, """
                    {"success":false,"message":"Tài khoản không còn quyền truy cập. Vui lòng đăng nhập lại.","data":null}
                    """);
            return;
        } catch (IllegalStateException e) {
            // A database outage must never fall back to stale token permissions.
            halt(503, """
                    {"success":false,"message":"Chưa thể kiểm tra tài khoản. Vui lòng thử lại sau.","data":null}
                    """);
            return;
        }
        request.attribute("authentication", authentication);
    }

    private static void halt(
            int status,
            String body
    ) {

        spark.Spark.halt(
                status,
                body
        );
    }
}