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
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@Transactional
class ItemApiTests {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private PriceRepository priceRepository;

    @Test
    void createsAnItemAndReturnsItInTheItemList() throws Exception {
        mockMvc.perform(post("/items/item")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Milk X",
                                  "barcode": "0001234567890"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.name").value("Milk X"))
                .andExpect(jsonPath("$.barcode").value("0001234567890"))
                .andExpect(jsonPath("$.stores").doesNotExist());

        mockMvc.perform(get("/items/"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name").value("Milk X"))
                .andExpect(jsonPath("$[0].barcode").value("0001234567890"))
                .andExpect(jsonPath("$[0].stores", hasSize(0)));
    }

    @Test
    void includesAvailableStoresAndPricesWhenReadingAnItem() throws Exception {
        Item item = new Item();
        item.setName("Milk X");
        item.setBarcode("0001234567890");
        Item savedItem = itemRepository.save(item);

        Store firstStore = storeRepository.save(store("Market A"));
        Store secondStore = storeRepository.save(store("Market B"));
        priceRepository.save(price(savedItem, firstStore, "1.25"));
        priceRepository.save(price(savedItem, secondStore, "2.50"));

        mockMvc.perform(get("/items/item/{id}", savedItem.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stores", hasSize(2)))
                .andExpect(jsonPath("$.stores[*].name", hasItem("Market A")))
                .andExpect(jsonPath("$.stores[*].name", hasItem("Market B")))
                .andExpect(jsonPath("$.stores[?(@.id == " + firstStore.getId() + ")].price",
                        hasItem(1.25)))
                .andExpect(jsonPath("$.stores[?(@.id == " + secondStore.getId() + ")].price",
                        hasItem(2.50)));

        mockMvc.perform(get("/items/"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].stores", hasSize(2)))
                .andExpect(jsonPath("$[0].stores[*].name", hasItem("Market A")))
                .andExpect(jsonPath("$[0].stores[*].name", hasItem("Market B")))
                .andExpect(jsonPath("$[0].stores[?(@.id == " + firstStore.getId() + ")].price",
                        hasItem(1.25)))
                .andExpect(jsonPath("$[0].stores[?(@.id == " + secondStore.getId() + ")].price",
                        hasItem(2.50)));
    }

    @Test
    void validatesItemNameAndOptionalBarcode() throws Exception {
        mockMvc.perform(post("/items/item")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Unbarcoded item"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.barcode").doesNotExist());

        mockMvc.perform(post("/items/item")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": " Item with whitespace",
                                  "barcode": "1234567X"
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    private Store store(String name) {
        Store store = new Store();
        store.setName(name);
        return storeRepository.save(store);
    }

    private Price price(Item item, Store store, String amount) {
        Price price = new Price();
        price.setItem(item);
        price.setStore(store);
        price.setPrice(new BigDecimal(amount));
        return price;
    }
}
