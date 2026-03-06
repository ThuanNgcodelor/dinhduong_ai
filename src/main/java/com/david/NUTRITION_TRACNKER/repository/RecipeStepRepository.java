package com.david.NUTRITION_TRACNKER.repository;

import com.david.NUTRITION_TRACNKER.entity.RecipeStep;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RecipeStepRepository extends JpaRepository<RecipeStep, Integer> {
    List<RecipeStep> findByRecipeId(Integer recipeId);
    
    // Service đang gọi hàm này để xóa các bước khi reject
    void deleteAllByRecipeId(Integer recipeId);
}