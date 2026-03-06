package com.david.NUTRITION_TRACNKER.config;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.david.NUTRITION_TRACNKER.entity.Ingredient;
import com.david.NUTRITION_TRACNKER.entity.IngredientCategory;
import com.david.NUTRITION_TRACNKER.entity.Recipe;
import com.david.NUTRITION_TRACNKER.entity.RecipeIngredient;
import com.david.NUTRITION_TRACNKER.entity.RecipeStep;
import com.david.NUTRITION_TRACNKER.entity.User;
import com.david.NUTRITION_TRACNKER.entity.UserPantry;
import com.david.NUTRITION_TRACNKER.entity.enums.DifficultyLevel;
import com.david.NUTRITION_TRACNKER.entity.enums.RecipeStatus;
import com.david.NUTRITION_TRACNKER.entity.enums.UserRole;
import com.david.NUTRITION_TRACNKER.entity.enums.UserStatus;
import com.david.NUTRITION_TRACNKER.repository.IngredientCategoryRepository;
import com.david.NUTRITION_TRACNKER.repository.IngredientRepository;
import com.david.NUTRITION_TRACNKER.repository.RecipeIngredientRepository;
import com.david.NUTRITION_TRACNKER.repository.RecipeRepository;
import com.david.NUTRITION_TRACNKER.repository.RecipeStepRepository;
import com.david.NUTRITION_TRACNKER.repository.UserPantryRepository;
import com.david.NUTRITION_TRACNKER.repository.UserRepository;

@Configuration
public class DataInitializer {

    private static final String DEFAULT_PASSWORD = "Thuannguyen@417";

    // =====================================================================
    // BEAN 1 — Tài khoản test
    // =====================================================================
    @Bean(name = "seedAccounts")
    public CommandLineRunner seedAccounts(UserRepository userRepo, PasswordEncoder encoder) {
        return args -> {
            System.out.println("\n══════════ [Seed] Accounts ══════════");
            createUser(userRepo, encoder, "admin@dev.com",                 "Quản Trị Viên",             UserRole.ADMIN);
            createUser(userRepo, encoder, "nu@dev.com",                    "Chuyên Gia Dinh Dưỡng",     UserRole.NUTRITIONIST);
            createUser(userRepo, encoder, "user@dev.com",                  "Người Dùng Demo",            UserRole.USER);
            createUser(userRepo, encoder, "nguyentrungthuan417@gmail.com", "Nguyễn Trung Thuận",         UserRole.USER);
        };
    }

