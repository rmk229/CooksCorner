package kz.yermek.util;

import kz.yermek.dto.IngredientDto;
import kz.yermek.dto.RecipeDto;
import kz.yermek.dto.RecipeListDto;
import kz.yermek.exceptions.RecipeNotFoundException;
import kz.yermek.models.Ingredient;
import kz.yermek.models.Recipe;
import kz.yermek.repositories.RecipeRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class RecipeMapper {
    private final RecipeRepository recipeRepository;
    public RecipeMapper(RecipeRepository recipeRepository) {
        this.recipeRepository = recipeRepository;
    }
    public RecipeListDto toRecipeListDto(Recipe recipe, boolean isLikedByUser, boolean isSavedByUser) {
        return new RecipeListDto(
                recipe.getId(),
                getImageUrl(recipe),
                recipe.getRecipeName(),
                getCreatorName(recipe),
                getLikesCount(recipe),
                getSavesCount(recipe),
                isSavedByUser,
                isLikedByUser
        );
    }
    public List<RecipeListDto> toRecipeListDtoList(List<Recipe> recipes, Long userId) {
        return recipes.stream()
                .map(recipe -> toRecipeListDto(recipe,
                        isLiked(recipe.getId(), userId),
                        isSaved(recipe.getId(), userId)))
                .collect(Collectors.toList());
    }
    public RecipeDto toRecipeDto(Recipe recipe, Long userId) {
        return new RecipeDto(
                recipe.getId(),
                recipe.getRecipeName(),
                getImageUrl(recipe),
                getCreatorName(recipe),
                recipe.getCookingTime(),
                getDifficulty(recipe),
                getLikesCount(recipe),
                isLiked(recipe.getId(), userId),
                isSaved(recipe.getId(), userId),
                recipe.getDescription(),
                mapIngredients(recipe.getIngredients())
        );
    }
    private String getImageUrl(Recipe recipe) {
        return (recipe.getImage() != null) ? recipe.getImage().getUrl() : null;
    }

    private String getCreatorName(Recipe recipe) {
        return (recipe.getCreatedBy() != null) ? recipe.getCreatedBy().getName() : "Unknown";
    }
    private int getLikesCount(Recipe recipe) {
        return recipe.getLikes().size();
    }

    private int getSavesCount(Recipe recipe) {
        return recipe.getSaves().size();
    }

    private String getDifficulty(Recipe recipe) {
        return (recipe.getDifficulty() != null) ? recipe.getDifficulty().name() : "Unknown";
    }

    private List<IngredientDto> mapIngredients(List<Ingredient> ingredients) {
        return ingredients.stream()
                .map(ingredient -> new IngredientDto(ingredient.getId(), ingredient.getName(), ingredient.getAmount()))
                .collect(Collectors.toList());
    }

    private boolean isLiked(Long recipeId, Long userId) {
        Recipe recipe = recipeRepository.findById(recipeId).orElseThrow(()-> new RecipeNotFoundException("Recipe not found"));
        return recipe != null && recipe.getLikes().stream().anyMatch(user -> user.getId().equals(userId));
    }


    private boolean isSaved(Long recipeId, Long userId) {
        Recipe recipe = recipeRepository.findById(recipeId).orElseThrow(()-> new RecipeNotFoundException("Recipe not found"));
        return recipe != null && recipe.getSaves().stream().anyMatch(user -> user.getId().equals(userId));
    }
}
