package com.example.finance_tracker.features.dashboard.ui

import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.example.finance_tracker.features.dashboard.state.DashboardView
import java.util.Locale
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.remember

@Composable
fun DashboardTabSelector(
    selectedView: DashboardView,
    onViewSelected: (DashboardView) -> Unit
) {
    val tabs = DashboardView.values()
    TabRow(selectedTabIndex = tabs.indexOf(selectedView)) {
        tabs.forEachIndexed { index, view ->
            Tab(
                selected = selectedView == view,
                onClick = { onViewSelected(view) },
                text = {
                    Text(
                        view.name.replaceFirstChar {
                            if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString()
                        }
                    )
                },
                interactionSource = remember { MutableInteractionSource() }
            )
        }
    }
}