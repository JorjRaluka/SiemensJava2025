package com.siemens.internship;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.siemens.internship.controller.ItemController;
import com.siemens.internship.model.DAO.Item;
import com.siemens.internship.model.DTO.ItemDTO;
import com.siemens.internship.model.DTO.ItemRepeatableDTO;
import com.siemens.internship.service.ItemService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ItemController.class)
class ItemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ItemService service;

    @Autowired
    private ObjectMapper objectMapper;

    private ItemDTO itemDTO;
    private ItemRepeatableDTO itemRepeatableDTO;
    private Item item;

    @BeforeEach
    void setUp() {
        itemDTO = new ItemDTO(null, "Name", "Desc", "NEW", null, "email@test.com");
        itemRepeatableDTO = new ItemRepeatableDTO(itemDTO, 3);
        item = new Item(1L, "Name", "Desc", "NEW", "email@test.com");
    }

    @Test
    void shouldCreateItem() throws Exception {
        when(service.save(any())).thenReturn(itemDTO);

        mockMvc.perform(post("/api/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemDTO)))
                .andExpect(status().isCreated());
    }

    @Test
    void shouldCreateRepeatableItem() throws Exception {
        when(service.saveItemRepeatable(any())).thenReturn(itemDTO);

        mockMvc.perform(post("/api/items/repeatable")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemRepeatableDTO)))
                .andExpect(status().isCreated());
    }

    @Test
    void shouldGetItemById_WhenFound() throws Exception {
        when(service.findById(1L)).thenReturn(Optional.of(itemDTO));

        mockMvc.perform(get("/api/items/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Name"));
    }

    @Test
    void shouldReturnNotFound_WhenItemNotFound() throws Exception {
        when(service.findById(1L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/items/1"))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldUpdateItem_WhenFound() throws Exception {
        when(service.updateItem(eq(1L), any())).thenReturn(Optional.of(itemDTO));

        mockMvc.perform(put("/api/items/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(item)))
                .andExpect(status().isOk());
    }

    @Test
    void shouldReturnNotFound_WhenUpdatingNonExistentItem() throws Exception {
        when(service.updateItem(eq(1L), any())).thenReturn(Optional.empty());

        mockMvc.perform(put("/api/items/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(item)))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturnNotFound_WhenDeletingNonExistentItem() throws Exception {
        when(service.existsById(1L)).thenReturn(false);

        mockMvc.perform(delete("/api/items/1"))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldDeleteItem_WhenExists() throws Exception {
        when(service.existsById(1L)).thenReturn(true);

        mockMvc.perform(delete("/api/items/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void shouldProcessItems() throws Exception {
        List<ItemDTO> processedItems = List.of(itemDTO);
        when(service.processItemsAsync()).thenReturn(CompletableFuture.completedFuture(processedItems));

        mockMvc.perform(get("/api/items/process"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Name"));
    }

    @Test
    void shouldGetAllItems() throws Exception {
        when(service.findAll()).thenReturn(Collections.singletonList(itemDTO));

        mockMvc.perform(get("/api/items"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Name"));
    }
}
