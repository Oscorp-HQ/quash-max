package com.quashbugs.quash.controller;

import com.quashbugs.quash.dto.request.UpdateUserRequestDTO;
import com.quashbugs.quash.dto.response.ResponseDTO;
import com.quashbugs.quash.model.User;
import com.quashbugs.quash.service.UserService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@SecurityRequirement(name = "jwtAuth")
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(
            UserService userService) {
        this.userService = userService;
    }

    /**
     * Retrieves the details of the authenticated user.
     *
     * @param authentication The user's authentication information.
     * @return ResponseEntity with a ResponseDTO containing the user's details.
     */
    @GetMapping
    public ResponseEntity<ResponseDTO> getUser(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            User user = (User) authentication.getPrincipal();
            if (user != null) {
                return new ResponseEntity<>(new ResponseDTO(true, "User details retrieved successfully", user), HttpStatus.OK);
            } else {
                return new ResponseEntity<>(new ResponseDTO(false, "Invalid token or user not found", null), HttpStatus.UNAUTHORIZED);
            }
        } else {
            return new ResponseEntity<>(new ResponseDTO(false, "Invalid token or user not authenticated", null), HttpStatus.UNAUTHORIZED);
        }
    }

    /**
     * Retrieves the verification status of the authenticated user's email.
     *
     * @param authentication The user's authentication information.
     * @return ResponseEntity with a ResponseDTO indicating the email verification status.
     */
    @GetMapping("/is_verified")
    public ResponseEntity<ResponseDTO> getUserVerified(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            User user = (User) authentication.getPrincipal();

            if (user != null) {
                return new ResponseEntity<>(new ResponseDTO(true, "User verifcation status retrived", user.isEmailVerified()), HttpStatus.OK);
            } else {
                return new ResponseEntity<>(new ResponseDTO(false, "Invalid token or user not found", null), HttpStatus.UNAUTHORIZED);
            }
        } else {
            return new ResponseEntity<>(new ResponseDTO(false, "Invalid token or user not authenticated", null), HttpStatus.UNAUTHORIZED);
        }
    }

    /**
     * Updates the user's profile information.
     *
     * @param authentication The user's authentication information.
     * @param updatedUser    The updated user profile information.
     * @return ResponseEntity with a ResponseDTO indicating the success of the update.
     */
    @PatchMapping
    public ResponseEntity<ResponseDTO> updateUser(Authentication authentication, @RequestBody User updatedUser) {
        if (authentication != null && authentication.isAuthenticated()) {
            User user = (User) authentication.getPrincipal();
            userService.updateUser(user, updatedUser);
            if (user != null) {
                return new ResponseEntity<>(new ResponseDTO(true, "User updated successfully", user), HttpStatus.OK);
            } else {
                return new ResponseEntity<>(new ResponseDTO(false, "User not found or update failed", null), HttpStatus.NOT_FOUND);
            }
        } else {
            return new ResponseEntity<>(new ResponseDTO(false, "Invalid token or user not authenticated", null), HttpStatus.UNAUTHORIZED);
        }
    }

    /**
     * Updates the user's profile information for the dashboard.
     *
     * @param authentication    The user's authentication information.
     * @param updateUserRequestDTO The request containing updated user information for the dashboard.
     * @return ResponseEntity with a ResponseDTO indicating the success of the update.
     */
    @PatchMapping("/update-user")
    public ResponseEntity<ResponseDTO> updateUserForDashboard(Authentication authentication, @RequestBody UpdateUserRequestDTO updateUserRequestDTO) throws Exception {
        if (authentication != null && authentication.isAuthenticated()) {
            User user = (User) authentication.getPrincipal();
            if (user == null) {
                return new ResponseEntity<>(new ResponseDTO(false, "User not found or update failed", null), HttpStatus.NOT_FOUND);
            } else {
                user.setFullName(updateUserRequestDTO.getFullName());
                user.setUserOrganisationRole(updateUserRequestDTO.getUserOrganisationRole());
                if (!user.isShouldNavigateToDashboard()) {
                    user.setShouldNavigateToDashboard(true);
                }
                userService.saveUser(user);
                return new ResponseEntity<>(new ResponseDTO(true, "User updated successfully", user), HttpStatus.OK);
            }
        } else {
            return new ResponseEntity<>(new ResponseDTO(false, "Invalid token or user not authenticated", null), HttpStatus.UNAUTHORIZED);
        }
    }

    /**
     * Deletes the authenticated user's account.
     *
     * @param authentication The user's authentication information.
     * @return ResponseEntity with a ResponseDTO indicating the success of the deletion.
     */
    @DeleteMapping
    public ResponseEntity<ResponseDTO> deleteUser(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            try {
                System.out.println(authentication);
                User user = (User) authentication.getPrincipal();
                boolean deleted = userService.deleteUser(user);

                if (deleted) {
                    return new ResponseEntity<>(new ResponseDTO(true, "User deleted successfully", null), HttpStatus.OK);
                } else {
                    return new ResponseEntity<>(new ResponseDTO(false, "User delete failed", null), HttpStatus.INTERNAL_SERVER_ERROR);
                }
            } catch (Exception e) {
                return new ResponseEntity<>(new ResponseDTO(false, "An error occurred: " + e.getMessage(), null), HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } else {
            // User not authenticated or invalid token
            return new ResponseEntity<>(new ResponseDTO(false, "Invalid token or user not authenticated", null), HttpStatus.UNAUTHORIZED);
        }
    }
}