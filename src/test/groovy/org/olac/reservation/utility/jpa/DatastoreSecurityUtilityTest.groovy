package org.olac.reservation.utility.jpa

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier
import com.google.api.client.json.webtoken.JsonWebSignature
import org.olac.reservation.exception.OlacException
import org.olac.reservation.utility.jpa.entity.AccountEntity
import org.olac.reservation.utility.jpa.repository.AccountRepository
import org.olac.reservation.utility.model.Account
import org.olac.reservation.utility.spring.JwtUtility
import org.springframework.security.authentication.AnonymousAuthenticationToken
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UsernameNotFoundException
import spock.lang.Specification
import spock.lang.Unroll

import java.security.GeneralSecurityException

class DatastoreSecurityUtilityTest extends Specification {

    def static final ANONYMOUS_AUTHENTICATION = new AnonymousAuthenticationToken('my-key', 'principal', [new SimpleGrantedAuthority('USER')])
    def static final TEST_USER_AUTHENTICATION = new UsernamePasswordAuthenticationToken('my-test-user', 'my-test-password', [new SimpleGrantedAuthority("ROLE_EVENT_COORDINATOR")])
    def static final TEST_ADMIN_AUTHENTICATION = new UsernamePasswordAuthenticationToken('my-admin-user', 'my-admin-password', [new SimpleGrantedAuthority("ROLE_ADMIN")])

    def static final TEST_USER_ENTITY = new AccountEntity(id: 1, username: 'my-test-user', email: 'test@nowhere.com', enabled: true, admin: false)
    def static final TEST_ADMIN_ENTITY = new AccountEntity(id: 2, username: 'my-admin-user', email: 'admin@nowhere.com', enabled: true, admin: true)
    def static final TEST_DISABLED_ENTITY = new AccountEntity(id: 3, username: 'my-disabled-user', email: 'disabled@nowhere.com', enabled: false, admin: false)

    def repository = Mock(AccountRepository) {
        _ * findByUsername(TEST_USER_ENTITY.username) >> Optional.of(TEST_USER_ENTITY)
        _ * findByUsername(TEST_ADMIN_ENTITY.username) >> Optional.of(TEST_ADMIN_ENTITY)
        _ * findByUsername(TEST_DISABLED_ENTITY.username) >> Optional.of(TEST_DISABLED_ENTITY)
        _ * findByUsername(_) >> Optional.empty()

        _ * findByEmailIgnoreCase(TEST_USER_ENTITY.email) >> Optional.of(TEST_USER_ENTITY)
        _ * findByEmailIgnoreCase(TEST_ADMIN_ENTITY.email) >> Optional.of(TEST_ADMIN_ENTITY)
        _ * findByEmailIgnoreCase(TEST_DISABLED_ENTITY.email) >> Optional.of(TEST_DISABLED_ENTITY)
        _ * findByEmailIgnoreCase(_) >> Optional.empty()

        _ * findAll() >> [TEST_USER_ENTITY, TEST_ADMIN_ENTITY, TEST_DISABLED_ENTITY]
    }
    def jwtUtility = Mock(JwtUtility)
    def tokenVerifier = Mock(GoogleIdTokenVerifier)

    def utility = new DatastoreSecurityUtility(repository, jwtUtility, tokenVerifier)

    @Unroll
    def "The security utility should return the username of the currently authenticated user"() {
        given:
          SecurityContextHolder.getContext().setAuthentication(authentication)

        expect:
          utility.getCurrentUserName() == expected

        where:
          authentication            || expected
          null                      || 'anonymous'
          ANONYMOUS_AUTHENTICATION  || 'anonymous'
          TEST_USER_AUTHENTICATION  || TEST_USER_AUTHENTICATION.principal
          TEST_ADMIN_AUTHENTICATION || TEST_ADMIN_AUTHENTICATION.principal
    }

