package com.paradoxdevs.grocery.integration;

import com.paradoxdevs.grocery.entity.Item;
import com.paradoxdevs.grocery.error.ErrorCode;
import com.paradoxdevs.grocery.repository.ItemRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@Transactional
class ItemLookupApiTests {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ItemRepository itemRepository;

    @Test
    void getsItemByIdAndReturnsNotFoundForUnknownId() throws Exception {
        Item item = new Item();
        item.setName("Milk X");
        item.setBarcode("0001234567890");
        Item savedItem = itemRepository.save(item);

        mockMvc.perform(get("/items/item/{id}", savedItem.getId())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(savedItem.getId()))
                .andExpect(jsonPath("$.name").value("Milk X"))
                .andExpect(jsonPath("$.barcode").value("0001234567890"));

        mockMvc.perform(get("/items/item/{id}", savedItem.getId() + 1_000_000))
                .andExpect(status().isNotFound());
    }

    @Test
    void returnsBadRequestWhenItemIdHasAnInvalidType() throws Exception {
        mockMvc.perform(get("/items/item/not-a-number"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(ErrorCode.REQUEST_VALIDATION_ERROR.getCode()))
                .andExpect(jsonPath("$.fieldErrors[0]").value("id: must be a valid Long"));
    }

    @Test
    void updatesItemByIdAndReturnsNotFoundForUnknownId() throws Exception {
        Item item = new Item();
        item.setName("Milk X");
        item.setBarcode("0001234567890");
        Item savedItem = itemRepository.save(item);

        mockMvc.perform(put("/items/item/{id}", savedItem.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Milk Y",
                                  "barcode": "9876543210000"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(savedItem.getId()))
                .andExpect(jsonPath("$.name").value("Milk Y"))
                .andExpect(jsonPath("$.barcode").value("9876543210000"));

        mockMvc.perform(put("/items/item/{id}", savedItem.getId() + 1_000_000)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Missing",
                                  "barcode": null
                                }
                                """))
                .andExpect(status().isNotFound());
    }
}
