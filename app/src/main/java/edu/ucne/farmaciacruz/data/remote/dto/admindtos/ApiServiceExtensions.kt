package edu.ucne.farmaciacruz.data.remote.dto.admindtos

import com.google.android.gms.common.api.Response
import edu.ucne.farmaciacruz.data.remote.ApiResponse
import edu.ucne.farmaciacruz.data.remote.dto.CambiarEstadoDto
import edu.ucne.farmaciacruz.data.remote.dto.CambiarRolDto
import edu.ucne.farmaciacruz.data.remote.dto.UsuarioReadDto
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface ApiServiceExtensions {
    // Estadísticas
    @GET("api/Admin/estadisticas")
    suspend fun getEstadisticas(): Response<ApiResponse<Map<String, Any>>>

    // Gestión de Usuarios
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