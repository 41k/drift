package drift.dto

import drift.model.Car
import spock.lang.Specification

import static drift.util.TestConstants.*

class CarDtoTest extends Specification {

    def 'should build DTO'() {
        expect:
        CarDto.from(
                Car.builder().id(CAR_ID).ownerId(USER_ID_1)
                        .brand(BRAND).model(MODEL).power(POWER_1).image(PATH).active(true).build()) ==
                CarDto.builder().id(CAR_ID).ownerId(USER_ID_1)
                        .brand(BRAND).model(MODEL).power(POWER_1).image(PATH).active(true).build()
    }
}
