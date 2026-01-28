package com.financialplanner.moduleapi.mappers;

import com.financialplanner.moduleapi.dtos.timeperiod.TimePeriodRequest;
import com.financialplanner.moduleapi.dtos.timeperiod.TimePeriodResponse;
import com.financialplanner.modulecommonbc.sanitizer.Sanitizer;
import com.financialplanner.moduleitemsbc.infrastructure.persistence.entity.TimePeriod;
import org.springframework.stereotype.Component;

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
