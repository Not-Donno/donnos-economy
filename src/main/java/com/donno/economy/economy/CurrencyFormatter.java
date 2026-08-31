package com.donno.economy.economy;

import java.text.NumberFormat;
import java.util.Locale;

/** Formats money stored in tenths of one Nepali rupee. */
public final class CurrencyFormatter {
    private CurrencyFormatter() {}

    public static String format(long tenths) {
        NumberFormat format = NumberFormat.getNumberInstance(Locale.US);
        format.setMinimumFractionDigits(0);
        format.setMaximumFractionDigits(1);
        return "NRs " + format.format(tenths / 10.0);
    }
}
