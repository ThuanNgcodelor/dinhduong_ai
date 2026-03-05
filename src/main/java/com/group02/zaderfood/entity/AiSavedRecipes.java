package com.group02.zaderfood.entity;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.hibernate.annotations.Nationalized;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
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
@Table(name = "AiSavedRecipes")
public class AiSavedRecipes implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "AiRecipeId")
    private Integer aiRecipeId;

    @Column(name = "UserId")
    private Integer userId;

    @Column(name = "Name")
    @Nationalized
    private String name;

    @Column(name = "Description", columnDefinition = "TEXT")
    @Nationalized
    private String description;

    // Lưu danh sách dưới dạng văn bản (VD: "Trứng: 2 quả\nSữa: 100ml")
    @Column(name = "IngredientsText", columnDefinition = "TEXT")
    @Nationalized
    private String ingredientsText;

    @Column(name = "StepsText", columnDefinition = "TEXT")
    @Nationalized
    private String stepsText;

    @Column(name = "TimeMinutes")
    private Integer timeMinutes;

    @Column(name = "TotalCalories")
    private BigDecimal totalCalories;

    @Column(name = "Servings")
    private Integer servings;

    @Column(name = "ImageUrl")
    private String imageUrl;

    @Column(name = "SavedAt")
    private LocalDateTime savedAt;
    
    // Relation (Optional, nếu cần lấy thông tin User)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "UserId", insertable = false, updatable = false)
    private User user;
}