    // =====================================================================
    // BEAN 2 — Categories + Ingredients  (find-or-create by name)
    // =====================================================================
    @Bean(name = "seedIngredients")
    public CommandLineRunner seedIngredients(IngredientCategoryRepository catRepo,
                                              IngredientRepository ingRepo) {
        return args -> {
            System.out.println("\n══════════ [Seed] Ingredients ══════════");
            LocalDateTime now = LocalDateTime.now();

            // ── Categories (find-or-create) ──────────────────────────────
            Map<String, Integer> catIds = new LinkedHashMap<>();
            String[] catNames = {
                "🥩 Thịt & Hải sản", "🥗 Rau củ & Nấm",
                "🍚 Tinh bột & Ngũ cốc", "🥛 Sữa & Trứng",
                "🌿 Gia vị & Dầu", "🫘 Đậu & Hạt", "🍌 Trái cây"
            };
            for (String catName : catNames) {
                IngredientCategory existing = catRepo.findAll().stream()
                        .filter(c -> catName.equals(c.getName())).findFirst().orElse(null);
                if (existing == null) {
                    IngredientCategory c = new IngredientCategory();
                    c.setName(catName); c.setCreatedAt(now); c.setUpdatedAt(now); c.setIsDeleted(false);
                    existing = catRepo.save(c);
                    System.out.println("  ➕ Category: " + catName);
                }
                catIds.put(catName, existing.getCategoryId());
            }

            Integer cMeat  = catIds.get("🥩 Thịt & Hải sản");
            Integer cVeg   = catIds.get("🥗 Rau củ & Nấm");
            Integer cCarb  = catIds.get("🍚 Tinh bột & Ngũ cốc");
            Integer cDairy = catIds.get("🥛 Sữa & Trứng");
            Integer cSpice = catIds.get("🌿 Gia vị & Dầu");
            Integer cBean  = catIds.get("🫘 Đậu & Hạt");
            Integer cFruit = catIds.get("🍌 Trái cây");

            // ── Ingredients (find-or-create by exact name) ───────────────
            // Dùng name làm khoá duy nhất
            Map<String, Integer> existingNames = new HashMap<>();
            for (Ingredient i : ingRepo.findAll()) {
                if (i.getName() != null) existingNames.put(i.getName(), i.getIngredientId());
            }

            List<Object[]> defs = new ArrayList<>();
            // format: name, cal, protein, carbs, fat, unit, catId
            // ── Thịt & Hải sản
            defs.add(d("Thịt bò nạc thăn",           143, 26.0,  0.0,  3.7, "g",    cMeat));
            defs.add(d("Thịt bò băm nạc 95%",         137, 21.4,  0.0,  5.0, "g",    cMeat));
            defs.add(d("Thịt heo nạc thăn",           143, 26.0,  0.0,  3.5, "g",    cMeat));
            defs.add(d("Thịt heo vai 80% nạc",        160, 20.0,  0.0,  8.0, "g",    cMeat));
            defs.add(d("Thịt gà ức không da",         110, 23.1,  0.0,  1.2, "g",    cMeat));
            defs.add(d("Thịt gà đùi không da",        150, 22.0,  0.0,  6.3, "g",    cMeat));
            defs.add(d("Cá hồi Na Uy",                206, 20.0,  0.0, 13.0, "g",    cMeat));
            defs.add(d("Cá ngừ tươi",                 132, 28.2,  0.0,  1.0, "g",    cMeat));
            defs.add(d("Cá tra phi lê",                90, 18.0,  0.0,  2.0, "g",    cMeat));
            defs.add(d("Cá basa phi lê",               82, 17.5,  0.0,  1.5, "g",    cMeat));
            defs.add(d("Cá diêu hồng",                 97, 20.0,  0.0,  2.0, "g",    cMeat));
            defs.add(d("Tôm sú bóc vỏ",               85, 18.0,  1.0,  0.9, "g",    cMeat));
            defs.add(d("Tôm thẻ chân trắng",           81, 17.5,  0.5,  0.8, "g",    cMeat));
            defs.add(d("Mực tươi",                     82, 15.0,  2.0,  1.0, "g",    cMeat));
            defs.add(d("Cua biển",                     87, 18.1,  0.0,  1.1, "g",    cMeat));
            defs.add(d("Ngao nghêu",                   35,  5.8,  1.5,  0.5, "g",    cMeat));
            // ── Rau củ & Nấm
            defs.add(d("Cải xanh cải thìa",            13,  1.5,  2.2,  0.2, "g",    cVeg));
            defs.add(d("Rau muống",                    19,  2.6,  2.4,  0.4, "g",    cVeg));
            defs.add(d("Rau cải ngọt",                 20,  1.8,  3.5,  0.3, "g",    cVeg));
            defs.add(d("Bông cải xanh broccoli",       34,  2.8,  6.6,  0.4, "g",    cVeg));
            defs.add(d("Bông cải trắng",               25,  2.0,  5.0,  0.3, "g",    cVeg));
            defs.add(d("Rau bina spinach",             23,  2.9,  3.6,  0.4, "g",    cVeg));
            defs.add(d("Cải thảo",                     13,  1.2,  2.2,  0.2, "g",    cVeg));
            defs.add(d("Cà chua bi",                   18,  0.9,  3.9,  0.2, "g",    cVeg));
            defs.add(d("Cà chua lớn",                  18,  0.9,  3.9,  0.2, "g",    cVeg));
            defs.add(d("Ớt chuông đỏ",                 31,  1.0,  6.0,  0.3, "g",    cVeg));
            defs.add(d("Ớt chuông vàng",               27,  1.0,  6.3,  0.2, "g",    cVeg));
            defs.add(d("Dưa leo",                      16,  0.7,  3.6,  0.1, "g",    cVeg));
            defs.add(d("Cà rốt",                       41,  0.9,  9.6,  0.2, "g",    cVeg));
            defs.add(d("Củ cải trắng",                 18,  0.6,  4.1,  0.1, "g",    cVeg));
            defs.add(d("Khoai tây",                    77,  2.0, 17.0,  0.1, "g",    cVeg));
            defs.add(d("Bí đao",                       13,  0.6,  3.0,  0.1, "g",    cVeg));
            defs.add(d("Bí đỏ bí ngô",                 26,  1.0,  6.5,  0.1, "g",    cVeg));
            defs.add(d("Hành tây",                     40,  1.1,  9.3,  0.1, "g",    cVeg));
            defs.add(d("Hành lá",                      32,  1.8,  7.3,  0.2, "g",    cVeg));
            defs.add(d("Tỏi tươi",                    149,  6.4, 33.0,  0.5, "g",    cVeg));
            defs.add(d("Gừng tươi",                    80,  1.8, 18.0,  0.8, "g",    cVeg));
            defs.add(d("Sả",                           99,  1.8, 25.3,  0.5, "g",    cVeg));
            defs.add(d("Nấm hương shiitake",           34,  2.2,  6.8,  0.5, "g",    cVeg));
            defs.add(d("Nấm kim châm",                 37,  2.7,  7.6,  0.4, "g",    cVeg));
            defs.add(d("Nấm bào ngư",                  33,  3.3,  6.1,  0.4, "g",    cVeg));
            defs.add(d("Đậu hũ tofu cứng",             70,  8.1,  1.9,  3.7, "g",    cVeg));
            defs.add(d("Đậu hũ non",                   55,  5.9,  2.0,  2.5, "g",    cVeg));
            // ── Tinh bột
            defs.add(d("Gạo trắng nấu chín",          130,  2.7, 28.0,  0.3, "g",    cCarb));
            defs.add(d("Gạo lứt nấu chín",            112,  2.3, 24.0,  0.8, "g",    cCarb));
            defs.add(d("Bún tươi luộc",               109,  2.5, 24.0,  0.3, "g",    cCarb));
            defs.add(d("Bánh phở tươi",               108,  2.2, 24.0,  0.3, "g",    cCarb));
            defs.add(d("Mì gạo hủ tiếu khô",          348,  6.0, 79.0,  0.3, "g",    cCarb));
            defs.add(d("Bánh mì nguyên cám",           247,  8.5, 41.0,  4.2, "g",    cCarb));
            defs.add(d("Bánh mì trắng",               265,  9.0, 49.0,  3.2, "g",    cCarb));
            defs.add(d("Yến mạch cán dẹp rolled oats", 389, 17.0, 66.0,  7.0, "g",    cCarb));
            defs.add(d("Khoai lang đỏ luộc",           86,  1.6, 20.0,  0.1, "g",    cCarb));
            defs.add(d("Khoai lang tím luộc",          89,  1.6, 21.0,  0.1, "g",    cCarb));
            defs.add(d("Quinoa nấu chín",             120,  4.4, 21.3,  1.9, "g",    cCarb));
            defs.add(d("Hạt chia",                    486, 17.0, 42.0, 31.0, "g",    cCarb));
            // ── Sữa & Trứng
            defs.add(d("Trứng gà nguyên quả",         143, 12.6,  0.7,  9.5, "quả",  cDairy));
            defs.add(d("Lòng trắng trứng",             52, 10.9,  0.7,  0.2, "quả",  cDairy));
            defs.add(d("Sữa tươi không đường",          61,  3.2,  4.8,  3.2, "ml",   cDairy));
            defs.add(d("Sữa chua Hy Lạp 0%",           59, 10.0,  4.0,  0.4, "g",    cDairy));
            defs.add(d("Phô mai cottage low-fat",       72, 11.1,  3.4,  1.0, "g",    cDairy));
            defs.add(d("Phô mai cheddar",             402, 25.0,  1.3, 33.0, "g",    cDairy));
            // ── Gia vị & Dầu
            defs.add(d("Dầu ô liu extra virgin",      884,  0.0,  0.0,100.0, "ml",   cSpice));
            defs.add(d("Dầu mè vừng",                 884,  0.0,  0.0,100.0, "ml",   cSpice));
            defs.add(d("Dầu dừa",                     884,  0.0,  0.0,100.0, "ml",   cSpice));
            defs.add(d("Nước mắm",                     35,  5.4,  3.5,  0.0, "ml",   cSpice));
            defs.add(d("Nước tương soy sauce",         60,  5.8,  5.6,  0.0, "ml",   cSpice));
            defs.add(d("Giấm gạo",                     18,  0.0,  0.0,  0.0, "ml",   cSpice));
            defs.add(d("Mật ong",                     304,  0.3, 82.4,  0.0, "ml",   cSpice));
            defs.add(d("Muối ăn",                       0,  0.0,  0.0,  0.0, "g",    cSpice));
            defs.add(d("Tiêu đen xay",                251, 10.4, 64.8,  3.3, "g",    cSpice));
            defs.add(d("Bơ butter",                   717,  0.9,  0.1, 81.1, "g",    cSpice));
            // ── Đậu & Hạt
            defs.add(d("Đậu xanh luộc",              105,  7.0, 19.0,  0.4, "g",    cBean));
            defs.add(d("Đậu đen luộc",               132,  8.9, 24.0,  0.5, "g",    cBean));
            defs.add(d("Đậu gà chickpeas luộc",       164,  8.9, 27.0,  2.6, "g",    cBean));
            defs.add(d("Hạt điều rang muối",          553, 18.2, 30.2, 43.8, "g",    cBean));
            defs.add(d("Hạnh nhân almonds",           579, 21.2, 21.6, 49.9, "g",    cBean));
            defs.add(d("Óc chó walnuts",              654, 15.2, 13.7, 65.2, "g",    cBean));
            defs.add(d("Bơ đậu phộng natural",        590, 25.0, 20.0, 50.0, "g",    cBean));
            // ── Trái cây
            defs.add(d("Chuối già hương",              89,  1.1, 23.0,  0.3, "quả",  cFruit));
            defs.add(d("Bơ avocado",                  160,  2.0,  9.0, 15.0, "quả",  cFruit));
            defs.add(d("Táo xanh Granny Smith",        52,  0.3, 14.0,  0.2, "quả",  cFruit));
            defs.add(d("Dưa hấu",                      30,  0.6,  7.6,  0.2, "g",    cFruit));
            defs.add(d("Xoài chín",                    60,  0.8, 15.0,  0.4, "g",    cFruit));
            defs.add(d("Cam",                          47,  0.9, 11.8,  0.1, "quả",  cFruit));
            defs.add(d("Dâu tây",                      33,  0.7,  7.7,  0.3, "g",    cFruit));
            defs.add(d("Việt quất blueberry",          57,  0.7, 14.5,  0.3, "g",    cFruit));

            int created = 0;
            for (Object[] def : defs) {
                String name = (String) def[0];
                if (!existingNames.containsKey(name)) {
                    Ingredient ing = buildIng(def, now);
                    Ingredient saved = ingRepo.save(ing);
                    existingNames.put(name, saved.getIngredientId());
                    created++;
                }
            }
            System.out.println("  ✅  " + created + " new ingredients created. (total in DB: " + ingRepo.count() + ")");
        };
    }

