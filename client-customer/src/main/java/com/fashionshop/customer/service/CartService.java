package com.fashionshop.customer.service;

import com.fashionshop.customer.model.CartItem;
import com.fashionshop.customer.model.Product;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class CartService {

    private static final CartService INSTANCE =
            new CartService();

    private final ObservableList<CartItem> items =
            FXCollections.observableArrayList();

    private CartService() {
    }

    public static CartService getInstance() {
        return INSTANCE;
    }

    public ObservableList<CartItem> getItems() {
        return items;
    }

    public void addProduct(
            Product product,
            int quantity
    ) {

        for (CartItem item : items) {

            if (item.getProduct().getId()
                    == product.getId()) {

                item.setQuantity(
                        item.getQuantity() + quantity
                );

                refreshItems();
                return;
            }
        }

        items.add(
                new CartItem(product, quantity)
        );

        refreshItems();
    }

    public void removeItem(
            CartItem item
    ) {

        items.remove(item);
        refreshItems();
    }

    public void clear() {
        items.clear();
        refreshItems();
    }

    public int getTotalQuantity() {

        return items.stream()
                .mapToInt(CartItem::getQuantity)
                .sum();
    }

    public double getTotalPrice() {

        return items.stream()
                .mapToDouble(CartItem::getSubtotal)
                .sum();
    }

    private void refreshItems() {

        for (int i = 0; i < items.size(); i++) {
            items.set(i, items.get(i));
        }
    }
}