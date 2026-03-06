package com.david.NUTRITION_TRACNKER.service;

import com.david.NUTRITION_TRACNKER.dto.WeeklyPlanDTO;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Lưu trữ trạng thái các công việc AI chạy nền (in-memory).
 * Mỗi job có: ID, trạng thái (PENDING / DONE / ERROR) và kết quả.
 */
@Service
public class AsyncJobService {

    public enum JobStatus { PENDING, DONE, ERROR }

    public record JobResult(JobStatus status, WeeklyPlanDTO result, String errorMessage) {}

    private final Map<String, JobResult> jobs = new ConcurrentHashMap<>();

    /** Tạo job mới, trả về jobId */
    public String createJob() {
        String jobId = UUID.randomUUID().toString();
        jobs.put(jobId, new JobResult(JobStatus.PENDING, null, null));
        return jobId;
    }

    /** Cập nhật job sau khi AI xong */
    public void completeJob(String jobId, WeeklyPlanDTO result) {
        jobs.put(jobId, new JobResult(JobStatus.DONE, result, null));
    }

    /** Cập nhật job nếu AI bị lỗi */
    public void failJob(String jobId, String errorMessage) {
        jobs.put(jobId, new JobResult(JobStatus.ERROR, null, errorMessage));
    }

    /** Lấy kết quả của job */
    public JobResult getJobResult(String jobId) {
        return jobs.getOrDefault(jobId, new JobResult(JobStatus.ERROR, null, "Job không tồn tại"));
    }

    /** Xóa job sau khi đã dùng xong */
    public void removeJob(String jobId) {
        jobs.remove(jobId);
    }
}
