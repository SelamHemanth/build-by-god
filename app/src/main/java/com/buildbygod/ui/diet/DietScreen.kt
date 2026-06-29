package com.buildbygod.ui.diet

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.buildbygod.domain.model.DietPlan
import com.buildbygod.domain.model.FoodCategory
import com.buildbygod.domain.model.FoodItem
import com.buildbygod.domain.model.Meal
import com.buildbygod.domain.model.emoji
import com.buildbygod.ui.components.ControlsRow
import com.buildbygod.ui.components.FilterChip
import com.buildbygod.ui.components.FoodIcon
import com.buildbygod.ui.components.GlassTopBar
import com.buildbygod.ui.components.SortChip
import com.buildbygod.ui.theme.AccentAmber
import com.buildbygod.ui.theme.AccentBlue
import com.buildbygod.ui.theme.AccentGreen
import com.buildbygod.ui.theme.AccentPink
import com.buildbygod.ui.theme.AccentViolet
import com.buildbygod.ui.theme.GlassCard
import com.buildbygod.ui.theme.Surface2
import com.buildbygod.ui.theme.TextPrimary
import com.buildbygod.ui.theme.TextSecondary

private enum class DietTab(val label: String) { SUGGESTED("Suggested plan"), CUSTOM("My ingredients") }

/** A recognisable accent tint per food group so the emoji tiles read as a colour-coded library. */
private fun foodAccent(category: FoodCategory): Color = when (category) {
    FoodCategory.PROTEIN, FoodCategory.DAIRY, FoodCategory.LEGUME, FoodCategory.SUPPLEMENT -> AccentPink
    FoodCategory.GRAIN, FoodCategory.PREPARED, FoodCategory.SNACK -> AccentAmber
    FoodCategory.VEGETABLE, FoodCategory.FRUIT -> AccentGreen
    FoodCategory.NUT_SEED, FoodCategory.FAT_OIL, FoodCategory.CONDIMENT -> AccentViolet
    FoodCategory.SWEET, FoodCategory.BEVERAGE, FoodCategory.ALCOHOL -> AccentBlue
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DietScreen(
    onBack: () -> Unit,
    vm: DietViewModel = hiltViewModel()
) {
    val state by vm.state.collectAsStateWithLifecycle()
    var tab by remember { mutableStateOf(DietTab.SUGGESTED) }
    var showLogSheet by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxSize()) {
        GlassTopBar(title = "Diet plan", onBack = onBack)

        LazyColumn(
            Modifier.fillMaxSize().padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item { TargetCard(state) }

            item { EatenTodayCard(state, onAdd = { showLogSheet = true }, onRemove = vm::removeLogged) }

            item {
                val followed = state.followedToday
                Box(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(if (followed) AccentGreen.copy(alpha = 0.18f) else Surface2.copy(alpha = 0.5f))
                        .clickable { vm.toggleFollowedToday() }
                        .padding(vertical = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(
                            if (followed) Icons.Filled.Check else Icons.Filled.Add,
                            contentDescription = null,
                            tint = if (followed) AccentGreen else AccentBlue
                        )
                        Text(
                            if (followed) "Diet on track today" else "Mark today's diet on track",
                            color = if (followed) AccentGreen else TextPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    DietToggle("Veg", state.vegOnly, Modifier.weight(1f)) { vm.setVegOnly(true) }
                    DietToggle("Non-veg", !state.vegOnly, Modifier.weight(1f)) { vm.setVegOnly(false) }
                }
            }

            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    DietTab.entries.forEach { t ->
                        SegTab(t.label, tab == t, Modifier.weight(1f)) { tab = t }
                    }
                }
            }

            when (tab) {
                DietTab.SUGGESTED -> suggestedContent(state.suggested)
                DietTab.CUSTOM -> customContent(state, vm)
            }

            item { Spacer(Modifier.height(90.dp)) }
        }
    }

    if (showLogSheet) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { showLogSheet = false },
            sheetState = sheetState,
            containerColor = Surface2
        ) {
            LogFoodSheet(state, vm)
        }
    }
}

