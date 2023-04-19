package com.example.musicplayer.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.example.musicplayer.R
import com.example.musicplayer.databinding.FragmentAudioListBinding

class AudioListFragment : Fragment() {

    private lateinit var binding: FragmentAudioListBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View{
        // Inflate the layout for this fragment
        binding = FragmentAudioListBinding.inflate(layoutInflater,container,false)
        binding.audio1Id.setOnClickListener {
            findNavController().navigate(R.id.action_audioListFragment_to_audioFragment)
        }
        return binding.root
    }
}