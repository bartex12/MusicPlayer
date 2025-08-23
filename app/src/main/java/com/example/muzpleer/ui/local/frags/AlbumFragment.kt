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
import com.example.muzpleer.ui.local.adapters.AlbumsAdapter
import com.example.muzpleer.ui.local.viewmodel.SharedViewModel
import com.example.muzpleer.util.getSortedDataAlbum
import com.example.muzpleer.util.getSortedDataSong
import org.koin.androidx.viewmodel.ext.android.activityViewModel


class AlbumFragment: Fragment() {
    private var _binding: FragmentAlbumBinding? = null
    private val binding get() = _binding!!
    private val viewModel: SharedViewModel by activityViewModel()
    private lateinit var adapter: AlbumsAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAlbumBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = AlbumsAdapter (viewModel){ album ->
            val playlist = getSortedDataSong(album.songs)
            viewModel.setPlaylist(playlist) //устанавливаем список песен как плейлист

            // Навигация через Bundle
            val bundle = Bundle().apply {
                putLong("albumId", album.id)
                Log.d(TAG,"33 AlbumFragment onViewCreated bundle: albumId = ${album.id} ")
            }
            findNavController().navigate( R.id.alltracksFragment, bundle)
                //AlltracksFragment.newInstance( albumTracks).arguments)
        }

        binding.albumRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@AlbumFragment.adapter
        }

        viewModel.filteredAlbums.observe(viewLifecycleOwner) { filteredAlbums ->
            Log.d(TAG,"33 AlbumFragment onViewCreated filteredAlbums.observe: filteredAlbums.size= ${filteredAlbums.size} ")
            if (viewModel.getSongs().isEmpty()) binding.progressBarAlbum.visibility = View.VISIBLE else binding.progressBarAlbum.visibility = View.GONE
            if (filteredAlbums.isEmpty()) binding.imageHolder3Album.visibility = View.VISIBLE else binding.imageHolder3Album.visibility = View.GONE
            val sortedData =getSortedDataAlbum(filteredAlbums)
            adapter.albums = sortedData  //передаём данные в адаптер
        }
        //восстанавливаем позицию списка после поворота или возвращения на экран
        binding.albumRecyclerView.layoutManager?.scrollToPosition(viewModel.getPositionAlbum())

        initMenu()
    }

    //запоминаем  позицию списка, на которой сделан клик - на случай поворота экрана
    override fun onPause() {
        super.onPause()
        //определяем первую видимую позицию
        val manager = binding.albumRecyclerView.layoutManager as LinearLayoutManager
        val firstPosition = manager.findFirstVisibleItemPosition()
        Log.d(TAG, "AlbumFragment onPause firstPosition = $firstPosition")
        viewModel.savePositionAlbum(firstPosition)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "33333"
        private lateinit var viewPager: ViewPager

        fun newInstance(viewPager: ViewPager): AlbumFragment {
            this.viewPager = viewPager
            return AlbumFragment()
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
                searchView.queryHint = getString(R.string.search_album)
                //устанавливаем в панели действий кнопку ( > )для отправки поискового запроса
                searchView.isSubmitButtonEnabled = true
                //устанавливаем слушатель
                searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                    override fun onQueryTextSubmit(query: String?) = false

                    override fun onQueryTextChange(newText: String?): Boolean {
                        viewModel.filterAlbums(newText.orEmpty())
                        return true
                    }
                })
            }
            override fun onPrepareMenu(menu: Menu) {
                menu.findItem(R.id.action_to).isVisible =false
            }
            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {

                return false
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }
}