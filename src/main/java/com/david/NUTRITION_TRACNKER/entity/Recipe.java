package com.david.NUTRITION_TRACNKER.entity;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.hibernate.annotations.Nationalized;
import org.springframework.web.multipart.MultipartFile;

import com.david.NUTRITION_TRACNKER.entity.enums.DifficultyLevel;
import com.david.NUTRITION_TRACNKER.entity.enums.RecipeStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "Recipes")
public class Recipe implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "RecipeId")
    private Integer recipeId;

    @Column(name = "Name")
    @Nationalized
    private String name;

    @Column(name = "Description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "Difficulty")
    @Nationalized
    @Enumerated(EnumType.STRING)
    private DifficultyLevel difficulty;

    @Column(name = "PrepTimeMin")
    private Integer prepTimeMin;

    @Column(name = "CookTimeMin")
    private Integer cookTimeMin;

    @Column(name = "Servings")
    private Integer servings;

    @Column(name = "TotalCalories")
    private BigDecimal totalCalories;

    // Khối lượng 1 phần ăn tính theo gram (VD: 1 phần = 200g)
    // Dùng để tính calo khi người dùng nhập gram tùy ý (VD: 400g = 2 × calo/phần)
    @Column(name = "ServingWeightGrams")
    private Integer servingWeightGrams;

    @Column(name = "ImageUrl")
    private String imageUrl;
    
    @Column(name = "IsNutritionist")
    private boolean isNutritionist;

    @Column(name = "Status")
    @Enumerated(EnumType.STRING)
    private RecipeStatus status;

    @Column(name = "CreatedByUserId")
    private Integer createdByUserId;

    @Column(name = "CreatedAt")
    private LocalDateTime createdAt;

    @Column(name = "UpdatedAt")
    private LocalDateTime updatedAt;

    @Column(name = "IsDeleted")
    private Boolean isDeleted;

    @Column(name = "DeletedAt")
    private LocalDateTime deletedAt;
    
    //
    @Transient
    private BigDecimal protein;

    @Transient
    private BigDecimal carbs;

    @Transient
    private BigDecimal fat;
    
    @Transient
    private MultipartFile imageFile;

    // JOIN
    @OneToMany(mappedBy = "recipe")
    private List<RecipeIngredient> recipeIngredients;

    @OneToMany(mappedBy = "recipe")
    private List<RecipeStep> recipeSteps;

    @ManyToOne
    @JoinColumn(name = "CreatedByUserId", insertable = false, updatable = false)
    private User user;
}
