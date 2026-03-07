package com.david.NUTRITION_TRACNKER.service;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.david.NUTRITION_TRACNKER.dto.AiFoodResponse;
import com.david.NUTRITION_TRACNKER.dto.PantryRecipeMatchDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.io.IOException;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.springframework.web.client.RestClientException;

import com.david.NUTRITION_TRACNKER.dto.WeeklyPlanDTO;
import com.david.NUTRITION_TRACNKER.entity.AiRequestLog;
import com.david.NUTRITION_TRACNKER.entity.AiSavedRecipes;
import com.david.NUTRITION_TRACNKER.entity.CollectionItem;
import com.david.NUTRITION_TRACNKER.entity.Recipe;
import com.david.NUTRITION_TRACNKER.entity.RecipeCollection;
import com.david.NUTRITION_TRACNKER.repository.AiRequestLogRepository;
import com.david.NUTRITION_TRACNKER.repository.AiSavedRecipeRepository;
import com.david.NUTRITION_TRACNKER.repository.CollectionItemRepository;
import com.david.NUTRITION_TRACNKER.repository.RecipeCollectionRepository;
import com.david.NUTRITION_TRACNKER.repository.RecipeRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AiFoodService {

    @Autowired
    private RecipeRepository recipeRepository;

    @Value("${ollama.host.url}") // Lấy từ application.properties
    private String ollamaUrl;

    @Value("${ollama.model.vision:llava}") 
    private String ollamaVisionModel;

    @Value("${ollama.model.text:llama3}") 
    private String ollamaTextModel;

    @Autowired
    private AiSavedRecipeRepository aiRecipeRepo;
    @Autowired
    private RecipeCollectionRepository collectionRepo;
    @Autowired
    private CollectionItemRepository collectionItemRepo;
    @Autowired
    private FileStorageService fileStorageService;
    @Autowired
    private AiRequestLogRepository logRepository;
    @Autowired
    private AsyncJobService asyncJobService;
    
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper()
            .configure(JsonParser.Feature.ALLOW_COMMENTS, true)
            .configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true)
            .configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    private RestTemplate getRestTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(60000);
        factory.setReadTimeout(600000); // 10 phút
        return new RestTemplate(factory);
    }
    
    private Integer getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof CustomUserDetails) {
            return ((CustomUserDetails) auth.getPrincipal()).getUserId();
        }
        return null; // Khách vãng lai hoặc system call
    }
    
    private void saveAiLog(String type, String prompt, String responseJson) {
        try {
            int tokens = 0;
            String actualResponseContent = "";

            // Cố gắng trích xuất số token và nội dung từ JSON raw của Ollama
            if (responseJson != null) {
                JsonNode root = objectMapper.readTree(responseJson);
                if (root.has("eval_count")) {
                    tokens = root.get("eval_count").asInt();
                }
                if (root.has("response")) {
                    actualResponseContent = root.get("response").asText();
                } else {
                    actualResponseContent = responseJson; // Fallback nếu không đúng chuẩn
                }
            }

            AiRequestLog log = new AiRequestLog();
            log.setUserId(getCurrentUserId());
            log.setRequestType(type);
            log.setPromptContent(prompt.length() > 2000 ? prompt.substring(0, 2000) + "..." : prompt);
            
            // Safety Truncation: Cắt bớt nếu Response quá dài để tránh lỗi Data Truncation
            // LONGTEXT MySQL hỗ trợ 4GB, nhưng Hibernate/JDBC có thể giới hạn ở mức 65k nếu không cấu hình đúng.
            if (actualResponseContent.length() > 60000) {
                actualResponseContent = actualResponseContent.substring(0, 60000) + "... [TRUNCATED]";
            }
            log.setAiResponse(actualResponseContent); 
            log.setTokensUsed(tokens);
            log.setCreatedAt(LocalDateTime.now());

            logRepository.save(log);
        } catch (Exception e) {
            System.err.println("Failed to save AI Log: " + e.getMessage());
        }
    }

    public AiFoodResponse analyzeFood(String textDescription, MultipartFile imageFile) {
        try {
            // 1. Chuyển ảnh sang Base64
            String base64Image = null;
            if (imageFile != null && !imageFile.isEmpty()) {
                String contentType = imageFile.getContentType();
                if (contentType == null
                        || (!contentType.equals("image/jpeg") && !contentType.equals("image/png") && !contentType.equals("image/jpg"))) {
                    return createErrorResponse("Invalid file type. Only PNG and JPG are allowed.");
                }

                base64Image = Base64.getEncoder().encodeToString(imageFile.getBytes());
            }

            // 2. Tối ưu Prompt: Yêu cầu AI trả về toàn bộ tiếng Việt
            String promptText = "Bạn là chuyên gia dinh dưỡng người Việt Nam. Hãy phân tích ảnh món ăn này cẩn thận. "
                    + "QUAN TRỌNG: Trả lời TOÀN BỘ bằng tiếng Việt. Mọi trường văn bản phải là tiếng Việt. "
                    + "Tạo một đối tượng JSON chứa thông tin dinh dưỡng theo cấu trúc sau: "
                    + "{ \"dishName\": \"tên món ăn bằng tiếng Việt\", \"calories\": số nguyên, \"protein\": \"ví dụ: 18g\", "
                    + "\"carbs\": \"ví dụ: 40g\", \"fat\": \"ví dụ: 12g\", \"time\": \"ví dụ: 30 phút\", "
                    + "\"ingredients\": [\"nguyên liệu 1 bằng tiếng Việt\"], "
                    + "\"instructions\": [\"bước 1 bằng tiếng Việt\"] }. "
                    + "Không dùng markdown như ```json. Chỉ trả về raw JSON thuần túy.";

            if (textDescription != null && !textDescription.isEmpty()) {
                promptText = "Gợi ý từ người dùng: " + textDescription + ". " + promptText;
            }

            // 3. Tạo Payload đúng chuẩn Ollama
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", ollamaVisionModel);
            requestBody.put("prompt", promptText);
            requestBody.put("stream", false);
            if (base64Image != null) {
                requestBody.put("images", Collections.singletonList(base64Image));
            }

            // 4. Gửi Request
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.add("User-Agent", "ZaderFood-Backend-v1");
            headers.add("ngrok-skip-browser-warning", "true");
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            System.out.println("--- SENDING PROMPT TO AI ---");
            // Nhận về String thô để xử lý an toàn
            String rawResponse = restTemplate.postForObject(ollamaUrl, entity, String.class);
            System.err.println(rawResponse);

            saveAiLog("ANALYZE_FOOD", promptText, rawResponse);
            
            // 5. Xử lý kết quả
            return parseOllamaResponse(rawResponse);

        } catch (IOException e) {
            return createErrorResponse("Error processing file: " + e.getMessage());
        } catch (RestClientException e) {
            return createErrorResponse("AI Service Error: " + e.getMessage());
        }
    }

    private AiFoodResponse parseOllamaResponse(String jsonResponse) {
        try {
            // Ollama trả về JSON dạng: { "model":..., "response": "Nội dung AI trả lời..." }
            JsonNode root = objectMapper.readTree(jsonResponse);

            if (root.has("response")) {
                String aiText = root.get("response").asText();

                // Làm sạch: Xóa markdown ```json và ``` nếu AI lỡ thêm vào
                String cleanJson = aiText
                        .replaceAll("(?i)```json", "") // Xóa ```json (không phân biệt hoa thường)
                        .replaceAll("```", "") // Xóa ```
                        .trim();

                // Tìm điểm bắt đầu '{' và kết thúc '}' để lấy đúng phần JSON
                int jsonStart = cleanJson.indexOf("{");
                int jsonEnd = cleanJson.lastIndexOf("}");
                if (jsonStart != -1 && jsonEnd != -1) {
                    cleanJson = cleanJson.substring(jsonStart, jsonEnd + 1);
                    return objectMapper.readValue(cleanJson, AiFoodResponse.class);
                } else {
                    // Trường hợp AI nói lung tung không ra JSON
                    return createErrorResponse("AI identified: " + aiText);
                }
            }
        } catch (JsonProcessingException e) {
        }
        return createErrorResponse("Failed to parse AI response.");
    }

    private AiFoodResponse createErrorResponse(String errorMsg) {
        AiFoodResponse response = new AiFoodResponse();
        response.setError(errorMsg);
        return response;
    }

    public WeeklyPlanDTO generateWeeklyPlan(int calories, String dietType, String goal) {
        return generateWeeklyPlan(calories, dietType, goal, null);
    }

    public WeeklyPlanDTO generateWeeklyPlan(int calories, String dietType, String goal, String foodConstraint) {
        try {
            // 1. Lấy dữ liệu: Giới hạn 40 món
            List<Recipe> candidates = recipeRepository.findRandomNutritionistRecipes();
            if (candidates.size() > 40) {
                candidates = candidates.subList(0, 40);
            }

            if (candidates.isEmpty()) {
                return null;
            }

            // 2. Format dữ liệu
            String recipeContext = candidates.stream()
                    .map(r -> String.format("{ID:%d, Name:'%s', Cal:%s}",
                    r.getRecipeId(),
                    r.getName().replace("'", ""),
                    r.getTotalCalories()))
                    .collect(Collectors.joining(", "));

            // Thêm ràng buộc thực ăn nếu có
            String constraintText = (foodConstraint != null && !foodConstraint.isBlank())
                    ? "\nYêu cầu ĐẶC BIỆT của người dùng (bắt buộc tuân thủ): " + foodConstraint + "\n"
                    : "";

            // 3. PROMPT
            String promptText = String.format(
                    "Vai trò: Chuyên gia dinh dưỡng Việt Nam.\n"
                    + "Ngữ cảnh: Tôi có các công thức sau: [%s].\n"
                    + "%s"
                    + "Nhiệm vụ: Lập thực đơn 7 ngày (Thứ 2 đến Chủ Nhật) CHỈ sử dụng các công thức được cung cấp.\n"
                    + "Ràng buộc:\n"
                    + "1. Chế độ ăn của người dùng: '%s'. Mục tiêu: %s.\n"
                    + "2. MỤC TIÊU CALO: %d kcal/ngày. Chọn 6 bữa mỗi ngày:\n"
                    + "   - Bữa_Sáng (~20%% calo ngày)\n"
                    + "   - Ăn_Nhẹ_Sáng (~10%%)\n"
                    + "   - Bữa_Trưa (~30%%)\n"
                    + "   - Ăn_Nhẹ_Chiều (~10%%)\n"
                    + "   - Bữa_Tối (~25%%)\n"
                    + "   - Ăn_Nhẹ_Tối (~5%%)\n"
                    + "   Tổng calo phải xấp xỉ mục tiêu ngày (cho phép +/- 300 kcal).\n"
                    + "3. XUẤT TOÀN BỘ JSON. KHÔNG dùng markdown. KHÔNG cắt ngang.\n"
                    + "4. Cấu trúc JSON phải chính xác như sau:\n"
                    + "{ \"days\": [ \n"
                    + "  { \"dayName\": \"Thứ Hai\", \"totalCalories\": 0, \"meals\": [ \n"
                    + "    { \"type\": \"Breakfast\", \"recipeId\": 1, \"recipeName\": \"Tên Món\", \"calories\": 0 }, \n"
                    + "    { \"type\": \"Snack_Morning\", \"recipeId\": 2, \"recipeName\": \"Tên Món\", \"calories\": 0 }, \n"
                    + "    { \"type\": \"Lunch\", \"recipeId\": 3, \"recipeName\": \"Tên Món\", \"calories\": 0 }, \n"
                    + "    { \"type\": \"Snack_Afternoon\", \"recipeId\": 4, \"recipeName\": \"Tên Món\", \"calories\": 0 }, \n"
                    + "    { \"type\": \"Dinner\", \"recipeId\": 5, \"recipeName\": \"Tên Món\", \"calories\": 0 }, \n"
                    + "    { \"type\": \"Snack_Evening\", \"recipeId\": 6, \"recipeName\": \"Tên Món\", \"calories\": 0 } \n"
                    + "  ] },\n"
                    + "  ... (Lặp lại cho tất cả 7 ngày) ... \n"
                    + "] }\n"
                    + "QUAN TRỌNG: Chỉ xuất JSON hợp lệ. Không dừng cho đến khi hoàn thành cả 7 ngày.",
                    recipeContext, constraintText, dietType, goal, calories
            );

            // 4. Payload
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", ollamaTextModel);
            requestBody.put("prompt", promptText);
            requestBody.put("stream", false);

            requestBody.put("options", Map.of(
                    "num_ctx", 16384, // Tăng context cho model 120B
                    "num_predict", 8192, // Tăng lên 8k token để đảm bảo viết hết 7 ngày
                    "temperature", 0.3 // Giảm sáng tạo để tập trung vào đúng định dạng JSON
            ));

            // 5. Gửi Request
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.add("User-Agent", "ZaderFood-Backend-v1");
            headers.add("ngrok-skip-browser-warning", "true");
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            System.out.println("--- SENDING PROMPT TO AI ---");
            String rawResponse = getRestTemplate().postForObject(ollamaUrl, entity, String.class);

            saveAiLog("WEEKLY_PLAN", promptText, rawResponse);
            
            return parseWeeklyPlanResponse(rawResponse);

        } catch (Exception e) {
            System.err.println("AI SERVICE ERROR: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    private WeeklyPlanDTO parseWeeklyPlanResponse(String jsonResponse) {
        try {
            JsonNode root = objectMapper.readTree(jsonResponse);
            if (root.has("response")) {
                String aiText = root.get("response").asText();

                // --- DEBUG: IN RA CONSOLE ĐỂ XEM LỖI ---
                System.out.println("--- RAW AI RESPONSE ---");
                System.out.println(aiText);
                System.out.println("-----------------------");

                // CLEAN UP JSON
                String cleanJson = aiText.replaceAll("```json", "")
                        .replaceAll("```", "")
                        .replaceAll("//.*", "") // Xóa comment //
                        .trim();

                // Tìm điểm bắt đầu và kết thúc JSON hợp lệ
                int jsonStart = cleanJson.indexOf("{");
                int jsonEnd = cleanJson.lastIndexOf("}");

                if (jsonStart != -1 && jsonEnd != -1) {
                    cleanJson = cleanJson.substring(jsonStart, jsonEnd + 1);
                    WeeklyPlanDTO dto = objectMapper.readValue(cleanJson, WeeklyPlanDTO.class);

                    if (dto.days != null) {
                        for (WeeklyPlanDTO.DailyPlan day : dto.days) {
                            if (day.meals != null) {
                                int realTotal = 0;
                                for (WeeklyPlanDTO.Meal meal : day.meals) {
                                    realTotal += meal.calories;
                                    if (meal.recipeId != null) {
                                        recipeRepository.findById(meal.recipeId).ifPresent(r -> {
                                            meal.servingWeightGrams = r.getServingWeightGrams();
                                        });
                                    }
                                }
                                day.totalCalories = realTotal;
                            }
                        }
                    }
                    // -------------------------------------

                    return dto;
                }
            }
        } catch (Exception e) {
            System.err.println("PARSE ERROR. Check RAW AI RESPONSE above.");
            e.printStackTrace();
        }
        return null;
    }

    public List<PantryRecipeMatchDTO> generateRecipesFromPantry(String ingredientsList, String userProfileInfo) {
        try {
            // --- PROMPT ĐÃ CẬP NHẬT: BẮT BUỘC TIẾNG VIỆT, PHÙ HỢP VỚI THỊ TRƯỜNG VIỆT NAM ---
            String promptText = String.format(
                    "Vai trò: Đầu bếp và Chuyên gia dinh dưỡng Việt Nam chuyên nghiệp.\n"
                    + "Đầu vào: Tủ lạnh của người dùng: [%s]. Hồ sơ người dùng: %s.\n"
                    + "Nhiệm vụ: Tạo ĐÚNG 3 đến 4 công thức nấu ăn độc đáo, phù hợp với khẩu vị và thị trường Việt Nam.\n"
                    + "Ưu tiên các món ăn quen thuộc như: Cơm, Phở, Bún, Canh, Xào, Kho, Hấp, Lẩu v.v.\n"
                    + "Ràng buộc:\n"
                    + "1. TOÀN BỘ NỘI DUNG PHẢI BẰNG TIẾNG VIỆT (tên món, mô tả, nguyên liệu, các bước thực hiện).\n"
                    + "2. XUẤT MẢNG JSON THUẦN TÚY. KHÔNG có văn bản giới thiệu. KHÔNG có ghi chú. KHÔNG Markdown (```json).\n"
                    + "3. Cấu trúc phải là một Mảng đơn các Object: [ { ... }, { ... } ]\n"
                    + "4. Các trường bắt buộc: name, description, ingredientsList, stepsList, timeMin, calories, servings, matchPercentage, missingCount.\n"
                    + "Cấu trúc đầu ra:\n"
                    + "[ \n"
                    + "  { \n"
                    + "    \"name\": \"Tên Món Ăn Tiếng Việt\", \n"
                    + "    \"description\": \"Mô tả ngắn gọn, hấp dẫn bằng tiếng Việt\", \n"
                    + "    \"timeMin\": 30, \n"
                    + "    \"calories\": 450, \n"
                    + "    \"servings\": 2, \n"
                    + "    \"ingredientsList\": [\"2 quả Trứng\", \"1 muỗng canh Dầu ăn\"], \n"
                    + "    \"stepsList\": [\"Bước 1: Đập trứng vào bát\", \"Bước 2: Chiên đều\"], \n"
                    + "    \"matchPercentage\": 90, \n"
                    + "    \"missingCount\": 0, \n"
                    + "    \"imageUrl\": \"https://placehold.co/300?text=Mon+An+VN\" \n"
                    + "  } \n"
                    + "]",
                    ingredientsList, userProfileInfo
            );

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", ollamaTextModel); // Hoặc model bạn đang dùng (mistral/gemma)
            requestBody.put("prompt", promptText);
            requestBody.put("stream", false);

            // Tăng context để AI không bị cắt chữ khi trả về nhiều trường
            requestBody.put("options", Map.of("temperature", 0.7, "num_ctx", 4096));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.add("User-Agent", "ZaderFood-Backend-v1");
            headers.add("ngrok-skip-browser-warning", "true");
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            String rawResponse = getRestTemplate().postForObject(ollamaUrl, entity, String.class);
            System.out.println(rawResponse);
            
            saveAiLog("PANTRY_RECIPES", promptText, rawResponse);

            return parsePantryAiResponse(rawResponse);

        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    private List<PantryRecipeMatchDTO> parsePantryAiResponse(String jsonResponse) {
        try {
            JsonNode root = objectMapper.readTree(jsonResponse);
            if (root.has("response")) {
                String aiText = root.get("response").asText();

                // --- DEBUG: In ra để xem AI trả về gì ---
                System.out.println("AI Raw Text: " + aiText);

                // 1. Tách chuỗi JSON an toàn hơn Regex cũ
                String cleanJson = extractJsonArray(aiText);

                // 2. Parse thử thành JsonNode để kiểm tra cấu trúc
                JsonNode arrayNode = objectMapper.readTree(cleanJson);

                // 3. Xử lý trường hợp mảng lồng mảng [[...]]
                if (arrayNode.isArray() && arrayNode.size() > 0 && arrayNode.get(0).isArray()) {
                    System.out.println("AI returned nested array, unwrapping...");
                    // Lấy mảng bên trong ra để parse
                    cleanJson = arrayNode.get(0).toString();
                }

                // 4. Parse thành List DTO
                return objectMapper.readValue(cleanJson, new com.fasterxml.jackson.core.type.TypeReference<List<PantryRecipeMatchDTO>>() {
                });
            }
        } catch (Exception e) {
            System.err.println("AI Parse Error: " + e.getMessage());
            // e.printStackTrace(); // Bật lên nếu cần xem chi tiết
        }
        return Collections.emptyList();
    }

    // Hàm phụ trợ: Tìm JSON Array chuẩn xác trong văn bản hỗn loạn
    private String extractJsonArray(String text) {
        int start = text.indexOf("[");
        int end = text.lastIndexOf("]");

        if (start == -1 || end == -1 || end < start) {
            return "[]"; // Không tìm thấy JSON
        }

        return text.substring(start, end + 1);
    }

    @Transactional
    public void saveAiRecipeToCollection(Integer userId, AiFoodResponse aiData, String imageUrl) {
        // 1. CHUẨN HÓA DỮ LIỆU
        // Xử lý Time: "12 minutes" -> 12
        Integer timeMin = 0;
        try {
            String timeStr = aiData.getTime().replaceAll("[^0-9]", "");
            timeMin = timeStr.isEmpty() ? 0 : Integer.parseInt(timeStr);
        } catch (Exception e) {
        }

        if (imageUrl != null && imageUrl.startsWith("data:image")) {
            // Nếu là base64 -> Lưu thành file -> Lấy đường dẫn ngắn (/uploads/xyz.jpg)
            imageUrl = fileStorageService.storeBase64(imageUrl);
        }

        // 2. LƯU VÀO BẢNG AiSavedRecipes
        String ingText = String.join("\n", aiData.getIngredients());
        String stepText = String.join("\n", aiData.getInstructions());

        // 2. LƯU VÀO BẢNG AiSavedRecipes
        AiSavedRecipes savedRecipe = AiSavedRecipes.builder()
                .userId(userId)
                .name(aiData.getDishName())
                .description("AI Generated Recipe") // Có thể update AI để trả về description sau
                .ingredientsText(ingText)
                .stepsText(stepText)
                .timeMinutes(timeMin)
                .totalCalories(BigDecimal.valueOf(aiData.getCalories()))
                .servings(1) // Mặc định
                .imageUrl(imageUrl)
                .build();

        savedRecipe = aiRecipeRepo.save(savedRecipe);

        // 3. XỬ LÝ COLLECTION "Ai Chef Favorites"
        String collectionName = "Ai Chef Favorites";
        RecipeCollection collection = collectionRepo.findByUserIdAndName(userId, collectionName)
                .orElse(null);

        if (collection == null) {
            // Tạo mới nếu chưa có
            collection = RecipeCollection.builder()
                    .userId(userId)
                    .name(collectionName)
                    .isPublic(false)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .isDeleted(false)
                    .build();
            collection = collectionRepo.save(collection);
        }

        // 4. LIÊN KẾT VÀO COLLECTION (CollectionItems)
        // Kiểm tra xem đã tồn tại trong collection chưa (tránh duplicate)
        boolean exists = collectionItemRepo.existsByCollectionIdAndAiRecipeId(collection.getCollectionId(), savedRecipe.getAiRecipeId());

        if (!exists) {
            CollectionItem item = CollectionItem.builder()
                    .collectionId(collection.getCollectionId())
                    .aiRecipeId(savedRecipe.getAiRecipeId()) // Liên kết ID của AI Recipe
                    .recipeId(null) // Cái này null vì đây là món AI, không phải Recipe thường
                    .addedAt(LocalDateTime.now())
                    .build();
            collectionItemRepo.save(item);
        }
    }


    /**
     * Chạy AI trong nền (thread pool) – không bị Cloudflare timeout.
     * Spring sẽ tự quản lý thread, cần @EnableAsync trong @SpringBootApplication.
     */
    @Async
    public void generateWeeklyPlanAsync(String jobId, int calories, String dietType, String goal, String foodConstraint) {
        try {
            WeeklyPlanDTO plan = generateWeeklyPlan(calories, dietType, goal, foodConstraint);
            if (plan != null) {
                asyncJobService.completeJob(jobId, plan);
            } else {
                asyncJobService.failJob(jobId, "AI trả về kết quả rỗng. Vui lòng thử lại.");
            }
        } catch (Exception e) {
            asyncJobService.failJob(jobId, "Lỗi AI: " + e.getMessage());
        }
    }
}

