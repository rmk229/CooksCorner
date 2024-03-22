package kz.yermek.services;

public interface ActionService {
    void putLikeIntoRecipe(Long recipeId, Long userId);

    void removeLikeFromRecipe(Long recipeId, Long userId);

    void removeMarkFromRecipe(Long recipeId, Long userId);

    void putMarkIntoRecipe(Long recipeId, Long userId);

    void unfollowUser(Long userId, Long currentUserId);

    void followUser(Long userId, Long currentUserId);
}
