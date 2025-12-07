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
import androidx.core.content.ContextCompat
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager.widget.ViewPager
import com.example.muzpleer.R
import com.example.muzpleer.databinding.FragmentSingersBinding
import com.example.muzpleer.ui.local.adapters.ArtistsAdapter
import com.example.muzpleer.ui.local.adapters.touch.ItemTouchHelperCallback
import com.example.muzpleer.ui.local.frags.AlbumFragment
import com.example.muzpleer.ui.local.viewmodel.SharedViewModel
import com.example.muzpleer.util.getSortedDataArtist
import com.example.muzpleer.util.getSortedDataSong
import com.google.android.material.snackbar.Snackbar
import org.koin.androidx.viewmodel.ext.android.activityViewModel

class ArtistsFragment:Fragment() {
    private var _binding: FragmentSingersBinding? = null
    private val binding get() = _binding!!
    private val viewModel: SharedViewModel by activityViewModel()
    private lateinit var adapter: ArtistsAdapter

    private lateinit var itemTouchHelper: ItemTouchHelper
    private var isEditMode = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

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

        adapter = ArtistsAdapter(viewModel) { artist ->
            val playlist = getSortedDataSong(artist.songs)
            viewModel.setPlaylist(playlist) //устанавливаем список песен как плейлист

            // Навигация через Bundle
            val bundle = Bundle().apply {
                putInt("from", 3)
                putLong("artistId", artist.id)
                Log.d(TAG,"33 ArtistsFragment onViewCreated bundle: artistId = ${artist.id}  from = 3")
            }
            findNavController().navigate( R.id.alltracksFragment,bundle)
        }

        binding.singersRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@ArtistsFragment.adapter
        }

        // Настраиваем ItemTouchHelper
        val callback = ItemTouchHelperCallback(adapter)
        itemTouchHelper = ItemTouchHelper(callback)
        // Передаем ItemTouchHelper в адаптер
        adapter.setItemTouchHelper(itemTouchHelper)
        itemTouchHelper.attachToRecyclerView(binding.singersRecyclerView) // ? это же в toggleEditMode

        binding.singersRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@ArtistsFragment.adapter
        }

        viewModel.filteredArtists.observe(viewLifecycleOwner) { filteredArtists ->
            Log.d(TAG,"34 ArtistsFragment onViewCreated filteredArtists.observe: filteredArtists.size= ${filteredArtists.size} ")
            if (viewModel.getSongs().isEmpty()) binding.progressBarSingers.visibility = View.VISIBLE else binding.progressBarSingers.visibility = View.GONE
            if (filteredArtists.isEmpty()) binding.imageHolder3Singers.visibility = View.VISIBLE else binding.imageHolder3Singers.visibility = View.GONE
            //здесь нельзя делать сортировку, иначе собьётся перемещение папок
            adapter.artists = filteredArtists  //передаём данные в адаптер
        }
        //восстанавливаем позицию списка после поворота или возвращения на экран
        binding.singersRecyclerView.layoutManager?.scrollToPosition(viewModel.getPositionArtist())

        //initMenu()
    }

    //запоминаем  позицию списка, на которой сделан клик - на случай поворота экрана
    override fun onPause() {
        super.onPause()
        //определяем первую видимую позицию
        val manager = binding.singersRecyclerView.layoutManager as LinearLayoutManager
        val firstPosition = manager.findFirstVisibleItemPosition()
        viewModel.savePositionArtist(firstPosition)
        Log.d(TAG, "ArtistsFragment onPause firstPosition = $firstPosition")
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

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_other, menu)

        val searchItem: MenuItem = menu.findItem(R.id.search_toolbar_other)
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
        // Показываем/скрываем пункт в зависимости от режима
        val editItem = menu.findItem(R.id.action_edit_order)
        editItem.title = if (isEditMode) "Готово" else "Редактировать порядок"

        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        menu.findItem(R.id.action_go_to_song).isVisible =false

        val editItem = menu.findItem(R.id.action_edit_order)
        // Меняем цвет в зависимости от режима
        val color = if (isEditMode) {
            ContextCompat.getColor(requireContext(), R.color.green)
        } else {
            ContextCompat.getColor(requireContext(), R.color.white)
        }
        editItem?.icon?.setTint(color)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.action_edit_order -> {
                toggleEditMode()
                true
            }
        }
        return super.onOptionsItemSelected(item)
    }

//    fun initMenu() {
//        val menuHost: MenuHost = requireActivity()
//        menuHost.addMenuProvider(object : MenuProvider {
//
//            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
//                menuInflater.inflate(R.menu.menu_other, menu)
//
//                val searchItem: MenuItem = menu.findItem(R.id.search_toolbar_other)
//                val searchView =searchItem.actionView as SearchView
//                //значок лупы слева в развёрнутом сост и сворачиваем строку поиска (true)
//                searchView.setIconifiedByDefault(true)
//                //пишем подсказку в строке поиска
//                searchView.queryHint = getString(R.string.search_artist)
//                //устанавливаем в панели действий кнопку ( > )для отправки поискового запроса
//                searchView.isSubmitButtonEnabled = true
//                //устанавливаем слушатель
//                searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
//                    override fun onQueryTextSubmit(query: String?) = false
//
//                    override fun onQueryTextChange(newText: String?): Boolean {
//                        viewModel.filterArtists(newText.orEmpty())
//                        return true
//                    }
//                })
//                // Показываем/скрываем пункт в зависимости от режима
//                val editItem = menu.findItem(R.id.action_edit_order)
//                editItem.title = if (isEditMode) "Готово" else "Редактировать порядок"
//            }
//            override fun onPrepareMenu(menu: Menu) {
//                menu.findItem(R.id.action_go_to_song).isVisible =false
//
//                val editItem = menu.findItem(R.id.action_edit_order)
//                // Меняем цвет в зависимости от режима
//                val color = if (isEditMode) {
//                    ContextCompat.getColor(requireContext(), R.color.green)
//                } else {
//                    ContextCompat.getColor(requireContext(), R.color.white)
//                }
//                editItem?.icon?.setTint(color)
//            }
//            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
//                when(menuItem.itemId){
//                    R.id.action_edit_order -> {
//                        toggleEditMode()
//                        true
//                    }
//                }
//                return false
//            }
//        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
//    }

    private fun toggleEditMode() {
        isEditMode = !isEditMode
        adapter.setEditMode(isEditMode)

        // Включаем/выключаем возможность перетаскивания
        if (isEditMode) {
            itemTouchHelper.attachToRecyclerView(binding.singersRecyclerView)
        } else {
            itemTouchHelper.attachToRecyclerView(null) // Отключаем перетаскивание
        }

        // Обновляем меню
        activity?.invalidateOptionsMenu()

        // Показываем/скрываем подсказку
        if (isEditMode) {
            showEditModeHint()
        }
    }
    private fun showEditModeHint() {
        Snackbar.make(binding.root, "Перетаскивайте песни для изменения порядка",
            Snackbar.LENGTH_LONG)
            .setAction("OK") {toggleEditMode() }
            .show()
    }
}