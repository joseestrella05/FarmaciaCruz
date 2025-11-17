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
