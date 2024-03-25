package kz.yermek.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import kz.yermek.dto.MyProfileDto;
import kz.yermek.dto.UserDto;
import kz.yermek.dto.UserProfileDto;
import kz.yermek.dto.UserUpdateProfileDto;
import kz.yermek.services.UserService;
import kz.yermek.util.JwtTokenUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Validator;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/api/v1/users/")
@RequiredArgsConstructor
public class UserController {

    private final JwtTokenUtils tokenUtils;
    private final UserService userService;
    private final ObjectMapper objectMapper;
    private final Validator validator;


    @Operation(
            summary = "Get user profile",
            description = "Get user profile using user id",
            responses = {
                    @ApiResponse(responseCode = "200", description = "User profile"),
                    @ApiResponse(responseCode = "404", description = "User not found", content = @Content),
                    @ApiResponse(responseCode = "403", description = "Authentication required")
            }
    )
    @GetMapping("/get-user-profile/{userId}")
    public ResponseEntity<UserProfileDto> getRecipesByUser(@PathVariable Long userId, Authentication authentication){

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required");
        }
        Long currentUserId = tokenUtils.getUserIdFromAuthentication(authentication);
        return userService.getUserProfile(userId, currentUserId);
    }

    @Operation(
            summary = "Search user",
            description = "Search users based on user name query",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Users list"),
                    @ApiResponse(responseCode = "404", description = "User not found", content = @Content),
                    @ApiResponse(responseCode = "403", description = "Authentication required")
            }
    )
    @GetMapping("/search")
    public ResponseEntity<List<UserDto>> search(@RequestParam(name = "query") String query) {
        return ResponseEntity.ok(userService.searchUser(query));
    }

    @Operation(
            summary = "Get own profile",
            description = "User can get own profile",
            responses = {
                    @ApiResponse(responseCode = "200", description = "User profile"),
                    @ApiResponse(responseCode = "403", description = "Authentication required")
            }
    )
    @GetMapping("/my-profile")
    public ResponseEntity<MyProfileDto> getRecipesByUser(Authentication authentication){

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required");
        }
        Long currentUserId = tokenUtils.getUserIdFromAuthentication(authentication);
        return userService.getOwnProfile(currentUserId);
    }



    @Operation(
            summary = "Update profile",
            description = "Using this endpoint user can update his or her profile",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Profile updated successfully"),
                    @ApiResponse(responseCode = "403", description = "Authentication required")
            }
    )
    @PutMapping(value = "/update-profile")
    public ResponseEntity<String> changeProfile(@RequestPart("dto") String dto,
                                                @RequestPart(value = "image", required = false) MultipartFile image,
                                                Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required");
        }

        UserUpdateProfileDto request;
        Long currentUserId = tokenUtils.getUserIdFromAuthentication(authentication);
        try {
            request = objectMapper.readValue(dto, UserUpdateProfileDto.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        validateRequest(request);
        if (image != null) {
            if (!Objects.requireNonNull(image.getContentType()).startsWith("image/")) {
                throw new IllegalArgumentException("Uploaded file is not an image");
            }
        }
        return ResponseEntity.ok(userService.updateUser(request, currentUserId, image));
    }

    private void validateRequest(UserUpdateProfileDto request) {
        BindingResult bindingResult = new BeanPropertyBindingResult(request, "userUpdateProfileDto");
        validator.validate(request, bindingResult);

        if (bindingResult.hasErrors()) {
            throw new IllegalArgumentException("Invalid input " + bindingResult.getAllErrors());
        }
    }
}
