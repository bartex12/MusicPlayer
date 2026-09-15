package com.example.muzpleer.ui.local.frags

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.muzpleer.R
import com.example.muzpleer.databinding.FragmentPlaylistSelectionBinding
import com.example.muzpleer.model.Playlist
import com.example.muzpleer.ui.local.adapters.PlaylistsSelectionAdapter
import com.example.muzpleer.ui.local.frags.AlbumFragment.Companion.TAG
import com.example.muzpleer.ui.local.viewmodel.SharedViewModel

class PlaylistsSelectionFragment:Fragment()  {
    private lateinit var binding: FragmentPlaylistSelectionBinding
    private val viewModel: SharedViewModel by activityViewModels()
    private lateinit var adapter: PlaylistsSelectionAdapter
    private var playlistId: Long = -1
    private var selectionType: SelectionType = SelectionType.ALL_SONGS

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding =FragmentPlaylistSelectionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        playlistId = arguments?.getLong("playlistId") ?: -1
        selectionType = arguments?.getSerializable("selectionType") as? SelectionType
            ?: SelectionType.ALL_SONGS

        viewModel.setAppBarTitle("Выбрать песни из плейлистов")

        adapter = PlaylistsSelectionAdapter (viewModel){ playlist ->

            // Навигация через Bundle
            val bundle = Bundle().apply {
                putLong("playlistId", playlistId)
                putLong("selectedPlaylistId", playlist.id)
                putSerializable("selectionType", selectionType)
                Log.d(TAG,"PlaylistsSelectionFragment onViewCreated bundle: selectionType =$selectionType " +
                        "selectedPlaylistId = ${playlist.id}  playlistId = $playlistId ")
            }
            findNavController().navigate( R.id.songsSelectionFragment, bundle)
        }

        binding.playlistSelectionRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@PlaylistsSelectionFragment.adapter
        }

        viewModel.filteredPlaylists.observe(viewLifecycleOwner) { filteredPlaylists ->
            Log.d(TAG,"PlaylistSelectionFragment onViewCreated filteredPlaylists.observe: filteredPlaylists.size= ${filteredPlaylists.size} ")
            if (viewModel.getSongs().isEmpty()) binding.progressBarPlaylistSelection.visibility = View.VISIBLE else
                binding.progressBarPlaylistSelection.visibility = View.GONE
            if (filteredPlaylists.isEmpty()) binding.imageHolder3PlaylistSelection.visibility = View.VISIBLE else
                binding.imageHolder3PlaylistSelection.visibility = View.GONE
            //val sortedData =getSortedDataFolder(filteredPlaylists) //сортировка другая

            val newPlaylists :MutableList<Playlist> = mutableListOf()
            filteredPlaylists.forEach {currentPlaylist->
                if(currentPlaylist.id != playlistId) {
                    newPlaylists.add(currentPlaylist)
                }
            }
            adapter.playlists = newPlaylists  //передаём данные в адаптер
            Log.d(TAG, "!!№№%% PlaylistsSelectionFragment onViewCreated filteredPlaylists.observe" +
                    " filteredPlaylists.size = ${filteredPlaylists.size} newPlaylists.size = ${newPlaylists.size} ")
        }
        //восстанавливаем позицию списка после поворота или возвращения на экран
        binding.playlistSelectionRecyclerView.layoutManager?.scrollToPosition(viewModel.getPositionFolder())
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_other3, menu)

        val searchItem: MenuItem = menu.findItem(R.id.search_toolbar_other3)
        val searchView =searchItem.actionView as SearchView

        //значок лупы слева в развёрнутом сост и сворачиваем строку поиска (true)
        searchView.setIconifiedByDefault(true)
        //пишем подсказку в строке поиска
        searchView.queryHint = getString(R.string.search_folder)
        //устанавливаем в панели действий кнопку ( > )для отправки поискового запроса
        //searchView.isSubmitButtonEnabled = true
        //устанавливаем слушатель
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?) = false

            override fun onQueryTextChange(newText: String?): Boolean {
                viewModel.filterPlaylists(newText.orEmpty())
                return true
            }
        })
        super.onCreateOptionsMenu(menu, inflater)
    }

}