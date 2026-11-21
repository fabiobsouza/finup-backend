package com.finup.common;

import java.time.YearMonth;

public final class Periods {

    private Periods() {}

    /** Formata como "mm/aaaa" para mensagens ao usuário. */
    public static String format(YearMonth ym) {
        return String.format("%02d/%d", ym.getMonthValue(), ym.getYear());
    }
}
