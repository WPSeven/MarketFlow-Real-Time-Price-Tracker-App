package com.waiphyo.marketflow.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import com.waiphyo.marketflow.ui.feed.FeedScreen

// ── Route constants ───────────────────────────────────────────────────────────

object Routes {
    const val FEED = "feed"
    const val DETAIL = "detail/{${NavArgs.SYMBOL}}"
    fun detail(symbol: String) = "detail/$symbol"
}

object NavArgs {
    const val SYMBOL = "symbol"
}

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
fun AppNavigation(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Routes.FEED,
    ) {
        // ── Feed ───────
        composable(route = Routes.FEED) {
            FeedScreen(
                onSymbolClick = { symbol ->
                    navController.navigate(Routes.detail(symbol))
                }
            )
        }

        // ── Detail ───────
        composable(
            route = Routes.DETAIL,
            arguments = listOf(
                navArgument(NavArgs.SYMBOL) { type = NavType.StringType }
            ),
            deepLinks = listOf(
                navDeepLink { uriPattern = "stocks://symbol/{${NavArgs.SYMBOL}}" }
            ),
        ) {

        }
    }
}
