package ua.sunbeam.genericstore.api.controller;

import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import ua.sunbeam.genericstore.api.model.LoginBody;
import ua.sunbeam.genericstore.api.model.LoginResponse;
import ua.sunbeam.genericstore.api.model.PasswordResetRequestBody;
import ua.sunbeam.genericstore.api.model.RegistrationBody;
import ua.sunbeam.genericstore.error.*;
import ua.sunbeam.genericstore.model.LocalUser;
import ua.sunbeam.genericstore.model.ResetPasswordToken;
import ua.sunbeam.genericstore.service.RPTService;
import ua.sunbeam.genericstore.service.UserService;


@CrossOrigin(maxAge = 3600)
@RestController
@RequestMapping("/auth/v1")
public class AuthenticationController {

    private final UserService userService;
    private final ValidationErrorsParser validationErrorsParser;
    private final RPTService rtpService;

    public AuthenticationController(UserService userService,
                                    ValidationErrorsParser validationErrorsParser,
                                    RPTService RPTService) {
        this.userService = userService;
        this.validationErrorsParser = validationErrorsParser;
        this.rtpService = RPTService;
    }

    @Transactional
    @CrossOrigin
    @PostMapping("/register")
    public ResponseEntity<Object> registerUser
            (@Valid @RequestBody RegistrationBody body, BindingResult result) {
        try {
            userService.registerUser(body, result);
            if (result.hasErrors()) {
                throw new DetaiIsNotVerified(validationErrorsParser.ParseErrorsFrom(result));
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


    @GetMapping("/logout")
    public ResponseEntity<String> logoutUser() {
        return new ResponseEntity<>(HttpStatus.OK);
    }


    @GetMapping("/me")
    public UserDetails getUserData() {
        return userService.GetUserByEmail((String) SecurityContextHolder.getContext().getAuthentication().getPrincipal());
    }

    @Transactional
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
    public ResponseEntity<Object> forgotPassword(@RequestParam String email) throws EmailsNotVerifiedException {
        try {
            ResetPasswordToken token = userService.ResetPassword(email);
            return new ResponseEntity<>(token, HttpStatus.OK);
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
        boolean isValid = rtpService.VerifyRPT(resetBody.getToken());
        if (isValid) {
            ResetPasswordToken token = rtpService.getTokenByToken(resetBody.getToken());
            try {
                boolean isPasswordChanged = userService.SetUserPasswordByEmail(
                        token.getLocalUser().getEmail(), resetBody.getNewPassword(), result);
                if (result.hasErrors()) {
                    throw new DetaiIsNotVerified(validationErrorsParser.ParseErrorsFrom(result));
                }
                if (isPasswordChanged) {
                    rtpService.RemoveToken(token);
                    return ResponseEntity.ok().build();
                }

            } catch (EmailsNotVerifiedException e) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body("EMAIL_NOT_VERIFIED");
            } catch (DetaiIsNotVerified e) {
                return ResponseEntity.badRequest().body(e.getErrors());
            }
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }
}
