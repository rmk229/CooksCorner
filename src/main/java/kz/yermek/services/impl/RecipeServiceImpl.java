package kz.yermek.services.impl;

import jakarta.transaction.Transactional;
import kz.yermek.dto.IngredientDto;
import kz.yermek.dto.RecipeDto;
import kz.yermek.dto.RecipeListDto;
import kz.yermek.dto.request.RecipeRequestDto;
import kz.yermek.enums.Category;
import kz.yermek.enums.Difficulty;
import kz.yermek.exceptions.RecipeNotFoundException;
import kz.yermek.models.Ingredient;
import kz.yermek.models.Recipe;
import kz.yermek.models.User;
import kz.yermek.repositories.RecipeRepository;
import kz.yermek.repositories.UserRepository;
import kz.yermek.services.ImageService;
import kz.yermek.services.RecipeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RecipeServiceImpl implements RecipeService {

    private final ImageService imageService;
    private final RecipeRepository recipeRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public Recipe addRecipe(RecipeRequestDto requestDto, MultipartFile image, Long userId) {
        Recipe recipe = new Recipe();
        recipe.setRecipeName(requestDto.recipeName());
        recipe.setCategory(Category.valueOf(requestDto.category().toUpperCase()));
        recipe.setDifficulty(Difficulty.valueOf(requestDto.difficulty().toUpperCase()));
        recipe.setDescription(requestDto.description());
        recipe.setImage(imageService.saveImage(image));
        recipe.setCookingTime(requestDto.cookingTime());
        User user = userRepository.findById(userId).orElseThrow(()-> new UsernameNotFoundException("User not found"));
        recipe.setCreatedBy(user);
        List<Ingredient> ingredients = new ArrayList<>();
        for(Ingredient ingredient: requestDto.ingredients()){
            Ingredient ingredient1 = new Ingredient();
            ingredient1.setRecipe(recipe);
            ingredient1.setName(ingredient.getName());
            ingredient1.setAmount(ingredient.getAmount());
            ingredients.add(ingredient1);
        }
        recipe.setIngredients(ingredients);
        return recipeRepository.save(recipe);
    }

    @Override
    public ResponseEntity<List<RecipeListDto>> getByCategory(Category category, Long userId) {
        List<Recipe> recipes = recipeRepository.findPopularRecipes(category);

        List<RecipeListDto> recipesDto = recipes.stream().map(recipe -> {
            int likesCount = recipe.getLikes().size();
            int savesCount = recipe.getSaves().size();
            boolean isLikedByUser = isLiked(recipe.getId(), userId);
            boolean isSavedByUser = isSaved(recipe.getId(), userId);
            String imageUrl = (recipe.getImage() != null) ? recipe.getImage().getUrl() : null;
            return new RecipeListDto(
                    recipe.getId(),
                    imageUrl,
                    recipe.getRecipeName(),
                    recipe.getCreatedBy().getName(),
                    likesCount,
                    savesCount,
                    isSavedByUser,
                    isLikedByUser

            );
        }).collect(Collectors.toList());
        if (recipesDto.isEmpty()) {
            return ResponseEntity.notFound().build();
        } else {
            return ResponseEntity.ok(recipesDto);
        }
    }

    @Override
    public RecipeDto getRecipeById(Long recipeId, Long userId) {
        Recipe recipe = recipeRepository.findById(recipeId)
                .orElseThrow(() -> new RecipeNotFoundException("Recipe not found"));
        String imageUrl = (recipe.getImage() != null) ? recipe.getImage().getUrl() : null;
        String createdBy = (recipe.getCreatedBy() != null) ? recipe.getCreatedBy().getName() : "Unknown";

        List<IngredientDto> ingredients = recipe.getIngredients().stream()
                .map(ingredient -> new IngredientDto(ingredient.getName(), ingredient.getAmount()))
                .collect(Collectors.toList());

        return new RecipeDto(
                recipe.getId(),
                recipe.getRecipeName(),
                imageUrl,
                createdBy,
                recipe.getCookingTime(),
                (recipe.getDifficulty() != null) ? recipe.getDifficulty().name() : "Unknown",
                recipe.getLikes().size(),
                isLiked(recipeId, userId),
                isSaved(recipeId, userId),
                recipe.getDescription(),
                ingredients
        );
    }

    @Override
    public boolean isLiked(Long recipeId, Long userId) {
        Recipe recipe = recipeRepository.findById(recipeId).orElseThrow(() -> new RecipeNotFoundException("Recipe not found"));
        return recipe != null && recipe.getLikes().stream().anyMatch(user -> user.getId().equals(userId));
    }

    @Override
    public boolean isSaved(Long recipeId, Long userId) {
        Recipe recipe = recipeRepository.findById(recipeId).orElseThrow(() -> new RecipeNotFoundException("Recipe not found"));
        return recipe != null && recipe.getSaves().stream().anyMatch(user -> user.getId().equals(userId));
    }

    @Override
    public List<RecipeListDto> searchRecipes(String query, Long currentUserId) {
        List<Recipe> recipes = recipeRepository.searchRecipes(query);
        List<RecipeListDto> recipesDto = new ArrayList<>(recipes.size());
        for (Recipe recipe : recipes) {
            int likesCount = recipe.getLikes().size();
            int savesCount = recipe.getSaves().size();
            boolean isLiked = isLiked(recipe.getId(), currentUserId);
            boolean isSaved = isSaved(recipe.getId(), currentUserId);
            String imageUrl = (recipe.getImage() != null) ? recipe.getImage().getUrl() : null;
            RecipeListDto dto = new RecipeListDto(
                    recipe.getId(),
                    imageUrl,
                    recipe.getRecipeName(),
                    recipe.getCreatedBy().getName(),
                    likesCount,
                    savesCount,
                    isLiked,
                    isSaved
            );
            recipesDto.add(dto);
        }
        return recipesDto;
    }

    @Override
    public ResponseEntity<List<RecipeListDto>> getMyRecipe(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() ->
                new UsernameNotFoundException("User not found"));
        List<Recipe> recipes = recipeRepository.findRecipesByUserId(user.getId());

        List<RecipeListDto> recipesDto = recipes.stream().map(recipe -> {
            int likesCount = recipe.getLikes().size();
            int savesCount = recipe.getSaves().size();
            boolean isLikedByUser = isLiked(recipe.getId(), userId);
            boolean isSavedByUser = isSaved(recipe.getId(), userId);

            String imageUrl = (recipe.getImage() != null) ? recipe.getImage().getUrl() : null;
            return new RecipeListDto(
                    recipe.getId(),
                    imageUrl,
                    recipe.getRecipeName(),
                    recipe.getCreatedBy().getName(),
                    likesCount,
                    savesCount,
                    isLikedByUser,
                    isSavedByUser
            );
        }).collect(Collectors.toList());
        return recipesDto.isEmpty() ? ResponseEntity.notFound().build() : ResponseEntity.ok(recipesDto);
    }

    @Override
    public ResponseEntity<List<RecipeListDto>> getMyFlaggedRecipe(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() ->
                new UsernameNotFoundException("User not found"));
        List<Recipe> recipes = recipeRepository.findFlaggedRecipes(user.getId());
        List<RecipeListDto> recipesDto = recipes.stream().map(recipe -> {
            int likesCount = recipe.getLikes().size();
            int savesCount = recipe.getSaves().size();
            boolean isLikedByUser = isLiked(recipe.getId(), userId);
            boolean isSavedByUser = isSaved(recipe.getId(), userId);
            String imageUrl = (recipe.getImage() != null) ? recipe.getImage().getUrl() : null;

            return new RecipeListDto(
                    recipe.getId(),
                    imageUrl,
                    recipe.getRecipeName(),
                    recipe.getCreatedBy().getName(),
                    likesCount,
                    savesCount,
                    isLikedByUser,
                    isSavedByUser
            );
        }).collect(Collectors.toList());
        if (recipesDto.isEmpty()) {
            return ResponseEntity.notFound().build();
        } else {
            return ResponseEntity.ok(recipesDto);
        }

    }

    @Override
    public ResponseEntity<List<RecipeListDto>> getRecipesByUserId(Long userId, Long currentUserId) {
        User user = userRepository.findById(userId).orElseThrow(() ->
                new UsernameNotFoundException("User not found"));
        List<Recipe> recipes = recipeRepository.findRecipesByUserId(user.getId());
        List<RecipeListDto> recipesDto = recipes.stream().map(recipe -> {
            int likesCount = recipe.getLikes().size();
            int savesCount = recipe.getSaves().size();
            boolean isLikedByUser = isLiked(recipe.getId(), currentUserId);
            boolean isSavedByUser = isSaved(recipe.getId(), currentUserId);

            String imageUrl = (recipe.getImage() != null) ? recipe.getImage().getUrl() : null;
            return new RecipeListDto(
                    recipe.getId(),
                    imageUrl,
                    recipe.getRecipeName(),
                    recipe.getCreatedBy().getName(),
                    likesCount,
                    savesCount,
                    isLikedByUser,
                    isSavedByUser
            );
        }).collect(Collectors.toList());
        if (recipesDto.isEmpty()) {
            return ResponseEntity.notFound().build();
        } else {
            return ResponseEntity.ok(recipesDto);
        }
    }
}
