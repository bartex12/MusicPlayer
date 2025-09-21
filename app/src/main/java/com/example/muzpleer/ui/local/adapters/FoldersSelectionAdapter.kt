package com.example.muzpleer.ui.local.adapters

import android.annotation.SuppressLint
import android.content.ContentUris
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.muzpleer.R
import com.example.muzpleer.databinding.ItemAlbumSelectionBinding
import com.example.muzpleer.model.Artist
import com.example.muzpleer.model.Folder
import com.example.muzpleer.ui.local.viewmodel.SharedViewModel
import com.example.muzpleer.util.getTracksCountString

class FoldersSelectionAdapter (
    private val viewModel: SharedViewModel,
    private val onFolderClick: (Folder) -> Unit
) : RecyclerView.Adapter<FoldersSelectionAdapter.AlbumViewHolder>() {

    var folders:List<Folder> = listOf()
        @SuppressLint("NotifyDataSetChanged")
        set(value){
            field = value
            notifyDataSetChanged()
        }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlbumViewHolder {
        val binding = ItemAlbumSelectionBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return AlbumViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AlbumViewHolder, position: Int) {
        holder.bind(folders[position])

        // Следим за изменениями выбранной позиции
        viewModel.selectedAlbumPosition
            .observe(holder.itemView.context as LifecycleOwner) { selectedPos ->
                holder.itemView.isSelected = position == selectedPos
            }
    }

    override fun getItemCount() = folders.size

    inner class AlbumViewHolder(private val binding: ItemAlbumSelectionBinding)
        : RecyclerView.ViewHolder(binding.root) {

        fun bind(folder: Folder) {
            binding.albumTitleSelection.text = folder.name
            binding.albumArtistSelection.text = folder.path
            binding.tracksCountSelection.text = getTracksCountString( folder.songs.size)

            // Загружаем обложку, если есть
            val albumArtUri = ContentUris.withAppendedId(
                "content://media/external/audio/albumart".toUri(),
                folder.songs.firstOrNull()?.albumId ?: -1
            )

            // Загрузка обложки альбома
            Glide.with(binding.root.context)
                .load(albumArtUri)
                .placeholder(R.drawable.muz_player5)
                .error(R.drawable.muz_player5)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(binding.albumArtSelection)

            itemView.setOnClickListener {
                viewModel.setSelectedAlbumPosition(absoluteAdapterPosition)
                onFolderClick(folder)
            }
        }
    }
}