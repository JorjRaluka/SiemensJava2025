package com.siemens.internship;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.siemens.internship.controller.ItemController;
import com.siemens.internship.model.DAO.Item;
import com.siemens.internship.model.DTO.ItemDTO;
import com.siemens.internship.model.DTO.ItemRepeatableDTO;
import com.siemens.internship.service.ItemService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.*;
import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ItemController.class)
public class ItemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ItemService service;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testCreateItem() throws Exception {
        ItemDTO dto = new ItemDTO();
        dto.setName("Test");
        dto.setEmail("email@test.com");

        when(service.save(any())).thenReturn(dto);

        mockMvc.perform(post("/api/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated());
    }

    @Test
    void testCreateRepeatableItem() throws Exception {
        ItemDTO dto = new ItemDTO(null, "Name", "Desc", "NEW", null, "email@test.com");
        ItemRepeatableDTO repeatable = new ItemRepeatableDTO(dto, 3);

        when(service.saveItemRepeatable(any())).thenReturn(dto);

        mockMvc.perform(post("/api/items/repeatable")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(repeatable)))
                .andExpect(status().isCreated());
    }

    @Test
    void testGetItemById_Found() throws Exception {
        Item item = new Item(1L, "Name", "Desc", "NEW", "email@test.com");
        when(service.findById(1L)).thenReturn(Optional.of(item));

        mockMvc.perform(get("/api/items/1"))
                .andExpect(status().isOk());
    }

    @Test
    void testGetItemById_NotFound() throws Exception {
        when(service.findById(1L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/items/1"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testUpdateItem_Success() throws Exception {
        Item item = new Item(1L, "Updated", "Desc", "NEW", "email@test.com");
        when(service.updateItem(eq(1L), any())).thenReturn(Optional.of(item));

        mockMvc.perform(put("/api/items/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(item)))
                .andExpect(status().isOk());
    }

    @Test
    void testUpdateItem_NotFound() throws Exception {
        Item item = new Item(1L, "Updated", "Desc", "NEW", "email@test.com");
        when(service.updateItem(eq(1L), any())).thenReturn(Optional.empty());

        mockMvc.perform(put("/api/items/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(item)))
                .andExpect(status().isNotFound());
    }

    @Test
    void testDeleteItem_NotFound() throws Exception {
        when(service.existsById(1L)).thenReturn(false);

        mockMvc.perform(delete("/api/items/1"))
                .andExpect(status().isNotFound());
    }
    @Test
    void testProcessItems() throws Exception {
        List<Item> items = List.of(
                new Item(1L, "Name", "Desc", "PROCESSED", "email@test.com"));
        when(service.processItemsAsync()).thenReturn(CompletableFuture.completedFuture(items));

        mockMvc.perform(get("/api/items/process"))
                .andExpect(status().isOk());
    }



    @Test
    void testGetAllItems() throws Exception {
        mockMvc.perform(get("/api/items"))
                .andExpect(status().isOk());
    }

    @Test
    void testDeleteItem() throws Exception {
        when(service.existsById(1L)).thenReturn(true);

        mockMvc.perform(delete("/api/items/1"))
                .andExpect(status().isNoContent());
    }
}
