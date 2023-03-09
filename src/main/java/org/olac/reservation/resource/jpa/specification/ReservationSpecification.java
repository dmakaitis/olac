package org.olac.reservation.resource.jpa.specification;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.olac.reservation.resource.jpa.entity.ReservationEntity;
import org.olac.reservation.resource.jpa.entity.ReservationEntity_;
import org.springframework.data.jpa.domain.Specification;

public class ReservationSpecification {

    private ReservationSpecification() {
        // Private constructor
    }

    public static Specification<ReservationEntity> withId(long id) {
        return new ComparableSpec("withId", id,
                (Root<ReservationEntity> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) ->
                        criteriaBuilder.equal(root.get(ReservationEntity_.id), id));
    }

    public static Specification<ReservationEntity> withReservationId(String reservationId) {
        return new ComparableSpec("withReservationId", reservationId,
                (Root<ReservationEntity> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) ->
                        criteriaBuilder.equal(root.get(ReservationEntity_.reservationId), reservationId));
    }

    public static Specification<ReservationEntity> withFirstNameContaining(String filter) {
        return new ComparableSpec("withFirstNameContaining", filter,
                (Root<ReservationEntity> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) ->
                        criteriaBuilder.like(criteriaBuilder.lower(root.get(ReservationEntity_.firstName)), "%" + filter.toLowerCase() + "%"));
    }

    public static Specification<ReservationEntity> withLastNameContaining(String filter) {
        return new ComparableSpec("withLastNameContaining", filter,
                (Root<ReservationEntity> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) ->
                        criteriaBuilder.like(criteriaBuilder.lower(root.get(ReservationEntity_.lastName)), "%" + filter.toLowerCase() + "%"));
    }

    public static Specification<ReservationEntity> withEmailContaining(String filter) {
        return new ComparableSpec("withEmailContaining", filter,
                (Root<ReservationEntity> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) ->
                        criteriaBuilder.like(criteriaBuilder.lower(root.get(ReservationEntity_.email)), "%" + filter.toLowerCase() + "%"));
    }

    @Data
    @AllArgsConstructor
    public static class ComparableSpec implements Specification<ReservationEntity> {
        private final String specificationType;
        private final transient Object value;
        @EqualsAndHashCode.Exclude
        @ToString.Exclude
        private final Specification<ReservationEntity> specification;

        @Override
        public Predicate toPredicate(Root<ReservationEntity> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
            return specification.toPredicate(root, query, criteriaBuilder);
        }
    }

}
