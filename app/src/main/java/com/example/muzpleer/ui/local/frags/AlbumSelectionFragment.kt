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
import com.example.muzpleer.ui.local.adapters.AlbumsSelectionAdapter
import com.example.muzpleer.ui.local.frags.AlbumFragment.Companion.TAG
import com.example.muzpleer.ui.local.viewmodel.SharedViewModel
import com.example.muzpleer.util.getSortedDataAlbum

class AlbumSelectionFragment: Fragment() {
    private lateinit var binding: FragmentAlbumSelectionBinding
    private val viewModel: SharedViewModel by activityViewModels()
    private lateinit var adapter: AlbumsSelectionAdapter
    private var playlistId: Long = -1
    private var selectionType: SelectionType = SelectionType.ALL_SONGS

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAlbumSelectionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        playlistId = arguments?.getLong("playlistId") ?: -1
        selectionType = arguments?.getSerializable("selectionType") as? SelectionType
            ?: SelectionType.ALL_SONGS

        adapter = AlbumsSelectionAdapter (viewModel){ album ->

            // Навигация через Bundle
            val bundle = Bundle().apply {
                putLong("playlistId", playlistId)
                putLong("albumId", album.albumId)
                putSerializable("selectionType", selectionType)
                Log.d(TAG,"AlbumSelectionFragment onViewCreated bundle: selectionType =$selectionType " +
                        "albumId = ${album.id} playlistId = $playlistId ")
            }
            findNavController().navigate( R.id.songsSelectionFragment, bundle)
        }

        binding.albumSelectionRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@AlbumSelectionFragment.adapter
        }

        viewModel.filteredAlbums.observe(viewLifecycleOwner) { filteredAlbums ->
            Log.d(TAG,"AlbumSelectionFragment onViewCreated filteredAlbums.observe: filteredAlbums.size= ${filteredAlbums.size} ")
            if (viewModel.getSongs().isEmpty()) binding.progressBarAlbumSelection.visibility = View.VISIBLE else
                binding.progressBarAlbumSelection.visibility = View.GONE
            if (filteredAlbums.isEmpty()) binding.imageHolder3AlbumSelection.visibility = View.VISIBLE else
                binding.imageHolder3AlbumSelection.visibility = View.GONE
            val sortedData =getSortedDataAlbum(filteredAlbums)
            adapter.albums = sortedData  //передаём данные в адаптер
        }

        //восстанавливаем позицию списка после поворота или возвращения на экран
        binding.albumSelectionRecyclerView.layoutManager?.scrollToPosition(viewModel.getPositionAlbum())
    }
}