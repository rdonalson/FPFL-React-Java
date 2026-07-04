package com.financialplanner.moduledisplaybc.model;

import java.math.BigDecimal;
import java.time.LocalDate;

@lombok.Data
public class Ledger {
    private int rollupKey;
    private int year;
    private LocalDate wDate;
    private BigDecimal creditSummary = BigDecimal.ZERO;
    private BigDecimal debitSummary = BigDecimal.ZERO;
    private BigDecimal net = BigDecimal.ZERO;
    private BigDecimal runningTotal = BigDecimal.ZERO;
    private LocalDate occurrenceDate;
    private int fkItemType;
    private String itemType;
    private String periodName;
    private String name;
    private BigDecimal amount = BigDecimal.ZERO;
}
