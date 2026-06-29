package com.buildbygod.domain.model

/** Where a food sits in a day's meals and how the planner uses it. */
enum class FoodRole { PROTEIN, CARB, VEGFRUIT, FAT, DRINK }

/**
 * A pictorial emoji for a food so users can recognise items at a glance without reading the name.
 * Looks up a specific glyph per item id and falls back to a sensible per-category icon. Emojis are
 * rendered by the OS, so this adds zero asset weight and works fully offline.
 */
fun FoodItem.emoji(): String = foodEmojiById[id] ?: category.defaultEmoji

private val FoodCategory.defaultEmoji: String
    get() = when (this) {
        FoodCategory.GRAIN -> "🌾"
        FoodCategory.PROTEIN -> "🍗"
        FoodCategory.DAIRY -> "🥛"
        FoodCategory.LEGUME -> "🫘"
        FoodCategory.VEGETABLE -> "🥬"
        FoodCategory.FRUIT -> "🍎"
        FoodCategory.NUT_SEED -> "🥜"
        FoodCategory.FAT_OIL -> "🫒"
        FoodCategory.PREPARED -> "🍲"
        FoodCategory.CONDIMENT -> "🥫"
        FoodCategory.SUPPLEMENT -> "💊"
        FoodCategory.SNACK -> "🍿"
        FoodCategory.SWEET -> "🍰"
        FoodCategory.BEVERAGE -> "🥤"
        FoodCategory.ALCOHOL -> "🍷"
    }

private val foodEmojiById: Map<String, String> = mapOf(
    // Grains & cereals
    "oats" to "🥣", "rice_white" to "🍚", "rice_brown" to "🍚", "roti" to "🫓",
    "bread_brown" to "🍞", "quinoa" to "🍚", "poha" to "🍚", "upma" to "🥣",
    "pasta" to "🍝", "sweet_potato" to "🍠", "potato" to "🥔", "bread_white" to "🍞",
    "bread_multigrain" to "🍞", "cornflakes" to "🥣", "muesli" to "🥣", "granola" to "🥣",
    "dalia" to "🥣", "millet" to "🌾", "couscous" to "🍚", "corn" to "🌽",
    "paratha" to "🫓", "naan" to "🫓",
    // Meat, fish & eggs
    "egg_whole" to "🥚", "egg_white" to "🥚", "chicken_breast" to "🍗", "chicken_thigh" to "🍗",
    "fish_salmon" to "🐟", "fish_tilapia" to "🐟", "tuna" to "🐟", "prawns" to "🦐",
    "mutton" to "🍖", "turkey" to "🦃", "beef" to "🥩", "pork" to "🥓",
    "duck" to "🦆", "fish_mackerel" to "🐟", "sardine" to "🐟", "crab" to "🦀",
    "egg_boiled" to "🥚",
    // Dairy
    "milk" to "🥛", "curd" to "🥣", "greek_yogurt" to "🥣", "paneer" to "🧀",
    "cottage_cheese" to "🧀", "cheese" to "🧀", "mozzarella" to "🧀", "milk_full" to "🥛",
    "cream" to "🥛", "condensed_milk" to "🥛",
    // Lentils & beans
    "dal" to "🍲", "chickpeas" to "🫘", "rajma" to "🫘", "soya_chunks" to "🫛",
    "tofu" to "🧈", "moong" to "🌱", "black_beans" to "🫘", "lentils_green" to "🫘",
    "peas" to "🫛", "edamame" to "🫛", "tempeh" to "🫛", "chana" to "🫘",
    // Vegetables
    "broccoli" to "🥦", "spinach" to "🥬", "mixed_veg" to "🥗", "tomato" to "🍅",
    "carrot" to "🥕", "cucumber" to "🥒", "capsicum" to "🫑", "cauliflower" to "🥦",
    "beans_green" to "🫛", "mushroom" to "🍄", "salad" to "🥗", "cabbage" to "🥬",
    "kale" to "🥬", "lettuce" to "🥬", "zucchini" to "🥒", "eggplant" to "🍆",
    "okra" to "🥬", "beetroot" to "🥬", "pumpkin" to "🎃", "onion" to "🧅",
    "asparagus" to "🥬", "beans_sprouts" to "🌱",
    // Fruits
    "banana" to "🍌", "apple" to "🍎", "orange" to "🍊", "berries" to "🫐",
    "mango" to "🥭", "papaya" to "🍈", "grapes" to "🍇", "dates" to "🌴",
    "pear" to "🍐", "pineapple" to "🍍", "watermelon" to "🍉", "guava" to "🍈",
    "kiwi" to "🥝", "strawberry" to "🍓", "pomegranate" to "🍎", "fig" to "🫐",
    "plum" to "🍑", "cherry" to "🍒", "raisins" to "🍇",
    // Nuts & seeds
    "almonds" to "🌰", "walnuts" to "🌰", "peanuts" to "🥜", "peanut_butter" to "🥜",
    "chia" to "🌱", "flax" to "🌱", "pumpkin_seeds" to "🌱", "cashew" to "🌰",
    "pistachio" to "🌰", "hazelnut" to "🌰", "macadamia" to "🌰", "sunflower_seeds" to "🌻",
    "sesame" to "🌱", "coconut" to "🥥", "almond_butter" to "🥜",
    // Fats & oils
    "olive_oil" to "🫒", "ghee" to "🧈", "avocado" to "🥑", "butter" to "🧈",
    "coconut_oil" to "🥥", "mustard_oil" to "🛢️", "sunflower_oil" to "🌻",
    // Supplements
    "whey" to "🥤", "plant_protein" to "🥤", "mass_gainer" to "🥤", "creatine" to "💊",
    "casein" to "🥤", "bcaa" to "💊", "preworkout" to "⚡", "electrolyte" to "🧂",
    "fish_oil" to "💊", "multivitamin" to "💊",
    // Snacks
    "dark_choc" to "🍫", "makhana" to "🍿", "hummus" to "🥣", "protein_bar" to "🍫",
    "granola_bar" to "🍫", "popcorn" to "🍿", "chips" to "🍟", "biscuit" to "🍪",
    "trail_mix" to "🥜", "peanut_chikki" to "🍯",
    // Cooked dishes
    "idli" to "🍚", "dosa" to "🫓", "chapati_sabzi" to "🍛", "biryani_veg" to "🍛",
    "chicken_curry" to "🍛", "omelette" to "🍳", "sandwich_veg" to "🥪", "pizza" to "🍕",
    "burger" to "🍔", "salad_bowl" to "🥗",
    // Soups
    "soup_tomato" to "🍲", "soup_veg" to "🍲", "soup_chicken" to "🍲", "bone_broth" to "🍲",
    "dal_soup" to "🍲",
    // Condiments
    "ketchup" to "🥫", "mayo" to "🥫", "mustard_sauce" to "🥫", "soy_sauce" to "🍶",
    "honey" to "🍯", "maple_syrup" to "🍁", "jam" to "🍓", "pesto" to "🌿",
    // Sweets
    "ice_cream" to "🍨", "milk_choc" to "🍫", "gulab_jamun" to "🍩", "rasgulla" to "🍥",
    "kheer" to "🍮", "brownie" to "🍫", "donut" to "🍩", "cake" to "🍰",
    // Drinks (non-alcoholic)
    "water" to "💧", "sparkling_water" to "💧", "coffee_black" to "☕", "coffee_milk" to "☕",
    "tea_milk" to "🍵", "green_tea" to "🍵", "coconut_water" to "🥥", "buttermilk" to "🥛",
    "lassi" to "🥛", "orange_juice" to "🧃", "apple_juice" to "🧃", "pomegranate_juice" to "🧃",
    "sugarcane_juice" to "🧃", "lemonade" to "🍋", "smoothie" to "🥤", "milkshake" to "🥤",
    "protein_shake" to "🥤", "almond_milk" to "🥛", "soy_milk" to "🥛", "oat_milk" to "🥛",
    "cola" to "🥤", "diet_cola" to "🥤", "sports_drink" to "🥤", "energy_drink" to "⚡",
    "kombucha" to "🍵",
    // Alcohol
    "beer" to "🍺", "wine_red" to "🍷", "wine_white" to "🥂", "whiskey" to "🥃",
    "vodka" to "🥃", "cocktail" to "🍸"
)

