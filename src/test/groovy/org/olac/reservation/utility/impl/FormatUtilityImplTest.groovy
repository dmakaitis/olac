package org.olac.reservation.utility.impl

import spock.lang.Specification
import spock.lang.Unroll

class FormatUtilityImplTest extends Specification {

    def service = new FormatUtilityImpl()

    @Unroll
    def "The format utility should propery format #value"() {
        expect:
          service.formatCurrency(value) == expected

        where:
          value || expected
          123.0 || '$123.00'
          0.03  || '$0.03'
          97    || '$97.00'
    }
}
