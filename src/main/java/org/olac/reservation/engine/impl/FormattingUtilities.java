package org.olac.reservation.engine.impl;

import org.springframework.stereotype.Component;

import java.text.NumberFormat;

@Component("tutil")
public class FormattingUtilities {

    public String currency(double value) {
        return NumberFormat.getCurrencyInstance().format(value);
    }

}
