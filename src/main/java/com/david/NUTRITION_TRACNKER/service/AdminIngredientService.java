package com.david.NUTRITION_TRACNKER.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.david.NUTRITION_TRACNKER.dto.IngredientInputDTO;
import com.david.NUTRITION_TRACNKER.entity.Ingredient;
import com.david.NUTRITION_TRACNKER.entity.IngredientCategory;
import com.david.NUTRITION_TRACNKER.repository.IngredientCategoryRepository;
import com.david.NUTRITION_TRACNKER.repository.IngredientRepository;

@Service
public class AdminIngredientService {

    @Autowired
    private IngredientRepository ingredientRepository;
    @Autowired
    private IngredientCategoryRepository categoryRepository;
    @Autowired
    private IngredientService ingredientService; // Tái sử dụng logic thêm mới
    @Autowired
    private FileStorageService fileStorageService;

    // 1. Tìm kiếm và phân trang
    public Page<Ingredient> getIngredients(String keyword, Integer categoryId, Boolean isActive, int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by("createdAt").descending());
        return ingredientRepository.searchIngredients(keyword, categoryId, isActive, pageable);
    }

    // 2. Lấy tất cả danh mục (cho dropdown filter)
    public List<IngredientCategory> getAllCategories() {
        return categoryRepository.findAll();
    }

    // 3. Thêm mới nguyên liệu (Gọi lại service cũ để tái sử dụng logic upload ảnh)
    public void createIngredient(IngredientInputDTO dto, Integer adminId) {
        ingredientService.createSystemIngredient(dto, adminId);
    }

    // 4. Đổi trạng thái Active/Inactive
    public void toggleStatus(Integer id) {
        Ingredient ing = ingredientRepository.findById(id).orElseThrow(() -> new RuntimeException("Không tìm thấy nguyên liệu"));
        ing.setIsActive(!Boolean.TRUE.equals(ing.getIsActive()));
        ingredientRepository.save(ing);
    }

    // 5. Xóa mềm (Soft Delete)
    public void deleteIngredient(Integer id) {
        Ingredient ing = ingredientRepository.findById(id).orElseThrow(() -> new RuntimeException("Không tìm thấy nguyên liệu"));
        ing.setIsDeleted(true);
        ing.setDeletedAt(LocalDateTime.now());
        ingredientRepository.save(ing);
    }

    // --- QUẢN LÝ CATEGORY ---
    public void createCategory(String name) {
        IngredientCategory cat = new IngredientCategory();
        cat.setName(name);
        cat.setCreatedAt(LocalDateTime.now());
        cat.setIsDeleted(false);
        categoryRepository.save(cat);
    }

    /**
     * Ưu tiên Xóa Cứng (Hard Delete). Nếu dính khóa ngoại (đang được sử dụng)
     * -> Chuyển sang Xóa Mềm (Soft Delete).
     *
     * @return String thông báo trạng thái để Controller biết đường hiển thị
     */
    @Transactional
    public String deleteCategorySmart(Integer id) {
        IngredientCategory cat = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy danh mục"));

        cat.setIsDeleted(true);
        cat.setDeletedAt(LocalDateTime.now());
        categoryRepository.save(cat);
        System.out.print("SOFT");
       
        return null;
    }

    public void updateCategory(Integer id, String newName) {
        IngredientCategory cat = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy danh mục"));

        cat.setName(newName);
        cat.setUpdatedAt(LocalDateTime.now());

        categoryRepository.save(cat);
    }

    @Transactional
    public Ingredient updateIngredient(IngredientInputDTO input) {
        // 1. Kiểm tra ID
        if (input.getIngredientId() == null) {
            throw new RuntimeException("Cần có ID nguyên liệu để cập nhật");
        }

        // 2. Tìm nguyên liệu cũ
        Ingredient existing = ingredientRepository.findById(input.getIngredientId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nguyên liệu"));

        // 3. Cập nhật thông tin cơ bản
        // Ưu tiên lấy 'name' (từ form edit), nếu null thì lấy 'newName' (dự phòng)
        String nameToUpdate = (input.getName() != null && !input.getName().isEmpty())
                ? input.getName()
                : input.getNewName();
        existing.setName(nameToUpdate);

        existing.setCategoryId(input.getCategoryId());
        existing.setBaseUnit(input.getBaseUnit());

        // Cập nhật Macros
        existing.setCaloriesPer100g(input.getCaloriesPer100g());
        existing.setProtein(input.getProtein());
        existing.setCarbs(input.getCarbs());
        existing.setFat(input.getFat());

        existing.setUpdatedAt(LocalDateTime.now());

        // 4. Xử lý ảnh (nếu có upload ảnh mới)
        // Kiểm tra cả 2 trường file (do sự khác biệt tên giữa form add/edit)
        MultipartFile fileToSave = (input.getImageFile() != null && !input.getImageFile().isEmpty())
                ? input.getImageFile()
                : input.getNewIngredientImage();

        if (fileToSave != null && !fileToSave.isEmpty()) {
            String newImageUrl = fileStorageService.storeFile(fileToSave);
            existing.setImageUrl(newImageUrl);
        }

        return ingredientRepository.save(existing);
    }
}
