package drift.controller;

import drift.dto.CarRegistrationDto;
import drift.dto.CarUpdateDto;
import drift.service.CarService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;

import static drift.controller.AccessTokenAuthenticationFilter.ACCESS_TOKEN_HEADER;

@RestController
@RequestMapping("/api/v1/cars")
@RequiredArgsConstructor
public class CarController {

    private final CarService carService;

    @Operation(summary = "Create car")
    @PostMapping
    public String registerCar(@RequestHeader(ACCESS_TOKEN_HEADER) String accessToken,
                              @RequestBody @Valid CarRegistrationDto dto) {
        return carService.registerCar(dto);
    }

    @Operation(summary = "Update car")
    @PutMapping("/{carId}")
    public void updateCar(@RequestHeader(ACCESS_TOKEN_HEADER) String accessToken,
                          @PathVariable String carId,
                          @RequestBody @Valid CarUpdateDto dto) {
        carService.updateCar(carId, dto);
    }

    @Operation(summary = "Deactivate car")
    @DeleteMapping("/{carId}")
    public void deactivateCar(@RequestHeader(ACCESS_TOKEN_HEADER) String accessToken,
                              @PathVariable String carId) {
        carService.deactivateCar(carId);
    }

    @Operation(summary = "Upload car image")
    @PostMapping(value = "/{carId}/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public void uploadImage(@RequestHeader(ACCESS_TOKEN_HEADER) String accessToken,
                            @PathVariable String carId,
                            @RequestParam MultipartFile image) {
        carService.uploadImage(carId, image);
    }
}
