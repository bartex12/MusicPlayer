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
import com.example.muzpleer.databinding.FragmentAlltracksBinding
import com.example.muzpleer.model.Song
import com.example.muzpleer.model.SongAndPlaylist
import com.example.muzpleer.ui.local.adapters.SongsAdapter
import com.example.muzpleer.ui.local.viewmodel.SharedViewModel
import com.example.muzpleer.util.getSortedDataSong
import org.koin.androidx.viewmodel.ext.android.activityViewModel

class SongListFragment:Fragment() {
    private var _binding: FragmentAlltracksBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: SongsAdapter
    private val viewModel: SharedViewModel by activityViewModel()
    private var currentSearchQuery = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAlltracksBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initMenu()

        val from:Int? = arguments?.getInt("from")
        Log.d(TAG, " !@# SongListFragment onViewCreated from =  $from")
        if (from != null){
            when (from){
                2->{
                    if (arguments?.getLong("albumId") != null)  {
                        val albumId = arguments?.getLong("albumId")!!
                        Log.d(TAG, "40!@# SongListFragment arguments albumId  $albumId")

                        viewModel.getSongsByAlbum(albumId)

                        viewModel.filteredListAlbumSong.observe(viewLifecycleOwner) { albumSongs ->
                            Log.d(TAG, "41!@# SongListFragment arguments filteredListAlbumSong size ${albumSongs.size}")
                            adapter.data = getSortedDataSong(albumSongs)
                        }
                        //обновление обложки при её замене
                        viewModel.coverImageUri.observe(viewLifecycleOwner) { uri ->
                            val selectedSong = viewModel.getSelectedSong()
                            selectedSong?. let{selectedSong->
                                selectedSong.artUri = uri.toString()
                                Log.d(TAG,"42!@# SongListFragment coverImageUri.observe uri = $uri ")
                            }
                            adapter.notifyDataSetChanged()
                        }
                    }
                }
                3->{
                    if(arguments?.getLong("artistId") != null) {
                        val artistId = arguments?.getLong("artistId")!!
                        Log.d(TAG, "43!@# SongListFragment arguments artistId  $artistId")

                        viewModel.getSongsByArtist(artistId)

                        viewModel.filteredListArtistSong.observe(viewLifecycleOwner) { artistSongs ->
                            Log.d(TAG, "44!@# SongListFragment filteredListArtistSong.observe filteredListArtistSong size ${artistSongs.size}")
                            adapter.data = getSortedDataSong(artistSongs)
                        }
                        //обновление обложки при её замене
                        viewModel.coverImageUri.observe(viewLifecycleOwner) { uri ->
                            val selectedSong = viewModel.getSelectedSong()
                            selectedSong?. let{selectedSong->
                                selectedSong.artUri = uri.toString()
                                Log.d(TAG,"45!@# SongListFragment coverImageUri.observe uri = $uri ")
                            }
                            adapter.notifyDataSetChanged()
                        }
                    }
                }
                4->{
                    if(arguments?.getString("folderPath") != null) {
                        val folderPath = arguments?.getString("folderPath")!!
                        Log.d(TAG, "46!@# SongListFragment arguments folderPath  $folderPath")
                        viewModel.getSongsByFolder(folderPath)

                        viewModel.filteredListFolderSong.observe(viewLifecycleOwner) { folderSongs ->
                            Log.d(TAG, "47!@# SongListFragment filteredListFolderSong.observe folderSongs size ${folderSongs.size}")
                            adapter.data = getSortedDataSong(folderSongs)
                        }
                        //обновление обложки при её замене
                        viewModel.coverImageUri.observe(viewLifecycleOwner) { uri ->
                            val selectedSong = viewModel.getSelectedSong()
                            selectedSong?. let{selectedSong->
                                selectedSong.artUri = uri.toString()
                                Log.d(TAG,"48!@# SongListFragment coverImageUri.observe uri = $uri ")
                            }
                            adapter.notifyDataSetChanged()
                        }
                    }
                }
            }
        }
        //Создаём адаптер и передаём туда sourceOfSong, чтобы для песен был один адаптер
        adapter = SongsAdapter(viewModel,  { song ->
            val playlist = viewModel.getPlaylist()

            viewModel.setSongAndPlaylist(
                SongAndPlaylist(
                    song = song,
                    playlist = playlist)
            )
            viewModel.setCurrentSong(song)
        },{song->
            val playlist = viewModel.getPlaylist()
            viewModel.setSongAndPlaylist(
                SongAndPlaylist(
                    song = song,
                    playlist = playlist)
            )
            viewModel.setCurrentSong(song)
            findNavController().navigate(R.id.action_alltracksFragment_to_playerFragment)
        })

