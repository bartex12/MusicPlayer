package com.example.muzpleer.ui.local.frags

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.muzpleer.R
import com.example.muzpleer.databinding.FragmentAlltracksBinding
import com.example.muzpleer.model.AdapterSource
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

        val from:Int? = arguments?.getInt("from")
        Log.d(TAG, " !@# SongListFragment onViewCreated from =  $from")
        if (from != null){
            when (from){
                2->{
                    if (arguments?.getLong("albumId") != null)  {
                        val albumId = arguments?.getLong("albumId")!!
                        Log.d(TAG, "40!@# SongListFragment arguments albumId  $albumId")

                        viewModel.getSongsByAlbum(albumId)

                        viewModel.listAlbumSong.observe(viewLifecycleOwner) { albumSongs ->
                            Log.d(TAG, "41!@# SongListFragment arguments songs size ${albumSongs.size}")
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

                        viewModel.listArtistSong.observe(viewLifecycleOwner) { artistSongs ->
                            Log.d(TAG, "44!@# SongListFragment listArtistSong.observe artistSongs size ${artistSongs.size}")
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

                        viewModel.listFolderSong.observe(viewLifecycleOwner) { folderSongs ->
                            Log.d(TAG, "47!@# SongListFragment listFolderSong.observe folderSongs size ${folderSongs.size}")
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
}