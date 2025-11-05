package com.kkt981019.bitcoin_chart

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.kkt981019.bitcoin_chart.screen.coindetail.CoinDetailScreen
import com.kkt981019.bitcoin_chart.screen.MainScreen
import com.kkt981019.bitcoin_chart.screen.MyPageScreen

@Composable
fun AppNavHost() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "coin_list") {
        composable("coin_list") {
            MainScreen(navController = navController)
        }
        composable(
            route = "coin_detail/{symbol}/{koreanName}/{englishName}",
            arguments = listOf(
                navArgument("symbol") { type = NavType.StringType },
                navArgument("koreanName") { type = NavType.StringType },
                navArgument("englishName") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val symbol = backStackEntry.arguments?.getString("symbol") ?: ""
            val koreanName = backStackEntry.arguments?.getString("koreanName") ?: ""
            val englishName = backStackEntry.arguments?.getString("englishName") ?: ""
            CoinDetailScreen(
                symbol = symbol,
                koreanName = koreanName,
                navController = navController,
                englishName = englishName
            )
        }
        composable("my_page") {
            MyPageScreen(navController = navController)
        }
    }
}