        binding.alltracksRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@SongListFragment.adapter
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "33333"
        private const val ARG_ALLTRACKSLIST = "ARG_ALLTRACKSLIST"

        fun newInstance( alltrackslist: List<Song>): SongListFragment {
            return SongListFragment().apply {
                arguments = bundleOf(
                    ARG_ALLTRACKSLIST to ArrayList(alltrackslist))
            }
        }
    }

    fun initMenu() {
        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(object : MenuProvider {

            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.main, menu)

                val searchItem: MenuItem = menu.findItem(R.id.search_toolbar)
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
                        if (arguments?.getLong("albumId") != null)  {
                            viewModel.filterAlbumSongs(newText.orEmpty())
                        }
                        if (arguments?.getLong("artistId") != null)  {
                            viewModel.filterArtistSongs(newText.orEmpty())
                        }
                        if (arguments?.getLong("folderPath") != null)  {
                            viewModel.filterFolderSongs(newText.orEmpty())
                        }
                        return true
                    }
                })
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                when(menuItem.itemId){
                    R.id.action_to->{
                        if (arguments?.getLong("albumId") != null)  {
                            val albumSongs = viewModel.listAlbumSong.value //список песен альюома
                            val currentSong = viewModel.getCurrentSong()
                            if (albumSongs!=null){
                                val indexOfSong = getSortedDataSong(albumSongs).indexOfFirst { it.mediaUri == currentSong?.mediaUri }
                                Log.d(TAG, "1$$$ SongListFragment onMenuItemSelected indexOfSong = $indexOfSong")
                                (binding.alltracksRecyclerView.layoutManager as LinearLayoutManager).let{
                                    if(indexOfSong >= 0 ) it.scrollToPositionWithOffset(indexOfSong, 0) else it.scrollToPosition(0)
                                }
                            }
                        }
                        if (arguments?.getLong("artistId") != null)  {
                            val artistSongs = viewModel.listArtistSong.value //список песен артиста
                            val currentSong = viewModel.getCurrentSong()
                            if (artistSongs!=null){
                                val indexOfSong = getSortedDataSong(artistSongs).indexOfFirst { it.mediaUri == currentSong?.mediaUri }
                                Log.d(TAG, "2$$$ SongListFragment onMenuItemSelected indexOfSong = $indexOfSong")
                                (binding.alltracksRecyclerView.layoutManager as LinearLayoutManager).let{
                                    if(indexOfSong >= 0 ) it.scrollToPositionWithOffset(indexOfSong, 0) else it.scrollToPosition(0)
                                }
                            }
                        }

                        if(arguments?.getString("folderPath") != null) {
                            val folderSongs = viewModel.listFolderSong.value //список песен артиста
                            val currentSong = viewModel.getCurrentSong()
                            if (folderSongs!=null){
                                val indexOfSong = getSortedDataSong(folderSongs).indexOfFirst { it.mediaUri == currentSong?.mediaUri }
                                Log.d(TAG, "3$$$ SongListFragment onMenuItemSelected indexOfSong = $indexOfSong")
                                (binding.alltracksRecyclerView.layoutManager as LinearLayoutManager).let{
                                    if(indexOfSong >= 0 ) it.scrollToPositionWithOffset(indexOfSong, 0) else it.scrollToPosition(0)
                                }
                            }
                        }
                        return true
                    }
                }
                return false
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }
}