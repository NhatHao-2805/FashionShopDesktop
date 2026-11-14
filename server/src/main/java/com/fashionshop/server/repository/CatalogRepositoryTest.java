package com.fashionshop.server.repository;

import com.fashionshop.server.model.Category;
import com.fashionshop.server.model.Product;

import java.util.List;

public class CatalogRepositoryTest {

    public static void main(String[] args) {

        System.out.println();
        System.out.println("==============================================");
        System.out.println("        CATALOG REPOSITORY TEST");
        System.out.println("==============================================");

        CategoryRepository categoryRepository =
                new CategoryRepository();

        ProductRepository productRepository =
                new ProductRepository();

        // ==========================================
        // TEST CATEGORIES
        // ==========================================

        System.out.println();
        System.out.println("[1] Testing categories...");

        List<Category> categories =
                categoryRepository.findAllActive();

        System.out.println(
                "Active categories: " + categories.size()
        );

        for (Category category : categories) {

            System.out.println(
                    "  ID=" + category.getId()
                            + " | Name=" + category.getName()
                            + " | Status=" + category.getStatus()
            );
        }

        // ==========================================
        // TEST PRODUCTS
        // ==========================================

        System.out.println();
        System.out.println("[2] Testing products...");

        List<Product> products =
                productRepository.findAllActive();

        System.out.println(
                "Active products: " + products.size()
        );

        for (Product product : products) {

            System.out.println(
                    "  ID=" + product.getId()
                            + " | Name=" + product.getName()
                            + " | Price=" + product.getPrice()
                            + " | Category=" + product.getCategoryName()
                            + " | Stock=" + product.getStock()
            );
        }

        // ==========================================
        // TEST PRODUCT BY ID
        // ==========================================

        System.out.println();
        System.out.println("[3] Testing find product by ID...");

        if (!products.isEmpty()) {

            long productId =
                    products.get(0).getId();

            Product product =
                    productRepository.findById(productId);

            if (product == null) {

                throw new IllegalStateException(
                        "Không tìm thấy product ID="
                                + productId
                );
            }

            System.out.println(
                    "Found product:"
            );

            System.out.println(
                    "  ID          : " + product.getId()
            );

            System.out.println(
                    "  Name        : " + product.getName()
            );

            System.out.println(
                    "  Description : " + product.getDescription()
            );

            System.out.println(
                    "  Price       : " + product.getPrice()
            );

            System.out.println(
                    "  Category    : " + product.getCategoryName()
            );

            System.out.println(
                    "  Size        : " + product.getSize()
            );

            System.out.println(
                    "  Color       : " + product.getColor()
            );

            System.out.println(
                    "  Stock       : " + product.getStock()
            );

            System.out.println(
                    "  Image       : " + product.getImageUrl()
            );
        }

        // ==========================================
        // TEST SEARCH
        // ==========================================

        System.out.println();
        System.out.println("[4] Testing product search...");

        List<Product> searchResults =
                productRepository.search(
                        "áo",
                        null
                );

        System.out.println(
                "Search result count: "
                        + searchResults.size()
        );

        for (Product product : searchResults) {

            System.out.println(
                    "  ID=" + product.getId()
                            + " | Name=" + product.getName()
            );
        }

        // ==========================================
        // TEST CATEGORY FILTER
        // ==========================================

        System.out.println();
        System.out.println("[5] Testing category filter...");

        if (!categories.isEmpty()) {

            long categoryId =
                    categories.get(0).getId();

            List<Product> categoryProducts =
                    productRepository.search(
                            null,
                            categoryId
                    );

            System.out.println(
                    "Category ID: " + categoryId
            );

            System.out.println(
                    "Product count: "
                            + categoryProducts.size()
            );

            for (Product product : categoryProducts) {

                System.out.println(
                        "  ID=" + product.getId()
                                + " | Name=" + product.getName()
                );
            }
        }

        System.out.println();
        System.out.println("==============================================");
        System.out.println("        CATALOG REPOSITORY TEST PASSED");
        System.out.println("==============================================");
    }
}