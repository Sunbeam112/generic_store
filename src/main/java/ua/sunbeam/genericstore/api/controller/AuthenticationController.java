package ua.sunbeam.genericstore.api.controller;

import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import ua.sunbeam.genericstore.api.model.LoginBody;
import ua.sunbeam.genericstore.api.model.LoginResponse;
import ua.sunbeam.genericstore.api.model.PasswordResetRequestBody;
import ua.sunbeam.genericstore.api.model.RegistrationBody;
import ua.sunbeam.genericstore.error.*;
import ua.sunbeam.genericstore.model.ResetPasswordToken;
import ua.sunbeam.genericstore.service.RPTService;
import ua.sunbeam.genericstore.service.UserService;

import java.util.Optional;


@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/auth/v1")
public class AuthenticationController {

    private final UserService userService;
    private final ValidationErrorsParser validationErrorsParser;
    private final RPTService rtpService;

    public AuthenticationController(UserService userService,
                                    ValidationErrorsParser validationErrorsParser,
                                    RPTService rptService) {
        this.userService = userService;
        this.validationErrorsParser = validationErrorsParser;
        this.rtpService = rptService;
    }


    @CrossOrigin
    @PostMapping("/register")
    /**
     * Registers a new user in the system.
     *
     * @param body The registration body containing user details.
     * @param result The binding result for validation errors.
     * @return A ResponseEntity indicating the outcome of the registration attempt.
     * - HttpStatus.CREATED (201) if the user is successfully registered.
     * - HttpStatus.CONFLICT (409) if a user with the provided email already exists.
     * - HttpStatus.INTERNAL_SERVER_ERROR (500) if there's an issue sending the verification email.
     * - HttpStatus.BAD_REQUEST (400) if the provided details are not valid.
     */
    public ResponseEntity<Object> registerUser
            (@Valid @RequestBody RegistrationBody body, BindingResult result) {
        try {
            userService.registerUser(body, result);
            if (result.hasErrors()) {
                throw new DetaiIsNotVerified(validationErrorsParser.parseErrorsFrom(result));
            }

            return new ResponseEntity<>(HttpStatus.CREATED);
        } catch (UserAlreadyExist e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        } catch (EmailFailureException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        } catch (DetaiIsNotVerified e) {
            return ResponseEntity.badRequest().body(e.getErrors());
        }
    }

