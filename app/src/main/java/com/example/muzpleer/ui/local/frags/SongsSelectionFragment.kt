package com.example.muzpleer.ui.local.frags

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.muzpleer.R
import com.example.muzpleer.databinding.FragmentSongsSelectionBinding
import com.example.muzpleer.ui.local.adapters.SongSelectionAdapter
import com.example.muzpleer.ui.local.viewmodel.SharedViewModel

class SongsSelectionFragment : Fragment() {
    private lateinit var binding: FragmentSongsSelectionBinding
    private val viewModel: SharedViewModel by activityViewModels()
    private lateinit var adapter: SongSelectionAdapter
    private var playlistId: Long = -1
    private var selectionType: SelectionType = SelectionType.ALL_SONGS

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSongsSelectionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        playlistId = arguments?.getLong("playlistId") ?: -1
        selectionType = arguments?.getSerializable("selectionType") as? SelectionType
            ?: SelectionType.ALL_SONGS

        when (selectionType) {
            SelectionType.ALL_SONGS -> {
                viewModel.loadAllSongsForAdding()
            }
            SelectionType.FAVORITES ->{
                viewModel.loadFavoritesSongsForAdding()
            }
            SelectionType.ALBUM ->{
                val albumId = arguments?.getLong("albumId") ?: -1
                viewModel.loadAlbumSongsForAdding(albumId)
            }
            SelectionType.ARTIST ->{
                val artistId = arguments?.getLong("artistId") ?: -1
                viewModel.loadArtistSongsForAdding(artistId)
            }
            SelectionType.FOLDER ->{
                val folderPath = arguments?.getString("folderPath") ?: ""
                viewModel.loadFolderSongsForAdding(folderPath)
            }
        }

        setupRecyclerView()
        setupObservers()
        setupButtons()
    }

    private fun setupRecyclerView() {
        adapter = SongSelectionAdapter { selectedSongs ->
            updateSelectionCount(selectedSongs.size)
            updateAddButtonState(selectedSongs.isNotEmpty())
        }
        binding.songsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.songsRecyclerView.adapter = adapter
    }

    private fun setupObservers() {
        when (selectionType) {
            SelectionType.ALL_SONGS -> {
                viewModel.songs.observe(viewLifecycleOwner) { songs ->
                    adapter.submitList(songs)
                    updateSelectionCount(0)
                }
            }
            SelectionType.FAVORITES -> {
                viewModel.favoriteSongs.observe(viewLifecycleOwner) { songs ->
                    adapter.submitList(songs)
                    updateSelectionCount(0)
                }
            }
            SelectionType.ALBUM ->{
                viewModel.listAlbumSong.observe(viewLifecycleOwner) { songs ->
                    adapter.submitList(songs)
                    updateSelectionCount(0)
                }
            }
            SelectionType.ARTIST ->{
                viewModel.listArtistSong.observe(viewLifecycleOwner) { songs ->
                    adapter.submitList(songs)
                    updateSelectionCount(0)
                }
            }
            SelectionType.FOLDER ->{
                viewModel.listFolderSong.observe(viewLifecycleOwner) { folders ->
                    adapter.submitList(folders)
                    updateSelectionCount(0)
                }
            }
        }
    }

    private fun setupButtons() {
        binding.selectAllButton.setOnClickListener {
            adapter.selectAll()
        }

        binding.addButton.setOnClickListener {
            addSelectedSongsToPlaylist()
        }
    }

    private fun updateSelectionCount(count: Int) {
        binding.selectionCount.text = "Выбрано: $count"
    }

    private fun updateAddButtonState(isEnabled: Boolean) {
        binding.addButton.isEnabled = isEnabled
    }

    private fun addSelectedSongsToPlaylist() {
        val selectedSongs = adapter.getSelectedSongs()
        if (selectedSongs.isNotEmpty()) {
            viewModel.addSongsToPlaylistWhithSongs(playlistId, selectedSongs)
            findNavController().navigateUp() // Вернуться к выбору источника, если песни или избранное
            if(selectionType == SelectionType.ALBUM || selectionType == SelectionType.ARTIST ||
                selectionType == SelectionType.FOLDER){
                findNavController().navigateUp()
            }
        }else {
            Toast.makeText(requireContext(), "Выберите песни для добавления", Toast.LENGTH_SHORT).show()
        }
    }
}