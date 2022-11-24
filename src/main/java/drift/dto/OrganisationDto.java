package drift.dto;

import drift.model.Organisation;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Value;

@Value
@Builder(access = AccessLevel.PROTECTED)
public class OrganisationDto {

    String id;
    String ownerId;
    String name;
    String description;
    String image;
    boolean active;

    public static OrganisationDto from(Organisation organisation) {
        return OrganisationDto.builder()
                .id(organisation.getId())
                .ownerId(organisation.getOwnerId())
                .name(organisation.getName())
                .description(organisation.getDescription())
                .image(organisation.getImage())
                .active(organisation.isActive())
                .build();
    }
}
