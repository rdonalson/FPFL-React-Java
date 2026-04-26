package com.financialplanner.moduleapi.mappers;

import com.financialplanner.moduleapi.dtos.initialamount.InitialAmountRequest;
import com.financialplanner.moduleapi.dtos.initialamount.InitialAmountResponse;
import com.financialplanner.modulecommonbc.sanitizer.Sanitizer;
import com.financialplanner.moduleitemsbc.domain.repository.ItemTypeRepository;
import com.financialplanner.moduleitemsbc.infrastructure.persistence.entity.Item;
import com.financialplanner.moduleitemsbc.infrastructure.persistence.entity.ItemType;
import org.springframework.stereotype.Component;

/**
 * Maps between InitialAmount domain entity and API DTOs.
 * Follows the same sanitization and repository-lookup pattern used by ItemMapper.
 */
@Component
public class InitialAmountMapper {

    private final Sanitizer sanitizer;
    private final ItemTypeRepository itemTypeRepository;

    public InitialAmountMapper(Sanitizer sanitizer, ItemTypeRepository itemTypeRepository) {
        this.sanitizer          = sanitizer;
        this.itemTypeRepository = itemTypeRepository;
    }

    public InitialAmountResponse toResponse(Item domain) {
        sanitizer.sanitize(domain);
        return new InitialAmountResponse(domain.getId(), domain.getUserId(), domain.getName(), domain.getAmount(),
                                         domain.getItemType(), domain.getBeginDate());
    }

    public Item toEntity(InitialAmountRequest request) {
        sanitizer.sanitize(request);

        ItemType itemType = itemTypeRepository.getReferenceById(request.fkItemType()
                                                                       .longValue());
        return new Item(null, request.userId(), request.name(), request.amount(), itemType, request.beginDate());
    }
}
