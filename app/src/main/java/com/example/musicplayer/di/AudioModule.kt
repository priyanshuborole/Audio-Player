package com.example.musicplayer.di

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.preference.PreferenceManager
import androidx.annotation.RequiresApi
import androidx.lifecycle.SavedStateHandle
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import com.example.musicplayer.audio.AudioHandler
import com.example.musicplayer.service.notification.SimpleMediaNotificationManager
import com.example.musicplayer.ui.AudioViewModel
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class AudioModule {

    @Provides
    @Singleton
    fun providePlayer(
        @ApplicationContext context: Context
    ): ExoPlayer =
        ExoPlayer.Builder(context)
            .build()

    @Provides
    @Singleton
    fun provideSharedPreferences(@ApplicationContext context: Context): SharedPreferences {
        return context.getSharedPreferences("my_app_prefs", Context.MODE_PRIVATE)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @Provides
    @Singleton
    fun provideNotificationManager(
        @ApplicationContext context: Context,
        player: ExoPlayer
    ): SimpleMediaNotificationManager =
        SimpleMediaNotificationManager(
            context = context,
            player = player
        )

    @Provides
    @Singleton
    fun provideMediaSession(
        @ApplicationContext context: Context,
        player: ExoPlayer
    ): MediaSession =
        MediaSession.Builder(context, player).build()

    @Provides
    @Singleton
    fun provideServiceHandler(
        player: ExoPlayer
    ): AudioHandler =
        AudioHandler(
            player = player
        )

}