package drift.dto

import spock.lang.Specification

import static drift.util.TestConstants.CHAMPIONSHIP
import static drift.util.TestConstants.CHAMPIONSHIP_DTO

class ChampionshipDtoTest extends Specification {

    def 'should build DTO'() {
        expect:
        ChampionshipDto.from(CHAMPIONSHIP) == CHAMPIONSHIP_DTO
    }
}
