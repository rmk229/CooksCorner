package kz.yermek.repositories;

import kz.yermek.enums.Category;
import kz.yermek.models.Recipe;
import kz.yermek.models.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RecipeRepository extends JpaRepository<Recipe, Long> {

    @Query("select r from Recipe r where r.category = :category ORDER BY SIZE(r.likes) DESC")
    Page<Recipe> findPopularRecipes(@Param("category") Category category, Pageable pageable);


    @Query("SELECT r FROM Recipe r WHERE r.createdBy.id = :userId")
    Page<Recipe> findRecipesPageByUserId(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT r FROM Recipe r WHERE r.createdBy.id = :userId")
    List<Recipe> findRecipesPageByUserId(@Param("userId") Long userId);

    @Query("SELECT c FROM Recipe c WHERE c.recipeName LIKE CONCAT('%', :query, '%')")
    List<Recipe> searchRecipes(@Param("query") String query);


    @Query("SELECT r FROM Recipe r JOIN r.saves s WHERE s.id = :userId")
    Page<Recipe> findFlaggedRecipes(@Param("userId") Long userId, Pageable pageable);



}
