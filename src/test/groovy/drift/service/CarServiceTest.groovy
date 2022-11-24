package drift.service

import drift.model.Car
import drift.model.ImageCategory
import drift.repository.CarRepository
import org.springframework.web.multipart.MultipartFile
import spock.lang.Specification

import static drift.util.TestConstants.*

class CarServiceTest extends Specification {

    private idGenerator = Mock(IdGenerator)
    private securityService = Mock(SecurityService)
    private carRepository = Mock(CarRepository)
    private fileService = Mock(FileService)
    private image = Mock(MultipartFile)

    private carService = new CarService(idGenerator, securityService, carRepository, fileService)


    // --- Car registration

    def 'should register car successfully'() {
        when:
        def carId = carService.registerCar(CAR_REGISTRATION_DTO)

        then:
        1 * idGenerator.generate() >> CAR_ID
        1 * securityService.getRequesterId() >> USER_ID_1
        1 * carRepository.save(CAR) >> CAR
        0 * _

        and:
        carId == CAR_ID
    }


    // --- Car update

    def 'should update car successfully'() {
        given:
        def updatedCar = CAR.toBuilder().power(POWER_2).build()

        when:
        carService.updateCar(CAR_ID, CAR_UPDATE_DTO)

        then:
        1 * carRepository.findById(CAR_ID) >> Optional.of(CAR)
        1 * securityService.validateOwner(USER_ID_1, Car)
        1 * carRepository.save(updatedCar)
        0 * _
    }

    def 'should throw exception during car update if car is not found by provided id'() {
        when:
        carService.updateCar(CAR_ID, CAR_UPDATE_DTO)

        then:
        1 * carRepository.findById(CAR_ID) >> Optional.empty()
        0 * _

        and:
        def exception = thrown(IllegalArgumentException)
        exception.message == 'Wrong car id'
    }

    def 'should throw exception during car update if requester is not owner'() {
        when:
        carService.updateCar(CAR_ID, CAR_UPDATE_DTO)

        then:
        1 * carRepository.findById(CAR_ID) >> Optional.of(CAR)
        1 * securityService.validateOwner(USER_ID_1, Car) >> { throw new SecurityException(EXCEPTION_MESSAGE) }
        0 * _

        and:
        def exception = thrown(SecurityException)
        exception.message == EXCEPTION_MESSAGE
    }


    // --- Car deactivation

    def 'should deactivate car successfully'() {
        given:
        def deactivatedCar = CAR.toBuilder().active(false).build()

        when:
        carService.deactivateCar(CAR_ID)

        then:
        1 * carRepository.findById(CAR_ID) >> Optional.of(CAR)
        1 * securityService.validateOwner(USER_ID_1, Car)
        1 * carRepository.save(deactivatedCar)
        0 * _
    }

    def 'should throw exception during car deactivation if car is not found by provided id'() {
        when:
        carService.deactivateCar(CAR_ID)

        then:
        1 * carRepository.findById(CAR_ID) >> Optional.empty()
        0 * _

        and:
        def exception = thrown(IllegalArgumentException)
        exception.message == 'Wrong car id'
    }

    def 'should throw exception during car deactivation if requester is not owner'() {
        when:
        carService.deactivateCar(CAR_ID)

        then:
        1 * carRepository.findById(CAR_ID) >> Optional.of(CAR)
        1 * securityService.validateOwner(USER_ID_1, Car) >> { throw new SecurityException(EXCEPTION_MESSAGE) }
        0 * _

        and:
        def exception = thrown(SecurityException)
        exception.message == EXCEPTION_MESSAGE
    }


    // --- Image upload

    def 'should upload image successfully'() {
        given:
        def updatedCar = CAR.toBuilder().image(PATH).build()

        when:
        carService.uploadImage(CAR_ID, image)

        then:
        1 * carRepository.findById(CAR_ID) >> Optional.of(CAR)
        1 * securityService.validateOwner(USER_ID_1, Car)
        1 * fileService.uploadImage(ImageCategory.CAR, CAR_ID, image) >> PATH
        1 * carRepository.save(updatedCar)
        0 * _
    }

    def 'should throw exception during image upload if car is not found by provided id'() {
        when:
        carService.uploadImage(CAR_ID, image)

        then:
        1 * carRepository.findById(CAR_ID) >> Optional.empty()
        0 * _

        and:
        def exception = thrown(IllegalArgumentException)
        exception.message == 'Wrong car id'
    }

    def 'should throw exception during image upload if requester is not owner'() {
        when:
        carService.uploadImage(CAR_ID, image)

        then:
        1 * carRepository.findById(CAR_ID) >> Optional.of(CAR)
        1 * securityService.validateOwner(USER_ID_1, Car) >> { throw new SecurityException(EXCEPTION_MESSAGE) }
        0 * _

        and:
        def exception = thrown(SecurityException)
        exception.message == EXCEPTION_MESSAGE
    }
}
