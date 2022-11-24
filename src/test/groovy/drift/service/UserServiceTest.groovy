package drift.service

import drift.dto.UserDto
import drift.model.ImageCategory
import drift.model.User
import drift.repository.UserRepository
import org.springframework.web.multipart.MultipartFile
import spock.lang.Specification

import static drift.util.TestConstants.*

class UserServiceTest extends Specification {

    private idGenerator = Mock(IdGenerator)
    private userRepository = Mock(UserRepository)
    private securityService = Mock(SecurityService)
    private mailService = Mock(MailService)
    private fileService = Mock(FileService)
    private image = Mock(MultipartFile)

    private userService = new UserService(idGenerator, userRepository, securityService, mailService, fileService)


    // --- User registration

    def 'should register user successfully'() {
        given:
        def userToRegister = User.builder()
                .id(USER_ID_1).email(NORMALIZED_EMAIL)
                .password(PASSWORD_1_ENCODED).verificationCode(VERIFICATION_CODE).build()

        when:
        userService.registerUser(USER_REGISTRATION_DTO)

        then:
        1 * userRepository.findByEmail(NORMALIZED_EMAIL) >> Optional.empty()
        1 * idGenerator.generate() >> USER_ID_1
        1 * securityService.encode(PASSWORD_1) >> PASSWORD_1_ENCODED
        1 * securityService.generateVerificationCode() >> VERIFICATION_CODE
        1 * userRepository.save(userToRegister)
        1 * mailService.sendVerificationMail(userToRegister)
        0 * _
    }

    def 'should re-register deactivated user with the same email'() {
        given:
        def deactivatedUser = User.builder()
                .id(USER_ID_1).email(NORMALIZED_EMAIL).password(PASSWORD_2_ENCODED)
                .countryCode(COUNTRY_CODE).active(false).build()
        def userToRegister = User.builder()
                .id(USER_ID_1).email(NORMALIZED_EMAIL)
                .password(PASSWORD_1_ENCODED).verificationCode(VERIFICATION_CODE).build()

        when:
        userService.registerUser(USER_REGISTRATION_DTO)

        then:
        1 * userRepository.findByEmail(NORMALIZED_EMAIL) >> Optional.of(deactivatedUser)
        1 * securityService.encode(PASSWORD_1) >> PASSWORD_1_ENCODED
        1 * securityService.generateVerificationCode() >> VERIFICATION_CODE
        1 * userRepository.save(userToRegister)
        1 * mailService.sendVerificationMail(userToRegister)
        0 * _
    }

    def 'should not register user if active user with the same email already exists'() {
        given:
        def activeUser = User.builder().active(true).build()

        when:
        userService.registerUser(USER_REGISTRATION_DTO)

        then:
        1 * userRepository.findByEmail(NORMALIZED_EMAIL) >> Optional.of(activeUser)
        0 * _

        and:
        def exception = thrown(IllegalArgumentException)
        exception.message == 'Active user with provided email already exists'
    }


    // --- User activation

    def 'should activate user successfully'() {
        given:
        def registeredUser = User.builder()
                .id(USER_ID_1).email(NORMALIZED_EMAIL)
                .password(PASSWORD_1_ENCODED).verificationCode(VERIFICATION_CODE).active(false).build()
        def activatedUser = registeredUser.toBuilder().verificationCode(null).active(true).build()

        when:
        userService.activateUser(USER_ACTIVATION_DTO)

        then:
        1 * userRepository.findByEmailAndActive(NORMALIZED_EMAIL, false) >> Optional.of(registeredUser)
        1 * userRepository.save(activatedUser)
        0 * _
    }

    def 'should throw exception during user activation if registered and not activated user is not found'() {
        when:
        userService.activateUser(USER_ACTIVATION_DTO)

        then:
        1 * userRepository.findByEmailAndActive(NORMALIZED_EMAIL, false) >> Optional.empty()
        0 * _

        and:
        def exception = thrown(IllegalArgumentException)
        exception.message == 'Wrong email'
    }

