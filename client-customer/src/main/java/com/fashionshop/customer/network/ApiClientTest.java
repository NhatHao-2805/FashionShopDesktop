package com.fashionshop.customer.network;

public class ApiClientTest {

    public static void main(String[] args) {

        ApiClient apiClient = new ApiClient();

        try {

            String response =
                    apiClient.get("/api/products");

            System.out.println("===== SERVER RESPONSE =====");
            System.out.println(response);
            System.out.println("===========================");

        } catch (Exception e) {

            System.out.println("KHONG THE KET NOI SERVER");

            e.printStackTrace();
        }
    }
}