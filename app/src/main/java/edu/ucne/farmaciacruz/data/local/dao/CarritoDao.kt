package edu.ucne.farmaciacruz.data.local.dao

import androidx.room.*
import edu.ucne.farmaciacruz.data.local.entity.CarritoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CarritoDao {

    @Query("SELECT * FROM carrito WHERE usuarioId = :usuarioId ORDER BY fechaAgregado DESC")
    fun getCarritoByUsuario(usuarioId: Int): Flow<List<CarritoEntity>>

    @Query("SELECT * FROM carrito WHERE usuarioId = :usuarioId AND productoId = :productoId")
    suspend fun getCarritoItem(usuarioId: Int, productoId: Int): CarritoEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCarritoItem(item: CarritoEntity)

    @Update
    suspend fun updateCarritoItem(item: CarritoEntity)

    @Delete
    suspend fun deleteCarritoItem(item: CarritoEntity)

    @Query("DELETE FROM carrito WHERE usuarioId = :usuarioId AND productoId = :productoId")
    suspend fun deleteByProductoId(usuarioId: Int, productoId: Int)

    @Query("DELETE FROM carrito WHERE usuarioId = :usuarioId")
    suspend fun clearCarrito(usuarioId: Int)

    @Query("SELECT SUM(cantidad) FROM carrito WHERE usuarioId = :usuarioId")
    fun getTotalItems(usuarioId: Int): Flow<Int?>
}