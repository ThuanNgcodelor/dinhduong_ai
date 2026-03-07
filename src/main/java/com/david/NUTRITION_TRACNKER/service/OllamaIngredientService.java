package com.david.NUTRITION_TRACNKER.service;

import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import com.david.NUTRITION_TRACNKER.dto.IngredientAnalysisDTO;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class OllamaIngredientService {

    @Value("${ollama.host.url}") 
    private String ollamaUrl;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public IngredientAnalysisDTO analyzeIngredient(String name, String category, String baseUnit, MultipartFile imageFile) {
        try {
            // 1. Chuyển ảnh sang Base64 (Nếu có)
            String base64Image = null;
            if (imageFile != null && !imageFile.isEmpty()) {
                base64Image = Base64.getEncoder().encodeToString(imageFile.getBytes());
            }

            // 2. Tạo Prompt - BẮT BUỘC TIẾNG VIỆT
            String promptText = String.format(
                "Bạn là chuyên gia dinh dưỡng Việt Nam. Hãy phân tích nguyên liệu thực phẩm sau: '%s' (Danh mục: %s). " +
                "Đơn vị cơ bản là: %s. " +
                "Ước tính giá trị dinh dưỡng trung bình cho 100%s của nguyên liệu này dựa trên tên và ảnh (nếu có). " +
                "CHỈ trả về một đối tượng JSON với các khóa: 'calories' (số), 'protein' (số), 'fat' (số), 'carbs' (số). " +
                "Không thêm giải thích hay định dạng markdown như ```json. Chỉ trả về JSON thuần túy.",
                name, category, baseUnit, baseUnit
            );

            // 3. Tạo Payload gửi Ollama
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", "llava"); // Dùng model LLaVA
            requestBody.put("prompt", promptText);
            requestBody.put("stream", false); // Tắt stream để nhận 1 cục JSON
            
            if (base64Image != null) {
                requestBody.put("images", Collections.singletonList(base64Image));
            }

            // 4. Gửi Request
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            String rawResponse = restTemplate.postForObject(ollamaUrl, entity, String.class);

            // 5. Parse kết quả
            return parseOllamaResponse(rawResponse);

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private IngredientAnalysisDTO parseOllamaResponse(String jsonResponse) {
        try {
            // Ollama trả về: { "response": "{ \"calories\": ... }" }
            JsonNode root = objectMapper.readTree(jsonResponse);
            
            if (root.has("response")) {
                String aiText = root.get("response").asText();
                
                // Làm sạch Markdown nếu AI lỡ thêm vào
                String cleanJson = aiText.replaceAll("```json", "")
                                         .replaceAll("```", "")
                                         .trim();

                // Cắt lấy phần JSON thuần túy nhất
                int jsonStart = cleanJson.indexOf("{");
                int jsonEnd = cleanJson.lastIndexOf("}");
                
                if (jsonStart != -1 && jsonEnd != -1) {
                    cleanJson = cleanJson.substring(jsonStart, jsonEnd + 1);
                    return objectMapper.readValue(cleanJson, IngredientAnalysisDTO.class);
                }
            }
        } catch (Exception e) {
            System.err.println("Error parsing Ollama response: " + e.getMessage());
        }
        return null;
    }
}