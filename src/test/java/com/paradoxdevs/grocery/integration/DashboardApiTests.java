package com.paradoxdevs.grocery.integration;

import com.paradoxdevs.grocery.entity.Item;
import com.paradoxdevs.grocery.entity.Store;
import com.paradoxdevs.grocery.repository.ItemRepository;
import com.paradoxdevs.grocery.repository.StoreRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@Transactional
class DashboardApiTests {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private StoreRepository storeRepository;

    @Test
    void returnsZeroCountsWhenThereAreNoItemsOrStores() throws Exception {
        mockMvc.perform(get("/dashboard/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.itemCount").value(0))
                .andExpect(jsonPath("$.storeCount").value(0));
    }

    @Test
    void returnsSavedItemAndStoreCounts() throws Exception {
        Item item = new Item();
        item.setName("Milk");
        itemRepository.save(item);
        itemRepository.save(item("Bread"));

        Store store = new Store();
        store.setName("Market");
        storeRepository.save(store);

        mockMvc.perform(get("/dashboard/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.itemCount").value(2))
                .andExpect(jsonPath("$.storeCount").value(1));
    }

    private Item item(String name) {
        Item item = new Item();
        item.setName(name);
        return item;
    }
}
