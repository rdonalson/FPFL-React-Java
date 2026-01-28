package com.financialplanner.moduleapi.controllers;

import com.financialplanner.moduleapi.dtos.itemtype.ItemTypeRequest;
import com.financialplanner.moduleapi.dtos.itemtype.ItemTypeResponse;
import com.financialplanner.moduleapi.dtos.itemtype.UpdateItemTypeNameRequest;
import com.financialplanner.moduleapi.mappers.ItemTypeMapper;
import com.financialplanner.moduleapi.response.ApiResponse;
import com.financialplanner.moduleapi.response.ApiResponseFactory;
import com.financialplanner.moduleitemsbc.domain.service.ItemTypeService;
import com.financialplanner.moduleitemsbc.infrastructure.persistence.entity.ItemType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

/**
 * The {@code ItemTypeController} class is a REST controller that manages CRUD operations
 * for item types. This controller provides endpoints to list, retrieve, create, update,
 * and delete item types, adhering to RESTful principles.
 * The controller interacts with the service layer to process business logic and employs a
 * mapper to convert between domain entities and Data Transfer Objects (DTOs). API responses
 * are standardized using an {@code ApiResponseFactory}.
 */
@RestController
@RequestMapping("/item-types")
public class ItemTypeController {

    private final ItemTypeService service;
    private final ItemTypeMapper mapper;
    private final ApiResponseFactory responseFactory;

    /**
     * Constructs an instance of {@code ItemTypeController}.
     *
     * @param service         the service layer dependency for managing item type operations
     * @param mapper          the mapper for converting between domain entities and DTOs
     * @param responseFactory the factory for constructing standardized API responses
     */
    public ItemTypeController(ItemTypeService service, ItemTypeMapper mapper, ApiResponseFactory responseFactory) {
        this.service         = service;
        this.mapper          = mapper;
        this.responseFactory = responseFactory;
    }

    /**
     * Retrieves a list of all item types.
     * The method fetches all available item type entities, maps them to response objects,
     * and returns a formatted API response with a success message.
     *
     * @return ResponseEntity containing an ApiResponse object with a list of ItemTypeResponse
     * and a success message.
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<ItemTypeResponse>>> list() {
        // Retrieve all item type entities from the repository
        List<ItemType> items = service.list();
        // Map each entity to its corresponding response object
        List<ItemTypeResponse> responseList = items.stream()
                                                   .map(mapper::toResponse)
                                                   .toList();
        // Build sanitized ApiResponse using ResponseFactory
        ApiResponse<List<ItemTypeResponse>> response = responseFactory.success(responseList,
                                                                               "ItemTypes fetched successfully");
        // Return 200 Success + sanitized body
        return ResponseEntity.ok(response);
    }

    /**
     * Retrieves a specific item type by its identifier.
     * This method takes the unique identifier of the item type as input,
     * retrieves the corresponding item type from the service layer,
     * and returns it wrapped in a standardized response body.
     *
     * @param id the unique identifier of the item type to be retrieved
     *
     * @return a {@code ResponseEntity} containing an {@code ApiResponse} with the
     * details of the requested item type, along with a success message
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ItemTypeResponse>> get(@PathVariable("id") Long id) {
        // Retrieve item type entity from the repository
        ItemTypeResponse item = mapper.toResponse(service.get(id));
        // Build sanitized ApiResponse using ResponseFactory
        ApiResponse<ItemTypeResponse> body = responseFactory.success(item,
                                                                     "ItemType " + id + " retrieved successfully");
        // Return 200 Success + sanitized body
        return ResponseEntity.ok(body);
    }

    /**
     * Creates a new item type based on the provided request data.
     * This method accepts an {@code ItemTypeRequest} payload, converts it into a domain entity,
     * persists the entity, and returns a response containing the created item type's details.
     * A Location header is included in the response indicating the URI of the newly created resource.
     *
     * @param request the {@code ItemTypeRequest} containing details of the item type to be created
     *
     * @return a {@code ResponseEntity} containing the created {@code ItemTypeResponse} wrapped in an {@code
     * ApiResponse},
     * along with a Location header indicating the URI of the newly created item type
     */
    @SuppressWarnings({"QodanaXss", "JvmTaintAnalysis", "XSS"})
    @PostMapping
    public ResponseEntity<ApiResponse<ItemTypeResponse>> create(@RequestBody ItemTypeRequest request) {
        // Convert request → entity & sanitize input
        ItemType entity = mapper.toEntity(request);
        // Persist entity and then convert & sanitize the response
        ItemTypeResponse response = mapper.toResponse(service.create(entity));
        // Build Location header for 201 Created
        URI location = URI.create("/item-types/create/" + response.id());
        // Build sanitized ApiResponse using ResponseFactory
        ApiResponse<ItemTypeResponse> body = responseFactory.created(response, "ItemType created successfully",
                                                                     location.toString());
        // Return 201 Created + Location + sanitized body
        return ResponseEntity.created(location)
                             .body(body);
    }

    /**
     * Updates an existing item type with the specified ID using the provided update request.
     *
     * @param id      the unique identifier of the item type to be updated
     * @param request the update request containing the new name for the item type
     *
     * @return a {@link ResponseEntity} containing an {@link ApiResponse} with the updated item type details
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ItemTypeResponse>> update(@PathVariable("id") Long id,
                                                                @RequestBody UpdateItemTypeNameRequest request) {
        // Convert request → entity & sanitize input
        ItemType entity = mapper.toEntity(new ItemTypeRequest(id, request.name()));
        // Persist entity and then convert & sanitize the response
        ItemTypeResponse response = mapper.toResponse(service.update(entity));
        // Build sanitized ApiResponse using ResponseFactory
        ApiResponse<ItemTypeResponse> body = responseFactory.success(response, "ItemType updated successfully");
        // Return 200 Updated + sanitized body
        return ResponseEntity.ok(body);
    }

    /**
     * Deletes an item type identified by the provided ID.
     * This method performs the deletion of an item type from the system and returns
     * an appropriate response indicating the success of the operation.
     *
     * @param id The unique identifier of the item type to be deleted.
     *
     * @return A {@code ResponseEntity} containing an {@code ApiResponse} object with
     * a success message confirming the deletion and an HTTP 200 status code.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable("id") Long id) {
        // Build ApiResponse using ResponseFactory
        service.delete(id);
        // Build sanitized ApiResponse using ResponseFactory
        ApiResponse<Void> body = responseFactory.success("ItemType " + id + " deleted successfully");
        // Return 200 Deleted
        return ResponseEntity.ok(body);
    }
}

