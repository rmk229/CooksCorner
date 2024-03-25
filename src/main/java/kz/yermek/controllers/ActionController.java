package kz.yermek.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import kz.yermek.services.ActionService;
import kz.yermek.services.RecipeService;
import kz.yermek.services.UserService;
import kz.yermek.util.JwtTokenUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/actions")
public class ActionController {

    private final JwtTokenUtils tokenUtils;
    private final ActionService actionService;
    private final RecipeService recipeService;
    private final UserService userService;


    @PutMapping("/like/{recipeId}")
    @Operation(summary = "Toggle like for a recipe")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Like status liked successfully"),
            @ApiResponse(responseCode = "200", description = "Like status unliked successfully"),
            @ApiResponse(responseCode = "401", description = "Authentication required")
    })
    public ResponseEntity<String> toggleLike(Authentication authentication, @PathVariable(name = "recipeId") Long recipeId) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Authentication required");
        }
        Long userId = tokenUtils.getUserIdFromAuthentication(authentication);
        boolean isLiked = recipeService.isLiked(recipeId, userId);

        if (isLiked) {
            actionService.removeLikeFromRecipe(recipeId, userId);
            return ResponseEntity.ok("Like removed successfully");
        } else {
            actionService.putLikeIntoRecipe(recipeId, userId);
            return ResponseEntity.ok("Like added successfully");
        }
    }

    @PutMapping("/mark/{recipeId}")
    @Operation(summary = "Toggle mark for a recipe")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Mark status toggled successfully"),
            @ApiResponse(responseCode = "200", description = "Mark status toggled back successfully"),
            @ApiResponse(responseCode = "401", description = "Authentication required")
    })
    public ResponseEntity<String> toggleMark(Authentication authentication, @PathVariable(name = "recipeId") Long recipeId) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Authentication required");
        }
        Long userId = tokenUtils.getUserIdFromAuthentication(authentication);
        boolean isSaved = recipeService.isSaved(recipeId, userId);

        if (isSaved) {
            actionService.removeMarkFromRecipe(recipeId, userId);
            return ResponseEntity.ok("Mark removed successfully");
        } else{
            actionService.putMarkIntoRecipe(recipeId, userId);
            return ResponseEntity.ok("Mark added successfully");
        }

    }

    @PutMapping("/follow/{userId}")
    @Operation(summary = "Toggle follow for a user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User followed successfully"),
            @ApiResponse(responseCode = "200", description = "User unfollowed successfully"),
            @ApiResponse(responseCode = "401", description = "Authentication required")
    })
    public ResponseEntity<String> toggleFollow(Authentication authentication, @PathVariable(name = "userId") Long userId) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Authentication required");
        }
        Long currentUserId = tokenUtils.getUserIdFromAuthentication(authentication);
        boolean isFollowed = userService.isFollowed(userId, currentUserId);
        System.out.println(isFollowed);
        if (isFollowed) {
            actionService.unfollowUser(userId, currentUserId);
            return ResponseEntity.ok("Unfollowed successfully");
        } else {
            actionService.followUser(userId, currentUserId);
            return ResponseEntity.ok("Followed successfully");
        }
    }
}
