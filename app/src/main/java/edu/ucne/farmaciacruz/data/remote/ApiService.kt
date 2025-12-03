package edu.ucne.farmaciacruz.data.remote

import edu.ucne.farmaciacruz.data.remote.dto.*
import retrofit2.Response
import retrofit2.http.*

interface ApiService {
    @POST("api/Usuarios/login")
    suspend fun login(@Body request: LoginRequest): Response<ApiResponse<AuthResponseDto>>

    @POST("api/Usuarios/registro")
    suspend fun register(@Body request: RegisterRequest): Response<ApiResponse<AuthResponseDto>>

    @POST("api/Usuarios/refresh-token")
    suspend fun refreshToken(@Body request: RefreshTokenRequest): Response<ApiResponse<AuthResponseDto>>

    @GET("api/Usuarios/perfil")
    suspend fun getProfile(): Response<ApiResponse<UsuarioDto>>

    @PUT("api/Usuarios/perfil")
    suspend fun updateProfile(@Body request: UpdateProfileRequest): Response<ApiResponse<UsuarioDto>>

    @POST("api/Usuarios/cambiar-password")
    suspend fun changePassword(@Body request: ChangePasswordRequest): Response<ApiResponse<Unit>>

    @POST("api/Usuarios/solicitar-recuperacion-password")
    suspend fun RecoveryPassword(@Body request: RecoveryRequest): Response<ApiResponse<Unit>>


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