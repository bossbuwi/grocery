package com.paradoxdevs.grocery.service.impl;

import com.paradoxdevs.grocery.api.request.ItemRequest;
import com.paradoxdevs.grocery.api.response.ItemResponse;
import com.paradoxdevs.grocery.entity.Item;
import com.paradoxdevs.grocery.repository.ItemRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ItemServiceImplTest {
    @Mock
    private ItemRepository itemRepository;

    @InjectMocks
    private ItemServiceImpl itemService;

    @Test
    void createPersistsRequestFieldsAndMapsSavedItemToResponse() {
        ItemRequest request = new ItemRequest();
        request.setName("Milk X");
        request.setBarcode("0001234567890");

        when(itemRepository.save(any(Item.class))).thenAnswer(invocation -> {
            Item item = invocation.getArgument(0);
            item.setId(42L);
            return item;
        });

        ItemResponse response = itemService.create(request);

        ArgumentCaptor<Item> itemCaptor = ArgumentCaptor.forClass(Item.class);
        verify(itemRepository).save(itemCaptor.capture());
        assertEquals("Milk X", itemCaptor.getValue().getName());
        assertEquals("0001234567890", itemCaptor.getValue().getBarcode());
        assertEquals(42L, response.getId());
        assertEquals("Milk X", response.getName());
        assertEquals("0001234567890", response.getBarcode());
    }

    @Test
    void findAllMapsRepositoryItemsToResponses() {
        Item milk = item(1L, "Milk X", "0001234567890");
        Item cereal = item(2L, "Cereal Y", null);
        when(itemRepository.findAll()).thenReturn(List.of(milk, cereal));

        List<ItemResponse> responses = itemService.findAll();

        assertEquals(2, responses.size());
        assertEquals(1L, responses.getFirst().getId());
        assertEquals("Milk X", responses.get(0).getName());
        assertEquals("0001234567890", responses.get(0).getBarcode());
        assertEquals(2L, responses.get(1).getId());
        assertEquals("Cereal Y", responses.get(1).getName());
        assertNull(responses.get(1).getBarcode());
        verify(itemRepository).findAll();
    }

    private Item item(Long id, String name, String barcode) {
        Item item = new Item();
        item.setId(id);
        item.setName(name);
        item.setBarcode(barcode);
        return item;
    }
}
