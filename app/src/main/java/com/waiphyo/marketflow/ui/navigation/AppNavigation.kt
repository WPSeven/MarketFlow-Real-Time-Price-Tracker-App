package com.waiphyo.marketflow.ui.navigation

import android.content.res.Configuration
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import androidx.navigation.navOptions
import com.waiphyo.marketflow.R
import com.waiphyo.marketflow.ui.account.AccountScreen
import com.waiphyo.marketflow.ui.detail.DetailScreen
import com.waiphyo.marketflow.ui.favorites.FavoritesScreen
import com.waiphyo.marketflow.ui.feed.FeedHomePreviewContent
import com.waiphyo.marketflow.ui.feed.FeedScreen
import com.waiphyo.marketflow.ui.theme.PriceTrackerTheme

// ── Route constants ───────────────────────────────────────────────────────────

object Routes {
    const val FEED = "feed"
    const val FAVORITES = "favorites"
    const val ACCOUNT = "account"
    const val DETAIL = "detail/{${NavArgs.SYMBOL}}"
    fun detail(symbol: String) = "detail/$symbol"
}

object NavArgs {
    const val SYMBOL = "symbol"
}

private data class BottomNavItem(
    val route: String,
    val label: String,
    val iconRes: Int,
)

private val bottomNavItems = listOf(
    BottomNavItem(Routes.FEED, "Home", R.drawable.ic_home),
    BottomNavItem(Routes.FAVORITES, "Favorites", R.drawable.ic_favorite),
    BottomNavItem(Routes.ACCOUNT, "Account", R.drawable.ic_profile),
)

private val BottomNavIconSize = 24.dp

private val BottomNavLabelStyle = TextStyle(
    fontWeight = FontWeight.SemiBold,
    fontSize = 12.sp,
    letterSpacing = 0.15.sp,
)

// ── NavHost ───────────────────────────────────────────────────────────────────

/**
 * Defines the two-destination NavHost.
 *
 * Destinations:
 *  - [Routes.FEED]   — start destination, shows the live price list.
 *  - [Routes.DETAIL] — shows details for a single symbol; also reachable via
 *                       deep link `stocks://symbol/{symbol}`.
 */
@Composable
fun AppNavigation(
    navController: NavHostController,
    isDarkTheme: Boolean,
    onThemeChange: (Boolean) -> Unit,
) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val showBottomBar = bottomNavItems.any { it.route == currentRoute }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                MarketFlowBottomBar(
                    currentRoute = currentRoute,
                    onItemClick = { route ->
                        navController.navigate(
                            route = route,
                            navOptions = navOptions {
                                popUpTo(Routes.FEED) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            },
                        )
                    },
                )
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Routes.FEED,
            modifier = Modifier.fillMaxSize(),
        ) {
            // ── Feed ───────
            composable(route = Routes.FEED) {
                FeedScreen(
                    onSymbolClick = { symbol ->
                        navController.navigate(Routes.detail(symbol))
                    },
                )
            }

            // ── Favorites ───────
            composable(route = Routes.FAVORITES) {
                FavoritesScreen(paddingValues = innerPadding)
            }

            // ── Account ───────
            composable(route = Routes.ACCOUNT) {
                AccountScreen(
                    userName = "MarketFlow User",
                    userEmail = "user@marketflow.app",
                    isDarkTheme = isDarkTheme,
                    onThemeChange = onThemeChange,
                    paddingValues = innerPadding,
                )
            }

            // ── Detail ───────
            composable(
                route = Routes.DETAIL,
                arguments = listOf(
                    navArgument(NavArgs.SYMBOL) { type = NavType.StringType },
                ),
                deepLinks = listOf(
                    navDeepLink { uriPattern = "stocks://symbol/{${NavArgs.SYMBOL}}" },
                ),
            ) {
                DetailScreen(
                    onNavigateUp = { navController.navigateUp() }
                )
            }
        }
    }
}

@Composable
private fun MarketFlowBottomBar(
    currentRoute: String?,
    onItemClick: (String) -> Unit,
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
    ) {
        bottomNavItems.forEach { item ->
            val selected = currentRoute == item.route
            NavigationBarItem(
                selected = selected,
                onClick = { onItemClick(item.route) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    indicatorColor = MaterialTheme.colorScheme.surfaceVariant,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                ),
                icon = {
                    Icon(
                        painter = painterResource(id = item.iconRes),
                        contentDescription = item.label,
                        modifier = Modifier.size(BottomNavIconSize),
                    )
                },
                label = {
                    Text(
                        text = item.label,
                        style = MaterialTheme.typography.labelSmall.merge(BottomNavLabelStyle),
                    )
                },
                alwaysShowLabel = true,
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 393, heightDp = 852, name = "Home + Bottom Nav")
@Composable
private fun AppNavigationHomePreview() {
    PriceTrackerTheme {
        Scaffold(
            bottomBar = {
                MarketFlowBottomBar(
                    currentRoute = Routes.FEED,
                    onItemClick = {},
                )
            },
        ) { paddingValues ->
            Column(modifier = Modifier.padding(paddingValues)) {
                FeedHomePreviewContent()
            }
        }
    }
}

@Preview(
    showBackground = true,
    widthDp = 393,
    heightDp = 852,
    name = "Home + Bottom Nav Dark",
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun AppNavigationHomeDarkPreview() {
    PriceTrackerTheme(darkTheme = true) {
        Scaffold(
            bottomBar = {
                MarketFlowBottomBar(
                    currentRoute = Routes.FEED,
                    onItemClick = {},
                )
            },
        ) { paddingValues ->
            Column(modifier = Modifier.padding(paddingValues)) {
                FeedHomePreviewContent()
            }
        }
    }
}
