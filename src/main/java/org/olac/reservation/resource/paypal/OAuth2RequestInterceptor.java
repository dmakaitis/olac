package org.olac.reservation.resource.paypal;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class OAuth2RequestInterceptor implements RequestInterceptor {

    private final OAuthClient oAuthClient;

    @Override
    public void apply(RequestTemplate template) {
        AccessToken accessToken = oAuthClient.getAccessToken("client_credentials");
        String token = accessToken.getToken();
        template.header("Authorization", "Bearer " + token);
    }

}
