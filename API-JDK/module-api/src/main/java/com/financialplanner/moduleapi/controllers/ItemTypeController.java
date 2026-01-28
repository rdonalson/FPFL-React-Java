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
 * Controller for managing item types through RESTful APIs.
 * This controller provides endpoints for creating, reading, updating, and deleting item types,
 * as well as for retrieving a list of all item types.
 * Responsibilities of this controller include:
 * - Exposing an API for creating new item types via POST requests.
 * - Allowing retrieval of a specific item type by its identifier via GET requests.
 * - Enabling retrieval of a list of existing item types via GET requests.
 * - Supporting updates to existing item types via PUT requests.
 * - Facilitating deletion of item types by their unique identifier via DELETE requests.
 * The controller internally uses:
 * - {@code ItemTypeService} for service-layer logic and interactions with the persistence layer.
 * - {@code ItemTypeMapper} for mapping between DTOs and domain entities.
 * - {@code ApiResponseFactory} for formatting and constructing API responses.
 * Request mappings:
 * - Base URL: {@code /item-types}
 * Endpoint Details:
 * - {@code GET /item-types}: Retrieves a list of all item types.
 * - {@code GET /item-types/{id}}: Retrieves details of a specific item type based on its ID.
 * - {@code POST /item-types}: Creates a new item type based on the provided request payload.
 * - {@code PUT /item-types/{id}}: Updates the name of an existing item type.
 * - {@code DELETE /item-types/{id}}: Deletes an item type based on its ID.
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
     * @param service         the service layer responsible for item type operations
     * @param mapper          the mapper for converting between DTOs and domain entities
     * @param responseFactory the factory for creating standardized API responses
     */
    public ItemTypeController(ItemTypeService service, ItemTypeMapper mapper, ApiResponseFactory responseFactory) {
        this.service         = service;
        this.mapper          = mapper;
        this.responseFactory = responseFactory;
    }

    /**
     * Retrieves a list of all item types.
     *
     * @return ResponseEntity containing an ApiResponse with a list of ItemTypeResponse objects,
     * a success message, and a 200 OK HTTP status.
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
     * Retrieves the details of a specific item type by its unique identifier.
     *
     * @param id The unique identifier of the item type to be retrieved.
     *
     * @return A {@link ResponseEntity} containing an {@link ApiResponse} that encapsulates
     * the retrieved {@link ItemTypeResponse} instance and a success message,
     * or an appropriate error message if the item type is not found.
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
     * Handles the creation of a new item type.
     * Converts the provided request payload into a domain entity, persists the entity,
     * and constructs an appropriate API response.
     *
     * @param request the request payload containing the details of the item type to be created
     *
     * @return a {@code ResponseEntity} containing an {@code ApiResponse} with the created item type's details,
     * a success message, and the resource's location
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
     * Updates the name of an existing item type identified by the provided ID.
     *
     * @param id      The unique identifier of the item type to be updated.
     * @param request The request payload containing the new name for the item type.
     *
     * @return A ResponseEntity containing an ApiResponse with the updated item type
     * information and a success message.
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
     * Deletes an item type with the specified identifier.
     *
     * @param id the unique identifier of the item type to be deleted
     *
     * @return a ResponseEntity containing an ApiResponse with a message indicating successful deletion
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

