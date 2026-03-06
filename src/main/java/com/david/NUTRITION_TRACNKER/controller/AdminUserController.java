package com.david.NUTRITION_TRACNKER.controller;

import java.io.IOException;
import java.security.Principal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.david.NUTRITION_TRACNKER.entity.User;
import com.david.NUTRITION_TRACNKER.entity.enums.UserRole;
import com.david.NUTRITION_TRACNKER.entity.enums.UserStatus;
import com.david.NUTRITION_TRACNKER.repository.UserRepository;
import com.david.NUTRITION_TRACNKER.service.AdminUserService;

import jakarta.servlet.http.HttpServletResponse;

@Controller
@RequestMapping("/admin/users")
public class AdminUserController {

    @Autowired
    private AdminUserService adminUserService;
    @Autowired
    private UserRepository userRepository;

    @GetMapping
    public String listUsers(Model model,
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(required = false) UserRole role,
            @RequestParam(required = false) UserStatus status,
            @RequestParam(defaultValue = "1") int page,
            Principal principal) { // 1. Inject Principal vào đây

        // 2. Lấy User ID của người đang đăng nhập
        String currentEmail = principal.getName();
        User currentUser = userRepository.findByEmail(currentEmail)
                .orElseThrow(() -> new RuntimeException("Current user not found"));

        int pageSize = 5;

        // 3. Truyền ID vào service để loại trừ
        Page<User> userPage = adminUserService.getUsers(currentUser.getUserId(), keyword, role, status, page, pageSize);

        model.addAttribute("allRoles", UserRole.values());
        model.addAttribute("users", userPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", userPage.getTotalPages());
        model.addAttribute("totalItems", userPage.getTotalElements());
        model.addAttribute("keyword", keyword);
        model.addAttribute("role", role);
        model.addAttribute("status", status);

        model.addAttribute("roles", UserRole.values());
        model.addAttribute("statuses", UserStatus.values());
        model.addAttribute("newUser", new User());

        return "admin/users";
    }
    
    @PostMapping("/update")
    public String updateUser(@RequestParam Integer userId,
                             @RequestParam String fullName,
                             @RequestParam UserRole role,
                             RedirectAttributes redirectAttributes) {
        try {
            adminUserService.updateUser(userId, fullName, role);
            redirectAttributes.addFlashAttribute("message", "Cập nhật người dùng thành công!");
            redirectAttributes.addFlashAttribute("messageType", "success");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("message", "Lỗi cập nhật người dùng: " + e.getMessage());
            redirectAttributes.addFlashAttribute("messageType", "error");
        }
        return "redirect:/admin/users";
    }

    @PostMapping("/add")
    public String addUser(@ModelAttribute User newUser,
            @RequestParam("rawPassword") String rawPassword,
            RedirectAttributes redirectAttributes) {
        System.err.println("Create User");
        try {
            // Truyền rawPassword vào service
            adminUserService.createUser(newUser, rawPassword);

            redirectAttributes.addFlashAttribute("message", "Tạo người dùng thành công!");
            redirectAttributes.addFlashAttribute("messageType", "success");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("message", "Lỗi tạo người dùng: " + e.getMessage());
            redirectAttributes.addFlashAttribute("messageType", "error");
        }
        return "redirect:/admin/users";
    }

    @PostMapping("/toggle-status/{id}")
    public String toggleStatus(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        try {
            adminUserService.toggleUserStatus(id);
            redirectAttributes.addFlashAttribute("message", "Cập nhật trạng thái người dùng thành công!");
            redirectAttributes.addFlashAttribute("messageType", "success");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("message", "Lỗi cập nhật trạng thái: " + e.getMessage());
            redirectAttributes.addFlashAttribute("messageType", "error");
        }
        return "redirect:/admin/users";
    }

    @GetMapping("/export")
    public void exportToExcel(HttpServletResponse response) throws IOException {
        adminUserService.exportUsersToExcel(response);
    }

    @PostMapping("/ban/{id}")
    public String banUser(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        try {
            adminUserService.banUser(id);
            redirectAttributes.addFlashAttribute("message", "Người dùng đã bị cấm vĩnh viễn!");
            redirectAttributes.addFlashAttribute("messageType", "success");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("message", "Không thể cấm người dùng: " + e.getMessage());
            redirectAttributes.addFlashAttribute("messageType", "error");
        }
        return "redirect:/admin/users";
    }
}
