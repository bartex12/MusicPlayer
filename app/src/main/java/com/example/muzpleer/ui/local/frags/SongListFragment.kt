package com.example.muzpleer.ui.local.frags

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
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

//Больше не нужен - разбит на 3 класса
class SongListFragment:Fragment() {
    private var _binding: FragmentAlltracksBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: SongsAdapter
    private val viewModel: SharedViewModel by activityViewModel()

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

        when(arguments?.getInt("from")){
            2->{
                if (arguments?.getLong("albumId") != null)  {
                    val albumId = arguments?.getLong("albumId")!!
                    Log.d(TAG, "40!@# SongListFragment arguments albumId  $albumId")

                    viewModel.getSongsByAlbum(albumId)

                    viewModel.filteredListAlbumSong.observe(viewLifecycleOwner) { albumSongs ->
                        Log.d(TAG, "41!@# SongListFragment arguments filteredListAlbumSong size ${albumSongs.size}")
                        adapter.data = getSortedDataSong(albumSongs)

                        // Ключевое добавление - обновляем меню при загрузке данных
                        requireActivity().invalidateOptionsMenu()
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

                        // Ключевое добавление - обновляем меню при загрузке данных
                        requireActivity().invalidateOptionsMenu()
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
                        Log.d(TAG, "47!@# SongListFragment filteredListFolderSong.observe " +
                                "folderSongs size ${folderSongs.size} ")
                        adapter.data = getSortedDataSong(folderSongs)

                        // Ключевое добавление - обновляем меню при загрузке данных
                        requireActivity().invalidateOptionsMenu()
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
                menuInflater.inflate(R.menu.menu_other_2, menu)
            }

            override fun onPrepareMenu(menu: Menu) {
                menu.findItem(R.id.action_edit_order2).isVisible = false
                var songsCount = 0
                when(arguments?.getInt("from")){
                    2->{
                        songsCount =  viewModel.filteredListAlbumSong.value?.size?:0
                    }
                    3->{
                        songsCount =  viewModel.filteredListArtistSong.value?.size?:0
                    }
                    4->{
                        songsCount =  viewModel.filteredListFolderSong.value?.size?:0
                    }
                }
                Log.d(TAG, "$$$$$ SongListFragment onPrepareMenu songsCount = $songsCount ")
                // Простое условие - больше 7 песен
                menu.findItem(R.id.action_go_to_song2).isVisible = songsCount > 7
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                when(menuItem.itemId){
                    R.id.action_go_to_song2->{
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