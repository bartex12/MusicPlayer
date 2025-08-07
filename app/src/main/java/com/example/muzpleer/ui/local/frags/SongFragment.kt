package com.example.muzpleer.ui.local.frags

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager.widget.ViewPager
import com.example.muzpleer.R
import com.example.muzpleer.databinding.FragmentSongsBinding
import com.example.muzpleer.model.Song
import com.example.muzpleer.model.SongAndPlaylist
import com.example.muzpleer.ui.local.adapters.SongsAdapter
import com.example.muzpleer.ui.local.viewmodel.SharedViewModel
import com.example.muzpleer.ui.player.PlayerFragment
import org.koin.androidx.viewmodel.ext.android.activityViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.Locale
import kotlin.getValue

class SongFragment : Fragment() {
    private var _binding: FragmentSongsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: SharedViewModel by activityViewModel()
    private lateinit var adapter: SongsAdapter
    private var originalSongsList = mutableListOf<Song>() // Исходный список песен
    private var filteredSongsList = mutableListOf<Song>() // Отфильтрованный список

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSongsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = SongsAdapter { song ->

            val playlist = viewModel.filteredSongs.value ?: listOf()
            Log.d(
                TAG, "*** SongsFragment onViewCreated SongsAdapter  song.title = ${song.title} " +
                        " playlist.size = ${playlist.size} "
            )
            viewModel.setSongAndPlaylist(
                SongAndPlaylist(
                    song = song,
                    playlist = playlist
                )
            )
            // Обработка клика по треку
            findNavController().navigate(R.id.action_tabsLocalFragment_to_playerFragment)
            //PlayerFragment.Companion.newInstance(song, playlist).arguments
        }

        binding.localRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@SongFragment.adapter
        }

        // Обработка поиска
        binding.searchView.setOnQueryTextListener(object: android.widget.SearchView.OnQueryTextListener{
                override fun onQueryTextSubmit(query: String?): Boolean {
                    return false
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    viewModel.filterSongs(newText)
                    return true
                }
            })

        viewModel.filteredSongs.observe(viewLifecycleOwner) { songs ->
            Log.d( TAG,"3 LocalFragment onViewCreated musicList.observe: songs.size= ${songs.size} ")
            if (songs.isEmpty()) {
                binding.progressBar.visibility = View.VISIBLE
                binding.imageHolder3.visibility = View.VISIBLE
                Log.d( TAG,"4 LocalFragment onViewCreated musicList.observe: progressBar.visibility = View.VISIBLE "  )
            } else {
                binding.progressBar.visibility = View.GONE
                binding.imageHolder3.visibility = View.GONE
            }
            val sortedData = getSortedData(songs)
            adapter.data = sortedData  //передаём данные в адаптер
        }
    }

    private fun getSortedData(tracks:List<Song>):List<Song>{
        return tracks.sortedWith(compareBy(
            { track -> when {
                track.title.matches(Regex("^[а-яА-ЯёЁ].*")) -> 0
                track.title.matches(Regex("^[a-zA-Z].*")) -> 1
                else -> 2}
            },
            { track -> track.title.lowercase() }
        )
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "33333"
        private lateinit var viewPager: ViewPager

        fun newInstance(viewPager: ViewPager): SongFragment {
            this.viewPager = viewPager
            return SongFragment()
        }
    }
}