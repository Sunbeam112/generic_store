package ua.sunbeam.genericstore.api.controller;

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


@CrossOrigin(maxAge = 3600)
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
    public ResponseEntity<Void> logoutUser() {
        System.out.println("User has initiated a logout (client-side token discard expected).");
        return ResponseEntity.ok().build();
    }


    @GetMapping("/me")
    public UserDetails getUserData() {
        return userService.getUserByEmail((String) SecurityContextHolder.getContext().getAuthentication().getPrincipal());
    }


    @CrossOrigin
    @PostMapping("/verify")
    public ResponseEntity<LoginResponse> verifyUser(@RequestParam String token) {
        if (userService.verifyUser(token)) {
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.status(HttpStatus.CONFLICT).build();
    }


    @CrossOrigin
    @PostMapping("/forgot_password")
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
