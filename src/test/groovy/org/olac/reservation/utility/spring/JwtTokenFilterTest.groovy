package org.olac.reservation.utility.spring

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.olac.reservation.config.OlacProperties
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import spock.lang.Specification

class JwtTokenFilterTest extends Specification {

    def properties = new OlacProperties(
            jwt: new OlacProperties.Jwt(
                    key: 'my-jwt-secret-with-an-even-longer-key',
                    timeoutMinutes: 120
            )
    )
    def jwtUtility = new JwtUtility(properties)
    def userDetailsService = Mock(UserDetailsService)

    def filter = new JwtTokenFilter(userDetailsService, jwtUtility)

    def request = Mock(HttpServletRequest)
    def response = Mock(HttpServletResponse)
    def filterChain = Mock(FilterChain)

    void setup() {
        SecurityContextHolder.context.authentication = null
    }

    def "If there is no authentication header on the request, the filter should simply pass control onto the next filter"() {
        when:
          filter.doFilterInternal(request, response, filterChain)

        then:
          1 * filterChain.doFilter(request, response)

          SecurityContextHolder.context.authentication == null
    }

    def "If the authentication header on the request is not a bearer token, the filter should simply pass control onto the next filter"() {
        given:
          _ * request.getHeader("Authorization") >> "Not a bearer token"

        when:
          filter.doFilterInternal(request, response, filterChain)

        then:
          1 * filterChain.doFilter(request, response)

          SecurityContextHolder.context.authentication == null
    }

    def "If the authentication request contains a bearer token with an invalid JWT token, the filter should simply pass control onto the next filter"() {
        given:
          _ * request.getHeader("Authorization") >> "Bearer an-invalid-bearer-token"

        when:
          filter.doFilterInternal(request, response, filterChain)

        then:
          1 * filterChain.doFilter(request, response)

          SecurityContextHolder.context.authentication == null
    }

    def "If the authentication request contains a bearer token with a valid JWT token but the user does not exist, the filter should simply pass control onto the next filter"() {
        given:
          def token = jwtUtility.generateJwtToken("some-nonexistant-user")
          _ * request.getHeader("Authorization") >> "Bearer ${token}"

        when:
          filter.doFilterInternal(request, response, filterChain)

        then:
          1 * filterChain.doFilter(request, response)

          SecurityContextHolder.context.authentication == null
    }

    def "If the authentication request contains a bearer token with a valid JWT token and the user does exist, the filter should update the security context before passing control onto the next filter"() {
        given:
          def username = "some-existing-user"
          def token = jwtUtility.generateJwtToken(username)
          def expectedAuthorities = [
                  new SimpleGrantedAuthority("ROLE_A"),
                  new SimpleGrantedAuthority("ROLE_B")
          ]

          _ * userDetailsService.loadUserByUsername(username) >> Mock(UserDetails) {
              _ * getUsername() >> username
              _ * getAuthorities() >> expectedAuthorities
          }
          _ * request.getHeader("Authorization") >> "Bearer ${token}"

        when:
          filter.doFilterInternal(request, response, filterChain)

        then:
          1 * filterChain.doFilter(request, response)

          SecurityContextHolder.context.authentication != null
          SecurityContextHolder.context.authentication.authenticated
          SecurityContextHolder.context.authentication.principal instanceof UserDetails
          ((UserDetails) SecurityContextHolder.context.authentication.principal).username == username
          SecurityContextHolder.context.authentication.authorities == expectedAuthorities
    }

}
