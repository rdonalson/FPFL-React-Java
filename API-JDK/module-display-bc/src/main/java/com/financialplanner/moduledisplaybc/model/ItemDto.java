package com.financialplanner.moduledisplaybc.model;

import com.financialplanner.modulecommonbc.sanitizer.annotations.LenientText;

@lombok.Data
public class ItemDto {
    private Integer rollupKey;
    private Integer year;
    private Integer itemKey;
    private String occurrenceDate;
    private int fkItemType;
    private String itemType;
    /** Debt Paydown entries carry "25% of Paycheck" here, so it can't be held to strict text. */
    @LenientText
    private String period;
    private String name;
    private Double amount;
}
