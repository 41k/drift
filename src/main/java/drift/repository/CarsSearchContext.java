package drift.repository;

import lombok.Data;

import java.util.Collection;

@Data
public class CarsSearchContext {
    private Collection<String> carIds;
    private Collection<String> ownerIds;
    private Boolean active;
}
