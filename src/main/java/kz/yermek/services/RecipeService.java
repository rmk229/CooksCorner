package kz.yermek.services;

import kz.yermek.dto.RecipeDto;
import kz.yermek.dto.RecipeListDto;
import kz.yermek.dto.request.RecipeRequestDto;
import kz.yermek.enums.Category;
import kz.yermek.models.Recipe;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;


public interface RecipeService {
    Recipe addRecipe(RecipeRequestDto requestDto, MultipartFile image, Long userId);
    ResponseEntity<List<RecipeListDto>> getByCategory(Category category, Long userId);
    RecipeDto getRecipeById(Long recipeId, Long userId);
    boolean isLiked(Long recipeId, Long userId);
    boolean isSaved(Long recipeId, Long userId);
    ResponseEntity<List<RecipeListDto>> searchRecipes(String query, Long userId);
    ResponseEntity<List<RecipeListDto>> getMyRecipe(Long userId);
    ResponseEntity<List<RecipeListDto>> getMyFlaggedRecipe(Long userId);
    ResponseEntity<List<RecipeListDto>> getRecipesByUserId(Long userId, Long currentUserId);
}
