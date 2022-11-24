package drift.controller;

import drift.model.Role;
import drift.service.RoleService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;

import static drift.controller.AccessTokenAuthenticationFilter.ACCESS_TOKEN_HEADER;

@RestController
@RequestMapping("/api/v1/users/{userId}/roles")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;

    @Operation(summary = "Get user roles")
    @GetMapping
    public Collection<Role> getRoles(@RequestHeader(ACCESS_TOKEN_HEADER) String accessToken,
                                     @PathVariable String userId) {
        return roleService.getRoles(userId);
    }
}
