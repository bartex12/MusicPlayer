package com.example.muzpleer.ui.local.adapters

import android.annotation.SuppressLint
import android.content.ContentUris
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.muzpleer.R
import com.example.muzpleer.databinding.ItemMusicBinding
import com.example.muzpleer.model.Song
import com.example.muzpleer.ui.local.viewmodel.SharedViewModel

class SongsAdapter(
    private val viewModel: SharedViewModel,
    private val onItemClick: (Song) -> Unit,
    private val onLongClickListener:(Song)->Unit
) : RecyclerView.Adapter<SongsAdapter.MusicViewHolder>() {

    companion object{
        const val TAG = "33333"
    }

    var data:List<Song> = listOf()
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

        // Следим за изменениями выбранной позиции
        viewModel.selectedSongPosition
            .observe(holder.itemView.context as LifecycleOwner) { selectedPos ->
                holder.itemView.isSelected = position == selectedPos
        }
        //следим за текущей песней
        viewModel.currentSong
            .observe(holder.itemView.context as LifecycleOwner) { currentSong ->
                try{
                    holder.itemView.isSelected =  if (data[position].isLocal){
                         data[position].mediaUri == currentSong?.mediaUri
                     }else{
                          data[position].resourceId == currentSong?.resourceId
                     }
                }catch(e: Exception){
                    Log.d(TAG, "SongsAdapter Ошибка: ${e.message}")
                }
            }
    }

    override fun getItemCount(): Int {
        return data.size
    }

    inner class MusicViewHolder(private val binding: ItemMusicBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(track: Song) {
            binding.trackTitle.text = track.title
            binding.trackArtist.text = track.artist
            binding.trackDuration.text = track.duration.formatAsTime()

            // Загружаем обложку, если есть
            val albumArtUri = ContentUris.withAppendedId(
                "content://media/external/audio/albumart".toUri(),
                track.albumId)

            // Загрузка обложки
                Glide.with(binding.root.context)
                    .load(albumArtUri)
                    .placeholder(R.drawable.muz_player3)
                    .error(R.drawable.muz_player3)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(binding.trackArtwork)

            binding.root.setOnClickListener {
                viewModel.setSelectedPosition(absoluteAdapterPosition)
                onItemClick(track)
            }
            // устанавливаем слушатель долгих нажатий на списке
            binding.root.setOnLongClickListener {
                viewModel.setSelectedPosition(absoluteAdapterPosition)
                onLongClickListener(track)
                false
            }
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

//            ///обложка имеет Uri track.artworkUri
//            Log.d(TAG, " %%% MusicAdapter MusicViewHolder bind: albumArtUri =  $albumArtUri  title = ${track.title}")
//            try {
//                binding.root.context.contentResolver.openInputStream(albumArtUri)?.use { stream ->
//                    //val bitmap = BitmapFactory.decodeStream(stream)
//                   // Log.d(TAG, "MusicViewHolder Обложка найдена: ${bitmap.width}x${bitmap.height}")
//                    Log.d(TAG, "MusicViewHolder Обложка найдена")
//                } ?: {
//                    Log.d(TAG,  "MusicViewHolder Обложка не найдена")
//                }
//            } catch (e: Exception) {
//                Log.d(TAG, "MusicViewHolder Ошибка: ${e.message}")
//            }