package com.david.NUTRITION_TRACNKER.entity;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.hibernate.annotations.Nationalized;

import com.david.NUTRITION_TRACNKER.entity.enums.MealType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "MealItems")
public class MealItem implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MealItemId")
    private Integer mealItemId;

    @Column(name = "MealPlanId")
    private Integer mealPlanId;

    @Column(name = "RecipeId")
    private Integer recipeId;

    @Column(name = "CustomDishName", columnDefinition = "TEXT")
    @Nationalized
    private String customDishName;

    @Column(name = "Calories")
    private BigDecimal calories;
    
    @Column(name = "Protein")
    private BigDecimal protein;

    @Column(name = "Carbs")
    private BigDecimal carbs;

    @Column(name = "Fat")
    private BigDecimal fat;

    @Column(name = "Status") // Enum: PENDING, EATEN, SKIPPED, REPLACED
    private String status;
    
    @Column(name = "ImageUrl")
    private String imageUrl;

    @Column(name = "MealTimeType", length = 50)
    @Enumerated(EnumType.STRING)
    private MealType mealTimeType;

    @Column(name = "QuantityMultiplier")
    private BigDecimal quantityMultiplier;

    @Column(name = "IsCustomEntry")
    private Boolean isCustomEntry;

    @Column(name = "OrderIndex")
    private Integer orderIndex;

    @Column(name = "CreatedAt")
    private LocalDateTime createdAt;

    @Column(name = "UpdatedAt")
    private LocalDateTime updatedAt;

    @Column(name = "IsDeleted")
    private Boolean isDeleted;

    @Column(name = "DeletedAt")
    private LocalDateTime deletedAt;

}
