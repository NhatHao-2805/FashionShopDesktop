package com.fashionshop.customer.service;
import com.fashionshop.customer.model.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.*;
class CheckoutReviewTest {
    private Product product(double price, int stock) {
        Product p=new Product(); p.setId(1); p.setName("Áo"); p.setPrice(price); p.setStock(stock);
        p.setSize("M,L"); p.setColor("Đen"); p.setStatus("ACTIVE"); return p;
    }
    @Test void returnsNewPriceAndStockWithoutMutatingOriginalCart() {
        Product old=product(100,10), latest=product(120,8);
        CartItem cart=new CartItem(old,2,"M","Đen");
        var review=CheckoutReview.review(List.of(cart),List.of(latest));
        assertEquals(240,review.total()); assertEquals(2,review.changes().size());
        assertEquals(100,cart.getProduct().getPrice()); assertSame(latest,review.items().get(0).getProduct());
    }
    @Test void sumsStockAcrossSizesAndRejectsRemovedVariants() {
        Product old=product(100,10);
        var cart=List.of(new CartItem(old,2,"M","Đen"),new CartItem(old,2,"L","Đen"));
        assertThrows(IllegalArgumentException.class,()->CheckoutReview.review(cart,List.of(product(100,3))));
        Product changed=product(100,10); changed.setSize("S");
        assertThrows(IllegalArgumentException.class,()->CheckoutReview.review(cart,List.of(changed)));
        assertThrows(IllegalArgumentException.class,()->CheckoutReview.review(cart,List.of()));
    }
    @Test void switchingAccountsClearsCart() {
        User a=new User(); a.setId(1); a.setRole("CUSTOMER");
        User b=new User(); b.setId(2); b.setRole("CUSTOMER");
        var session=SessionManager.getInstance(); var cart=CartService.getInstance();
        try {
            session.login("test-a",a); cart.addProduct(product(100,10),1,"M","Đen");
            session.login("test-b",b); assertTrue(cart.getItems().isEmpty());
            cart.addProduct(product(100,10),1,"M","Đen"); session.logout(); assertTrue(cart.getItems().isEmpty());
        } finally { session.logout(); }
    }
}
