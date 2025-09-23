package com.example.muzpleer.ui.local.adapters

import android.annotation.SuppressLint
import android.content.ContentUris
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.core.net.toUri
import androidx.lifecycle.LifecycleOwner
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.example.muzpleer.databinding.ItemAlbumBinding
import com.example.muzpleer.model.Album
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.muzpleer.R
import com.example.muzpleer.model.Artist
import com.example.muzpleer.ui.local.frags.CoverChangeLevelFragment.LevelType
import com.example.muzpleer.ui.local.viewmodel.SharedViewModel
import com.example.muzpleer.util.getTracksCountString
import com.example.muzpleer.util.showCoverImageWithGlide

class AlbumsAdapter(
    private val viewModel: SharedViewModel,
    private val onAlbumClick: (Album) -> Unit
) : RecyclerView.Adapter<AlbumsAdapter.AlbumViewHolder>() {

    var albums:List<Album> = listOf()
        @SuppressLint("NotifyDataSetChanged")
        set(value){
            field = value
            notifyDataSetChanged()
        }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlbumViewHolder {
        val binding = ItemAlbumBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return AlbumViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AlbumViewHolder, position: Int) {
        holder.bind(albums[position])

        // Следим за изменениями выбранной позиции
        viewModel.selectedAlbumPosition
            .observe(holder.itemView.context as LifecycleOwner) { selectedPos ->
                holder.itemView.isSelected = position == selectedPos
            }
    }

    override fun getItemCount() = albums.size

    inner class AlbumViewHolder(private val binding: ItemAlbumBinding)
        : RecyclerView.ViewHolder(binding.root) {

        fun bind(album: Album) {
            binding.albumTitle.text = album.title
            binding.albumArtist.text = album.artist
            binding.tracksCount.text = getTracksCountString( album.songs.size)

//            // Загружаем обложку, если есть
//            val albumArtUri = album.artworkUri
//                ?: ContentUris.withAppendedId(
//                    "content://media/external/audio/albumart".toUri(),
//                    album.albumId)
//
//            // Загрузка обложки альбома
//            Glide.with(binding.root.context)
//                .load(albumArtUri)
//                .placeholder(R.drawable.muz_player5)
//                .error(R.drawable.muz_player5)
//                .diskCacheStrategy(DiskCacheStrategy.ALL)
//                .into(binding.albumArt)

            //из-за того, что обложки не отображаются, как во вкладках, приходится использовать более сложный код
            showCoverImageWithGlide(binding.root.context, album.artworkUri, binding.albumArt)


            binding.albumMenuButton.setOnClickListener { view ->
                showPopupMenu(view, album)
            }

            itemView.setOnClickListener {
                viewModel.setSelectedAlbumPosition(absoluteAdapterPosition)
                onAlbumClick(album)
            }
        }
    }

    private fun showPopupMenu(view: View, album: Album) {
        val context = view.context
        val popup = PopupMenu(context, view)
        popup.menuInflater.inflate(R.menu.level_item_menu, popup.menu)

        popup.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_change_cover_level -> {  //изменить обложку плейлиста
                    navigateToChangeLevelCover(view, album)
                    true
                }
                else -> false
            }
        }
        popup.show()
    }

    private fun navigateToChangeLevelCover(view: View,  album: Album) {
        Log.d(TAG, "-=- AlbumsAdapter navigateToChangeLevelCover: " +
                "album = ${album.title} id = ${album.id} albumId = ${album.albumId} artworkUri = ${album.artworkUri.toString()}")
        viewModel.setCurrentAlbum(album)
        val bundle: Bundle  = Bundle().apply{
            putLong("levelId", album.id)
            putSerializable("levelType", LevelType.ALBUM)
        }
        view.findNavController().navigate(R.id.coverChangeLevelFragment, bundle)
    }
    companion object{
        const val TAG = "33333"
    }
}