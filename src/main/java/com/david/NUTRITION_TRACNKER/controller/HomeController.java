package com.david.NUTRITION_TRACNKER.controller;

import com.david.NUTRITION_TRACNKER.entity.Recipe;
import com.david.NUTRITION_TRACNKER.entity.UserProfile;
import com.david.NUTRITION_TRACNKER.repository.UserProfileRepository;
import com.david.NUTRITION_TRACNKER.service.CustomUserDetails;
import com.david.NUTRITION_TRACNKER.service.RecipeService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @Autowired
    private RecipeService recipeService;
    
    @Autowired
    private UserProfileRepository userProfileRepository;

    @GetMapping("/")
    public String home(Model model, @AuthenticationPrincipal CustomUserDetails user) {
        
        // Kiểm tra nếu user đã đăng nhập
        if (user != null) {
            UserProfile profile = userProfileRepository.findById(user.getUserId()).orElse(null);
            model.addAttribute("userProfile", profile);
            
            // Logic kiểm tra nhanh để biến này có sẵn cho View dùng
            boolean isProfileMissing = profile == null 
                                    || profile.getBmr() == null 
                                    || profile.getTdee() == null 
                                    || profile.getCalorieGoalPerDay() == null;
            
            model.addAttribute("isProfileMissing", isProfileMissing);
        } else {
            model.addAttribute("isProfileMissing", false); // Chưa đăng nhập thì không hiện nhắc nhở này
        }

        List<Recipe> newRecipes = recipeService.getLatestRecipes(8);
        model.addAttribute("newRecipes", newRecipes);
        return "home";
    }
    
    @GetMapping("/about")
    public String about() {
        return "about";
    }

    @GetMapping("/feedback")
    public String feedback() {
        return "feedback";
    }
    
    @GetMapping("/privacy-policy")
    public String privacyPolicy() {
        return "privacy-policy";
    }

    @GetMapping("/terms-of-service")
    public String termsOfService() {
        return "terms-of-service";
    }
}