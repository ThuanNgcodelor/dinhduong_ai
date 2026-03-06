package com.david.NUTRITION_TRACNKER.repository;

import com.david.NUTRITION_TRACNKER.entity.RecipeIngredient;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RecipeIngredientRepository extends JpaRepository<RecipeIngredient, Integer> {
    List<RecipeIngredient> findByRecipeId(Integer recipeId);

    // Hàm xóa theo recipeId
    void deleteAllByRecipeId(Integer recipeId);
    
    List<RecipeIngredient> findByRecipeRecipeId(Integer recipeId);
}