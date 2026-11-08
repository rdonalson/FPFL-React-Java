package com.financialplanner.moduledisplaybc.model;

import com.financialplanner.modulecommonbc.sanitizer.annotations.LenientText;

@lombok.Data
public class ItemDto {
    private Integer rollupKey;
    private Integer year;
    private Integer itemKey;
    private String occurrenceDate;
    private int fkItemType;
    /** Cards &amp; Loans entries carry "Card/Loan Payment" here, so it can't be held to strict text. */
    @LenientText
    private String itemType;
    /** Debt Paydown entries carry "25% of Paycheck" here, so it can't be held to strict text. */
    @LenientText
    private String period;
    /** Cards &amp; Loans entries carry "{account name} payment", and account names are lenient text. */
    @LenientText
    private String name;
    private Double amount;
}
