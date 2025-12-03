package edu.ucne.farmaciacruz.data.repository

import edu.ucne.farmaciacruz.core.common.ErrorMessages
import edu.ucne.farmaciacruz.data.local.PreferencesManager
import edu.ucne.farmaciacruz.data.remote.ApiService
import edu.ucne.farmaciacruz.data.remote.dto.LoginRequest
import edu.ucne.farmaciacruz.data.remote.dto.RecoveryRequest
import edu.ucne.farmaciacruz.data.remote.dto.RegisterRequest
import edu.ucne.farmaciacruz.domain.model.Resource
import edu.ucne.farmaciacruz.domain.model.User
import edu.ucne.farmaciacruz.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val apiService: ApiService,
    private val preferencesManager: PreferencesManager
) : AuthRepository {

    override fun login(email: String, password: String): Flow<Resource<User>> = flow {
        emit(Resource.Loading())

        try {
            val response = apiService.login(LoginRequest(email, password))

            if (response.isSuccessful && response.body() != null) {
                val data = response.body()!!.data!!

                preferencesManager.saveToken(data.token)
                preferencesManager.saveRefreshToken(data.refreshToken)

                preferencesManager.saveUserData(
                    userId = data.usuario.usuarioId,
                    email = data.usuario.email,
                    name = "${data.usuario.nombre} ${data.usuario.apellido}",
                    role = data.usuario.rol
                )

                emit(Resource.Success(User(
                    id = data.usuario.usuarioId,
                    email = data.usuario.email,
                    nombre = data.usuario.nombre,
                    apellido = data.usuario.apellido,
                    telefono = data.usuario.telefono,
                    rol = data.usuario.rol
                )))
            } else {
                val message = when (response.code()) {
                    401 -> ErrorMessages.NO_AUTORIZADO
                    403 -> "Cuenta desactivada"
                    else -> response.body()?.mensaje ?: ErrorMessages.ERROR_DESCONOCIDO
                }
                emit(Resource.Error(message))
            }

        } catch (e: HttpException) {
            emit(Resource.Error("${ErrorMessages.ERROR_RED}: ${e.message()}"))
        } catch (e: IOException) {
            emit(Resource.Error(ErrorMessages.ERROR_CONEXION))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: ErrorMessages.ERROR_DESCONOCIDO))
        }
    }

    override fun register(
        email: String,
        password: String,
        nombre: String,
        apellido: String,
        telefono: String?
    ): Flow<Resource<User>> = flow {
        emit(Resource.Loading())

        try {
            val request = RegisterRequest(email, password, nombre, apellido, telefono)
            val response = apiService.register(request)

            if (response.isSuccessful && response.body() != null) {
                val data = response.body()!!.data!!

                preferencesManager.saveToken(data.token)
                preferencesManager.saveRefreshToken(data.refreshToken)

                preferencesManager.saveUserData(
                    userId = data.usuario.usuarioId,
                    email = data.usuario.email,
                    name = "${data.usuario.nombre} ${data.usuario.apellido}",
                    role = data.usuario.rol
                )

                emit(Resource.Success(User(
                    id = data.usuario.usuarioId,
                    email = data.usuario.email,
                    nombre = data.usuario.nombre,
                    apellido = data.usuario.apellido,
                    telefono = data.usuario.telefono,
                    rol = data.usuario.rol
                )))
            } else {
                val message = when (response.code()) {
                    400 -> "Datos inválidos"
                    409 -> "El email ya está registrado"
                    else -> ErrorMessages.ERROR_DESCONOCIDO
                }
                emit(Resource.Error(message))
            }

        } catch (e: HttpException) {
            emit(Resource.Error("${ErrorMessages.ERROR_RED}: ${e.message()}"))
        } catch (e: IOException) {
            emit(Resource.Error(ErrorMessages.ERROR_CONEXION))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: ErrorMessages.ERROR_DESCONOCIDO))
        }
    }

    override suspend fun logout() {
        preferencesManager.clearUserData()
    }

    override fun isLoggedIn(): Flow<Boolean> = flow {
        preferencesManager.getToken().collect { token ->
            emit(!token.isNullOrEmpty())
        }
    }

    override fun getUserData(): Flow<User?> = flow {
        val id = preferencesManager.getUserId().firstOrNull()
        val email = preferencesManager.getUserEmail().firstOrNull()
        val name = preferencesManager.getUserName().firstOrNull()
        val role = preferencesManager.getUserRole().firstOrNull()

        if (id != null && email != null && name != null && role != null) {
            val parts = name.split(" ")
            emit(User(
                id = id,
                email = email,
                nombre = parts.getOrNull(0) ?: "",
                apellido = parts.getOrNull(1) ?: "",
                telefono = null,
                rol = role
            ))
        } else {
            emit(null)
        }
    }

    override fun recoveryPassword(email: String): Flow<Resource<Unit>> =
        flow {
            emit(Resource.Loading())

            val response = apiService.RecoveryPassword(RecoveryRequest(email))

            if (response.isSuccessful) {
                emit(Resource.Success(Unit))
            } else {
                emit(Resource.Error("No se pudo enviar el correo"))
            }

        }.catch {
            emit(Resource.Error(ErrorMessages.ERROR_CONEXION))
        }
}