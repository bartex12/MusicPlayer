package com.example.muzpleer.ui.local

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.viewpager.widget.ViewPager
import com.example.muzpleer.databinding.FragmentTabsBinding
import com.example.muzpleer.databinding.FragmentTabslocalBinding
import com.example.muzpleer.ui.local.adapters.ViewPageAdapterLocal
import com.example.muzpleer.ui.tabs.adapters.ViewPageAdapter
import com.example.muzpleer.ui.tabs.base.BaseViewModel
import com.google.android.material.tabs.TabLayout
import kotlin.getValue

class TabsLocalFragment: Fragment() {
    companion object{
        const val TAG = "33333"
    }
    private var _binding: FragmentTabslocalBinding? = null
    private val binding get() = _binding!!

    lateinit var viewPager: ViewPager
    private lateinit var tabLayout: TabLayout
    private lateinit var adapter: ViewPageAdapterLocal

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

        Log.d(TAG, "***TabsFragment onViewCreated:  ")
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