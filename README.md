DOCUMENTATION:
->Layers:
1.Controller Layer:Item Controller
2.Service Layer: ItemService
3.Repository Layer: ItemRepository
4.Model Layer:DAO-Item, DTO-ItemDTO,ItemRepeatableDTO

Item:

| Field         | Type   | Description                               |
| ------------- | ------ | ----------------------------------------- |
| `id`          | Long   | Unique identifier (auto-generated)        |
| `name`        | String | Name of the item                          |
| `description` | String | Description of the item                   |
| `status`      | String | Status (e.g., "PROCESSED")                |
| `email`       | String | Email of the requester (validated format) |

ItemDTO:

| Field         | Type   | Description                                |
| ------------- | ------ | ------------------------------------------ |
| `id`          | Long   | Unique identifier                          |
| `name`        | String | Name of the item                           |
| `description` | String | Description of the item                    |
| `status`      | String | Status of the item                         |
| `processedBy` | String | Name of the thread that processed the item |
| `email`       | String | Email of the user (validated)              |

RepeatableDTO:

| Field   | Type      | Description                                |
| ------- | --------- | ------------------------------------------ |
| `item`  | `ItemDTO` | The item to be duplicated                  |
| `count` | `int`     | Number of times the item should be created |

Controller Documentation


GET /api/items

Method: GET
Path Variables: None
Description: Fetches all items.


GET /api/items/{id}

Method: GET
Path Variables: id (Long) – the ID of the item to retrieve
Description: Retrieves a specific item by its ID.


POST /api/items

Method: POST
Payload: ItemDTO 
Description: Creates a new item.


POST /api/items/repeatable

Method: POST
Payload: ItemRepeatableDTO 
Description: Creates multiple copies of an item based on the given count.


PUT /api/items/{id}

Method: PUT
Payload: Item (JSON)
Path Variables: id (Long) – ID of the item to update
Description: Updates an existing item by ID.


DELETE /api/items/{id}

Method: DELETE
Path Variables: id (Long) – ID of the item to delete
Description: Deletes the item by its ID.


GET /api/items/process

Method: GET
Payload: None
Path Variables: None
Description: Asynchronously processes items in parallel threads and marks them as "PROCESSED".


Service Documentation



List<ItemDTO> findAll()

Input: None
Output: List of all items as ItemDTO
Description: Fetches all items from the repository and maps them to DTOs.



Optional<ItemDTO> findById(Long id)

Input: id – Long
Output: Optional<ItemDTO>
Description: Finds an item by ID and converts it to a DTO.



ItemDTO save(ItemDTO itemDTO)

Input: itemDTO – DTO object to be saved
Output: ItemDTO – the saved item DTO
Description: Converts the DTO to an entity, saves it, and returns the saved DTO.



Optional<ItemDTO> updateItem(Long id, Item updatedItem)

Input: id – Long, updatedItem – Item entity with updated data
Output: Optional<ItemDTO>
Description: Updates an existing item by ID and returns the updated DTO if found.



boolean existsById(Long id)

Input: id – Long
Output: boolean
Description: Checks if an item exists by its ID.



ItemDTO deleteById(Long id)
Input: id – Long
Output: ItemDTO – the deleted item as DTO
Description: Deletes an item by ID and returns the deleted item.



CompletableFuture<List<ItemDTO>> processItemsAsync()

Input: None
Output: CompletableFuture<List<ItemDTO>> – list of processed items
Description: Asynchronously processes items using parallel threads, sets their status to "PROCESSED", and records which thread processed each item.


ItemDTO saveItemRepeatable(ItemRepeatableDTO itemRepeatable)
Input: ItemRepeatableDTO – contains one item and a count
Output: ItemDTO – the original item DTO
Description: Creates multiple copies of the given item and saves them to the repository.


List<List<Long>> partitionList(List<Long> items, int numberOfPartitions)

Input:
items – List<Long>: The list of item IDs to partition
numberOfPartitions – int: The number of partitions (usually equal to the number of threads)
Output: List<List<Long>> – A list of partitions, each containing a sublist of item IDs
Description:
Splits the provided list of item IDs into smaller sublists to distribute the workload among multiple threads. Ensures that each partition has a reasonable number of items and avoids empty or unevenly distributed partitions.

