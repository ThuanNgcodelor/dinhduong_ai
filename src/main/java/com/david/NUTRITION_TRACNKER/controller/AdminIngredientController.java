package com.david.NUTRITION_TRACNKER.controller;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.david.NUTRITION_TRACNKER.dto.IngredientInputDTO;
import com.david.NUTRITION_TRACNKER.entity.Ingredient;
import com.david.NUTRITION_TRACNKER.entity.IngredientCategory;
import com.david.NUTRITION_TRACNKER.entity.User;
import com.david.NUTRITION_TRACNKER.repository.IngredientCategoryRepository;
import com.david.NUTRITION_TRACNKER.repository.IngredientRepository;
import com.david.NUTRITION_TRACNKER.repository.UserRepository;
import com.david.NUTRITION_TRACNKER.service.AdminIngredientService;

@Controller
@RequestMapping("/admin")
public class AdminIngredientController {

    @Autowired
    private AdminIngredientService adminService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private IngredientRepository ingredientRepo;
    @Autowired
    private IngredientCategoryRepository categoryRepo;

    // --- 1. TRANG QUẢN LÝ NGUYÊN LIỆU ---
    @GetMapping("/ingredients")
    public String listIngredients(Model model,
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(required = false) Integer categoryId,
            @RequestParam(required = false) Boolean status,
            Authentication authentication) {

        // THAY ĐỔI: Lấy size lớn (ví dụ 500 hoặc 1000) để JS xử lý phân trang dưới Client
        Page<Ingredient> pageData = adminService.getIngredients(keyword, categoryId, status, 1, 1000);

        boolean isNutritionist = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_NUTRITIONIST") || a.getAuthority().equals("NUTRITIONIST"));

        model.addAttribute("isNutritionist", isNutritionist);
        model.addAttribute("ingredients", pageData.getContent());
        model.addAttribute("categories", adminService.getAllCategories());

        // Các biến page, totalPages cũ không còn cần thiết cho server-side pagination nữa
        // nhưng giữ lại keyword/filter để form search hoạt động
        model.addAttribute("keyword", keyword);
        model.addAttribute("categoryId", categoryId);
        model.addAttribute("status", status);

        model.addAttribute("newIngredient", new IngredientInputDTO());

        return "admin/ingredients";
    }

    @PostMapping("/ingredients/add")
    public String addIngredient(@ModelAttribute IngredientInputDTO dto, Principal principal, RedirectAttributes ra) {
        try {
            String email = principal.getName();
            User admin = userRepository.findByEmail(email).orElseThrow();
            adminService.createIngredient(dto, admin.getUserId());
            ra.addFlashAttribute("message", "Thêm nguyên liệu mới thành công!");
            ra.addFlashAttribute("messageType", "success");
        } catch (Exception e) {
            ra.addFlashAttribute("message", "Lỗi: " + e.getMessage());
            ra.addFlashAttribute("messageType", "error");
        }
        return "redirect:/admin/ingredients";
    }

    @PostMapping("/ingredients/toggle/{id}")
    public String toggleIngredient(@PathVariable Integer id, RedirectAttributes ra) {
        adminService.toggleStatus(id);
        ra.addFlashAttribute("message", "Cập nhật trạng thái nguyên liệu thành công!");
        ra.addFlashAttribute("messageType", "success");
        return "redirect:/admin/ingredients";
    }

    @PostMapping("/ingredients/delete/{id}")
    public String deleteIngredient(@PathVariable Integer id, RedirectAttributes ra) {
        adminService.deleteIngredient(id);
        ra.addFlashAttribute("message", "Đã xóa nguyên liệu.");
        ra.addFlashAttribute("messageType", "success");
        return "redirect:/admin/ingredients";
    }

    // --- 2. TRANG QUẢN LÝ DANH MỤC ---
    @GetMapping("/categories")
    public String listCategories(Model model, Authentication authentication) {
        boolean isNutritionist = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_NUTRITIONIST") || a.getAuthority().equals("NUTRITIONIST"));

        String sidebarFragment = isNutritionist ? "sidebar_nutritionist" : "sidebar";

        model.addAttribute("isNutritionist", isNutritionist);
        model.addAttribute("categories", adminService.getAllCategories());
        return "admin/categories";
    }

    @PostMapping("/categories/add")
    public String addCategory(@RequestParam String name, RedirectAttributes ra) {
        try {
            adminService.createCategory(name);
            ra.addFlashAttribute("message", "Đã thêm danh mục!");
            ra.addFlashAttribute("messageType", "success");
        } catch (Exception e) {
            ra.addFlashAttribute("messageType", "error");
        }
        return "redirect:/admin/categories";
    }

    // Trong AdminIngredientController.java
    @PostMapping("/categories/delete/{id}")
    public String deleteCategory(@PathVariable Integer id, RedirectAttributes ra) {
        try {
            // Gọi hàm xóa thông minh mới
            String result = adminService.deleteCategorySmart(id);

            if ("HARD".equals(result)) {
                ra.addFlashAttribute("message", "Đã xóa danh mục vĩnh viễn!");
                ra.addFlashAttribute("messageType", "success");
            } else {
                ra.addFlashAttribute("message", "Danh mục đang được sử dụng. Đã chuyển sang Xóa mềm (Lưu trữ).");
                ra.addFlashAttribute("messageType", "warning"); // Màu vàng cảnh báo
            }

        } catch (Exception e) {
            ra.addFlashAttribute("message", "Lỗi xóa danh mục: " + e.getMessage());
            ra.addFlashAttribute("messageType", "error");
        }
        return "redirect:/admin/categories";
    }

