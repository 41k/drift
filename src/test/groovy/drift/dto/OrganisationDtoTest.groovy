package drift.dto

import drift.model.Organisation
import spock.lang.Specification

import static drift.util.TestConstants.*

class OrganisationDtoTest extends Specification {

    def 'should build DTO'() {
        expect:
        OrganisationDto.from(
                Organisation.builder().id(ORGANISATION_ID).ownerId(USER_ID_1)
                        .name(NAME_1).description(DESCRIPTION_1).image(PATH).active(true).build()) ==
                OrganisationDto.builder().id(ORGANISATION_ID).ownerId(USER_ID_1)
                        .name(NAME_1).description(DESCRIPTION_1).image(PATH).active(true).build()
    }
}
