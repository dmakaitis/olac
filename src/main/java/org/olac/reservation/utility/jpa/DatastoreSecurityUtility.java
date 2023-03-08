package org.olac.reservation.utility.jpa;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.olac.reservation.exception.OlacException;
import org.olac.reservation.utility.SecurityUtility;
import org.olac.reservation.utility.jpa.entity.AccountEntity;
import org.olac.reservation.utility.jpa.repository.AccountRepository;
import org.olac.reservation.utility.model.Account;
import org.olac.reservation.utility.model.ValidateUserResponse;
import org.olac.reservation.utility.spring.JwtUtility;
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

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.StreamSupport;

import static java.util.Arrays.asList;
import static java.util.Collections.singleton;

@Service
@RequiredArgsConstructor
@Slf4j
public class DatastoreSecurityUtility implements SecurityUtility, UserDetailsService {

    public static final String UNRECOGNIZED_USER_OR_CREDENTIALS = "Unrecognized user or credentials";
    private final AccountRepository repository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtUtility jwtUtility;

    @Override
    public String getCurrentUserName() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication instanceof AnonymousAuthenticationToken) {
            return "anonymous";
        }
        return authentication.getName();
    }

    @Override
    public boolean isCurrentUserAdmin() {
        return Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
                .map(auth -> auth.getAuthorities().stream().map(GrantedAuthority::getAuthority).anyMatch(s -> s.equals("ROLE_ADMIN")))
                .orElse(false);
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
    public Account createAccount(String username, String email, boolean admin) {
        AccountEntity entity = new AccountEntity();
        entity.setUsername(username);
        entity.setEmail(email);
        entity.setAdmin(admin);

        return toAccount(repository.save(entity));
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

        // Make sure we're not disabling our last active administrator...
        if (isEnabledAdmin(entity) && !isEnabledAdmin(account) && StreamSupport.stream(repository.findAll().spliterator(), false)
                .filter(a -> !a.getUsername().equals(account.getUsername()))
                .noneMatch(DatastoreSecurityUtility::isEnabledAdmin)) {
            account.setAdmin(true);
            account.setEnabled(true);
        }

        entity.setEmail(account.getEmail());
        entity.setEnabled(account.isEnabled());
        entity.setAdmin(account.isAdmin());

        repository.save(entity);

        return true;
    }

    @Override
    public ValidateUserResponse validateUserWithGoogleIdentity(String credential) {
        GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), new GsonFactory())
                .setAudience(asList("192542427030-lo0r4n23ecl4bl35v1rq0ejhn3gfffgj.apps.googleusercontent.com"))
                .build();

        try {
            GoogleIdToken token = verifier.verify(credential);
            if (token != null) {
                GoogleIdToken.Payload payload = token.getPayload();

                // Get the username associated with the email address...
                Optional<ValidateUserResponse> response = repository.findByEmailIgnoreCase(payload.getEmail())
                        .map(AccountEntity::getUsername)
                        .map(username -> ValidateUserResponse.builder()
                                .username(username)
                                .jwtToken(jwtUtility.generateJwtToken(username))
                                .build());

                if (response.isPresent()) {
                    log.info("Authenticated user using Google account: {}", response.get().getUsername());
                    return response.get();
                }
            }

            throw new OlacException(UNRECOGNIZED_USER_OR_CREDENTIALS);
        } catch (GeneralSecurityException | IOException e) {
            throw new OlacException(UNRECOGNIZED_USER_OR_CREDENTIALS, e);
        }
    }

    private static boolean isEnabledAdmin(Account account) {
        return account.isAdmin() && account.isEnabled();
    }

    private static boolean isEnabledAdmin(AccountEntity account) {
        return account.isAdmin() && account.isEnabled();
    }

    private Account toAccount(AccountEntity accountEntity) {
        return Account.builder()
                .id(accountEntity.getId())
                .username(accountEntity.getUsername())
                .email(accountEntity.getEmail())
                .enabled(accountEntity.isEnabled())
                .admin(accountEntity.isAdmin())
                .build();
    }

    @RequiredArgsConstructor
    public static class UserDetailsImpl implements UserDetails {

        private static final GrantedAuthority EVENT_COORDINATOR_ROLE = new SimpleGrantedAuthority("ROLE_EVENT_COORDINATOR");
        private static final GrantedAuthority ADMIN_ROLE = new SimpleGrantedAuthority("ROLE_ADMIN");

        private final transient AccountEntity account;

        @Override
        public Collection<? extends GrantedAuthority> getAuthorities() {
            return account.isAdmin() ? new HashSet<>(asList(EVENT_COORDINATOR_ROLE, ADMIN_ROLE)) : singleton(EVENT_COORDINATOR_ROLE);
        }

        @Override
        public String getPassword() {
            return "";
        }

        @Override
        public String getUsername() {
            return account.getUsername();
        }

        @Override
        public boolean isAccountNonExpired() {
            return true;
        }

        @Override
        public boolean isAccountNonLocked() {
            return true;
        }

        @Override
        public boolean isCredentialsNonExpired() {
            return true;
        }

        @Override
        public boolean isEnabled() {
            return account.isEnabled();
        }

    }

}
