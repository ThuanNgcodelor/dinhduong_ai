package com.david.NUTRITION_TRACNKER.entity;

import java.io.Serializable;
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
@Table(name = "RecipeSteps")
public class RecipeStep implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "StepId")
    private Integer stepId;

    @Column(name = "RecipeId")
    private Integer recipeId;

    @Column(name = "StepNumber")
    private Integer stepNumber;
    
    @Nationalized
    @Column(name = "Instruction", columnDefinition = "TEXT")
    private String instruction;

    @Column(name = "MediaUrl")
    private String mediaUrl;

    @Column(name = "CreatedAt")
    private LocalDateTime createdAt;
    @Column(name = "UpdatedAt")
    private LocalDateTime updatedAt;

    
    // JOIN
    @ManyToOne
    @JoinColumn(name = "RecipeId", insertable = false, updatable = false)
    private Recipe recipe;

}
