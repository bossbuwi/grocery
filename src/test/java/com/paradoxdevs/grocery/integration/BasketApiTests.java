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
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
@Transactional
class BasketApiTests {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private PriceRepository priceRepository;

    @Test
    void createsListsReadsAndCalculatesBasketFromCurrentPrices() throws Exception {
        String token = login("basket-owner");
        Store store = storeRepository.save(store("Market"));
        Item milk = itemRepository.save(item("Milk"));
        Price price = priceRepository.save(price(milk, store, "3.49"));

        long basketId = createBasket(token, store.getId());
        mockMvc.perform(get("/baskets/{id}", basketId).header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.storeId").value(store.getId()))
                .andExpect(jsonPath("$.items", hasSize(0)))
                .andExpect(jsonPath("$.total").value(0));

        mockMvc.perform(post("/baskets/{id}/items", basketId)
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(addItemRequest(milk.getId(), 2)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(1)))
                .andExpect(jsonPath("$.items[0].quantity").value(2))
                .andExpect(jsonPath("$.items[0].unitPrice").value(3.49))
                .andExpect(jsonPath("$.items[0].lineTotal").value(6.98))
                .andExpect(jsonPath("$.total").value(6.98));

        mockMvc.perform(post("/baskets/{id}/items", basketId)
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"itemId\":%d}".formatted(milk.getId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(1)))
                .andExpect(jsonPath("$.items[0].quantity").value(3))
                .andExpect(jsonPath("$.total").value(10.47));

        price.setPrice(new BigDecimal("4.00"));
        priceRepository.save(price);
        mockMvc.perform(get("/baskets/{id}", basketId).header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].unitPrice").value(4.00))
                .andExpect(jsonPath("$.total").value(12.00));

        mockMvc.perform(get("/baskets").header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(basketId));

        mockMvc.perform(delete("/baskets/{id}/items/{itemId}", basketId, milk.getId())
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(0)))
                .andExpect(jsonPath("$.total").value(0));
    }

