package com.financialplanner.moduleapi.controllers;

import com.financialplanner.moduleapi.dto.itemtype.ItemTypeRequest;
import com.financialplanner.moduleapi.dto.itemtype.ItemTypeResponse;
import com.financialplanner.moduleapi.dto.itemtype.UpdateItemTypeNameRequest;
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
 * Controller for managing item types through RESTful APIs.
 * This controller provides endpoints for creating, reading, updating, and deleting item types,
 * as well as for retrieving a list of all item types.
 *
 * Responsibilities of this controller include:
 * - Exposing an API for creating new item types via POST requests.
 * - Allowing retrieval of a specific item type by its identifier via GET requests.
 * - Enabling retrieval of a list of existing item types via GET requests.
 * - Supporting updates to existing item types via PUT requests.
 * - Facilitating deletion of item types by their unique identifier via DELETE requests.
 *
 * The controller internally uses:
 * - {@code ItemTypeService} for service-layer logic and interactions with the persistence layer.
 * - {@code ItemTypeDtoMapper} for mapping between DTOs and domain entities.
 * - {@code ApiResponseFactory} for formatting and constructing API responses.
 *
 * Request mappings:
 * - Base URL: {@code /item-types}
 *
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
    private final ItemTypeDtoMapper mapper;
    private final ApiResponseFactory responseFactory;

    public ItemTypeController(ItemTypeService service, ItemTypeDtoMapper mapper, ApiResponseFactory responseFactory) {
        this.service         = service;
        this.mapper          = mapper;
        this.responseFactory = responseFactory;
    }

    @GetMapping
    public List<ItemTypeResponse> list() {
        return service.list()
                      .stream()
                      .map(mapper::toResponse)
                      .toList();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ItemTypeResponse>> get(@PathVariable("id") Long id) {
        ItemTypeResponse item = mapper.toResponse(service.get(id));
        return ResponseEntity.ok(new ApiResponse<>(200, "ItemType retrieved successfully", item, null));
    }

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

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ItemTypeResponse>> update(@PathVariable("id") Long id,
                                                                @RequestBody UpdateItemTypeNameRequest request) {
        // Convert request → entity & sanitize input
        ItemType entity = mapper.toEntity(new ItemTypeRequest(id, request.name()));
        // Persist entity and then convert & sanitize the response
        ItemTypeResponse response = mapper.toResponse(service.update(entity));
        // Build sanitized ApiResponse using ResponseFactory
        ApiResponse<ItemTypeResponse> body = responseFactory.success(response, "ItemType updated successfully", null);
        // Return 200 Updated + sanitized body
        return ResponseEntity.ok(body);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable("id") Long id) {
        // Build ApiResponse using ResponseFactory
        service.delete(id);
        ApiResponse<Void> body = responseFactory.success("ItemType " + id + " deleted successfully", null);
        // Return 200 Deleted
        return ResponseEntity.ok(body);
    }
}

