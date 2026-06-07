package com.example.muzpleer.ui.local.adapters

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import androidx.lifecycle.LifecycleOwner
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.example.muzpleer.MainActivity
import com.example.muzpleer.R
import com.example.muzpleer.databinding.ItemMusicBinding
import com.example.muzpleer.model.Song
import com.example.muzpleer.ui.local.viewmodel.SharedViewModel
import com.example.muzpleer.util.formatAsTime
import com.example.muzpleer.util.formatDate
import com.example.muzpleer.util.formatFileSize
import com.example.muzpleer.util.isContentProviderUri
import java.io.File


class SongsAdapter(
    private val viewModel: SharedViewModel,
    private val onItemClick: (Song) -> Unit,
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
                    val dataMediaUri = getNormalizedPath(data[position].mediaUri)
                    val currentSongMediaUri = getNormalizedPath(currentSong?.mediaUri.toString())

                    holder.itemView.isSelected = dataMediaUri == currentSongMediaUri
                }catch(e: Exception){
                    Log.d(TAG, "SongsAdapter Ошибка: ${e.message}")
                }
            }
    }

    fun getNormalizedPath(uriString: String): String {
        return if (uriString.startsWith("file://")) {
            Uri.decode(uriString.substring(7))
        } else {
            uriString
        }
    }

    override fun getItemCount(): Int {
        return data.size
    }

    inner class MusicViewHolder(private val binding: ItemMusicBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(track: Song) {
            Log.d(TAG, "WWW-1 SongAdapter track.title = ${track.title}  ")

            binding.trackTitle.text = track.title
            binding.trackArtist.text = track.artist
            binding.trackDuration.text = track.duration.formatAsTime()

            Log.d(TAG, "WWW 1 SongAdapter bind track.artUri =  ${track.artUri}")
            // Загрузка обложки, если заменили её на другую
            track.artUri?.let {artUri->
                Log.d(TAG,"WWW 3 SongAdapter bind  artUri = $artUri")
                try {
                    if (isContentProviderUri(artUri)){
                        // Загрузка обложки из  content:/com.android.providers.downloads
                        showImageWithGlide(binding.root.context, artUri, binding.trackArtwork)
                    }else{
                        // Загрузка обложки из кэша приложения
                        showImageWithGlide(binding.root.context, File(artUri), binding.trackArtwork)
                    }
                }catch (e: Exception){
                    Log.e(TAG, " ❌ Glide load failed in SongsAdapter for URI: $artUri", e)
                    binding.trackArtwork.setImageResource(R.drawable.muz_player3)
                }
            }?: binding.trackArtwork.setImageResource(R.drawable.muz_player2)

            binding.root.setOnClickListener {
                viewModel.setSelectedPosition(absoluteAdapterPosition)
                onItemClick(track)
            }

            binding.menuButton.setOnClickListener { view ->
                showPopupMenu(view, track)
            }
        }
    }

    fun showImageWithGlide(context:Context, artUri: Any, imageView: ImageView){
        // Загрузка обложки
        Glide.with(context)
            .load(artUri)
            .placeholder(R.drawable.muz_player3)
            .error(R.drawable.muz_player2)
            .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
            .addListener(object : RequestListener<Drawable> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: com.bumptech.glide.request.target.Target<Drawable?>,
                    isFirstResource: Boolean
                ): Boolean {
                    Log.e(TAG, " ❌ Glide load failed in SongsAdapter for URI: $artUri", e)
                    return false
                }

                override fun onResourceReady(
                    resource: Drawable,
                    model: Any,
                    target: com.bumptech.glide.request.target.Target<Drawable?>?,
                    dataSource: DataSource,
                    isFirstResource: Boolean
                ): Boolean {
                    Log.d(TAG, "✅Glide load success in SongsAdapter  for URI: $artUri")
                    Log.d(TAG, "✅ DataSource in SongsAdapter : $dataSource") // 👈 Важно! Покажет откуда загружено
                    return false
                }
            })
            .into(imageView)
    }

    private fun showPopupMenu(view: View, song: Song) {
        val context = view.context
        val popup = PopupMenu(context, view)
        popup.menuInflater.inflate(R.menu.song_item_menu, popup.menu)

        viewModel.checkIsFavoriteWithCallback(song.id) {isFavorite->
            popup.menu.findItem(R.id.action_add_to_favorites).isVisible = !isFavorite
            popup.menu.findItem(R.id.action_remove_from_favorites).isVisible = isFavorite
        }

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
                R.id.action_add_to_playlist -> {
                    showChoosePlaylistDialog(context, song)
                    true
                }
                R.id.action_change_cover -> {  //сменить обложку
                    Log.d(TAG, "!!!SongsAdapter showPopupMenu action_change_cover:" +
                            "song title = ${song.title} song artUri =  ${song.artUri}")
                    //запоминаем во ViewModel выбранную песню
                    viewModel.setSelectedSong(song)
                    //идём во фрагмент замены обложки с источником вызова адаптера
                    view.findNavController().navigate(R.id.coverChangeFragment)
                    true
                }
                R.id.edit_song_info -> { //изменить информацию о песне
                    viewModel.setSelectedSong(song)
                    // Переходим к редактированию
                    view.findNavController().navigate(R.id.editSongFragment)
                    true
                }
                R.id.action_send -> {
                    shareSong(context, song) // Вызов функции для отправки песни
                    true
                }
                R.id.  action_song_info -> {
                    showSongInfoDialog(context, song)
                    true
                }
                else -> false
            }
        }
        popup.show()
    }

    private fun showChoosePlaylistDialog(context: Context, song: Song) {
        val dialog = Dialog(context)
        dialog.setContentView(R.layout.dialog_choose_playlist)
        dialog.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        dialog.window?.setBackgroundDrawableResource(android.R.color.white)
        dialog.setCancelable(true)

        val recyclerView = dialog.findViewById<RecyclerView>(R.id.rvPlaylists)
        val adapter = PlaylistChooseAdapter { playlist ->
            // Добавляем песню в выбранный плейлист
            viewModel.addSongToPlaylist(song.id, playlist.id)

            // Показываем уведомление
            Toast.makeText(
                context,
                "Песня добавлена в \"${playlist.playlistName}\"",
                Toast.LENGTH_SHORT
            ).show()

            dialog.dismiss()
        }

        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter

        // Загружаем список плейлистов
        viewModel.filteredPlaylists.observe(context as LifecycleOwner) { playlists ->
            adapter.playlists = playlists.filter { it.id != -1L } // Исключаем "Все песни"
        }

        dialog.show()
    }

    private fun showSongInfoDialog(context:Context,song: Song) {
        val dialog = Dialog(context)
        dialog.setContentView(R.layout.dialog_song_info)
        dialog.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        dialog.window?.setBackgroundDrawableResource(android.R.color.white)

        // Заполняем данные
        dialog.findViewById<TextView>(R.id.tvTitle).text = song.title ?: "Неизвестно"
        dialog.findViewById<TextView>(R.id.tvArtist).text = song.artist ?: "Неизвестно"
        dialog.findViewById<TextView>(R.id.tvAlbum).text = song.albumName ?: "Неизвестно"
        dialog.findViewById<TextView>(R.id.tvAuthor).text = song.author ?: "Неизвестно"
        dialog.findViewById<TextView>(R.id.tvLocation).text = song.folderPath ?: "Неизвестно"

        // Для размера файла и дат нужно получить полную информацию из базы
        viewModel.getSongDetails(song.id) { songFile ->
            dialog.findViewById<TextView>(R.id.tvSize).text =
                formatFileSize(songFile?.size ?: 0)
            dialog.findViewById<TextView>(R.id.tvYear).text =
                songFile?.year?.toString() ?: "Неизвестно"
            dialog.findViewById<TextView>(R.id.tvDateAdded).text =
                formatDate(songFile?.dateAdded ?: 0)
            dialog.findViewById<TextView>(R.id.tvDateModified).text =
                formatDate(songFile?.lastModified ?: 0)
        }
        // Обработка кнопки
        dialog.findViewById<Button>(R.id.btnUnderstand).setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
    }

    fun shareSong(context:Context, song: Song) {
        try {
            // Создаем URI для файла
            val file = File(song.mediaUri)
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            // Создаем интент для отправки песни
            val shareIntent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_STREAM, uri)
                type = "audio/*"
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                // Добавляем дополнительную информацию о песне
                putExtra(Intent.EXTRA_SUBJECT, song.title)
                putExtra(Intent.EXTRA_TEXT, "Песня: ${song.title}")
            }
            //такой вариант не поддерживается в телеге
            //context.startActivity(Intent.createChooser(shareIntent, "Поделиться песней"))
            // Создаем chooser с заголовком
            val chooserIntent = Intent.createChooser(shareIntent, "Поделиться песней")

            // Предоставляем временные права доступа
            val resInfoList = context.packageManager
                .queryIntentActivities(chooserIntent, PackageManager.MATCH_DEFAULT_ONLY)

            for (resolveInfo in resInfoList) {
                val packageName = resolveInfo.activityInfo.packageName
                context.grantUriPermission(
                    packageName,
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            }

            // Запускаем chooser
            context.startActivity(chooserIntent)

        } catch (e: Exception) {
            Log.d(SharedViewModel.Companion.TAG, "SongsAdapter shareSong Ошибка при отправке песни: ${e.message}")
            Toast.makeText(context, "Не удалось поделиться песней", Toast.LENGTH_SHORT).show()
        }
    }
}
