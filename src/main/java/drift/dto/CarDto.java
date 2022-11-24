package drift.dto;

import drift.model.Car;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Value;

@Value
@Builder(access = AccessLevel.PROTECTED)
public class CarDto {

    String id;
    String ownerId;
    String brand;
    String model;
    Double power;
    String image;
    boolean active;

    public static CarDto from(Car car) {
        return CarDto.builder()
                .id(car.getId())
                .ownerId(car.getOwnerId())
                .brand(car.getBrand())
                .model(car.getModel())
                .power(car.getPower())
                .image(car.getImage())
                .active(car.isActive())
                .build();
    }
}
