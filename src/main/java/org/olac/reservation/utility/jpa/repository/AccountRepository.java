package org.olac.reservation.utility.jpa.repository;

import org.olac.reservation.utility.jpa.entity.AccountEntity;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface AccountRepository extends CrudRepository<AccountEntity, Long> {

    Optional<AccountEntity> findByUsername(String username);

    Optional<AccountEntity> findByEmailIgnoreCase(String email);

}
