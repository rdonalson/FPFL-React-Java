package com.financialplanner.modulecommonbc.sanitizer;

import com.financialplanner.modulecommonbc.exception.SanitizationException;
import com.financialplanner.modulecommonbc.sanitizer.annotations.LenientText;
import com.financialplanner.modulecommonbc.sanitizer.annotations.StrictText;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The sanitizer every request and response goes through: strict text by default, lenient where annotated,
 * URIs checked separately, applied recursively through POJOs, records, collections, maps and arrays.
 */
class SanitizerImplTest {

    private final SanitizerImpl sanitizer = new SanitizerImpl();

    // ---- test shapes --------------------------------------------------------------------------------

    static class Pojo {
        String plain;
        @LenientText String lenient;
        @StrictText String strict;
        String link;
        Integer count = 3;
        LocalDate date = LocalDate.of(2026, 10, 24);
        Pojo child;
        static String ignoredStatic = "<static>";
        transient String ignoredTransient = "<transient>";
    }

    record Row(String name, @LenientText String note) {}

    static class Holder {
        List<Row> rows = new ArrayList<>();
        Map<String, String> map = new HashMap<>();
        String[] array;
        Row single;
    }

    record Skipped(String text) implements NoSanitize {}

    // ---- strings ------------------------------------------------------------------------------------

    @Test
    void strictStringsAllowLettersDigitsAndSimplePunctuation() {
        assertEquals("Rick O'Donalson-Jr. 2", sanitizer.sanitizeString("Rick O'Donalson-Jr. 2"));
        assertEquals("Café Ünïcode_ok", sanitizer.sanitizeString("Café Ünïcode_ok"));
        assertNull(sanitizer.sanitizeString(null));
    }

    @Test
    void strictStringsRejectMarkupAndSymbols() {
        for (String bad : List.of("<script>alert(1)</script>", "a;b", "50%", "x & y", "{\"json\":1}", "a=b")) {
            assertThrows(SanitizationException.class, () -> sanitizer.sanitizeString(bad), bad);
        }
    }

    @Test
    void normalizesWhitespaceAndInvisibleCharacters() {
        assertEquals("Chase Visa", sanitizer.sanitizeString("  Chase​   Visa﻿ "));
        assertEquals("ab", sanitizer.sanitizeString("a\u0007b"));
    }

    // ---- POJO fields --------------------------------------------------------------------------------

    @Test
    void pojoFieldsAreStrictUnlessMarkedLenient() {
        Pojo p = new Pojo();
        p.plain = "  Rent  ";
        p.lenient = "25%  of   Paycheck";
        p.strict = "Monthly";
        p.link = "/items/5?x=1";

        sanitizer.sanitize(p);

        assertEquals("Rent", p.plain);
        assertEquals("25% of Paycheck", p.lenient);
        assertEquals("Monthly", p.strict);
        assertEquals("/items/5?x=1", p.link);
        assertEquals(3, p.count);
        assertEquals(LocalDate.of(2026, 10, 24), p.date);
        assertEquals("<static>", Pojo.ignoredStatic);
        assertEquals("<transient>", p.ignoredTransient);
    }

    @Test
    void aBadStrictFieldFailsTheWholeObject() {
        Pojo p = new Pojo();
        p.plain = "50% off";
        assertThrows(SanitizationException.class, () -> sanitizer.sanitize(p));

        Pojo q = new Pojo();
        q.strict = "<b>";
        assertThrows(SanitizationException.class, () -> sanitizer.sanitize(q));
    }

    @Test
    void badUrisAreRejected() {
        Pojo p = new Pojo();
        p.link = "/items/<5>";
        assertThrows(SanitizationException.class, () -> sanitizer.sanitize(p));
    }

    @Test
    void nestedObjectsAndCyclesAreHandled() {
        Pojo parent = new Pojo();
        Pojo child = new Pojo();
        parent.child = child;
        child.child = parent; // cycle
        child.plain = "  Child  ";

        sanitizer.sanitize(parent);

        assertEquals("Child", child.plain);
    }

    // ---- records and containers ---------------------------------------------------------------------

    @Test
    void recordsInsideCollectionsAreReplacedWithCleanCopies() {
        Holder h = new Holder();
        h.rows.add(new Row("  Rent ", "25%   of Paycheck"));
        h.rows.add(new Row("Gym", "ok"));

        sanitizer.sanitize(h);

        assertEquals(new Row("Rent", "25% of Paycheck"), h.rows.get(0));
        assertEquals(new Row("Gym", "ok"), h.rows.get(1));
    }

    @Test
    void recordFieldsAreReplacedAndStrictComponentsChecked() {
        Holder h = new Holder();
        h.single = new Row(" Paycheck ", "x");
        sanitizer.sanitize(h);
        assertEquals("Paycheck", h.single.name());

        Holder bad = new Holder();
        bad.rows.add(new Row("a & b", "fine"));
        assertThrows(SanitizationException.class, () -> sanitizer.sanitize(bad));
    }

    @Test
    void mapsAndArraysAreCleaned() {
        Holder h = new Holder();
        h.map.put(" key ", " value ");
        h.array = new String[] {" one ", "two"};

        sanitizer.sanitize(h);

        assertEquals(Map.of("key", "value"), h.map);
        assertArrayEquals(new String[] {"one", "two"}, h.array);
    }

    @Test
    void unmodifiableCollectionsAreLeftAloneRatherThanFailing() {
        Holder h = new Holder();
        h.rows = List.of(new Row(" Rent ", "x"));
        assertDoesNotThrow(() -> sanitizer.sanitize(h));
    }

    @Test
    void noSanitizeTypesAndNullsAreSkipped() {
        assertDoesNotThrow(() -> sanitizer.sanitize(new Skipped("<anything goes>")));
        assertDoesNotThrow(() -> sanitizer.sanitize(null));
    }
}
