package com.example.musicplayer.ui

import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.exoplayer.ExoPlayer
import com.example.musicplayer.audio.AudioHandler
import com.example.musicplayer.audio.PlayerEvent
import com.example.musicplayer.audio.SimpleMediaState
import dagger.assisted.Assisted
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class AudioViewModel @Inject constructor(
    private val audioHandler: AudioHandler,
    private val player: ExoPlayer,
    private val sharedPreferences: SharedPreferences
) : ViewModel() {
    companion object {
        private const val DURATION_KEY = "duration"
        private const val PROGRESS_KEY = "progress"
        private const val PROGRESS_STRING_KEY = "progressString"
        private const val IS_PLAYING_KEY = "isPlaying"
    }

    init {
        Log.d("TAG", "isPlaying: ${player.isPlaying} ")
        Log.d("TAG", "currentPosition: ${player.currentPosition} ")
    }

    var duration: Long = 0L
    var progress: Float = 0f
    var progressString: String = "00:00"
    var isPlaying: Boolean = false

    fun updateDuration(duration: Long) {
        this@AudioViewModel.duration = duration

    }

    fun updateProgress(progress: Float) {
        this@AudioViewModel.progress = progress

    }

    fun updateProgressString(progressString: String) {
        this@AudioViewModel.progressString = progressString

        Log.d("NPCM", "updateProgressString: ${sharedPreferences.getString(PROGRESS_STRING_KEY,"")}")
    }

    fun updateIsPlaying(isPlaying: Boolean) {
        this@AudioViewModel.isPlaying = isPlaying

    }


    private val _uiState = MutableLiveData<UIState>()
    val uiState: LiveData<UIState> = _uiState

    private val _uiEvent = MutableLiveData<UIEvent>()
    val uiEvent: LiveData<UIEvent> = _uiEvent

    init {

        duration = sharedPreferences.getLong(DURATION_KEY, 0L)
        progress = sharedPreferences.getFloat(PROGRESS_KEY, 0f)
        progressString = sharedPreferences.getString(PROGRESS_STRING_KEY,"00:00").toString()
        isPlaying = sharedPreferences.getBoolean(IS_PLAYING_KEY,false)

        Log.d("NPCM", "restore :  $progressString + $isPlaying")
        viewModelScope.launch {
            loadData()

            audioHandler.simpleMediaState.collect { mediaState ->
                when (mediaState) {
                    is SimpleMediaState.Buffering -> calculateProgressValues(mediaState.progress)
                    is SimpleMediaState.Initial -> _uiState.value = UIState.Initial
                    is SimpleMediaState.Playing -> {
                        isPlaying = mediaState.isPlaying
                        Log.d("NPCM", "state playing :$isPlaying ")
                    }
                    is SimpleMediaState.Progress -> calculateProgressValues(mediaState.progress)
                    is SimpleMediaState.Ready -> {
                        duration = mediaState.duration
                        _uiState.value = UIState.Ready
                    }
                }
            }
        }


    }

    override fun onCleared() {
        viewModelScope.launch {
            audioHandler.onPlayerEvent(PlayerEvent.Stop)
        }
    }

    fun onUIEvent(uiEvent: UIEvent) = viewModelScope.launch {
        when (uiEvent) {
            UIEvent.Backward -> audioHandler.onPlayerEvent(PlayerEvent.Backward)
            UIEvent.Forward -> audioHandler.onPlayerEvent(PlayerEvent.Forward)
            UIEvent.PlayPause -> audioHandler.onPlayerEvent(PlayerEvent.PlayPause)
            is UIEvent.UpdateProgress -> {
                progress = uiEvent.newProgress
                audioHandler.onPlayerEvent(
                    PlayerEvent.UpdateProgress(
                        uiEvent.newProgress
                    )
                )

                Log.d("NPCM", "PROGRESS playing :$progress ")
            }
        }
    }

    fun formatDuration(duration: Long): String {
        val minutes: Long = TimeUnit.MINUTES.convert(duration, TimeUnit.MILLISECONDS)
        val seconds: Long = (TimeUnit.SECONDS.convert(duration, TimeUnit.MILLISECONDS)
                - minutes * TimeUnit.SECONDS.convert(1, TimeUnit.MINUTES))
        return String.format("%02d:%02d", minutes, seconds)
    }

    private fun calculateProgressValues(currentProgress: Long) {
        if (duration!=null) {
            progress = if (currentProgress > 0) (currentProgress.toFloat() / duration) else 0f
            progressString = formatDuration(currentProgress)
            Log.d("NPCM", "calculateProgressValues: $progressString")
        }
    }

    private fun loadData() {
        val mediaItem = MediaItem.Builder()
            .setUri("https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3")
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setFolderType(MediaMetadata.FOLDER_TYPE_ALBUMS)
                    .setArtworkUri(Uri.parse("https://i.pinimg.com/736x/4b/02/1f/4b021f002b90ab163ef41aaaaa17c7a4.jpg"))
                    .setAlbumTitle("SoundHelix")
                    .setDisplayTitle("Song 1")
                    .build()
            ).build()

        audioHandler.addMediaItem(mediaItem)
    }
//    fun saveState(outState: Bundle) {
//        outState.putLong(DURATION_KEY, duration)
//        outState.putFloat(PROGRESS_KEY, progress)
//        outState.putString(PROGRESS_STRING_KEY, progressString)
//        outState.putBoolean(IS_PLAYING_KEY, isPlaying)
//    }
////
//    fun restoreState(savedInstanceState: Bundle?) {
//        duration = savedInstanceState?.getLong(DURATION_KEY) ?: 0L
//        progress = savedInstanceState?.getFloat(PROGRESS_KEY) ?: 0f
//        progressString = savedInstanceState?.getString(PROGRESS_STRING_KEY) ?: "00:00"
//        isPlaying = savedInstanceState?.getBoolean(IS_PLAYING_KEY) ?: false
//    }

}

sealed class UIEvent {
    object PlayPause : UIEvent()
    object Backward : UIEvent()
    object Forward : UIEvent()
    data class UpdateProgress(val newProgress: Float) : UIEvent()
}

sealed class UIState {
    object Initial : UIState()
    object Ready : UIState()
}