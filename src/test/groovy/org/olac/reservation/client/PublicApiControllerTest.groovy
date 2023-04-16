package org.olac.reservation.client

import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletRequest
import org.olac.reservation.config.OlacProperties
import org.olac.reservation.manager.ReservationManager
import spock.lang.Specification

class PublicApiControllerTest extends Specification {

    def reservationManager = Mock(ReservationManager)
    def properties = new OlacProperties(
            paypal: new OlacProperties.PayPal(
                    apiBase: "https://my-test-paypal-server.com",
                    client: "paypal-client-id"
            )
    )

    def api = new PublicApiController(reservationManager, properties)

    def "We should be able to retrieve properties if there are no cookies on the incoming request"() {
        given:
          def httpRequest = Mock(HttpServletRequest)

        expect:
          api.getClientConfiguration(httpRequest) == new PublicApiController.ClientConfiguration(
                  showLogin: false,
                  enableReservations: true,
                  payPal: new PublicApiController.ClientConfiguration.PayPayConfig(
                          apiBase: properties.paypal.apiBase,
                          clientId: properties.paypal.client
                  )
          )
    }

    def "If the show login cookie is missing, do not show the login button"() {
        given:
          def httpRequest = Mock(HttpServletRequest) {
              _ * getCookies() >> []
          }

        expect:
          api.getClientConfiguration(httpRequest) == new PublicApiController.ClientConfiguration(
                  showLogin: false,
                  enableReservations: true,
                  payPal: new PublicApiController.ClientConfiguration.PayPayConfig(
                          apiBase: properties.paypal.apiBase,
                          clientId: properties.paypal.client
                  )
          )
    }

    def "If the show login cookie is present with the correct value, show the login button"() {
        given:
          def httpRequest = Mock(HttpServletRequest) {
              _ * getCookies() >> [
                      new Cookie(AuthApiController.COOKIE_SHOW_LOGIN, AuthApiController.COOKIE_VALUE_YES)
              ]
          }

        expect:
          api.getClientConfiguration(httpRequest) == new PublicApiController.ClientConfiguration(
                  showLogin: true,
                  enableReservations: true,
                  payPal: new PublicApiController.ClientConfiguration.PayPayConfig(
                          apiBase: properties.paypal.apiBase,
                          clientId: properties.paypal.client
                  )
          )
    }

    def "If the show login cookie is present with the wrong value, do not show the login button"() {
        given:
          def httpRequest = Mock(HttpServletRequest) {
              _ * getCookies() >> [
                      new Cookie(AuthApiController.COOKIE_SHOW_LOGIN, "Some other value")
              ]
          }

        expect:
          api.getClientConfiguration(httpRequest) == new PublicApiController.ClientConfiguration(
                  showLogin: false,
                  enableReservations: true,
                  payPal: new PublicApiController.ClientConfiguration.PayPayConfig(
                          apiBase: properties.paypal.apiBase,
                          clientId: properties.paypal.client
                  )
          )
    }

}
