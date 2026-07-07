package com.financialplanner.moduledisplaybc.model;

import java.math.BigDecimal;
import java.time.LocalDate;

@lombok.Data
public class Ledger {
    private int rollupKey;
    private int year;
    private LocalDate wDate;
    private double creditSummary = 0.0;
    private double debitSummary = 0.0;
    private double net = 0.0;
    private double runningTotal = 0.0;
    private LocalDate occurrenceDate;
    private int fkItemType;
    private String itemType;
    private String periodName;
    private String name;
    private double amount = 0.0;
}
