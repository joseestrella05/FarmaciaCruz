package edu.ucne.farmaciacruz.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import edu.ucne.farmaciacruz.MainViewModel
import edu.ucne.farmaciacruz.presentation.configuracion.ConfiguracionScreen
import edu.ucne.farmaciacruz.presentation.login.LoginScreen
import edu.ucne.farmaciacruz.presentation.login.recoverypassword.RecuperarPasswordScreen
import edu.ucne.farmaciacruz.presentation.login.registro.RegistroScreen
import edu.ucne.farmaciacruz.presentation.producto.ProductosScreen

@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController(),
    mainViewModel: MainViewModel = hiltViewModel()
) {
    val token by mainViewModel.isLoggedIn.collectAsState()

    val startDestination = if (!token.isNullOrEmpty()) ProductosRoute else LoginRoute

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable<LoginRoute> {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(ProductosRoute) {
                        popUpTo(LoginRoute) { inclusive = true }
                    }
                },
                onRegistroClick = {
                    navController.navigate(RegistroRoute)
                },
                onOlvidoPasswordClick = {
                    navController.navigate(RecuperarPasswordRoute)
                }
            )
        }

        composable<ProductosRoute> {
            LaunchedEffect(token) {
                if (token.isNullOrEmpty()) {
                    navController.navigate(LoginRoute) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            }

            ProductosScreen(
                onProductoClick = { productoId ->
                    navController.navigate(ProductoDetalleRoute(productoId))
                },
                onConfigClick = {
                    navController.navigate(ConfiguracionRoute)
                },
                onCarritoClick = {
                    navController.navigate(CarritoRoute)
                }
            )
        }

        composable<ConfiguracionRoute> {
            ConfiguracionScreen(
                onBack = {
                    navController.popBackStack()
                },
                onLogout = {
                    navController.navigate(LoginRoute) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable<ProductoDetalleRoute> { backStackEntry ->
            val args = backStackEntry.toRoute<ProductoDetalleRoute>()
        }

        composable<CarritoRoute> {
        }

        composable<RegistroRoute> {
            RegistroScreen(
                onRegistroSuccess = {
                    navController.navigate(ProductosRoute) {
                        popUpTo(LoginRoute) { inclusive = true }
                    }
                },
                onBackToLogin = {
                    navController.popBackStack()
                }
            )
        }

        composable<RecuperarPasswordRoute> {
            RecuperarPasswordScreen(
                onBackToLogin = {
                    navController.popBackStack()
                }
            )
        }

    }
}