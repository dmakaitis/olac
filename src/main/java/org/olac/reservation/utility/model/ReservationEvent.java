package org.olac.reservation.utility.model;

import lombok.Builder;
import lombok.Data;

import java.util.Date;

@Data
@Builder
public class ReservationEvent {

    private String reservationId;
    private Date timestamp;
    private String user;
    private String description;

}
