package mobappdev.example.sensorapplication.di

/**
 * File: AppModule.kt
 * Purpose: Defines the implementation of Dagger-Hilt injection.
 * Author: Jitse van Esch
 * Created: 2023-07-08
 * Last modified: 2023-09-21
 */

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import mobappdev.example.sensorapplication.data.AndroidPolarController
import mobappdev.example.sensorapplication.data.CalculationModel
import mobappdev.example.sensorapplication.data.CsvExporter
import mobappdev.example.sensorapplication.data.InternalSensorControllerImpl
import mobappdev.example.sensorapplication.domain.InternalSensorController
import mobappdev.example.sensorapplication.domain.PolarController
import mobappdev.example.sensorapplication.persistence.MeasurementDB
import mobappdev.example.sensorapplication.persistence.MeasurementsDao
import mobappdev.example.sensorapplication.persistence.MeasurementsRepository
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object AppModule {


    @Provides
    @Singleton
    fun provideMeasurementDB(
        @ApplicationContext app: Context
    ) = Room.databaseBuilder(
        app,
        MeasurementDB::class.java,
        "measurementsDB"
    ).fallbackToDestructiveMigration().build()

    @Singleton
    @Provides
    fun provideMeasurementsDao(db: MeasurementDB) = db.getMeasurementsDao()

    @Provides
    @Singleton
    fun provideMeasurementRepository(dao: MeasurementsDao): MeasurementsRepository {
        return MeasurementsRepository(dao)
    }

    @Provides
    @Singleton
    fun provideCalculationModel() : CalculationModel {
        return CalculationModel()
    }

    @Provides
    @Singleton
    fun providePolarController(@ApplicationContext context: Context, calculationModel: CalculationModel): PolarController {
        return AndroidPolarController(context, calculationModel)
    }

    @Provides
    @Singleton
    fun provideCsvExporter(@ApplicationContext context: Context): CsvExporter {
        return CsvExporter(context)
    }

    //@Provides
    //@Singleton
    //fun provideCsvExporter(): CsvExporter {
    //    return CsvExporter(context)
    //}

    @Provides
    @Singleton
    fun provideInternalSensorController(@ApplicationContext context: Context, calculationModel: CalculationModel): InternalSensorController {
        return InternalSensorControllerImpl(context, calculationModel)
    }


}