    // =====================================================================
    // BEAN 3 — Eat-clean recipes (find-or-create by name)
    // =====================================================================
    @Bean(name = "seedRecipes")
    public CommandLineRunner seedRecipes(RecipeRepository recipeRepo,
                                          IngredientRepository ingRepo,
                                          RecipeIngredientRepository riRepo,
                                          RecipeStepRepository stepRepo,
                                          UserRepository userRepo) {
        return args -> {
            System.out.println("\n══════════ [Seed] Recipes ══════════");

            User author = userRepo.findByEmail("nu@dev.com").orElse(null);
            Integer authorId = (author != null) ? author.getUserId() : null;

            // Reload fresh ingredient map (exact name → id) AFTER seedIngredients ran
            Map<String, Integer> imap = new HashMap<>();
            for (Ingredient i : ingRepo.findAll()) {
                if (i.getName() != null) imap.put(i.getName(), i.getIngredientId());
            }

            LocalDateTime now = LocalDateTime.now();

            // Recipes to seed (find-or-create by name)
            Set<String> existingRecipeNames = new HashSet<>();
            for (Recipe r : recipeRepo.findAll()) {
                if (r.getName() != null) existingRecipeNames.add(r.getName());
            }
            int created = 0;

            // ── 1. Cơm gạo lứt ức gà áp chảo ─────────────────────────
            created += upsertRecipe(recipeRepo, riRepo, stepRepo, existingRecipeNames, authorId, now,
                "Cơm gạo lứt ức gà áp chảo",
                "Bữa ăn eat-clean tiêu chuẩn: cơm gạo lứt kết hợp ức gà áp chảo với rau xanh. Giàu protein, ít chất béo, phù hợp cả bulking lẫn cutting.",
                DifficultyLevel.EASY, 10, 20, 2, new BigDecimal("480"),
                List.of(
                    ri(imap, "Thịt gà ức không da", 300, "g", "Đập dẹp, ướp muối tiêu"),
                    ri(imap, "Gạo lứt nấu chín",    150, "g", "Vo sạch nấu cơm"),
                    ri(imap, "Bông cải xanh broccoli", 150, "g", "Cắt nhỏ, hấp 5 phút"),
                    ri(imap, "Cà chua bi",           100, "g", null),
                    ri(imap, "Tỏi tươi",              10, "g", "Băm nhuyễn"),
                    ri(imap, "Dầu ô liu extra virgin", 10, "ml", null),
                    ri(imap, "Muối ăn",                2, "g", null),
                    ri(imap, "Tiêu đen xay",           1, "g", null)
                ),
                List.of(
                    "Vo gạo lứt, nấu cơm với tỉ lệ gạo:nước = 1:1.5.",
                    "Ức gà rửa sạch, dùng búa đập mỏng đều. Ướp muối, tiêu, tỏi băm — để 10 phút.",
                    "Bông cải rửa sạch, hấp 5 phút giữ màu xanh và độ giòn.",
                    "Đun chảo chống dính với dầu ô liu lửa vừa-cao. Áp chảo gà mỗi mặt 5–6 phút đến vàng đẹp.",
                    "Để gà nghỉ 3 phút trước khi cắt lát. Dọn cơm, xếp gà, bông cải và cà chua bi. Dùng ngay."
                )
            );

            // ── 2. Salad cá ngừ Địa Trung Hải ─────────────────────────
            created += upsertRecipe(recipeRepo, riRepo, stepRepo, existingRecipeNames, authorId, now,
                "Salad cá ngừ kiểu Địa Trung Hải",
                "Salad eat-clean tươi mát, giàu omega-3, nhiều chất xơ. Không cần nấu, chỉ 10 phút.",
                DifficultyLevel.EASY, 10, 0, 1, new BigDecimal("310"),
                List.of(
                    ri(imap, "Cá ngừ tươi",          150, "g", "Đóng gói nước muối, vớt ráo"),
                    ri(imap, "Rau bina spinach",       80, "g", null),
                    ri(imap, "Cà chua bi",             80, "g", "Cắt đôi"),
                    ri(imap, "Dưa leo",                80, "g", "Thái lát mỏng"),
                    ri(imap, "Ớt chuông đỏ",           50, "g", "Thái sợi"),
                    ri(imap, "Hành tây",               30, "g", "Thái mỏng, ngâm dấm 5 phút"),
                    ri(imap, "Dầu ô liu extra virgin", 15, "ml", null),
                    ri(imap, "Giấm gạo",               10, "ml", null),
                    ri(imap, "Muối ăn",                 2, "g", null),
                    ri(imap, "Tiêu đen xay",            1, "g", null)
                ),
                List.of(
                    "Rửa sạch tất cả rau, để ráo nước.",
                    "Hành tây thái mỏng, ngâm trong giấm gạo + ít muối 5 phút để giảm vị hăng.",
                    "Trộn dầu ô liu + giấm gạo + muối + tiêu thành vinaigrette.",
                    "Xếp rau bina ra đĩa, thêm cà chua, dưa leo, ớt chuông, hành, cá ngừ lên trên.",
                    "Rưới vinaigrette đều lên, dùng ngay."
                )
            );

            // ── 3. Gỏi cuốn tôm bơ ─────────────────────────────────────
            created += upsertRecipe(recipeRepo, riRepo, stepRepo, existingRecipeNames, authorId, now,
                "Gỏi cuốn tôm bơ eat-clean",
                "Gỏi cuốn tươi với tôm, bơ avocado và rau sống. Không chiên rán, không sốt nhiều đường — lành mạnh hoàn toàn.",
                DifficultyLevel.EASY, 15, 10, 2, new BigDecimal("360"),
                List.of(
                    ri(imap, "Tôm sú bóc vỏ",        200, "g", "Luộc chín"),
                    ri(imap, "Bơ avocado",            100, "g", "Thái lát"),
                    ri(imap, "Rau bina spinach",       60, "g", null),
                    ri(imap, "Cà rốt",                 60, "g", "Bào sợi"),
                    ri(imap, "Dưa leo",                60, "g", "Thái sợi"),
                    ri(imap, "Bún tươi luộc",         100, "g", "Bún sợi nhỏ"),
                    ri(imap, "Nước mắm",               15, "ml", null),
                    ri(imap, "Giấm gạo",               10, "ml", null),
                    ri(imap, "Tỏi tươi",                5, "g", "Băm nhuyễn")
                ),
                List.of(
                    "Luộc tôm với nước sôi + muối + gừng khoảng 3–4 phút. Vớt ra ngay, ngâm nước lạnh, bóc vỏ.",
                    "Bào sợi cà rốt, thái sợi dưa leo, thái lát bơ.",
                    "Pha nước chấm: nước mắm + giấm gạo + tỏi băm + 10ml nước ấm, khuấy đều.",
                    "Trụng bánh tráng qua nước ấm, đặt lên mặt ẩm. Xếp rau bina, bún, cà rốt, dưa leo, bơ, tôm rồi cuộn chặt tay.",
                    "Dùng kèm nước chấm, ăn ngay khi bánh còn mềm."
                )
            );

            // ── 4. Cháo yến mạch thịt gà ──────────────────────────────
            created += upsertRecipe(recipeRepo, riRepo, stepRepo, existingRecipeNames, authorId, now,
                "Cháo yến mạch thịt gà rau củ",
                "Cháo ăn sáng lành mạnh — yến mạch cán dẹp nấu mềm với gà xé sợi và rau củ, no lâu, đủ protein.",
                DifficultyLevel.EASY, 5, 20, 1, new BigDecimal("380"),
                List.of(
                    ri(imap, "Yến mạch cán dẹp rolled oats", 60, "g", "Yến mạch cán dẹp"),
                    ri(imap, "Thịt gà ức không da",          120, "g", "Luộc chín, xé sợi"),
                    ri(imap, "Cà rốt",                        60, "g", "Thái hạt lựu nhỏ"),
                    ri(imap, "Nấm hương shiitake",             50, "g", "Thái lát"),
                    ri(imap, "Hành lá",                        10, "g", "Thái nhỏ để trang trí"),
                    ri(imap, "Gừng tươi",                       5, "g", "Thái lát mỏng"),
                    ri(imap, "Nước mắm",                       10, "ml", null),
                    ri(imap, "Muối ăn",                         2, "g", null),
                    ri(imap, "Tiêu đen xay",                    1, "g", null)
                ),
                List.of(
                    "Luộc ức gà với gừng + muối 15 phút đến chín hẳn. Lấy ra xé sợi, giữ lại nước luộc.",
                    "Đổ 500ml nước luộc gà vào nồi sôi. Cho cà rốt hầm 5 phút.",
                    "Thêm yến mạch và nấm hương, khuấy liên tục, nấu nhỏ lửa thêm 10 phút đến khi sánh mịn.",
                    "Nêm nước mắm, muối, tiêu vừa ăn. Cho gà xé vào, đảo đều.",
                    "Múc ra bát, rắc hành lá. Dùng nóng."
                )
            );

            // ── 5. Cá hồi nướng giấy bạc ──────────────────────────────
            created += upsertRecipe(recipeRepo, riRepo, stepRepo, existingRecipeNames, authorId, now,
                "Cá hồi nướng giấy bạc rau củ",
                "Cá hồi nướng giấy bạc cùng rau củ — giữ nguyên dưỡng chất, thơm ngon, không dầu mỡ thừa.",
                DifficultyLevel.EASY, 10, 20, 2, new BigDecimal("420"),
                List.of(
                    ri(imap, "Cá hồi Na Uy",              300, "g", "2 miếng phi lê ~150g/miếng"),
                    ri(imap, "Ớt chuông đỏ",               80, "g", "Thái dài"),
                    ri(imap, "Bông cải xanh broccoli",    100, "g", "Tách bông nhỏ"),
                    ri(imap, "Cà chua bi",                  80, "g", "Cắt đôi"),
                    ri(imap, "Tỏi tươi",                    10, "g", "Thái lát mỏng"),
                    ri(imap, "Dầu ô liu extra virgin",      15, "ml", null),
                    ri(imap, "Giấm gạo",                    10, "ml", null),
                    ri(imap, "Muối ăn",                      3, "g", null),
                    ri(imap, "Tiêu đen xay",                 2, "g", null),
                    ri(imap, "Hành lá",                     10, "g", "Trang trí")
                ),
                List.of(
                    "Làm nóng lò 200°C (nồi chiên không dầu 190°C).",
                    "Trộn ớt chuông, bông cải, cà chua với dầu ô liu + muối + tiêu.",
                    "Ướp cá hồi: dầu ô liu + giấm gạo + tỏi lát + muối + tiêu, để 5 phút.",
                    "Trải giấy bạc lên khay. Xếp rau củ, đặt cá hồi lên trên. Gấp kín giấy bạc.",
                    "Nướng 18–20 phút. Mở giấy bạc, bật broil 3 phút cho da giòn.",
                    "Rắc hành lá, dùng kèm cơm gạo lứt hoặc khoai lang."
                )
            );

            // ── 6. Bữa sáng trứng bơ nguyên cám ──────────────────────
            created += upsertRecipe(recipeRepo, riRepo, stepRepo, existingRecipeNames, authorId, now,
                "Bữa sáng trứng bơ bánh mì nguyên cám",
                "Combo bữa sáng eat-clean kinh điển: trứng ốp la + bơ avocado + bánh mì nguyên cám. Giàu chất béo tốt và protein.",
                DifficultyLevel.EASY, 5, 8, 1, new BigDecimal("390"),
                List.of(
                    ri(imap, "Trứng gà nguyên quả",    2, "quả", null),
                    ri(imap, "Bơ avocado",             80, "g", "Nghiền hoặc thái lát"),
                    ri(imap, "Bánh mì nguyên cám",     80, "g", "2 lát"),
                    ri(imap, "Dầu ô liu extra virgin",  8, "ml", null),
                    ri(imap, "Muối ăn",                 1, "g", null),
                    ri(imap, "Tiêu đen xay",            1, "g", null),
                    ri(imap, "Cà chua bi",             50, "g", "Trang trí"),
                    ri(imap, "Hành lá",                 5, "g", null)
                ),
                List.of(
                    "Nướng bánh mì nguyên cám đến vàng giòn.",
                    "Cắt bơ avocado làm đôi, bỏ hạt. Nghiền thịt bơ với muối + tiêu, hoặc thái lát tuỳ thích.",
                    "Đổ dầu ô liu vào chảo chống dính lửa vừa. Đập trứng ốp la, nắp vung 2–3 phút đến lòng trắng chín.",
                    "Phết bơ avocado lên bánh mì, xếp trứng ốp la lên.",
                    "Bày cà chua bi và hành lá, rắc muối mỏ biển + tiêu đen. Thưởng thức ngay."
                )
            );

            // ── 7. Bò xào rau củ kiểu Thái ───────────────────────────
            created += upsertRecipe(recipeRepo, riRepo, stepRepo, existingRecipeNames, authorId, now,
                "Bò xào rau củ kiểu Thái ít dầu",
                "Thịt bò nạc xào nhanh với rau củ đầy màu sắc theo phong cách Thái — đậm đà, giàu protein, ít chất béo bão hòa.",
                DifficultyLevel.MEDIUM, 10, 15, 2, new BigDecimal("450"),
                List.of(
                    ri(imap, "Thịt bò nạc thăn",       250, "g", "Thái mỏng ngang thớ"),
                    ri(imap, "Ớt chuông đỏ",            80, "g", "Thái dài"),
                    ri(imap, "Ớt chuông vàng",           80, "g", "Thái dài"),
                    ri(imap, "Bông cải xanh broccoli",  100, "g", "Tách bông nhỏ"),
                    ri(imap, "Nấm hương shiitake",       60, "g", "Thái lát"),
                    ri(imap, "Hành tây",                 60, "g", "Thái múi cau"),
                    ri(imap, "Tỏi tươi",                 10, "g", "Băm nhuyễn"),
                    ri(imap, "Gừng tươi",                 8, "g", "Thái sợi"),
                    ri(imap, "Nước tương soy sauce",     20, "ml", null),
                    ri(imap, "Dầu mè vừng",              10, "ml", null),
                    ri(imap, "Dầu ô liu extra virgin",   10, "ml", null),
                    ri(imap, "Muối ăn",                   2, "g", null)
                ),
                List.of(
                    "Thái bò mỏng ngang thớ, ướp nước tương + dầu mè + tiêu 10 phút.",
                    "Luộc sơ bông cải 3 phút, vớt ra ngâm nước lạnh ngay để giữ màu xanh.",
                    "Đun wok/chảo dầu ô liu lửa lớn nhất. Khi bốc khói nhẹ, xào bò nhanh 2–3 phút. Lấy ra.",
                    "Phi thơm tỏi + gừng 30 giây. Thêm hành tây, ớt chuông, nấm xào 3–4 phút.",
                    "Cho bò và bông cải vào, đảo đều. Nêm lại chút nước tương + muối. Tắt bếp.",
                    "Dùng ngay với cơm gạo lứt."
                )
            );

            // ── 8. Smoothie protein chuối bơ đậu phộng ───────────────
            created += upsertRecipe(recipeRepo, riRepo, stepRepo, existingRecipeNames, authorId, now,
                "Smoothie protein chuối bơ đậu phộng",
                "Sinh tố sau tập gym đủ protein, carbs nhanh và chất béo tốt. Blend xong uống ngay.",
                DifficultyLevel.EASY, 5, 0, 1, new BigDecimal("420"),
                List.of(
                    ri(imap, "Chuối già hương",              150, "g", "Cắt khúc, đông đá trước"),
                    ri(imap, "Sữa tươi không đường",         200, "ml", null),
                    ri(imap, "Sữa chua Hy Lạp 0%",          100, "g", "Không đường"),
                    ri(imap, "Bơ đậu phộng natural",          30, "g", "Natural, không đường"),
                    ri(imap, "Yến mạch cán dẹp rolled oats",  20, "g", null),
                    ri(imap, "Hạt chia",                      10, "g", null),
                    ri(imap, "Mật ong",                       10, "ml", "Tùy chọn")
                ),
                List.of(
                    "Bóc vỏ chuối, cắt khúc, bỏ túi zip đông đá ít nhất 2 tiếng để smoothie đặc sánh.",
                    "Cho tất cả nguyên liệu vào máy xay: chuối đông đá, sữa, sữa chua, bơ đậu phộng, yến mạch, hạt chia.",
                    "Xay tốc độ cao 60 giây đến mịn hoàn toàn.",
                    "Nếm thử, thêm mật ong nếu muốn ngọt hơn.",
                    "Đổ ra ly, thêm ít yến mạch và slice chuối trang trí. Uống ngay."
                )
            );

            // ── 9. Đậu hũ sốt gừng mật ong ──────────────────────────
            created += upsertRecipe(recipeRepo, riRepo, stepRepo, existingRecipeNames, authorId, now,
                "Đậu hũ áp chảo sốt gừng mật ong",
                "Món đậu hũ chay eat-clean, giàu protein thực vật, sốt gừng mật ong thơm ngon, ăn kèm cơm gạo lứt.",
                DifficultyLevel.EASY, 10, 15, 2, new BigDecimal("320"),
                List.of(
                    ri(imap, "Đậu hũ tofu cứng",          350, "g", "Cắt miếng vừa, thấm khô với khăn giấy"),
                    ri(imap, "Gừng tươi",                   15, "g", "Bào nhuyễn"),
                    ri(imap, "Tỏi tươi",                    10, "g", "Băm nhuyễn"),
                    ri(imap, "Mật ong",                     20, "ml", null),
                    ri(imap, "Nước tương soy sauce",        25, "ml", null),
                    ri(imap, "Giấm gạo",                    10, "ml", null),
                    ri(imap, "Dầu mè vừng",                  5, "ml", null),
                    ri(imap, "Dầu ô liu extra virgin",      15, "ml", null),
                    ri(imap, "Hành lá",                     10, "g", "Thái nhỏ trang trí"),
                    ri(imap, "Gạo lứt nấu chín",           150, "g", "Nấu sẵn")
                ),
                List.of(
                    "Đậu hũ cắt miếng dày 1–1.5cm, để lên khăn giấy thấm hết nước dư — bước này quan trọng để đậu không bị bắn dầu và giòn hơn.",
                    "Trộn sốt: nước tương + mật ong + giấm gạo + dầu mè. Khuấy đều.",
                    "Đun chảo chống dính với dầu ô liu lửa vừa-cao. Áp chảo đậu hũ mỗi mặt 4–5 phút đến vàng giòn.",
                    "Phi thơm tỏi + gừng bào 30 giây. Đổ hỗn hợp sốt vào, đun sôi nhẹ 1 phút.",
                    "Cho đậu hũ vào, đảo nhẹ để ngấm sốt đều. Tắt bếp.",
                    "Dọn cơm gạo lứt ra đĩa, xếp đậu hũ lên, rắc hành lá. Dùng ngay."
                )
            );

            System.out.println("  ✅  " + created + " new recipes created. (total: " + recipeRepo.count() + ")");
        };
    }

