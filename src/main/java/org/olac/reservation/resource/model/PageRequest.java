package org.olac.reservation.resource.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageRequest {
    private int page;
    private int itemsPerPage;
    private String sortBy;
    private boolean descending;
}
