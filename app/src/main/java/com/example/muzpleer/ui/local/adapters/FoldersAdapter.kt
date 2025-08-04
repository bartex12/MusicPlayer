package com.example.muzpleer.ui.local.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.muzpleer.databinding.ItemFolderBinding
import com.example.muzpleer.model.Folder
import com.example.muzpleer.R

class FoldersAdapter(
    private val onFolderClick: (Folder) -> Unit
) : RecyclerView.Adapter<FoldersAdapter.FolderViewHolder>() {

    var folders:List<Folder> = listOf()
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

        fun bind(folder: Folder) {
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