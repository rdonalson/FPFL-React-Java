package com.financialplanner.moduleapi.controllers;

import com.financialplanner.moduleapi.dtos.timeperiod.TimePeriodRequest;
import com.financialplanner.moduleapi.dtos.timeperiod.TimePeriodResponse;
import com.financialplanner.moduleapi.dtos.timeperiod.UpdateTimePeriodNameRequest;
import com.financialplanner.moduleapi.mappers.TimePeriodMapper;
import com.financialplanner.moduleapi.response.ApiResponse;
import com.financialplanner.moduleapi.response.ApiResponseFactory;
import com.financialplanner.moduleitemsbc.domain.service.TimePeriodService;
import com.financialplanner.moduleitemsbc.infrastructure.persistence.entity.TimePeriod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

/**
 * Controller class for managing time period resources in the system.
 * Provides endpoints for CRUD operations on time periods, acting as an interface
 * between the client and the service layer. Each endpoint returns standardized
 * API responses to ensure a consistent interaction format.
 */
@RestController
@RequestMapping("/time-periods")
public class TimePeriodController {

    private final TimePeriodService service;
    private final TimePeriodMapper mapper;
    private final ApiResponseFactory responseFactory;

    /**
     * Constructs a {@code TimePeriodController} instance.
     *
     * @param service         The {@code TimePeriodService} instance used for managing the business logic of time
     *                        periods.
     * @param mapper          The {@code TimePeriodMapper} instance used to map between entities and DTOs.
     * @param responseFactory The {@code ApiResponseFactory} instance used to build standardized API responses.
     */
    public TimePeriodController(TimePeriodService service, TimePeriodMapper mapper,
                                ApiResponseFactory responseFactory) {
        this.service         = service;
        this.mapper          = mapper;
        this.responseFactory = responseFactory;
    }

    /**
     * Retrieves a list of all available time periods.
     * Fetches time period entities from the service layer, maps them to their
     * corresponding response objects, and returns them in a standardized API response.
     *
     * @return ResponseEntity containing an ApiResponse object with a list of TimePeriodResponse objects.
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
     * Handles HTTP GET requests to retrieve a specific time period by its unique identifier.
     *
     * @param id the unique identifier of the time period to retrieve; must not be null
     *
     * @return a {@code ResponseEntity} containing an {@code ApiResponse} with the detailed response
     * object of the time period and a success message if found
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
     * Creates a new TimePeriod entity based on the specified request,
     * persists it, and returns a sanitized response containing the
     * created TimePeriod details along with a Location header for the created resource.
     *
     * @param request a {@code TimePeriodRequest} object containing the details needed to create a new TimePeriod.
     *                The request should include all the necessary fields for creating the entity.
     *
     * @return a {@code ResponseEntity} wrapping an {@code ApiResponse} that contains the created
     * {@code TimePeriodResponse} object, a success message, and the Location where the new
     * resource can be accessed.
     */
    @SuppressWarnings({"QodanaXss", "JvmTaintAnalysis", "XSS"})
    @PostMapping
    public ResponseEntity<ApiResponse<TimePeriodResponse>> create(@RequestBody TimePeriodRequest request) {
        // Convert request → entity & sanitize input
        TimePeriod entity = mapper.toEntity(request);
        // Persist entity and then convert & sanitize the response
        TimePeriodResponse response = mapper.toResponse(service.create(entity));
        // Build Location header for 201 Created
        URI location = URI.create("/time-periods/create/" + response.id());
        // Build sanitized ApiResponse using ResponseFactory
        ApiResponse<TimePeriodResponse> body = responseFactory.created(response, "TimePeriod created successfully",
                                                                       location.toString());
        // Return 201 Created + Location + sanitized body
        return ResponseEntity.created(location)
                             .body(body);
    }

    /**
     * Updates an existing time period identified by its unique ID with the new details provided
     * in the request body.
     *
     * @param id      the unique identifier of the time period to be updated; it must not be {@code null}.
     * @param request an instance of {@link UpdateTimePeriodNameRequest} containing the updated
     *                name for the time period; it must not be {@code null}.
     *
     * @return a {@link ResponseEntity} containing an {@link ApiResponse} with the updated
     * {@link TimePeriodResponse} and a success message; it returns HTTP 200 OK if the update
     * is successful.
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
     * Deletes a TimePeriod entity by its unique identifier.
     * Sends a standardized API response indicating the success of the operation.
     *
     * @param id the unique identifier of the TimePeriod to be deleted
     *
     * @return a ResponseEntity containing an {@code ApiResponse<Void>} which includes a success message
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


