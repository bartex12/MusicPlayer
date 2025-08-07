package com.example.muzpleer.ui.local

import android.graphics.Color
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
import androidx.viewpager.widget.ViewPager
import com.example.muzpleer.R
import com.example.muzpleer.databinding.FragmentTabslocalBinding
import com.example.muzpleer.model.Song
import com.example.muzpleer.ui.local.adapters.ViewPageAdapterLocal
import com.example.muzpleer.ui.local.frags.SongFragment
import com.example.muzpleer.ui.local.viewmodel.SharedViewModel
import com.google.android.material.tabs.TabLayout
import org.koin.androidx.viewmodel.ext.android.activityViewModel
import java.util.Locale
import kotlin.getValue

class TabLocalFragment: Fragment()  {
    companion object{
        const val TAG = "33333"
    }
    private var _binding: FragmentTabslocalBinding? = null
    private val binding get() = _binding!!

    lateinit var viewPager: ViewPager
    private lateinit var tabLayout: TabLayout
    private lateinit var adapter: ViewPageAdapterLocal
    private val viewModel: SharedViewModel by activityViewModel()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTabslocalBinding.inflate(inflater, container, false )
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Log.d(TAG, "#TabLocalFragment onViewCreated:  ")
        initViews()
        initPageAdapter()

        //устанавливаем текущую вкладку - берём из преференсис     0- вкладка секундомера
        viewPager.currentItem  =  0
        //Log.d(TAG, "***TabsFragment setFragmentResultListener tabPosition = ${viewPager.currentItem}")

    }

    override fun onPause() {
        super.onPause()
        //baseViewModel.saveTabPosition(viewPager.currentItem)
    }

    private fun initViews() {
        viewPager = binding.viewPagerLocal
        tabLayout = binding.tabLayoutLocal
    }

    private fun initPageAdapter() {
        adapter =  ViewPageAdapterLocal(requireActivity(), childFragmentManager, viewPager)
        viewPager.adapter = adapter
        tabLayout.setupWithViewPager(viewPager)
        //устанавливаем цвет текста черный а при выделении - синий
        tabLayout.setTabTextColors(Color.BLUE, Color.WHITE)
        tabLayout.setSelectedTabIndicatorColor(Color.WHITE)
    }

}