    // =====================================================================
    // BEAN 4 — Tủ lạnh cho nguyentrungthuan417@gmail.com
    // =====================================================================
    @Bean(name = "seedPantry")
    public CommandLineRunner seedPantry(UserRepository userRepo,
                                         IngredientRepository ingRepo,
                                         UserPantryRepository pantryRepo) {
        return args -> {
            System.out.println("\n══════════ [Seed] Pantry ══════════");

            User thuanUser = userRepo.findByEmail("nguyentrungthuan417@gmail.com").orElse(null);
            if (thuanUser == null) {
                System.out.println("  ⚠️  User nguyentrungthuan417@gmail.com not found — skip.");
                return;
            }
            if (!pantryRepo.findByUserIdOrderByExpiryDateAsc(thuanUser.getUserId()).isEmpty()) {
                System.out.println("  ⏭️  Pantry already seeded — skip.");
                return;
            }

            Map<String, Integer> imap = new HashMap<>();
            for (Ingredient i : ingRepo.findAll()) {
                if (i.getName() != null) imap.put(i.getName(), i.getIngredientId());
            }

            LocalDateTime now = LocalDateTime.now();
            LocalDate d7  = LocalDate.now().plusDays(7);
            LocalDate d10 = LocalDate.now().plusDays(10);
            LocalDate d14 = LocalDate.now().plusDays(14);
            LocalDate d30 = LocalDate.now().plusDays(30);

            List<UserPantry> pantry = new ArrayList<>();
            Integer uid = thuanUser.getUserId();

            // Thực phẩm tươi sống
            addPantry(pantry, uid, imap, "Thịt gà ức không da",   400, "g",    d10, now);
            addPantry(pantry, uid, imap, "Cá tra phi lê",          300, "g",    d10, now);
            addPantry(pantry, uid, imap, "Tôm thẻ chân trắng",    200, "g",    d10, now);
            addPantry(pantry, uid, imap, "Trứng gà nguyên quả",     6, "quả",  d10, now);
            addPantry(pantry, uid, imap, "Bơ avocado",              2, "quả",  LocalDate.now().plusDays(4), now);
            addPantry(pantry, uid, imap, "Chuối già hương",          4, "quả",  LocalDate.now().plusDays(5), now);
            addPantry(pantry, uid, imap, "Sữa chua Hy Lạp 0%",   250, "g",    d7,  now);
            // Rau
            addPantry(pantry, uid, imap, "Bông cải xanh broccoli", 300, "g",  LocalDate.now().plusDays(5), now);
            addPantry(pantry, uid, imap, "Rau bina spinach",       100, "g",   LocalDate.now().plusDays(4), now);
            addPantry(pantry, uid, imap, "Cà rốt",                 200, "g",   d14, now);
            addPantry(pantry, uid, imap, "Cà chua bi",             150, "g",   d7,  now);
            addPantry(pantry, uid, imap, "Dưa leo",                200, "g",   LocalDate.now().plusDays(6), now);
            addPantry(pantry, uid, imap, "Ớt chuông đỏ",           150, "g",   d7,  now);
            addPantry(pantry, uid, imap, "Tỏi tươi",                50, "g",   d30, now);
            addPantry(pantry, uid, imap, "Hành tây",               200, "g",   d14, now);
            addPantry(pantry, uid, imap, "Đậu hũ tofu cứng",       300, "g",   d7,  now);
            // Khô / lâu hỏng
            addPantry(pantry, uid, imap, "Gạo lứt nấu chín",       500, "g",   d30, now);
            addPantry(pantry, uid, imap, "Yến mạch cán dẹp rolled oats", 300, "g", d30, now);
            addPantry(pantry, uid, imap, "Hạt chia",               100, "g",   d30, now);
            addPantry(pantry, uid, imap, "Hạnh nhân almonds",      100, "g",   d30, now);
            addPantry(pantry, uid, imap, "Bơ đậu phộng natural",   200, "g",   d30, now);
            // Gia vị
            addPantry(pantry, uid, imap, "Dầu ô liu extra virgin",  200, "ml", d30, now);
            addPantry(pantry, uid, imap, "Nước mắm",               150, "ml",  d30, now);
            addPantry(pantry, uid, imap, "Nước tương soy sauce",   100, "ml",  d30, now);
            addPantry(pantry, uid, imap, "Mật ong",                 80, "ml",  d30, now);
            addPantry(pantry, uid, imap, "Sữa tươi không đường",   500, "ml",  d7,  now);

            pantry.removeIf(p -> p.getIngredientId() == null);
            pantryRepo.saveAll(pantry);
            System.out.println("  ✅  " + pantry.size() + " pantry items added for " + thuanUser.getEmail());
            System.out.println("══════════════════════════════════════════\n");
        };
    }

