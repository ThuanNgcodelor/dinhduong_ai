package com.david.NUTRITION_TRACNKER.entity;

import com.david.NUTRITION_TRACNKER.entity.enums.DietType;
import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "UserDietaryPreferences")
public class UserDietaryPreference implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private Integer id;

    @Column(name = "UserId")
    private Integer userId;

    @Column(name = "DietType")
    @Enumerated(EnumType.STRING)
    private DietType dietType;

    @Column(name = "CreatedAt")
    private LocalDateTime createdAt;
    
    // Nếu bạn muốn thiết lập quan hệ object để join bảng dễ dàng:
    // @ManyToOne(fetch = FetchType.LAZY)
    // @JoinColumn(name = "UserId", insertable = false, updatable = false)
    // private User user;
}