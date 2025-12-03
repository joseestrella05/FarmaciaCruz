package edu.ucne.farmaciacruz.domain.model

data class AdminStats(
    val totalUsuarios: Int = 0,
    val usuariosActivos: Int = 0,
    val totalProductos: Int = 0,
    val totalOrdenes: Int = 0,
    val ventasDelMes: Double = 0.0,
    val usuariosPorRol: Map<String, Int> = emptyMap(),
    val productosPorCategoria: Map<String, Int> = emptyMap()
)