    // =====================================================================
    // ════ PRIVATE HELPERS ════════════════════════════════════════════════
    // =====================================================================

    private void createUser(UserRepository repo, PasswordEncoder enc,
                            String email, String name, UserRole role) {
        if (!repo.existsByEmail(email)) {
            User u = new User();
            u.setEmail(email);
            u.setPasswordHash(enc.encode(DEFAULT_PASSWORD));
            u.setFullName(name);
            u.setRole(role);
            u.setStatus(UserStatus.ACTIVE);
            u.setIsEmailVerified(true);
            u.setCreatedAt(LocalDateTime.now());
            u.setUpdatedAt(LocalDateTime.now());
            u.setIsDeleted(false);
            repo.save(u);
            System.out.printf("  ✅  %-42s [%s]%n", email, role);
        } else {
            System.out.printf("  ⏭️  %-42s already exists%n", email);
        }
    }

    /** Tạo mảng định nghĩa nguyên liệu (dùng để build Ingredient) */
    private Object[] d(String name, double cal, double protein, double carbs, double fat,
                       String unit, Integer catId) {
        return new Object[]{name, cal, protein, carbs, fat, unit, catId};
    }

    private Ingredient buildIng(Object[] def, LocalDateTime now) {
        Ingredient i = new Ingredient();
        i.setName((String) def[0]);
        i.setCaloriesPer100g(BigDecimal.valueOf((double) def[1]));
        i.setProtein(BigDecimal.valueOf((double) def[2]));
        i.setCarbs(BigDecimal.valueOf((double) def[3]));
        i.setFat(BigDecimal.valueOf((double) def[4]));
        i.setBaseUnit((String) def[5]);
        i.setCategoryId((Integer) def[6]);
        i.setIsActive(true);
        i.setIsDeleted(false);
        i.setCreatedAt(now);
        i.setUpdatedAt(now);
        return i;
    }

