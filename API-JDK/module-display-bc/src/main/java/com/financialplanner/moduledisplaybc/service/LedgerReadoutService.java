package com.financialplanner.moduledisplaybc.service;

import com.financialplanner.moduledisplaybc.accounts.AccountLedgerModels.AccountLedger;
import com.financialplanner.moduledisplaybc.model.LedgerDto;
import com.financialplanner.moduledisplaybc.model.LedgerRequest;
import com.financialplanner.moduledisplaybc.paydown.PaydownModels.DebtPaydown;

import java.util.List;

/**
 * Service interface for generating a ledger readout based on a user's financial data.
 * The ledger readout contains detailed breakdowns of financial transactions, including
 * credits, debits, and running totals, over a specified date range.
 * <p>
 * Money that credits send to tracked debts (Debt Paydown) appears on the ledger as extra
 * "Debt Paydown" debits, and money they send to cards and loans (Cards &amp; Loans) as extra
 * "Card/Loan Payment" debits.
 */
public interface LedgerReadoutService {

    /** Name shown as the item type of a ledger entry for money sent to a tracked debt. */
    String PAYDOWN_TYPE_NAME = "Debt Paydown";

    List<LedgerDto> buildLedgerReadout(LedgerRequest request);

    /** Each tracked debt's balance, payments and interest over the same window as the ledger. */
    List<DebtPaydown> buildPaydown(LedgerRequest request);

    /** Cards &amp; Loans: each card or loan's balance, purchases, payments and interest over the same window. */
    List<AccountLedger> buildAccounts(LedgerRequest request);
}
