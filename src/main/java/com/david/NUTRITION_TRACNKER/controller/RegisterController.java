package com.david.NUTRITION_TRACNKER.controller;

import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam; // <--- Import này
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.david.NUTRITION_TRACNKER.dto.UserRegisterDTO;
import com.david.NUTRITION_TRACNKER.entity.User;
import com.david.NUTRITION_TRACNKER.entity.enums.AuthProvider;
import com.david.NUTRITION_TRACNKER.repository.UserRepository;
import com.david.NUTRITION_TRACNKER.service.EmailService;
import com.david.NUTRITION_TRACNKER.service.UserService;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/register")
public class RegisterController {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmailService emailService;

    // --- INPUT EMAIL ---
    @GetMapping("/step1")
    public String showStep1() {
        return "auth/register-step1";
    }

    @PostMapping("/step1")
    public String processStep1(@RequestParam("email") String email,
            HttpSession session,
            Model model,
            RedirectAttributes redirectAttributes) {
        
        java.util.Optional<User> existingUser = userRepository.findByEmail(email);
        if (existingUser.isPresent()) {
            if (existingUser.get().getAuthProvider() == AuthProvider.GOOGLE) {
                model.addAttribute("error", "Bạn đã đăng ký bằng google, vui lòng login bằng google giúp tôi");
            } else {
                model.addAttribute("error", "Email này đã được sử dụng!");
            }
            return "auth/register-step1";
        }

        String otp = String.format("%06d", new java.util.Random().nextInt(999999));
        try {
            emailService.sendOtpEmail(email, otp);
        } catch (Exception e) {
            model.addAttribute("error", "Lỗi gửi email: " + e.getMessage());
            return "auth/register-step1";
        }

        session.setAttribute("regEmail", email);
        session.setAttribute("regOtp", otp);

        redirectAttributes.addFlashAttribute("message", "Mã xác nhận đã được gửi đến email của bạn!");

        return "redirect:/register/step2";
    }

    // --- INPUT OTP ---
    @GetMapping("/step2")
    public String showStep2(HttpSession session) {
        if (session.getAttribute("regEmail") == null) {
            return "redirect:/register/step1";
        }
        return "auth/register-step2";
    }

    @PostMapping("/step2")
    public String processStep2(@RequestParam("otp") String otp,
            HttpSession session,
            Model model,
            RedirectAttributes redirectAttributes) {
        String sessionOtp = (String) session.getAttribute("regOtp");

        if (sessionOtp != null && sessionOtp.equals(otp)) {
            session.setAttribute("isVerified", true);
            redirectAttributes.addFlashAttribute("message", "Xác nhận thành công! Vui lòng thiết lập tài khoản của bạn.");
            return "redirect:/register/step3";
        }

        model.addAttribute("error", "Mã OTP không chính xác!");
        return "auth/register-step2";
    }

    // --- INPUT INFO ---
    @GetMapping("/step3")
    public String showStep3(HttpSession session, Model model) {
        if (session.getAttribute("isVerified") == null) {
            return "redirect:/register/step1";
        }
        model.addAttribute("user", new UserRegisterDTO());
        return "auth/register-step3";
    }

    @PostMapping("/step3")
    public String processStep3(@ModelAttribute("user") UserRegisterDTO dto,
            HttpSession session,
            Model model,
            RedirectAttributes redirectAttributes) {
        String email = (String) session.getAttribute("regEmail");
        dto.setEmail(email);

        if (!dto.getPassword().equals(dto.getConfirmPassword())) {
            model.addAttribute("error", "Mật khẩu xác nhận không khớp!");
            return "auth/register-step3";
        }

        try {
            userService.registerUser(dto);

            session.removeAttribute("regEmail");
            session.removeAttribute("regOtp");
            session.removeAttribute("isVerified");

            redirectAttributes.addFlashAttribute("message", "Đăng ký thành công! Vui lòng đăng nhập.");

            return "redirect:/login";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "auth/register-step3";
        }
    }
    
    // --- RESEND OTP ---
    @GetMapping("/resend")
    public String resendOtp(HttpSession session, RedirectAttributes redirectAttributes) {
        String email = (String) session.getAttribute("regEmail");
        
        if (email == null) {
            return "redirect:/register/step1";
        }
        
        String newOtp = String.format("%06d", new Random().nextInt(999999));

        try {
            emailService.sendOtpEmail(email, newOtp);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi gửi email: " + e.getMessage());
            return "redirect:/register/step2";
        }

        session.setAttribute("regOtp", newOtp);

        redirectAttributes.addFlashAttribute("message", "Mã xác nhận mới đã được gửi!");
        
        return "redirect:/register/step2";
    }
}
