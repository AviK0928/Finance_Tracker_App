package com.example.finance_tracker.core.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshContainer
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.input.nestedscroll.nestedScroll

/**
 * Pull down on the scrollable content to reload it. [onRefresh] suspends until the reload is done;
 * the indicator stops when it returns, so it cannot be left spinning when a fast reload (for example
 * an offline failure) finishes before the next frame. The content must scroll (LazyColumn or
 * verticalScroll): the pull is read from the scroll the content does not consume.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RefreshableBox(
    onRefresh: suspend () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val pullState = rememberPullToRefreshState()
    val currentOnRefresh by rememberUpdatedState(onRefresh)

    // The state turns refreshing when a pull is released past the threshold
    if (pullState.isRefreshing) {
        LaunchedEffect(true) {
            try {
                currentOnRefresh()
            } finally {
                pullState.endRefresh()
            }
        }
    }

    // clipToBounds: the resting indicator sits above the box and must not show over the top bar
    Box(modifier = modifier.nestedScroll(pullState.nestedScrollConnection).clipToBounds()) {
        content()
        PullToRefreshContainer(state = pullState, modifier = Modifier.align(Alignment.TopCenter))
    }
}
