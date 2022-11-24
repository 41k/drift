package drift.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.Claim;
import drift.configuration.properties.SecurityProperties;
import drift.model.Role;
import lombok.SneakyThrows;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Clock;
import java.util.*;

public class SecurityService {

    private static final String USER_ID = "userId";

    private final SecurityProperties properties;
    private final RoleService roleService;
    private final Clock clock;
    private final PasswordEncoder encoder;
    private final Random randomGenerator;
    private final Algorithm tokenKey;
    private final JWTVerifier tokenVerifier;

    @SneakyThrows
    public SecurityService(SecurityProperties properties, RoleService roleService, Clock clock) {
        this.properties = properties;
        this.roleService = roleService;
        this.clock = clock;
        this.encoder = new BCryptPasswordEncoder();
        this.randomGenerator = new Random();
        this.tokenKey = Algorithm.HMAC256(properties.getTokenKey());
        this.tokenVerifier = ((JWTVerifier.BaseVerification) JWT.require(tokenKey)).build(() -> new Date(clock.millis()));
    }

    public String encode(String value) {
        return encoder.encode(value);
    }

    public boolean matches(String value, String encodedValue) {
        return encoder.matches(value, encodedValue);
    }

    public String generateVerificationCode() {
        return String.format("%04d", randomGenerator.nextInt(9999));
    }

    public String generateAccessToken(String userId) {
        return JWT.create()
                .withPayload(Map.of(
                        USER_ID, userId
                ))
                .withExpiresAt(new Date(clock.millis() + properties.getTokenTtlInMillis()))
                .sign(tokenKey);
    }

    public void setupSecurityContext(String accessToken) {
        try {
            var decodedAccessToken = tokenVerifier.verify(accessToken);
            var userId = Optional.ofNullable(decodedAccessToken.getClaim(USER_ID))
                    .map(Claim::asString)
                    .orElseThrow();
            var authorities = List.<GrantedAuthority>of();
            var principal = new User(userId, userId, authorities);
            var authentication = new UsernamePasswordAuthenticationToken(principal, accessToken, authorities);
            SecurityContextHolder.getContext().setAuthentication(authentication);
        } catch (Exception e) {
            SecurityContextHolder.clearContext();
        }
    }

    public void validateOwner(String ownerId, Class<?> modelClass) {
        if (!ownerId.equals(getRequesterId())) {
            throw new SecurityException("Requester should be owner of " + modelClass.getSimpleName());
        }
    }

    public String getRequesterId() {
        return getUserDetails().map(UserDetails::getUsername).orElseThrow(SecurityException::new);
    }

    public void validateRequesterRoles(Role... roles) {
        var userId = getRequesterId();
        var listOfRoles = List.of(roles);
        if (!roleService.userHasRoles(userId, listOfRoles)) {
            throw new SecurityException("Roles " + listOfRoles.toString() + " are required");
        }
    }

    private Optional<UserDetails> getUserDetails() {
        return Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
                .map(Authentication::getPrincipal)
                .map(principal -> (UserDetails) principal);
    }
}
