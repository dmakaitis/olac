package org.olac.reservation

import org.springframework.boot.test.context.SpringBootTest
import spock.lang.Specification

@SpringBootTest
class ReservationApplicationTest extends Specification {

    def "Context loads"() {
        expect:
          true
    }

}
