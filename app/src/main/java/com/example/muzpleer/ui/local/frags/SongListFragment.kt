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

        adapter = SongsAdapter(viewModel, { song ->
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

        when {
            arguments?.getLong("albumId") != null -> {
                val albumId = arguments?.getLong("albumId")!!
                Log.d(TAG, " !@# SongListFragment arguments albumId  $albumId")

                viewModel.getSongsByAlbum(albumId)

                viewModel.listAlbumSong.observe(viewLifecycleOwner) { albumSongs ->
                    Log.d(TAG, " !@# SongListFragment arguments songs size ${albumSongs.size}")
                    adapter.data = getSortedDataSong(albumSongs)
                }
            }

            arguments?.getString("artistId") != null -> {
                val artistId = arguments?.getString("artistId")!!
                viewModel.getSongsByArtist(artistId).observe(viewLifecycleOwner) { songs ->
                    adapter.data = getSortedDataSong(songs)
                }
            }

            arguments?.getString("folderPath") != null -> {
                val folderPath = arguments?.getString("folderPath")!!
                viewModel.getSongsByFolder(folderPath).observe(viewLifecycleOwner) { songs ->
                    adapter.data = getSortedDataSong(songs)
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
}