package com.example.muzpleer.ui.local.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.muzpleer.R
import com.example.muzpleer.databinding.ItemArtistBinding
import com.example.muzpleer.model.Artist

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
            binding.tvTracksCount.text = " Песен: ${artist.tracks.size}"

            // Загрузка обложки
            Glide.with(binding.root.context)
                .load(artist.artworkUri)
                .placeholder(R.drawable.placeholder2)
                .error(R.drawable.placeholder2)
                .into(binding.ivArtistArtwork)

            binding.root.setOnClickListener { onItemClick(artist) }
        }
    }
}