package com.david.NUTRITION_TRACNKER.dto;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class OllamaRequest {
    private String model;
    private String prompt;
    private boolean stream;
    private List<String> images;
}