package org.olac.reservation.utility.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReservationAuditEvent {

    private String reservationId;
    private Date timestamp;
    private String user;
    private String description;

}
