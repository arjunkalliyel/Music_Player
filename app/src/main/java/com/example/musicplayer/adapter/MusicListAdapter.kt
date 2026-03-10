package com.example.musicplayer.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.musicplayer.R
import com.example.musicplayer.databinding.ItemMusicBinding
import com.example.musicplayer.model.AudioTrack

class MusicListAdapter(
    private val tracks: List<AudioTrack>,
    private val onClick: (Int) -> Unit
) : RecyclerView.Adapter<MusicListAdapter.ItemViewHolder>() {

    inner class ItemViewHolder(val binding: ItemMusicBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(track: AudioTrack) {

            binding.songTitle.text = track.title
            binding.songArtist.text = track.artist

            Glide.with(binding.root.context)
                .load(track.albumArt)
                .placeholder(R.drawable.ic_placeholder)
                .into(binding.ivAlbumIcon)

            binding.root.setOnClickListener {
                onClick(adapterPosition)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {

        val binding = ItemMusicBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )

        return ItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.bind(tracks[position])
    }

    override fun getItemCount(): Int {
        return tracks.size
    }
}
