package com.example.muzpleer.ui.local.adapters

import android.annotation.SuppressLint
import android.content.ContentUris
import android.content.Context
import android.graphics.drawable.Drawable
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.net.toUri
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.example.muzpleer.R
import com.example.muzpleer.databinding.ItemAlbumSelectionBinding
import com.example.muzpleer.model.Artist
import com.example.muzpleer.ui.local.viewmodel.SharedViewModel
import com.example.muzpleer.util.getTracksCountString
import com.example.muzpleer.util.isContentProviderUri
import java.io.File

class ArtistSelectionAdapter(
    private val viewModel: SharedViewModel,
    private val onArtistClick: (Artist) -> Unit
) : RecyclerView.Adapter<ArtistSelectionAdapter.AlbumViewHolder>() {

    var artists:List<Artist> = listOf()
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
        holder.bind(artists[position])

        // Следим за изменениями выбранной позиции
        viewModel.selectedAlbumPosition
            .observe(holder.itemView.context as LifecycleOwner) { selectedPos ->
                holder.itemView.isSelected = position == selectedPos
            }
    }

    override fun getItemCount() = artists.size

    inner class AlbumViewHolder(private val binding: ItemAlbumSelectionBinding)
        : RecyclerView.ViewHolder(binding.root) {

        fun bind(artist: Artist) {
            binding.albumTitleSelection.text = artist.name
            //binding.albumArtistSelection.text = artist.albums
            binding.tracksCountSelection.text = getTracksCountString( artist.songs.size)

            artist.artworkUri?.let{artUri->
                // Загружаем изображение
                try {
                    if (isContentProviderUri(artUri.toString())){
                        // Загрузка обложки из  content:/com.android.providers.downloads
                        showImageWithGlide(binding.root.context, artUri, binding.albumArtSelection)
                    }else{
                        // Загрузка обложки из кэша приложения
                        showImageWithGlide(binding.root.context, File(artUri.toString()), binding.albumArtSelection)
                    }
                } catch (e: SecurityException) {
                    Log.e(TAG, "❌ArtistSelectionAdapter Security exception when loading: ${e.message}")
                    binding.albumArtSelection.setImageResource(R.drawable.muz_player2)
                }
            }?: binding.albumArtSelection.setImageResource(R.drawable.muz_player2)

//            // Загружаем обложку, если есть
//            val albumArtUri = ContentUris.withAppendedId(
//                "content://media/external/audio/albumart".toUri(),
//                artist.albums.firstOrNull()?.albumId ?: -1
//            )
////            // Загружаем обложку, если есть
////            val albumArtUri = ContentUris.withAppendedId(
////                "content://media/external/audio/albumart".toUri(),
////                artist.id)
//
//            // Загрузка обложки альбома
//            Glide.with(binding.root.context)
//                .load(albumArtUri)
//                .placeholder(R.drawable.muz_player5)
//                .error(R.drawable.muz_player5)
//                .diskCacheStrategy(DiskCacheStrategy.ALL)
//                .into(binding.albumArtSelection)

            itemView.setOnClickListener {
                viewModel.setSelectedAlbumPosition(absoluteAdapterPosition)
                onArtistClick(artist)
            }
        }
    }

    companion object{
        const val TAG = "33333"
    }

    fun showImageWithGlide(context: Context, artUri: Any, imageView: ImageView){
        // Загрузка обложки
        Glide.with(context)
            .load(artUri)
            .placeholder(R.drawable.muz_player5)
            .error(R.drawable.muz_player2)
            .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
            .addListener(object : RequestListener<Drawable> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: com.bumptech.glide.request.target.Target<Drawable?>,
                    isFirstResource: Boolean
                ): Boolean {
                    Log.e(TAG, " ❌ Glide load failed for URI: $artUri", e)
                    return false
                }

                override fun onResourceReady(
                    resource: Drawable,
                    model: Any,
                    target: com.bumptech.glide.request.target.Target<Drawable?>?,
                    dataSource: DataSource,
                    isFirstResource: Boolean
                ): Boolean {
                    Log.d(TAG, "✅Glide load success for URI: $artUri")
                    Log.d(TAG, "✅ DataSource: $dataSource") // 👈 Важно! Покажет откуда загружено
                    return false
                }
            })
            .into(imageView)
    }
}