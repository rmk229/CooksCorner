package kz.yermek.dto;

import lombok.Builder;

import java.io.Serializable;

@Builder
public record RecipeListDto(Long id, String imageUrl, String recipeName, String author, int likesQuantity, int savesQuantity, boolean isSaved, boolean isLiked) implements Serializable {


}
