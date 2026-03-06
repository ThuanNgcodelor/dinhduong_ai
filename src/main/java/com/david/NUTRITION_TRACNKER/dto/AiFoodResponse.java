package com.david.NUTRITION_TRACNKER.dto;

import lombok.Data;
import java.util.List;

@Data
public class AiFoodResponse {
    private String dishName;
    private int calories;
    private String protein;
    private String carbs;
    private String fat;
    private String time;
    private List<String> ingredients;
    private List<String> instructions;
    private String error;
}