    @Test
    void allAuthenticatedUsersCanViewAndModifyAnyActiveBasket() throws Exception {
        String creatorToken = login("basket-creator");
        String otherUserToken = login("basket-other-user");
        Store store = storeRepository.save(store("Shared Market"));
        Item bread = itemRepository.save(item("Bread"));
        priceRepository.save(price(bread, store, "2.75"));
        long basketId = createBasket(creatorToken, store.getId());

        mockMvc.perform(get("/baskets").header("Authorization", bearer(otherUserToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(basketId));

        mockMvc.perform(post("/baskets/{id}/items", basketId)
                        .header("Authorization", bearer(otherUserToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(addItemRequest(bread.getId(), 1)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].itemName").value("Bread"))
                .andExpect(jsonPath("$.total").value(2.75));

        mockMvc.perform(post("/baskets/{id}/close", basketId)
                        .header("Authorization", bearer(otherUserToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CLOSED"));
        mockMvc.perform(get("/baskets/{id}", basketId).header("Authorization", bearer(creatorToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CLOSED"));
    }

    @Test
    void rejectsUnpricedItemsAndInvalidQuantities() throws Exception {
        String token = login("basket-validation");
        Store store = storeRepository.save(store("Market"));
        Item item = itemRepository.save(item("Unpriced Item"));
        long basketId = createBasket(token, store.getId());

        mockMvc.perform(post("/baskets/{id}/items", basketId)
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(addItemRequest(item.getId(), 1)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(ErrorCode.BASKET_ITEM_NOT_PRICED.getCode()))
                .andExpect(jsonPath("$.message").value(
                        "No price is recorded for this item at the basket's store."));

        priceRepository.save(price(item, store, "1.25"));
        mockMvc.perform(post("/baskets/{id}/items", basketId)
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(addItemRequest(item.getId(), 0)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void closedBasketsAreReadOnlyAndClosingIsIdempotent() throws Exception {
        String token = login("basket-closed");
        Store store = storeRepository.save(store("Market"));
        Item milk = itemRepository.save(item("Milk"));
        priceRepository.save(price(milk, store, "3.49"));
        long basketId = createBasket(token, store.getId());

        mockMvc.perform(post("/baskets/{id}/close", basketId)
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CLOSED"));
        mockMvc.perform(post("/baskets/{id}/close", basketId)
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CLOSED"));

        mockMvc.perform(post("/baskets/{id}/items", basketId)
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(addItemRequest(milk.getId(), 1)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value(ErrorCode.BASKET_CLOSED.getCode()));
        mockMvc.perform(delete("/baskets/{id}/items/{itemId}", basketId, milk.getId())
                        .header("Authorization", bearer(token)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value(ErrorCode.BASKET_CLOSED.getCode()));

        mockMvc.perform(get("/baskets/{id}", basketId).header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CLOSED"));
    }

    @Test
    void protectsCatalogRecordsReferencedByBaskets() throws Exception {
        String token = login("basket-protection");
        Store store = storeRepository.save(store("Market"));
        Store otherStore = storeRepository.save(store("Other Market"));
        Store emptyStore = storeRepository.save(store("Empty Market"));
        Item milk = itemRepository.save(item("Milk"));
        Price price = priceRepository.save(price(milk, store, "3.49"));
        long basketId = createBasket(token, store.getId());
        long emptyBasketId = createBasket(token, emptyStore.getId());

        mockMvc.perform(post("/baskets/{id}/items", basketId)
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(addItemRequest(milk.getId(), 1)))
                .andExpect(status().isOk());

        mockMvc.perform(delete("/prices/price/{id}", price.getId())
                        .header("Authorization", bearer(token)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("The requested resource has 1 BasketItem."));
        mockMvc.perform(put("/prices/price/{id}", price.getId())
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(priceRequest(milk.getId(), otherStore.getId(), "3.99")))
                .andExpect(status().isConflict());
        mockMvc.perform(delete("/items/item/{id}", milk.getId())
                        .header("Authorization", bearer(token)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("The requested resource has 1 BasketItem."));
        mockMvc.perform(delete("/stores/store/{id}", emptyStore.getId())
                        .header("Authorization", bearer(token)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("The requested resource has 1 Basket."));

        mockMvc.perform(get("/baskets/{id}", emptyBasketId).header("Authorization", bearer(token)))
                .andExpect(status().isOk());
    }

    @Test
    void returnsNotFoundForMissingBasketsAndStores() throws Exception {
        String token = login("basket-not-found");
        mockMvc.perform(get("/baskets/{id}", 404L).header("Authorization", bearer(token)))
                .andExpect(status().isNotFound());
        mockMvc.perform(post("/baskets")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"storeId\":404}"))
                .andExpect(status().isNotFound());
    }

    private String login(String username) throws Exception {
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "%s",
                                  "password": "StrongPassword123"
                                }
                                """.formatted(username)))
                .andExpect(status().isCreated());

        MvcResult result = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "%s",
                                  "password": "StrongPassword123"
                                }
                                """.formatted(username)))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode response = objectMapper.readTree(result.getResponse().getContentAsString());
        return response.get("accessToken").asText();
    }

    private long createBasket(String token, Long storeId) throws Exception {
        MvcResult result = mockMvc.perform(post("/baskets")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"storeId\":%d}".formatted(storeId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }

    private String addItemRequest(Long itemId, int quantity) {
        return "{\"itemId\":%d,\"quantity\":%d}".formatted(itemId, quantity);
    }

    private String priceRequest(Long itemId, Long storeId, String amount) {
        return """
                {
                  "itemId": %d,
                  "storeId": %d,
                  "price": %s
                }
                """.formatted(itemId, storeId, amount);
    }

    private Store store(String name) {
        Store store = new Store();
        store.setName(name);
        return store;
    }

    private Item item(String name) {
        Item item = new Item();
        item.setName(name);
        return item;
    }

    private Price price(Item item, Store store, String amount) {
        Price price = new Price();
        price.setItem(item);
        price.setStore(store);
        price.setPrice(new BigDecimal(amount));
        return price;
    }
}
