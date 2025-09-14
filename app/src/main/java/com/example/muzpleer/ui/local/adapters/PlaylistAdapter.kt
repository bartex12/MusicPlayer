package com.example.muzpleer.ui.local.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.muzpleer.R
import com.example.muzpleer.databinding.ItemPlaylistBinding
import com.example.muzpleer.model.Playlist
import com.example.muzpleer.ui.local.viewmodel.SharedViewModel
import com.example.muzpleer.util.getTracksCountString

class PlaylistAdapter(
    private val viewModel: SharedViewModel,
    private val onPlaylistClick: (Playlist) -> Unit
) : RecyclerView.Adapter<PlaylistAdapter.PlaylistViewHolder>() {

    @SuppressLint("NotifyDataSetChanged")
    var playlist: List<Playlist> = listOf()
        set(value) {
            field=value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaylistViewHolder {
        val binding= ItemPlaylistBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return PlaylistViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PlaylistViewHolder, position: Int) {
        holder.bind(playlist[position])
    }

    override fun getItemCount()=playlist.size

    inner class PlaylistViewHolder(private val binding: ItemPlaylistBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(playlist: Playlist) {
            binding.playlistTitle.text=playlist.playlistName
            //todo время треков
            binding.playlistSubtitle.text=getTracksCountString(playlist.playlistSongs.size)

            // Загрузка обложки альбома
            Glide.with(binding.root.context)
                .load(playlist.playlistArtUri)
                .placeholder(R.drawable.muz_player5)
                .error(R.drawable.muz_player5)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(binding.playlistArtwork)

            itemView.setOnClickListener {
                viewModel.setSelectedAlbumPosition(absoluteAdapterPosition)
                onPlaylistClick(playlist)
            }
        }
    }
}
