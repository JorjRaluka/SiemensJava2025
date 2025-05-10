package com.siemens.internship.service;

import com.siemens.internship.model.DAO.Item;
import com.siemens.internship.model.DTO.ItemDTO;
import com.siemens.internship.model.DTO.ItemRepeatableDTO;
import com.siemens.internship.repository.ItemRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.*;
import java.util.stream.Collectors;

@Service
public class ItemService {
    @Autowired
    private ItemRepository itemRepository;
    @Autowired
    private ModelMapper modelMapper;

    public ItemDTO convertToDTO(Item item) {
        return modelMapper.map(item, ItemDTO.class);
    }

    public Item convertToEntity(ItemDTO itemDTO) {
        return modelMapper.map(itemDTO, Item.class);
    }

    private static final int NUMBER_OF_THREADS = 10;
    private static ExecutorService executor = Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    private final List<ItemDTO> processedItems = new CopyOnWriteArrayList<ItemDTO>();
    private int processedCount = 0;


    public List<ItemDTO> findAll() {
        return itemRepository.findAll().stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    public Optional<ItemDTO> findById(Long id) {
        return itemRepository.findById(id)
                .map(item->convertToDTO(item));
    }
    public Optional<ItemDTO>updateItem(Long id,Item updatedItem){
        return itemRepository.findById(id).map(existsById ->{
            updatedItem.setId(id);
            Item savedItem=itemRepository.save(updatedItem);
            return convertToDTO(savedItem);
        });
    }
    public boolean existsById(Long id) {
        return itemRepository.existsById(id);
    }

    public ItemDTO save(ItemDTO itemDTO) {
        Item item=convertToEntity(itemDTO);
        Item savedItem=itemRepository.save(item);
        return convertToDTO(savedItem);
    }

    public ItemDTO deleteById(Long id) {
        Item item=itemRepository.findById(id).orElseThrow(()->new RuntimeException("Item not found"));
        itemRepository.deleteById(id);
        return convertToDTO(item);
    }



    private List<List<Long>> partitionList(List<Long> items, int numberOfPartitions) {
        List<List<Long>> partitions = new ArrayList<>();

        int totalItems = items.size();

        if (totalItems == 0 || numberOfPartitions <= 0) {
            return partitions;
        }

        int partitionSize = Math.max(1, totalItems / numberOfPartitions);

        for (int i = 0; i < totalItems; i += partitionSize) {
            int end = Math.min(i + partitionSize, totalItems);
            partitions.add(new ArrayList<>(items.subList(i, end)));
        }

        return partitions;
    }

    /**
     * Your Tasks
     * Identify all concurrency and asynchronous programming issues in the code
     * Fix the implementation to ensure:
     * All items are properly processed before the CompletableFuture completes
     * Thread safety for all shared state
     * Proper error handling and propagation
     * Efficient use of system resources
     * Correct use of Spring's @Async annotation
     * Add appropriate comments explaining your changes and why they fix the issues
     * Write a brief explanation of what was wrong with the original implementation
     * <p>
     * Hints
     * Consider how CompletableFuture composition can help coordinate multiple async operations
     * Think about appropriate thread-safe collections
     * Examine how errors are handled and propagated
     * Consider the interaction between Spring's @Async and CompletableFuture
     */
    @Async
    public CompletableFuture<List<ItemDTO>> processItemsAsync() {
        List<Long> itemIds = itemRepository.findAllIds();
        List<CompletableFuture<Void>> futures = new ArrayList<>();

            List<List<Long>> partitions = partitionList(itemIds, NUMBER_OF_THREADS);

        for (List<Long> partition : partitions) {
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                List<ItemDTO> localProcessed = new ArrayList<>();

                for (Long id : partition) {
                    try {
                        Thread.sleep(100);
                        Item item = itemRepository.findById(id).orElse(null);
                        if (item == null) continue;

                        synchronized (this) {
                            processedCount++;
                        }


                        item.setStatus("PROCESSED");
                        itemRepository.save(item);

                        ItemDTO itemDTO=convertToDTO(item);
                        itemDTO.setProcessedBy(Thread.currentThread().getName());

                        localProcessed.add(itemDTO);
                    } catch (InterruptedException e) {
                        System.out.println("Error: " + e.getMessage());
                    }
                }

                synchronized (processedItems) {
                    processedItems.addAll(localProcessed);
                }
            }, executor);

            futures.add(future);
        }

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        return CompletableFuture.completedFuture(processedItems);
    }


    public ItemDTO saveItemRepeatable(ItemRepeatableDTO itemRepeatable) {
        ItemDTO itemToSave = itemRepeatable.getItem();
        Item copy=convertToEntity(itemToSave);
        for(int i = 0; i < itemRepeatable.getCount(); i++){
            Item item = Item.builder()
                    .description(copy.getDescription())
                    .email(copy.getEmail())
                    .name(copy.getName())
                    .status(copy.getStatus())
                    .build();
            itemRepository.save(item);
        }



        return itemToSave;
    }
}

