package ua.sunbeam.genericstore.api.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ua.sunbeam.genericstore.model.LocalUser;
import ua.sunbeam.genericstore.service.OrderService;
import ua.sunbeam.genericstore.service.UserService;

import java.util.Optional;

@RestController
@RequestMapping("/user")
public class UserController {

    private final UserService userService;
    private final OrderService orderService;

    public UserController(UserService userService, OrderService orderService) {
        this.userService = userService;
        this.orderService = orderService;
    }

    /**
     * Retrieves the authenticated user's data.
     * <p>
     * This endpoint is accessible at the `/me` path and requires an authenticated principal.
     * It extracts the user's email from the security context and attempts to retrieve
     * the corresponding user data.
     *
     * @return A {@link ResponseEntity} containing:
     * - {@link LocalUser} object and {@link HttpStatus#OK} if the user data is found.
     * - {@link HttpStatus#UNAUTHORIZED} if no principal is found in the security context.
     * - {@link HttpStatus#BAD_REQUEST} if the extracted user email is empty.
     * - {@link HttpStatus#NOT_FOUND} if the user with the given email is not found.
     */
    @GetMapping("/me")
    public ResponseEntity<LocalUser> getUserData() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (principal == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        String userEmail = principal.toString();

        if (userEmail.trim().isEmpty()) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }


        Optional<LocalUser> localUserOptional = userService.getUserByEmail(userEmail);

        return localUserOptional.map(user -> new ResponseEntity<>(user, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }


}
