package drift.dto;

import drift.model.User;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Value;

@Value
@Builder(access = AccessLevel.PROTECTED)
public class UserDto {

    String id;
    String email;
    String firstName;
    String lastName;
    String countryCode;
    String city;
    String image;
    boolean active;

    public static UserDto from(User user) {
        return UserDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .countryCode(user.getCountryCode())
                .city(user.getCity())
                .image(user.getImage())
                .active(user.isActive())
                .build();
    }
}
