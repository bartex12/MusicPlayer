package com.example.muzpleer.ui.local

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewpager.widget.ViewPager
import com.example.muzpleer.databinding.FragmentTabslocalBinding
import com.example.muzpleer.ui.local.adapters.ViewPageAdapterLocal
import com.example.muzpleer.ui.local.viewmodel.SharedViewModel
import com.google.android.material.tabs.TabLayout
import org.koin.androidx.viewmodel.ext.android.activityViewModel

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

        //устанавливаем текущую вкладку - берём из преференсис
        viewPager.currentItem  =  viewModel.getTabsLocalPosition()
    }

    override fun onPause() {
        super.onPause()
        //запоминаем текущую вкладку
        viewModel.saveTabsLocalPosition(viewPager.currentItem)
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
        tabLayout.setTabTextColors(Color.WHITE, Color.GREEN)
        tabLayout.setSelectedTabIndicatorColor(Color.GREEN)
    }

}