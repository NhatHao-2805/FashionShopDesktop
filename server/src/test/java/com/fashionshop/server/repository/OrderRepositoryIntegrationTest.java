package com.fashionshop.server.repository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import static org.junit.jupiter.api.Assertions.*;
import com.fashionshop.server.model.Order;
import com.fashionshop.server.model.OrderItem;
import java.sql.*;
import java.math.BigDecimal;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;

/** Creates and drops only a uniquely named test database; never uses the application database. */
@EnabledIfEnvironmentVariable(named="FASHION_TEST_DB_URL", matches=".+")
class OrderRepositoryIntegrationTest {
    static String database="fashion_test_"+UUID.randomUUID().toString().replace("-", "");
    static String url=System.getenv("FASHION_TEST_DB_URL");
    static String username=System.getenv().getOrDefault("FASHION_TEST_DB_USER","root");
    static String password=System.getenv().getOrDefault("FASHION_TEST_DB_PASSWORD","");
    static Connection connect() throws SQLException { return DriverManager.getConnection(url,username,password); }
    static Connection testConnection() throws SQLException { Connection c=connect(); c.setCatalog(database); return c; }
    OrderRepository repository=new OrderRepository(OrderRepositoryIntegrationTest::testConnection);
    @BeforeAll static void createDatabase() throws Exception {
        try(Connection c=connect(); Statement s=c.createStatement()) {
            s.execute("CREATE DATABASE `"+database+"` CHARACTER SET utf8mb4");
            c.setCatalog(database);
            String schema=Files.readString(Path.of(System.getProperty("basedir", "."), "../database/fashion_shop_mariadb.sql"));
            schema=schema.replaceFirst("(?s)CREATE DATABASE.*?;", "").replaceFirst("USE fashion_shop;", "");
            try (Statement schemaStatement = c.createStatement()) {
                for(String sql:schema.split(";")) if(!sql.isBlank()) schemaStatement.execute(sql);
            }
        }
    }
    @AfterAll static void cleanupDatabase() throws Exception {
        if(!database.matches("fashion_test_[a-f0-9]{32}")) throw new IllegalStateException("Invalid test database");
        try(Connection c=connect(); Statement s=c.createStatement()) { s.execute("DROP DATABASE IF EXISTS `"+database+"`"); }
    }
    @BeforeEach void fixtures() throws Exception {
        try(Connection c=testConnection(); Statement s=c.createStatement()) {
            s.executeUpdate("DELETE FROM order_requests");
            s.executeUpdate("DELETE FROM inventory_transactions"); s.executeUpdate("DELETE FROM order_items");
            s.executeUpdate("DELETE FROM orders"); s.executeUpdate("DELETE FROM product_variants");
            s.executeUpdate("DELETE FROM products"); s.executeUpdate("DELETE FROM users");
            s.executeUpdate("INSERT INTO users(id,name,email,password_hash,role,status) VALUES(1,'A','a@example.test','not-a-real-password','CUSTOMER','ACTIVE'),(2,'B','b@example.test','not-a-real-password','CUSTOMER','ACTIVE')");
            s.executeUpdate("INSERT INTO products(id,name,price,size,color,stock,status) VALUES(1,'Test shirt',100,'M,L','Black',1,'ACTIVE')");
        }
    }
    @Test void concurrentRetriesCommitOneOrderAndReserveStockOnce() throws Exception {
        trackVariants(1, 0);
        String key = UUID.randomUUID().toString();
        String digest = "a".repeat(64);
        var start = new CountDownLatch(1);
        try (var workers = Executors.newFixedThreadPool(2)) {
            Callable<Order> submit = () -> {
                start.await();
                return repository.create(order("M", new BigDecimal("100")), key, digest);
            };
            var first = workers.submit(submit);
            var second = workers.submit(submit);
            start.countDown();
            assertEquals(first.get(15, TimeUnit.SECONDS).getId(), second.get(15, TimeUnit.SECONDS).getId());
        }
        assertEquals(1, number("SELECT COUNT(*) FROM orders"));
        assertEquals(1, number("SELECT COUNT(*) FROM order_requests"));
        assertEquals(1, number("SELECT COUNT(*) FROM inventory_transactions"));
        assertEquals(0, number("SELECT stock FROM product_variants WHERE size_value='M'"));
    }

