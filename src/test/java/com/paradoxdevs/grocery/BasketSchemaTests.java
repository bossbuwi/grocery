package com.paradoxdevs.grocery;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Transactional
class BasketSchemaTests {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void databasePreventsDeletingStoresAndPricesReferencedByBaskets() {
        long storeId = insertStore("Basket Store");
        long itemId = insertItem("Basket Item");
        long priceId = insertPrice(itemId, storeId);
        long basketId = insertBasket(storeId, "ACTIVE");
        insertBasketItem(basketId, priceId, 1);

        assertThrows(DataIntegrityViolationException.class, () ->
                jdbcTemplate.update("DELETE FROM stores WHERE id = ?", storeId));
        assertThrows(DataIntegrityViolationException.class, () ->
                jdbcTemplate.update("DELETE FROM item_store_prices WHERE id = ?", priceId));
    }

    @Test
    void databasePreventsDeletingStoreReferencedByAnEmptyBasket() {
        long storeId = insertStore("Empty Basket Store");
        insertBasket(storeId, "ACTIVE");

        assertThrows(DataIntegrityViolationException.class, () ->
                jdbcTemplate.update("DELETE FROM stores WHERE id = ?", storeId));
    }

    @Test
    void databaseRejectsInvalidBasketStatusesAndQuantities() {
        long storeId = insertStore("Validation Store");
        long itemId = insertItem("Validation Item");
        long priceId = insertPrice(itemId, storeId);
        long basketId = insertBasket(storeId, "ACTIVE");

        assertThrows(DataIntegrityViolationException.class, () ->
                insertBasket(storeId, "PAUSED"));
        assertThrows(DataIntegrityViolationException.class, () ->
                insertBasketItem(basketId, priceId, 0));
        insertBasketItem(basketId, priceId, 1);
        assertThrows(DataIntegrityViolationException.class, () ->
                insertBasketItem(basketId, priceId, 2));
    }

    private long insertStore(String name) {
        jdbcTemplate.update("INSERT INTO stores (name) VALUES (?)", name);
        return jdbcTemplate.queryForObject("SELECT id FROM stores WHERE name = ?", Long.class, name);
    }

    private long insertItem(String name) {
        jdbcTemplate.update("INSERT INTO items (name) VALUES (?)", name);
        return jdbcTemplate.queryForObject("SELECT id FROM items WHERE name = ?", Long.class, name);
    }

    private long insertPrice(long itemId, long storeId) {
        jdbcTemplate.update(
                "INSERT INTO item_store_prices (item_id, store_id, price) VALUES (?, ?, 1.00)",
                itemId, storeId);
        return jdbcTemplate.queryForObject(
                "SELECT id FROM item_store_prices WHERE item_id = ? AND store_id = ?",
                Long.class, itemId, storeId);
    }

    private long insertBasket(long storeId, String status) {
        jdbcTemplate.update("INSERT INTO baskets (store_id, status) VALUES (?, ?)", storeId, status);
        return jdbcTemplate.queryForObject(
                "SELECT id FROM baskets WHERE store_id = ? AND status = ? ORDER BY id DESC LIMIT 1",
                Long.class, storeId, status);
    }

    private void insertBasketItem(long basketId, long priceId, int quantity) {
        jdbcTemplate.update(
                "INSERT INTO basket_items (basket_id, price_id, quantity) VALUES (?, ?, ?)",
                basketId, priceId, quantity);
    }
}
