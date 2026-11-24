package com.paradoxdevs.grocery.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.forwardedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class FrontendStaticResourceTests {
    private static final Pattern JAVASCRIPT_BUNDLE =
            Pattern.compile("src=\"([^\"]+\\.js)\"");

    @Autowired
    private MockMvc mockMvc;

    @Test
    void servesLoginPageAndItsAssetsWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(forwardedUrl("index.html"));

        MvcResult indexResult = mockMvc.perform(get("/index.html"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("<div id=\"root\"></div>")))
                .andReturn();

        Matcher scriptMatcher = JAVASCRIPT_BUNDLE.matcher(
                indexResult.getResponse().getContentAsString());
        org.junit.jupiter.api.Assertions.assertTrue(scriptMatcher.find());

        mockMvc.perform(get(scriptMatcher.group(1)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/items"))
                .andExpect(status().isUnauthorized());
    }
}
