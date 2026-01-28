package com.financialplanner.moduleapi.mappers;

import com.financialplanner.moduleapi.dtos.timeperiod.TimePeriodRequest;
import com.financialplanner.moduleapi.dtos.timeperiod.TimePeriodResponse;
import com.financialplanner.modulecommonbc.sanitizer.Sanitizer;
import com.financialplanner.moduleitemsbc.infrastructure.persistence.entity.TimePeriod;
import org.springframework.stereotype.Component;

/**
 * Maps between TimePeriod entity and its corresponding request and response DTOs.
 * This class is responsible for converting TimePeriod entities to TimePeriodResponse DTOs
 * and vice versa. It applies data sanitization before performing the conversion
 * to ensure input and output data are valid and meet specified constraints.
 * Responsibilities:
 * - Convert a TimePeriod entity to a TimePeriodResponse DTO.
 * - Convert a TimePeriodRequest DTO to a TimePeriod entity.
 * - Sanitize input objects prior to processing using the Sanitizer component.
 * Dependencies:
 * - Sanitizer: Used to sanitize data for both entities and DTOs to enforce integrity and security.
 * Thread Safety:
 * - Instances of this class are thread-safe as long as the provided Sanitizer implementation is thread-safe.
 * Design Notes:
 * - Relies on the Sanitizer component to ensure that input and output data conform to business rules.
 * - Focuses solely on mapping logic between domain and DTO representations, with no additional business logic.
 */
@Component
public class TimePeriodMapper {

    private final Sanitizer sanitizer;

    public TimePeriodMapper(Sanitizer sanitizer) {
        this.sanitizer = sanitizer;
    }

    public TimePeriodResponse toResponse(TimePeriod domain) {
        sanitizer.sanitize(domain);
        return new TimePeriodResponse(domain.getId(), domain.getName());
    }

    public TimePeriod toEntity(TimePeriodRequest request) {
        sanitizer.sanitize(request);
        return new TimePeriod(request.id(), request.name());
    }
}
