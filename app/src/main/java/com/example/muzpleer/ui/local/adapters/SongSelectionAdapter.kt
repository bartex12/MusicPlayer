package com.example.muzpleer.ui.local.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.muzpleer.R
import com.example.muzpleer.databinding.ItemSongSelectionBinding
import com.example.muzpleer.model.Song
import com.example.muzpleer.util.formatDuration

class SongSelectionAdapter(
    private val onSelectionChanged: (List<Song>) -> Unit
) :  ListAdapter<Song, SongSelectionAdapter.SongSelectionViewHolder>(
    object : DiffUtil.ItemCallback<Song>() {
        override fun areItemsTheSame(oldItem: Song, newItem: Song): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Song, newItem: Song): Boolean {
            return oldItem == newItem
        }
    }
) {
    private val selectedSongs = mutableSetOf<Long>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongSelectionViewHolder {
        val binding = ItemSongSelectionBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return SongSelectionViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SongSelectionViewHolder, position: Int) {
        val song = getItem(position)
        holder.bind(song, selectedSongs.contains(song.id))
    }

    fun getSelectedSongs(): List<Song> {
        return currentList.filter { selectedSongs.contains(it.id) }
    }

    fun selectAll() {
        selectedSongs.clear()
        selectedSongs.addAll(currentList.map { it.id })
        notifyDataSetChanged()
        onSelectionChanged(getSelectedSongs())
    }

    inner class SongSelectionViewHolder(private val binding: ItemSongSelectionBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(song: Song, isSelected: Boolean) {
            binding.songTitle.text = song.title
            binding.songArtist.text = song.artist
            binding.songDuration.text = formatDuration(song.duration)

            // Загрузка обложки
            Glide.with(binding.root.context)
                .load(song.artUri)
                .placeholder(R.drawable.muz_player5)
                .error(R.drawable.muz_player5)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(binding.songArtwork)

            // Устанавливаем состояние чекбокса БЕЗ вызова слушателя
            binding.checkbox.setOnCheckedChangeListener(null) // Сначала удаляем слушатель
            binding.checkbox.isChecked = isSelected

            binding.checkbox.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    selectedSongs.add(song.id)
                } else {
                    selectedSongs.remove(song.id)
                }
                onSelectionChanged(getSelectedSongs())
            }

            itemView.setOnClickListener {
                binding.checkbox.isChecked = !binding.checkbox.isChecked
            }
        }
    }
}