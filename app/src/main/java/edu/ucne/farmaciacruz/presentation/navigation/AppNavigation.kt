package edu.ucne.farmaciacruz.presentation.navigation

import androidx.compose.material3.ExperimentalMaterial3Api
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
import edu.ucne.farmaciacruz.data.local.PreferencesManager
import edu.ucne.farmaciacruz.presentation.admin.dashboard.AdminDashboardScreen
import edu.ucne.farmaciacruz.presentation.admin.ordenes.AdminOrdenesScreen
import edu.ucne.farmaciacruz.presentation.admin.productos.AdminProductosScreen
import edu.ucne.farmaciacruz.presentation.admin.usuarios.AdminUsuariosScreen
import edu.ucne.farmaciacruz.presentation.checkout.CheckoutScreen
import edu.ucne.farmaciacruz.presentation.configuracion.ConfiguracionScreen
import edu.ucne.farmaciacruz.presentation.login.LoginScreen
import edu.ucne.farmaciacruz.presentation.login.recoverypassword.RecuperarPasswordScreen
import edu.ucne.farmaciacruz.presentation.login.registro.RegistroScreen
import edu.ucne.farmaciacruz.presentation.order.MisOrdenesScreen
import edu.ucne.farmaciacruz.presentation.producto.ProductosScreen
import edu.ucne.farmaciacruz.presentation.producto.detalle.ProductoDetalleScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController(),
    preferencesManager: PreferencesManager = hiltViewModel<NavigationViewModel>().preferencesManager
) {
    val token by preferencesManager.getToken().collectAsState(initial = null)

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
                onCheckoutClick = {
                    navController.navigate(CheckoutRoute)
                }
            )
        }

        composable<CheckoutRoute> {
            CheckoutScreen(
                onBack = {
                    navController.popBackStack()
                },
                onNavigateToOrders = {
                    navController.navigate(MisOrdenesRoute) {
                        popUpTo(ProductosRoute)
                    }
                }
            )
        }

        composable<MisOrdenesRoute> {
            MisOrdenesScreen(
                onOrderClick = { orderId ->
                    navController.navigate(OrdenDetalleRoute(orderId))
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
                },
                onNavigateToAdmin = {
                    navController.navigate(AdminDashboardRoute)
                }
            )
        }

        composable<ProductoDetalleRoute> { backStackEntry ->
            val args = backStackEntry.toRoute<ProductoDetalleRoute>()
            ProductoDetalleScreen(
                onBack = {
                    navController.popBackStack()
                }
            )
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

       composable<AdminDashboardRoute> {
            AdminDashboardScreen(
                onNavigateToProductos = { navController.navigate(AdminProductosRoute) },
                onNavigateToUsuarios = { navController.navigate(AdminUsuariosRoute) },
                onNavigateToOrdenes = { navController.navigate(AdminOrdenesRoute) },
                onBack = { navController.popBackStack() }
            )
        }

        composable<AdminProductosRoute> {
            AdminProductosScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable<AdminUsuariosRoute> {
            AdminUsuariosScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable<AdminOrdenesRoute> {
            AdminOrdenesScreen(
                onBack = { navController.popBackStack() }
            )
        }
    }
}