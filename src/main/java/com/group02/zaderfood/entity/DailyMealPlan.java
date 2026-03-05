package com.group02.zaderfood.entity;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import org.hibernate.annotations.Nationalized;

import com.group02.zaderfood.entity.enums.PlanStatus;

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
@Table(name = "DailyMealPlans")
public class DailyMealPlan implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MealPlanId")
    private Integer mealPlanId;

    @Column(name = "UserId")
    private Integer userId;

    @Column(name = "PlanDate")
    private LocalDate planDate;

    @Column(name = "TotalCalories")
    private BigDecimal totalCalories;

    @Column(name = "IsGeneratedByAI")
    private Boolean isGeneratedByAI;

    @Column(name = "Notes", columnDefinition = "TEXT")
    @Nationalized
    private String notes;   
    
    @Column(name = "TotalProtein")
    private BigDecimal totalProtein;

    @Column(name = "TotalCarbs")
    private BigDecimal totalCarbs;

    @Column(name = "TotalFat")
    private BigDecimal totalFat;

    @Column(name = "Status")
    @Enumerated(EnumType.STRING)
    private PlanStatus status;

    @Column(name = "CreatedAt")
    private LocalDateTime createdAt;

    @Column(name = "UpdatedAt")
    private LocalDateTime updatedAt;

    @Column(name = "IsDeleted")
    private Boolean isDeleted;

    @Column(name = "DeletedAt")
    private LocalDateTime deletedAt;

}
