package com.financialplanner.moduledisplaybc.service;

import com.financialplanner.moduledisplaybc.model.Ledger;
import com.financialplanner.moduledisplaybc.model.LedgerRequest;

import java.util.List;

public interface LedgerReadoutService {
    List<Ledger> buildLedgerReadout(LedgerRequest request);
}
