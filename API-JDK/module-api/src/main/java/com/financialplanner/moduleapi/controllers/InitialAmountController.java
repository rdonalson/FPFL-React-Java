package com.financialplanner.moduleapi.controllers;

import com.financialplanner.moduleapi.dtos.initialamount.InitialAmountRequest;
import com.financialplanner.moduleapi.dtos.initialamount.InitialAmountResponse;
import com.financialplanner.moduleapi.mappers.InitialAmountMapper;
import com.financialplanner.moduleapi.response.ApiResponse;
import com.financialplanner.moduleapi.response.ApiResponseFactory;
import com.financialplanner.modulecommonbc.exception.DuplicateItemException;
import com.financialplanner.modulecommonbc.exception.ItemNotFoundException;
import com.financialplanner.modulecommonbc.exception.RepositoryException;
import com.financialplanner.moduleitemsbc.domain.service.ItemService;
import com.financialplanner.moduleitemsbc.infrastructure.persistence.entity.Item;
import org.springframework.dao.DataAccessException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.Objects;
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
    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<List<InitialAmountResponse>>> getByUserAndType(@PathVariable("userId") UUID userId) {
        final long INITIAL_AMOUNT_ITEM_TYPE_ID = 3L;
        try {
            List<Item> items = service.findByUserIdAndItemTypeId(userId, INITIAL_AMOUNT_ITEM_TYPE_ID);
            if (items.isEmpty()) {
                throw new ItemNotFoundException("InitialAmount for UserId" + userId + " not found");
            }
            List<InitialAmountResponse> responseList = items.stream()
                                                            .map(mapper::toResponse)
                                                            .toList();
            ApiResponse<List<InitialAmountResponse>> body = responseFactory.success(responseList,
                                                                                    "InitialAmount retrieved " +
                                                                                    "successfully");
            return ResponseEntity.ok(body);

        } catch (DataAccessException ex) {
            throw new RepositoryException("Database failure while retrieving InitialAmount for user " + userId, ex);
        }
    }

    /**
     * Create a new InitialAmount (backed by Item).
     */
    @SuppressWarnings({"QodanaXss", "JvmTaintAnalysis", "XSS"})
    @PostMapping
    public ResponseEntity<ApiResponse<InitialAmountResponse>> create(@RequestBody InitialAmountRequest request) {
        Item entity = mapper.toEntity(request);
        final long INITIAL_AMOUNT_ITEM_TYPE_ID = 3L;

        try {
            List<Item> items = service.findByUserIdAndItemTypeId(entity.getUserId(), INITIAL_AMOUNT_ITEM_TYPE_ID);
            if (!items.isEmpty()) {
                throw new DuplicateItemException("User already has an initial amount: " + entity.getUserId(), null);
            }
            InitialAmountResponse response = mapper.toResponse(service.create(entity));
            URI location = URI.create("/initial-amount/" + response.id());
            ApiResponse<InitialAmountResponse> body = responseFactory.created(response,
                                                                              "InitialAmount created successfully",
                                                                              location.toString());
            return ResponseEntity.created(location)
                                 .body(body);

        } catch (DataAccessException ex) {
            throw new RepositoryException("Database failure while saving Item " + entity.getId(), ex);
        }
    }

    /**
     * Update an existing InitialAmount (backed by Item) by id.
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<InitialAmountResponse>> update(@PathVariable("id") Long id,
                                                                     @RequestBody InitialAmountRequest request) {
        final long INITIAL_AMOUNT_ITEM_TYPE_ID = 3L;
        try {
            List<Item> items = service.findByUserIdAndItemTypeId(request.userId(), INITIAL_AMOUNT_ITEM_TYPE_ID);
            if (!items.isEmpty() && !Objects.equals(id, items.getFirst().getId())) {
                throw new ItemNotFoundException("InitialAmount for UserId: " + request.userId() + " not found");
            }
            else if (items.isEmpty()) {
                throw new ItemNotFoundException("InitialAmount for for UserId: " + request.userId() + " not found");
            }
            Item entity = mapper.toEntity(request);
            InitialAmountResponse response = mapper.toResponse(service.update(id, entity));
            ApiResponse<InitialAmountResponse> body = responseFactory.success(response,
                                                                              "InitialAmount updated successfully");
            return ResponseEntity.ok(body);

        } catch (DataAccessException ex) {
            throw new RepositoryException("Database failure while updating Item " + id, ex);
        }
    }
}
