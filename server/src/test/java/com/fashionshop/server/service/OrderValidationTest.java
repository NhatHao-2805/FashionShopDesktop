package com.fashionshop.server.service;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import com.fashionshop.server.model.*;
import com.fashionshop.server.repository.OrderRepository;
import java.math.BigDecimal;
import java.util.*;
class OrderValidationTest {
    private final OrderService service = new OrderService(new OrderRepository() {
        @Override public Order create(Order order) { return order; }
    });
    private OrderItem item(String size, int quantity) {
        OrderItem item = new OrderItem(); item.setProductId(1); item.setQuantity(quantity);
        item.setSelectedSize(size); item.setSelectedColor("Đen"); item.setExpectedPrice(new BigDecimal("100.00")); return item;
    }
    private Order order(List<OrderItem> items) {
        return service.createOrder(1,"Nguyễn Văn A","+84 912 345 678","123 Nguyễn Văn Linh, Đà Nẵng",null,"COD",items);
    }
    @Test void normalizesPhoneAndMergesOnlySameVariants() {
        Order result=order(List.of(item("M",1), item(" M ",2), item("L",1)));
        assertEquals("0912345678",result.getPhone());
        assertEquals(2,result.getItems().size()); assertEquals(3,result.getItems().get(0).getQuantity());
    }
    @Test void rejectsMissingPriceAndOverflow() {
        OrderItem missing=item("M",1); missing.setExpectedPrice(null);
        assertThrows(IllegalArgumentException.class,()->order(List.of(missing)));
        assertThrows(IllegalArgumentException.class,()->order(List.of(item("M",Integer.MAX_VALUE),item("M",1))));
    }
    @Test void validatesShippingBoundaries() {
        ShippingValidation.validate("An","0912345678","1234567890",null);
        ShippingValidation.validate("A".repeat(100),"0912345678","A".repeat(500),"A".repeat(1000));
        assertThrows(IllegalArgumentException.class,()->ShippingValidation.validate("A","0912345678","1234567890",null));
        assertThrows(IllegalArgumentException.class,()->ShippingValidation.validate("An","1234567890","1234567890",null));
        assertThrows(IllegalArgumentException.class,()->ShippingValidation.validate("An","0912345678","ngắn",null));
        assertThrows(IllegalArgumentException.class,()->ShippingValidation.validate("A".repeat(101),"0912345678","1234567890",null));
        assertThrows(IllegalArgumentException.class,()->ShippingValidation.validate("An","0912345678","A".repeat(501),null));
        assertThrows(IllegalArgumentException.class,()->ShippingValidation.validate("An","0912345678","1234567890","A".repeat(1001)));
    }
}
