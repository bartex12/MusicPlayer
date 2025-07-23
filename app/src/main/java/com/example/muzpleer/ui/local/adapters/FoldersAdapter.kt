package com.example.muzpleer.ui.local.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.muzpleer.databinding.ItemFolderBinding
import com.example.muzpleer.model.AudioFolder
import com.example.muzpleer.model.MusicAlbum
import com.example.muzpleer.R
import com.example.muzpleer.databinding.ItemAlbumBinding
import com.example.muzpleer.ui.local.adapters.AlbumsAdapter.AlbumViewHolder

class FoldersAdapter(
    private val onFolderClick: (AudioFolder) -> Unit
) : RecyclerView.Adapter<FoldersAdapter.FolderViewHolder>() {

    var folders:List<AudioFolder> = listOf()
        @SuppressLint("NotifyDataSetChanged")
        set(value){
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FolderViewHolder {
        val binding = ItemFolderBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return FolderViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FolderViewHolder, position: Int) {
        holder.bind(folders[position])
    }

    override fun getItemCount() = folders.size

    inner class FolderViewHolder(private val binding: ItemFolderBinding)
        : RecyclerView.ViewHolder(binding.root) {

        fun bind(folder: AudioFolder) {
            binding.tvFolderName.text = folder.name
            binding.tvFolderPath.text = folder.path
            binding.tvTracksCount.text = "${folder.tracks.size} треков"

            // Загрузка обложки первого трека
            folder.artworkUri?.let { uri ->
                Glide.with(binding.root.context)
                    .load(uri)
                    .placeholder(R.drawable.gimme)
                    .into(binding.ivFolderIcon)
            }

            binding.root.setOnClickListener { onFolderClick(folder) }
        }
    }
}