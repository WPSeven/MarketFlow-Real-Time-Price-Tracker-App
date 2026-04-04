package com.waiphyo.marketflow.ui.detail

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.waiphyo.marketflow.data.model.FlashState
import com.waiphyo.marketflow.data.model.StockSymbol
import com.waiphyo.marketflow.ui.theme.PriceTrackerTheme
import kotlin.math.abs

// ── Screen ────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    onNavigateUp: () -> Unit,
    viewModel: DetailViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val stock = uiState.stock

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stock?.symbol ?: "Details",
                        fontWeight = FontWeight.Bold,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            )
        },
    ) { padding ->
        if (stock == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }
        } else {
            DetailContent(
                stock = stock,
                modifier = Modifier.padding(padding),
            )
        }
    }
}

// ── Detail Content ────────────────────────────────────────────────────────────

@Composable
private fun DetailContent(
    stock: StockSymbol,
    modifier: Modifier = Modifier,
) {
    val flashTarget = when (stock.flashState) {
        FlashState.UP   -> Color(0x3300C853)
        FlashState.DOWN -> Color(0x33FF1744)
        FlashState.NONE -> Color.Transparent
    }
    val flashColor by animateColorAsState(
        targetValue = flashTarget,
        animationSpec = tween(durationMillis = 300),
        label = "detail_flash_${stock.symbol}",
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {

        // ── Price card ────────────────────────────────────────────────────────
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = flashColor.takeIf { it != Color.Transparent }
                ?: MaterialTheme.colorScheme.surfaceVariant),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = stock.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom,
                ) {
                    Text(
                        text = "$${String.format("%.2f", stock.price)}",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                    )
                    PriceChangeChip(stock = stock)
                }
            }
        }

        HorizontalDivider()

        // ── Description ───────────────────────────────────────────────────────
        Text(
            text = "About ${stock.symbol}",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = stock.description,
            style = MaterialTheme.typography.bodyLarge,
            lineHeight = 24.sp,
        )

        // ── Live info row ─────────────────────────────────────────────────────
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
            ),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                InfoCell(label = "Symbol", value = stock.symbol)
                InfoCell(label = "Change", value = "${stock.changeIndicator} $${String.format("%.2f",
                    abs(stock.priceChange)
                )}")
                InfoCell(label = "Direction", value = if (stock.isUp) "↑ Up" else "↓ Down",
                    valueColor = if (stock.isUp) Color(0xFF4CAF50) else Color(0xFFF44336))
            }
        }
    }
}

// ── Composable helpers ────────────────────────────────────────────────────────

@Composable
private fun PriceChangeChip(stock: StockSymbol) {
    val bgColor = if (stock.isUp) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
    val textColor = if (stock.isUp) Color(0xFF2E7D32) else Color(0xFFC62828)

    Card(
        colors = CardDefaults.cardColors(containerColor = bgColor),
        elevation = CardDefaults.cardElevation(0.dp),
    ) {
        Text(
            text = "${stock.changeIndicator} ${String.format("%.2f", abs(stock.priceChange))}",
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            color = textColor,
            fontWeight = FontWeight.SemiBold,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

@Composable
private fun InfoCell(
    label: String,
    value: String,
    valueColor: Color = MaterialTheme.colorScheme.onSecondaryContainer,
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.6f),
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = valueColor,
        )
    }
}

// ── Previews ──────────────────────────────────────────────────────────────────

private val previewStock = StockSymbol(
    symbol = "AAPL",
    name = "Apple Inc.",
    price = 195.89,
    previousPrice = 182.63,
    description = "Apple designs iPhones, Macs, and the world's largest app marketplace.",
    flashState = FlashState.UP,
)

@Preview(name = "Detail – Light", showBackground = true)
@Composable
private fun DetailContentLightPreview() {
    PriceTrackerTheme(darkTheme = false) {
        DetailContent(stock = previewStock)
    }
}

@Preview(name = "Detail – Dark", showBackground = true, backgroundColor = 0xFF1C1B1F)
@Composable
private fun DetailContentDarkPreview() {
    PriceTrackerTheme(darkTheme = true) {
        DetailContent(stock = previewStock)
    }
}
