package com.queueshub.data.di


import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import com.queueshub.data.local.Local
import com.queueshub.data.local.LocalSource
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object PreferencesModule {

    @Provides
    @Singleton
    fun provideSharedPreference(@ApplicationContext context: Context): SharedPreferences {
        return PreferenceManager.getDefaultSharedPreferences(context)
    }
    @Provides
    @Singleton
    fun provideLocalSource(local: Local): LocalSource = local

}