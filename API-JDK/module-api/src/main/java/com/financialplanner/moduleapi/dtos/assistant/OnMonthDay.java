package com.financialplanner.moduleapi.dtos.assistant;

/**
 * A month-and-day pair with no year, used by the quarterly, semi-annual and annual
 * recurrences.
 * <p>
 * Deliberately a local record rather than {@link java.time.MonthDay}: JDK value types
 * schema-generate inconsistently across model providers, and a two-field record produces a
 * predictable {@code {"month": 3, "day": 15}} shape.
 *
 * @param month month of the year, 1-12
 * @param day   day of the month, 1-31
 */
public record OnMonthDay(Integer month, Integer day) {}
