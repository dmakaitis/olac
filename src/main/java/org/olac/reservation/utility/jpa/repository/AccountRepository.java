package org.olac.reservation.utility.jpa.repository;

import org.olac.reservation.utility.jpa.entity.AccountEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AccountRepository extends JpaRepository<AccountEntity, Long> {

    Optional<AccountEntity> findByUsername(String username);

    Optional<AccountEntity> findByEmailIgnoreCase(String email);

}
