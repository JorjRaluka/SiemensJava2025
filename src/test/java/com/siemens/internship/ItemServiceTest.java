package com.siemens.internship;

import com.siemens.internship.model.DAO.Item;
import com.siemens.internship.model.DTO.ItemDTO;
import com.siemens.internship.model.DTO.ItemRepeatableDTO;
import com.siemens.internship.repository.ItemRepository;
import com.siemens.internship.service.ItemService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.mockito.*;

import java.util.*;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ItemServiceTest {

    @InjectMocks
    private ItemService service;

    @Mock
    private ItemRepository repository;

    @Mock
    private ModelMapper modelMapper;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testFindAll() {
        List<Item> items = List.of(new Item(1L, "Test", "desc", "NEW", "email@test.com"));
        when(repository.findAll()).thenReturn(items);

        List<Item> result = service.findAll();
        assertEquals(1, result.size());
    }

    @Test
    void testSave() {
        ItemDTO dto = new ItemDTO();
        dto.setName("Test");
        Item entity = new Item();
        when(modelMapper.map(dto, Item.class)).thenReturn(entity);
        when(repository.save(entity)).thenReturn(entity);
        when(modelMapper.map(entity, ItemDTO.class)).thenReturn(dto);

        ItemDTO saved = service.save(dto);
        assertEquals("Test", saved.getName());
    }

    @Test
    void testUpdate() {
        Long id = 1L;
        Item updated = new Item();
        when(repository.findById(id)).thenReturn(Optional.of(updated));
        when(repository.save(any())).thenReturn(updated);

        Optional<Item> result = service.updateItem(id, updated);
        assertTrue(result.isPresent());
    }

    @Test
    void testDelete() {
        service.deleteById(1L);
        verify(repository, times(1)).deleteById(1L);
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

        when(modelMapper.map(any(ItemDTO.class), eq(Item.class))).thenAnswer(inv -> {
            ItemDTO d = inv.getArgument(0);
            return Item.builder().name(d.getName()).description(d.getDescription()).email(d.getEmail()).status(d.getStatus()).build();
        });

        service.saveItemRepeatable(repeatableDTO);

        verify(repository, times(3)).save(any(Item.class));
    }
    @Test
    void testUpdateItem_NotFound() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        Optional<Item> result = service.updateItem(99L, new Item());
        assertTrue(result.isEmpty());
    }
    @Test
    void testExistsById() {
        when(repository.existsById(1L)).thenReturn(true);
        assertTrue(service.existsById(1L));
    }

    @Test
    void testProcessItemsAsync()throws Exception {
        List<Long> ids = new ArrayList<>();
        for (long i = 1; i <= 10; i++) {
            ids.add(i);
        }

        List<Item> mockItems = new ArrayList<>();
        for (Long id : ids) {
            Item item = Item.builder()
                    .id(id)
                    .name("Item" + id)
                    .description("Desc" + id)
                    .status("NEW")
                    .email("item" + id + "@example.com")
                    .build();
            mockItems.add(item);
            when(repository.findById(id)).thenReturn(Optional.of(item));
        }
        when(repository.findAllIds()).thenReturn(ids);
        when(repository.save(any(Item.class))).thenAnswer(inv -> inv.getArgument(0));
        for (Item item : mockItems) {
            ItemDTO dto = ItemDTO.builder()
                    .id(item.getId())
                    .name(item.getName())
                    .description(item.getDescription())
                    .status("PROCESSED")
                    .email(item.getEmail())
                    .processedBy("MockThread")
                    .build();
            when(modelMapper.map(eq(item), eq(ItemDTO.class))).thenReturn(dto);
        }
        CompletableFuture<List<Item>> future = service.processItemsAsync();
        List<Item> processed = future.get();
        assertEquals(10, processed.size());
        for (Item item : processed) {
            assertEquals("PROCESSED", item.getStatus());
        }
        verify(repository, times(10)).save(any(Item.class));
    }




}
