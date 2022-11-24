package drift.repository;

import lombok.Data;

import java.util.Collection;

@Data
public class OrganisationsSearchContext {
    private Collection<String> organisationIds;
    private Collection<String> ownerIds;
    private Boolean active;
}
