package drift.repository;

import lombok.Data;

import java.util.Collection;

@Data
public class UsersSearchContext {
    private Collection<String> userIds;
    private Boolean active;
}
