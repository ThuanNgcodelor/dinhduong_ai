package com.david.NUTRITION_TRACNKER.controller;

import java.time.LocalDate;
import java.time.Period;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.david.NUTRITION_TRACNKER.dto.NutritionCalculatorDTO;
import com.david.NUTRITION_TRACNKER.dto.UserProfileDTO;
import com.david.NUTRITION_TRACNKER.service.CustomUserDetails;
import com.david.NUTRITION_TRACNKER.service.UserService;

@Controller
@RequestMapping("/nutrition")
public class NutritionController {

    @Autowired
    private UserService userService;

    // 1. Hiển thị trang Calculator
    @GetMapping("/calculator")
    public String showCalculator(Model model, @AuthenticationPrincipal CustomUserDetails currentUser) {
        NutritionCalculatorDTO dto = new NutritionCalculatorDTO();

        // Nếu user đã đăng nhập, pre-fill dữ liệu từ profile có sẵn
        if (currentUser != null) {
            try {
                UserProfileDTO profile = userService.getUserProfile(currentUser.getUserId());
                dto.setWeightKg(profile.getWeightKg());
                dto.setHeightCm(profile.getHeightCm());
                dto.setGender(profile.getGender());
                dto.setActivityLevel(profile.getActivityLevel());
                
                // Tính tuổi từ ngày sinh
                if (profile.getBirthDate() != null) {
                    dto.setAge(Period.between(profile.getBirthDate(), LocalDate.now()).getYears());
                }
            } catch (Exception e) {
                // Ignore error if profile not found
            }
        }
        
        // Mặc định Goal là Maintain
        if (dto.getGoal() == null) dto.setGoal("MAINTAIN");
        
        model.addAttribute("calculatorDTO", dto);
        return "user/nutritionCalculator";
    }

    // 2. Xử lý nút "Calculate"
    @PostMapping("/calculate")
    public String calculate(@ModelAttribute("calculatorDTO") NutritionCalculatorDTO dto, Model model) {
        // Gọi service tính toán
        NutritionCalculatorDTO result = userService.calculateNutrition(dto);
        
        // Đẩy lại kết quả ra view
        model.addAttribute("calculatorDTO", result);
        model.addAttribute("calculated", true); // Cờ để hiển thị phần kết quả
        
        return "user/nutritionCalculator";
    }

    // 3. Xử lý nút "Save to Profile"
    @PostMapping("/save")
    public String saveToProfile(@ModelAttribute("calculatorDTO") NutritionCalculatorDTO dto,
                                @AuthenticationPrincipal CustomUserDetails currentUser,
                                RedirectAttributes redirectAttributes) {
        if (currentUser == null) {
            return "redirect:/login";
        }

        try {
            userService.saveNutritionProfile(currentUser.getUserId(), dto);
            redirectAttributes.addFlashAttribute("successMessage", "Đã lưu mục tiêu dinh dưỡng vào hồ sơ của bạn!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi lưu hồ sơ.");
        }
        
        return "redirect:/user/settings"; // Chuyển hướng về trang cài đặt sau khi lưu
    }
}