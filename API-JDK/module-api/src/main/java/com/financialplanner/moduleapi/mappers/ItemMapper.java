package com.financialplanner.moduleapi.mappers;

import com.financialplanner.moduleapi.dtos.item.ItemRequest;
import com.financialplanner.moduleapi.dtos.item.ItemResponse;
import com.financialplanner.modulecommonbc.sanitizer.Sanitizer;
import com.financialplanner.moduleitemsbc.domain.repository.ItemTypeRepository;
import com.financialplanner.moduleitemsbc.domain.repository.TimePeriodRepository;
import com.financialplanner.moduleitemsbc.infrastructure.persistence.entity.Item;
import com.financialplanner.moduleitemsbc.infrastructure.persistence.entity.ItemType;
import com.financialplanner.moduleitemsbc.infrastructure.persistence.entity.TimePeriod;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * The {@code ItemMapper} class is responsible for mapping between {@code Item} domain objects
 * and their corresponding request and response objects. This class facilitates the transformation
 * of data for use in client-facing responses and persistence operations.
 * Responsibilities:
 * - Convert {@code Item} domain objects to {@code ItemResponse} objects.
 * - Convert {@code ItemRequest} objects to {@code Item} entities.
 * - Perform sanitization on objects to ensure clean and safe data.
 * Dependencies:
 * - {@code Sanitizer}: Used to sanitize input objects and strings.
 * - {@code ItemTypeRepository}: Provides access to {@code ItemType} entities.
 * - {@code TimePeriodRepository}: Provides access to {@code TimePeriod} entities.
 * Thread Safety:
 * - Instances of this class are thread-safe as long as the dependent components
 * ({@code Sanitizer}, {@code ItemTypeRepository}, {@code TimePeriodRepository}) are thread-safe.
 * Exceptions:
 * - This class may throw exceptions related to repository operations, such as entity not found
 * or database connectivity issues, during the conversion process.
 */
@Component
public class ItemMapper {

    private final Sanitizer sanitizer;
    private final ItemTypeRepository itemTypeRepository;
    private final TimePeriodRepository timePeriodRepository;

    public ItemMapper(Sanitizer sanitizer, ItemTypeRepository itemTypeRepository,
                      TimePeriodRepository timePeriodRepository) {
        this.sanitizer            = sanitizer;
        this.itemTypeRepository   = itemTypeRepository;
        this.timePeriodRepository = timePeriodRepository;
    }

    public ItemResponse toResponse(Item domain) {
        sanitizer.sanitize(domain);
        return new ItemResponse(domain.getId(), domain.getUserId(), domain.getName(), domain.getAmount(),
                                domain.getItemType(), domain.getTimePeriod(), domain.getBeginDate(),
                                domain.getEndDate(), domain.getWeeklyDow(), domain.getEveryOtherWeekDow(),
                                domain.getBiMonthlyDay1(), domain.getBiMonthlyDay2(), domain.getMonthlyDom(),
                                domain.getQuarterly1Month(), domain.getQuarterly1Day(), domain.getQuarterly2Month(),
                                domain.getQuarterly2Day(), domain.getQuarterly3Month(), domain.getQuarterly3Day(),
                                domain.getQuarterly4Month(), domain.getQuarterly4Day(), domain.getSemiAnnual1Month(),
                                domain.getSemiAnnual1Day(), domain.getSemiAnnual2Month(), domain.getSemiAnnual2Day(),
                                domain.getAnnualMoy(), domain.getAnnualDom(), domain.getDateRangeReq());
    }

    public Item toEntity(ItemRequest request) {
        sanitizer.sanitize(request);

        ItemType itemType = itemTypeRepository.getReferenceById(request.fkItemType()
                                                                       .longValue());
        Optional<TimePeriod> timePeriod = timePeriodRepository.findById(request.fkPeriod()
                                                                               .longValue());

        return new Item(null, request.userId(), request.name(), request.amount(), itemType, timePeriod.orElse(null),
                        request.beginDate(), request.endDate(), request.weeklyDow(), request.everyOtherWeekDow(),
                        request.biMonthlyDay1(), request.biMonthlyDay2(), request.monthlyDom(),
                        request.quarterly1Month(), request.quarterly1Day(), request.quarterly2Month(),
                        request.quarterly2Day(), request.quarterly3Month(), request.quarterly3Day(),
                        request.quarterly4Month(), request.quarterly4Day(), request.semiAnnual1Month(),
                        request.semiAnnual1Day(), request.semiAnnual2Month(), request.semiAnnual2Day(),
                        request.annualMoy(), request.annualDom(), request.dateRangeReq());
    }
}
