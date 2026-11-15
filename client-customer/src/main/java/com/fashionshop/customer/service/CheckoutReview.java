package com.fashionshop.customer.service;

import com.fashionshop.customer.model.*;
import java.util.*;
import java.math.BigDecimal;

public final class CheckoutReview {
    public record Result(List<CartItem> items, List<String> changes) {
        public double total() { return items.stream().mapToDouble(CartItem::getSubtotal).sum(); }
    }
    public static Result review(List<CartItem> cart, List<Product> catalog) {
        if (cart.isEmpty()) throw new IllegalArgumentException("Giỏ hàng đang trống.");
        Map<Long, Product> products = new HashMap<>();
        for (Product product : catalog) products.put(product.getId(), product);
        Map<Long, Long> quantities = new HashMap<>();
        List<CartItem> refreshed = new ArrayList<>();
        Set<String> changes = new LinkedHashSet<>();
        for (CartItem item : cart) {
            Product old = item.getProduct(), current = products.get(old.getId());
            if (current == null || !"ACTIVE".equalsIgnoreCase(current.getStatus()))
                throw new IllegalArgumentException(old.getName() + " không còn được bán. Vui lòng sửa giỏ hàng.");
            long requested = quantities.merge(old.getId(), (long)item.getQuantity(), Long::sum);
            if (item.getQuantity() <= 0 || requested > current.getStock())
                throw new IllegalArgumentException(current.getName() + " chỉ còn " + current.getStock() + " sản phẩm. Vui lòng sửa giỏ hàng.");
            validateChoice(current.getSize(), item.getSelectedSize());
            validateChoice(current.getColor(), item.getSelectedColor());
            if (BigDecimal.valueOf(old.getPrice()).compareTo(BigDecimal.valueOf(current.getPrice())) != 0)
                changes.add(current.getName() + ": giá " + old.getPrice() + " → " + current.getPrice() + " đ");
            if (old.getStock() != current.getStock())
                changes.add(current.getName() + ": tồn kho " + old.getStock() + " → " + current.getStock());
            refreshed.add(new CartItem(current, item.getQuantity(), item.getSelectedSize(), item.getSelectedColor()));
        }
        return new Result(List.copyOf(refreshed), List.copyOf(changes));
    }
    private static void validateChoice(String options, String selected) {
        List<String> allowed = options == null ? List.of() : Arrays.stream(options.split(","))
                .map(String::trim).filter(value -> !value.isEmpty()).toList();
        if (allowed.isEmpty() ? selected != null && !selected.isBlank() : !allowed.contains(selected))
            throw new IllegalArgumentException("Size/màu đã thay đổi. Vui lòng chọn lại sản phẩm.");
    }
}
