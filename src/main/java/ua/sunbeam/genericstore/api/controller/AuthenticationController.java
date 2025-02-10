package ua.sunbeam.genericstore.api.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import ua.sunbeam.genericstore.api.model.LoginBody;
import ua.sunbeam.genericstore.api.model.LoginResponse;
import ua.sunbeam.genericstore.api.model.RegistrationBody;
import ua.sunbeam.genericstore.error.EmailFailureException;
import ua.sunbeam.genericstore.error.UserAlreadyExist;
import ua.sunbeam.genericstore.error.UserNotVerifiedException;
import ua.sunbeam.genericstore.model.LocalUser;
import ua.sunbeam.genericstore.service.UserDetailsService;
import ua.sunbeam.genericstore.service.UserService;


@CrossOrigin(maxAge = 3600)
@RestController
@RequestMapping("/auth/v1")
public class AuthenticationController {

    private final UserService userService;


    public AuthenticationController(UserService userService, UserDetailsService userDetailsService) {
        this.userService = userService;
    }

    @CrossOrigin
    @PostMapping("/register")
    public ResponseEntity<String> registerUser(@Valid @RequestBody RegistrationBody body) throws UserAlreadyExist, EmailFailureException {
        try {
            userService.registerUser(body);
            return ResponseEntity.status(HttpStatus.OK).build();
        } catch (UserAlreadyExist ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        } catch (EmailFailureException ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        } catch (MethodArgumentNotValidException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
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
