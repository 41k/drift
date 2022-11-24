package drift.dto

import spock.lang.Specification

import static drift.util.TestConstants.SCORING_SYSTEM
import static drift.util.TestConstants.SCORING_SYSTEM_DTO

class ScoringSystemDtoTest extends Specification {

    def 'should build DTO'() {
        expect:
        ScoringSystemDto.from(SCORING_SYSTEM) == SCORING_SYSTEM_DTO
    }
}
