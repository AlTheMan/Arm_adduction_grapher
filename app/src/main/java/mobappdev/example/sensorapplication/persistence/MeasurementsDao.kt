package mobappdev.example.sensorapplication.persistence

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import mobappdev.example.sensorapplication.data.AngleMeasurements.Measurement
import mobappdev.example.sensorapplication.persistence.dto.MeasurementDTO
import mobappdev.example.sensorapplication.persistence.dto.MeasurementSummary


@Dao
interface MeasurementsDao {
    @Insert
    suspend fun insert(entity: MeasurementsEntity)

    @Query("SELECT count(id) FROM measurements")
    suspend fun numberOfMeasurements() : Int

    @Query("SELECT * FROM measurements")
    suspend fun getAll() : List<MeasurementsEntity>

    @Query("SELECT id, type, timeOfMeasurement FROM measurements")
    fun getAllSummary() : Flow<List<MeasurementSummary>>

    @Query("SELECT measurements FROM measurements WHERE id = :id")
    fun getMeasurement(id: Int) : String

    @Query("DELETE FROM measurements WHERE id = :id")
    suspend fun deleteById(id: Int)

}