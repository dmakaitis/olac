package org.olac.reservation.resource.paypal

import feign.Request
import feign.RequestTemplate
import spock.lang.Specification

class OAuth2RequestInterceptorTest extends Specification {

    def oauthClient = Mock(OAuthClient)

    def interceptor = new OAuth2RequestInterceptor(oauthClient)

    def "The interceptor should request an OAuth token, then apply it"() {
        given:
          def token = "my-access-token"
          def accessToken = new AccessToken(token: token)

          def template = new RequestTemplate()
          template.method(Request.HttpMethod.GET)

          _ * oauthClient.getAccessToken("client_credentials") >> accessToken


        when:
          interceptor.apply(template)

        then:
          template.resolve([:]).request().headers()["Authorization"] == ["Bearer ${token}"]
    }

}
