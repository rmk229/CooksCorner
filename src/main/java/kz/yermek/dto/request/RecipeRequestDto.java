package kz.yermek.dto.request;

import java.io.Serializable;
import java.util.List;

public record RecipeRequestDto(
        String recipeName,
        String description,
        String category,
        String difficulty,
        String cookingTime,
        List<IngredientRequestDto> ingredients
) implements Serializable {
}
