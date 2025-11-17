package edu.ucne.farmaciacruz.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import edu.ucne.farmaciacruz.presentation.Configuracion.ConfiguracionScreen
import edu.ucne.farmaciacruz.presentation.login.LoginScreen
import edu.ucne.farmaciacruz.presentation.producto.ProductosScreen

@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController(),
    startDestination: Any = LoginRoute
) {
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
    }
}
