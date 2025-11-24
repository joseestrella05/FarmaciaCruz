package edu.ucne.farmaciacruz.data.remote.api

import edu.ucne.farmaciacruz.data.remote.dto.AuthResponseDto
import edu.ucne.farmaciacruz.data.remote.dto.CreateOrderDto
import edu.ucne.farmaciacruz.data.remote.dto.OrderResponseDto
import edu.ucne.farmaciacruz.data.remote.dto.ProductoDto
import edu.ucne.farmaciacruz.data.remote.dto.UsuarioDto
import edu.ucne.farmaciacruz.data.remote.request.ChangePasswordRequest
import edu.ucne.farmaciacruz.data.remote.request.CreateProductoRequest
import edu.ucne.farmaciacruz.data.remote.request.LoginRequest
import edu.ucne.farmaciacruz.data.remote.request.RecoveryRequest
import edu.ucne.farmaciacruz.data.remote.request.RefreshTokenRequest
import edu.ucne.farmaciacruz.data.remote.request.RegisterRequest
import edu.ucne.farmaciacruz.data.remote.request.UpdateOrderStatusRequest
import edu.ucne.farmaciacruz.data.remote.request.UpdateProfileRequest
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.Response

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

    @PUT("api/Orders/{id}/estado")
    suspend fun updateOrderStatus(
        @Path("id") orderId: Int,
        @Body status: UpdateOrderStatusRequest
    ): Response<Unit>
}