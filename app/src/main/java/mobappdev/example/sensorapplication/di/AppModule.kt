package mobappdev.example.sensorapplication.di

/**
 * File: AppModule.kt
 * Purpose: Defines the implementation of Dagger-Hilt injection.
 * Author: Jitse van Esch
 * Created: 2023-07-08
 * Last modified: 2023-09-21
 */

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import mobappdev.example.sensorapplication.data.AndroidPolarController
import mobappdev.example.sensorapplication.data.InternalSensorControllerImpl
import mobappdev.example.sensorapplication.domain.InternalSensorController
import mobappdev.example.sensorapplication.domain.PolarController
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun providePolarController(@ApplicationContext context: Context): PolarController {
        return AndroidPolarController(context)
    }

    @Provides
    @Singleton
    fun provideInternalSensorController(@ApplicationContext context: Context): InternalSensorController {
        return InternalSensorControllerImpl(context)
    }
}