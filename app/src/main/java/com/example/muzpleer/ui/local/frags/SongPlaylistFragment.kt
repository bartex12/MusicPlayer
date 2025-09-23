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
import androidx.core.os.bundleOf
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.muzpleer.R
import com.example.muzpleer.databinding.FragmentAlltracksForPlaylistBinding
import com.example.muzpleer.model.Song
import com.example.muzpleer.model.SongAndPlaylist
import com.example.muzpleer.ui.local.adapters.SongsAdapter
import com.example.muzpleer.ui.local.viewmodel.SharedViewModel
import com.example.muzpleer.util.getNormalizedPath
import com.example.muzpleer.util.getSortedDataSong
import org.koin.androidx.viewmodel.ext.android.activityViewModel
import kotlin.getValue

class SongPlaylistFragment:Fragment() {
    private var _binding: FragmentAlltracksForPlaylistBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: SongsAdapter
    private val viewModel: SharedViewModel by activityViewModel()
    private var currentSearchQuery = ""
    private  var playlistId:Long = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val from:Int? = arguments?.getInt("from")
        if (from != null){
            when (from){
                5->{
                    if(arguments?.getLong("playlistId") != null) {
                        playlistId = requireArguments().getLong("playlistId")
                        Log.d(TAG, "!@#@ SongPlaylistFragment onCreate from = $from playlistId  $playlistId")
                    }
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAlltracksForPlaylistBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initMenu()

        viewModel.loadCurrentPlaylistForSongs(playlistId)  //грузим текущий плейлист ради песен для адаптера

        viewModel.currentFilteredPlaylistSongs.observe(viewLifecycleOwner) { currentPlaylistSongs ->
            Log.d(TAG, "!@#@ SongPlaylistFragment currentFilteredPlaylistSongs.observe currentFilteredPlaylistSongs size ${currentPlaylistSongs?.size}")
            adapter.data = getSortedDataSong(currentPlaylistSongs?: listOf())
        }
        adapter = SongsAdapter(viewModel,  { song ->
            val playlistSongs  = getSortedDataSong(viewModel.getCurrentPlaylist()?.playlistSongs ?: listOf()) //можно было взять и из currentFilteredPlaylistSongs
            Log.d(TAG,"!@#@ SongPlaylistFragment размер плейлиста = ${playlistSongs.size} имя первой песни плейлиста " +
                    "= ${getSortedDataSong(playlistSongs).first().title} ")

            viewModel.setSongAndPlaylist(
                SongAndPlaylist(
                    song = song,
                    playlist = playlistSongs
                )
            )
            viewModel.setCurrentSong(song)
        },{song->
            val playlistSongs  = getSortedDataSong(viewModel.getCurrentPlaylist()?.playlistSongs ?: listOf())
            viewModel.setSongAndPlaylist(
                SongAndPlaylist(
                    song = song,
                    playlist = playlistSongs)
            )
            viewModel.setCurrentSong(song)
            findNavController().navigate(R.id.action_songPlaylistFragment_to_playerFragment)
        })

        binding.alltracksPlaylistRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@SongPlaylistFragment.adapter
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "33333"
        private const val ARG_ALLTRACKSLIST = "ARG_ALLTRACKSLIST"

        fun newInstance( alltrackslist: List<Song>): SongPlaylistFragment {
            return SongPlaylistFragment().apply {
                arguments = bundleOf(
                    ARG_ALLTRACKSLIST to ArrayList(alltrackslist))
            }
        }
    }

    fun initMenu() {
        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(object : MenuProvider {

            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.menu_other, menu)

                val searchItem: MenuItem = menu.findItem(R.id.search_toolbar_other)
                val searchView =searchItem.actionView as SearchView
                //значок лупы слева в развёрнутом сост и сворачиваем строку поиска (true)
                searchView.setIconifiedByDefault(true)
                //пишем подсказку в строке поиска
                searchView.queryHint = getString(R.string.search_song)
                //устанавливаем в панели действий кнопку ( > )для отправки поискового запроса
                // searchView.isSubmitButtonEnabled = true

                //Сохраняем состояние поиска при смене ориентации:
                if ( currentSearchQuery.isNotEmpty()) {
                    searchItem.expandActionView()
                    searchView.setQuery(currentSearchQuery, false)
                }
                //устанавливаем слушатель
                searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                    override fun onQueryTextSubmit(query: String?) = false

                    override fun onQueryTextChange(newText: String?): Boolean {
                        if(arguments?.getLong("playlistId") != null)  {
                            viewModel.filterPlaylistSongs(newText.orEmpty())
                        }
                        return true
                    }
                })
            }

            override fun onPrepareMenu(menu: Menu) {
                menu.findItem(R.id.action_edit_order).isVisible =false
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                when(menuItem.itemId){
                    R.id.action_go_to_song->{
                        //список песен в плейлисте
                        val currentPlaylistSongs  = getSortedDataSong(viewModel.getCurrentPlaylist()?.playlistSongs ?: listOf())
                       //текущая песня
                        val currentSong = viewModel.getCurrentSong()

                        val currentSongMediaUri = getNormalizedPath(currentSong?.mediaUri ?: "")
                        val indexOfSong = getSortedDataSong(currentPlaylistSongs)
                            .indexOfFirst { getNormalizedPath(it.mediaUri) == currentSongMediaUri }
                        Log.d(TAG, "4$$$ SongListFragment onMenuItemSelected indexOfSong = $indexOfSong currentSongMediaUri = $currentSongMediaUri")

                        (binding.alltracksPlaylistRecyclerView.layoutManager as LinearLayoutManager).let{
                            if(indexOfSong >= 0 ) it.scrollToPositionWithOffset(indexOfSong, 0) else it.scrollToPosition(0)
                        }
                        return true
                    }
                }
                return false
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }
}