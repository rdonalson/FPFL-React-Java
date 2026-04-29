package com.financialplanner.moduleapi.mappers;

import com.financialplanner.moduleapi.dtos.initialamount.InitialAmountRequest;
import com.financialplanner.moduleapi.dtos.initialamount.InitialAmountResponse;
import com.financialplanner.modulecommonbc.sanitizer.Sanitizer;
import com.financialplanner.moduleitemsbc.domain.repository.ItemTypeRepository;
import com.financialplanner.moduleitemsbc.infrastructure.persistence.entity.Item;
import com.financialplanner.moduleitemsbc.infrastructure.persistence.entity.ItemType;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Date;

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

        ItemType itemType = itemTypeRepository.getReferenceById(3L);
        return new Item(null, request.userId(), "IA", request.amount(), itemType, LocalDate.now());
    }
}
