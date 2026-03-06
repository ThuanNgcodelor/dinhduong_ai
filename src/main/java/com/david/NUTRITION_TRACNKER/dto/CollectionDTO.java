package com.david.NUTRITION_TRACNKER.dto;

import com.david.NUTRITION_TRACNKER.entity.RecipeCollection;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CollectionDTO {
    private RecipeCollection collection;
    private int itemCount;
}