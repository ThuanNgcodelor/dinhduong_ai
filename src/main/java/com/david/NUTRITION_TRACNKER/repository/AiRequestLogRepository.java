package com.david.NUTRITION_TRACNKER.repository;

import com.david.NUTRITION_TRACNKER.entity.AiRequestLog;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface AiRequestLogRepository extends JpaRepository<AiRequestLog, Integer> {

    // Tính tổng token đã dùng
    @Query("SELECT SUM(a.tokensUsed) FROM AiRequestLog a")
    Long getTotalTokensUsed();
    
    List<AiRequestLog> findByCreatedAtAfter(LocalDateTime date);
}