@Composable
private fun EatenTodayCard(
    state: DietUiState,
    onAdd: () -> Unit,
    onRemove: (Long) -> Unit
) {
    val t = state.targets
    val c = state.consumed
    GlassCard(Modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column {
                Text("Eaten today", style = MaterialTheme.typography.titleMedium, color = TextPrimary, fontWeight = FontWeight.Bold)
                Text("${c.kcal} of ${t.kcal} kcal", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
            }
            Row(
                Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(AccentBlue.copy(alpha = 0.18f))
                    .clickable { onAdd() }
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(Icons.Filled.Add, contentDescription = null, tint = AccentBlue, modifier = Modifier.size(18.dp))
                Text("Log food", color = AccentBlue, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.labelLarge)
            }
        }

        Spacer(Modifier.height(12.dp))
        IntakeBar("Calories", c.kcal, t.kcal, "kcal", AccentBlue)
        Spacer(Modifier.height(8.dp))
        IntakeBar("Protein", c.protein, t.protein, "g", AccentPink)
        Spacer(Modifier.height(8.dp))
        IntakeBar("Carbs", c.carbs, t.carbs, "g", AccentAmber)
        Spacer(Modifier.height(8.dp))
        IntakeBar("Fat", c.fat, t.fat, "g", AccentViolet)

        if (state.eaten.isEmpty()) {
            Spacer(Modifier.height(10.dp))
            Text(
                "Nothing logged yet. Tap \"Log food\" to add what you ate or the supplements you took.",
                style = MaterialTheme.typography.labelMedium, color = TextSecondary
            )
        } else {
            Spacer(Modifier.height(12.dp))
            state.eaten.forEach { item ->
                Row(
                    Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FoodIcon(emoji = item.food.emoji(), accent = foodAccent(item.food.category), size = 32.dp, cornerRadius = 10.dp)
                    Column(Modifier.weight(1f).padding(start = 10.dp)) {
                        Text(item.food.name, style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
                        Text(
                            "${item.portion.grams} g · ${item.portion.kcal} kcal · ${item.portion.protein}g P",
                            style = MaterialTheme.typography.labelSmall, color = TextSecondary
                        )
                    }
                    Box(
                        Modifier
                            .size(26.dp)
                            .clip(CircleShape)
                            .background(AccentPink.copy(alpha = 0.15f))
                            .clickable { onRemove(item.id) },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Filled.Close, contentDescription = "Remove", tint = AccentPink, modifier = Modifier.size(15.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun IntakeBar(label: String, value: Int, target: Int, unit: String, color: Color) {
    val frac = if (target <= 0) 0f else (value.toFloat() / target).coerceIn(0f, 1f)
    Column(Modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, style = MaterialTheme.typography.labelMedium, color = TextSecondary)
            Text("$value / $target $unit", style = MaterialTheme.typography.labelMedium, color = color, fontWeight = FontWeight.SemiBold)
        }
        Spacer(Modifier.height(4.dp))
        Box(
            Modifier
                .fillMaxWidth()
                .height(7.dp)
                .clip(CircleShape)
                .background(Surface2.copy(alpha = 0.7f))
        ) {
            Box(
                Modifier
                    .fillMaxWidth(frac)
                    .height(7.dp)
                    .clip(CircleShape)
                    .background(color)
            )
        }
    }
}

@Composable
private fun LogFoodSheet(state: DietUiState, vm: DietViewModel) {
    var q by remember { mutableStateOf("") }
    val portions = listOf(0.5f to "½", 1f to "1", 1.5f to "1½", 2f to "2")
    var mult by remember { mutableStateOf(1f) }

    Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp).padding(bottom = 24.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(Icons.Filled.Restaurant, contentDescription = null, tint = AccentBlue)
            Text("Log what you ate", style = MaterialTheme.typography.titleMedium, color = TextPrimary, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(4.dp))
        Text(
            "Foods and supplements count toward today's totals.",
            style = MaterialTheme.typography.labelMedium, color = TextSecondary
        )
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = q,
            onValueChange = { q = it; vm.setQuery(it) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Search foods & supplements") },
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null, tint = TextSecondary) }
        )
        Spacer(Modifier.height(10.dp))
        FoodControls(state, vm)
        Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("Portion", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
            portions.forEach { (m, lbl) ->
                val sel = m == mult
                Box(
                    Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (sel) AccentBlue.copy(alpha = 0.25f) else Surface2.copy(alpha = 0.6f))
                        .clickable { mult = m }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(lbl, color = if (sel) AccentBlue else TextSecondary, fontWeight = FontWeight.SemiBold)
                }
            }
        }
        Spacer(Modifier.height(10.dp))
        LazyColumn(Modifier.heightIn(max = 360.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            items(state.searchResults, key = { it.id }) { food ->
                val grams = (food.servingG * mult).toInt().coerceAtLeast(1)
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Surface2.copy(alpha = 0.5f))
                        .clickable { vm.logFood(food.id, grams) }
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FoodIcon(emoji = food.emoji(), accent = foodAccent(food.category), size = 38.dp)
                    Column(Modifier.weight(1f).padding(start = 12.dp)) {
                        Text(food.name, color = TextPrimary, style = MaterialTheme.typography.titleSmall)
                        Text(
                            "$grams g · ${(food.kcal * grams / 100)} kcal · ${food.category.label}",
                            color = TextSecondary, style = MaterialTheme.typography.labelSmall
                        )
                    }
                    Icon(Icons.Filled.Add, contentDescription = "Add", tint = AccentGreen)
                }
            }
        }
    }
}

private fun androidx.compose.foundation.lazy.LazyListScope.suggestedContent(plan: DietPlan) {
    if (plan.isEmpty) {
        item { EmptyHint("No plan yet. Adjust your preference above.") }
        return
    }
    item {
        Text("Your day, morning to night", style = MaterialTheme.typography.titleMedium, color = TextPrimary, fontWeight = FontWeight.SemiBold)
    }
    items(plan.meals.filter { it.portions.isNotEmpty() }, key = { it.slot.name }) { meal ->
        MealCard(meal)
    }
    item { PlanTotals(plan) }
}

private fun androidx.compose.foundation.lazy.LazyListScope.customContent(state: DietUiState, vm: DietViewModel) {
    item {
        GlassCard(Modifier.fillMaxWidth()) {
            Text("Build from what you have", style = MaterialTheme.typography.titleMedium, color = TextPrimary, fontWeight = FontWeight.SemiBold)
            Text(
                "Search and tap the ingredients you have at home. We'll plan meals using only those.",
                style = MaterialTheme.typography.bodyMedium, color = TextSecondary
            )
        }
    }
    item { SearchAndPick(state, vm) }
    if (state.pantry.isNotEmpty()) {
        item {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Your plan", style = MaterialTheme.typography.titleMedium, color = TextPrimary, fontWeight = FontWeight.SemiBold)
                Text("Clear", style = MaterialTheme.typography.labelMedium, color = AccentPink, modifier = Modifier.clickable { vm.clearPantry() })
            }
        }
        if (state.custom.isEmpty) {
            item { EmptyHint("Add a protein and a carb source to generate a balanced plan.") }
        } else {
            items(state.custom.meals.filter { it.portions.isNotEmpty() }, key = { "c_${it.slot.name}" }) { meal ->
                MealCard(meal)
            }
            item { PlanTotals(state.custom) }
        }
    } else {
        item { EmptyHint("Pick a few ingredients above to get started.") }
    }
}