    @CrossOrigin
    @PostMapping("/login")
    /**
     * Authenticates a user and provides a JWT upon successful login.
     *
     * @param loginBody The login body containing user credentials (email and password).
     * @return A ResponseEntity containing a LoginResponse with a JWT if successful,
     * or an error status and reason if authentication fails.
     * - HttpStatus.OK (200) with a LoginResponse containing the JWT if login is successful.
     * - HttpStatus.FORBIDDEN (403) if the user is not verified, with a reason.
     * - HttpStatus.INTERNAL_SERVER_ERROR (500) if there's an issue with email services.
     * - HttpStatus.BAD_REQUEST (400) for general authentication failures or invalid input.
     */
    public ResponseEntity<LoginResponse> loginUser(@Valid @RequestBody LoginBody loginBody) {
        String jwt;
        try {
            jwt = userService.loginUser(loginBody);

        } catch (UserNotVerifiedException e) {
            LoginResponse response = new LoginResponse();
            response.setSuccess(false);
            String reason = "USER_NOT_VERIFIED";
            if (e.isNewEmailSent()) {
                reason += "_NEW_EMAIL_SENT";
            }
            response.setFailureReason(reason);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
        } catch (EmailFailureException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        if (jwt == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } else {
            LoginResponse response = new LoginResponse();
            response.setToken(jwt);
            response.setSuccess(true);
            return ResponseEntity.ok(response);
        }

    }


    @PostMapping("/logout")
    /**
     * Handles user logout. This is primarily a client-side action where the client discards the token.
     * The server simply acknowledges the logout request.
     *
     * @return A ResponseEntity with HttpStatus.OK (200) indicating successful acknowledgement.
     */
    public ResponseEntity<Void> logoutUser() {
        System.out.println("User has initiated a logout (client-side token discard expected).");
        return ResponseEntity.ok().build();
    }


    @GetMapping("/me")
    /**
     * Retrieves the details of the authenticated user.
     *
     * @return The UserDetails object representing the currently authenticated user.
     */
    public UserDetails getUserData() {
        return userService.getUserByEmail((String) SecurityContextHolder.getContext().getAuthentication().getPrincipal());
    }


    @CrossOrigin
    @PostMapping("/verify")
    /**
     * Verifies a user's email address using a provided token.
     *
     * @param token The verification token sent to the user's email.
     * @return A ResponseEntity indicating the outcome of the verification.
     * - HttpStatus.OK (200) if the user is successfully verified.
     * - HttpStatus.CONFLICT (409) if the token is invalid or expired, or the user cannot be verified.
     */
    public ResponseEntity<LoginResponse> verifyUser(@RequestParam String token) {
        if (userService.verifyUser(token)) {
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.status(HttpStatus.CONFLICT).build();
    }


    @CrossOrigin
    @PostMapping("/forgot_password")
    /**
     * Initiates the forgotten password process by sending a reset password email to the provided email address.
     *
     * @param email The email address of the user who forgot their password.
     * @return A ResponseEntity indicating the outcome of the request.
     * - HttpStatus.OK (200) if the reset password email is successfully sent.
     * - HttpStatus.BAD_REQUEST (400) if the email is not provided.
     * - HttpStatus.NOT_FOUND (404) if no user exists with the provided email.
     * - HttpStatus.CONFLICT (409) if the email is not verified or a password reset is on cooldown.
     * - HttpStatus.INTERNAL_SERVER_ERROR (500) if there's an issue sending the email.
     */
    public ResponseEntity<Object> forgotPassword(@RequestParam String email) {
        if (email == null || email.trim().isBlank()) {
            return ResponseEntity.badRequest().body("EMAIL_NOT_PROVIDED");
        }
        if (!userService.isUserExistsByEmail(email)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("EMAIL_NOT_FOUND");
        }
        try {
            userService.trySendResetPasswordEmail(email);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (EmailsNotVerifiedException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("EMAIL_NOT_VERIFIED");
        } catch (EmailFailureException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        } catch (PasswordResetCooldown e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("PASSWORD_RESET_COOLDOWN");
        }
    }

    @CrossOrigin
    @PostMapping("/reset_password")
    /**
     * Resets the user's password using a valid reset password token.
     *
     * @param resetBody The request body containing the reset token and the new password.
     * @param result The binding result for validation errors of the new password.
     * @return A ResponseEntity indicating the outcome of the password reset attempt.
     * - HttpStatus.OK (200) if the password is successfully changed.
     * - HttpStatus.BAD_REQUEST (400) if the token is invalid or expired, or if the new password fails validation.
     * - HttpStatus.CONFLICT (409) if the user's email is not verified.
     * - HttpStatus.INTERNAL_SERVER_ERROR (500) if an unexpected error occurs during the password change.
     */
    public ResponseEntity<Object> changeUserPassword(@Valid @RequestBody PasswordResetRequestBody resetBody, BindingResult result) {
        Optional<ResetPasswordToken> opToken = rtpService.verifyAndGetRPT(resetBody.getToken());

        if (opToken.isPresent()) {
            ResetPasswordToken token = opToken.get();
            try {

                boolean isPasswordChanged = userService.setUserPasswordByEmail(
                        token.getLocalUser().getEmail(), resetBody.getNewPassword(), result);

                if (result.hasErrors()) {

                    throw new DetaiIsNotVerified(validationErrorsParser.parseErrorsFrom(result));
                }

                if (isPasswordChanged) {

                    rtpService.markTokenAsUsed(token);
                    return ResponseEntity.ok().build();
                } else {

                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("PASSWORD_CHANGE_FAILED");
                }

            } catch (EmailsNotVerifiedException e) {

                return ResponseEntity.status(HttpStatus.CONFLICT).body("EMAIL_NOT_VERIFIED");
            } catch (DetaiIsNotVerified e) {
                return ResponseEntity.badRequest().body(e.getErrors());
            } catch (Exception e) {
                System.err.println("Error during password reset: " + e.getMessage());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("AN_UNEXPECTED_ERROR_OCCURRED");
            }
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("INVALID_OR_EXPIRED_TOKEN");
        }
    }
}