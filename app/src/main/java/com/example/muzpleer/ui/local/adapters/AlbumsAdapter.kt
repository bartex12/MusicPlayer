package com.example.muzpleer.ui.local.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.muzpleer.databinding.ItemAlbumBinding
import com.example.muzpleer.model.Album
import com.bumptech.glide.Glide
import com.example.muzpleer.R

class AlbumsAdapter(
    private val onAlbumClick: (Album) -> Unit
) : RecyclerView.Adapter<AlbumsAdapter.AlbumViewHolder>() {

    var albums:List<Album> = listOf()
        @SuppressLint("NotifyDataSetChanged")
        set(value){
            field = value
            notifyDataSetChanged()
        }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlbumViewHolder {
        val binding = ItemAlbumBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return AlbumViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AlbumViewHolder, position: Int) {
        holder.bind(albums[position])
    }

    override fun getItemCount() = albums.size

    inner class AlbumViewHolder(private val binding: ItemAlbumBinding)
        : RecyclerView.ViewHolder(binding.root) {

        fun bind(album: Album) {
            binding.albumTitle.text = album.title
            binding.albumArtist.text = album.artist
            binding.tracksCount.text = "${album.songs.size} треков"

            // Загрузка обложки альбома
            Glide.with(binding.root.context)
                .load(album.artworkUri)
                .placeholder(R.drawable.placeholder1024)
                .into(binding.albumArt)

            itemView.setOnClickListener { onAlbumClick(album) }
        }
    }
}