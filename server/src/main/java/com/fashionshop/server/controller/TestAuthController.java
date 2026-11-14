package com.fashionshop.server.controller;

import com.fashionshop.server.security.JwtAuthentication;

import static spark.Spark.get;

public class TestAuthController {

    public void registerRoutes() {

        get("/api/protected/me", (request, response) -> {

            response.type("application/json; charset=UTF-8");

            JwtAuthentication authentication =
                    request.attribute("authentication");

            if (authentication == null) {
                response.status(401);

                return """
                        {
                          "success": false,
                          "message": "Unauthorized",
                          "data": null
                        }
                        """;
            }

            response.status(200);

            return """
                    {
                      "success": true,
                      "message": "JWT authentication successful",
                      "data": {
                        "user_id": %d,
                        "email": "%s",
                        "role": "%s",
                        "is_admin": %s,
                        "is_customer": %s
                      }
                    }
                    """.formatted(
                    authentication.getUserId(),
                    authentication.getEmail(),
                    authentication.getRole(),
                    authentication.isAdmin(),
                    authentication.isCustomer()
            );
        });
    }
}