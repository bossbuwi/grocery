package com.paradoxdevs.grocery.integration;

import com.paradoxdevs.grocery.entity.Item;
import com.paradoxdevs.grocery.entity.Price;
import com.paradoxdevs.grocery.entity.Store;
import com.paradoxdevs.grocery.error.ErrorCode;
import com.paradoxdevs.grocery.repository.ItemRepository;
import com.paradoxdevs.grocery.repository.PriceRepository;
import com.paradoxdevs.grocery.repository.StoreRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@Transactional
class StoreApiTests {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private PriceRepository priceRepository;

    @Test
    void createsStoreAndReturnsItInTheStoreList() throws Exception {
        mockMvc.perform(post("/stores/store")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Market"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.name").value("Market"));

        mockMvc.perform(get("/stores/"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name").value("Market"));
    }

    @Test
    void preventsDeletingStoreWhileItHasItemPrices() throws Exception {
        Store store = new Store();
        store.setName("Market");
        Store savedStore = storeRepository.save(store);

        Item item = new Item();
        item.setName("Milk");
        Item savedItem = itemRepository.save(item);

        Price price = new Price();
        price.setStore(savedStore);
        price.setItem(savedItem);
        price.setPrice(new BigDecimal("3.49"));
        priceRepository.save(price);

        mockMvc.perform(get("/stores/store/{id}", savedStore.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Market"));

        mockMvc.perform(put("/stores/store/{id}", savedStore.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Updated Market"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Market"));

        mockMvc.perform(delete("/stores/store/{id}", savedStore.getId()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value(ErrorCode.RESOURCE_HAS_CHILDREN.getCode()))
                .andExpect(jsonPath("$.message").value("The requested resource has 1 Price."));

        assertTrue(storeRepository.existsById(savedStore.getId()));
        assertTrue(priceRepository.existsById(price.getId()));
    }

    @Test
    void returnsNotFoundForUnknownStoreAndRejectsInvalidNames() throws Exception {
        mockMvc.perform(get("/stores/store/{id}", 404L))
                .andExpect(status().isNotFound());
        mockMvc.perform(put("/stores/store/{id}", 404L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Market\"}"))
                .andExpect(status().isNotFound());
        mockMvc.perform(delete("/stores/store/{id}", 404L))
                .andExpect(status().isNotFound());

        mockMvc.perform(post("/stores/store")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\" Market\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void rejectsUnknownRequestFields() throws Exception {
        mockMvc.perform(post("/stores/store")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Market",
                                  "barcode": "4800361291446"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(ErrorCode.MALFORMED_REQUEST.getCode()));
    }

    @Test
    void deletesStoreWhenItHasNoPrices() throws Exception {
        Store store = new Store();
        store.setName("Market");
        Store savedStore = storeRepository.save(store);

        mockMvc.perform(delete("/stores/store/{id}", savedStore.getId()))
                .andExpect(status().isNoContent());

        assertFalse(storeRepository.existsById(savedStore.getId()));
    }
}
