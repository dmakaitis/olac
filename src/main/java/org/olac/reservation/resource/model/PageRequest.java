package org.olac.reservation.resource.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PageRequest {
    private int page;
    private int itemsPerPage;
    private String sortBy;
    private boolean descending;
}
