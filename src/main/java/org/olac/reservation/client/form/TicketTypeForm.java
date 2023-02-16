package org.olac.reservation.client.form;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

@Data
public class TicketTypeForm {

    private String code;

    @NotBlank
    private String description;
    @PositiveOrZero
    @Digits(integer = 4, fraction = 2)
    private double cost;

}
