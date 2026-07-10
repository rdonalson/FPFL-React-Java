package com.financialplanner.moduledisplaybc.service;

import com.financialplanner.moduledisplaybc.model.LedgerDto;
import com.financialplanner.moduledisplaybc.model.LedgerRequest;

import java.util.List;

/**
 * Service interface for generating a ledger readout based on a user's financial data.
 * The ledger readout contains detailed breakdowns of financial transactions, including
 * credits, debits, and running totals, over a specified date range.
 */
public interface LedgerReadoutService {
    List<LedgerDto> buildLedgerReadout(LedgerRequest request);
}
