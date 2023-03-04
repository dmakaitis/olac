package org.olac.reservation.engine.impl;

import lombok.RequiredArgsConstructor;
import org.olac.reservation.utility.FormatUtility;
import org.springframework.stereotype.Component;

@Component("tutil")
@RequiredArgsConstructor
public class FormattingUtilities {

    private final FormatUtility formatUtility;

    public String currency(double value) {
        return formatUtility.formatCurrencty(value);
    }

}
