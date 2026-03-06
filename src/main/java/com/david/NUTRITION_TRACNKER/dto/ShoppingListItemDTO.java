package com.david.NUTRITION_TRACNKER.dto;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class ShoppingListItemDTO {
    private Integer itemId;
    private String name;
    private BigDecimal quantity;
    private String unit;
    private Boolean isBought;
    private String categoryName;
    private String imageUrl;
    
    private BigDecimal pantryStock;
}