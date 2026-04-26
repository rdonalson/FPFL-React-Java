package com.financialplanner.moduleapi.controllers;

import com.financialplanner.moduleapi.dtos.initialamount.InitialAmountRequest;
import com.financialplanner.moduleapi.dtos.initialamount.InitialAmountResponse;
import com.financialplanner.moduleapi.mappers.InitialAmountMapper;
import com.financialplanner.moduleapi.response.ApiResponse;
import com.financialplanner.moduleapi.response.ApiResponseFactory;
import com.financialplanner.moduleitemsbc.domain.service.ItemService;
import com.financialplanner.moduleitemsbc.infrastructure.persistence.entity.Item;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;

/**
 * REST controller for InitialAmount operations.
 * Uses the Item domain/entity for persistence (mapped from InitialAmountRequest).
 * Supports Get (by id and by userId+itemType), Create and Update. Delete is intentionally omitted.
 */
@RestController
@RequestMapping("/initial-amount")
public class InitialAmountController {

    private final ItemService service;
    private final InitialAmountMapper mapper;
    private final ApiResponseFactory responseFactory;

    public InitialAmountController(ItemService service, InitialAmountMapper mapper,
                                   ApiResponseFactory responseFactory) {
        this.service         = service;
        this.mapper          = mapper;
        this.responseFactory = responseFactory;
    }

    /**
     * Retrieve InitialAmounts for a given user and item type.
     */
    @GetMapping("/{userId}/{itemType}")
    public ResponseEntity<ApiResponse<List<InitialAmountResponse>>> getByUserAndType(
        @PathVariable("userId") UUID userId, @PathVariable("itemType") Long itemType) {
        List<Item> items = service.findByUserIdAndItemTypeId(userId, itemType);
        List<InitialAmountResponse> responseList = items.stream()
                                                        .map(mapper::toResponse)
                                                        .toList();
        ApiResponse<List<InitialAmountResponse>> body = responseFactory.success(responseList,
                                                                                "InitialAmounts retrieved " +
                                                                                "successfully");
        return ResponseEntity.ok(body);
    }

    /**
     * Create a new InitialAmount (backed by Item).
     */
    @SuppressWarnings({"QodanaXss", "JvmTaintAnalysis", "XSS"})
    @PostMapping
    public ResponseEntity<ApiResponse<InitialAmountResponse>> create(@RequestBody InitialAmountRequest request) {
        Item entity = mapper.toEntity(request);
        InitialAmountResponse response = mapper.toResponse(service.create(entity));
        URI location = URI.create("/initial-amount/" + response.id());
        ApiResponse<InitialAmountResponse> body = responseFactory.created(response,
                                                                          "InitialAmount created successfully",
                                                                          location.toString());
        return ResponseEntity.created(location)
                             .body(body);
    }

    /**
     * Update an existing InitialAmount (backed by Item) by id.
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<InitialAmountResponse>> update(@PathVariable("id") Long id,
                                                                     @RequestBody InitialAmountRequest request) {
        Item entity = mapper.toEntity(request);
        InitialAmountResponse response = mapper.toResponse(service.update(id, entity));
        ApiResponse<InitialAmountResponse> body = responseFactory.success(response,
                                                                          "InitialAmount updated successfully");
        return ResponseEntity.ok(body);
    }
}
