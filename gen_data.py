import random
import datetime

# User's provided ingredients mapped by ID
ingredients = {
    # Proteins
    24: {"name": "Thịt bò nạc thăn", "unit": "g", "cals": 143, "type": "protein"},
    25: {"name": "Thịt bò băm nạc 95%", "unit": "g", "cals": 137, "type": "protein"},
    26: {"name": "Thịt heo nạc thăn", "unit": "g", "cals": 143, "type": "protein"},
    28: {"name": "Thịt gà ức không da", "unit": "g", "cals": 110, "type": "protein"},
    30: {"name": "Cá hồi Na Uy", "unit": "g", "cals": 206, "type": "protein"},
    31: {"name": "Cá ngừ tươi", "unit": "g", "cals": 132, "type": "protein"},
    35: {"name": "Tôm sú bóc vỏ", "unit": "g", "cals": 85, "type": "protein"},
    37: {"name": "Mực tươi", "unit": "g", "cals": 82, "type": "protein"},
    63: {"name": "Đậu hũ tofu cứng", "unit": "g", "cals": 70, "type": "protein"}, # Vegan
    64: {"name": "Đậu hũ non", "unit": "g", "cals": 55, "type": "protein"},
    76: {"name": "Trứng gà nguyên quả", "unit": "quả", "cals": 143, "type": "protein"}, # Veg

    # Carbs
    65: {"name": "Gạo trắng nấu chín", "unit": "g", "cals": 130, "type": "carb"},
    66: {"name": "Gạo lứt nấu chín", "unit": "g", "cals": 112, "type": "carb"},
    67: {"name": "Bún tươi luộc", "unit": "g", "cals": 109, "type": "carb"},
    68: {"name": "Bánh phở tươi", "unit": "g", "cals": 108, "type": "carb"},
    70: {"name": "Bánh mì nguyên cám", "unit": "g", "cals": 247, "type": "carb"},
    71: {"name": "Yến mạch cán dẹp raw", "unit": "g", "cals": 389, "type": "carb"},
    72: {"name": "Khoai lang đỏ luộc", "unit": "g", "cals": 86, "type": "carb"},
    74: {"name": "Quinoa nấu chín", "unit": "g", "cals": 120, "type": "carb"},

    # Veggies
    40: {"name": "Cải xanh cải thìa", "unit": "g", "cals": 13, "type": "veg"},
    42: {"name": "Bông cải xanh broccoli", "unit": "g", "cals": 34, "type": "veg"},
    46: {"name": "Cà chua bi", "unit": "g", "cals": 18, "type": "veg"},
    48: {"name": "Ớt chuông đỏ", "unit": "g", "cals": 31, "type": "veg"},
    50: {"name": "Dưa leo", "unit": "g", "cals": 16, "type": "veg"},
    51: {"name": "Cà rốt", "unit": "g", "cals": 41, "type": "veg"},
    55: {"name": "Hành tây", "unit": "g", "cals": 40, "type": "veg"},
    60: {"name": "Nấm hương shiitake", "unit": "g", "cals": 34, "type": "veg"},

    # Fats / Condiments
    81: {"name": "Dầu ô liu extra virgin", "unit": "ml", "cals": 884, "type": "fat"},
    82: {"name": "Dầu mè vừng", "unit": "ml", "cals": 884, "type": "fat"},
    84: {"name": "Nước tương soy sauce", "unit": "ml", "cals": 60, "type": "fat"},
    87: {"name": "Muối ăn", "unit": "g", "cals": 0, "type": "spice"},
    88: {"name": "Tiêu đen xay", "unit": "g", "cals": 251, "type": "spice"},
    98: {"name": "Bơ avocado", "unit": "quả", "cals": 160, "type": "fat"},
    96: {"name": "Bơ đậu phộng natural", "unit": "g", "cals": 590, "type": "fat"}
}

cooking_methods = ["Áp chảo", "Nướng", "Luộc", "Hấp", "Xào", "Salad", "Kho nhạt"]
diets = ["Eat clean", "Keto", "High protein", "Low carb", "Vegan", "Vegetarian", "Weight loss", "Muscle gain", "Gluten free", "Dairy free", "Maintenance"]

recipes_sql = []
rec_ings_sql = []
rec_steps_sql = []

recipe_id_start = 1000
now = datetime.datetime.now().strftime("%Y-%m-%d %H:%M:%S")

proteins = [k for k, v in ingredients.items() if v["type"] == "protein"]
carbs = [k for k, v in ingredients.items() if v["type"] == "carb"]
veggies = [k for k, v in ingredients.items() if v["type"] == "veg"]
fats = [k for k, v in ingredients.items() if v["type"] == "fat"]

def get_cals(ing_id, qty):
    base = ingredients[ing_id]["cals"]
    unit = ingredients[ing_id]["unit"]
    if unit in ["g", "ml"]:
        return (base / 100.0) * qty
    else:
        return base * qty

