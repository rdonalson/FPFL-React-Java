package com.financialplanner.moduleapi.controllers;

import com.financialplanner.moduleapi.dto.timeperiod.TimePeriodRequest;
import com.financialplanner.moduleapi.dto.timeperiod.TimePeriodResponse;
import com.financialplanner.moduleapi.dto.timeperiod.UpdateTimePeriodNameRequest;
import com.financialplanner.moduleapi.mapper.TimePeriodDtoMapper;
import com.financialplanner.moduleapi.response.ApiResponse;
import com.financialplanner.moduleapi.response.ApiResponseFactory;
import com.financialplanner.moduleitemsbc.domain.service.TimePeriodService;
import com.financialplanner.moduleitemsbc.infrastructure.persistence.entity.TimePeriod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/time-periods")
public class TimePeriodController {

    private final TimePeriodService service;
    private final TimePeriodDtoMapper mapper;
    private final ApiResponseFactory responseFactory;

    /**
     * Constructs an instance of {@code TimePeriodController}.
     *
     * @param service         the service layer responsible for time period operations
     * @param mapper          the mapper for converting between DTOs and domain entities
     * @param responseFactory the factory for creating standardized API responses
     */
    public TimePeriodController(TimePeriodService service, TimePeriodDtoMapper mapper,
                                ApiResponseFactory responseFactory) {
        this.service         = service;
        this.mapper          = mapper;
        this.responseFactory = responseFactory;
    }

    /**
     * Retrieves a list of all time periods.
     *
     * @return ResponseEntity containing an ApiResponse with a list of TimePeriodResponse objects,
     * a success message, and a 200 OK HTTP status.
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<TimePeriodResponse>>> list() {
        // Retrieve all time period entities from the repository
        List<TimePeriod> items = service.list();
        // Map each entity to its corresponding response object
        List<TimePeriodResponse> responseList = items.stream()
                                                     .map(mapper::toResponse)
                                                     .toList();
        // Build sanitized ApiResponse using ResponseFactory
        ApiResponse<List<TimePeriodResponse>> response = responseFactory.success(responseList,
                                                                                 "TimePeriods fetched successfully");
        // Return 200 Success + sanitized body
        return ResponseEntity.ok(response);
    }

    /**
     * Retrieves the details of a specific time period by its unique identifier.
     *
     * @param id The unique identifier of the time period to be retrieved.
     *
     * @return A {@link ResponseEntity} containing an {@link ApiResponse} that encapsulates
     * the retrieved {@link TimePeriodResponse} instance and a success message,
     * or an appropriate error message if the time period is not found.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TimePeriodResponse>> get(@PathVariable("id") Long id) {
        // Retrieve time period entity from the repository
        TimePeriodResponse item = mapper.toResponse(service.get(id));
        // Build sanitized ApiResponse using ResponseFactory
        ApiResponse<TimePeriodResponse> body = responseFactory.success(item,
                                                                       "TimePeriod " + id + " retrieved successfully");
        // Return 200 Success + sanitized body
        return ResponseEntity.ok(body);
    }

    /**
     * Handles the creation of a new time period.
     * Converts the provided request payload into a domain entity, persists the entity,
     * and constructs an appropriate API response.
     *
     * @param request the request payload containing the details of the time period to be created
     *
     * @return a {@code ResponseEntity} containing an {@code ApiResponse} with the created time period's details,
     * a success message, and the resource's location
     */
    @SuppressWarnings({"QodanaXss", "JvmTaintAnalysis", "XSS"})
    @PostMapping
    public ResponseEntity<ApiResponse<TimePeriodResponse>> create(@RequestBody TimePeriodRequest request) {
        // Convert request → entity & sanitize input
        TimePeriod entity = mapper.toEntity(request);
        // Persist entity and then convert & sanitize the response
        TimePeriodResponse response = mapper.toResponse(service.create(entity));
        // Build Location header for 201 Created
        URI location = URI.create("/item-types/create/" + response.id());
        // Build sanitized ApiResponse using ResponseFactory
        ApiResponse<TimePeriodResponse> body = responseFactory.created(response, "TimePeriod created successfully",
                                                                       location.toString());
        // Return 201 Created + Location + sanitized body
        return ResponseEntity.created(location)
                             .body(body);
    }

    /**
     * Updates the name of an existing time period identified by the provided ID.
     *
     * @param id      The unique identifier of the time period to be updated.
     * @param request The request payload containing the new name for the time period.
     *
     * @return A ResponseEntity containing an ApiResponse with the updated time period
     * information and a success message.
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<TimePeriodResponse>> update(@PathVariable("id") Long id,
                                                                  @RequestBody UpdateTimePeriodNameRequest request) {
        // Convert request → entity & sanitize input
        TimePeriod entity = mapper.toEntity(new TimePeriodRequest(id, request.name()));
        // Persist entity and then convert & sanitize the response
        TimePeriodResponse response = mapper.toResponse(service.update(entity));
        // Build sanitized ApiResponse using ResponseFactory
        ApiResponse<TimePeriodResponse> body = responseFactory.success(response, "TimePeriod updated successfully");
        // Return 200 Updated + sanitized body
        return ResponseEntity.ok(body);
    }

    /**
     * Deletes an time period with the specified identifier.
     *
     * @param id the unique identifier of the time period to be deleted
     *
     * @return a ResponseEntity containing an ApiResponse with a message indicating successful deletion
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable("id") Long id) {
        // Build ApiResponse using ResponseFactory
        service.delete(id);
        // Build sanitized ApiResponse using ResponseFactory
        ApiResponse<Void> body = responseFactory.success("TimePeriod " + id + " deleted successfully");
        // Return 200 Deleted
        return ResponseEntity.ok(body);
    }
}


