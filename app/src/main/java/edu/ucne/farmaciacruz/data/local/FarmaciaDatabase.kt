package edu.ucne.farmaciacruz.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import edu.ucne.farmaciacruz.data.local.dao.CarritoDao
import edu.ucne.farmaciacruz.data.local.entity.CarritoEntity

@Database(
    entities = [CarritoEntity::class],
    version = 1,
    exportSchema = false
)
abstract class FarmaciaDatabase : RoomDatabase() {
    abstract fun carritoDao(): CarritoDao
}