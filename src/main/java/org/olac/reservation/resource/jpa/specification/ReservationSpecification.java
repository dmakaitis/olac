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

    public static Specification<ReservationEntity> withId(long id) {
        return (Root<ReservationEntity> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) ->
                criteriaBuilder.equal(root.get(ReservationEntity_.id), id);
    }

    public static Specification<ReservationEntity> withReservationId(String reservationId) {
        return (Root<ReservationEntity> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) ->
                criteriaBuilder.equal(root.get(ReservationEntity_.reservationId), reservationId);
    }

    public static Specification<ReservationEntity> withFirstNameContaining(String filter) {
        return (Root<ReservationEntity> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) ->
                criteriaBuilder.like(criteriaBuilder.lower(root.get(ReservationEntity_.firstName)), "%" + filter.toLowerCase() + "%");
    }

    public static Specification<ReservationEntity> withLastNameContaining(String filter) {
        return (Root<ReservationEntity> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) ->
                criteriaBuilder.like(criteriaBuilder.lower(root.get(ReservationEntity_.lastName)), "%" + filter.toLowerCase() + "%");
    }

    public static Specification<ReservationEntity> withEmailContaining(String filter) {
        return (Root<ReservationEntity> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) ->
                criteriaBuilder.like(criteriaBuilder.lower(root.get(ReservationEntity_.email)), "%" + filter.toLowerCase() + "%");
    }

}
