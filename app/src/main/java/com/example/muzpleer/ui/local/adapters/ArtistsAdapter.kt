package com.example.muzpleer.ui.local.adapters

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.lifecycle.LifecycleOwner
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.example.muzpleer.R
import com.example.muzpleer.databinding.ItemArtistBinding
import com.example.muzpleer.model.Artist
import com.example.muzpleer.ui.local.frags.CoverChangeLevelFragment.LevelType
import com.example.muzpleer.ui.local.viewmodel.SharedViewModel
import com.example.muzpleer.util.getAlbumsCountString
import com.example.muzpleer.util.getTracksCountString
import com.example.muzpleer.util.showCoverImageWithGlide

class ArtistsAdapter(
    private val viewModel: SharedViewModel,
    private val onItemClick: (Artist) -> Unit
) : RecyclerView.Adapter<ArtistsAdapter.ArtistViewHolder>() {

    var data:List<Artist> = listOf()
        @SuppressLint("NotifyDataSetChanged")
        set(value){
            field = value
            notifyDataSetChanged()
        }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArtistViewHolder {
        val binding = ItemArtistBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ArtistViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: ArtistViewHolder,
        position: Int
    ) {
        holder.bind(data[position])

        // Следим за изменениями выбранной позиции
        viewModel.selectedArtistPosition
            .observe(holder.itemView.context as LifecycleOwner) { selectedPos ->
                holder.itemView.isSelected = position == selectedPos
            }
    }

    override fun getItemCount(): Int {
        return data.size
    }

    inner class ArtistViewHolder(private val binding: ItemArtistBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(artist: Artist) {
            binding.tvArtistName.text = artist.name
            binding.tvTracksCount.text =buildString {
                append("${getAlbumsCountString(artist.albums.size)}, ")
                append(getTracksCountString(artist.songs.size))
            }

            //из-за того, что обложки не отображаются, как во вкладках, приходится использовать более сложный код
            showCoverImageWithGlide(binding.root.context, artist.artworkUri, binding.ivArtistArtwork)

            binding.artistMenuButton.setOnClickListener { view ->
                showPopupMenu(view, artist)
            }

            binding.root.setOnClickListener {
                viewModel.setSelectedArtistPosition(absoluteAdapterPosition)
                onItemClick(artist)
            }
        }
    }

    private fun showPopupMenu(view: View, artist: Artist) {
        val context = view.context
        val popup = PopupMenu(context, view)
        popup.menuInflater.inflate(R.menu.level_item_menu, popup.menu)

        popup.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_change_cover_level -> {  //изменить обложку плейлиста
                    navigateToChangeLevelCover(view, artist)
                    true
                }
                else -> false
            }
        }
        popup.show()
    }

    private fun navigateToChangeLevelCover(view: View,  artist: Artist) {
        Log.d(TAG, "-=- ArtistsAdapter navigateToChangeLevelCover: " +
                "artist = ${artist.name} id = ${artist.id} artworkUri = ${artist.artworkUri.toString()}")
        viewModel.setCurrentArtist(artist)
        val bundle: Bundle  = Bundle().apply{
            putLong("levelId", artist.id)
            putSerializable("levelType", LevelType.ARTIST)
        }
        view.findNavController().navigate(R.id.coverChangeLevelFragment, bundle)
    }
    companion object{
        const val TAG = "33333"
    }
}