    def 'should throw exception during user activation if wrong verification code is provided'() {
        given:
        def registeredUser = User.builder()
            .id(USER_ID_1).email(NORMALIZED_EMAIL)
            .password(PASSWORD_1_ENCODED).verificationCode('0015').active(false).build()

        when:
        userService.activateUser(USER_ACTIVATION_DTO)

        then:
        1 * userRepository.findByEmailAndActive(NORMALIZED_EMAIL, false) >> Optional.of(registeredUser)
        0 * _

        and:
        def exception = thrown(IllegalArgumentException)
        exception.message == 'Wrong verification code'
    }


    // --- Sign in

    def 'should return access token as a result of successful sign in'() {
        given:
        def user = User.builder().id(USER_ID_1).password(PASSWORD_1_ENCODED).build()

        when:
        def accessToken = userService.signIn(SIGN_IN_DTO)

        then:
        1 * userRepository.findByEmailAndActive(NORMALIZED_EMAIL, true) >> Optional.of(user)
        1 * securityService.matches(PASSWORD_1, PASSWORD_1_ENCODED) >> true
        1 * securityService.generateAccessToken(USER_ID_1) >> ACCESS_TOKEN
        0 * _

        and:
        accessToken == ACCESS_TOKEN
    }

    def 'should throw exception during sign in if active user is not found by email'() {
        when:
        userService.signIn(SIGN_IN_DTO)

        then:
        1 * userRepository.findByEmailAndActive(NORMALIZED_EMAIL, true) >> Optional.empty()
        0 * _

        and:
        def exception = thrown(IllegalArgumentException)
        exception.message == 'Wrong email or password'
    }

    def 'should throw exception during sign in if password is wrong'() {
        given:
        def user = User.builder().id(USER_ID_1).password(PASSWORD_2_ENCODED).build()

        when:
        userService.signIn(SIGN_IN_DTO)

        then:
        1 * userRepository.findByEmailAndActive(NORMALIZED_EMAIL, true) >> Optional.of(user)
        1 * securityService.matches(PASSWORD_1, PASSWORD_2_ENCODED) >> false
        0 * _

        and:
        def exception = thrown(IllegalArgumentException)
        exception.message == 'Wrong email or password'
    }


    // --- Reset password

    def 'should reset password successfully'() {
        given:
        def user = User.builder().password(PASSWORD_1_ENCODED).build()
        def userWithNewPassword = User.builder().password(PASSWORD_2_ENCODED).build()

        when:
        userService.resetPassword(RESET_PASSWORD_DTO)

        then:
        1 * userRepository.findByEmailAndActive(NORMALIZED_EMAIL, true) >> Optional.of(user)
        1 * idGenerator.generate() >> PASSWORD_2
        1 * securityService.encode(PASSWORD_2) >> PASSWORD_2_ENCODED
        1 * userRepository.save(userWithNewPassword)
        1 * mailService.sendPasswordResetMail(NORMALIZED_EMAIL, PASSWORD_2)
        0 * _
    }

    def 'should throw exception during password reset if active user is not found by provided email'() {
        when:
        userService.resetPassword(RESET_PASSWORD_DTO)

        then:
        1 * userRepository.findByEmailAndActive(NORMALIZED_EMAIL, true) >> Optional.empty()
        0 * _

        and:
        def exception = thrown(IllegalArgumentException)
        exception.message == 'Wrong email'
    }


    // --- User retrieval

    def 'should retrieve user by id'() {
        given:
        def user = User.builder().id(USER_ID_1).email(NORMALIZED_EMAIL).password(PASSWORD_1_ENCODED).active(true).build()
        def userDto = UserDto.from(user)

        when:
        def retrievedUserDto = userService.getUser(USER_ID_1)

        then:
        1 * userRepository.findByIdAndActive(USER_ID_1, true) >> Optional.of(user)
        0 * _

        and:
        retrievedUserDto == userDto
    }

    def 'should throw exception during user retrieval if active user is not found by provided id'() {
        when:
        userService.getUser(USER_ID_1)

        then:
        1 * userRepository.findByIdAndActive(USER_ID_1, true) >> Optional.empty()
        0 * _

        and:
        def exception = thrown(IllegalArgumentException)
        exception.message == 'Wrong user id'
    }


