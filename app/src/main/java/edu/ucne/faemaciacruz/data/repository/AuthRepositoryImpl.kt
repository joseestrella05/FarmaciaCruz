package edu.ucne.faemaciacruz.data.repository

import edu.ucne.faemaciacruz.data.local.PreferencesManager
import edu.ucne.faemaciacruz.data.remote.api.ApiService
import edu.ucne.faemaciacruz.data.remote.request.LoginRequest
import edu.ucne.faemaciacruz.data.remote.request.RegisterRequest
import edu.ucne.faemaciacruz.domain.model.Resource
import edu.ucne.faemaciacruz.domain.model.User
import edu.ucne.faemaciacruz.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
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

    override suspend fun login(email: String, password: String): Flow<Resource<User>> = flow {
        try {
            emit(Resource.Loading())

            val response = apiService.login(LoginRequest(email, password))

            if (response.isSuccessful && response.body() != null) {
                val authResponse = response.body()!!.data!!

                // Guardar tokens
                preferencesManager.saveToken(authResponse.token)
                preferencesManager.saveRefreshToken(authResponse.refreshToken)

                // Guardar datos del usuario
                preferencesManager.saveUserData(
                    userId = authResponse.usuario.usuarioId,
                    email = authResponse.usuario.email,
                    name = "${authResponse.usuario.nombre} ${authResponse.usuario.apellido}",
                    role = authResponse.usuario.rol
                )

                val user = User(
                    id = authResponse.usuario.usuarioId,
                    email = authResponse.usuario.email,
                    nombre = authResponse.usuario.nombre,
                    apellido = authResponse.usuario.apellido,
                    telefono = authResponse.usuario.telefono,
                    rol = authResponse.usuario.rol
                )

                emit(Resource.Success(user))
            } else {
                val errorMessage = when (response.code()) {
                    401 -> "Credenciales inválidas"
                    403 -> "Cuenta desactivada"
                    429 -> "Demasiados intentos. Intenta más tarde"
                    else -> response.body()?.mensaje ?: "Error al iniciar sesión"
                }
                emit(Resource.Error(errorMessage))
            }
        } catch (e: HttpException) {
            emit(Resource.Error("Error de red: ${e.message()}"))
        } catch (e: IOException) {
            emit(Resource.Error("Error de conexión. Verifica tu internet"))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Error desconocido"))
        }
    }

    override suspend fun register(
        email: String,
        password: String,
        nombre: String,
        apellido: String,
        telefono: String?
    ): Flow<Resource<User>> = flow {
        try {
            emit(Resource.Loading())

            val request = RegisterRequest(
                email = email,
                password = password,
                nombre = nombre,
                apellido = apellido,
                telefono = telefono
            )

            val response = apiService.register(request)

            if (response.isSuccessful && response.body() != null) {
                val authResponse = response.body()!!.data!!

                // Guardar tokens
                preferencesManager.saveToken(authResponse.token)
                preferencesManager.saveRefreshToken(authResponse.refreshToken)

                // Guardar datos del usuario
                preferencesManager.saveUserData(
                    userId = authResponse.usuario.usuarioId,
                    email = authResponse.usuario.email,
                    name = "${authResponse.usuario.nombre} ${authResponse.usuario.apellido}",
                    role = authResponse.usuario.rol
                )

                val user = User(
                    id = authResponse.usuario.usuarioId,
                    email = authResponse.usuario.email,
                    nombre = authResponse.usuario.nombre,
                    apellido = authResponse.usuario.apellido,
                    telefono = authResponse.usuario.telefono,
                    rol = authResponse.usuario.rol
                )

                emit(Resource.Success(user))
            } else {
                val errorMessage = when (response.code()) {
                    400 -> "Datos inválidos"
                    409 -> "El email ya está registrado"
                    else -> "Error al registrarse"
                }
                emit(Resource.Error(errorMessage))
            }
        } catch (e: HttpException) {
            emit(Resource.Error("Error de red: ${e.message()}"))
        } catch (e: IOException) {
            emit(Resource.Error("Error de conexión"))
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Error desconocido"))
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
        val userId = preferencesManager.getUserId()
        val email = preferencesManager.getUserEmail()
        val name = preferencesManager.getUserName()
        val role = preferencesManager.getUserRole()

        // Combinar flows
        userId.collect { id ->
            if (id != null) {
                email.collect { mail ->
                    name.collect { n ->
                        role.collect { r ->
                            if (mail != null && n != null && r != null) {
                                val names = n.split(" ")
                                emit(User(
                                    id = id,
                                    email = mail,
                                    nombre = names.getOrNull(0) ?: "",
                                    apellido = names.getOrNull(1) ?: "",
                                    telefono = null,
                                    rol = r
                                ))
                            }
                        }
                    }
                }
            } else {
                emit(null)
            }
        }
    }
}