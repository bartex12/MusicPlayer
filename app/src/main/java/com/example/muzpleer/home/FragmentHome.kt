package com.example.muzpleer.home

import android.os.Bundle
import android.view.*
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.Navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.muzpleer.R
import com.example.muzpleer.databinding.FragmentHomeBinding
import com.example.muzpleer.ui.local.LocalMusicViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import kotlin.getValue

class HomeFragment: Fragment(R.layout.fragment_home)  {
    companion object{
        const val TAG = "33333"
    }
    private var _binding:FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var  homeViewModel:HomeViewModel

    private lateinit var adapter: HomeAdapter
    private lateinit var navController: NavController

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root //fragment_home
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        navController = findNavController(view)

        initAdapter()
        initMenu()

        val homeViewModel: HomeViewModel by viewModel()
        //наблюдаем за изменением данных
        homeViewModel.getListMain()
            .observe(viewLifecycleOwner) { dataHomes ->
                adapter.dataHomeList = dataHomes
            }
    }

    private fun initMenu() {
        val menuHost: MenuHost = requireActivity()

        menuHost.addMenuProvider(object : MenuProvider{
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.home_menu, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.nav_settings -> {
                        //navController.navigate(R.id.action_nav_home_to_nav_settings)
                        true
                    }
                    R.id.nav_help -> {
                        // navController.navigate(R.id.action_nav_home_to_nav_help)
                        true
                    }
                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    private fun initAdapter() {
        //используем встроенный LinearLayoutManager
        val layoutManager = LinearLayoutManager(activity)
        //передаём список в адаптер
        adapter = HomeAdapter { position->
            when (position) {
                0 -> navController.navigate(R.id.action_homeFragment_to_localFragment)
                1 -> navController.navigate(R.id.action_homeFragment_to_tabsFragment)
                2 -> navController.navigate(R.id.action_homeFragment_to_settingsFragment)
            }
        }
        //устанавливаем LayoutManager для RecyclerView
        binding.recyclerViewHome.layoutManager = layoutManager
        //устанавливаем адаптер для RecyclerView
        binding.recyclerViewHome.adapter = adapter
    }

}