package edu.ucne.farmaciacruz.presentation.navigation

import kotlinx.serialization.Serializable

@Serializable
object LoginRoute

@Serializable
object RegistroRoute

@Serializable
object RecuperarPasswordRoute

@Serializable
object ProductosRoute

@Serializable
object CarritoRoute

@Serializable
object ConfiguracionRoute

@Serializable
data class ProductoDetalleRoute(val productoId: Int)

@Serializable
object CheckoutRoute

@Serializable
object MisOrdenesRoute

@Serializable
data class OrdenDetalleRoute(val orderId: Int)

@Serializable
object AdminDashboardRoute

@Serializable
object AdminProductosRoute

@Serializable
object AdminUsuariosRoute

@Serializable
object AdminOrdenesRoute