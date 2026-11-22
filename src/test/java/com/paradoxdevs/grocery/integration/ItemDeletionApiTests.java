package com.paradoxdevs.grocery.integration;

import com.paradoxdevs.grocery.entity.Item;
import com.paradoxdevs.grocery.entity.Price;
import com.paradoxdevs.grocery.entity.Store;
import com.paradoxdevs.grocery.repository.ItemRepository;
import com.paradoxdevs.grocery.repository.PriceRepository;
import com.paradoxdevs.grocery.repository.StoreRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@Transactional
class ItemDeletionApiTests {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private PriceRepository priceRepository;

    @Autowired
    private StoreRepository storeRepository;

    @Test
    void deletesItemAndItsStorePrices() throws Exception {
        Item item = new Item();
        item.setName("Milk X");
        item.setBarcode("0001234567890");
        Item savedItem = itemRepository.save(item);

        Store store = new Store();
        store.setName("Market");
        Store savedStore = storeRepository.save(store);

        Price price = new Price();
        price.setItem(savedItem);
        price.setStore(savedStore);
        price.setPrice(new BigDecimal("3.49"));
        priceRepository.save(price);

        mockMvc.perform(delete("/items/item/{id}", savedItem.getId()))
                .andExpect(status().isNoContent());

        assertFalse(itemRepository.existsById(savedItem.getId()));
        assertTrue(priceRepository.findAll().isEmpty());
    }

    @Test
    void returnsNotFoundWhenDeletingUnknownItem() throws Exception {
        mockMvc.perform(delete("/items/item/{id}", 404L))
                .andExpect(status().isNotFound());
    }
}