enum class FoodCategory(val label: String) {
    GRAIN("Grains & cereals"),
    PROTEIN("Meat, fish & eggs"),
    DAIRY("Dairy"),
    LEGUME("Lentils & beans"),
    VEGETABLE("Vegetables"),
    FRUIT("Fruits"),
    NUT_SEED("Nuts & seeds"),
    FAT_OIL("Fats & oils"),
    PREPARED("Cooked dishes"),
    CONDIMENT("Sauces & condiments"),
    SUPPLEMENT("Supplements"),
    SNACK("Snacks"),
    SWEET("Sweets & desserts"),
    BEVERAGE("Drinks (non-alcoholic)"),
    ALCOHOL("Alcoholic drinks")
}

/**
 * One catalog food with macros per 100 g (kcal, protein, carbs, fat) plus a sensible serving size.
 * [veg] is true for vegetarian/vegan-friendly items (eggs and dairy count as vegetarian here).
 */
data class FoodItem(
    val id: String,
    val name: String,
    val category: FoodCategory,
    val role: FoodRole,
    val veg: Boolean,
    val kcal: Int,
    val protein: Double,
    val carbs: Double,
    val fat: Double,
    val servingG: Int,
    val servingLabel: String
)

/** Macros for a chosen amount of a food. */
data class FoodPortion(
    val food: FoodItem,
    val grams: Int
) {
    val kcal: Int get() = (food.kcal * grams / 100.0).toInt()
    val protein: Int get() = (food.protein * grams / 100.0).toInt()
    val carbs: Int get() = (food.carbs * grams / 100.0).toInt()
    val fat: Int get() = (food.fat * grams / 100.0).toInt()
}

/**
 * Offline food catalog with per-100g macros. Curated to cover common Indian/Western staples,
 * vegetables, fruits, nuts, oils and supplements so the planner can build varied meals.
 */
object FoodCatalog {