    @Unroll
    def "The security utility should correctly return if the current user is an administrator"() {
        given:
          SecurityContextHolder.getContext().setAuthentication(authentication)

        expect:
          utility.isCurrentUserAdmin() == expected

        where:
          authentication            || expected
          null                      || false
          ANONYMOUS_AUTHENTICATION  || false
          TEST_USER_AUTHENTICATION  || false
          TEST_ADMIN_AUTHENTICATION || true
    }

    def "The utility should throw an exception if ask to retrieve a user that doesn't exist"() {
        when:
          utility.loadUserByUsername("some-username")

        then:
          thrown(UsernameNotFoundException)
    }

    @Unroll
    def "The utility should return the user details for an existing user"() {
        when:
          def result = utility.loadUserByUsername(username)

        then:
          result.username == username
          result.enabled == enabled
          result.accountNonExpired
          result.accountNonLocked
          result.credentialsNonExpired
          result.password != null
          result.authorities.collect { it.authority }.contains("ROLE_EVENT_COORDINATOR")
          result.authorities.collect { it.authority }.contains("ROLE_ADMIN") == admin

        where:
          username                      || enabled | admin
          TEST_USER_ENTITY.username     || true    | false
          TEST_ADMIN_ENTITY.username    || true    | true
          TEST_DISABLED_ENTITY.username || false   | false
    }

    def "The utility should be able to return all users"() {
        when:
          def result = utility.getAccounts()

        then:
          result.size() == 3
          result.find { it.username == TEST_USER_ENTITY.username } == new Account(id: TEST_USER_ENTITY.id, username: TEST_USER_ENTITY.username, email: TEST_USER_ENTITY.email, enabled: TEST_USER_ENTITY.enabled, admin: false)
          result.find { it.username == TEST_ADMIN_ENTITY.username } == new Account(id: TEST_ADMIN_ENTITY.id, username: TEST_ADMIN_ENTITY.username, email: TEST_ADMIN_ENTITY.email, enabled: TEST_ADMIN_ENTITY.enabled, admin: true)
          result.find { it.username == TEST_DISABLED_ENTITY.username } == new Account(id: TEST_DISABLED_ENTITY.id, username: TEST_DISABLED_ENTITY.username, email: TEST_DISABLED_ENTITY.email, enabled: TEST_DISABLED_ENTITY.enabled, admin: false)
    }

    @Unroll
    def "The utility should be able to create an account"() {
        given:
          def expectedEntity = new AccountEntity(id: new Random().nextLong(), username: username, email: email, admin: admin, enabled: true)
          def expectedAccount = new Account(id: expectedEntity.id, username: expectedEntity.username, email: expectedEntity.email, admin: expectedEntity.admin, enabled: expectedEntity.enabled)

        when:
          def result = utility.createAccount(username, email, admin)

        then:
          1 * repository.save(new AccountEntity(username: username, email: email, admin: admin, enabled: true)) >> expectedEntity

          result == expectedAccount

        where:
          username | email               | admin
          'bob'    | 'bob@nowhere.com'   | false
          'alice'  | 'alice@nowhere.com' | true
    }

    def "We should not be able to create an account with no username"() {
        when:
          utility.createAccount('', 'bob@nowhere.com', false)

        then:
          thrown(OlacException)
    }

    def "We should not be able to create an account with no email"() {
        when:
          utility.createAccount('bob', '', false)

        then:
          thrown(OlacException)
    }

    @Unroll
    def "The utility should be able to find an account"() {
        expect:
          utility.findAccount(username) == expected

        where:
          username                      || expected
          TEST_USER_ENTITY.username     || Optional.of(new Account(id: TEST_USER_ENTITY.id, username: TEST_USER_ENTITY.username, email: TEST_USER_ENTITY.email, enabled: TEST_USER_ENTITY.enabled, admin: TEST_USER_ENTITY.admin))
          TEST_ADMIN_ENTITY.username    || Optional.of(new Account(id: TEST_ADMIN_ENTITY.id, username: TEST_ADMIN_ENTITY.username, email: TEST_ADMIN_ENTITY.email, enabled: TEST_ADMIN_ENTITY.enabled, admin: TEST_ADMIN_ENTITY.admin))
          TEST_DISABLED_ENTITY.username || Optional.of(new Account(id: TEST_DISABLED_ENTITY.id, username: TEST_DISABLED_ENTITY.username, email: TEST_DISABLED_ENTITY.email, enabled: TEST_DISABLED_ENTITY.enabled, admin: TEST_DISABLED_ENTITY.admin))
          "no-such-user"                || Optional.empty()
          ""                            || Optional.empty()
          null                          || Optional.empty()
    }

