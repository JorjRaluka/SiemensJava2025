
package com.siemens.internship.controller;

import com.siemens.internship.model.DAO.Item;
import com.siemens.internship.model.DTO.ItemDTO;
import com.siemens.internship.model.DTO.ItemRepeatableDTO;
import com.siemens.internship.service.ItemService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/api/items")
public class ItemController {

    @Autowired
    private ItemService itemService;

    @GetMapping
    public ResponseEntity<List<ItemDTO>> getAllItems() {
        return new ResponseEntity<>(itemService.findAll(), HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<ItemDTO> createItem(@Valid @RequestBody ItemDTO item, BindingResult result) {
        if (result.hasErrors()) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(itemService.save(item), HttpStatus.CREATED);
    }

    @PostMapping("/repeatable")
    public ResponseEntity<ItemDTO> createItemRepeatable(@Valid @RequestBody ItemRepeatableDTO item, BindingResult result) {
        if (result.hasErrors()) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }

        return new ResponseEntity<>(itemService.saveItemRepeatable(item), HttpStatus.CREATED);
    }


    @GetMapping("/{id}")
    public ResponseEntity<ItemDTO> getItemById(@PathVariable Long id) {
        return itemService.findById(id)
                .map(item -> new ResponseEntity<>(item, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ItemDTO> updateItem(@PathVariable Long id, @Valid @RequestBody Item item,BindingResult result) {
        if(result.hasErrors()){
            return new ResponseEntity<>(null,HttpStatus.BAD_REQUEST);
        }
        return itemService.updateItem(id, item)
                .map(value-> new ResponseEntity<>(value,HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));

    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ItemDTO> deleteItem(@PathVariable Long id) {
        if(!itemService.existsById(id)){
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        itemService.deleteById(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @GetMapping("/process")
    public ResponseEntity<List<ItemDTO>> processItems() throws ExecutionException, InterruptedException {
        return new ResponseEntity<>(itemService.processItemsAsync().get(), HttpStatus.OK);
    }
}