    @Test void replayAfterCancellationReturnsSameOrderAndRejectsDifferentPayload() throws Exception {
        trackVariants(1, 0);
        String key = UUID.randomUUID().toString();
        String digest = "a".repeat(64);
        Order created = repository.create(order("M", new BigDecimal("100")), key, digest);
        assertEquals(created.getId(), repository.findByRequestId(1, key).getId());
        assertNull(repository.findByRequestId(2, key));
        repository.cancel(created.getId(), 1);
        Order replay = repository.create(order("M", new BigDecimal("100")), key, digest);
        assertEquals(created.getId(), replay.getId());
        assertEquals("CANCELLED", replay.getStatus());
        assertThrows(OrderRepository.CheckoutConflict.class,
                () -> repository.create(order("M", new BigDecimal("100")), key, "b".repeat(64)));
        assertEquals(1, number("SELECT COUNT(*) FROM orders"));
        assertEquals(1, number("SELECT stock FROM product_variants WHERE size_value='M'"));
    }

    @Test void failedCheckoutDoesNotConsumeKeyAndKeyIsScopedToCustomer() throws Exception {
        String key = UUID.randomUUID().toString();
        String digest = "a".repeat(64);
        assertThrows(IllegalArgumentException.class,
                () -> repository.create(order("M", new BigDecimal("99")), key, digest));
        assertEquals(0, number("SELECT COUNT(*) FROM order_requests"));
        Order created = repository.create(order("M", new BigDecimal("100")), key, digest);
        repository.cancel(created.getId(), 1);
        Order other = order("M", new BigDecimal("100"));
        other.setUserId(2);
        assertNotEquals(created.getId(), repository.create(other, key, digest).getId());
        assertEquals(2, number("SELECT COUNT(*) FROM order_requests"));
    }
    Order order(String size, BigDecimal price) {
        Order o=new Order(); o.setUserId(1); o.setReceiverName("Test customer"); o.setPhone("0912345678");
        o.setAddress("123 Test Street"); o.setPaymentMethod("COD");
        OrderItem i=new OrderItem(); i.setProductId(1); i.setQuantity(1); i.setSelectedSize(size); i.setSelectedColor("Black"); i.setExpectedPrice(price);
        o.setItems(List.of(i)); return o;
    }
    int number(String query) throws SQLException {
        try(Connection c=testConnection(); Statement s=c.createStatement(); ResultSet r=s.executeQuery(query)) { r.next(); return r.getInt(1); }
    }
    @Test void concurrentPurchasesCannotOversell() throws Exception {
        trackVariants(1, 0);
        ExecutorService workers=Executors.newFixedThreadPool(2); CountDownLatch start=new CountDownLatch(1);
        Callable<Boolean> buy=()->{ start.await(); try { repository.create(order("M",new BigDecimal("100"))); return true; }
            catch(IllegalArgumentException expected) { return false; } };
        try {
            Future<Boolean> a=workers.submit(buy), b=workers.submit(buy); start.countDown();
            int success=(a.get(15,TimeUnit.SECONDS)?1:0)+(b.get(15,TimeUnit.SECONDS)?1:0);
            assertEquals(1,success); assertEquals(0,number("SELECT stock FROM products WHERE id=1"));
            assertEquals(0, number("SELECT stock FROM product_variants WHERE size_value='M'"));
            assertEquals(1,number("SELECT COUNT(*) FROM orders"));
        } finally { workers.shutdownNow(); }
    }
    @Test void cancellationRestoresStockOnlyOnceAndProtectsOwnership() throws Exception {
        Order created=repository.create(order("M",new BigDecimal("100")));
        assertNull(repository.findByIdAndUserId(created.getId(),2));
        assertThrows(IllegalArgumentException.class,()->repository.cancel(created.getId(),2));
        assertEquals(0,number("SELECT stock FROM products WHERE id=1"));
        repository.cancel(created.getId(),1);
        assertEquals(1,number("SELECT stock FROM products WHERE id=1"));
        assertThrows(IllegalArgumentException.class,()->repository.cancel(created.getId(),1));
        assertEquals(1,number("SELECT stock FROM products WHERE id=1"));
        Order read=repository.findByIdAndUserId(created.getId(),1);
        assertEquals("M",read.getItems().get(0).getSelectedSize());
    }
    @Test void insufficientCombinedVariantStockRollsBackAllWrites() throws Exception {
        Order request=order("M",new BigDecimal("100"));
        OrderItem second=order("L",new BigDecimal("100")).getItems().get(0);
        request.setItems(List.of(request.getItems().get(0), second));
        assertThrows(IllegalArgumentException.class,()->repository.create(request));
        assertEquals(0,number("SELECT COUNT(*) FROM orders"));
        assertEquals(0,number("SELECT COUNT(*) FROM order_items"));
        assertEquals(0,number("SELECT COUNT(*) FROM inventory_transactions"));
        assertEquals(1,number("SELECT stock FROM products WHERE id=1"));
    }
    @Test void stalePriceAndInvalidVariantLeaveNoOrderOrStockChanges() throws Exception {
        assertThrows(IllegalArgumentException.class,()->repository.create(order("M",new BigDecimal("99"))));
        assertThrows(IllegalArgumentException.class,()->repository.create(order("XXL",new BigDecimal("100"))));
        assertEquals(0,number("SELECT COUNT(*) FROM orders"));
        assertEquals(1,number("SELECT stock FROM products WHERE id=1"));
    }

