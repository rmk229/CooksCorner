package kz.yermek.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import kz.yermek.dto.RecipeDto;
import kz.yermek.dto.RecipeListDto;
import kz.yermek.dto.request.RecipeRequestDto;
import kz.yermek.enums.Category;
import kz.yermek.services.ImageService;
import kz.yermek.services.RecipeService;
import kz.yermek.util.JsonValidator;
import kz.yermek.util.JwtTokenUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/recipes")
public class RecipeController {
    private final RecipeService recipeService;
    private final JwtTokenUtils tokenUtils;
    private final ObjectMapper objectMapper;
    private final JsonValidator jsonValidator;
    private final ImageService imageService;

    @Operation(
            summary = "Get recipes by category",
            description = "Using this endpoint it is possible to get recipes by category",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Recipe list"),
                    @ApiResponse(responseCode = "404", description = "Recipe not found", content = @Content),
                    @ApiResponse(responseCode = "403", description = "Authentication required")
            }
    )
    @GetMapping("/get-by-category")
    public ResponseEntity<List<RecipeListDto>> getRecipes(@RequestParam(value = "category") String category,
                                                          @RequestParam(value = "page", defaultValue = "0") int page,
                                                          @RequestParam(value = "size", defaultValue = "12") int size,
                                                          Authentication authentication) {

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required");
        }
        Long userId = tokenUtils.getUserIdFromAuthentication(authentication);
        String upperCaseCategory = category.toUpperCase();
        try {
            Category categoryEnum = Category.valueOf(upperCaseCategory);
            return recipeService.getByCategory(categoryEnum, userId);
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException(exception.getMessage());
        }
    }

    @Operation(
            summary = "Get recipes of the current user",
            description = "Using this endpoint it is possible to get recipes of the current user",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Recipe list"),
                    @ApiResponse(responseCode = "404", description = "Recipe not found", content = @Content),
                    @ApiResponse(responseCode = "403", description = "Authentication required")
            }
    )
    @GetMapping("/my-recipes")
    public ResponseEntity<List<RecipeListDto>> getMyRecipes(@RequestParam(value = "page", defaultValue = "0") int page,
                                                            @RequestParam(value = "size", defaultValue = "12") int size,
                                                            Authentication authentication) {

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required");
        }
        Long userId = tokenUtils.getUserIdFromAuthentication(authentication);

        return recipeService.getMyRecipe(userId);
    }



    @Operation(
            summary = "Get saved recipes of the current user",
            description = "Using this endpoint it is possible to get saved recipes o the current user",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Recipe list"),
                    @ApiResponse(responseCode = "404", description = "Recipe not found", content = @Content),
                    @ApiResponse(responseCode = "403", description = "Authentication required")
            }
    )

    @GetMapping("/my-flagged-recipes")
    public ResponseEntity<List<RecipeListDto>> getMyFlaggedRecipes(Authentication authentication,
                                                                   @RequestParam(value = "page", defaultValue = "0") int page,
                                                                   @RequestParam(value = "size", defaultValue = "12") int size) {

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required");
        }
        Long userId = tokenUtils.getUserIdFromAuthentication(authentication);
        return recipeService.getMyFlaggedRecipe(userId);
    }

    @Operation(
            summary = "Get recipes by user id",
            description = "Using this endpoint it is possible to get recipes by user id",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Recipe list"),
                    @ApiResponse(responseCode = "404", description = "Recipe not found", content = @Content),
                    @ApiResponse(responseCode = "403", description = "Authentication required")
            }
    )

    @GetMapping("/get-recipes-by-userId/{userId}")
    public ResponseEntity<List<RecipeListDto>> getRecipesByUserId(Authentication authentication,
                                                                  @PathVariable(name = "userId") Long userId,
                                                                  @RequestParam(value = "page", defaultValue = "0") int page,
                                                                  @RequestParam(value = "size", defaultValue = "12") int size) {

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required");
        }
        Long currentUserId = tokenUtils.getUserIdFromAuthentication(authentication);
        return recipeService.getRecipesByUserId(userId, currentUserId);
    }


    @Operation(
            summary = "Get detailed page of the recipe",
            description = "Using this endpoint it is possible to get detailed recipe",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Recipe"),
                    @ApiResponse(responseCode = "404", description = "Recipe not found", content = @Content),
                    @ApiResponse(responseCode = "403", description = "Authentication required")
            }
    )

    @GetMapping("/{recipeId}")
    public ResponseEntity<RecipeDto> getRecipeById(@PathVariable Long recipeId, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required");
        }
        Long userId = tokenUtils.getUserIdFromAuthentication(authentication);
        return ResponseEntity.ok(recipeService.getRecipeById(recipeId, userId));
    }


    @Operation(
            summary = "Add recipe",
            description = "Whenever user wants to create a new recipe then he or she should to use this endpoint",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Recipe successfully created"),
                    @ApiResponse(responseCode = "403", description = "Authentication required")
            }
    )
    @PostMapping("/add-recipe")
    public ResponseEntity<String> addRecipe(@RequestPart("recipeDto") String recipeDto,
                                            @RequestPart("photo") MultipartFile image,
                                            Authentication authentication) {
        try {
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Authentication required");
            }
            Long userId = tokenUtils.getUserIdFromAuthentication(authentication);
            RecipeRequestDto requestDto = objectMapper.readValue(recipeDto, RecipeRequestDto.class);
            jsonValidator.validateRecipeRequest(requestDto);
            if (image == null || image.isEmpty() || !imageService.isImageFile(image)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid image file");
            }
            recipeService.addRecipe(requestDto, image, userId);
            return ResponseEntity.ok("Recipe has been added successfully");
        } catch (JsonProcessingException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid recipe DTO JSON: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to add recipe: " + e.getMessage());
        }
    }




    @Operation(
            summary = "Search recipe",
            description = "Search recipes based on user query ",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Recipe list"),
                    @ApiResponse(responseCode = "404", description = "Recipe not found", content = @Content),
                    @ApiResponse(responseCode = "403", description = "Authentication required")
            }
    )
    @GetMapping("/search")
    public ResponseEntity<List<RecipeListDto>> search(@RequestParam(name = "query")String query, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required");
        }
        Long userId = tokenUtils.getUserIdFromAuthentication(authentication);
        return recipeService.searchRecipes(query, userId);
    }
}
