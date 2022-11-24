package drift.dto

import drift.model.User
import spock.lang.Specification

import static drift.util.TestConstants.*

class UserDtoTest extends Specification {

    def 'should build DTO'() {
        expect:
        UserDto.from(
                User.builder()
                        .id(USER_ID_1).email(EMAIL).firstName(FIRST_NAME)
                        .lastName(LAST_NAME).countryCode(COUNTRY_CODE).city(CITY).image(PATH).active(true).build()) ==
                UserDto.builder()
                        .id(USER_ID_1).email(EMAIL).firstName(FIRST_NAME)
                        .lastName(LAST_NAME).countryCode(COUNTRY_CODE).city(CITY).image(PATH).active(true).build()
    }
}
