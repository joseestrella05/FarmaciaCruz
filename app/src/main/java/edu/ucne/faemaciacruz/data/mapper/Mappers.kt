package edu.ucne.faemaciacruz.data.mapper

import edu.ucne.faemaciacruz.data.remote.dto.AuthResponseDto
import edu.ucne.faemaciacruz.data.remote.dto.ProductoDto
import edu.ucne.faemaciacruz.data.remote.dto.UsuarioDto
import edu.ucne.faemaciacruz.domain.model.Producto
import edu.ucne.faemaciacruz.domain.model.User


fun UsuarioDto.toDomain(): User {
    return User(
        id = this.usuarioId,
        email = this.email,
        nombre = this.nombre,
        apellido = this.apellido,
        telefono = this.telefono,
        rol = this.rol
    )
}

fun AuthResponseDto.toUserDomain(): User {
    return this.usuario.toDomain()
}

fun ProductoDto.toDomain(): Producto {
    return Producto(
        id = this.productoId,
        nombre = this.nombre,
        categoria = this.categoria,
        descripcion = this.descripcion,
        precio = this.precio,
        imagenUrl = this.imagenUrl
    )
}

fun Producto.toDto(): ProductoDto {
    return ProductoDto(
        productoId = this.id,
        nombre = this.nombre,
        categoria = this.categoria,
        descripcion = this.descripcion,
        precio = this.precio,
        imagenUrl = this.imagenUrl
    )
}


fun List<ProductoDto>.toDomain(): List<Producto> {
    return this.map { it.toDomain() }
}

fun List<Producto>.toDto(): List<ProductoDto> {
    return this.map { it.toDto() }
}