package com.fashionshop.customer.service;

import com.fashionshop.customer.model.*;
import java.util.*;
import java.math.BigDecimal;

public final class CheckoutReview {
    private record StockKey(long productId, String size, String color) {}

    public record Result(List<CartItem> items, List<String> changes) {
        public double total() { return items.stream().mapToDouble(CartItem::getSubtotal).sum(); }
    }
    public static Result review(List<CartItem> cart, List<Product> catalog) {
        if (cart.isEmpty()) throw new IllegalArgumentException("Giỏ hàng đang trống.");
        Map<Long, Product> products = new HashMap<>();
        for (Product product : catalog) products.put(product.getId(), product);
        Map<StockKey, Long> quantities = new HashMap<>();
        List<CartItem> refreshed = new ArrayList<>();
        Set<String> changes = new LinkedHashSet<>();
        for (CartItem item : cart) {
            Product old = item.getProduct(), current = products.get(old.getId());
            if (current == null || !"ACTIVE".equalsIgnoreCase(current.getStatus()))
                throw new IllegalArgumentException(old.getName() + " không còn được bán. Vui lòng sửa giỏ hàng.");
            String size = item.getSelectedSize(), color = item.getSelectedColor();
            validateChoice(current.getSize(), size);
            validateChoice(current.getColor(), color);
            var stockKey = current.tracksVariants() ? new StockKey(old.getId(), size, color)
                    : new StockKey(old.getId(), null, null);
            long requested = quantities.merge(stockKey, (long)item.getQuantity(), Long::sum);
            int available = current.availableStock(size, color);
            int previous = old.availableStock(size, color);
            String label = current.tracksVariants()
                    ? current.getName() + " (" + String.join(" / ", size == null ? "Không có size" : size,
                            color == null ? "Không có màu" : color) + ")"
                    : current.getName();
            if (item.getQuantity() <= 0 || requested > available)
                throw new IllegalArgumentException(label + " chỉ còn " + available + " sản phẩm. Vui lòng sửa giỏ hàng.");
            if (BigDecimal.valueOf(old.getPrice()).compareTo(BigDecimal.valueOf(current.getPrice())) != 0)
                changes.add(current.getName() + ": giá " + old.getPrice() + " → " + current.getPrice() + " đ");
            if (previous != available)
                changes.add(label + ": tồn kho " + previous + " → " + available);
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
