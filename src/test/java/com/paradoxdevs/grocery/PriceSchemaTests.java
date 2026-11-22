package com.paradoxdevs.grocery;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Transactional
class PriceSchemaTests {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void storesDifferentCurrentPricesForEachGroceryStorePair() {
        long groceryId = insertGrocery("Milk X", "0001234567890");
        long storeAId = insertStore("Grocery A");
        long storeBId = insertStore("Grocery B");

        jdbcTemplate.update(
                "INSERT INTO item_store_prices (item_id, store_id, price) VALUES (?, ?, ?)",
                groceryId, storeAId, new BigDecimal("1.00"));
        jdbcTemplate.update(
                "INSERT INTO item_store_prices (item_id, store_id, price) VALUES (?, ?, ?)",
                groceryId, storeBId, new BigDecimal("2.00"));

        assertEquals(2, jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM item_store_prices WHERE item_id = ?",
                Integer.class, groceryId));
        assertThrows(DataIntegrityViolationException.class, () -> jdbcTemplate.update(
                "INSERT INTO item_store_prices (item_id, store_id, price) VALUES (?, ?, ?)",
                groceryId, storeAId, new BigDecimal("1.50")));
    }

    @Test
    void barcodeIsOptionalButUniqueWhenPresent() {
        insertGrocery("Cereal X", "0009876543210");
        jdbcTemplate.update("INSERT INTO items (name) VALUES (?)", "Unbarcoded Item A");
        jdbcTemplate.update("INSERT INTO items (name) VALUES (?)", "Unbarcoded Item B");

        assertEquals(2, jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM items WHERE barcode IS NULL", Integer.class));
        assertThrows(DataIntegrityViolationException.class, () ->
                insertGrocery("Duplicate Barcode Item", "0009876543210"));
    }

    @Test
    void databasePreventsDeletingStoreWithPrices() {
        long itemId = insertGrocery("Milk X", "0001234567890");
        long storeId = insertStore("Grocery A");
        jdbcTemplate.update(
                "INSERT INTO item_store_prices (item_id, store_id, price) VALUES (?, ?, ?)",
                itemId, storeId, new BigDecimal("1.00"));

        assertThrows(DataIntegrityViolationException.class, () ->
                jdbcTemplate.update("DELETE FROM stores WHERE id = ?", storeId));
    }

    private long insertGrocery(String name, String barcode) {
        jdbcTemplate.update("INSERT INTO items (name, barcode) VALUES (?, ?)", name, barcode);
        return jdbcTemplate.queryForObject(
                "SELECT id FROM items WHERE barcode = ?", Long.class, barcode);
    }

    private long insertStore(String name) {
        jdbcTemplate.update("INSERT INTO stores (name) VALUES (?)", name);
        return jdbcTemplate.queryForObject(
                "SELECT id FROM stores WHERE name = ?", Long.class, name);
    }
}
