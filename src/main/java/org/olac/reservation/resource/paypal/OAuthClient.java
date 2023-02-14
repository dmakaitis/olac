package org.olac.reservation.resource.paypal;

import feign.Headers;
import feign.Param;
import feign.RequestLine;

public interface OAuthClient {

    @RequestLine("POST /v1/oauth2/token")
    @Headers("Content-Type: application/x-www-form-urlencoded")
    AccessToken getAccessToken(@Param("grant_type") String grantType);

}
