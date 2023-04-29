package com.example.musicplayer.ui

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.fragment.app.viewModels
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import com.example.musicplayer.R
import com.example.musicplayer.databinding.FragmentAudioBinding
import com.example.musicplayer.service.SimpleMediaService
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import javax.inject.Inject

@AndroidEntryPoint
class  AudioFragment : Fragment() {

    private  val viewModel : AudioViewModel by viewModels()

    @Inject
    lateinit var player: ExoPlayer

    private lateinit var binding: FragmentAudioBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View{
        // Inflate the layout for this fragment
        binding = FragmentAudioBinding.inflate(layoutInflater,container,false)
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.uiState.observe(viewLifecycleOwner){
            when (it){
                UIState.Initial -> {
                  //  Log.d("NPCM", "onViewCreated: INITIAL")
                }
                UIState.Ready -> {
                    //Log.d("NPCM", "onViewCreated: READY")
                }
            }
        }

        binding.btnPlay.setOnClickListener {
            viewModel.onUIEvent(UIEvent.PlayPause)
            val intent = Intent(requireContext(), SimpleMediaService::class.java)
            requireContext().startForegroundService(intent)
        }
        binding.btnNext.setOnClickListener {
            viewModel.onUIEvent(UIEvent.Forward)
        }
        binding.btnPrev.setOnClickListener {
            viewModel.onUIEvent(UIEvent.Backward)
        }
    }



    override fun onPause() {
        super.onPause()
        val duration = viewModel.duration
        val progress = viewModel.progress
        val progressString = viewModel.progressString
        val isPlaying = viewModel.isPlaying

        Log.d("NPCM", "save :  $progressString + $isPlaying")

        if (duration != null) {
            viewModel.updateDuration(duration)
        }
        if (progress != null) {
            viewModel.updateProgress(progress)
        }
        if (progressString != null) {
            viewModel.updateProgressString(progressString)
        }
        if (isPlaying != null) {
            viewModel.updateIsPlaying(isPlaying)
        }
    }

//    override fun onPause() {
//        super.onPause()
//        viewModel.duration =
//    }

}