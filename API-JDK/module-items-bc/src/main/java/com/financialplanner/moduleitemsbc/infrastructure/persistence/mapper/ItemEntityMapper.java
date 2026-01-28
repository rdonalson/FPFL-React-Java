package com.financialplanner.moduleitemsbc.infrastructure.persistence.mapper;

import com.financialplanner.moduleitemsbc.infrastructure.persistence.entity.Item;
import org.springframework.stereotype.Component;

/**
 * A mapper class responsible for mapping and copying properties from an existing
 * Item entity to a new Item instance, while allowing modification of the entity's ID.
 * This class is typically used to create a new instance of an Item by reusing
 * existing property values from another Item object.
 */
@Component
public class ItemEntityMapper {
    public Item copyEntity(Long id, Item entity) {
        return new Item(id, entity.getUserId(), entity.getName(), entity.getAmount(), entity.getItemType(),
                        entity.getTimePeriod(), entity.getBeginDate(), entity.getEndDate(), entity.getWeeklyDow(),
                        entity.getEveryOtherWeekDow(), entity.getBiMonthlyDay1(), entity.getBiMonthlyDay2(),
                        entity.getMonthlyDom(), entity.getQuarterly1Month(), entity.getQuarterly1Day(),
                        entity.getQuarterly2Month(), entity.getQuarterly2Day(), entity.getQuarterly3Month(),
                        entity.getQuarterly3Day(), entity.getQuarterly4Month(), entity.getQuarterly4Day(),
                        entity.getSemiAnnual1Month(), entity.getSemiAnnual1Day(), entity.getSemiAnnual2Month(),
                        entity.getSemiAnnual2Day(), entity.getAnnualMoy(), entity.getAnnualDom(),
                        entity.getDateRangeReq());
    }
}
