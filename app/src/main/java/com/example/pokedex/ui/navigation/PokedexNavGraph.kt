package com.example.pokedex.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.pokedex.ui.detail.PokemonDetailScreen
import com.example.pokedex.ui.detail.PokemonDetailViewModel
import com.example.pokedex.ui.list.PokemonListScreen

private object Routes {
    const val LIST = "list"
    const val DETAIL = "detail/{${PokemonDetailViewModel.ARG_NAME}}"
    fun detail(name: String) = "detail/$name"
}

@Composable
fun PokedexNavGraph() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Routes.LIST) {
        composable(Routes.LIST) {
            PokemonListScreen(
                onPokemonClick = { name -> navController.navigate(Routes.detail(name)) }
            )
        }
        composable(
            route = Routes.DETAIL,
            arguments = listOf(navArgument(PokemonDetailViewModel.ARG_NAME) { type = NavType.StringType })
        ) {
            PokemonDetailScreen(onBack = { navController.popBackStack() })
        }
    }
}
