package com.quashbugs.quash.controller;

import com.quashbugs.quash.dto.request.ResetPasswordRequestDTO;
import com.quashbugs.quash.dto.request.SignInRequestDTO;
import com.quashbugs.quash.dto.request.SignUpRequestDTO;
import com.quashbugs.quash.dto.response.ResponseDTO;
import com.quashbugs.quash.dto.response.UserExistenceResponseDTO;
import com.quashbugs.quash.model.User;
import com.quashbugs.quash.service.AuthenticationService;
import com.quashbugs.quash.service.JwtService;
import com.quashbugs.quash.service.UserService;
import io.jsonwebtoken.Claims;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationService authenticationService;

    private final UserService userService;

    private final JwtService jwtService;

    @Value("${spring.frontend.url}")
    private String frontendBaseUrl;

    @Autowired
    public AuthController(AuthenticationService authenticationService, UserService userService, JwtService jwtService) {
        this.authenticationService = authenticationService;
        this.userService = userService;
        this.jwtService = jwtService;
    }

    /**
     * Handles user signup.
     *
     * @param request The signup request containing user information.
     * @return ResponseEntity with a ResponseDTO indicating success or failure.
     */
    @PostMapping("/signup")
    public ResponseEntity<ResponseDTO> signup(@RequestBody SignUpRequestDTO request) {
        try {
            if (request.getWorkEmail() == null || request.getWorkEmail().isEmpty() || request.getPassword() == null || request.getPassword().isEmpty()) {
                return new ResponseEntity<>(new ResponseDTO(false, "Work email and password is mandatory.", null), HttpStatus.BAD_REQUEST);
            } else if (request.getPassword().length() < 8 || request.getPassword().length() > 20) {
                return new ResponseEntity<>(new ResponseDTO(false, "Password must be between 8 and 20 characters.", null), HttpStatus.BAD_REQUEST);
            } else {
                var token = authenticationService.signup(request);
                authenticationService.sendEmail("VERIFY", (String) token.get("token"));
                return new ResponseEntity<>(new ResponseDTO(true, "User successfully added", token), HttpStatus.CREATED);
            }
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(new ResponseDTO(false, e.getMessage(), null), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return new ResponseEntity<>(new ResponseDTO(false, "An error occurred while signing up: " + e.getMessage(), null), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Handles user sign-in.
     *
     * @param request The sign-in request containing user credentials.
     * @return ResponseEntity with a ResponseDTO indicating success or failure.
     */
    @PostMapping("/signin")
    public ResponseEntity<ResponseDTO> signIn(@RequestBody SignInRequestDTO request) {
        try {
            var map = authenticationService.signIn(request);
            return new ResponseEntity<>(new ResponseDTO(true, "User successfully logged in", map), HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(new ResponseDTO(false, e.getMessage(), null), HttpStatus.UNAUTHORIZED);
        } catch (Exception e) {
            return new ResponseEntity<>(new ResponseDTO(false, "An error occurred: " + e.getMessage(), null), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Verifies a user's account using a verification token.
     *
     * @param token    The verification token.
     * @param response The HTTP response for redirection.
     * @throws IOException If an I/O error occurs during redirection.
     */
    @GetMapping(value = "/verify", params = "token")
    public void verifyAccount(@RequestParam("token") String token, HttpServletResponse response) throws IOException {
        try {
            boolean res = authenticationService.verifyEmail(token);
            if (res) {
                response.sendRedirect(frontendBaseUrl + "/onboarding");
            } else {
                response.sendRedirect(frontendBaseUrl + "/404");
            }
        } catch (Exception e) {
            response.sendRedirect(frontendBaseUrl + "/404");
        }
    }

    /**
     * Sends a reset password email to the user.
     *
     * @param workEmail The user's work email.
     * @return ResponseEntity with a ResponseDTO indicating success or failure.
     */
    @GetMapping(value = "/forgot_password", params = "workEmail")
    public ResponseEntity<ResponseDTO> forgotPassword(@RequestParam("workEmail") String workEmail) {
        try {
            authenticationService.sendEmail("FORGOT_PASSWORD", workEmail);
            return new ResponseEntity<>(new ResponseDTO(true, "Reset password sent", null), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(new ResponseDTO(false, "An error occurred while signing up: " + e.getMessage(), null), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Redirects the user to the password reset page.
     *
     * @param authToken The authentication token.
     * @param response  The HTTP response for redirection.
     * @throws IOException If an I/O error occurs during redirection.
     */
    @GetMapping(value = "/reset_password", params = "authToken")
    public void resetPassword(@RequestParam("authToken") String authToken, HttpServletResponse response) throws IOException {
        try {
            var user = authenticationService.getUserByToken(authToken);
            if (user != null) {
                response.sendRedirect(frontendBaseUrl + "/forgot-password/new-password?authToken=" + authToken);
            } else {
                response.sendRedirect(frontendBaseUrl + "/404");
            }
        } catch (Exception e) {
            response.sendRedirect(frontendBaseUrl + "/404");
        }
    }

    /**
     * Changes the user's password.
     *
     * @param authentication          The authentication object containing user information.
     * @param resetPasswordRequestDTO The request containing the new password.
     * @return ResponseEntity with a ResponseDTO indicating success or failure.
     */
    @PostMapping(value = "/reset_password")
    @SecurityRequirement(name = "jwtAuth")
    public ResponseEntity<ResponseDTO> changePassword(Authentication authentication, @RequestBody ResetPasswordRequestDTO resetPasswordRequestDTO) {
        try {
            User user = (User) authentication.getPrincipal();
            if (user != null) {
                authenticationService.changePassword(user, resetPasswordRequestDTO);
                return new ResponseEntity<>(new ResponseDTO(true, "Password Changed successfully", null), HttpStatus.OK);
            }
            return new ResponseEntity<>(new ResponseDTO(false, "User not found", null), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return new ResponseEntity<>(new ResponseDTO(false, "An error occurred: ", e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Sends a verification email to the user.
     *
     * @param authToken The authentication token.
     * @return ResponseEntity with a ResponseDTO indicating success or failure.
     */
    @GetMapping("/send_email")
    @SecurityRequirement(name = "jwtAuth")
    public ResponseEntity<ResponseDTO> sendEmail(@RequestParam("token") String authToken) {
        // Retrieve user based on the token
        try {
            authenticationService.sendEmail("VERIFY", authToken);
            return new ResponseEntity<>(new ResponseDTO(true, "Email successfully sent", null), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(new ResponseDTO(false, "An error occurred: " + e.getMessage(), null), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Initiates Google login.
     *
     * @return ResponseEntity for Google login initiation.
     */
    @GetMapping("/google/login")
    public ResponseEntity<Void> googleLogin() {
        return authenticationService.signInWithGoogle();
    }

    /**
     * Handles Google login callback.
     *
     * @param code The authorization code received from Google.
     * @return ResponseEntity with a ResponseDTO containing user information.
     */
    @GetMapping("/google/authorize")
    public ResponseEntity<ResponseDTO> googleCallback(@RequestParam("code") String code) {
        try {
            // Extract the access token from the response
            var token = authenticationService.getGoogleOAuth2User(code);
            return new ResponseEntity<>(new ResponseDTO(true, "Got user info", token), HttpStatus.OK);

        } catch (Exception e) {
            return new ResponseEntity<>(new ResponseDTO(false, "An error occurred: " + e.getMessage(), null), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Checks if a user with the specified email exists.
     *
     * @param email The email to check for existence.
     * @return ResponseEntity with a ResponseDTO indicating user existence.
     */
    @GetMapping("/user-exists")
    public ResponseEntity<ResponseDTO> checkIfUserExists(@RequestParam("email") String email) {
        try {
            User user = userService.getUserByWorkEmail(email);
            UserExistenceResponseDTO userExistenceResponseDTO = new UserExistenceResponseDTO();
            if (user == null) {
                userExistenceResponseDTO.setUserExists(false);
                return new ResponseEntity<>(new ResponseDTO(false, "User doesn't exists", userExistenceResponseDTO), HttpStatus.OK);
            } else {
                userExistenceResponseDTO.setUserExists(true);
                return new ResponseEntity<>(new ResponseDTO(true, "User exists", userExistenceResponseDTO), HttpStatus.OK);
            }
        } catch (Exception e) {
            return new ResponseEntity<>(new ResponseDTO(false, "Something went wrong", null), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Retrieves new access and refresh tokens based on a given refresh token.
     * Checks if the refresh token has expired, extracts the claims from the refresh token,
     * and then calls the `getTokens` method of the `authenticationService` to generate new tokens.
     * Returns a `ResponseEntity` object containing the new tokens if successful,
     * or an error message if any exception occurs.
     *
     * @param refreshToken The refresh token used to generate new access and refresh tokens.
     * @return A response entity object containing the new tokens if successful, or an error message if any exception occurs.
     */
    @GetMapping("/get-refresh-token")
    public ResponseEntity<ResponseDTO> getRefreshToken(String refreshToken) {
        try {
            if (jwtService.isTokenExpired(refreshToken)) {
                return ResponseEntity.badRequest().body(new ResponseDTO(false, "Refresh Token has expired", null));
            }

            Claims claims = jwtService.extractAllClaims(refreshToken);
            if (claims == null) {
                return ResponseEntity.badRequest().body(new ResponseDTO(false, "Invalid refresh token", null));
            }

            Map<String, Object> tokens = authenticationService.getTokens(claims);
            if (tokens == null) {
                return ResponseEntity.badRequest().body(new ResponseDTO(false, "Failed to generate tokens", null));
            }

            return ResponseEntity.ok(new ResponseDTO(true, "New tokens are generated", tokens));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ResponseDTO(false, "Error: " + e.getMessage(), null));
        }
    }
}