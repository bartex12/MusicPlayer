package com.example.muzpleer.ui.local.adapters

import android.annotation.SuppressLint
import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.widget.PopupMenu
import androidx.core.net.toUri
import androidx.lifecycle.LifecycleOwner
import androidx.navigation.findNavController
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
        //следим за текущей песней - чтобы при возврате с другой вкладки выделение оставалось
        viewModel.currentSong
            .observe(holder.itemView.context as LifecycleOwner) { currentSong ->
                try{
                    holder.itemView.isSelected = data[position].mediaUri == currentSong?.mediaUri
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

        private lateinit var currentSong: Song

        fun bind(track: Song) {
            currentSong = track

            binding.trackTitle.text = track.title
            binding.trackArtist.text = track.artist
            binding.trackDuration.text = track.duration.formatAsTime()

            if (track.artUri == null){
                // Загружаем обложку, когда не меняли её
                val albumArtUri = ContentUris.withAppendedId(
                    "content://media/external/audio/albumart".toUri(), track.albumId)
                // Загрузка обложки
                showImageWithGlide(binding.root.context, albumArtUri, binding.trackArtwork)
            }else {
                // Загрузка обложки, если заменили её на другую
                track.artUri?. let{
                    val uri =  it.toUri()
                    showImageWithGlide(binding.root.context, uri, binding.trackArtwork)
                }
            }

            binding.root.setOnClickListener {
                viewModel.setSelectedPosition(absoluteAdapterPosition)
                onItemClick(track)
            }

            binding.menuButton.setOnClickListener { view ->
                showPopupMenu(view, track)
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

    fun showImageWithGlide(context:Context, artUri:Uri, imageView: ImageView){
        // Загрузка обложки
        Glide.with(context)
            .load(artUri)
            .placeholder(R.drawable.muz_player3)
            .error(R.drawable.muz_player3)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .into(imageView)
    }

    private fun showPopupMenu(view: View, song: Song) {
        val popup = PopupMenu(view.context, view)
        popup.menuInflater.inflate(R.menu.song_item_menu, popup.menu)

        // Динамически показываем нужный пункт
        popup.menu.findItem(R.id.action_add_to_favorites).isVisible = !viewModel.isFavorite(song.id)
        popup.menu.findItem(R.id.action_remove_from_favorites).isVisible = viewModel.isFavorite(song.id)

        popup.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_add_to_favorites -> {
                    viewModel.toggleFavorite(song)
                    true
                }
                R.id.action_remove_from_favorites -> {
                    viewModel.toggleFavorite(song)
                    true
                }
                R.id.action_change_cover -> {
                    Log.d(TAG, "!!!SongsAdapter action_change_cover:" +
                            "song title = ${song.title} song artUri =  ${song.artUri}")
                    viewModel.setSelectedSong(song)
                    view.findNavController().navigate(R.id.coverChangeFragment)
                    true
                }
                else -> false
            }
        }
        popup.show()
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