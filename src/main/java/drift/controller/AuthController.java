package drift.controller;

import drift.dto.ResetPasswordDto;
import drift.dto.SignInDto;
import drift.dto.UserActivationDto;
import drift.dto.UserRegistrationDto;
import drift.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/v1/auth")
@Slf4j
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @Operation(summary = "User registration")
    @PostMapping("/registration/step-1")
    public void registerUser(@RequestBody @Valid UserRegistrationDto dto) {
        userService.registerUser(dto);
    }

    @Operation(summary = "User activation")
    @PostMapping("/registration/step-2")
    public void activateUser(@RequestBody @Valid UserActivationDto dto) {
        userService.activateUser(dto);
    }

    @Operation(summary = "Sign in")
    @PostMapping("/sign-in")
    public String signIn(@RequestBody @Valid SignInDto dto) {
        return userService.signIn(dto);
    }

    @Operation(summary = "Reset password")
    @PostMapping("/reset-password")
    public void signIn(@RequestBody @Valid ResetPasswordDto dto) {
        userService.resetPassword(dto);
    }
}
