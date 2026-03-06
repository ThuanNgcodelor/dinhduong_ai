package com.david.NUTRITION_TRACNKER.controller;

import com.david.NUTRITION_TRACNKER.dto.WeeklyPlanDTO;
import com.david.NUTRITION_TRACNKER.service.AiFoodService;
import com.david.NUTRITION_TRACNKER.service.AsyncJobService;
import com.david.NUTRITION_TRACNKER.service.AsyncJobService.JobResult;
import com.david.NUTRITION_TRACNKER.service.AsyncJobService.JobStatus;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

/**
 * REST endpoints cho luồng tạo thực đơn AI bất đồng bộ (Async).
 * Giải quyết lỗi Cloudflare 524 Timeout khi AI chạy > 100 giây.
 *
 * POST /api/meal-plan/generate-async  → Submit job, trả về jobId ngay lập tức
 * GET  /api/meal-plan/status/{jobId}  → Poll kết quả cho đến khi DONE/ERROR
 */
@RestController
@RequestMapping("/api/meal-plan")
public class MealPlanAsyncController {

    @Autowired
    private AiFoodService aiFoodService;

    @Autowired
    private AsyncJobService asyncJobService;

    /** Bước 1: Nhận request → tạo job → chạy AI trong nền → trả jobId ngay lập tức  */
    @PostMapping("/generate-async")
    public ResponseEntity<?> submitGenerateJob(
            @RequestParam int calories,
            @RequestParam String dietType,
            @RequestParam String goal) {

        String jobId = asyncJobService.createJob();
        aiFoodService.generateWeeklyPlanAsync(jobId, calories, dietType, goal);

        return ResponseEntity.ok(Map.of("jobId", jobId, "status", "PENDING"));
    }

    /** Bước 2: Frontend hỏi trạng thái định kỳ (polling mỗi 5 giây) */
    @GetMapping("/status/{jobId}")
    public ResponseEntity<?> getJobStatus(
            @PathVariable String jobId,
            @RequestParam(required = false) String startDateStr,
            HttpSession session) {

        JobResult job = asyncJobService.getJobResult(jobId);

        if (job.status() == JobStatus.PENDING) {
            return ResponseEntity.ok(Map.of("status", "PENDING"));
        }

        if (job.status() == JobStatus.ERROR) {
            asyncJobService.removeJob(jobId);
            return ResponseEntity.badRequest().body(Map.of(
                    "status", "ERROR",
                    "message", job.errorMessage()
            ));
        }

        // DONE → lưu vào session và trả redirectUrl
        WeeklyPlanDTO plan = job.result();
        asyncJobService.removeJob(jobId); // dọn dẹp sau khi lấy kết quả

        // Xác định ngày bắt đầu
        LocalDate startDate;
        try {
            startDate = (startDateStr != null && !startDateStr.isEmpty())
                    ? LocalDate.parse(startDateStr)
                    : LocalDate.now().plusDays(1);
        } catch (Exception e) {
            startDate = LocalDate.now().plusDays(1);
        }

        // Gán tên ngày Việt
        String[] dayNames = {"Thứ Hai", "Thứ Ba", "Thứ Tư", "Thứ Năm", "Thứ Sáu", "Thứ Bảy", "Chủ Nhật"};
        if (plan.days != null) {
            for (int i = 0; i < plan.days.size(); i++) {
                LocalDate day = startDate.plusDays(i);
                int dow = day.getDayOfWeek().getValue() - 1; // Mon=0…Sun=6
                String label = dayNames[dow] + " (" + day.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM")) + ")";
                plan.days.get(i).dayName = label;
            }
        }

        session.setAttribute("currentWeeklyPlan", plan);

        return ResponseEntity.ok(Map.of(
                "status", "DONE",
                "redirectUrl", "/meal-plan/customize",
                "message", "Tạo kế hoạch tuần thành công!"
        ));
    }
}
