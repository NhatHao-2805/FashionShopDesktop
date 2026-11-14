package com.fashionshop.server.security;

import io.jsonwebtoken.Claims;
import spark.Request;
import spark.Response;

import static spark.Spark.before;

public class AuthMiddleware {

    private final JwtService jwtService;

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

        try {

            Claims claims =
                    jwtService.parseToken(
                            token
                    );

            JwtAuthentication authentication =
                    JwtAuthentication.fromClaims(
                            claims
                    );

            request.attribute(
                    "authentication",
                    authentication
            );

        } catch (Exception e) {

            halt(
                    401,
                    """
                    {
                      "success": false,
                      "message": "Unauthorized: invalid or expired token",
                      "data": null
                    }
                    """
            );
        }
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