package com.financialplanner.moduleapi.controllers;

import com.financialplanner.moduleapi.dtos.item.ItemRequest;
import com.financialplanner.moduleapi.dtos.item.ItemResponse;
import com.financialplanner.moduleapi.mappers.ItemMapper;
import com.financialplanner.moduleapi.response.ApiResponse;
import com.financialplanner.moduleapi.response.ApiResponseFactory;
import com.financialplanner.moduleitemsbc.domain.service.ItemService;
import com.financialplanner.moduleitemsbc.infrastructure.persistence.entity.Item;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

/**
 * Controller responsible for handling API requests related to item management.
 * Provides endpoints for CRUD operations and managing item resources.
 */
@RestController
@RequestMapping("/items")
public class ItemController {

    private final ItemService service;
    private final ItemMapper mapper;
    private final ApiResponseFactory responseFactory;

    /**
     * Constructs a new ItemController to handle API requests for managing items.
     *
     * @param service         the service layer responsible for managing item entities
     * @param mapper          the mapper to convert between entities and DTOs
     * @param responseFactory the factory to build API responses
     */
    public ItemController(ItemService service, ItemMapper mapper, ApiResponseFactory responseFactory) {
        this.service         = service;
        this.mapper          = mapper;
        this.responseFactory = responseFactory;
    }

    /**
     * Retrieves a list of items.
     *
     * @return a ResponseEntity containing an ApiResponse with a list of ItemResponse objects.
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<ItemResponse>>> list() {
        // Retrieve all time period entities from the repository
        List<Item> items = service.list();
        // Map each entity to its corresponding response object
        List<ItemResponse> responseList = items.stream()
                                               .map(mapper::toResponse)
                                               .toList();
        // Build sanitized ApiResponse using ResponseFactory
        ApiResponse<List<ItemResponse>> response = responseFactory.success(responseList, "Items fetched successfully");
        // Return 200 Success + sanitized body
        return ResponseEntity.ok(response);
    }

    /**
     * Retrieves an item by its unique identifier.
     *
     * @param id the unique identifier of the item to be retrieved
     *
     * @return a {@link ResponseEntity} containing an {@link ApiResponse}
     * that wraps the item's details if retrieval is successful
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ItemResponse>> get(@PathVariable("id") Long id) {
        // Retrieve time period entity from the repository
        ItemResponse item = mapper.toResponse(service.get(id));
        // Build sanitized ApiResponse using ResponseFactory
        ApiResponse<ItemResponse> body = responseFactory.success(item, "Item " + id + " retrieved successfully");
        // Return 200 Success + sanitized body
        return ResponseEntity.ok(body);
    }

    /**
     * Creates a new item based on the provided request data.
     * Converts the request data into an entity, persists it,
     * and returns the created item with a location header.
     *
     * @param request the request payload containing item creation details
     *
     * @return ResponseEntity containing the created item in the response body,
     * location of the created item in the headers, and HTTP status 201 (Created)
     */
    @SuppressWarnings({"QodanaXss", "JvmTaintAnalysis", "XSS"})
    @PostMapping
    public ResponseEntity<ApiResponse<ItemResponse>> create(@RequestBody ItemRequest request) {
        // Convert request → entity & sanitize input
        Item entity = mapper.toEntity(request);
        // Persist entity and then convert & sanitize the response
        ItemResponse response = mapper.toResponse(service.create(entity));
        // Build Location header for 201 Created
        URI location = URI.create("/items/create/" + response.id());
        // Build sanitized ApiResponse using ResponseFactory
        ApiResponse<ItemResponse> body = responseFactory.created(response, "Item created successfully",
                                                                 location.toString());
        // Return 201 Created + Location + sanitized body
        return ResponseEntity.created(location)
                             .body(body);
    }

    /**
     * Updates an existing item with the specified ID based on the provided request body.
     *
     * @param id      The ID of the item to be updated.
     * @param request The request body containing the updated details of the item.
     *
     * @return A {@link ResponseEntity} containing an {@link ApiResponse} with the updated item details
     * as {@link ItemResponse}, and a success message.
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ItemResponse>> update(@PathVariable("id") Long id,
                                                            @RequestBody ItemRequest request) {
        // Convert request → entity & sanitize input
        Item entity = mapper.toEntity(request);
        // Persist entity and then convert & sanitize the response
        ItemResponse response = mapper.toResponse(service.update(id, entity));
        // Build sanitized ApiResponse using ResponseFactory
        ApiResponse<ItemResponse> body = responseFactory.success(response, "Item updated successfully");
        // Return 200 Updated + sanitized body
        return ResponseEntity.ok(body);
    }

    /**
     * Deletes an item identified by the given ID.
     * Upon successful deletion, a response confirming the deletion is returned.
     *
     * @param id the unique identifier of the item to be deleted
     *
     * @return a ResponseEntity containing an ApiResponse with a success message and no data payload
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable("id") Long id) {
        // Build ApiResponse using ResponseFactory
        service.delete(id);
        // Build sanitized ApiResponse using ResponseFactory
        ApiResponse<Void> body = responseFactory.success("Item " + id + " deleted successfully");
        // Return 200 Deleted
        return ResponseEntity.ok(body);
    }
}


