package com.group02.zaderfood.entity;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.hibernate.annotations.Nationalized;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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
@Table(name = "RecipeIngredients")
public class RecipeIngredient implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "RecipeIngredientId")
    private Integer recipeIngredientId;

    @Column(name = "RecipeId")
    private Integer recipeId;

    @Column(name = "IngredientId")
    private Integer ingredientId;

    @Column(name = "Quantity")
    private BigDecimal quantity;

    @Column(name = "Unit")
    @Nationalized
    private String unit;

    @Column(name = "Note", columnDefinition = "TEXT")
    @Nationalized
    private String note;

    @Column(name = "CreatedAt")
    private LocalDateTime createdAt;

    @Column(name = "UpdatedAt")
    private LocalDateTime updatedAt;

    @Column(name = "IsDeleted")
    private Boolean isDeleted;

    @Column(name = "DeletedAt")
    private LocalDateTime deletedAt;

    // JOIN 
    @ManyToOne
    @JoinColumn(name = "IngredientId", insertable = false, updatable = false)
    private Ingredient ingredient;
    
    @ManyToOne
    @JoinColumn(name = "RecipeId", insertable = false, updatable = false)
    private Recipe recipe;
}
