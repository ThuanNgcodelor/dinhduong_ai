// UserPantryRepository.java
package com.david.NUTRITION_TRACNKER.repository;

import com.david.NUTRITION_TRACNKER.entity.UserPantry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface UserPantryRepository extends JpaRepository<UserPantry, Integer> {
    List<UserPantry> findByUserIdOrderByExpiryDateAsc(Integer userId);
    UserPantry findByUserIdAndIngredientId(Integer userId, Integer ingredientId);
}