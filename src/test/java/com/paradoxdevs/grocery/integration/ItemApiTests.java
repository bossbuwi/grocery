package com.paradoxdevs.grocery.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.hasSize;
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
                .andExpect(jsonPath("$.barcode").value("0001234567890"));

        mockMvc.perform(get("/items/"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name").value("Milk X"))
                .andExpect(jsonPath("$[0].barcode").value("0001234567890"));
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
}
