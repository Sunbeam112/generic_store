package ua.sunbeam.genericstore.api.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import ua.sunbeam.genericstore.api.model.LoginBody;
import ua.sunbeam.genericstore.api.model.LoginResponse;
import ua.sunbeam.genericstore.api.model.RegistrationBody;
import ua.sunbeam.genericstore.error.DataIsNotVerified;
import ua.sunbeam.genericstore.error.EmailFailureException;
import ua.sunbeam.genericstore.error.UserAlreadyExist;
import ua.sunbeam.genericstore.error.UserNotVerifiedException;
import ua.sunbeam.genericstore.model.LocalUser;
import ua.sunbeam.genericstore.service.UserService;


@CrossOrigin(maxAge = 3600)
@RestController
@RequestMapping("/auth/v1")
public class AuthenticationController {

    private final UserService userService;
    private final ValidationErrorsParser validationErrorsParser;

    public AuthenticationController(
            UserService userService,
            ValidationErrorsParser validationErrorsParser) {
        this.userService = userService;

        this.validationErrorsParser = validationErrorsParser;
    }

    @CrossOrigin
    @PostMapping("/register")
    public ResponseEntity<Object> registerUser
            (@Valid @RequestBody RegistrationBody body, BindingResult result) {
        try {
            userService.registerUser(body, result);
            if (result.hasErrors()) {
                throw new DataIsNotVerified(validationErrorsParser.ParseErrorsFrom(result));
            }

            return ResponseEntity.status(HttpStatus.OK).build();
        } catch (UserAlreadyExist ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        } catch (EmailFailureException ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        } catch (DataIsNotVerified ex) {
            return ResponseEntity.badRequest().body(ex.getErrors());
        }
    }

    @CrossOrigin
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> loginUser(@Valid @RequestBody LoginBody loginBody) {
        String jwt;
        try {
            jwt = userService.loginUser(loginBody);

        } catch (UserNotVerifiedException ex) {
            LoginResponse response = new LoginResponse();
            response.setSuccess(false);
            String reason = "USER_NOT_VERIFIED";
            if (ex.isNewEmailSent()) {
                reason += "_NEW_EMAIL_SENT";
            }
            response.setFailureReason(reason);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
        } catch (EmailFailureException ex) {
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


    @CrossOrigin
    @GetMapping("/me")
    public ResponseEntity<LocalUser> userProfile(@AuthenticationPrincipal LocalUser user) {
        return ResponseEntity.ok(user);

    }

    @CrossOrigin
    @PostMapping("/verify")
    public ResponseEntity<LoginResponse> verifyUser(@RequestParam String token) {
        if (userService.verifyUser(token)) {
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.status(HttpStatus.CONFLICT).build();
    }
}