    /**
     * Tạo RecipeIngredient bằng exact name lookup từ imap.
     * Nếu không tìm thấy thì log cảnh báo và bỏ qua (không save null FK).
     */
    private RecipeIngredient ri(Map<String, Integer> imap, String name,
                                double qty, String unit, String note) {
        Integer ingId = imap.get(name);
        if (ingId == null) {
            System.out.println("  ⚠️  Ingredient not found: [" + name + "] — skipped");
            return null; // caller phải filter null
        }
        RecipeIngredient ri = new RecipeIngredient();
        ri.setIngredientId(ingId);
        ri.setQuantity(BigDecimal.valueOf(qty));
        ri.setUnit(unit);
        ri.setNote(note);
        ri.setIsDeleted(false);
        ri.setCreatedAt(LocalDateTime.now());
        ri.setUpdatedAt(LocalDateTime.now());
        return ri;
    }

    private void addPantry(List<UserPantry> list, Integer userId,
                           Map<String, Integer> imap, String name,
                           double qty, String unit, LocalDate expiry, LocalDateTime now) {
        Integer ingId = imap.get(name);
        if (ingId == null) {
            System.out.println("  ⚠️  Pantry ingredient not found: [" + name + "] — skipped");
            return;
        }
        UserPantry p = new UserPantry();
        p.setUserId(userId);
        p.setIngredientId(ingId);
        p.setQuantity(BigDecimal.valueOf(qty));
        p.setUnit(unit);
        p.setExpiryDate(expiry);
        p.setCreatedAt(now);
        list.add(p);
    }

