package org.olac.reservation.resource.model;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class Page<T> {

    private int pageNumber;
    private int itemsPerPage;
    private long totalItems;
    private List<T> data;
    private String sortBy;
    private boolean descending;

}
