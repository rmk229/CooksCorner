package kz.yermek.dto.request;

import java.io.Serializable;

public record IngredientRequestDto(String name, String weight) implements Serializable {
}
