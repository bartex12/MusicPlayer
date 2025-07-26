package com.example.muzpleer.ui.local.adapters

import android.annotation.SuppressLint
import android.content.ContentUris
import android.graphics.BitmapFactory
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.muzpleer.R
import com.example.muzpleer.databinding.ItemMusicBinding
import com.example.muzpleer.model.MusicTrack

class MusicAdapter(
    private val onItemClick: (MusicTrack) -> Unit
) : RecyclerView.Adapter<MusicAdapter.MusicViewHolder>() {
    companion object{
        const val TAG = "33333"
    }

    var data:List<MusicTrack> = listOf()
        @SuppressLint("NotifyDataSetChanged")
        set(value){
            field = value
            notifyDataSetChanged()
        }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MusicViewHolder {
        val binding = ItemMusicBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return MusicViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: MusicViewHolder,
        position: Int
    ) {
        holder.bind(data[position])
    }

    override fun getItemCount(): Int {
        return data.size
    }

    inner class MusicViewHolder(private val binding: ItemMusicBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(track: MusicTrack) {
            binding.trackTitle.text = track.title
            binding.trackArtist.text = track.artist
            binding.trackDuration.text = track.duration.formatAsTime()

            // Загружаем обложку, если есть
            val albumArtUri = ContentUris.withAppendedId(
                "content://media/external/audio/albumart".toUri(),
                track.albumId)

//            ///обложка имеет Uri track.artworkUri
//            Log.d(TAG, " %%% MusicAdapter MusicViewHolder bind: albumArtUri =  $albumArtUri  title = ${track.title}")
//            try {
//                binding.root.context.contentResolver.openInputStream(albumArtUri)?.use { stream ->
//                    val bitmap = BitmapFactory.decodeStream(stream)
//                    Log.d(TAG, "MusicViewHolder Обложка найдена: ${bitmap.width}x${bitmap.height}")
//                } ?: Log.d(TAG,  "MusicViewHolder Обложка не найдена")
//            } catch (e: Exception) {
//                Log.e(TAG, "MusicViewHolder Ошибка: ${e.message}")
//            }

            // Загрузка обложки
                Glide.with(binding.root.context)
                    .load(albumArtUri)
                    .placeholder(R.drawable.placeholder2)
                    .error(R.drawable.placeholder2)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(binding.trackArtwork)

            binding.root.setOnClickListener { onItemClick(track) }
        }

        @SuppressLint("DefaultLocale")
        private fun Long.formatAsTime(): String {
            val seconds = this / 1000
            val minutes = seconds / 60
            val remainingSeconds = seconds % 60
            return String.format("%02d:%02d", minutes, remainingSeconds)
        }
    }
}