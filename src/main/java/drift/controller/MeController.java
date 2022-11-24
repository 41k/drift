package drift.controller;

import drift.dto.UserDto;
import drift.dto.UserUpdateDto;
import drift.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;

import static drift.controller.AccessTokenAuthenticationFilter.ACCESS_TOKEN_HEADER;

@RestController
@RequestMapping("/api/v1/me")
@RequiredArgsConstructor
public class MeController {

    private final UserService userService;

    @Operation(summary = "Get currently signed in user")
    @GetMapping
    public UserDto get(@RequestHeader(ACCESS_TOKEN_HEADER) String accessToken) {
        return userService.getMe();
    }

    @Operation(summary = "Update currently signed in user")
    @PutMapping
    public void update(@RequestHeader(ACCESS_TOKEN_HEADER) String accessToken,
                       @RequestBody @Valid UserUpdateDto dto) {
        userService.updateMe(dto);
    }

    @Operation(summary = "Deactivate currently signed in user")
    @DeleteMapping
    public void deactivate(@RequestHeader(ACCESS_TOKEN_HEADER) String accessToken) {
        userService.deactivateMe();
    }

    @Operation(summary = "Upload photo of currently signed in user")
    @PostMapping(value = "/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public void uploadImage(@RequestHeader(ACCESS_TOKEN_HEADER) String accessToken,
                            @RequestParam MultipartFile image) {
        userService.uploadImage(image);
    }
}
