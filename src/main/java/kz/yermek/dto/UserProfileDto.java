package kz.yermek.dto;

import lombok.Builder;

import java.io.Serializable;

@Builder
public record UserProfileDto(String imageUrl, String name, int recipeQuantity, int followerQuantity, int followingQuantity, String bio,  boolean isFollowed) implements Serializable {

}
