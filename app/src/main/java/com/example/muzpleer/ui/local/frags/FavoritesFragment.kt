package com.example.muzpleer.ui.local.frags

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager.widget.ViewPager
import com.example.muzpleer.R
import com.example.muzpleer.databinding.FragmentFavoriteBinding
import com.example.muzpleer.model.SongAndPlaylist
import com.example.muzpleer.ui.local.adapters.SongsAdapter
import com.example.muzpleer.ui.local.viewmodel.SharedViewModel
import com.example.muzpleer.util.getSortedDataSong
import org.koin.androidx.viewmodel.ext.android.activityViewModel

class FavoritesFragment: Fragment() {
    private var _binding: FragmentFavoriteBinding? = null
    private val binding get() = _binding!!
    private val viewModel: SharedViewModel by activityViewModel()
    private lateinit var adapter: SongsAdapter

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
        })

        binding.favoriteRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@FavoritesFragment.adapter
        }

        viewModel.favoriteSongs.observe(viewLifecycleOwner) { favorites ->
            val sortedData = getSortedDataSong(favorites)
            adapter.data = sortedData  //передаём данные в адаптер
            binding.favoriteEmpty.visibility = if (favorites.isEmpty()) View.VISIBLE else View.GONE
        }
        //восстанавливаем позицию списка после поворота или возвращения на экран и при новой загрузке
        binding.favoriteRecyclerView.layoutManager?.scrollToPosition(viewModel.getPositionFavoriteSong())
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

        fun newInstance(viewPager: ViewPager): FavoritesFragment {
            this.viewPager = viewPager
            return FavoritesFragment()
        }
    }
}