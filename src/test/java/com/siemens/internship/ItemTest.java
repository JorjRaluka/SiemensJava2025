package com.siemens.internship;


import com.siemens.internship.model.DAO.Item;
import com.siemens.internship.model.DTO.ItemDTO;
import com.siemens.internship.model.DTO.ItemRepeatableDTO;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ItemTest {

    @Test
    public void testItemEntity() {
        Item item = new Item();
        item.setId(1L);
        item.setName("Test");
        item.setDescription("desc");
        item.setStatus("NEW");
        item.setEmail("test@example.com");

        assertEquals(1L, item.getId());
        assertEquals("Test", item.getName());
        assertEquals("desc", item.getDescription());
        assertEquals("NEW", item.getStatus());
        assertEquals("test@example.com", item.getEmail());
    }

    @Test
    public void testItemDTO() {
        ItemDTO dto = ItemDTO.builder()
                .id(1L)
                .name("DTO")
                .description("desc")
                .status("NEW")
                .processedBy("Thread-1")
                .email("dto@example.com")
                .build();

        assertEquals("DTO", dto.getName());
        assertEquals("Thread-1", dto.getProcessedBy());
    }

    @Test
    public void testItemRepeatableDTO() {
        ItemDTO itemDTO = new ItemDTO();
        itemDTO.setName("Repeat");

        ItemRepeatableDTO repeatable = new ItemRepeatableDTO(itemDTO, 3);

        assertEquals(3, repeatable.getCount());
        assertEquals("Repeat", repeatable.getItem().getName());
    }
}
