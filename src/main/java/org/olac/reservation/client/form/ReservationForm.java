package org.olac.reservation.client.form;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.olac.reservation.client.validation.PhoneNumber;

import java.util.List;

@Data
public class ReservationForm {

    @NotBlank(message = "You must provide a first name")
    private String lastName;
    @NotBlank(message = "You must provide a last name")
    private String firstName;
    @NotBlank(message = "You must provide a valid email address")
    @Email(message = "You must provide a valid email address")
    private String email;
    @PhoneNumber
    private String phone;

    @Valid
    private List<TicketTypeCount> ticketTypeCounts;

    private String total;

}
