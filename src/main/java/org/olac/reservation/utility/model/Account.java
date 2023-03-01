package org.olac.reservation.utility.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Account {

    private Long id;
    private String username;
    private String email;
    @Builder.Default
    private boolean enabled = true;
    @Builder.Default
    private boolean admin = false;

}
