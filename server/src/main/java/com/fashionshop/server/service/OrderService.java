package com.fashionshop.server.service;

import com.fashionshop.server.model.Order;
import com.fashionshop.server.model.OrderItem;
import com.fashionshop.server.repository.OrderRepository;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class OrderService {

    private final OrderRepository orderRepository;

    public OrderService() { this(new OrderRepository()); }
    public OrderService(OrderRepository repository) {
        orderRepository = java.util.Objects.requireNonNull(repository);
    }

    public Order createOrder(
            long userId,
            String receiverName,
            String phone,
            String address,
            String note,
            String paymentMethod,
            List<OrderItem> items
    ) {
        return createOrder(userId, receiverName, phone, address, note, paymentMethod, items, null, null);
    }

    public Order createOrder(long userId, String receiverName, String phone, String address,
                             String note, String paymentMethod, List<OrderItem> items,
                             String requestId, String fingerprint) {
        ShippingValidation.validate(receiverName, phone, address, note);
        phone = ShippingValidation.normalizePhone(phone);

        if (userId <= 0) {

            throw new IllegalArgumentException(
                    "User không hợp lệ."
            );
        }

        if (receiverName == null
                || receiverName.isBlank()) {

            throw new IllegalArgumentException(
                    "Tên người nhận không được để trống."
            );
        }

        if (phone == null
                || phone.isBlank()) {

            throw new IllegalArgumentException(
                    "Số điện thoại không được để trống."
            );
        }

        if (address == null
                || address.isBlank()) {

            throw new IllegalArgumentException(
                    "Địa chỉ không được để trống."
            );
        }

        if (items == null
                || items.isEmpty()) {

            throw new IllegalArgumentException(
                    "Đơn hàng phải có ít nhất một sản phẩm."
            );
        }

        String finalPaymentMethod =
                paymentMethod == null
                        || paymentMethod.isBlank()
                        ? "COD"
                        : paymentMethod.trim()
                        .toUpperCase();

        if (!"COD".equals(
                finalPaymentMethod
        )) {

            throw new IllegalArgumentException(
                    "Hiện tại hệ thống chỉ hỗ trợ COD."
            );
        }

        List<OrderItem> normalizedItems =
                mergeDuplicateItems(items);

        Order order =
                new Order();

        order.setUserId(userId);

        order.setReceiverName(
                receiverName.trim()
        );

        order.setPhone(
                phone.trim()
        );

        order.setAddress(
                address.trim()
        );

        order.setNote(
                note == null
                        ? null
                        : note.trim()
        );

        order.setPaymentMethod(
                finalPaymentMethod
        );

        order.setItems(
                normalizedItems
        );

        return requestId == null ? orderRepository.create(order) : orderRepository.create(order, requestId, fingerprint);
    }

    public List<Order> getOrdersByUser(
            long userId
    ) {

        validateUserId(userId);

        return orderRepository.findByUserId(
                userId
        );
    }

    public Order getOrderByRequestId(long userId, String requestId) {
        validateUserId(userId);
        return orderRepository.findByRequestId(userId, requestId);
    }

    public Order getOrderDetail(
            long userId,
            long orderId
    ) {

        validateUserId(userId);

        if (orderId <= 0) {

            throw new IllegalArgumentException(
                    "ID đơn hàng không hợp lệ."
            );
        }

        Order order =
                orderRepository.findByIdAndUserId(
                        orderId,
                        userId
                );

        if (order == null) {

            throw new IllegalArgumentException(
                    "Không tìm thấy đơn hàng."
            );
        }

        return order;
    }

    public Order cancelOrder(
            long userId,
            long orderId
    ) {

        validateUserId(userId);

        if (orderId <= 0) {

            throw new IllegalArgumentException(
                    "ID đơn hàng không hợp lệ."
            );
        }

        return orderRepository.cancel(
                orderId,
                userId
        );
    }

    private List<OrderItem> mergeDuplicateItems(
            List<OrderItem> items
    ) {

        record Key(long productId, String size, String color) { }
        Map<Key, OrderItem> merged = new LinkedHashMap<>();
        for (OrderItem item : items) {
            if (item == null || item.getProductId() <= 0 || item.getQuantity() <= 0)
                throw new IllegalArgumentException("Sản phẩm hoặc số lượng không hợp lệ.");
            item.setSelectedSize(item.getSelectedSize() == null ? null : item.getSelectedSize().trim());
            item.setSelectedColor(item.getSelectedColor() == null ? null : item.getSelectedColor().trim());
            if (item.getExpectedPrice() == null || item.getExpectedPrice().signum() < 0
                    || item.getExpectedPrice().scale() > 2)
                throw new IllegalArgumentException("Vui lòng tải lại giỏ hàng để xác nhận giá hiện tại.");
            Key key = new Key(item.getProductId(), item.getSelectedSize(), item.getSelectedColor());
            OrderItem previous = merged.get(key);
            if (previous == null) merged.put(key, item);
            else {
                if (previous.getExpectedPrice().compareTo(item.getExpectedPrice()) != 0)
                    throw new IllegalArgumentException("Giá sản phẩm không nhất quán. Vui lòng tải lại giỏ hàng.");
                try { previous.setQuantity(Math.addExact(previous.getQuantity(), item.getQuantity())); }
                catch (ArithmeticException e) { throw new IllegalArgumentException("Số lượng quá lớn."); }
            }
        }
        List<OrderItem> result = new ArrayList<>(merged.values());
        return result;
    }

    private void validateUserId(
            long userId
    ) {

        if (userId <= 0) {

            throw new IllegalArgumentException(
                    "User không hợp lệ."
            );
        }
    }
}
