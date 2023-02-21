package org.olac.reservation.utility.impl;

import org.olac.reservation.utility.DateTimeUtility;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class DateTimeUtilityImpl implements DateTimeUtility {

    @Override
    public Date getCurrentTime() {
        return new Date();
    }

}
