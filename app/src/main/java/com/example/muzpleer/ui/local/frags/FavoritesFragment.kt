package com.example.muzpleer.ui.local.frags

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager.widget.ViewPager
import com.example.muzpleer.R
import com.example.muzpleer.databinding.FragmentFavoriteBinding
import com.example.muzpleer.model.SongAndPlaylist
import com.example.muzpleer.ui.local.adapters.FavoritesAdapter
import com.example.muzpleer.ui.local.adapters.touch.ItemTouchHelperCallback
import com.example.muzpleer.ui.local.viewmodel.SharedViewModel
import com.example.muzpleer.util.getSortedDataSong
import com.google.android.material.snackbar.Snackbar
import org.koin.androidx.viewmodel.ext.android.activityViewModel

class FavoritesFragment: Fragment() {
    private var _binding: FragmentFavoriteBinding? = null
    private val binding get() = _binding!!
    private val viewModel: SharedViewModel by activityViewModel()
    private lateinit var adapter: FavoritesAdapter
    private lateinit var itemTouchHelper: ItemTouchHelper
    private var isEditMode = false

//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setHasOptionsMenu(true)
//    }

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

        adapter = FavoritesAdapter(viewModel) { song ->
            //устанавливаем список песен как плейлист
            val playlist=viewModel.getFavoriteSongs()
            viewModel.setPlaylist(playlist) //устанавливаем список песен как плейлист
            viewModel.setSongAndPlaylist(
                SongAndPlaylist(
                    song=song,  //текущая песня
                    playlist=playlist //текущий плейлист
                )
            )
        }

        // Настраиваем ItemTouchHelper
        val callback = ItemTouchHelperCallback(adapter)
        itemTouchHelper = ItemTouchHelper(callback)
        // Передаем ItemTouchHelper в адаптер
        adapter.setItemTouchHelper(itemTouchHelper)
        itemTouchHelper.attachToRecyclerView(binding.favoriteRecyclerView)

        binding.favoriteRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@FavoritesFragment.adapter
        }

        //загружаем избранные песни
        viewModel.loadFavoriteSongs()

        viewModel.filteredFavoriteSongs.observe(viewLifecycleOwner) { filteredFavorites ->
            //здесь нельзя делать сортировку, иначе собьётся перемещение папок!!!
            //val sortedData = getSortedDataSong(filteredFavorites)
           // adapter.data = sortedData  //передаём данные в адаптер
            adapter.data = filteredFavorites  //передаём данные в адаптер
            binding.favoriteEmpty.visibility = if (filteredFavorites.isEmpty()) View.VISIBLE else View.GONE
            binding.emptyImageViewFavorite.visibility = if (filteredFavorites.isEmpty()) View.VISIBLE else View.GONE

            // Ключевое добавление - обновляем меню при загрузке данных
            requireActivity().invalidateOptionsMenu()
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

        fun newInstance(viewPager: ViewPager): FavoritesFragment {
            this.viewPager = viewPager
            return FavoritesFragment()
        }
    }
