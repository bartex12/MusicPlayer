package com.example.muzpleer.ui.local.adapters

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.lifecycle.LifecycleOwner
import androidx.navigation.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.muzpleer.R
import com.example.muzpleer.databinding.ItemAlbumBinding
import com.example.muzpleer.model.Album
import com.example.muzpleer.ui.local.adapters.touch.ItemTouchHelperAdapter
import com.example.muzpleer.ui.local.frags.CoverChangeLevelFragment.LevelType
import com.example.muzpleer.ui.local.viewmodel.SharedViewModel
import com.example.muzpleer.util.getTracksCountString
import com.example.muzpleer.util.showCoverImageWithGlide
import java.util.Collections

class AlbumsAdapter(
    private val viewModel: SharedViewModel,
    private val onAlbumClick: (Album) -> Unit
) : RecyclerView.Adapter<AlbumsAdapter.AlbumViewHolder>(),
    ItemTouchHelperAdapter {

    var albums:List<Album> = listOf()
        @SuppressLint("NotifyDataSetChanged")
        set(value){
            field = value
            notifyDataSetChanged()
        }

    private var isEditMode = false
    private lateinit var itemTouchHelper: ItemTouchHelper // Добавляем ссылку
    // Временный список для перетаскивания
    private val dragData = mutableListOf<Album>()

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

        @SuppressLint("ClickableViewAccessibility")
        fun bind(album: Album) {
            binding.albumTitle.text = album.title
            binding.albumArtist.text = album.artist
            binding.tracksCount.text = getTracksCountString( album.songs.size)

            //из-за того, что обложки не отображаются, как во вкладках, приходится использовать более сложный код
            showCoverImageWithGlide(binding.root.context, album.artworkUri, binding.albumArt)

            binding.albumMenuButton.setOnClickListener { view ->
                showPopupMenu(view, album)
            }

            itemView.setOnClickListener {
                viewModel.setSelectedAlbumPosition(absoluteAdapterPosition)
                onAlbumClick(album)
            }

            // Показываем/скрываем иконку перетаскивания в режиме редактирования
            if (isEditMode) {
                binding.albumDragHandle.visibility = View.VISIBLE
                binding.albumMenuButton.visibility = View.GONE
                // Добавляем слушатель касаний для иконки перетаскивания
                binding.albumDragHandle.setOnTouchListener { v, event ->
                    if (event.action == MotionEvent.ACTION_DOWN) {
                        // Запускаем перетаскивание при нажатии на иконку
                        itemTouchHelper.startDrag(this)
                    }
                    false
                }
            } else {
                binding.albumDragHandle.visibility = View.GONE
                binding.albumMenuButton.visibility = View.VISIBLE
                binding.albumDragHandle.setOnTouchListener(null) // Убираем слушатель
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

    fun setEditMode(enable: Boolean) {
        isEditMode = enable
        if (enable) {
            // Инициализируем временный список
            dragData.clear()
            dragData.addAll(albums)
        }
        notifyDataSetChanged() // Перерисовываем для показа/скрытия иконки перетаскивания
    }

    // Метод для установки ItemTouchHelper
    fun setItemTouchHelper(helper: ItemTouchHelper) {
        itemTouchHelper = helper
    }

    //пошли методы от интерфейса ItemTouchHelperAdapter
    override fun onItemMove(fromPosition: Int, toPosition: Int): Boolean {
        // Работаем с временным списком
        if (fromPosition < toPosition) {
            for (i in fromPosition until toPosition) {
                Collections.swap(dragData, i, i + 1)
            }
        } else {
            for (i in fromPosition downTo toPosition + 1) {
                Collections.swap(dragData, i, i - 1)
            }
        }
        notifyItemMoved(fromPosition, toPosition)

        return true
    }

    override fun onDrop() {
        // Сохраняем окончательный порядок
        albums = dragData.toList()
        viewModel.updateAlbumsOrder(albums)
    }

    override fun onItemDismiss(position: Int) {
        // Не используется, но должен быть реализован
    }

    companion object{
        const val TAG = "33333"
    }
}