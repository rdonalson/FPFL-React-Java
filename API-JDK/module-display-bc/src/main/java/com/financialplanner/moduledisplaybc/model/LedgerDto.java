package com.financialplanner.moduledisplaybc.model;

import lombok.Data;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
public class LedgerDto {
    private int rollupKey;
    private int year;
    private LocalDate wDate;
    private double creditSummary;
    private double debitSummary;
    private double net;
    private double runningTotal;
    private List<ItemDto> items = new ArrayList<>();
}
