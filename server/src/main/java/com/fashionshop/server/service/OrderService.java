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

    public OrderService() {

        this.orderRepository =
                new OrderRepository();
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

        return orderRepository.create(
                order
        );
    }

    public List<Order> getOrdersByUser(
            long userId
    ) {

        validateUserId(userId);

        return orderRepository.findByUserId(
                userId
        );
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

        Map<Long, Integer> quantities =
                new LinkedHashMap<>();

        for (OrderItem item : items) {

            if (item == null) {

                throw new IllegalArgumentException(
                        "Sản phẩm trong đơn hàng không hợp lệ."
                );
            }

            if (item.getProductId() <= 0) {

                throw new IllegalArgumentException(
                        "Product ID không hợp lệ."
                );
            }

            if (item.getQuantity() <= 0) {

                throw new IllegalArgumentException(
                        "Số lượng sản phẩm phải lớn hơn 0."
                );
            }

            quantities.merge(
                    item.getProductId(),
                    item.getQuantity(),
                    Integer::sum
            );
        }

        List<OrderItem> result =
                new ArrayList<>();

        for (
                Map.Entry<Long, Integer> entry :
                quantities.entrySet()
        ) {

            OrderItem item =
                    new OrderItem();

            item.setProductId(
                    entry.getKey()
            );

            item.setQuantity(
                    entry.getValue()
            );

            result.add(item);
        }

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