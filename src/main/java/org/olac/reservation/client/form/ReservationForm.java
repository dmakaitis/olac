package org.olac.reservation.client.form;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Data
public class ReservationForm {

    @NotBlank
    private String lastName;
    @NotBlank
    private String firstName;
    @NotBlank
    private String email;
    @NotBlank
    private String phone;

    @Valid
    private List<TicketTypeCount> ticketTypeCounts;

}
