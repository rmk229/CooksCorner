package kz.yermek.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import kz.yermek.models.Ingredient;

import java.io.Serializable;
import java.util.List;

public record RecipeRequestDto(

        String recipeName,

        String description,

        String category,

        String difficulty,
        String cookingTime,
        List<Ingredient> ingredients
) implements Serializable {
}
