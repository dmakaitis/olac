package org.olac.reservation.utility.jpa;

import lombok.RequiredArgsConstructor;
import org.olac.reservation.exception.OlacException;
import org.olac.reservation.utility.SecurityUtility;
import org.olac.reservation.utility.jpa.entity.AccountEntity;
import org.olac.reservation.utility.jpa.repository.AccountRepository;
import org.olac.reservation.utility.model.Account;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.StreamSupport;

import static java.util.Arrays.asList;
import static java.util.Collections.singleton;

@Service
@RequiredArgsConstructor
public class DatastoreSecurityUtility implements SecurityUtility, UserDetailsService {

    private final AccountRepository repository;
    private final BCryptPasswordEncoder passwordEncoder;

    @Override
    public String getCurrentUserName() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication instanceof AnonymousAuthenticationToken) {
            return "anonymous";
        }
        return authentication.getName();
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<UserDetailsImpl> userDetails = repository.findByUsername(username)
                .map(UserDetailsImpl::new)
                .filter(u -> !u.getAuthorities().isEmpty());

        if (userDetails.isEmpty()) {
            throw new UsernameNotFoundException("Username does not exist: " + username);
        }

        return userDetails.get();
    }

    @Override
    public List<Account> getAccounts() {
        return StreamSupport.stream(repository.findAll().spliterator(), false)
                .map(this::toAccount)
                .toList();
    }

    @Override
    public Account createAccount(String username, String password, boolean admin) {
        // Ensure the username is unique
        Optional<AccountEntity> check = repository.findByUsername(username);
        if (check.isPresent()) {
            throw new OlacException("Username muse be unique");
        }

        AccountEntity entity = new AccountEntity();
        entity.setUsername(username);
        entity.setPassword(passwordEncoder.encode(password));
        entity.setAdmin(admin);
        return toAccount(repository.save(entity));
    }

    @Override
    public boolean setPassword(String username, String newPassword) {
        Optional<AccountEntity> accountOptional = repository.findByUsername(username);
        if (accountOptional.isEmpty()) {
            return false;
        }
        AccountEntity account = accountOptional.get();

        account.setPassword(passwordEncoder.encode(newPassword));

        repository.save(account);

        return true;
    }

    @Override
    public boolean validatePassword(String username, String password) {
        Optional<AccountEntity> accountOptional = repository.findByUsername(username);
        if (accountOptional.isEmpty()) {
            return false;
        }
        AccountEntity account = accountOptional.get();

        return passwordEncoder.matches(password, account.getPassword());
    }

    @Override
    public Optional<Account> findAccount(String username) {
        return repository.findByUsername(username)
                .map(this::toAccount);
    }

    @Override
    public boolean updateAccount(Account account) {
        Optional<AccountEntity> accountOptional = repository.findByUsername(account.getUsername());
        if (accountOptional.isEmpty()) {
            return false;
        }
        AccountEntity entity = accountOptional.get();

        entity.setExpired(account.isExpired());
        entity.setLocked(account.isLocked());
        entity.setCredentialsExpired(account.isCredentialsExpired());
        entity.setEnabled(account.isEnabled());
        entity.setAdmin(account.isAdmin());

        repository.save(entity);

        return true;
    }

    private Account toAccount(AccountEntity accountEntity) {
        return Account.builder()
                .id(accountEntity.getId())
                .username(accountEntity.getUsername())
                .expired(accountEntity.isExpired())
                .locked(accountEntity.isLocked())
                .credentialsExpired(accountEntity.isCredentialsExpired())
                .enabled(accountEntity.isEnabled())
                .admin(accountEntity.isAdmin())
                .build();
    }

    @RequiredArgsConstructor
    public static class UserDetailsImpl implements UserDetails {

        private static final GrantedAuthority USER_ROLE = new SimpleGrantedAuthority("ROLE_USER");
        private static final GrantedAuthority ADMIN_ROLE = new SimpleGrantedAuthority("ROLE_ADMIN");

        private final transient AccountEntity account;

        @Override
        public Collection<? extends GrantedAuthority> getAuthorities() {
            return account.isAdmin() ? new HashSet<>(asList(USER_ROLE, ADMIN_ROLE)) : singleton(USER_ROLE);
        }

        @Override
        public String getPassword() {
            return account.getPassword();
        }

        @Override
        public String getUsername() {
            return account.getUsername();
        }

        @Override
        public boolean isAccountNonExpired() {
            return !account.isExpired();
        }

        @Override
        public boolean isAccountNonLocked() {
            return !account.isLocked();
        }

        @Override
        public boolean isCredentialsNonExpired() {
            return !account.isCredentialsExpired();
        }

        @Override
        public boolean isEnabled() {
            return account.isEnabled();
        }

    }

}
