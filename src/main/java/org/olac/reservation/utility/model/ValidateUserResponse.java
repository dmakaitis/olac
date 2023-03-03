package org.olac.reservation.utility.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ValidateUserResponse {
    private String username;
    private String jwtToken;
}
