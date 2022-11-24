package drift.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class CarRegistrationDto {
    @NotBlank
    private String brand;
    @NotBlank
    private String model;
    private Double power;
}
