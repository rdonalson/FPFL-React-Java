package com.financialplanner.moduledisplaybc.model;

@lombok.Data
public class ItemDto {
    private Integer rollupKey;
    private Integer year;
    private Integer itemKey;
    private String occurrenceDate;
    private int fkItemType;
    private String itemType;
    private String period;
    private String name;
    private Double amount;
}
