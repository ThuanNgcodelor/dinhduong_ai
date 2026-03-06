package com.david.NUTRITION_TRACNKER.controller;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.david.NUTRITION_TRACNKER.dto.CollectionDTO;
import com.david.NUTRITION_TRACNKER.dto.PantryRecipeMatchDTO;
import com.david.NUTRITION_TRACNKER.dto.UnifiedRecipeDTO;
import com.david.NUTRITION_TRACNKER.entity.AiSavedRecipes;
import com.david.NUTRITION_TRACNKER.entity.Recipe;
import com.david.NUTRITION_TRACNKER.entity.RecipeCollection;
import com.david.NUTRITION_TRACNKER.repository.AiSavedRecipeRepository;
import com.david.NUTRITION_TRACNKER.service.CustomUserDetails;
import com.david.NUTRITION_TRACNKER.service.FavoriteService;

@Controller
@RequestMapping("/user/favorites")
public class FavoriteController {

    @Autowired
    private FavoriteService favoriteService;

    @Autowired
    private AiSavedRecipeRepository aiSavedRepo;

    @GetMapping
    public String viewFavorites(Model model,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(name = "view", defaultValue = "recipes") String view,
            @RequestParam(name = "collectionId", required = false) Integer collectionId,
            @RequestParam(name = "keyword", required = false) String keyword,
            @RequestParam(name = "sort", required = false) String sort) {

        if (userDetails == null) {
            return "redirect:/login";
        }
        Integer userId = userDetails.getUserId();

        // VIEW 1: FAVORITE RECIPES
        if ("recipes".equals(view)) {
            List<Recipe> favoriteRecipes = favoriteService.getFavoriteRecipes(userId, keyword, sort);
            model.addAttribute("favoriteRecipes", favoriteRecipes);
        } // VIEW 2: LIST COLLECTIONS
        else if ("collections".equals(view)) {
            List<CollectionDTO> myCollections = favoriteService.getUserCollectionsWithCount(userId);
            model.addAttribute("myCollections", myCollections);
        } // VIEW 3: COLLECTION DETAIL (Dynamic Tab)
        else if ("detail".equals(view) && collectionId != null) {
            Pair<RecipeCollection, List<UnifiedRecipeDTO>> result = favoriteService.getCollectionDetail(userId, collectionId);
            if (result != null) {
                model.addAttribute("selectedCollection", result.getFirst());
                model.addAttribute("collectionRecipes", result.getSecond()); // Danh sách hỗn hợp
            } else {
                return "redirect:/user/favorites?view=collections";
            }
        }

        model.addAttribute("activeTab", view);
        return "user/my-favorite-collection";
    }

    // Action: Tạo Collection (Có Toast)
    @PostMapping("/collections/create")
    public String createCollection(@AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam("name") String name,
            RedirectAttributes redirectAttributes) {
        if (userDetails == null) {
            return "redirect:/login";
        }
        
        if (name != null && !name.trim().isEmpty()) {
            favoriteService.createCollection(userDetails.getUserId(), name.trim());
            redirectAttributes.addFlashAttribute("successMessage", "Đã tạo bộ sưu tập thành công!");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Tên bộ sưu tập không hợp lệ!");
        }
        return "redirect:/user/favorites?view=collections";
    }

    // Action: Xóa Recipe khỏi Collection cụ thể (Có Toast)
    @PostMapping("/collections/remove-item")
    public String removeItemFromCollection(@AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam("collectionId") Integer collectionId,
            @RequestParam("recipeId") Integer recipeId,
            @RequestParam(name = "isAi", defaultValue = "false") boolean isAi, // Thêm param này
            RedirectAttributes redirectAttributes) {
        if (userDetails != null) {
            favoriteService.removeRecipeFromCollection(userDetails.getUserId(), collectionId, recipeId, isAi);
            redirectAttributes.addFlashAttribute("successMessage", "Đã xóa công thức khỏi bộ sưu tập.");
        }
        return "redirect:/user/favorites?view=detail&collectionId=" + collectionId;
    }

    // Action: Xóa Recipe khỏi Favorites gốc (Giữ nguyên logic cũ, thêm toast)
    @PostMapping("/remove")
    public String removeRecipe(@AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam("recipeId") Integer recipeId,
            RedirectAttributes redirectAttributes) {
        if (userDetails != null) {
            favoriteService.removeFromFavorites(userDetails.getUserId(), recipeId);
            redirectAttributes.addFlashAttribute("successMessage", "Đã xóa khỏi danh sách yêu thích.");
        }
        return "redirect:/user/favorites?view=recipes";
    }

    @PostMapping("/collections/share")
    @ResponseBody // Trả về JSON/String thay vì View
    public ResponseEntity<?> shareCollection(@AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam("collectionId") Integer collectionId) {
        if (userDetails != null) {
            String result = favoriteService.toggleShareCollection(userDetails.getUserId(), collectionId);
            if ("SUCCESS".equals(result)) {
                // Trả về URL đầy đủ để frontend copy
                String shareUrl = "/collections/view/" + collectionId;
                return ResponseEntity.ok(shareUrl);
            }
        }
        return ResponseEntity.badRequest().body("Lỗi khi chia sẻ bộ sưu tập");
    }

    @PostMapping("/collections/delete")
    public String deleteCollection(@AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam("collectionId") Integer collectionId,
            RedirectAttributes redirectAttributes) {
        if (userDetails != null) {
            try {
                favoriteService.deleteCollection(userDetails.getUserId(), collectionId);
                redirectAttributes.addFlashAttribute("successMessage", "Đã xóa bộ sưu tập thành công.");
            } catch (Exception e) {
                redirectAttributes.addFlashAttribute("errorMessage", "Không thể xóa bộ sưu tập.");
            }
        }
        return "redirect:/user/favorites?view=collections";
    }

    @GetMapping("/ai-detail/{id}")
    public String viewAiSavedDetail(@PathVariable Integer id,
            @RequestParam(required = false) Integer collectionId, // [NEW] Nhận ID collection
            Model model) {
        AiSavedRecipes saved = aiSavedRepo.findById(id).orElse(null);
        if (saved == null) {
            return "redirect:/user/favorites";
        }

        // Convert DTO... (Code cũ giữ nguyên)
        PantryRecipeMatchDTO dto = new PantryRecipeMatchDTO();
        dto.setName(saved.getName());
        dto.setDescription(saved.getDescription());
        dto.setCalories(saved.getTotalCalories());
        dto.setTimeMin(saved.getTimeMinutes());
        dto.setServings(saved.getServings());
        dto.setImageUrl(saved.getImageUrl());

        if (saved.getIngredientsText() != null) {
            dto.setIngredientsList(Arrays.asList(saved.getIngredientsText().split("\n")));
        }
        if (saved.getStepsText() != null) {
            dto.setStepsList(Arrays.asList(saved.getStepsText().split("\n")));
        }

        model.addAttribute("recipe", dto);
        model.addAttribute("isSavedView", true); // [QUAN TRỌNG] Ẩn nút Save

        // [CONFIG NÚT BACK THÔNG MINH]
        if (collectionId != null) {
            // Nếu có collectionId -> Quay về đúng collection đó
            model.addAttribute("backUrl", "/user/favorites?view=detail&collectionId=" + collectionId);
            model.addAttribute("backLabel", "Về Bộ Sưu Tập");
        } else {
            // Mặc định -> Quay về danh sách Recipes
            model.addAttribute("backUrl", "/user/favorites?view=recipes");
            model.addAttribute("backLabel", "Về Yêu Thích");
        }

        return "user/aiRecipeDetail";
    }
}
