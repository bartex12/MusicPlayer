package com.example.muzpleer.ui.local.adapters

import android.annotation.SuppressLint
import android.content.ContentUris
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.muzpleer.R
import com.example.muzpleer.databinding.ItemArtistBinding
import com.example.muzpleer.model.Artist
import com.example.muzpleer.util.getAlbumsCountString
import com.example.muzpleer.util.getTracksCountString

class ArtistsAdapter(
    private val onItemClick: (Artist) -> Unit
) : RecyclerView.Adapter<ArtistsAdapter.ArtistViewHolder>() {

    var data:List<Artist> = listOf()
        @SuppressLint("NotifyDataSetChanged")
        set(value){
            field = value
            notifyDataSetChanged()
        }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArtistViewHolder {
        val binding = ItemArtistBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ArtistViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: ArtistViewHolder,
        position: Int
    ) {
        holder.bind(data[position])
    }

    override fun getItemCount(): Int {
        return data.size
    }

    inner class ArtistViewHolder(private val binding: ItemArtistBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(artist: Artist) {
            binding.tvArtistName.text = artist.name
            binding.tvTracksCount.text =buildString {
                append("${getAlbumsCountString(artist.albums.size)}, ")
                append(getTracksCountString(artist.songs.size))
            }
            // Загружаем обложку, если есть
            val albumArtUri = ContentUris.withAppendedId(
                "content://media/external/audio/albumart".toUri(),
                artist.albums.firstOrNull()?.albumId ?: -1
            )

            // Загрузка обложки
            Glide.with(binding.root.context)
                .load(albumArtUri)
                .placeholder(R.drawable.muz_player2)
                .error(R.drawable.muz_player2)
                .into(binding.ivArtistArtwork)

            binding.root.setOnClickListener { onItemClick(artist) }
        }
    }
}