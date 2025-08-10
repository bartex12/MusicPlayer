package com.example.muzpleer.ui.local.frags

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.SearchView
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager.widget.ViewPager
import com.example.muzpleer.R
import com.example.muzpleer.databinding.FragmentFoldersBinding
import com.example.muzpleer.model.Folder
import com.example.muzpleer.ui.local.adapters.FoldersAdapter
import com.example.muzpleer.ui.local.viewmodel.SharedViewModel
import com.example.muzpleer.util.getSortedDataFolder
import com.example.muzpleer.util.getSortedDataSong
import org.koin.androidx.viewmodel.ext.android.activityViewModel

class FolderFragment : Fragment(){
    private var _binding: FragmentFoldersBinding? = null
    private val binding get() = _binding!!
    private val viewModel: SharedViewModel by activityViewModel()
    private lateinit var adapter: FoldersAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFoldersBinding.inflate(inflater, container, false)
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = FoldersAdapter { folder ->
            val playlist = getSortedDataSong(folder.songs)
            viewModel.setPlaylist(playlist) //устанавливаем список песен как плейлист

            // Навигация через Bundle
            val bundle = Bundle().apply {
                putString("folderPath", folder.path)
            }
            findNavController().navigate( R.id.alltracksFragment, bundle)
                //AlltracksFragment.newInstance( folderTracks).arguments)
        }

        binding.foldersRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@FolderFragment.adapter
        }

        viewModel.filteredFolders.observe(viewLifecycleOwner) { filteredFolders ->
            Log.d(TAG,"35 FolderFragment onViewCreated filteredFolders.observe: filteredFolders.size= ${filteredFolders.size} ")
            if (viewModel.getSong().isEmpty()) binding.progressBarFolder.visibility = View.VISIBLE else binding.progressBarFolder.visibility = View.GONE
            if (filteredFolders.isEmpty()) binding.imageHolder3Folder.visibility = View.VISIBLE else binding.imageHolder3Folder.visibility = View.GONE
            val sortedData =getSortedDataFolder(filteredFolders)
            adapter.folders = sortedData  //передаём данные в адаптер
        }
        //восстанавливаем позицию списка после поворота или возвращения на экран
        binding.foldersRecyclerView.layoutManager?.scrollToPosition(viewModel.getPositionFolder())

        initMenu()
    }

    //запоминаем  позицию списка, на которой сделан клик - на случай поворота экрана
    override fun onPause() {
        super.onPause()
        //определяем первую видимую позицию
        val manager = binding.foldersRecyclerView.layoutManager as LinearLayoutManager
        val firstPosition = manager.findFirstVisibleItemPosition()
        viewModel.savePositionFolder(firstPosition)
        Log.d(TAG, "FolderFragment onPause firstPosition = $firstPosition")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "33333"
        private lateinit var viewPager: ViewPager

        fun newInstance(viewPager: ViewPager): FolderFragment {
            this.viewPager = viewPager
            return FolderFragment()
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
                searchView.queryHint = getString(R.string.search_folder)
                //устанавливаем в панели действий кнопку ( > )для отправки поискового запроса
                searchView.isSubmitButtonEnabled = true
                //устанавливаем слушатель
                searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                    override fun onQueryTextSubmit(query: String?) = false

                    override fun onQueryTextChange(newText: String?): Boolean {
                        viewModel.filterFolders(newText.orEmpty())
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