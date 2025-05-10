package com.siemens.internship;

import com.siemens.internship.model.DAO.Item;
import com.siemens.internship.model.DTO.ItemDTO;
import com.siemens.internship.model.DTO.ItemRepeatableDTO;
import com.siemens.internship.repository.ItemRepository;
import com.siemens.internship.service.ItemService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.*;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ItemServiceTest {

    @InjectMocks
    private ItemService service;

    @Mock
    private ItemRepository repository;

    @Mock
    private ModelMapper modelMapper;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testFindAll() {
        List<Item> items = List.of(new Item(1L, "Test", "desc", "NEW", "email@test.com"));
        ItemDTO dto = new ItemDTO(1L, "Test", "desc", "NEW", null, "email@test.com");

        when(repository.findAll()).thenReturn(items);
        when(modelMapper.map(any(Item.class), eq(ItemDTO.class))).thenReturn(dto);

        List<ItemDTO> result = service.findAll();

        assertEquals(1, result.size());
        assertEquals("Test", result.get(0).getName());
    }

    @Test
    void testSave() {
        ItemDTO dto = new ItemDTO(null, "Test", "desc", "NEW", null, "email@test.com");
        Item entity = new Item(1L, "Test", "desc", "NEW", "email@test.com");

        when(modelMapper.map(dto, Item.class)).thenReturn(entity);
        when(repository.save(entity)).thenReturn(entity);
        when(modelMapper.map(entity, ItemDTO.class)).thenReturn(dto);

        ItemDTO saved = service.save(dto);

        assertEquals("Test", saved.getName());
    }

    @Test
    void testUpdateItem_Success() {
        Item existing = new Item(1L, "Old", "desc", "NEW", "email@test.com");
        Item updated = new Item(1L, "Updated", "desc", "NEW", "email@test.com");

        when(repository.findById(1L)).thenReturn(Optional.of(existing));
        when(repository.save(updated)).thenReturn(updated);
        when(modelMapper.map(updated, ItemDTO.class)).thenReturn(new ItemDTO(1L, "Updated", "desc", "NEW", null, "email@test.com"));

        Optional<ItemDTO> result = service.updateItem(1L, updated);

        assertTrue(result.isPresent());
        assertEquals("Updated", result.get().getName());
    }

    @Test
    void testUpdateItem_NotFound() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        Optional<ItemDTO> result = service.updateItem(99L, new Item());

        assertTrue(result.isEmpty());
    }

    @Test
    void testDelete() {
        Item item = new Item(1L, "Test", "desc", "NEW", "email@test.com");

        when(repository.findById(1L)).thenReturn(Optional.of(item));

        service.deleteById(1L);

        verify(repository).deleteById(1L);
    }

    @Test
    void testSaveItemRepeatable() {
        ItemDTO dto = ItemDTO.builder()
                .name("Repeat")
                .description("Test")
                .status("NEW")
                .email("r@example.com")
                .build();

        ItemRepeatableDTO repeatableDTO = new ItemRepeatableDTO(dto, 3);

        when(modelMapper.map(eq(dto), eq(Item.class))).thenReturn(Item.builder()
                .name("Repeat")
                .description("Test")
                .email("r@example.com")
                .status("NEW")
                .build());

        service.saveItemRepeatable(repeatableDTO);

        verify(repository, times(3)).save(any(Item.class));
    }

    @Test
    void testExistsById() {
        when(repository.existsById(1L)).thenReturn(true);

        assertTrue(service.existsById(1L));
    }

    @Test
    void testProcessItemsAsync() throws Exception {
        List<Long> ids = new ArrayList<>();
        List<Item> items = new ArrayList<>();
        for (long i = 1; i <= 10; i++) {
            ids.add(i);
            Item item = Item.builder()
                    .id(i)
                    .name("Item" + i)
                    .description("Desc" + i)
                    .status("NEW")
                    .email("item" + i + "@example.com")
                    .build();
            items.add(item);

            when(repository.findById(i)).thenReturn(Optional.of(item));
            when(repository.save(any(Item.class))).thenAnswer(inv -> inv.getArgument(0));
            when(modelMapper.map(eq(item), eq(ItemDTO.class))).thenAnswer(inv -> {
                Item it = inv.getArgument(0);
                return ItemDTO.builder()
                        .id(it.getId())
                        .name(it.getName())
                        .description(it.getDescription())
                        .status("PROCESSED")
                        .email(it.getEmail())
                        .processedBy("MockThread")
                        .build();
            });
        }

        when(repository.findAllIds()).thenReturn(ids);
        CompletableFuture<List<ItemDTO>> future = service.processItemsAsync();
        List<ItemDTO> processed = future.get(); // Wait for async task to complete
        assertEquals(10, processed.size());
        for (ItemDTO dto : processed) {
            assertEquals("PROCESSED", dto.getStatus());
            assertTrue(dto.getProcessedBy() != null && !dto.getProcessedBy().isEmpty());
        }

        verify(repository, times(10)).save(any(Item.class));
    }
}
