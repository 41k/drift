package drift.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class OrganisationUpdateDto {
    private String description;
}
