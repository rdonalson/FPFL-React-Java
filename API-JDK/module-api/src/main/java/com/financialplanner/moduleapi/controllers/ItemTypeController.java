package com.financialplanner.moduleapi.controllers;

import com.financialplanner.moduleapi.dto.itemtype.ItemTypeRequest;
import com.financialplanner.moduleapi.dto.itemtype.ItemTypeResponse;
import com.financialplanner.moduleapi.mapper.ItemTypeDtoMapper;
import com.financialplanner.moduleapi.response.ApiResponse;
import com.financialplanner.moduleapi.response.ApiResponseFactory;
import com.financialplanner.moduleitemsbc.domain.service.ItemTypeService;
import com.financialplanner.moduleitemsbc.infrastructure.persistence.entity.ItemType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
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
     * entities (e.g., {@code ItemType}).
     * <p>
     * This field is typically utilized in the controller layer to delegate
     * mapping responsibilities to a separate component, thus ensuring clean
     * and maintainable separation of concerns. It enables seamless conversion
     * when handling requests and responses for item types.
     */
    private final ItemTypeDtoMapper mapper;
    private final ApiResponseFactory responseFactory;

    /**
     * Constructs a new instance of the ItemTypeController class.
     * This controller is responsible for handling HTTP requests related to item types,
     * such as retrieving, creating, updating, and deleting item types.
     * It uses the provided service and mapper for delegating business logic and object mapping, respectively.
     *
     * @param service the ItemTypeService instance used to manage business logic; must not be null
     * @param mapper  the ItemTypeDtoMapper instance used to convert between DTOs and entities; must not be null
     */
    public ItemTypeController(ItemTypeService service, ItemTypeDtoMapper mapper, ApiResponseFactory responseFactory) {
        this.service         = service;
        this.mapper          = mapper;
        this.responseFactory = responseFactory;
    }

    /**
     * Retrieves a list of all item types.
     *
     * @return a list of ItemType objects representing all item types currently available
     */
    @GetMapping
    public List<ItemTypeResponse> list() {
        return service.list()
                      .stream()
                      .map(mapper::toResponse)
                      .toList();
    }

    /**
     * Retrieves an ItemType based on the given unique identifier.
     * Delegates the retrieval operation to the service layer.
     *
     * @param id the unique identifier of the ItemType to retrieve; must not be null
     * @return the ItemType associated with the given ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ItemTypeResponse>> get(@PathVariable("id") Long id) {
        ItemTypeResponse item = mapper.toResponse(service.get(id));
        return ResponseEntity.ok(new ApiResponse<>(200, "ItemType retrieved successfully", item, null));
    }

    /**
     * Creates a new ItemType entity based on the provided request payload.
     * Converts the request DTO to a domain entity, sanitizes the input data,
     * persists the entity, and then maps and sanitizes the response. A Location
     * header is included in the response to identify the created resource.
     * QODANA-FP: XSS.201.Created.Response
     * Reason: All user-controlled fields sanitized at inbound DTO, DB entity, and ApiResponse wrapper.
     * Evidence: See /docs/security/false-positives/xss-201-created.md
     *
     * @param request the ItemTypeRequest object containing the necessary details
     *                for creating a new ItemType; must not be null.
     * @return a ResponseEntity containing an ApiResponse with the details of the
     * created ItemType and the associated HTTP status code 201 (Created).
     */
    @SuppressWarnings({"QodanaXss", "JvmTaintAnalysis", "XSS"})
    @PostMapping
    public ResponseEntity<ApiResponse<ItemTypeResponse>> create(@RequestBody ItemTypeRequest request) {
        // Convert request → entity & sanitize input
        ItemType entity = mapper.toEntity(request);
        // Persist entity and then convert & sanitize the response
        ItemTypeResponse response = mapper.toResponse(service.create(entity));
        // Build Location header for 201 Created
        URI location = URI.create("/item-types/create/" + response.name());
        // Build sanitized ApiResponse using ResponseFactory
        ApiResponse<ItemTypeResponse> body = responseFactory.created(response, "ItemType created successfully",
                                                                     location.toString());
        // Return 201 Created + Location + sanitized body
        return ResponseEntity.created(location)
                             .body(body);
    }


    /**
     * Updates the details of an existing item type based on the provided ID and name.
     * Creates a new {@code ItemType} instance, sets the ID and name, and calls the service layer
     * to perform the update operation. The updated {@code ItemType} is returned as the result.
     *
     * @param id   the unique identifier of the item type to be updated; must not be null
     * @param name the new name of the item type to be updated; must not be null
     * @return the updated {@code ItemType} instance reflecting the changes applied
     */
    //    @PutMapping("/{id}/{name}")
    //    public ItemTypeResponse update(@PathVariable("id") Long id, @PathVariable("name") String name) {
    //        ItemType entity = new ItemType();
    //        entity.setId(id);
    //        entity.setName(name);
    //        return service.update(entity);
    //    }

    /**
     * Deletes an item type by its unique identifier.
     * This method delegates the deletion operation to the associated service layer,
     * which interacts with the persistence layer to remove the entity from the data store.
     *
     * @param id the unique identifier of the item type to be deleted; must not be null.
     */
    @DeleteMapping("/{id}")
    public void delete(@PathVariable("id") Long id) {
        service.delete(id);
    }
}

