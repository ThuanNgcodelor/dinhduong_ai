package com.group02.zaderfood.entity;

import java.io.Serializable;
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
import lombok.ToString;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "Reviews")
public class Review implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ReviewId")
    private Integer reviewId;

    @Column(name = "UserId")
    private Integer userId;

    @Column(name = "RecipeId")
    private Integer recipeId;

    @Column(name = "Rating")
    private Integer rating;

    @Column(name = "Comment", columnDefinition = "TEXT")
    @Nationalized
    private String comment;

    @Column(name = "CreatedAt")
    private LocalDateTime createdAt;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "RecipeId", insertable = false, updatable = false)
    @ToString.Exclude
    private Recipe recipe;
    
}
