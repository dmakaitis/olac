package org.olac.reservation.resource.jpa.specification;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import org.olac.reservation.resource.jpa.entity.ReservationEntity;
import org.olac.reservation.resource.jpa.entity.ReservationEntity_;
import org.springframework.data.jpa.domain.Specification;

public class ReservationSpecification {

    private ReservationSpecification() {
        // Private constructor
    }

    public static Specification<ReservationEntity> withReservationId(String reservationId) {
        return (Root<ReservationEntity> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) ->
                criteriaBuilder.equal(root.get(ReservationEntity_.reservationId), reservationId);
    }

    public static Specification<ReservationEntity> withLastNameStartingWith(String lastName) {
        return (Root<ReservationEntity> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) ->
                criteriaBuilder.like(criteriaBuilder.lower(root.get(ReservationEntity_.lastName)), lastName.toLowerCase() + "%");
    }

}