//
//    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
//        inflater.inflate(R.menu.menu_favorites, menu)
//
//        val searchItem: MenuItem = menu.findItem(R.id.search_toolbar_favorite)
//        val searchView = searchItem.actionView as SearchView
//
//        // ЭТО КЛЮЧЕВОЕ ИЗМЕНЕНИЕ:
//        searchView.isIconified = false // Раскрываем сразу
//        searchView.queryHint = getString(R.string.search_favorite)
//
//        // Запрашиваем фокус
//        searchView.post {
//            searchView.requestFocus()
//            val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
//            imm.showSoftInput(searchView, InputMethodManager.SHOW_IMPLICIT)
//        }
//
//        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
//            override fun onQueryTextSubmit(query: String?) = false
//            override fun onQueryTextChange(newText: String?): Boolean {
//                viewModel.filterFavoriteSongs(newText.orEmpty())
//                return true
//            }
//        })
//
//        // Предотвращаем сворачивание при клике
//        searchView.setOnClickListener {
//            // Не делаем ничего - оставляем развернутым
//        }
//
//        super.onCreateOptionsMenu(menu, inflater)
//    }
//
//    override fun onPrepareOptionsMenu(menu: Menu) {
//        super.onPrepareOptionsMenu(menu)
//
//        val editItem = menu.findItem(R.id.action_edit_order_favorite)
//        val color = if (isEditMode) {
//            ContextCompat.getColor(requireContext(), R.color.green)
//        } else {
//            ContextCompat.getColor(requireContext(), R.color.white)
//        }
//        editItem?.icon?.setTint(color)
//
//        val songsCount = viewModel.filteredFavoriteSongs.value?.size ?: 0
//        menu.findItem(R.id.action_go_to_song_favorite).isVisible = songsCount > 9
//    }
//
//    override fun onOptionsItemSelected(item: MenuItem): Boolean {
//        when(item.itemId) {
//            R.id.action_go_to_song_favorite -> {
//                val favoriteSongs = viewModel.getFavoriteSongs()
//                val currentSong = viewModel.getCurrentSong()
//                val indexOfSong = getSortedDataSong(favoriteSongs).indexOfFirst {
//                    it.mediaUri == currentSong?.mediaUri
//                }
//                (binding.favoriteRecyclerView.layoutManager as LinearLayoutManager).let {
//                    if(indexOfSong >= 0) it.scrollToPositionWithOffset(indexOfSong, 0)
//                    else it.scrollToPosition(0)
//                }
//                return true
//            }
//            R.id.action_edit_order_favorite -> {
//                toggleEditMode()
//                activity?.invalidateOptionsMenu()
//                return true
//            }
//        }
//        return super.onOptionsItemSelected(item)
//    }

    fun initMenu() {
        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(object : MenuProvider {

            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.menu_favorites, menu)

                val searchItem: MenuItem = menu.findItem(R.id.search_toolbar_favorite)
                val searchView =searchItem.actionView as SearchView
                //значок лупы слева в развёрнутом сост и сворачиваем строку поиска (true)
                searchView.setIconifiedByDefault(true)
                //пишем подсказку в строке поиска
                searchView.queryHint = getString(R.string.search_favorite)
                //устанавливаем в панели действий кнопку ( > )для отправки поискового запроса
                //searchView.isSubmitButtonEnabled = true
                //устанавливаем слушатель
                searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                    override fun onQueryTextSubmit(query: String?) = false

                    override fun onQueryTextChange(newText: String?): Boolean {
                        viewModel.filterFavoriteSongs(newText.orEmpty())
                        return true
                    }
                })

                // Показываем/скрываем пункт в зависимости от режима
                val editItem = menu.findItem(R.id.action_edit_order_favorite)
                editItem.title = if (isEditMode) "Готово" else "Редактировать порядок"
            }
            override fun onPrepareMenu(menu: Menu) {
                super.onPrepareMenu(menu)
                val editItem = menu.findItem(R.id.action_edit_order_favorite)

                // Меняем цвет в зависимости от режима
                val color = if (isEditMode) {
                    ContextCompat.getColor(requireContext(), R.color.green)
                } else {
                    ContextCompat.getColor(requireContext(), R.color.white)
                }
                editItem?.icon?.setTint(color)

                // вычисляем количество песен в списке
                val songsCount = viewModel.filteredFavoriteSongs.value?.size ?: 0
                Log.d(SongPlaylistFragment.Companion.TAG, "$$$$$ SongPlaylistFragment onPrepareMenu songsCount = $songsCount ")
                // Простое условие - больше 9 песен
                menu.findItem(R.id.action_go_to_song_favorite).isVisible = songsCount > 9
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                when(menuItem.itemId){
                    R.id.action_go_to_song_favorite->{
                        val favoriteSongs = viewModel.getFavoriteSongs() //список песен артиста
                        val currentSong = viewModel.getCurrentSong()
                        val indexOfSong = getSortedDataSong(favoriteSongs).indexOfFirst { it.mediaUri == currentSong?.mediaUri }
                        Log.d(TAG, "$$$ FavoritesFragment onMenuItemSelected indexOfSong = $indexOfSong")
                        (binding.favoriteRecyclerView.layoutManager as LinearLayoutManager).let{
                            if(indexOfSong >= 0 ) it.scrollToPositionWithOffset(indexOfSong, 0) else it.scrollToPosition(0)
                        }
                        return true
                    }

                    R.id.action_edit_order_favorite -> {
                        toggleEditMode()
                        true
                    }
                }
                return false
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    private fun toggleEditMode() {
        isEditMode = !isEditMode
        adapter.setEditMode(isEditMode)

        // Включаем/выключаем возможность перетаскивания
        if (isEditMode) {
            itemTouchHelper.attachToRecyclerView(binding.favoriteRecyclerView)
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