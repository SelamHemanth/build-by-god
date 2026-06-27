package com.buildbygod.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.material3.MaterialTheme
import com.buildbygod.ui.theme.GlassStroke
import com.buildbygod.ui.theme.Ink
import com.buildbygod.ui.theme.InkElevated
import com.buildbygod.ui.theme.LocalFitTokens
import com.buildbygod.ui.theme.TextSecondary

@Composable
fun BuildByGodBottomBar(
    currentRoute: String?,
    onSelect: (TopLevel) -> Unit
) {
    val tokens = LocalFitTokens.current
    Box(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .clip(RoundedCornerShape(26.dp))
            .background(
                Brush.linearGradient(listOf(GlassStroke, Color.Transparent))
            )
            .padding(1.dp)
            .clip(RoundedCornerShape(25.dp))
            .background(InkElevated.copy(alpha = 0.95f))
            .navigationBarsPadding()
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 6.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TopLevel.entries.forEach { item ->
                val selected = currentRoute == item.route
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .weight(1f)
                        .selectable(selected = selected, onClick = { onSelect(item) })
                        .padding(vertical = 4.dp)
                ) {
                    Box(
                        Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .then(
                                if (selected)
                                    Modifier.background(tokens.accentGradient)
                                else Modifier
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            item.icon,
                            contentDescription = item.label,
                            tint = if (selected) Ink else TextSecondary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    if (selected) {
                        Text(
                            item.label,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onBackground,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}