    val items: List<FoodItem> = buildList {
        // ---- Grains & cereals (carb) ----
        add(FoodItem("oats", "Rolled oats", FoodCategory.GRAIN, FoodRole.CARB, true, 389, 16.9, 66.3, 6.9, 40, "1/2 cup dry (40g)"))
        add(FoodItem("rice_white", "White rice (cooked)", FoodCategory.GRAIN, FoodRole.CARB, true, 130, 2.7, 28.0, 0.3, 150, "1 cup (150g)"))
        add(FoodItem("rice_brown", "Brown rice (cooked)", FoodCategory.GRAIN, FoodRole.CARB, true, 123, 2.7, 25.6, 1.0, 150, "1 cup (150g)"))
        add(FoodItem("roti", "Whole wheat roti", FoodCategory.GRAIN, FoodRole.CARB, true, 297, 11.0, 51.0, 7.0, 40, "1 roti (40g)"))
        add(FoodItem("bread_brown", "Brown bread", FoodCategory.GRAIN, FoodRole.CARB, true, 247, 13.0, 41.0, 3.4, 60, "2 slices (60g)"))
        add(FoodItem("quinoa", "Quinoa (cooked)", FoodCategory.GRAIN, FoodRole.CARB, true, 120, 4.4, 21.3, 1.9, 150, "1 cup (150g)"))
        add(FoodItem("poha", "Poha (flattened rice)", FoodCategory.GRAIN, FoodRole.CARB, true, 130, 2.6, 28.0, 0.4, 120, "1 bowl (120g)"))
        add(FoodItem("upma", "Semolina/sooji", FoodCategory.GRAIN, FoodRole.CARB, true, 360, 12.7, 72.8, 1.1, 50, "1/2 cup dry (50g)"))
        add(FoodItem("pasta", "Whole wheat pasta (cooked)", FoodCategory.GRAIN, FoodRole.CARB, true, 124, 5.3, 25.0, 0.9, 150, "1 cup (150g)"))
        add(FoodItem("sweet_potato", "Sweet potato (boiled)", FoodCategory.GRAIN, FoodRole.CARB, true, 90, 2.0, 20.7, 0.1, 150, "1 medium (150g)"))
        add(FoodItem("potato", "Potato (boiled)", FoodCategory.GRAIN, FoodRole.CARB, true, 87, 1.9, 20.1, 0.1, 150, "1 medium (150g)"))
        add(FoodItem("bread_white", "White bread", FoodCategory.GRAIN, FoodRole.CARB, true, 265, 9.0, 49.0, 3.2, 60, "2 slices (60g)"))
        add(FoodItem("bread_multigrain", "Multigrain bread", FoodCategory.GRAIN, FoodRole.CARB, true, 265, 13.0, 43.0, 4.2, 60, "2 slices (60g)"))
        add(FoodItem("cornflakes", "Corn flakes", FoodCategory.GRAIN, FoodRole.CARB, true, 357, 7.5, 84.0, 0.4, 40, "1 bowl (40g)"))
        add(FoodItem("muesli", "Muesli", FoodCategory.GRAIN, FoodRole.CARB, true, 363, 9.7, 66.0, 5.9, 50, "(50g)"))
        add(FoodItem("granola", "Granola", FoodCategory.GRAIN, FoodRole.CARB, true, 471, 10.0, 64.0, 20.0, 50, "(50g)"))
        add(FoodItem("dalia", "Broken wheat (dalia)", FoodCategory.GRAIN, FoodRole.CARB, true, 342, 12.0, 76.0, 1.5, 50, "(50g dry)"))
        add(FoodItem("millet", "Millet / ragi (cooked)", FoodCategory.GRAIN, FoodRole.CARB, true, 119, 3.5, 23.7, 1.0, 150, "1 cup (150g)"))
        add(FoodItem("couscous", "Couscous (cooked)", FoodCategory.GRAIN, FoodRole.CARB, true, 112, 3.8, 23.2, 0.2, 150, "1 cup (150g)"))
        add(FoodItem("corn", "Sweet corn", FoodCategory.GRAIN, FoodRole.CARB, true, 86, 3.3, 19.0, 1.4, 150, "1 cup (150g)"))
        add(FoodItem("paratha", "Plain paratha", FoodCategory.GRAIN, FoodRole.CARB, true, 326, 7.0, 44.0, 14.0, 60, "1 paratha (60g)"))
        add(FoodItem("naan", "Naan", FoodCategory.GRAIN, FoodRole.CARB, true, 310, 9.0, 53.0, 6.0, 90, "1 naan (90g)"))

        // ---- Meat, fish & eggs (protein, non-veg unless egg) ----
        add(FoodItem("egg_whole", "Whole egg", FoodCategory.PROTEIN, FoodRole.PROTEIN, true, 155, 13.0, 1.1, 11.0, 100, "2 eggs (100g)"))
        add(FoodItem("egg_white", "Egg whites", FoodCategory.PROTEIN, FoodRole.PROTEIN, true, 52, 11.0, 0.7, 0.2, 100, "3 whites (100g)"))
        add(FoodItem("chicken_breast", "Chicken breast (cooked)", FoodCategory.PROTEIN, FoodRole.PROTEIN, false, 165, 31.0, 0.0, 3.6, 120, "1 fillet (120g)"))
        add(FoodItem("chicken_thigh", "Chicken thigh (cooked)", FoodCategory.PROTEIN, FoodRole.PROTEIN, false, 209, 26.0, 0.0, 11.0, 120, "(120g)"))
        add(FoodItem("fish_salmon", "Salmon (cooked)", FoodCategory.PROTEIN, FoodRole.PROTEIN, false, 208, 20.0, 0.0, 13.0, 120, "1 fillet (120g)"))
        add(FoodItem("fish_tilapia", "Tilapia/white fish", FoodCategory.PROTEIN, FoodRole.PROTEIN, false, 128, 26.0, 0.0, 2.7, 120, "1 fillet (120g)"))
        add(FoodItem("tuna", "Tuna (canned, water)", FoodCategory.PROTEIN, FoodRole.PROTEIN, false, 116, 26.0, 0.0, 1.0, 100, "1 can (100g)"))
        add(FoodItem("prawns", "Prawns/shrimp (cooked)", FoodCategory.PROTEIN, FoodRole.PROTEIN, false, 99, 24.0, 0.2, 0.3, 100, "(100g)"))
        add(FoodItem("mutton", "Mutton (cooked)", FoodCategory.PROTEIN, FoodRole.PROTEIN, false, 258, 25.0, 0.0, 17.0, 120, "(120g)"))
        add(FoodItem("turkey", "Turkey breast", FoodCategory.PROTEIN, FoodRole.PROTEIN, false, 135, 30.0, 0.0, 1.0, 120, "(120g)"))
        add(FoodItem("beef", "Lean beef (cooked)", FoodCategory.PROTEIN, FoodRole.PROTEIN, false, 217, 26.0, 0.0, 12.0, 120, "(120g)"))
        add(FoodItem("pork", "Pork (cooked)", FoodCategory.PROTEIN, FoodRole.PROTEIN, false, 242, 27.0, 0.0, 14.0, 120, "(120g)"))
        add(FoodItem("duck", "Duck (cooked)", FoodCategory.PROTEIN, FoodRole.PROTEIN, false, 337, 19.0, 0.0, 28.0, 120, "(120g)"))
        add(FoodItem("fish_mackerel", "Mackerel (cooked)", FoodCategory.PROTEIN, FoodRole.PROTEIN, false, 262, 24.0, 0.0, 18.0, 120, "(120g)"))
        add(FoodItem("sardine", "Sardines (canned)", FoodCategory.PROTEIN, FoodRole.PROTEIN, false, 208, 25.0, 0.0, 11.0, 100, "(100g)"))
        add(FoodItem("crab", "Crab (cooked)", FoodCategory.PROTEIN, FoodRole.PROTEIN, false, 97, 19.0, 0.0, 1.5, 100, "(100g)"))
        add(FoodItem("egg_boiled", "Boiled egg", FoodCategory.PROTEIN, FoodRole.PROTEIN, true, 155, 13.0, 1.1, 11.0, 50, "1 egg (50g)"))

        // ---- Dairy (protein/fat) ----
        add(FoodItem("milk", "Milk (toned)", FoodCategory.DAIRY, FoodRole.PROTEIN, true, 56, 3.3, 5.0, 2.5, 200, "1 glass (200g)"))
        add(FoodItem("curd", "Curd / yogurt", FoodCategory.DAIRY, FoodRole.PROTEIN, true, 61, 3.5, 4.7, 3.3, 150, "1 bowl (150g)"))
        add(FoodItem("greek_yogurt", "Greek yogurt", FoodCategory.DAIRY, FoodRole.PROTEIN, true, 59, 10.0, 3.6, 0.4, 150, "1 cup (150g)"))
        add(FoodItem("paneer", "Paneer (cottage cheese)", FoodCategory.DAIRY, FoodRole.PROTEIN, true, 265, 18.0, 1.2, 21.0, 100, "(100g)"))
        add(FoodItem("cottage_cheese", "Low-fat cottage cheese", FoodCategory.DAIRY, FoodRole.PROTEIN, true, 98, 11.0, 3.4, 4.3, 100, "(100g)"))
        add(FoodItem("cheese", "Cheese (cheddar)", FoodCategory.DAIRY, FoodRole.FAT, true, 402, 25.0, 1.3, 33.0, 30, "1 slice (30g)"))
        add(FoodItem("mozzarella", "Mozzarella", FoodCategory.DAIRY, FoodRole.PROTEIN, true, 280, 28.0, 3.1, 17.0, 30, "(30g)"))
        add(FoodItem("milk_full", "Milk (full cream)", FoodCategory.DAIRY, FoodRole.PROTEIN, true, 66, 3.2, 4.8, 3.6, 200, "1 glass (200g)"))
        add(FoodItem("cream", "Fresh cream", FoodCategory.DAIRY, FoodRole.FAT, true, 345, 2.1, 3.4, 37.0, 30, "(30g)"))
        add(FoodItem("condensed_milk", "Condensed milk", FoodCategory.DAIRY, FoodRole.CARB, true, 321, 7.9, 54.0, 8.7, 30, "(30g)"))

        // ---- Lentils & beans (protein, veg) ----
        add(FoodItem("dal", "Dal (cooked lentils)", FoodCategory.LEGUME, FoodRole.PROTEIN, true, 116, 9.0, 20.0, 0.4, 150, "1 bowl (150g)"))
        add(FoodItem("chickpeas", "Chickpeas (boiled)", FoodCategory.LEGUME, FoodRole.PROTEIN, true, 164, 8.9, 27.4, 2.6, 120, "(120g)"))
        add(FoodItem("rajma", "Kidney beans (boiled)", FoodCategory.LEGUME, FoodRole.PROTEIN, true, 127, 8.7, 22.8, 0.5, 120, "(120g)"))
        add(FoodItem("soya_chunks", "Soya chunks", FoodCategory.LEGUME, FoodRole.PROTEIN, true, 345, 52.0, 33.0, 0.5, 50, "(50g dry)"))
        add(FoodItem("tofu", "Tofu (firm)", FoodCategory.LEGUME, FoodRole.PROTEIN, true, 144, 17.3, 2.8, 8.7, 120, "(120g)"))
        add(FoodItem("moong", "Sprouted moong", FoodCategory.LEGUME, FoodRole.PROTEIN, true, 30, 3.0, 5.9, 0.2, 100, "1 cup (100g)"))
        add(FoodItem("black_beans", "Black beans (boiled)", FoodCategory.LEGUME, FoodRole.PROTEIN, true, 132, 8.9, 23.7, 0.5, 120, "(120g)"))
        add(FoodItem("lentils_green", "Green/brown lentils (boiled)", FoodCategory.LEGUME, FoodRole.PROTEIN, true, 116, 9.0, 20.1, 0.4, 120, "(120g)"))
        add(FoodItem("peas", "Green peas", FoodCategory.LEGUME, FoodRole.PROTEIN, true, 81, 5.4, 14.5, 0.4, 100, "(100g)"))
        add(FoodItem("edamame", "Edamame", FoodCategory.LEGUME, FoodRole.PROTEIN, true, 121, 12.0, 9.0, 5.0, 100, "(100g)"))
        add(FoodItem("tempeh", "Tempeh", FoodCategory.LEGUME, FoodRole.PROTEIN, true, 192, 20.0, 7.6, 11.0, 100, "(100g)"))
        add(FoodItem("chana", "Black chana (boiled)", FoodCategory.LEGUME, FoodRole.PROTEIN, true, 164, 8.9, 27.4, 2.6, 120, "(120g)"))

        // ---- Vegetables (vegfruit, veg) ----
        add(FoodItem("broccoli", "Broccoli", FoodCategory.VEGETABLE, FoodRole.VEGFRUIT, true, 34, 2.8, 6.6, 0.4, 100, "1 cup (100g)"))
        add(FoodItem("spinach", "Spinach", FoodCategory.VEGETABLE, FoodRole.VEGFRUIT, true, 23, 2.9, 3.6, 0.4, 100, "(100g)"))
        add(FoodItem("mixed_veg", "Mixed vegetables", FoodCategory.VEGETABLE, FoodRole.VEGFRUIT, true, 65, 2.6, 13.0, 0.4, 150, "(150g)"))
        add(FoodItem("tomato", "Tomato", FoodCategory.VEGETABLE, FoodRole.VEGFRUIT, true, 18, 0.9, 3.9, 0.2, 100, "1 medium (100g)"))
        add(FoodItem("carrot", "Carrot", FoodCategory.VEGETABLE, FoodRole.VEGFRUIT, true, 41, 0.9, 9.6, 0.2, 100, "(100g)"))
        add(FoodItem("cucumber", "Cucumber", FoodCategory.VEGETABLE, FoodRole.VEGFRUIT, true, 15, 0.7, 3.6, 0.1, 100, "(100g)"))
        add(FoodItem("capsicum", "Bell pepper", FoodCategory.VEGETABLE, FoodRole.VEGFRUIT, true, 31, 1.0, 6.0, 0.3, 100, "(100g)"))
        add(FoodItem("cauliflower", "Cauliflower", FoodCategory.VEGETABLE, FoodRole.VEGFRUIT, true, 25, 1.9, 5.0, 0.3, 100, "(100g)"))
        add(FoodItem("beans_green", "Green beans", FoodCategory.VEGETABLE, FoodRole.VEGFRUIT, true, 31, 1.8, 7.0, 0.2, 100, "(100g)"))
        add(FoodItem("mushroom", "Mushrooms", FoodCategory.VEGETABLE, FoodRole.VEGFRUIT, true, 22, 3.1, 3.3, 0.3, 100, "(100g)"))
        add(FoodItem("salad", "Green salad (leafy)", FoodCategory.VEGETABLE, FoodRole.VEGFRUIT, true, 17, 1.4, 3.0, 0.2, 100, "1 bowl (100g)"))
        add(FoodItem("cabbage", "Cabbage", FoodCategory.VEGETABLE, FoodRole.VEGFRUIT, true, 25, 1.3, 5.8, 0.1, 100, "(100g)"))
        add(FoodItem("kale", "Kale", FoodCategory.VEGETABLE, FoodRole.VEGFRUIT, true, 49, 4.3, 8.8, 0.9, 100, "(100g)"))
        add(FoodItem("lettuce", "Lettuce", FoodCategory.VEGETABLE, FoodRole.VEGFRUIT, true, 15, 1.4, 2.9, 0.2, 100, "(100g)"))
        add(FoodItem("zucchini", "Zucchini", FoodCategory.VEGETABLE, FoodRole.VEGFRUIT, true, 17, 1.2, 3.1, 0.3, 100, "(100g)"))
        add(FoodItem("eggplant", "Eggplant (brinjal)", FoodCategory.VEGETABLE, FoodRole.VEGFRUIT, true, 25, 1.0, 5.9, 0.2, 100, "(100g)"))
        add(FoodItem("okra", "Okra (bhindi)", FoodCategory.VEGETABLE, FoodRole.VEGFRUIT, true, 33, 1.9, 7.5, 0.2, 100, "(100g)"))
        add(FoodItem("beetroot", "Beetroot", FoodCategory.VEGETABLE, FoodRole.VEGFRUIT, true, 43, 1.6, 9.6, 0.2, 100, "(100g)"))
        add(FoodItem("pumpkin", "Pumpkin", FoodCategory.VEGETABLE, FoodRole.VEGFRUIT, true, 26, 1.0, 6.5, 0.1, 100, "(100g)"))
        add(FoodItem("onion", "Onion", FoodCategory.VEGETABLE, FoodRole.VEGFRUIT, true, 40, 1.1, 9.3, 0.1, 100, "(100g)"))
        add(FoodItem("asparagus", "Asparagus", FoodCategory.VEGETABLE, FoodRole.VEGFRUIT, true, 20, 2.2, 3.9, 0.1, 100, "(100g)"))
        add(FoodItem("beans_sprouts", "Bean sprouts", FoodCategory.VEGETABLE, FoodRole.VEGFRUIT, true, 31, 3.0, 6.0, 0.2, 100, "(100g)"))

        // ---- Fruits (vegfruit, veg) ----
        add(FoodItem("banana", "Banana", FoodCategory.FRUIT, FoodRole.VEGFRUIT, true, 89, 1.1, 22.8, 0.3, 120, "1 medium (120g)"))
        add(FoodItem("apple", "Apple", FoodCategory.FRUIT, FoodRole.VEGFRUIT, true, 52, 0.3, 13.8, 0.2, 150, "1 medium (150g)"))
        add(FoodItem("orange", "Orange", FoodCategory.FRUIT, FoodRole.VEGFRUIT, true, 47, 0.9, 11.8, 0.1, 130, "1 medium (130g)"))
        add(FoodItem("berries", "Mixed berries", FoodCategory.FRUIT, FoodRole.VEGFRUIT, true, 57, 0.7, 14.0, 0.3, 100, "(100g)"))
        add(FoodItem("mango", "Mango", FoodCategory.FRUIT, FoodRole.VEGFRUIT, true, 60, 0.8, 15.0, 0.4, 150, "(150g)"))
        add(FoodItem("papaya", "Papaya", FoodCategory.FRUIT, FoodRole.VEGFRUIT, true, 43, 0.5, 11.0, 0.3, 150, "(150g)"))
        add(FoodItem("grapes", "Grapes", FoodCategory.FRUIT, FoodRole.VEGFRUIT, true, 69, 0.7, 18.0, 0.2, 100, "(100g)"))
        add(FoodItem("dates", "Dates", FoodCategory.FRUIT, FoodRole.VEGFRUIT, true, 277, 1.8, 75.0, 0.2, 30, "3 dates (30g)"))
        add(FoodItem("pear", "Pear", FoodCategory.FRUIT, FoodRole.VEGFRUIT, true, 57, 0.4, 15.2, 0.1, 150, "1 medium (150g)"))
        add(FoodItem("pineapple", "Pineapple", FoodCategory.FRUIT, FoodRole.VEGFRUIT, true, 50, 0.5, 13.1, 0.1, 150, "(150g)"))
        add(FoodItem("watermelon", "Watermelon", FoodCategory.FRUIT, FoodRole.VEGFRUIT, true, 30, 0.6, 7.6, 0.2, 200, "(200g)"))
        add(FoodItem("guava", "Guava", FoodCategory.FRUIT, FoodRole.VEGFRUIT, true, 68, 2.6, 14.3, 1.0, 100, "1 fruit (100g)"))
        add(FoodItem("kiwi", "Kiwi", FoodCategory.FRUIT, FoodRole.VEGFRUIT, true, 61, 1.1, 14.7, 0.5, 75, "1 fruit (75g)"))
        add(FoodItem("strawberry", "Strawberries", FoodCategory.FRUIT, FoodRole.VEGFRUIT, true, 32, 0.7, 7.7, 0.3, 100, "(100g)"))
        add(FoodItem("pomegranate", "Pomegranate", FoodCategory.FRUIT, FoodRole.VEGFRUIT, true, 83, 1.7, 18.7, 1.2, 100, "(100g)"))
        add(FoodItem("fig", "Figs", FoodCategory.FRUIT, FoodRole.VEGFRUIT, true, 74, 0.8, 19.2, 0.3, 50, "(50g)"))
        add(FoodItem("plum", "Plum", FoodCategory.FRUIT, FoodRole.VEGFRUIT, true, 46, 0.7, 11.4, 0.3, 70, "1 fruit (70g)"))
        add(FoodItem("cherry", "Cherries", FoodCategory.FRUIT, FoodRole.VEGFRUIT, true, 63, 1.1, 16.0, 0.2, 100, "(100g)"))
        add(FoodItem("raisins", "Raisins", FoodCategory.FRUIT, FoodRole.VEGFRUIT, true, 299, 3.1, 79.0, 0.5, 30, "(30g)"))

        // ---- Nuts & seeds (fat, veg) ----
        add(FoodItem("almonds", "Almonds", FoodCategory.NUT_SEED, FoodRole.FAT, true, 579, 21.0, 21.6, 49.9, 28, "handful (28g)"))
        add(FoodItem("walnuts", "Walnuts", FoodCategory.NUT_SEED, FoodRole.FAT, true, 654, 15.0, 14.0, 65.0, 28, "handful (28g)"))
        add(FoodItem("peanuts", "Peanuts", FoodCategory.NUT_SEED, FoodRole.FAT, true, 567, 25.8, 16.1, 49.2, 28, "handful (28g)"))
        add(FoodItem("peanut_butter", "Peanut butter", FoodCategory.NUT_SEED, FoodRole.FAT, true, 588, 25.0, 20.0, 50.0, 32, "2 tbsp (32g)"))
        add(FoodItem("chia", "Chia seeds", FoodCategory.NUT_SEED, FoodRole.FAT, true, 486, 16.5, 42.0, 30.7, 15, "1 tbsp (15g)"))
        add(FoodItem("flax", "Flax seeds", FoodCategory.NUT_SEED, FoodRole.FAT, true, 534, 18.3, 28.9, 42.2, 15, "1 tbsp (15g)"))
        add(FoodItem("pumpkin_seeds", "Pumpkin seeds", FoodCategory.NUT_SEED, FoodRole.FAT, true, 559, 30.0, 11.0, 49.0, 28, "(28g)"))
        add(FoodItem("cashew", "Cashews", FoodCategory.NUT_SEED, FoodRole.FAT, true, 553, 18.2, 30.2, 43.9, 28, "handful (28g)"))
        add(FoodItem("pistachio", "Pistachios", FoodCategory.NUT_SEED, FoodRole.FAT, true, 562, 20.2, 27.2, 45.3, 28, "handful (28g)"))
        add(FoodItem("hazelnut", "Hazelnuts", FoodCategory.NUT_SEED, FoodRole.FAT, true, 628, 15.0, 17.0, 61.0, 28, "(28g)"))
        add(FoodItem("macadamia", "Macadamia nuts", FoodCategory.NUT_SEED, FoodRole.FAT, true, 718, 7.9, 14.0, 76.0, 28, "(28g)"))
        add(FoodItem("sunflower_seeds", "Sunflower seeds", FoodCategory.NUT_SEED, FoodRole.FAT, true, 584, 20.8, 20.0, 51.5, 28, "(28g)"))
        add(FoodItem("sesame", "Sesame seeds", FoodCategory.NUT_SEED, FoodRole.FAT, true, 573, 17.7, 23.4, 49.7, 15, "1 tbsp (15g)"))
        add(FoodItem("coconut", "Coconut (fresh)", FoodCategory.NUT_SEED, FoodRole.FAT, true, 354, 3.3, 15.2, 33.5, 40, "(40g)"))
        add(FoodItem("almond_butter", "Almond butter", FoodCategory.NUT_SEED, FoodRole.FAT, true, 614, 21.0, 19.0, 56.0, 32, "2 tbsp (32g)"))

        // ---- Fats & oils (fat, veg) ----
        add(FoodItem("olive_oil", "Olive oil", FoodCategory.FAT_OIL, FoodRole.FAT, true, 884, 0.0, 0.0, 100.0, 14, "1 tbsp (14g)"))
        add(FoodItem("ghee", "Ghee", FoodCategory.FAT_OIL, FoodRole.FAT, true, 900, 0.0, 0.0, 100.0, 10, "1 tsp (10g)"))
        add(FoodItem("avocado", "Avocado", FoodCategory.FAT_OIL, FoodRole.FAT, true, 160, 2.0, 8.5, 14.7, 100, "1/2 fruit (100g)"))
        add(FoodItem("butter", "Butter", FoodCategory.FAT_OIL, FoodRole.FAT, true, 717, 0.9, 0.1, 81.0, 10, "1 tsp (10g)"))
        add(FoodItem("coconut_oil", "Coconut oil", FoodCategory.FAT_OIL, FoodRole.FAT, true, 892, 0.0, 0.0, 99.0, 14, "1 tbsp (14g)"))
        add(FoodItem("mustard_oil", "Mustard oil", FoodCategory.FAT_OIL, FoodRole.FAT, true, 884, 0.0, 0.0, 100.0, 14, "1 tbsp (14g)"))
        add(FoodItem("sunflower_oil", "Sunflower oil", FoodCategory.FAT_OIL, FoodRole.FAT, true, 884, 0.0, 0.0, 100.0, 14, "1 tbsp (14g)"))

        // ---- Supplements (protein, veg-friendly) ----
        add(FoodItem("whey", "Whey protein", FoodCategory.SUPPLEMENT, FoodRole.PROTEIN, true, 400, 80.0, 8.0, 6.0, 30, "1 scoop (30g)"))
        add(FoodItem("plant_protein", "Plant protein", FoodCategory.SUPPLEMENT, FoodRole.PROTEIN, true, 380, 75.0, 8.0, 5.0, 30, "1 scoop (30g)"))
        add(FoodItem("mass_gainer", "Mass gainer", FoodCategory.SUPPLEMENT, FoodRole.CARB, true, 380, 18.0, 70.0, 3.0, 60, "1 scoop (60g)"))
        add(FoodItem("creatine", "Creatine monohydrate", FoodCategory.SUPPLEMENT, FoodRole.PROTEIN, true, 0, 0.0, 0.0, 0.0, 5, "1 tsp (5g)"))
        add(FoodItem("casein", "Casein protein", FoodCategory.SUPPLEMENT, FoodRole.PROTEIN, true, 360, 78.0, 9.0, 2.0, 30, "1 scoop (30g)"))
        add(FoodItem("bcaa", "BCAA powder", FoodCategory.SUPPLEMENT, FoodRole.PROTEIN, true, 40, 9.0, 0.0, 0.0, 10, "1 scoop (10g)"))
        add(FoodItem("preworkout", "Pre-workout", FoodCategory.SUPPLEMENT, FoodRole.DRINK, true, 20, 0.0, 5.0, 0.0, 10, "1 scoop (10g)"))
        add(FoodItem("electrolyte", "Electrolyte mix", FoodCategory.SUPPLEMENT, FoodRole.DRINK, true, 40, 0.0, 10.0, 0.0, 10, "1 sachet (10g)"))
        add(FoodItem("fish_oil", "Fish oil capsule", FoodCategory.SUPPLEMENT, FoodRole.FAT, false, 900, 0.0, 0.0, 100.0, 2, "1-2 caps (2g)"))
        add(FoodItem("multivitamin", "Multivitamin", FoodCategory.SUPPLEMENT, FoodRole.PROTEIN, true, 0, 0.0, 0.0, 0.0, 1, "1 tablet"))

        // ---- Snacks / extras ----
        add(FoodItem("dark_choc", "Dark chocolate (85%)", FoodCategory.SNACK, FoodRole.FAT, true, 600, 7.8, 30.0, 50.0, 20, "2 squares (20g)"))
        add(FoodItem("makhana", "Roasted makhana", FoodCategory.SNACK, FoodRole.CARB, true, 347, 9.7, 76.9, 0.1, 30, "(30g)"))
        add(FoodItem("hummus", "Hummus", FoodCategory.SNACK, FoodRole.FAT, true, 166, 7.9, 14.3, 9.6, 60, "(60g)"))
        add(FoodItem("protein_bar", "Protein bar", FoodCategory.SNACK, FoodRole.PROTEIN, true, 350, 30.0, 35.0, 9.0, 60, "1 bar (60g)"))
        add(FoodItem("granola_bar", "Granola bar", FoodCategory.SNACK, FoodRole.CARB, true, 471, 8.0, 64.0, 20.0, 40, "1 bar (40g)"))
        add(FoodItem("popcorn", "Popcorn (plain)", FoodCategory.SNACK, FoodRole.CARB, true, 387, 12.0, 78.0, 4.5, 25, "(25g)"))
        add(FoodItem("chips", "Potato chips", FoodCategory.SNACK, FoodRole.FAT, true, 536, 7.0, 53.0, 35.0, 30, "(30g)"))
        add(FoodItem("biscuit", "Digestive biscuit", FoodCategory.SNACK, FoodRole.CARB, true, 471, 7.0, 62.0, 21.0, 30, "(30g)"))
        add(FoodItem("trail_mix", "Trail mix", FoodCategory.SNACK, FoodRole.FAT, true, 462, 13.8, 44.9, 29.4, 40, "(40g)"))
        add(FoodItem("peanut_chikki", "Peanut chikki", FoodCategory.SNACK, FoodRole.CARB, true, 460, 14.0, 55.0, 21.0, 30, "(30g)"))

        // ---- Cooked dishes / prepared meals ----
        add(FoodItem("idli", "Idli", FoodCategory.PREPARED, FoodRole.CARB, true, 132, 4.0, 28.0, 0.4, 100, "2 idlis (100g)"))
        add(FoodItem("dosa", "Plain dosa", FoodCategory.PREPARED, FoodRole.CARB, true, 168, 3.9, 28.0, 4.5, 100, "1 dosa (100g)"))
        add(FoodItem("chapati_sabzi", "Veg curry (sabzi)", FoodCategory.PREPARED, FoodRole.VEGFRUIT, true, 110, 3.0, 12.0, 6.0, 150, "1 bowl (150g)"))
        add(FoodItem("biryani_veg", "Veg biryani", FoodCategory.PREPARED, FoodRole.CARB, true, 180, 4.5, 28.0, 5.5, 200, "1 plate (200g)"))
        add(FoodItem("chicken_curry", "Chicken curry", FoodCategory.PREPARED, FoodRole.PROTEIN, false, 180, 15.0, 5.0, 11.0, 200, "1 bowl (200g)"))
        add(FoodItem("omelette", "Omelette (2 eggs)", FoodCategory.PREPARED, FoodRole.PROTEIN, true, 180, 12.0, 1.5, 14.0, 120, "(120g)"))
        add(FoodItem("sandwich_veg", "Veg sandwich", FoodCategory.PREPARED, FoodRole.CARB, true, 230, 8.0, 30.0, 9.0, 150, "1 sandwich (150g)"))
        add(FoodItem("pizza", "Pizza (cheese)", FoodCategory.PREPARED, FoodRole.CARB, true, 266, 11.0, 33.0, 10.0, 120, "1 slice (120g)"))
        add(FoodItem("burger", "Veg burger", FoodCategory.PREPARED, FoodRole.CARB, true, 250, 9.0, 30.0, 11.0, 150, "1 burger (150g)"))
        add(FoodItem("salad_bowl", "Chicken salad bowl", FoodCategory.PREPARED, FoodRole.PROTEIN, false, 120, 12.0, 6.0, 5.0, 250, "1 bowl (250g)"))

        // ---- Soups & broths (liquid) ----
        add(FoodItem("soup_tomato", "Tomato soup", FoodCategory.PREPARED, FoodRole.DRINK, true, 38, 1.0, 7.0, 0.9, 240, "1 bowl (240ml)"))
        add(FoodItem("soup_veg", "Vegetable soup", FoodCategory.PREPARED, FoodRole.DRINK, true, 35, 1.5, 6.0, 0.8, 240, "1 bowl (240ml)"))
        add(FoodItem("soup_chicken", "Chicken soup", FoodCategory.PREPARED, FoodRole.DRINK, false, 45, 4.0, 4.0, 1.5, 240, "1 bowl (240ml)"))
        add(FoodItem("bone_broth", "Bone broth", FoodCategory.PREPARED, FoodRole.DRINK, false, 38, 6.0, 1.0, 1.0, 240, "1 cup (240ml)"))
        add(FoodItem("dal_soup", "Lentil soup", FoodCategory.PREPARED, FoodRole.DRINK, true, 60, 3.5, 9.0, 1.0, 240, "1 bowl (240ml)"))

        // ---- Sauces & condiments ----
        add(FoodItem("ketchup", "Tomato ketchup", FoodCategory.CONDIMENT, FoodRole.CARB, true, 101, 1.0, 25.0, 0.1, 15, "1 tbsp (15g)"))
        add(FoodItem("mayo", "Mayonnaise", FoodCategory.CONDIMENT, FoodRole.FAT, true, 680, 1.0, 1.0, 75.0, 15, "1 tbsp (15g)"))
        add(FoodItem("mustard_sauce", "Mustard", FoodCategory.CONDIMENT, FoodRole.VEGFRUIT, true, 66, 4.4, 5.0, 4.0, 10, "1 tsp (10g)"))
        add(FoodItem("soy_sauce", "Soy sauce", FoodCategory.CONDIMENT, FoodRole.VEGFRUIT, true, 53, 8.1, 4.9, 0.6, 15, "1 tbsp (15ml)"))
        add(FoodItem("honey", "Honey", FoodCategory.CONDIMENT, FoodRole.CARB, true, 304, 0.3, 82.0, 0.0, 21, "1 tbsp (21g)"))
        add(FoodItem("maple_syrup", "Maple syrup", FoodCategory.CONDIMENT, FoodRole.CARB, true, 260, 0.0, 67.0, 0.1, 20, "1 tbsp (20g)"))
        add(FoodItem("jam", "Fruit jam", FoodCategory.CONDIMENT, FoodRole.CARB, true, 278, 0.4, 69.0, 0.1, 20, "1 tbsp (20g)"))
        add(FoodItem("pesto", "Pesto", FoodCategory.CONDIMENT, FoodRole.FAT, true, 450, 5.0, 6.0, 45.0, 15, "1 tbsp (15g)"))

        // ---- Sweets & desserts ----
        add(FoodItem("ice_cream", "Ice cream (vanilla)", FoodCategory.SWEET, FoodRole.FAT, true, 207, 3.5, 24.0, 11.0, 100, "1 scoop (100g)"))
        add(FoodItem("milk_choc", "Milk chocolate", FoodCategory.SWEET, FoodRole.FAT, true, 535, 7.7, 59.0, 30.0, 25, "(25g)"))
        add(FoodItem("gulab_jamun", "Gulab jamun", FoodCategory.SWEET, FoodRole.CARB, true, 330, 4.0, 45.0, 15.0, 50, "1 piece (50g)"))
        add(FoodItem("rasgulla", "Rasgulla", FoodCategory.SWEET, FoodRole.CARB, true, 186, 4.0, 38.0, 1.0, 60, "1 piece (60g)"))
        add(FoodItem("kheer", "Kheer / rice pudding", FoodCategory.SWEET, FoodRole.CARB, true, 140, 4.0, 22.0, 4.0, 150, "1 bowl (150g)"))
        add(FoodItem("brownie", "Chocolate brownie", FoodCategory.SWEET, FoodRole.FAT, true, 466, 6.0, 50.0, 28.0, 60, "1 piece (60g)"))
        add(FoodItem("donut", "Donut", FoodCategory.SWEET, FoodRole.CARB, true, 452, 4.9, 51.0, 25.0, 60, "1 donut (60g)"))
        add(FoodItem("cake", "Sponge cake", FoodCategory.SWEET, FoodRole.CARB, true, 297, 5.0, 50.0, 9.0, 80, "1 slice (80g)"))

        // ---- Drinks: water & non-alcoholic (liquids) ----
        add(FoodItem("water", "Water", FoodCategory.BEVERAGE, FoodRole.DRINK, true, 0, 0.0, 0.0, 0.0, 250, "1 glass (250ml)"))
        add(FoodItem("sparkling_water", "Sparkling water", FoodCategory.BEVERAGE, FoodRole.DRINK, true, 0, 0.0, 0.0, 0.0, 250, "1 glass (250ml)"))
        add(FoodItem("coffee_black", "Black coffee", FoodCategory.BEVERAGE, FoodRole.DRINK, true, 2, 0.3, 0.0, 0.0, 240, "1 cup (240ml)"))
        add(FoodItem("coffee_milk", "Coffee with milk", FoodCategory.BEVERAGE, FoodRole.DRINK, true, 42, 2.0, 4.0, 1.8, 240, "1 cup (240ml)"))
        add(FoodItem("tea_milk", "Tea with milk", FoodCategory.BEVERAGE, FoodRole.DRINK, true, 40, 1.5, 5.0, 1.5, 150, "1 cup (150ml)"))
        add(FoodItem("green_tea", "Green tea", FoodCategory.BEVERAGE, FoodRole.DRINK, true, 1, 0.0, 0.2, 0.0, 240, "1 cup (240ml)"))
        add(FoodItem("coconut_water", "Coconut water", FoodCategory.BEVERAGE, FoodRole.DRINK, true, 19, 0.7, 3.7, 0.2, 240, "1 glass (240ml)"))
        add(FoodItem("buttermilk", "Buttermilk", FoodCategory.BEVERAGE, FoodRole.DRINK, true, 40, 3.3, 4.8, 0.9, 200, "1 glass (200ml)"))
        add(FoodItem("lassi", "Sweet lassi", FoodCategory.BEVERAGE, FoodRole.DRINK, true, 98, 3.0, 16.0, 2.5, 250, "1 glass (250ml)"))
        add(FoodItem("orange_juice", "Orange juice", FoodCategory.BEVERAGE, FoodRole.DRINK, true, 45, 0.7, 10.4, 0.2, 250, "1 glass (250ml)"))
        add(FoodItem("apple_juice", "Apple juice", FoodCategory.BEVERAGE, FoodRole.DRINK, true, 46, 0.1, 11.3, 0.1, 250, "1 glass (250ml)"))
        add(FoodItem("pomegranate_juice", "Pomegranate juice", FoodCategory.BEVERAGE, FoodRole.DRINK, true, 54, 0.4, 13.0, 0.3, 250, "1 glass (250ml)"))
        add(FoodItem("sugarcane_juice", "Sugarcane juice", FoodCategory.BEVERAGE, FoodRole.DRINK, true, 73, 0.2, 18.0, 0.1, 250, "1 glass (250ml)"))
        add(FoodItem("lemonade", "Lemonade (nimbu pani)", FoodCategory.BEVERAGE, FoodRole.DRINK, true, 40, 0.1, 10.0, 0.0, 250, "1 glass (250ml)"))
        add(FoodItem("smoothie", "Fruit smoothie", FoodCategory.BEVERAGE, FoodRole.DRINK, true, 80, 2.0, 16.0, 1.0, 250, "1 glass (250ml)"))
        add(FoodItem("milkshake", "Milkshake", FoodCategory.BEVERAGE, FoodRole.DRINK, true, 112, 3.5, 18.0, 3.0, 250, "1 glass (250ml)"))
        add(FoodItem("protein_shake", "Protein shake (with milk)", FoodCategory.BEVERAGE, FoodRole.PROTEIN, true, 90, 9.0, 7.0, 2.5, 300, "1 shake (300ml)"))
        add(FoodItem("almond_milk", "Almond milk (unsweetened)", FoodCategory.BEVERAGE, FoodRole.DRINK, true, 15, 0.6, 0.6, 1.1, 240, "1 glass (240ml)"))
        add(FoodItem("soy_milk", "Soy milk", FoodCategory.BEVERAGE, FoodRole.PROTEIN, true, 43, 3.3, 2.9, 1.8, 240, "1 glass (240ml)"))
        add(FoodItem("oat_milk", "Oat milk", FoodCategory.BEVERAGE, FoodRole.DRINK, true, 46, 1.0, 7.0, 1.5, 240, "1 glass (240ml)"))
        add(FoodItem("cola", "Cola (soft drink)", FoodCategory.BEVERAGE, FoodRole.DRINK, true, 42, 0.0, 10.6, 0.0, 330, "1 can (330ml)"))
        add(FoodItem("diet_cola", "Diet cola", FoodCategory.BEVERAGE, FoodRole.DRINK, true, 1, 0.0, 0.1, 0.0, 330, "1 can (330ml)"))
        add(FoodItem("sports_drink", "Sports drink", FoodCategory.BEVERAGE, FoodRole.DRINK, true, 26, 0.0, 6.0, 0.0, 350, "1 bottle (350ml)"))
        add(FoodItem("energy_drink", "Energy drink", FoodCategory.BEVERAGE, FoodRole.DRINK, true, 45, 0.0, 11.0, 0.0, 250, "1 can (250ml)"))
        add(FoodItem("kombucha", "Kombucha", FoodCategory.BEVERAGE, FoodRole.DRINK, true, 30, 0.0, 7.0, 0.0, 240, "1 glass (240ml)"))

        // ---- Alcoholic drinks ----
        add(FoodItem("beer", "Beer", FoodCategory.ALCOHOL, FoodRole.DRINK, true, 43, 0.5, 3.6, 0.0, 330, "1 can (330ml)"))
        add(FoodItem("wine_red", "Red wine", FoodCategory.ALCOHOL, FoodRole.DRINK, true, 85, 0.1, 2.6, 0.0, 150, "1 glass (150ml)"))
        add(FoodItem("wine_white", "White wine", FoodCategory.ALCOHOL, FoodRole.DRINK, true, 82, 0.1, 2.6, 0.0, 150, "1 glass (150ml)"))
        add(FoodItem("whiskey", "Whiskey / spirits", FoodCategory.ALCOHOL, FoodRole.DRINK, true, 250, 0.0, 0.0, 0.0, 30, "1 peg (30ml)"))
        add(FoodItem("vodka", "Vodka", FoodCategory.ALCOHOL, FoodRole.DRINK, true, 231, 0.0, 0.0, 0.0, 30, "1 shot (30ml)"))
        add(FoodItem("cocktail", "Cocktail (mixed)", FoodCategory.ALCOHOL, FoodRole.DRINK, true, 90, 0.0, 12.0, 0.0, 200, "1 glass (200ml)"))
    }

    private val byId = items.associateBy { it.id }
    fun byId(id: String): FoodItem? = byId[id]

    fun search(query: String, vegOnly: Boolean): List<FoodItem> {
        val q = query.trim().lowercase()
        return items.filter { (!vegOnly || it.veg) && (q.isBlank() || it.name.lowercase().contains(q)) }
            .sortedBy { it.name }
    }
}
