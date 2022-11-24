package drift.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class UserUpdateDto {

    private String password;
    private String firstName;
    private String lastName;
    private String countryCode;
    private String city;
}
