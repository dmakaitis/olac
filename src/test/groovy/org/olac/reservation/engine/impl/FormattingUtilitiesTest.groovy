package org.olac.reservation.engine.impl

import org.olac.reservation.utility.FormatUtility
import spock.lang.Specification

class FormattingUtilitiesTest extends Specification {

    def formatUtility = Mock(FormatUtility)
    def utility = new FormattingUtilities(formatUtility)

    def "This Thymeleaf utility should just forward calls onto the application's format utility"() {
        given:
          def input = 123.90
          def expected = '$123.90'

        when:
          def result = utility.currency(input)

        then:
          result == expected

          1 * formatUtility.formatCurrency(input) >> expected
    }

}
