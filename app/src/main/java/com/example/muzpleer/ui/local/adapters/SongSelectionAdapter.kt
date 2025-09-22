package com.example.muzpleer.ui.local.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.net.toUri
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.muzpleer.R
import com.example.muzpleer.databinding.ItemSongSelectionBinding
import com.example.muzpleer.model.Song
import com.example.muzpleer.util.formatDuration
import java.io.File

//адаптер для песен, из которых будут выбираться песни для добавления в плейлист
//так как не все обложки отображаются как надо, использован сложный вариант
// загрузки обложек через showImageWithGlide
class SongSelectionAdapter (
    private val onSelectionChanged: (List<Song>) -> Unit
) : RecyclerView.Adapter<SongSelectionAdapter.SongSelectionViewHolder>() {

    companion object{
        const val TAG = "33333"
    }

    var data:List<Song> = listOf()
        @SuppressLint("NotifyDataSetChanged")
        set(value){
            field = value
            notifyDataSetChanged()
        }

    private val selectedSongs = mutableSetOf<Long>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongSelectionViewHolder {
        val binding = ItemSongSelectionBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return SongSelectionViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: SongSelectionViewHolder,
        position: Int
    ) {
        val song = data[position]
        holder.bind(song, selectedSongs.contains(song.id))
    }

    override fun getItemCount(): Int {
        return data.size
    }

    fun getSelectedSongs(): List<Song> {
        return data.filter { selectedSongs.contains(it.id) }
    }

    fun selectAll() {
        selectedSongs.clear()
        selectedSongs.addAll(data.map { it.id })
        notifyDataSetChanged()
        onSelectionChanged(getSelectedSongs())
    }

    inner class SongSelectionViewHolder(private val binding: ItemSongSelectionBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(song: Song, isSelected: Boolean) {

            binding.songTitle.text = song.title
            binding.songArtist.text = song.artist
            binding.songDuration.text = formatDuration(song.duration)

            //из-за того, что обложки не отображаются, как во вкладках, приходится использовать более сложный код
            showImageWithGlide(binding.root.context, song.artUri?.toUri(), binding.songArtwork)

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

    fun showImageWithGlide(context: Context, artUri: Uri?, imageView: ImageView) {
        when {
            artUri == null -> {
                // Загрузка стандартной обложки
                Glide.with(context)
                    .load(R.drawable.muz_player3)
                    .into(imageView)
            }
            artUri.scheme == "content" && artUri.authority == "com.android.providers.downloads.documents" -> {
                // Обработка специальных content URI
                loadDownloadDocumentUri(context, artUri, imageView)
            }
            artUri.toString().contains("albumart") -> {
                // Стандартные album art URI
                Glide.with(context)
                    .load(artUri)
                    .placeholder(R.drawable.muz_player3)
                    .error(R.drawable.muz_player3)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(imageView)
            }
            else -> {
                // Все остальные URI
                Glide.with(context)
                    .load(artUri)
                    .placeholder(R.drawable.muz_player3)
                    .error(R.drawable.muz_player3)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(imageView)
            }
        }
    }

    private fun loadDownloadDocumentUri(context: Context, uri: Uri, imageView: ImageView) {
        try {
            // Пытаемся получить реальный путь файла
            val filePath = getFilePathFromUri(context, uri)
            if (filePath != null) {
                Glide.with(context)
                    .load(File(filePath))
                    .placeholder(R.drawable.muz_player3)
                    .error(R.drawable.muz_player3)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(imageView)
            } else {
                // Fallback: пытаемся загрузить через ContentResolver
                Glide.with(context)
                    .load(uri)
                    .placeholder(R.drawable.muz_player3)
                    .error(R.drawable.muz_player3)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(imageView)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading download document URI: ${e.message}")
            Glide.with(context)
                .load(R.drawable.muz_player3)
                .into(imageView)
        }
    }

}

