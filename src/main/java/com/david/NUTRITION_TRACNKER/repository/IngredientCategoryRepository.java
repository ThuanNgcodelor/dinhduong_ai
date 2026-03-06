package com.david.NUTRITION_TRACNKER.repository;

import com.david.NUTRITION_TRACNKER.entity.IngredientCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IngredientCategoryRepository extends JpaRepository<IngredientCategory, Integer> {
}