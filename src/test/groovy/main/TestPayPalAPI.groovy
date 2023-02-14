package main

import com.fasterxml.jackson.databind.ObjectMapper
import org.olac.reservation.config.PayPalConfig

class TestPayPalAPI {

    static void main(String[] args) {
        def mapper = new ObjectMapper()
        def config = new PayPalConfig()

        def oauthClient = config.oAuthClient(mapper)
        def paypalClient = config.payPalClient(oauthClient, mapper)

        println paypalClient.getOrderDetails("2GA54266DE864450V")
    }

}
