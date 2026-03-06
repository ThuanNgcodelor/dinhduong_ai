package com.david.NUTRITION_TRACNKER.controller;

import com.david.NUTRITION_TRACNKER.dto.AdminDashboardDTO;
import com.david.NUTRITION_TRACNKER.dto.NutritionistDashboardDTO;
import com.david.NUTRITION_TRACNKER.service.AdminService;
import com.david.NUTRITION_TRACNKER.service.CustomUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private AdminService adminService;

    @GetMapping("/dashboard")
    public String dashboard(Model model, @AuthenticationPrincipal CustomUserDetails currentUser) {
        if (currentUser.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_NUTRITIONIST") || a.getAuthority().equals("NUTRITIONIST"))) {

            // Gọi logic tính toán RIÊNG
            NutritionistDashboardDTO nutriStats = adminService.getNutritionistStats();
            model.addAttribute("stats", nutriStats);

            // Trả về view RIÊNG
            return "admin/nutritionist_dashboard";
        }

        // Logic cho Admin hệ thống (Giữ nguyên cái cũ)
        AdminDashboardDTO stats = adminService.getDashboardStats();
        model.addAttribute("stats", stats);
        return "admin/admin_dashboard";
    }
}
