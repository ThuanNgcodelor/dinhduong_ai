package com.david.NUTRITION_TRACNKER.controller;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.david.NUTRITION_TRACNKER.dto.WeeklyPlanDTO;
import com.david.NUTRITION_TRACNKER.service.AiFoodService;
import com.david.NUTRITION_TRACNKER.service.AsyncJobService;
import com.david.NUTRITION_TRACNKER.service.AsyncJobService.JobResult;
import com.david.NUTRITION_TRACNKER.service.AsyncJobService.JobStatus;
import com.david.NUTRITION_TRACNKER.service.MealPlanService;

import jakarta.servlet.http.HttpSession;

/**
 * REST endpoints cho luồng tạo thực đơn AI bất đồng bộ (Async).
 * Giải quyết lỗi Cloudflare 524 Timeout khi AI chạy > 100 giây.
 *
 * POST /api/meal-plan/generate-async          → Submit job AI bất đồng bộ
 * GET  /api/meal-plan/status/{jobId}          → Poll kết quả
 * POST /api/meal-plan/generate-with-recipes   → Phân bổ từ recipe đã chọn (không cần AI)
 */
@RestController
@RequestMapping("/api/meal-plan")
public class MealPlanAsyncController {

    @Autowired
    private AiFoodService aiFoodService;

    @Autowired
    private AsyncJobService asyncJobService;

    @Autowired
    private MealPlanService mealPlanService;

    /** Bước 1: Nhận request → tạo job → chạy AI trong nền → trả jobId ngay lập tức */
    @PostMapping("/generate-async")
    public ResponseEntity<?> submitGenerateJob(
            @RequestParam int calories,
            @RequestParam String dietType,
            @RequestParam String goal,
            @RequestParam(required = false, defaultValue = "") String foodConstraint) {

        String jobId = asyncJobService.createJob();
        aiFoodService.generateWeeklyPlanAsync(jobId, calories, dietType, goal, foodConstraint);

        return ResponseEntity.ok(Map.of("jobId", jobId, "status", "PENDING"));
    }

    /**
     * Phân bổ kế hoạch tuần từ danh sách recipe đã chọn.
     * Không cần AI – thực hiện ngay lập tức và lưu vào session.
     */
    @PostMapping("/generate-with-recipes")
    public ResponseEntity<?> generateWithRecipes(
            @RequestParam List<Integer> recipeIds,
            @RequestParam(required = false, defaultValue = "2000") int calories,
            @RequestParam(required = false) String startDateStr,
            HttpSession session) {

        if (recipeIds == null || recipeIds.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Vui lòng chọn ít nhất 1 công thức."));
        }

        // Xác định ngày bắt đầu
        LocalDate startDate;
        try {
            startDate = (startDateStr != null && !startDateStr.isEmpty())
                    ? LocalDate.parse(startDateStr)
                    : LocalDate.now().plusDays(1);
        } catch (Exception e) {
            startDate = LocalDate.now().plusDays(1);
        }

        try {
            // Xây dựng kế hoạch 7 ngày từ các recipe đã chọn
            WeeklyPlanDTO plan = mealPlanService.buildPlanFromRecipes(recipeIds, calories, startDate);

            // Gắn tên ngày tiếng Việt
            String[] dayNames = {"Thứ Hai", "Thứ Ba", "Thứ Tư", "Thứ Năm", "Thứ Sáu", "Thứ Bảy", "Chủ Nhật"};
            if (plan.days != null) {
                for (int i = 0; i < plan.days.size(); i++) {
                    LocalDate day = startDate.plusDays(i);
                    int dow = day.getDayOfWeek().getValue() - 1;
                    String label = dayNames[dow] + " (" + day.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM")) + ")";
                    plan.days.get(i).dayName = label;
                }
            }

            session.setAttribute("currentWeeklyPlan", plan);

            return ResponseEntity.ok(Map.of(
                    "status", "DONE",
                    "redirectUrl", "/meal-plan/customize",
                    "message", "Đã tạo kế hoạch từ " + recipeIds.size() + " công thức!"
            ));

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                    "message", "Không thể tạo kế hoạch: " + e.getMessage()
            ));
        }
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
        asyncJobService.removeJob(jobId);

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
                int dow = day.getDayOfWeek().getValue() - 1;
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
