package mobappdev.example.sensorapplication.persistence

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import mobappdev.example.sensorapplication.persistence.converters.LocalDateTimeConverters
import mobappdev.example.sensorapplication.persistence.converters.MeasurementConverters

@Database(entities = [MeasurementsEntity::class], version = 1, exportSchema = false)
@TypeConverters(MeasurementConverters::class, LocalDateTimeConverters::class)
abstract class MeasurementDB : RoomDatabase() {
    abstract fun getMeasurementsDao(): MeasurementsDao
}