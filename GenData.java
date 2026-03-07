import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public class GenData {
    static class Ing {
        int id; String name, unit, type; double cals; boolean isVegan;
        Ing(int id, String name, String unit, double cals, String type, boolean isVegan) {
            this.id = id; this.name = name; this.unit = unit; this.cals = cals; this.type = type; this.isVegan = isVegan;
        }
    }

    public static void main(String[] args) throws Exception {
        Map<Integer, Ing> map = new HashMap<>();
        map.put(24, new Ing(24, "Thịt bò nạc thăn", "g", 143, "protein", false));
        map.put(25, new Ing(25, "Thịt bò băm nạc 95%", "g", 137, "protein", false));
        map.put(26, new Ing(26, "Thịt heo nạc thăn", "g", 143, "protein", false));
        map.put(28, new Ing(28, "Thịt gà ức không da", "g", 110, "protein", false));
        map.put(30, new Ing(30, "Cá hồi Na Uy", "g", 206, "protein", false));
        map.put(31, new Ing(31, "Cá ngừ tươi", "g", 132, "protein", false));
        map.put(35, new Ing(35, "Tôm sú bóc vỏ", "g", 85, "protein", false));
        map.put(63, new Ing(63, "Đậu hũ tofu cứng", "g", 70, "protein", true));
        map.put(64, new Ing(64, "Đậu hũ non", "g", 55, "protein", true));
        map.put(76, new Ing(76, "Trứng gà nguyên quả", "quả", 143, "protein", false));
        
        map.put(65, new Ing(65, "Gạo trắng nấu chín", "g", 130, "carb", true));
        map.put(66, new Ing(66, "Gạo lứt nấu chín", "g", 112, "carb", true));
        map.put(67, new Ing(67, "Bún tươi luộc", "g", 109, "carb", true));
        map.put(70, new Ing(70, "Bánh mì nguyên cám", "g", 247, "carb", true));
        map.put(71, new Ing(71, "Yến mạch cán dẹp", "g", 389, "carb", true));
        map.put(72, new Ing(72, "Khoai lang đỏ luộc", "g", 86, "carb", true));

        map.put(40, new Ing(40, "Cải xanh cải thìa", "g", 13, "veg", true));
        map.put(42, new Ing(42, "Bông cải xanh broccoli", "g", 34, "veg", true));
        map.put(46, new Ing(46, "Cà chua bi", "g", 18, "veg", true));
        map.put(48, new Ing(48, "Ớt chuông đỏ", "g", 31, "veg", true));
        map.put(50, new Ing(50, "Dưa leo", "g", 16, "veg", true));
        map.put(55, new Ing(55, "Hành tây", "g", 40, "veg", true));
        map.put(60, new Ing(60, "Nấm hương shiitake", "g", 34, "veg", true));

        map.put(81, new Ing(81, "Dầu ô liu extra virgin", "ml", 884, "fat", true));
        map.put(84, new Ing(84, "Nước tương soy sauce", "ml", 60, "fat", true));
        map.put(87, new Ing(87, "Muối ăn", "g", 0, "spice", true));
        map.put(88, new Ing(88, "Tiêu đen xay", "g", 251, "spice", true));
        map.put(98, new Ing(98, "Bơ avocado", "quả", 160, "fat", true));

        List<Integer> proteins = new ArrayList<>();
        List<Integer> carbs = new ArrayList<>();
        List<Integer> vegs = new ArrayList<>();
        List<Integer> fats = new ArrayList<>();
        
        for (Ing ig : map.values()) {
            if (ig.type.equals("protein")) proteins.add(ig.id);
            if (ig.type.equals("carb")) carbs.add(ig.id);
            if (ig.type.equals("veg")) vegs.add(ig.id);
            if (ig.type.equals("fat")) fats.add(ig.id);
        }

        String[] diets = {"Eat clean", "Keto", "High protein", "Low carb", "Vegan", "Vegetarian", "Weight loss", "Muscle gain", "Gluten free", "Dairy free", "Maintenance"};
        String[] methods = {"Áp chảo", "Nướng", "Luộc", "Hấp", "Xào", "Salad", "Kho nhạt", "Hầm dừ"};

        Random rand = new Random(888); 
        String now = java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        List<String> recipes = new ArrayList<>();
        List<String> recipeIngredients = new ArrayList<>();
        List<String> recipeSteps = new ArrayList<>();

        Set<String> uniqueSignatures = new HashSet<>();
        int generatedCount = 0;
        int maxRecipes = 400; // Generate exactly 400 recipes

        while(generatedCount < maxRecipes) {
            String diet = diets[rand.nextInt(diets.length)];
            String method = methods[rand.nextInt(methods.length)];

            List<Integer> selProts = new ArrayList<>();
            List<Integer> selCarbs = new ArrayList<>();
            List<Integer> selVegs = new ArrayList<>();
            List<Integer> selFats = new ArrayList<>();

            if (diet.equals("Vegan")) {
                selProts.add(63); // Tofu
            } else if (diet.equals("Vegetarian")) {
                selProts.add(rand.nextBoolean() ? 63 : 76);
            } else {
                int p; do { p = proteins.get(rand.nextInt(proteins.size())); } while(p == 63 || p == 64);
                selProts.add(p);
            }

            if (diet.equals("Keto") || diet.equals("Low carb")) {
                selFats.add(fats.get(rand.nextInt(fats.size())));
            } else if (diet.equals("Weight loss") || diet.equals("Eat clean")) {
                selCarbs.add(rand.nextBoolean() ? 66 : 72);
            } else if (diet.equals("Muscle gain")) {
                selCarbs.add(carbs.get(rand.nextInt(carbs.size())));
                int p2; do { p2 = proteins.get(rand.nextInt(proteins.size())); } while(p2 == selProts.get(0));
                selProts.add(p2);
            } else {
                selCarbs.add(carbs.get(rand.nextInt(carbs.size())));
            }

            selVegs.add(vegs.get(rand.nextInt(vegs.size())));
            if (rand.nextBoolean()) selVegs.add(vegs.get(rand.nextInt(vegs.size())));
            
            if (selFats.isEmpty()) selFats.add(fats.get(rand.nextInt(fats.size())));

            class Item { int id; double qty; Item(int i, double q) { id = i; qty = q;} }
            List<Item> items = new ArrayList<>();
            double totalCals = 0;

            for (int p : selProts) {
                double q = diet.equals("Muscle gain") ? 250 : 150;
                if (map.get(p).unit.equals("quả")) q = 2;
                items.add(new Item(p, q));
                totalCals += (map.get(p).unit.equals("g") || map.get(p).unit.equals("ml")) ? (map.get(p).cals * q / 100) : map.get(p).cals * q;
            }
            for (int c : selCarbs) {
                double q = diet.equals("Muscle gain") ? 200 : 100;
                items.add(new Item(c, q));
                totalCals += (map.get(c).cals * q / 100);
            }
            for (int v : selVegs) {
                double q = 100;
                items.add(new Item(v, q));
                totalCals += (map.get(v).cals * q / 100);
            }
            for (int f : selFats) {
                double q = map.get(f).unit.equals("quả") ? 0.5 : 10;
                items.add(new Item(f, q));
                totalCals += (map.get(f).unit.equals("ml")) ? (map.get(f).cals * q / 100) : map.get(f).cals * q;
            }
            items.add(new Item(87, 2)); // Salt
            items.add(new Item(88, 1)); // Pepper

            String mainName = map.get(selProts.get(0)).name;
            String sideName = map.get(selVegs.get(0)).name.split(" ")[0];
            
            // Signature checks for uniqueness: Same target Diet, Method, Protein, Veg -> count as duplicate
            // We use this to ensure we don't have basically identical recipes with different # tags
            String signature = diet + "|" + method + "|" + mainName + "|" + sideName;
            if (uniqueSignatures.contains(signature)) {
                continue; // try generating a new random set
            }
            uniqueSignatures.add(signature);

            int recipeId = 2000 + generatedCount;

            String title = String.format("%s %s và %s kiểu %s #%d", mainName, method.toLowerCase(), sideName, diet, generatedCount+1);
            String desc = "Công thức " + diet + " đặc biệt tốt cho sức khỏe với nguyên liệu tuyển chọn.";
            String diff = new String[]{"EASY", "MEDIUM", "HARD"}[rand.nextInt(3)];
            
            String keyword = "meal";
            String lowerMain = mainName.toLowerCase();
            if (lowerMain.contains("bò")) keyword = "beef";
            else if (lowerMain.contains("heo")) keyword = "pork";
            else if (lowerMain.contains("gà")) keyword = "chicken";
            else if (lowerMain.contains("cá")) keyword = "fish";
            else if (lowerMain.contains("tôm")) keyword = "shrimp";
            else if (lowerMain.contains("đậu hũ") || lowerMain.contains("tofu")) keyword = "tofu";
            else if (lowerMain.contains("trứng")) keyword = "egg";

            String imageUrl = "https://loremflickr.com/400/300/" + keyword + ",dish,food/all?lock=" + recipeId;
            
            recipes.add(String.format("(%d, '%s', '%s', '%s', %d, %d, %d, %d, 'ACTIVE', 1, 1, '%s', '%s', 0, '%s')", 
                recipeId, title, desc, diff, 10 + rand.nextInt(15), 15 + rand.nextInt(30), 1 + rand.nextInt(2), (int)totalCals, now, now, imageUrl));

            for (Item item : items) {
                recipeIngredients.add(String.format("(%d, %d, %.1f, '%s', '', '%s', '%s', 0)", 
                    recipeId, item.id, item.qty, map.get(item.id).unit, now, now));
            }

            recipeSteps.add(String.format("(%d, 1, 'Sơ chế nguyên liệu: Rửa sạch %s và %s.', '%s', '%s')", recipeId, mainName, sideName, now, now));
            recipeSteps.add(String.format("(%d, 2, 'Tiến hành %s nguyên liệu chính trên lửa vừa.', '%s', '%s')", recipeId, method.toLowerCase(), now, now));
            recipeSteps.add(String.format("(%d, 3, 'Trình bày và thưởng thức lúc còn nóng.', '%s', '%s')", recipeId, now, now));

            generatedCount++;
        }

        PrintWriter out = new PrintWriter("large_recipe_seed.sql", "UTF-8");
        out.println("SET FOREIGN_KEY_CHECKS = 0;\n");
        // Clear all previously generated bulk recipes so user can just run this file cleanly
        out.println("DELETE FROM RecipeSteps WHERE RecipeId >= 2000;");
        out.println("DELETE FROM RecipeIngredients WHERE RecipeId >= 2000;");
        out.println("DELETE FROM Recipes WHERE RecipeId >= 2000;\n");
        
        writeChunks(out, "INSERT INTO Recipes (RecipeId, Name, Description, Difficulty, PrepTimeMin, CookTimeMin, Servings, TotalCalories, Status, CreatedByUserId, IsNutritionist, CreatedAt, UpdatedAt, IsDeleted, ImageUrl) VALUES", recipes);
        writeChunks(out, "INSERT INTO RecipeIngredients (RecipeId, IngredientId, Quantity, Unit, Note, CreatedAt, UpdatedAt, IsDeleted) VALUES", recipeIngredients);
        writeChunks(out, "INSERT INTO RecipeSteps (RecipeId, StepNumber, Instruction, CreatedAt, UpdatedAt) VALUES", recipeSteps);
        
        out.println("SET FOREIGN_KEY_CHECKS = 1;\n");
        out.close();
        System.out.println("Generated SQL in large_recipe_seed.sql with " + generatedCount + " completely unique recipes!");
    }

    private static void writeChunks(PrintWriter out, String header, List<String> list) {
        int chunkSize = 50;
        for (int i = 0; i < list.size(); i += chunkSize) {
            out.println(header);
            int end = Math.min(i + chunkSize, list.size());
            for (int j = i; j < end; j++) {
                out.print(list.get(j));
                out.println(j == end - 1 ? ";" : ",");
            }
            out.println();
        }
    }
}