    @Test void trackedStockRejectsSoldOutSizeAndRestoresOnlyPurchasedVariant() throws Exception {
        trackVariants(0, 1);
        assertThrows(IllegalArgumentException.class, () -> repository.create(order("M", new BigDecimal("100"))));

        Order created = repository.create(order("L", new BigDecimal("100")));
        Order listed = repository.findByUserId(1).getFirst();
        assertEquals(created.getId(), listed.getId());
        assertEquals("PENDING", listed.getStatus());
        Order detail = repository.findByIdAndUserId(created.getId(), 1);
        assertEquals("L", detail.getItems().getFirst().getSelectedSize());
        assertEquals("Black", detail.getItems().getFirst().getSelectedColor());
        assertEquals(0, new BigDecimal("100").compareTo(detail.getTotalAmount()));
        assertEquals(0, number("SELECT stock FROM product_variants WHERE size_value='L'"));
        assertEquals(0, number("SELECT stock FROM products WHERE id=1"));

        repository.cancel(created.getId(), 1);
        assertEquals("CANCELLED", repository.findByIdAndUserId(created.getId(), 1).getStatus());
        assertEquals(1, number("SELECT stock FROM product_variants WHERE size_value='L'"));
        assertEquals(0, number("SELECT stock FROM product_variants WHERE size_value='M'"));
        assertEquals(1, number("SELECT stock FROM products WHERE id=1"));
        assertThrows(IllegalArgumentException.class, () -> repository.cancel(created.getId(), 1));
    }

    @Test void rollbackRestoresReservedVariantWhenAnotherLineIsUnavailable() throws Exception {
        trackVariants(1, 0);
        Order request = order("M", new BigDecimal("100"));
        request.setItems(List.of(request.getItems().getFirst(),
                order("L", new BigDecimal("100")).getItems().getFirst()));
        assertThrows(IllegalArgumentException.class, () -> repository.create(request));
        assertEquals(1, number("SELECT stock FROM product_variants WHERE size_value='M'"));
        assertEquals(1, number("SELECT stock FROM products WHERE id=1"));
        assertEquals(0, number("SELECT COUNT(*) FROM orders"));
    }

    @Test void cancellingLegacyOrderReturnsStockToUnallocatedPool() throws Exception {
        Order legacy = repository.create(order("M", new BigDecimal("100")));
        trackVariants(0, 0);
        repository.cancel(legacy.getId(), 1);
        assertEquals(1, number("SELECT stock FROM products WHERE id=1"));
        assertEquals(0, number("SELECT stock FROM product_variants WHERE size_value='M'"));
    }

    @Test void allocationKeepsPhysicalTotalAndRejectsStaleOrInvalidCounts() throws Exception {
        var variants = new VariantStockRepository(OrderRepositoryIntegrationTest::testConnection);
        var original = variants.get(1);
        assertTrue(original.variants().isEmpty());
        var inStock = new VariantStockRepository.Allocation("M", "Black", 1);
        var soldOut = new VariantStockRepository.Allocation("L", "Black", 0);
        assertThrows(IllegalArgumentException.class, () -> variants.allocate(1, original, List.of(inStock)));
        assertThrows(IllegalArgumentException.class, () -> variants.allocate(1, original,
                List.of(inStock, new VariantStockRepository.Allocation("L", "Black", 1))));
        var saved = variants.allocate(1, original, List.of(inStock, soldOut));
        assertEquals(original.total(), saved.total());
        assertEquals(2, saved.variants().size());
        assertThrows(IllegalArgumentException.class,
                () -> variants.allocate(1, original, List.of(inStock, soldOut)));
        repository.create(order("M", new BigDecimal("100")));
        assertThrows(IllegalArgumentException.class,
                () -> variants.allocate(1, saved, List.of(inStock, soldOut)));
    }

    private void trackVariants(int medium, int large) throws SQLException {
        try (Connection connection = testConnection();
             PreparedStatement insert = connection.prepareStatement("""
                     INSERT INTO product_variants (product_id, size_value, color_value, stock)
                     VALUES (1, 'M', 'Black', ?), (1, 'L', 'Black', ?)
                     """)) {
            insert.setInt(1, medium);
            insert.setInt(2, large);
            insert.executeUpdate();
        }
    }
}
