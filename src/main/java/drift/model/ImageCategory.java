package drift.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum ImageCategory {

    USER("users"),
    CAR("cars"),
    ORGANISATION("organisations"),
    CHAMPIONSHIP_STAGE("championship-stages"),
    TRAINING("trainings");

    @Getter
    private final String key;
}