    @PostMapping("/categories/update")
    public String updateCategory(@RequestParam Integer id, @RequestParam String name, RedirectAttributes ra) {
        try {
            adminService.updateCategory(id, name);
            ra.addFlashAttribute("message", "Cập nhật danh mục thành công!");
            ra.addFlashAttribute("messageType", "success");
        } catch (Exception e) {
            ra.addFlashAttribute("message", "Lỗi cập nhật danh mục: " + e.getMessage());
            ra.addFlashAttribute("messageType", "error");
        }
        return "redirect:/admin/categories";
    }

    @PostMapping("/ingredients/api/update")
    @ResponseBody
    public ResponseEntity<?> updateIngredientApi(@ModelAttribute IngredientInputDTO input) {
        try {
            Ingredient updated = adminService.updateIngredient(input);

            // [FIX QUAN TRỌNG] 
            // Không trả về Entity 'updated' trực tiếp để tránh lỗi JSON/Hibernate Proxy
            // Thay vào đó, tạo một Map thủ công chỉ chứa dữ liệu cần thiết cho JS
            Map<String, Object> data = new HashMap<>();
            data.put("ingredientId", updated.getIngredientId());
            data.put("name", updated.getName());
            data.put("categoryId", updated.getCategoryId());
            data.put("baseUnit", updated.getBaseUnit());
            data.put("caloriesPer100g", updated.getCaloriesPer100g());
            data.put("protein", updated.getProtein());
            data.put("carbs", updated.getCarbs());
            data.put("fat", updated.getFat());
            data.put("imageUrl", updated.getImageUrl());

            // Xử lý tên Category an toàn
            String categoryName = "Uncategorized";
            // Lưu ý: Cần kiểm tra null kỹ vì Hibernate Proxy có thể gây lỗi nếu truy cập sâu
            if (updated.getIngredientCategory() != null) {
                try {
                    categoryName = updated.getIngredientCategory().getName();
                } catch (Exception e) {
                    // Nếu lỗi proxy thì kệ nó, giữ default
                }
            }

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "data", data, // Trả về Map đơn giản này
                    "categoryName", categoryName
            ));
        } catch (Exception e) { // In lỗi ra console server để debug
            // In lỗi ra console server để debug
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @GetMapping("/ingredient/pending")
    public String viewPendingIngredients(Model model) {
        List<Ingredient> pendingList = ingredientRepo.findByIsActiveFalseOrderByCreatedAtDesc();
        model.addAttribute("ingredients", pendingList);
        return "admin/ingredient-pending-list";
    }

    // 2. XEM CHI TIẾT ĐỂ DUYỆT
    @GetMapping("/ingredient/review/{id}")
    public String reviewIngredient(@PathVariable Integer id, Model model) {
        Ingredient ingredient = ingredientRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid Id:" + id));

        List<IngredientCategory> categories = categoryRepo.findAll();

        model.addAttribute("ingredient", ingredient);
        model.addAttribute("categories", categories);
        return "admin/ingredient-review-detail";
    }

    // 3. XỬ LÝ DUYỆT (APPROVE) - Cập nhật thông tin & Set Active = true
    @PostMapping("/ingredient/approve/{id}")
    public String approveIngredient(@PathVariable Integer id,
            @RequestParam String name,
            @RequestParam Integer categoryId,
            @RequestParam BigDecimal calories,
            @RequestParam BigDecimal protein,
            @RequestParam BigDecimal carbs,
            @RequestParam BigDecimal fat,
            @RequestParam String baseUnit,
            RedirectAttributes ra) {
        try {
            Ingredient ing = ingredientRepo.findById(id).orElseThrow();

            // Cập nhật thông tin chuẩn hóa từ chuyên gia
            ing.setName(name);
            ing.setCategoryId(categoryId);
            ing.setCaloriesPer100g(calories);
            ing.setProtein(protein);
            ing.setCarbs(carbs);
            ing.setFat(fat);
            ing.setBaseUnit(baseUnit);

            ing.setIsActive(true); // KÍCH HOẠT

            ingredientRepo.save(ing);

            ra.addFlashAttribute("message", "Duyệt nguyên liệu thành công!");
            ra.addFlashAttribute("messageType", "success");
        } catch (Exception e) {
            ra.addFlashAttribute("message", "Lỗi khi duyệt: " + e.getMessage());
            ra.addFlashAttribute("messageType", "error");
        }
        return "redirect:/admin/ingredient/pending";
    }

    // 4. TỪ CHỐI (REJECT) - Xóa khỏi DB
    @PostMapping("/ingredient/reject/{id}")
    public String rejectIngredient(@PathVariable Integer id, RedirectAttributes ra) {
        try {
            ingredientRepo.deleteById(id); // Xóa cứng vì nguyên liệu rác chưa dùng ở đâu
            ra.addFlashAttribute("message", "Nguyên liệu đã bị từ chối và xóa bỏ.");
            ra.addFlashAttribute("messageType", "warning");
        } catch (Exception e) {
            ra.addFlashAttribute("message", "Lỗi khi từ chối: " + e.getMessage());
            ra.addFlashAttribute("messageType", "error");
        }
        return "redirect:/admin/ingredient/pending";
    }
}
