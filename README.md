
DOCUMENTATION:
->Layers:
1.Controller Layer:Item Controller
2.Service Layer: ItemService
3.Repository Layer: ItemRepository
4.Model Layer:DAO-Item, DTO-ItemDTO,ItemRepeatableDTO

DAO Layer:
JPA entity mapped to the database
fields: id,name,description,status,email(field validated with @Email)

DTO Layer:
1.ItemDTO:
adds processedBy field to indentify which thread processed the item
2.ItemRepeatableDTO:
wrapper for saving multiple copies of an item

ItemService:
converts between Item and ItemDTO using ModelMapper;
saveItemRepeatable()-saves the same item multiple times;
processItemsAsync()-uses a thread pool to asynchronously process and mark items as PROCESSED;divides item IDs into partitions based on thread count; uses CompletableFuture.runAsync() to parallelize updates; shared collection processedItems stores the result thread-safely
partitionList()-splits a list of item IDs into a specified number of partitions; each partition contains a sublist of IDs for parallel processing