@Composable
private fun SearchAndPick(state: DietUiState, vm: DietViewModel) {
    var q by remember { mutableStateOf("") }
    GlassCard(Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = q,
            onValueChange = { q = it; vm.setQuery(it) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Search foods") },
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null, tint = TextSecondary) }
        )
        Spacer(Modifier.height(8.dp))
        FoodControls(state, vm)
        Spacer(Modifier.height(8.dp))
        LazyColumn(Modifier.heightIn(max = 260.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            items(state.searchResults, key = { it.id }) { food ->
                val picked = food.id in state.pantry
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (picked) AccentGreen.copy(alpha = 0.15f) else Surface2.copy(alpha = 0.5f))
                        .clickable { vm.togglePantry(food.id) }
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FoodIcon(emoji = food.emoji(), accent = foodAccent(food.category), size = 38.dp)
                    Column(Modifier.weight(1f).padding(start = 12.dp)) {
                        Text(food.name, color = TextPrimary, style = MaterialTheme.typography.titleSmall)
                        Text(
                            "${food.kcal} kcal · ${food.protein.toInt()}g P / 100g · ${if (food.veg) "Veg" else "Non-veg"}",
                            color = TextSecondary, style = MaterialTheme.typography.labelSmall
                        )
                    }
                    Icon(
                        if (picked) Icons.Filled.Check else Icons.Filled.Add,
                        contentDescription = null,
                        tint = if (picked) AccentGreen else AccentBlue
                    )
                }
            }
        }
    }
}