# Generate 250 recipes
for i in range(250):
    recipe_id = recipe_id_start + i
    diet = random.choice(diets)
    method = random.choice(cooking_methods)
    
    sel_prots = []
    sel_carbs = []
    sel_vegs = []
    sel_fats = []
    
    if diet in ["Vegan"]:
        sel_prots = [random.choice([63, 64])] # Tofu
        method = random.choice(["Áp chảo", "Kho nhạt", "Hấp", "Xào"])
    elif diet in ["Vegetarian"]:
        sel_prots = [random.choice([63, 64, 76])]
    else:
        sel_prots = [random.choice([p for p in proteins if p not in [63, 64]])]
        
    if diet in ["Keto", "Low carb"]:
        sel_carbs = []
        sel_fats = random.sample(fats, k=random.randint(1, 2))
    elif diet in ["Weight loss", "Eat clean"]:
        sel_carbs = [random.choice([66, 72, 74])] # Gạo lứt, Khoai lang, Quinoa
    elif diet == "Muscle gain":
        sel_carbs = [random.choice(carbs)]
        if len(sel_prots) == 1:
            sel_prots.append(random.choice([p for p in proteins if p != sel_prots[0]]))
    else:
        sel_carbs = [random.choice(carbs)]
        
    sel_vegs = random.sample(veggies, k=random.randint(1, 3))
    if not sel_fats:
        sel_fats = [random.choice(fats)]
    
    spice1 = 87 # Muối
    spice2 = 88 # Tiêu
    
    # Calculate Calories & Amounts
    total_cals = 0
    ings_to_add = []
    
    for p in sel_prots:
        m = 200 if diet == "Muscle gain" else 150
        if ingredients[p]["unit"] == "quả": m = 2
        total_cals += get_cals(p, m)
        ings_to_add.append((p, m))
        
    for c in sel_carbs:
        m = 200 if diet == "Muscle gain" else 100
        total_cals += get_cals(c, m)
        ings_to_add.append((c, m))
        
    for v in sel_vegs:
        m = 100
        total_cals += get_cals(v, m)
        ings_to_add.append((v, m))
        
    for f in sel_fats:
        m = 10
        if ingredients[f]["unit"] == "quả": m = 0.5
        total_cals += get_cals(f, m)
        ings_to_add.append((f, m))
        
    ings_to_add.append((spice1, 2))
    ings_to_add.append((spice2, 1))

    main_ing_name = " ".join(ingredients[sel_prots[0]]["name"].split()[0:3])
    side_ing_name = ""
    if sel_vegs:
        side_ing_name = " và " + " ".join(ingredients[sel_vegs[0]]["name"].split()[0:2])
        
    diet_suffix = f" chuẩn {diet}"
    
    recipe_name = f"{main_ing_name} {method.lower()}{side_ing_name}{diet_suffix} #{i+1}"
    desc = f"Món ngon {diet} vô cùng dinh dưỡng. Phương pháp {method.lower()} giúp giữ trọn hương vị."
    
    diff = random.choice(["EASY", "MEDIUM", "HARD"])
    prep = random.randint(5, 20)
    cook = random.randint(10, 40)
    servings = random.randint(1, 2)
    cals = round(total_cals)
    
    recipes_sql.append(f"({recipe_id}, '{recipe_name}', '{desc}', '{diff}', {prep}, {cook}, {servings}, {cals}, 'ACTIVE', 1, 1, '{now}', '{now}', 0)")
    
    for ing_id, qty in ings_to_add:
        unit = ingredients[ing_id]["unit"]
        rec_ings_sql.append(f"({recipe_id}, {ing_id}, {qty}, '{unit}', '', '{now}', '{now}', 0)")
        
    rec_steps_sql.append(f"({recipe_id}, 1, 'Sơ chế nguyên liệu: Rửa sạch {ingredients[sel_prots[0]]['name']} và thái vừa ăn.', '{now}', '{now}')")
    if "Áp chảo" in method or "Xào" in method:
        rec_steps_sql.append(f"({recipe_id}, 2, 'Làm nóng chảo với dầu, cho nguyên liệu chính vào đảo đều đến khi xém vàng.', '{now}', '{now}')")
    elif "Nướng" in method:
        rec_steps_sql.append(f"({recipe_id}, 2, 'Làm nóng lò, ướp gia vị 10 phút, nướng ở 180 độ C trong {cook} phút.', '{now}', '{now}')")
    elif "Luộc" in method or "Hấp" in method:
        rec_steps_sql.append(f"({recipe_id}, 2, 'Đun sôi nước, cho nguyên liệu vào nấu chín mềm giữ nguyên độ ngọt tự nhiên.', '{now}', '{now}')")
    else:
        rec_steps_sql.append(f"({recipe_id}, 2, 'Chế biến các thành phần đã chuẩn bị sao cho thấm đều gia vị.', '{now}', '{now}')")
        
    rec_steps_sql.append(f"({recipe_id}, 3, 'Trình bày rau củ và món chính ra đĩa, rắc nêm thêm ít tiêu và thưởng thức nóng!', '{now}', '{now}')")

with open('large_recipe_seed.sql', 'w', encoding='utf-8') as f:
    f.write("SET FOREIGN_KEY_CHECKS = 0;\n\n")
    
    # 250 at once might be too long, let's chunk them
    def chunker(seq, size):
        return (seq[pos:pos + size] for pos in range(0, len(seq), size))
        
    for chunk in chunker(recipes_sql, 50):
        f.write("INSERT INTO Recipes (RecipeId, Name, Description, Difficulty, PrepTimeMin, CookTimeMin, Servings, TotalCalories, Status, CreatedByUserId, IsNutritionist, CreatedAt, UpdatedAt, IsDeleted)\nVALUES\n")
        f.write(",\n".join(chunk))
        f.write(";\n\n")

    for chunk in chunker(rec_ings_sql, 100):
        f.write("INSERT INTO RecipeIngredients (RecipeId, IngredientId, Quantity, Unit, Note, CreatedAt, UpdatedAt, IsDeleted)\nVALUES\n")
        f.write(",\n".join(chunk))
        f.write(";\n\n")

    for chunk in chunker(rec_steps_sql, 100):
        f.write("INSERT INTO RecipeSteps (RecipeId, StepNumber, Instruction, CreatedAt, UpdatedAt)\nVALUES\n")
        f.write(",\n".join(chunk))
        f.write(";\n\n")
        
    f.write("SET FOREIGN_KEY_CHECKS = 1;\n")
print(f"Generated 250 recipes in large_recipe_seed.sql")
