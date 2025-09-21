package com.example.muzpleer.ui.local.frags

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.muzpleer.R
import com.example.muzpleer.databinding.FragmentAlbumSelectionBinding
import com.example.muzpleer.databinding.FragmentArtistSelectionBinding
import com.example.muzpleer.ui.local.adapters.AlbumsSelectionAdapter
import com.example.muzpleer.ui.local.adapters.ArtistSelectionAdapter
import com.example.muzpleer.ui.local.frags.AlbumFragment.Companion.TAG
import com.example.muzpleer.ui.local.frags.AlbumSelectionFragment
import com.example.muzpleer.ui.local.viewmodel.SharedViewModel
import com.example.muzpleer.util.getSortedDataAlbum
import com.example.muzpleer.util.getSortedDataArtist
import kotlin.getValue

class ArtistsSelectionFragment: Fragment() {
    private lateinit var binding: FragmentArtistSelectionBinding
    private val viewModel: SharedViewModel by activityViewModels()
    private lateinit var adapter: ArtistSelectionAdapter
    private var playlistId: Long = -1
    private var selectionType: SelectionType = SelectionType.ALL_SONGS

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentArtistSelectionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        playlistId = arguments?.getLong("playlistId") ?: -1
        selectionType = arguments?.getSerializable("selectionType") as? SelectionType
            ?: SelectionType.ALL_SONGS

        adapter = ArtistSelectionAdapter (viewModel){ artist ->

            // Навигация через Bundle
            val bundle = Bundle().apply {
                putLong("playlistId", playlistId)
                putLong("artistId", artist.id)
                putSerializable("selectionType", selectionType)
                Log.d(TAG,"AlbumSelectionFragment onViewCreated bundle: selectionType =$selectionType " +
                        "artistId = ${artist.id} playlistId = $playlistId ")
            }
            findNavController().navigate( R.id.songsSelectionFragment, bundle)
        }

        binding.artistSelectionRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@ArtistsSelectionFragment.adapter
        }

        viewModel.filteredArtists.observe(viewLifecycleOwner) { filteredArtists ->
            Log.d(TAG,"ArtistsSelectionFragment onViewCreated filteredAlbums.observe: filteredAlbums.size= ${filteredArtists.size} ")
            if (viewModel.getSongs().isEmpty()) binding.progressBarArtistSelection.visibility = View.VISIBLE else
                binding.progressBarArtistSelection.visibility = View.GONE
            if (filteredArtists.isEmpty()) binding.imageHolder3ArtistSelection.visibility = View.VISIBLE else
                binding.imageHolder3ArtistSelection.visibility = View.GONE
            val sortedData =getSortedDataArtist(filteredArtists)
            adapter.artists = sortedData  //передаём данные в адаптер
        }

        //восстанавливаем позицию списка после поворота или возвращения на экран
        binding.artistSelectionRecyclerView.layoutManager?.scrollToPosition(viewModel.getPositionArtist())
    }
}