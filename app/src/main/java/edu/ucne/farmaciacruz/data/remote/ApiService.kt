package edu.ucne.farmaciacruz.data.remote

import edu.ucne.farmaciacruz.data.remote.dto.*
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    // ==================== AUTENTICACIÓN ====================

    @POST("api/Auth/register")
    suspend fun register(@Body dto: RegisterRequest): Response<ApiResponse<AuthResponseDto>>

    @POST("api/Auth/login")
    suspend fun login(@Body dto: LoginRequest): Response<ApiResponse<AuthResponseDto>>

    @POST("api/Auth/logout")
    suspend fun logout(): Response<Unit>

    @POST("api/Auth/forgot-password")
    suspend fun forgotPassword(@Body dto: RecoveryRequest): Response<ApiResponse<Unit>>


    @GET("api/Auth/me")
    suspend fun getMe(): Response<ApiResponse<UsuarioReadDto>>


    @GET("api/Productos")
    suspend fun getProductos(): Response<List<ProductoDto>>

    @GET("api/Productos/{id}")
    suspend fun getProducto(@Path("id") id: Int): Response<ProductoDto>

    @GET("api/Productos/categoria/{categoria}")
    suspend fun getProductosPorCategoria(@Path("categoria") categoria: String): Response<List<ProductoDto>>

    @POST("api/Productos")
    suspend fun createProducto(@Body producto: CreateProductoRequest): Response<ApiResponse<ProductoDto>>

    @PUT("api/Productos/{id}")
    suspend fun updateProducto(
        @Path("id") id: Int,
        @Body producto: ProductoDto
    ): Response<Unit>

    @DELETE("api/Productos/{id}")
    suspend fun deleteProducto(@Path("id") id: Int): Response<Unit>

    // ==================== ÓRDENES ====================

    @POST("api/Orders")
    suspend fun createOrder(@Body order: CreateOrderDto): Response<ApiResponse<OrderResponseDto>>

    @GET("api/Orders/{id}")
    suspend fun getOrder(@Path("id") orderId: Int): Response<ApiResponse<OrderResponseDto>>

    @GET("api/Orders/usuario/{usuarioId}")
    suspend fun getUserOrders(@Path("usuarioId") usuarioId: Int): Response<List<OrderResponseDto>>

    @GET("api/Orders")
    suspend fun getAllOrders(): Response<List<OrderResponseDto>>

    @PUT("api/Orders/{id}/estado")
    suspend fun updateOrderStatus(
        @Path("id") orderId: Int,
        @Body status: UpdateOrderStatusRequest
    ): Response<Unit>

    // ==================== ADMINISTRACIÓN ====================

    @GET("api/Admin/estadisticas")
    suspend fun getEstadisticas(): Response<ApiResponse<Map<String, Any>>>

    @GET("api/Admin/usuarios")
    suspend fun getAllUsuarios(): Response<ApiResponse<List<UsuarioReadDto>>>

    @GET("api/Admin/usuarios/{id}")
    suspend fun getUsuarioById(@Path("id") id: Int): Response<ApiResponse<UsuarioReadDto>>

    @PUT("api/Admin/usuarios/{id}/rol")
    suspend fun cambiarRolUsuario(
        @Path("id") id: Int,
        @Body dto: CambiarRolDto
    ): Response<ApiResponse<UsuarioReadDto>>

    @PUT("api/Admin/usuarios/{id}/estado")
    suspend fun cambiarEstadoUsuario(
        @Path("id") id: Int,
        @Body dto: CambiarEstadoDto
    ): Response<ApiResponse<UsuarioReadDto>>

    @DELETE("api/Admin/usuarios/{id}")
    suspend fun deleteUsuario(@Path("id") id: Int): Response<Unit>

    @POST("api/Admin/usuarios/{id}/desbloquear")
    suspend fun desbloquearUsuario(@Path("id") id: Int): Response<ApiResponse<UsuarioReadDto>>
}