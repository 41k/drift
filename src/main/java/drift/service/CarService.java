package drift.service;

import drift.dto.CarRegistrationDto;
import drift.dto.CarUpdateDto;
import drift.model.Car;
import drift.model.ImageCategory;
import drift.repository.CarRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.function.Supplier;

@Transactional
@RequiredArgsConstructor
public class CarService {

    private final IdGenerator idGenerator;
    private final SecurityService securityService;
    private final CarRepository carRepository;
    private final FileService fileService;

    public String registerCar(CarRegistrationDto dto) {
        return carRepository.save(
                Car.builder()
                        .id(idGenerator.generate())
                        .ownerId(securityService.getRequesterId())
                        .brand(dto.getBrand())
                        .model(dto.getModel())
                        .power(dto.getPower())
                        .active(true)
                        .build()
        ).getId();
    }

    public void updateCar(String carId, CarUpdateDto dto) {
        var car = getRequesterCar(carId);
        carRepository.save(car.toBuilder().power(dto.getPower()).build());
    }

    public void deactivateCar(String carId) {
        var car = getRequesterCar(carId);
        carRepository.save(car.toBuilder().active(false).build());
    }

    public void uploadImage(String carId, MultipartFile image) {
        var car = getRequesterCar(carId);
        var imagePath = fileService.uploadImage(ImageCategory.CAR, carId, image);
        var updatedCar = car.toBuilder().image(imagePath).build();
        carRepository.save(updatedCar);
    }

    private Car getRequesterCar(String carId) {
        var car = carRepository.findById(carId).orElseThrow(wrongCarIdException());
        securityService.validateOwner(car.getOwnerId(), Car.class);
        return car;
    }

    private Supplier<IllegalArgumentException> wrongCarIdException() {
        return () -> new IllegalArgumentException("Wrong car id");
    }
}
