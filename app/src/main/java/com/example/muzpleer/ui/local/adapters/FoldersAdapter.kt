package com.example.muzpleer.ui.local.adapters

import android.annotation.SuppressLint
import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.os.Bundle
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
import com.example.muzpleer.databinding.ItemFolderBinding
import com.example.muzpleer.model.Folder
import com.example.muzpleer.model.Playlist
import com.example.muzpleer.ui.local.frags.CoverChangeLevelFragment.LevelType
import com.example.muzpleer.ui.local.viewmodel.SharedViewModel
import com.example.muzpleer.util.getTracksCountString
import com.example.muzpleer.util.showCoverImageWithGlide
import java.io.File

class FoldersAdapter(
    private val viewModel: SharedViewModel,
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

        // Следим за изменениями выбранной позиции
        viewModel.selectedFolderPosition
            .observe(holder.itemView.context as LifecycleOwner) { selectedPos ->
                holder.itemView.isSelected = position == selectedPos
            }
    }

    override fun getItemCount() = folders.size

    inner class FolderViewHolder(private val binding: ItemFolderBinding)
        : RecyclerView.ViewHolder(binding.root) {

        fun bind(folder: Folder) {
            binding.tvFolderName.text = folder.name
            binding.tvFolderPath.text = folder.path
            binding.tvTracksCount.text = getTracksCountString(folder.songs.size)

            //из-за того, что обложки не отображаются, как во вкладках, приходится использовать более сложный код
            showCoverImageWithGlide(binding.root.context, folder.artworkUri, binding.ivFolderIcon)

            binding.folderMenuButton.setOnClickListener { view ->
                showPopupMenu(view, folder)
            }

            itemView.setOnClickListener {
                viewModel.setSelectedFolderPosition(absoluteAdapterPosition)
                onFolderClick(folder)
            }

        }
    }
    companion object{
        const val TAG = "33333"
    }

    private fun showPopupMenu(view: View, folder: Folder) {
        val context = view.context
        val popup = PopupMenu(context, view)
        popup.menuInflater.inflate(R.menu.level_item_menu, popup.menu)

        popup.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_change_cover_level -> {  //изменить обложку плейлиста
                    navigateToChangeLevelCover(view, folder)
                    true
                }
                else -> false
            }
        }
        popup.show()
    }

    private fun navigateToChangeLevelCover(view: View,  folder: Folder) {
        viewModel.setCurrentFolderByFolderPath(folder.path)
        val bundle: Bundle  = Bundle().apply{
            putLong("levelId", folder.id)
            putSerializable("levelType", LevelType.FOLDER)
        }
        view.findNavController().navigate(R.id.coverChangeLevelFragment, bundle)
    }
}