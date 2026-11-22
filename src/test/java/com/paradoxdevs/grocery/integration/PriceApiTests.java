package com.paradoxdevs.grocery.integration;

import com.paradoxdevs.grocery.entity.Item;
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

import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@Transactional
class PriceApiTests {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private PriceRepository priceRepository;

    @Test
    void createsListsFetchesUpdatesAndDeletesPrice() throws Exception {
        Item firstItem = itemRepository.save(item("Milk"));
        Item secondItem = itemRepository.save(item("Bread"));
        Store firstStore = storeRepository.save(store("Market A"));
        Store secondStore = storeRepository.save(store("Market B"));

        String createRequest = requestJson(firstItem.getId(), firstStore.getId(), "3.49");
        mockMvc.perform(post("/prices/price")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createRequest))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.itemId").value(firstItem.getId()))
                .andExpect(jsonPath("$.storeId").value(firstStore.getId()))
                .andExpect(jsonPath("$.price").value(3.49));
        long priceId = priceRepository.findAll().getFirst().getId();

        mockMvc.perform(get("/prices/"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].itemId").value(firstItem.getId()));

        mockMvc.perform(get("/prices/price/{id}", priceId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.storeId").value(firstStore.getId()));

        mockMvc.perform(put("/prices/price/{id}", priceId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson(secondItem.getId(), secondStore.getId(), "4.25")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.itemId").value(secondItem.getId()))
                .andExpect(jsonPath("$.storeId").value(secondStore.getId()))
                .andExpect(jsonPath("$.price").value(4.25));

        mockMvc.perform(delete("/prices/price/{id}", priceId))
                .andExpect(status().isNoContent());
        assertFalse(priceRepository.existsById(priceId));
    }

    @Test
    void rejectsMissingRelationsDuplicatePairsAndInvalidPrices() throws Exception {
        Item item = itemRepository.save(item("Milk"));
        Store store = storeRepository.save(store("Market"));

        mockMvc.perform(post("/prices/price")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson(999L, store.getId(), "3.49")))
                .andExpect(status().isNotFound());

        mockMvc.perform(post("/prices/price")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson(item.getId(), store.getId(), "3.49")))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/prices/price")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson(item.getId(), store.getId(), "4.49")))
                .andExpect(status().isConflict());

        mockMvc.perform(post("/prices/price")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson(item.getId(), store.getId(), "0")))
                .andExpect(status().isBadRequest());
    }

    @Test
    void returnsNotFoundForUnknownPriceOnReadUpdateAndDelete() throws Exception {
        mockMvc.perform(get("/prices/price/{id}", 404L))
                .andExpect(status().isNotFound());
        mockMvc.perform(put("/prices/price/{id}", 404L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson(1L, 1L, "1.00")))
                .andExpect(status().isNotFound());
        mockMvc.perform(delete("/prices/price/{id}", 404L))
                .andExpect(status().isNotFound());
    }

    @Test
    void returnsNotFoundWhenPriceIdIsOmittedOrRouteDoesNotExist() throws Exception {
        mockMvc.perform(delete("/prices/price/"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(ErrorCode.RESOURCE_NOT_FOUND.getCode()))
                .andExpect(jsonPath("$.message").value("The requested resource was not found."));

        mockMvc.perform(get("/not-a-route"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(ErrorCode.RESOURCE_NOT_FOUND.getCode()));
    }

    private Item item(String name) {
        Item item = new Item();
        item.setName(name);
        return item;
    }

    private Store store(String name) {
        Store store = new Store();
        store.setName(name);
        return store;
    }

    private String requestJson(Long itemId, Long storeId, String price) {
        return """
                {
                  "itemId": %d,
                  "storeId": %d,
                  "price": %s
                }
                """.formatted(itemId, storeId, price);
    }
}
