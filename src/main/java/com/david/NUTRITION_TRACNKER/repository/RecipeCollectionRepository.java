package com.david.NUTRITION_TRACNKER.repository;

import com.david.NUTRITION_TRACNKER.entity.RecipeCollection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface RecipeCollectionRepository extends JpaRepository<RecipeCollection, Integer> {
    Optional<RecipeCollection> findByUserIdAndName(Integer userId, String name);
    List<RecipeCollection> findByUserId(Integer userId);
}