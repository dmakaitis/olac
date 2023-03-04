package org.olac.reservation.utility.impl;

import org.olac.reservation.utility.FormatUtility;
import org.springframework.stereotype.Service;

import java.text.NumberFormat;

@Service
public class FormatUtilityImpl implements FormatUtility {

    private final NumberFormat currencyInstance = NumberFormat.getCurrencyInstance();

    @Override
    public String formatCurrencty(double value) {
        return currencyInstance.format(value);
    }

}
