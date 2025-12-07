package com.example.muzpleer.ui.local.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.muzpleer.R
import com.example.muzpleer.databinding.ItemPlaylistChooseBinding
import com.example.muzpleer.model.Playlist

class PlaylistChooseAdapter(
    private val onPlaylistClick: (Playlist) -> Unit
) : RecyclerView.Adapter<PlaylistChooseAdapter.PlaylistViewHolder>() {

    var playlists: List<Playlist> = listOf()
        @SuppressLint("NotifyDataSetChanged")
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaylistViewHolder {
        val binding = ItemPlaylistChooseBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return PlaylistViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PlaylistViewHolder, position: Int) {
        holder.bind(playlists[position])
    }

    override fun getItemCount(): Int = playlists.size

    inner class PlaylistViewHolder(
        private val binding: ItemPlaylistChooseBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(playlist: Playlist) {
            binding.tvPlaylistName.text = playlist.playlistName
            binding.tvSongsCount.text = "${playlist.songCount} песен"

            // Загрузка обложки плейлиста
            playlist.playlistArtUri?.let { uri ->
                Glide.with(binding.root.context)
                    .load(uri)
                    .placeholder(R.drawable.muz_player3)
                    .error(R.drawable.muz_player3)
                    .into(binding.ivPlaylistArt)
            } ?: run {
                binding.ivPlaylistArt.setImageResource(R.drawable.muz_player3)
            }

            binding.root.setOnClickListener {
                onPlaylistClick(playlist)
            }
        }
    }
}