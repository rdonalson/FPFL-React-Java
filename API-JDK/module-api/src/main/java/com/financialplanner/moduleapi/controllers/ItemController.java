package com.financialplanner.moduleapi.controllers;

import com.financialplanner.moduleapi.dtos.item.ItemRequest;
import com.financialplanner.moduleapi.dtos.item.ItemResponse;
import com.financialplanner.moduleapi.dtos.timeperiod.TimePeriodRequest;
import com.financialplanner.moduleapi.dtos.timeperiod.TimePeriodResponse;
import com.financialplanner.moduleapi.dtos.timeperiod.UpdateTimePeriodNameRequest;
import com.financialplanner.moduleapi.mappers.ItemMapper;
import com.financialplanner.moduleapi.response.ApiResponse;
import com.financialplanner.moduleapi.response.ApiResponseFactory;
import com.financialplanner.moduleitemsbc.domain.service.ItemService;
import com.financialplanner.moduleitemsbc.infrastructure.persistence.entity.Item;
import com.financialplanner.moduleitemsbc.infrastructure.persistence.entity.TimePeriod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/items")
public class ItemController {

    private final ItemService service;
    private final ItemMapper mapper;
    private final ApiResponseFactory responseFactory;

    public ItemController(ItemService service, ItemMapper mapper,
                          ApiResponseFactory responseFactory) {
        this.service         = service;
        this.mapper          = mapper;
        this.responseFactory = responseFactory;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ItemResponse>>> list() {
        // Retrieve all time period entities from the repository
        List<Item> items = service.list();
        // Map each entity to its corresponding response object
        List<ItemResponse> responseList = items.stream()
                                                     .map(mapper::toResponse)
                                                     .toList();
        // Build sanitized ApiResponse using ResponseFactory
        ApiResponse<List<ItemResponse>> response = responseFactory.success(responseList,
                                                                           "Items fetched successfully");
        // Return 200 Success + sanitized body
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ItemResponse>> get(@PathVariable("id") Long id) {
        // Retrieve time period entity from the repository
        ItemResponse item = mapper.toResponse(service.get(id));
        // Build sanitized ApiResponse using ResponseFactory
        ApiResponse<ItemResponse> body = responseFactory.success(item,
                                                                       "Item " + id + " retrieved successfully");
        // Return 200 Success + sanitized body
        return ResponseEntity.ok(body);
    }

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


