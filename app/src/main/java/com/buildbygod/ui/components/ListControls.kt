package com.buildbygod.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
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
import com.buildbygod.ui.theme.AccentBlue
import com.buildbygod.ui.theme.Surface2
import com.buildbygod.ui.theme.TextPrimary
import com.buildbygod.ui.theme.TextSecondary

/** Standard rounded search field used across every browsable list. */
@Composable
fun ListSearchField(
    query: String,
    onQuery: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQuery,
        placeholder = { Text(placeholder) },
        leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
        singleLine = true,
        shape = RoundedCornerShape(16.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = AccentBlue,
            unfocusedBorderColor = Surface2,
            focusedTextColor = TextPrimary,
            unfocusedTextColor = TextPrimary
        ),
        modifier = modifier
    )
}

/** A compact selectable filter chip with an accent outline when active. */
@Composable
fun FilterChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    accent: Color = AccentBlue
) {
    Box(
        modifier
            .clip(RoundedCornerShape(50))
            .background(if (selected) accent.copy(alpha = 0.22f) else Color.Transparent)
            .border(1.dp, if (selected) accent else TextSecondary.copy(alpha = 0.3f), RoundedCornerShape(50))
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            label,
            style = MaterialTheme.typography.labelMedium,
            color = if (selected) accent else TextSecondary,
            fontWeight = FontWeight.SemiBold
        )
    }
}

/**
 * A "Sort" chip that opens a dropdown of options. Generic over the option type [T].
 * [label] renders each option's display text; the chip shows the current selection.
 */
@Composable
fun <T> SortChip(
    options: List<T>,
    selected: T,
    label: (T) -> String,
    onSelect: (T) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    Box(modifier) {
        Row(
            Modifier
                .clip(RoundedCornerShape(50))
                .border(1.dp, AccentBlue.copy(alpha = 0.6f), RoundedCornerShape(50))
                .clickable { expanded = true }
                .padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(Icons.AutoMirrored.Filled.Sort, contentDescription = "Sort", tint = AccentBlue, modifier = Modifier.size(16.dp))
            Text(
                label(selected),
                style = MaterialTheme.typography.labelMedium,
                color = AccentBlue,
                fontWeight = FontWeight.SemiBold
            )
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(label(option), color = TextPrimary) },
                    trailingIcon = {
                        if (option == selected) Icon(Icons.Filled.Check, contentDescription = null, tint = AccentBlue)
                    },
                    onClick = {
                        onSelect(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

/** A horizontally scrollable row meant to host filter chips + a trailing sort chip. */
@Composable
fun ControlsRow(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Row(
        modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(Modifier.width(0.dp))
        content()
        Spacer(Modifier.width(0.dp))
    }
}
