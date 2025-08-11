package com.example.muzpleer.ui.local.adapters

import android.annotation.SuppressLint
import android.content.ContentUris
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.muzpleer.R
import com.example.muzpleer.databinding.ItemFolderBinding
import com.example.muzpleer.model.Folder
import com.example.muzpleer.util.getTracksCountString

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
            binding.tvTracksCount.text = getTracksCountString(folder.songs.size)

            // Загружаем обложку, если есть
            val albumArtUri = ContentUris.withAppendedId(
                "content://media/external/audio/albumart".toUri(),
                folder.songs.firstOrNull()?.albumId ?: -1
            )

            ///обложка имеет Uri track.artworkUri
//            Log.d(TAG, " %%% FolderAdapter FolderViewHolder bind: albumArtUri =  $albumArtUri")
//            try {
//                binding.root.context.contentResolver.openInputStream(albumArtUri)?.use { stream ->
//                    //val bitmap = BitmapFactory.decodeStream(stream)
//                   // Log.d(TAG, "MusicViewHolder Обложка найдена: ${bitmap.width}x${bitmap.height}")
//                    Log.d(TAG, "FolderViewHolder Обложка найдена")
//                } ?: {
//                    Log.d(TAG,  "FolderViewHolder Обложка не найдена")
//                }
//            } catch (e: Exception) {
//                Log.d(TAG, "FolderViewHolder Ошибка: ${e.message}")
//            }
            // Загрузка обложки первого трека списка песен папки
                Glide.with(binding.root.context)
                    .load(albumArtUri)
                    .placeholder(R.drawable.muz_player1)
                    .error(R.drawable.muz_player1)
                    .into(binding.ivFolderIcon)


            binding.root.setOnClickListener { onFolderClick(folder) }
        }
    }
    companion object{
        const val TAG = "33333"
    }
}