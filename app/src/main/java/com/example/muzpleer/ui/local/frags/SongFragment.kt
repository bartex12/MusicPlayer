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
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager.widget.ViewPager
import com.example.muzpleer.MainActivity
import com.example.muzpleer.R
import com.example.muzpleer.databinding.FragmentSongsBinding
import com.example.muzpleer.model.Song
import com.example.muzpleer.model.SongAndPlaylist
import com.example.muzpleer.ui.local.TabLocalFragment
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
    private var currentSearchQuery = ""

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

            val playlist = viewModel.songs.value ?: listOf()
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
        }

        binding.localRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@SongFragment.adapter
        }

        viewModel.songs.observe(viewLifecycleOwner) { songs ->
            Log.d( TAG,"31 SongsFragment onViewCreated songs.observe: songs.size= ${songs.size} ")
            if (songs.isEmpty()) {
                binding.progressBar.visibility = View.VISIBLE
                binding.imageHolder3.visibility = View.VISIBLE
            } else {
                binding.progressBar.visibility = View.GONE
                binding.imageHolder3.visibility = View.GONE
            }
            val sortedData = getSortedData(songs)
            adapter.data = sortedData  //передаём данные в адаптер
        }

        viewModel.filteredSongs.observe(viewLifecycleOwner) { filteredSongs ->
            Log.d( TAG,"32 SongsFragment onViewCreated filteredSongs.observe: filteredSongs.size= ${filteredSongs.size} ")
            val sortedData = getSortedData(filteredSongs)
            adapter.data = sortedData  //передаём данные в адаптер
        }

        initMenu()
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
                searchView.isSubmitButtonEnabled = true

                //Сохраняем состояние поиска при смене ориентации:
                if (       currentSearchQuery.isNotEmpty()) {
                    searchItem.expandActionView()
                    searchView.setQuery(currentSearchQuery, false)
                }
                //устанавливаем слушатель
                searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                    override fun onQueryTextSubmit(query: String?) = false

                    override fun onQueryTextChange(newText: String?): Boolean {
                        viewModel.filterSongs(newText.orEmpty())
                        return true
                    }
                })
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
//                return when (menuItem.itemId) {
//                    R.id.search_toolbar -> {
//                        Log.d(TAG, "#SongFragment onMenuItemSelected:  id = songFragment ")
//                        true
//                    }
//                    else -> false
//                }
                return false
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }
}