package org.olac.reservation.client.form;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TicketTypeCount {

    @NotBlank
    private String typeCode;
    private String description;
    private String costPerTicket;
    @PositiveOrZero
    private int count;

}
