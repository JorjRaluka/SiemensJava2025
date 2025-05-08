package com.siemens.internship;

import com.siemens.internship.model.DAO.Item;
import com.siemens.internship.repository.ItemRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
public class ItemRepositoryTest {

    @Autowired
    private ItemRepository repository;

    @Test
    public void testSaveAndFind() {
        Item item = new Item(null, "Test", "desc", "NEW", "email@test.com");
        item = repository.save(item);

        assertNotNull(item.getId());
        assertTrue(repository.existsById(item.getId()));

        List<Long> ids = repository.findAllIds();
        assertTrue(ids.contains(item.getId()));
    }
}
