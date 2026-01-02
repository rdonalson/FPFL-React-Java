package com.financialplanner.moduleapi.controllers;

import com.financialplanner.moduleapi.dto.ItemTypeDto;
import com.financialplanner.moduleapi.logging.ErrorLogger;
import com.financialplanner.moduleapi.mapper.ItemTypeDtoMapper;
import com.financialplanner.moduleapi.response.ApiResponse;
import com.financialplanner.moduleitemsbc.domain.entity.ItemTypeEntity;
import com.financialplanner.moduleitemsbc.domain.model.ItemType;
import com.financialplanner.moduleitemsbc.domain.service.ItemTypeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for managing item types.
 * Handles HTTP requests for CRUD (Create, Read, Update, Delete) operations on item type entities.
 * Delegates business logic to the {@code ItemTypeService} and object mapping responsibilities
 * to the {@code ItemTypeDtoMapper} for cleaner separation of concerns and enhanced maintainability.
 * Mapped to the endpoint "/item-types".
 */
@RestController
@RequestMapping("/item-types")
public class ItemTypeController {

    /**
     * Service class responsible for managing operations related to ItemType entities.
     * Used for performing create, read, update, and delete operations on item types
     * through interactions with the underlying persistence layer.
     */
    private final ItemTypeService service;
    /**
     * A mapper used to translate between {@code ItemTypeDto} objects, which are
     * used for data transfer between application layers, and corresponding domain
     * entities (e.g., {@code ItemTypeEntity}).
     * ---
     * This field is typically utilized in the controller layer to delegate
     * mapping responsibilities to a separate component, thus ensuring clean
     * and maintainable separation of concerns. It enables seamless conversion
     * when handling requests and responses for item types.
     */
    private final ItemTypeDtoMapper mapper;

    /**
     * Constructs a new instance of the ItemTypeController class.
     * This controller is responsible for handling HTTP requests related to item types,
     * such as retrieving, creating, updating, and deleting item types.
     * It uses the provided service and mapper for delegating business logic and object mapping, respectively.
     * ---
     * @param service the ItemTypeService instance used to manage business logic; must not be null
     * @param mapper the ItemTypeDtoMapper instance used to convert between DTOs and entities; must not be null
     */
    public ItemTypeController(ItemTypeService service, ItemTypeDtoMapper mapper) {
        this.service = service;
        this.mapper = mapper;
    }

    /**
     * Retrieves a list of all item types.
     * ---
     * @return a list of ItemType objects representing all item types currently available
     */
    @GetMapping
    public List<ItemType> list() {
        return service.list();
    }

    /**
     * Retrieves an ItemType based on the given unique identifier.
     * Delegates the retrieval operation to the service layer.
     * ---
     * @param id the unique identifier of the ItemType to retrieve; must not be null
     * @return the ItemType associated with the given ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ItemType>> get(@PathVariable("id") Long id) {
        ItemType item = service.get(id);
        return ResponseEntity.ok(
            new ApiResponse<>(
                200,
                "ItemType retrieved successfully",
                item,
                null
            )
        );
    }

    /**
     * Creates a new item type based on the provided request data.
     *
     * @param request the data transfer object containing the details of the item type to be created
     * @return the created item type
     */
    @PostMapping
    public ResponseEntity<ApiResponse<ItemType>> create(@RequestBody ItemTypeDto request) {
        ItemTypeEntity entity = mapper.toEntity(request);
        ItemType item = service.create(entity);
        return ResponseEntity.ok(
            new ApiResponse<>(
                201,
                "ItemType created successfully",
                item,
                null
            )
        );
    }

    /**
     * Updates the details of an existing item type based on the provided ID and name.
     * Creates a new {@code ItemTypeEntity} instance, sets the ID and name, and calls the service layer
     * to perform the update operation. The updated {@code ItemType} is returned as the result.
     * ---
     * @param id   the unique identifier of the item type to be updated; must not be null
     * @param name the new name of the item type to be updated; must not be null
     * @return     the updated {@code ItemType} instance reflecting the changes applied
     */
    @PutMapping("/{id}/{name}")
    public ItemType update(@PathVariable("id") Long id, @PathVariable("name") String name) {
        ItemTypeEntity entity = new ItemTypeEntity();
        entity.setId(id);
        entity.setName(name);
        return service.update(entity);
    }

    /**
     * Deletes an item type by its unique identifier.
     * This method delegates the deletion operation to the associated service layer,
     * which interacts with the persistence layer to remove the entity from the data store.
     * ---
     * @param id the unique identifier of the item type to be deleted; must not be null.
     */
    @DeleteMapping("/{id}")
    public void delete(@PathVariable("id") Long id) {
        service.delete(id);
    }
}

