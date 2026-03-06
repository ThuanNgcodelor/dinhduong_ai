package com.david.NUTRITION_TRACNKER.repository;

import com.david.NUTRITION_TRACNKER.entity.ShoppingList;
import java.time.LocalDate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ShoppingListRepository extends JpaRepository<ShoppingList, Integer> {
    List<ShoppingList> findByUserId(Integer userId);
    Optional<ShoppingList> findByUserIdAndFromDateAndToDate(Integer userId, LocalDate fromDate, LocalDate toDate);
    List<ShoppingList> findByUserIdOrderByCreatedAtDesc(Integer userId);
}