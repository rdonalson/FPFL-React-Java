package com.financialplanner.moduledisplaybc.service;

import com.financialplanner.moduledisplaybc.model.LedgerRequest;

import com.financialplanner.moduleitemsbc.domain.service.ItemService;
import com.financialplanner.moduleitemsbc.infrastructure.persistence.entity.Item;
import com.financialplanner.moduledisplaybc.model.Ledger;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class LedgerReadoutServiceImpl implements LedgerReadoutService {

    private final ItemService itemService;

    public LedgerReadoutServiceImpl(ItemService itemService) {
        this.itemService = itemService;
    }

    @Override
    public List<Ledger> buildLedgerReadout(LedgerRequest request) {

        // 1. Load all user items from module-items-bc
        List<Item> userItems = itemService.findByUserId(request.userId());

        // 2. Build LedgerTable: one Ledger row per day

        // (Later: enrich ledgerTable using userItems)

        return buildLedgerTable(
            request.startDate(),
            request.endDate()
                               );
    }

    private List<Ledger> buildLedgerTable(LocalDate start, LocalDate end) {
        List<Ledger> table = new ArrayList<>();

        LocalDate cursor = start;
        int rollupKey = 1;

        while (!cursor.isAfter(end)) {
            Ledger row = new Ledger();
            row.setRollupKey(rollupKey++);
            row.setYear(cursor.getYear());
            row.setWDate(cursor);

            table.add(row);
            cursor = cursor.plusDays(1);
        }

        return table;
    }
}
