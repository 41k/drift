package drift.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class OrganisationRegistrationDto {
    @NotBlank
    private String name;
    private String description;
}