    def "Trying to update an account with no username should fail"() {
        expect:
          !utility.updateAccount(new Account(username: '', email: 'test@testing.com', enabled: true, admin: false))
    }

    def "Trying to update an account that does not exist should fail"() {
        expect:
          !utility.updateAccount(new Account(username: 'no-such-user', email: 'test@testing.com', enabled: true, admin: false))
    }

    def "We should be able to make an update to a non-admin account"() {
        when:
          def success = utility.updateAccount(new Account(username: TEST_USER_ENTITY.username, email: 'test@testing.com', enabled: true, admin: false))

        then:
          1 * repository.save(new AccountEntity(id: TEST_USER_ENTITY.id, username: TEST_USER_ENTITY.username, email: 'test@testing.com', enabled: true, admin: false))

          success
    }

    def "We should be able to change the email of an admin account"() {
        when:
          def success = utility.updateAccount(new Account(username: TEST_ADMIN_ENTITY.username, email: 'test@testing.com', enabled: true, admin: true))

        then:
          1 * repository.save(new AccountEntity(id: TEST_ADMIN_ENTITY.id, username: TEST_ADMIN_ENTITY.username, email: 'test@testing.com', enabled: true, admin: true))

          success
    }

    def "If we try to disable the only admin account, the account should be left as an enabled administrator"() {
        when:
          def success = utility.updateAccount(new Account(username: TEST_ADMIN_ENTITY.username, email: 'test@testing.com', enabled: false, admin: false))

        then:
          1 * repository.save(new AccountEntity(id: TEST_ADMIN_ENTITY.id, username: TEST_ADMIN_ENTITY.username, email: 'test@testing.com', enabled: true, admin: true))

          success
    }

    def "If a Google identity token is invalid, then the user should not be authenticated"() {
        given:
          _ * tokenVerifier.verify(_) >> null

        when:
          utility.validateUserWithGoogleIdentity('invalid-google-token')

        then:
          thrown(OlacException)
    }

    def "If there is an exception thrown while validating a Google identity token, then the user should not be authenticated"() {
        given:
          _ * tokenVerifier.verify(_) >> { throw new GeneralSecurityException() }

        when:
          utility.validateUserWithGoogleIdentity('invalid-google-token')

        then:
          thrown(OlacException)
    }

    def "Given a valid Google identity token, the utility should authenticate an existing user"() {
        given:
          def token = 'a-valid-token'

          def expectedToken = 'my-expected-jwt-token'

          _ * tokenVerifier.verify(token) >> new GoogleIdToken(
                  new JsonWebSignature.Header(),
                  new GoogleIdToken.Payload(email: TEST_USER_ENTITY.email),
                  new byte[0],
                  new byte[0])
          _ * jwtUtility.generateJwtToken(TEST_USER_ENTITY.username) >> expectedToken

        when:
          def response = utility.validateUserWithGoogleIdentity(token)

        then:
          response.username == TEST_USER_ENTITY.username
          response.jwtToken == expectedToken
    }

    def "If a user does not exist, they should not be authenticated even with a valid Google identity token"() {
        given:
          def token = 'a-valid-token'

          _ * tokenVerifier.verify(token) >> new GoogleIdToken(
                  new JsonWebSignature.Header(),
                  new GoogleIdToken.Payload(email: 'nosuchuser@nowhere.com'),
                  new byte[0],
                  new byte[0])

        when:
          utility.validateUserWithGoogleIdentity(token)

        then:
          thrown(OlacException)
    }

}
