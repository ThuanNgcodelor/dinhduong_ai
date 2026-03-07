package com.david.NUTRITION_TRACNKER.entity;

import java.io.Serializable;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
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
@Table(name = "AiRequestLogs")
public class AiRequestLog implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "RequestId")
    private Integer requestId;

    @Column(name = "UserId")
    private Integer userId;

    @Column(name = "RequestType")
    private String requestType;

    @Column(name = "PromptContent")
    private String promptContent;

    @Lob
    @Column(name = "AiResponse", columnDefinition = "LONGTEXT")
    private String aiResponse;

    @Column(name = "TokensUsed")
    private Integer tokensUsed;

    @Column(name = "CreatedAt")
    private LocalDateTime createdAt;

}
