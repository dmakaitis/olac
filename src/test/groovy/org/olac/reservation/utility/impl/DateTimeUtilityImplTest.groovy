package org.olac.reservation.utility.impl

import spock.lang.Specification

class DateTimeUtilityImplTest extends Specification {

    def service = new DateTimeUtilityImpl()

    def "The date/time utility should return the current date/time"() {
        when:
          def before = new Date()
          def result = service.getCurrentTime()
          def after = new Date()

        then:
          result.time >= result.time
          result.time <= after.time
    }

}