    /**
     * Tạo recipe + ingredients + steps nếu chưa tồn tại (check by name).
     * @return 1 nếu tạo mới, 0 nếu đã tồn tại
     */
    private int upsertRecipe(RecipeRepository recipeRepo,
                             RecipeIngredientRepository riRepo,
                             RecipeStepRepository stepRepo,
                             Set<String> existingNames,
                             Integer authorId, LocalDateTime now,
                             String name, String desc,
                             DifficultyLevel diff, int prep, int cook,
                             int servings, BigDecimal cals,
                             List<RecipeIngredient> riList,
                             List<String> steps) {
        if (existingNames.contains(name)) {
            System.out.println("  ⏭️  Recipe exists: " + name);
            return 0;
        }
        Recipe r = new Recipe();
        r.setName(name);
        r.setDescription(desc);
        r.setDifficulty(diff);
        r.setPrepTimeMin(prep);
        r.setCookTimeMin(cook);
        r.setServings(servings);
        r.setTotalCalories(cals);
        r.setStatus(RecipeStatus.ACTIVE);
        r.setCreatedByUserId(authorId);
        r.setNutritionist(true);
        r.setIsDeleted(false);
        r.setCreatedAt(now);
        r.setUpdatedAt(now);
        Recipe saved = recipeRepo.save(r);

        // Lưu ingredients (bỏ qua các entry null = không tìm thấy nguyên liệu)
        for (RecipeIngredient ri : riList) {
            if (ri != null) {
                ri.setRecipeId(saved.getRecipeId());
                riRepo.save(ri);
            }
        }

        // Lưu steps
        for (int i = 0; i < steps.size(); i++) {
            RecipeStep step = new RecipeStep();
            step.setRecipeId(saved.getRecipeId());
            step.setStepNumber(i + 1);
            step.setInstruction(steps.get(i));
            step.setCreatedAt(now);
            stepRepo.save(step);
        }

        System.out.println("    🍽️  " + name);
        existingNames.add(name);
        return 1;
    }
}
