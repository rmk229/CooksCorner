package kz.yermek.services.impl;

import jakarta.transaction.Transactional;
import kz.yermek.dto.RecipeDto;
import kz.yermek.dto.RecipeListDto;
import kz.yermek.dto.request.IngredientRequestDto;
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
import kz.yermek.util.RecipeMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RecipeServiceImpl implements RecipeService {

    private final ImageService imageService;
    private final RecipeRepository recipeRepository;
    private final UserRepository userRepository;
    private final RecipeMapper recipeMapper;

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
        for(IngredientRequestDto ingredient: requestDto.ingredients()){
            Ingredient ingredient1 = new Ingredient();
            ingredient1.setRecipe(recipe);
            ingredient1.setName(ingredient.name());
            ingredient1.setAmount(ingredient.weight());
            ingredients.add(ingredient1);
        }
        recipe.setIngredients(ingredients);
        return recipeRepository.save(recipe);
    }

    @Override
    public ResponseEntity<List<RecipeListDto>> getByCategory(Category category, Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Recipe> recipePage = recipeRepository.findPopularRecipes(category, pageable);
        List<Recipe> recipes = recipePage.getContent();
        return ResponseEntity.ok(recipeMapper.toRecipeListDtoList(recipes, userId));
    }

    @Override
    public RecipeDto getRecipeById(Long recipeId, Long userId) {
        Recipe recipe = recipeRepository.findById(recipeId)
                .orElseThrow(() -> new RecipeNotFoundException("Recipe not found"));
        return recipeMapper.toRecipeDto(recipe, userId);
    }

    @Override
    public boolean isLiked(Long recipeId, Long userId){
        Recipe recipe = recipeRepository.findById(recipeId).orElseThrow(()-> new RecipeNotFoundException("Recipe not found"));
        return recipe != null && recipe.getLikes().stream().anyMatch(user -> user.getId().equals(userId));
    }

    @Override
    public  boolean isSaved(Long recipeId, Long userId){
        Recipe recipe = recipeRepository.findById(recipeId).orElseThrow(()-> new RecipeNotFoundException("Recipe not found"));
        return recipe != null && recipe.getSaves().stream().anyMatch(user -> user.getId().equals(userId));
    }

    @Override
    public ResponseEntity<List<RecipeListDto>> searchRecipes(String query, Long currentUserId) {
        List<Recipe> recipes = recipeRepository.searchRecipes(query);
        return ResponseEntity.ok(recipeMapper.toRecipeListDtoList(recipes, currentUserId));

    }

    @Override
    public ResponseEntity<List<RecipeListDto>> getMyRecipe(Long userId, int page, int size) {
        User user = userRepository.findById(userId).orElseThrow(() ->
                new UsernameNotFoundException("User not found"));

        Pageable pageable = PageRequest.of(page, size);
        Page<Recipe> recipePage  = recipeRepository.findRecipesPageByUserId(user.getId(), pageable);
        List<Recipe> recipes = recipePage.getContent();
        return ResponseEntity.ok(recipeMapper.toRecipeListDtoList(recipes, user.getId()));
    }

    @Override
    public ResponseEntity<List<RecipeListDto>> getMyFlaggedRecipe(Long userId, int page, int size) {
        User user = userRepository.findById(userId).orElseThrow(() ->
                new UsernameNotFoundException("User not found"));
        Pageable pageable = PageRequest.of(page, size);
        Page<Recipe> recipePage = recipeRepository.findFlaggedRecipes(user.getId(), pageable);
        List<Recipe> recipes = recipePage.getContent();
        return ResponseEntity.ok(recipeMapper.toRecipeListDtoList(recipes, user.getId()));

    }

    @Override
    public ResponseEntity<List<RecipeListDto>> getRecipesByUserId(Long userId, Long currentUserId, int page, int size) {
        User user = userRepository.findById(userId).orElseThrow(() ->
                new UsernameNotFoundException("User not found"));
        Pageable pageable = PageRequest.of(page, size);
        Page<Recipe> recipePage  = recipeRepository.findRecipesPageByUserId(user.getId(), pageable);
        List<Recipe> recipes = recipePage.getContent();
        return ResponseEntity.ok(recipeMapper.toRecipeListDtoList(recipes, currentUserId));
    }
}
