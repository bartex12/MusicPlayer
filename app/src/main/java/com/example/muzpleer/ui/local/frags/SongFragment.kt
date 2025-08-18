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
import com.example.muzpleer.databinding.FragmentSongsBinding
import com.example.muzpleer.model.SongAndPlaylist
import com.example.muzpleer.ui.local.adapters.SongsAdapter
import com.example.muzpleer.ui.local.viewmodel.SharedViewModel
import com.example.muzpleer.util.getSortedDataSong
import org.koin.androidx.viewmodel.ext.android.activityViewModel

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

        adapter = SongsAdapter(viewModel, { song ->
            //устанавливаем список песен как плейлист
            val playlist = getSortedDataSong(viewModel.getSongs())
            viewModel.setPlaylist(playlist) //устанавливаем список песен как плейлист
            viewModel.setSongAndPlaylist( SongAndPlaylist(
                    song = song,  //текущая песня
                    playlist = playlist //текущий плейлист
                ))
        }, {song->
            //устанавливаем список песен как плейлист
            val playlist = getSortedDataSong(viewModel.getSongs())
            viewModel.setPlaylist(playlist) //устанавливаем список песен как плейлист
            viewModel.setSongAndPlaylist( SongAndPlaylist(
                song = song,  //текущая песня
                playlist = playlist //текущий плейлист
            ))
            findNavController().navigate(R.id.action_tabsLocalFragment_to_playerFragment)
        })

        binding.localRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@SongFragment.adapter
        }

        viewModel.filteredSongs.observe(viewLifecycleOwner) { filteredSongs ->
            Log.d( TAG,"32 SongsFragment onViewCreated filteredSongs.observe: filteredSongs.size= ${filteredSongs.size} ")
            if (viewModel.getSongs().isEmpty()) binding.progressBar.visibility = View.VISIBLE else binding.progressBar.visibility = View.GONE
            if (filteredSongs.isEmpty()) binding.imageHolder3.visibility = View.VISIBLE else binding.imageHolder3.visibility = View.GONE
            val sortedData = getSortedDataSong(filteredSongs)
            adapter.data = sortedData  //передаём данные в адаптер
            Log.d( TAG,"32 SongsFragment onViewCreated sortedData = ${sortedData.map{it.title}} ")
        }
        //восстанавливаем позицию списка после поворота или возвращения на экран и при новой загрузке
        binding.localRecyclerView.layoutManager?.scrollToPosition(viewModel.getPositionSong())

        initMenu()
    }

    //запоминаем  позицию списка, на которой сделан клик - на случай поворота экрана
    override fun onPause() {
        super.onPause()
        //определяем первую видимую позицию
        val manager = binding.localRecyclerView.layoutManager as LinearLayoutManager
        val firstPosition = manager.findFirstVisibleItemPosition()
        viewModel.savePositionSong(firstPosition)
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
                        viewModel.filterSongs(newText.orEmpty())
                        return true
                    }

                })
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                when(menuItem.itemId){
                    R.id.action_to->{
                        Log.d(TAG, "$ SongFragment onMenuItemSelected текущая вкладка = ${viewPager.currentItem}")
                        viewPager.setCurrentItem(0)
                        Log.d(TAG, "$$ SongFragment onMenuItemSelected текущая вкладка = ${viewPager.currentItem}")
                        val pos = viewModel.getIndexOfCurrentSong()
                        Log.d(TAG, "$$$ SongFragment onMenuItemSelected pos = $pos")
                        (binding.localRecyclerView.layoutManager as LinearLayoutManager).let{
                            if(pos >=0 ) it.scrollToPositionWithOffset(pos, 0) else it.scrollToPosition(0)
                        }
                        return true
                    }
                }
                return false
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }
}