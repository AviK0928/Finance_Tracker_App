package com.example.finance_tracker.core.ui

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.foundation.interaction.MutableInteractionSource
import com.example.finance_tracker.core.navigation.BottomNavItem

@Composable
fun BottomAppNavBar(
    currentRoute: String,
    onNavigate: (String) -> Unit
) {
    NavigationBar {
        BottomNavItem.items.forEach { item ->
            val interactionSource = remember { MutableInteractionSource() }

            NavigationBarItem(
                selected = currentRoute == item.route,
                onClick = {
                    if (currentRoute != item.route) {
                        onNavigate(item.route)
                    }
                },
                icon = {
                    Icon(
                        painter = painterResource(id = item.icon),
                        contentDescription = stringResource(id = item.label)
                    )
                },
                label = {
                    Text(text = stringResource(id = item.label))
                },
                interactionSource = interactionSource
            )
        }
    }
}
