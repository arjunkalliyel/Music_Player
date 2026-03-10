package com.example.musicplayer.screens

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.musicplayer.R
import com.example.musicplayer.adapter.MusicListAdapter
import com.example.musicplayer.repository.MusicRepository


class MusicListFragment : Fragment() {

    private lateinit var repository: MusicRepository

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val view = inflater.inflate(R.layout.fragment_music_list, container, false)

        repository = MusicRepository(requireContext())

        val tracks = repository.getTracks()

        val recyclerView = view.findViewById<RecyclerView>(R.id.rv_library)

        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        recyclerView.adapter = MusicListAdapter(tracks) { position ->

            val intent = Intent(requireContext(), MainActivity::class.java)
            intent.putExtra("TRACK_INDEX", position)
            startActivity(intent)
        }

        return view
    }
}