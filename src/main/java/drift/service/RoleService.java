package drift.service;

import drift.model.Role;
import drift.repository.CarRepository;
import drift.repository.OrganisationRepository;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.Collection;

@RequiredArgsConstructor
public class RoleService {

    private final CarRepository carRepository;
    private final OrganisationRepository organisationRepository;

    public Collection<Role> getRoles(String userId) {
        return new ArrayList<>() {{
            add(Role.SPECTATOR);
            if (carRepository.existsByOwnerIdAndActive(userId, true)) {
                add(Role.DRIVER);
            }
            if (organisationRepository.existsByOwnerIdAndActive(userId, true)) {
                add(Role.ORGANIZER);
            }
        }};
    }

    public boolean userHasRoles(String userId, Collection<Role> roles) {
        return getRoles(userId).containsAll(roles);
    }
}
