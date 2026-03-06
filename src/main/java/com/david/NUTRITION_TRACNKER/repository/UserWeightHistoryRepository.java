package com.david.NUTRITION_TRACNKER.repository;

import com.david.NUTRITION_TRACNKER.entity.UserWeightHistory;
import java.time.LocalDateTime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserWeightHistoryRepository extends JpaRepository<UserWeightHistory, Integer> {
    // Lấy lịch sử để vẽ biểu đồ (nếu cần sau này)
    List<UserWeightHistory> findByUserIdOrderByRecordedAtDesc(Integer userId);
    
    List<UserWeightHistory> findByUserIdOrderByRecordedAtAsc(Integer userId);
    Optional<UserWeightHistory> findByUserIdAndRecordedAtBetween(Integer userId, LocalDateTime start, LocalDateTime end);
}