    // --- Requester's user retrieval

    def "should retrieve requester's user"() {
        given:
        def user = User.builder().id(USER_ID_1).email(NORMALIZED_EMAIL).password(PASSWORD_1_ENCODED).active(true).build()
        def userDto = UserDto.from(user)

        when:
        def retrievedUserDto = userService.getMe()

        then:
        1 * securityService.getRequesterId() >> USER_ID_1
        1 * userRepository.findByIdAndActive(USER_ID_1, true) >> Optional.of(user)
        0 * _

        and:
        retrievedUserDto == userDto
    }

    def "should throw exception during requester's user retrieval if user is not found by user id"() {
        when:
        userService.getMe()

        then:
        1 * securityService.getRequesterId() >> USER_ID_1
        1 * userRepository.findByIdAndActive(USER_ID_1, true) >> Optional.empty()
        0 * _

        and:
        def exception = thrown(IllegalArgumentException)
        exception.message == 'Wrong user id'
    }


    // --- Requester's user update

    def "should update requester's user successfully"() {
        given:
        def user = User.builder().id(USER_ID_1).email(NORMALIZED_EMAIL).password(PASSWORD_1_ENCODED).build()
        def updatedUser = User.builder()
                .id(USER_ID_1).email(NORMALIZED_EMAIL).password(PASSWORD_2_ENCODED)
                .firstName(FIRST_NAME).lastName(LAST_NAME).countryCode(COUNTRY_CODE).city(CITY).build()

        when:
        userService.updateMe(USER_UPDATE_DTO)

        then:
        1 * securityService.getRequesterId() >> USER_ID_1
        1 * userRepository.findByIdAndActive(USER_ID_1, true) >> Optional.of(user)
        1 * securityService.encode(PASSWORD_2) >> PASSWORD_2_ENCODED
        1 * userRepository.save(updatedUser)
        0 * _
    }

    def "should throw exception during requester's user update if user is not found by provided id"() {
        when:
        userService.updateMe(USER_UPDATE_DTO)

        then:
        1 * securityService.getRequesterId() >> USER_ID_1
        1 * userRepository.findByIdAndActive(USER_ID_1, true) >> Optional.empty()
        0 * _

        and:
        def exception = thrown(IllegalArgumentException)
        exception.message == 'Wrong user id'
    }


    // --- Requester's user deactivation

    def "should deactivate requester's user successfully"() {
        given:
        def user = User.builder().active(true).build()
        def deactivatedUser = User.builder().active(false).build()

        when:
        userService.deactivateMe()

        then:
        1 * securityService.getRequesterId() >> USER_ID_1
        1 * userRepository.findByIdAndActive(USER_ID_1, true) >> Optional.of(user)
        1 * userRepository.save(deactivatedUser)
        0 * _
    }

    def "should throw exception during requester's user deactivation if user is not found by provided id"() {
        when:
        userService.deactivateMe()

        then:
        1 * securityService.getRequesterId() >> USER_ID_1
        1 * userRepository.findByIdAndActive(USER_ID_1, true) >> Optional.empty()
        0 * _

        and:
        def exception = thrown(IllegalArgumentException)
        exception.message == 'Wrong user id'
    }


    // --- Image upload

    def 'should upload image successfully'() {
        given:
        def user = User.builder().build()
        def updatedUser = user.toBuilder().image(PATH).build()

        when:
        userService.uploadImage(image)

        then:
        1 * securityService.getRequesterId() >> USER_ID_1
        1 * userRepository.findByIdAndActive(USER_ID_1, true) >> Optional.of(user)
        1 * fileService.uploadImage(ImageCategory.USER, USER_ID_1, image) >> PATH
        1 * userRepository.save(updatedUser)
        0 * _
    }

    def "should throw exception during image upload if requester's user is not found"() {
        when:
        userService.uploadImage(image)

        then:
        1 * securityService.getRequesterId() >> USER_ID_1
        1 * userRepository.findByIdAndActive(USER_ID_1, true) >> Optional.empty()
        0 * _

        and:
        def exception = thrown(IllegalArgumentException)
        exception.message == 'Wrong user id'
    }
}
