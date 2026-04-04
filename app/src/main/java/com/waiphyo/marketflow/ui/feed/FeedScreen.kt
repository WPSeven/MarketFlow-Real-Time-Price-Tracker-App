@file:Suppress("ktlint:standard:function-naming", "FunctionName")

package com.waiphyo.marketflow.ui.feed

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.waiphyo.marketflow.data.model.FlashState
import com.waiphyo.marketflow.data.model.STOCK_CATALOGUE
import com.waiphyo.marketflow.data.model.StockSymbol
import com.waiphyo.marketflow.ui.theme.PriceTrackerTheme
import java.util.Locale

// ── Screen ────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedScreen(
    onSymbolClick: (String) -> Unit,
    viewModel: FeedViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    FeedScreenContent(
        uiState = uiState,
        onSymbolClick = onSymbolClick,
        onToggleFeed = viewModel::toggleFeed,
        onToggleSort = viewModel::toggleSortOrder,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun FeedScreenContent(
    uiState: FeedUiState,
    onSymbolClick: (String) -> Unit,
    onToggleFeed: () -> Unit,
    onToggleSort: () -> Unit,
) {
    Scaffold(
        topBar = {
            FeedTopBar(
                isConnected = uiState.isConnected,
                isFeedRunning = uiState.isFeedRunning,
                sortOrder = uiState.sortOrder,
                onToggle = onToggleFeed,
                onToggleSort = onToggleSort,
            )
        },
    ) { padding ->
        if (uiState.stocks.isEmpty()) {
            EmptyState(modifier = Modifier.padding(padding))
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
            ) {
                items(
                    items = uiState.stocks,
                    key = { it.symbol },
                ) { stock ->
                    StockRow(
                        stock = stock,
                        onClick = { onSymbolClick(stock.symbol) },
                    )
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant,
                        thickness = 0.5.dp,
                    )
                }
            }
        }
    }
}

// ── Top Bar ───────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FeedTopBar(
    isConnected: Boolean,
    isFeedRunning: Boolean,
    sortOrder: PriceSortOrder,
    onToggle: () -> Unit,
    onToggleSort: () -> Unit,
) {
    TopAppBar(
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Connection indicator dot
                Surface(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape),
                    color = if (isConnected) Color(0xFF4CAF50) else Color(0xFFF44336),
                ) {}
                Text(
                    text = if (isConnected) "  Connected" else "  Disconnected",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(start = 4.dp),
                )
            }
        },
        actions = {
            TextButton(onClick = onToggleSort) {
                val isDesc = sortOrder == PriceSortOrder.DESC
                Icon(
                    imageVector = if (isDesc) Icons.Filled.ArrowDownward else Icons.Filled.ArrowUpward,
                    contentDescription = if (isDesc) {
                        "Sorted by price high to low"
                    } else {
                        "Sorted by price low to high"
                    },
                    modifier = Modifier.size(18.dp),
                )
                Text(
                    text = if (isDesc) "High-Low" else "Low-High",
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.padding(start = 6.dp),
                )
            }
            Button(
                onClick = onToggle,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isFeedRunning)
                        MaterialTheme.colorScheme.error
                    else
                        MaterialTheme.colorScheme.primary,
                ),
                modifier = Modifier.padding(end = 8.dp),
            ) {
                Text(if (isFeedRunning) "Stop" else "Start")
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
    )
}

// ── Stock Row ─────────────────────────────────────────────────────────────────

@Composable
private fun StockRow(
    stock: StockSymbol,
    onClick: () -> Unit,
) {
    // Flash colour: green on UP, red on DOWN, transparent otherwise.
    val flashTarget = when (stock.flashState) {
        FlashState.UP   -> Color(0x3300C853)  // translucent green
        FlashState.DOWN -> Color(0x33FF1744)  // translucent red
        FlashState.NONE -> Color.Transparent
    }
    val flashColor by animateColorAsState(
        targetValue = flashTarget,
        animationSpec = tween(durationMillis = 300),
        label = "flash_${stock.symbol}",
    )

    Surface(
        color = flashColor,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Left: symbol + name
            Column {
                Text(
                    text = stock.symbol,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = stock.name,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            // Right: price + indicator
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "$${String.format(Locale.US, "%.2f", stock.price)}",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = "${stock.changeIndicator} ${String.format(Locale.US, "%.2f", kotlin.math.abs(stock.priceChange))}",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (stock.isUp) Color(0xFF4CAF50) else Color(0xFFF44336),
                    fontWeight = FontWeight.Medium,
                )
            }
        }
    }
}

// ── Empty State ───────────────────────────────────────────────────────────────

@Composable
private fun EmptyState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "📈",
            fontSize = 48.sp,
        )
        Text(
            text = "Press Start to begin tracking",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(top = 12.dp),
        )
    }
}

private val PreviewFeedUiState = FeedUiState(
    stocks = STOCK_CATALOGUE.take(6).mapIndexed { index, stock ->
        stock.copy(
            price = stock.price + (index * 3.14),
            previousPrice = stock.previousPrice,
            flashState = when (index % 3) {
                0 -> FlashState.UP
                1 -> FlashState.DOWN
                else -> FlashState.NONE
            },
        )
    }.sortedByDescending { it.price },
    isConnected = true,
    isFeedRunning = true,
    sortOrder = PriceSortOrder.DESC,
)

@Composable
fun FeedHomePreviewContent() {
    FeedScreenContent(
        uiState = PreviewFeedUiState,
        onSymbolClick = {},
        onToggleFeed = {},
        onToggleSort = {},
    )
}

@Preview(showBackground = true, widthDp = 393, heightDp = 852, name = "Feed Screen")
@Composable
private fun FeedScreenPreview() {
    PriceTrackerTheme {
        FeedHomePreviewContent()
    }
}

@Preview(showBackground = true, widthDp = 393, heightDp = 852, name = "Feed Screen - Empty")
@Composable
private fun FeedScreenEmptyPreview() {
    PriceTrackerTheme {
        FeedScreenContent(
            uiState = FeedUiState(),
            onSymbolClick = {},
            onToggleFeed = {},
            onToggleSort = {},
        )
    }
}

@Preview(
    showBackground = true,
    widthDp = 393,
    heightDp = 852,
    name = "Feed Screen - Dark",
    uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun FeedScreenDarkPreview() {
    PriceTrackerTheme(darkTheme = true) {
        FeedScreenContent(
            uiState = PreviewFeedUiState,
            onSymbolClick = {},
            onToggleFeed = {},
            onToggleSort = {},
        )
    }
}
