package com.donno.economy.economy;

import java.text.NumberFormat;
import java.util.Locale;

public final class CurrencyFormatter {

    private CurrencyFormatter() {
        // Utility class
    }

    public static String format(long amount) {
        return "NRs " + NumberFormat.getNumberInstance(Locale.US).format(amount);
    }
}