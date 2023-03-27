package org.olac.reservation.resource.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Page<T> {

    private int pageNumber;
    private int pageSize;
    private int itemsPerPage;
    private long totalItems;
    private List<T> data;
    private String sortBy;
    private boolean descending;
    private Object ext;

}
