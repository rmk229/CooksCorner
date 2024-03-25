package kz.yermek.dto;

import lombok.Builder;

import java.io.Serializable;
import java.util.List;

@Builder
public record RecipeDto(
        Long id,
        String recipeName,
        String imageUrl,
        String author,
        String cookingTime,
        String difficulty,
        int likeQuantity,
        boolean isLiked,
        boolean isSaved,
        String description,
        List<IngredientDto> ingredients


) implements Serializable {
}