@Composable
private fun FoodControls(state: DietUiState, vm: DietViewModel) {
    ControlsRow {
        SortChip(
            options = FoodSort.entries,
            selected = state.foodSort,
            label = { it.label },
            onSelect = vm::setFoodSort
        )
        FoodCategory.entries.forEach { cat ->
            FilterChip(
                label = cat.label,
                selected = state.foodCategory == cat,
                onClick = { vm.setFoodCategory(cat) }
            )
        }
    }
}

@Composable
private fun TargetCard(state: DietUiState) {
    val t = state.targets
    GlassCard(Modifier.fillMaxWidth()) {
        Text(
            if (t.complete) "Daily target" else "Estimated target",
            style = MaterialTheme.typography.labelMedium, color = AccentBlue, fontWeight = FontWeight.Bold
        )
        Text("${t.kcal} kcal", style = MaterialTheme.typography.headlineMedium, color = TextPrimary, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(10.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            MacroPill("Protein", "${t.protein}g", AccentPink, Modifier.weight(1f))
            MacroPill("Carbs", "${t.carbs}g", AccentAmber, Modifier.weight(1f))
            MacroPill("Fat", "${t.fat}g", AccentViolet, Modifier.weight(1f))
        }
        if (!t.complete) {
            Spacer(Modifier.height(8.dp))
            Text(
                "Complete your body details in Manage profile for personalised targets.",
                style = MaterialTheme.typography.labelMedium, color = TextSecondary
            )
        }
    }
}

@Composable
private fun MacroPill(label: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Column(
        modifier
            .clip(RoundedCornerShape(14.dp))
            .background(color.copy(alpha = 0.14f))
            .padding(vertical = 10.dp, horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(value, style = MaterialTheme.typography.titleMedium, color = color, fontWeight = FontWeight.Bold)
        Text(label, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
    }
}

@Composable
private fun MealCard(meal: Meal) {
    GlassCard(Modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(meal.slot.label, style = MaterialTheme.typography.titleMedium, color = TextPrimary, fontWeight = FontWeight.Bold)
            Text("${meal.kcal} kcal", style = MaterialTheme.typography.labelLarge, color = AccentBlue, fontWeight = FontWeight.SemiBold)
        }
        Spacer(Modifier.height(8.dp))
        meal.portions.forEach { p ->
            Row(
                Modifier.fillMaxWidth().padding(vertical = 3.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                FoodIcon(emoji = p.food.emoji(), accent = foodAccent(p.food.category), size = 30.dp, cornerRadius = 9.dp)
                Column(Modifier.weight(1f).padding(start = 10.dp)) {
                    Text(p.food.name, style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
                    Text("${p.grams} g", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                }
                Text(
                    "${p.protein}P  ${p.carbs}C  ${p.fat}F",
                    style = MaterialTheme.typography.labelSmall, color = TextSecondary
                )
            }
        }
    }
}

@Composable
private fun PlanTotals(plan: DietPlan) {
    GlassCard(Modifier.fillMaxWidth()) {
        Text("Day total", style = MaterialTheme.typography.labelMedium, color = AccentGreen, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(4.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("${plan.kcal} kcal", style = MaterialTheme.typography.titleMedium, color = TextPrimary, fontWeight = FontWeight.Bold)
            Text(
                "${plan.protein}g P · ${plan.carbs}g C · ${plan.fat}g F",
                style = MaterialTheme.typography.labelMedium, color = TextSecondary
            )
        }
    }
}

@Composable
private fun DietToggle(label: String, selected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        modifier
            .clip(RoundedCornerShape(14.dp))
            .background(if (selected) AccentGreen.copy(alpha = 0.18f) else Surface2.copy(alpha = 0.5f))
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(label, color = if (selected) AccentGreen else TextSecondary, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun SegTab(label: String, selected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        modifier
            .clip(RoundedCornerShape(14.dp))
            .background(if (selected) AccentBlue.copy(alpha = 0.2f) else Color.Transparent)
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(label, color = if (selected) AccentBlue else TextSecondary, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
private fun EmptyHint(text: String) {
    GlassCard(Modifier.fillMaxWidth()) {
        Text(text, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
    }
}
