package drift.service;

import drift.dto.*;
import drift.model.ImageCategory;
import drift.model.User;
import drift.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.function.Supplier;

@Transactional
@RequiredArgsConstructor
public class UserService {

    private final IdGenerator idGenerator;
    private final UserRepository userRepository;
    private final SecurityService securityService;
    private final MailService mailService;
    private final FileService fileService;

    public void registerUser(UserRegistrationDto dto) {
        var normalizedEmail = normalize(dto.getEmail());
        var existingUser = userRepository.findByEmail(normalizedEmail);
        if (existingUser.filter(User::isActive).isPresent()) {
            throw new IllegalArgumentException("Active user with provided email already exists");
        }
        var userId = existingUser.map(User::getId).orElseGet(idGenerator::generate);
        var user = User.builder()
                .id(userId)
                .email(normalizedEmail)
                .password(securityService.encode(dto.getPassword()))
                .verificationCode(securityService.generateVerificationCode())
                .build();
        userRepository.save(user);
        mailService.sendVerificationMail(user);
    }

    public void activateUser(UserActivationDto dto) {
        var normalizedEmail = normalize(dto.getEmail());
        var user = userRepository.findByEmailAndActive(normalizedEmail, false).orElseThrow(wrongEmailException());
        if (!dto.getVerificationCode().equals(user.getVerificationCode())) {
            throw new IllegalArgumentException("Wrong verification code");
        }
        userRepository.save(user.toBuilder().verificationCode(null).active(true).build());
    }

    public String signIn(SignInDto dto) {
        var normalizedEmail = normalize(dto.getEmail());
        return userRepository.findByEmailAndActive(normalizedEmail, true)
                .filter(user -> securityService.matches(dto.getPassword(), user.getPassword()))
                .map(User::getId)
                .map(securityService::generateAccessToken)
                .orElseThrow(() -> new IllegalArgumentException("Wrong email or password"));
    }

    public void resetPassword(ResetPasswordDto dto) {
        var normalizedEmail = normalize(dto.getEmail());
        var user = userRepository.findByEmailAndActive(normalizedEmail, true).orElseThrow(wrongEmailException());
        var newPassword = idGenerator.generate();
        var encodedNewPassword = securityService.encode(newPassword);
        userRepository.save(user.toBuilder().password(encodedNewPassword).build());
        mailService.sendPasswordResetMail(normalizedEmail, newPassword);
    }

    public UserDto getMe() {
        var requesterId = securityService.getRequesterId();
        return getUser(requesterId);
    }

    public UserDto getUser(String userId) {
        var user = getActiveUser(userId);
        return UserDto.from(user);
    }

    public void updateMe(UserUpdateDto dto) {
        var requesterId = securityService.getRequesterId();
        var userBuilder = getActiveUser(requesterId).toBuilder()
                .firstName(dto.getFirstName())
                .lastName(dto.getLastName())
                .countryCode(dto.getCountryCode())
                .city(dto.getCity());
        if (StringUtils.isNotBlank(dto.getPassword())) {
            userBuilder.password(securityService.encode(dto.getPassword()));
        }
        userRepository.save(userBuilder.build());
    }

    public void uploadImage(MultipartFile image) {
        var requesterId = securityService.getRequesterId();
        var user = getActiveUser(requesterId);
        var imagePath = fileService.uploadImage(ImageCategory.USER, requesterId, image);
        var updatedUser = user.toBuilder().image(imagePath).build();
        userRepository.save(updatedUser);
    }

    public void deactivateMe() {
        var requesterId = securityService.getRequesterId();
        var user = getActiveUser(requesterId);
        userRepository.save(user.toBuilder().active(false).build());
    }

    private User getActiveUser(String userId) {
        return userRepository.findByIdAndActive(userId, true).orElseThrow(wrongUserIdException());
    }

    private Supplier<IllegalArgumentException> wrongUserIdException() {
        return () -> new IllegalArgumentException("Wrong user id");
    }

    private Supplier<IllegalArgumentException> wrongEmailException() {
        return () -> new IllegalArgumentException("Wrong email");
    }

    private String normalize(String stringToNormalize) {
        return stringToNormalize.trim().toLowerCase();
    }
}
