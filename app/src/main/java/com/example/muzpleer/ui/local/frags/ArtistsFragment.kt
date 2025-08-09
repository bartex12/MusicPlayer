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
import com.example.muzpleer.databinding.FragmentSingersBinding
import com.example.muzpleer.model.Artist
import com.example.muzpleer.ui.local.adapters.ArtistsAdapter
import com.example.muzpleer.ui.local.viewmodel.SharedViewModel
import com.example.muzpleer.util.getSortedDataArtist
import org.koin.androidx.viewmodel.ext.android.activityViewModel

class ArtistsFragment:Fragment() {
    private var _binding: FragmentSingersBinding? = null
    private val binding get() = _binding!!
    private val viewModel: SharedViewModel by activityViewModel()
    private lateinit var adapter: ArtistsAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSingersBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = ArtistsAdapter { artist ->

            viewModel.setPlaylist(artist.songs) //устанавливаем список песен как плейлист

            // Навигация через Bundle
            val bundle = Bundle().apply {
                putString("artistId", artist.id)
            }
            findNavController().navigate( R.id.alltracksFragment,bundle)
               // AlltracksFragment.newInstance( artistTracks).arguments)
        }

        binding.singersRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@ArtistsFragment.adapter
        }

        viewModel.filteredArtists.observe(viewLifecycleOwner) { filteredArtists ->
            Log.d(TAG,"34 ArtistsFragment onViewCreated filteredArtists.observe: filteredArtists.size= ${filteredArtists.size} ")
            if (viewModel.getSong().isEmpty()) binding.progressBarSingers.visibility = View.VISIBLE else binding.progressBarSingers.visibility = View.GONE
            if (filteredArtists.isEmpty()) binding.imageHolder3Singers.visibility = View.VISIBLE else binding.imageHolder3Singers.visibility = View.GONE
            val sortedData =getSortedDataArtist(filteredArtists)
            adapter.data = sortedData  //передаём данные в адаптер
        }
        initMenu()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "33333"
        private lateinit var viewPager: ViewPager

        fun newInstance(viewPager: ViewPager): ArtistsFragment {
            this.viewPager = viewPager
            return ArtistsFragment()
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
                searchView.queryHint = getString(R.string.search_artist)
                //устанавливаем в панели действий кнопку ( > )для отправки поискового запроса
                searchView.isSubmitButtonEnabled = true
                //устанавливаем слушатель
                searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                    override fun onQueryTextSubmit(query: String?) = false

                    override fun onQueryTextChange(newText: String?): Boolean {
                        viewModel.filterArtists(newText.orEmpty())
                        return true
                    }
                })
            }
            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return false
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }
}