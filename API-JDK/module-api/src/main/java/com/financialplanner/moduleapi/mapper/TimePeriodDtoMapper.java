package com.financialplanner.moduleapi.mapper;

import com.financialplanner.moduleapi.dto.timeperiod.TimePeriodRequest;
import com.financialplanner.moduleapi.dto.timeperiod.TimePeriodResponse;
import com.financialplanner.modulecommonbc.sanitizer.Sanitizer;
import com.financialplanner.moduleitemsbc.infrastructure.persistence.entity.TimePeriod;
import org.springframework.stereotype.Component;

@Component
public class TimePeriodDtoMapper {

    private final Sanitizer sanitizer;

    public TimePeriodDtoMapper(Sanitizer sanitizer) {
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
