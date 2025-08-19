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
import com.example.muzpleer.R
import com.example.muzpleer.databinding.FragmentAlbumBinding
import com.example.muzpleer.databinding.FragmentFavoriteBinding
import com.example.muzpleer.databinding.FragmentSongsBinding
import com.example.muzpleer.model.SongAndPlaylist
import com.example.muzpleer.ui.local.adapters.AlbumsAdapter
import com.example.muzpleer.ui.local.adapters.SongsAdapter
import com.example.muzpleer.ui.local.frags.AlbumFragment
import com.example.muzpleer.ui.local.frags.SongFragment
import com.example.muzpleer.ui.local.viewmodel.SharedViewModel
import com.example.muzpleer.util.getSortedDataAlbum
import com.example.muzpleer.util.getSortedDataSong
import org.koin.androidx.viewmodel.ext.android.activityViewModel
import kotlin.getValue

class FavoritesFragment: Fragment() {
    private var _binding: FragmentFavoriteBinding? = null
    private val binding get() = _binding!!
    private val viewModel: SharedViewModel by activityViewModel()
    private lateinit var adapter: SongsAdapter
    private var currentSearchQuery = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFavoriteBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = SongsAdapter(viewModel, { song ->
            //устанавливаем список песен как плейлист
            val playlist = getSortedDataSong(viewModel.getFavoriteSongs())
            viewModel.setPlaylist(playlist) //устанавливаем список песен как плейлист
            viewModel.setSongAndPlaylist( SongAndPlaylist(
                song = song,  //текущая песня
                playlist = playlist //текущий плейлист
            ))
        }, {song->
            //устанавливаем список песен как плейлист
            val playlist = getSortedDataSong(viewModel.getFavoriteSongs())
            viewModel.setPlaylist(playlist) //устанавливаем список песен как плейлист
            viewModel.setSongAndPlaylist( SongAndPlaylist(
                song = song,  //текущая песня
                playlist = playlist //текущий плейлист
            ))
            findNavController().navigate(R.id.action_tabsLocalFragment_to_playerFragment)
        }).apply {
            setOnFavoriteClickListener { song ->
                viewModel.removeFromFavorites(song.id)
            }
        }

        binding.favoriteRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@FavoritesFragment.adapter
        }

        viewModel.favoriteSongs.observe(viewLifecycleOwner) { favoriteSongs ->
            Log.d( TAG,"32 SongsFragment onViewCreated favoriteSongs.observe: favoriteSongs.size= ${favoriteSongs.size} ")
            if (favoriteSongs.isEmpty()) binding.favoriteEmpty.visibility = View.VISIBLE else
                binding.favoriteEmpty.visibility = View.GONE

            //val sortedData = getSortedDataSong(favoriteSongs)
            adapter.data = favoriteSongs  //передаём данные в адаптер
            Log.d( TAG,"32 SongsFragment onViewCreated favoriteSongs = ${favoriteSongs.map{it.title}} ")
        }
        //восстанавливаем позицию списка после поворота или возвращения на экран и при новой загрузке
        binding.favoriteRecyclerView.layoutManager?.scrollToPosition(viewModel.getPositionFavoriteSong())

        initMenu()
    }

    //запоминаем  позицию списка, на которой сделан клик - на случай поворота экрана
    override fun onPause() {
        super.onPause()
        //определяем первую видимую позицию
        val manager = binding.favoriteRecyclerView.layoutManager as LinearLayoutManager
        val firstPosition = manager.findFirstVisibleItemPosition()
        viewModel.savePositionFavoriteSong(firstPosition)
        Log.d(TAG, "SongFragment onPause firstPosition = $firstPosition")
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
                menuInflater.inflate(R.menu.favorite, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                when(menuItem.itemId){
                    R.id.delete_favorite->{

                    }
                }
                return false
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }
}