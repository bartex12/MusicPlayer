package com.example.muzpleer.ui.my

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
import com.example.muzpleer.ui.my.adapters.ViewPageAdapter
import com.example.muzpleer.ui.my.viewmodel.MyViewModel
import com.google.android.material.tabs.TabLayout

class MyTabsFragment: Fragment() {
companion object{
    const val TAG = "33333"
}
    private var _binding: FragmentTabsBinding? = null
    private val binding get() = _binding!!

    lateinit var viewPager: ViewPager
    private lateinit var tabLayout: TabLayout
    private lateinit var adapter: ViewPageAdapter

    private val baseViewModel: MyViewModel by  viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTabsBinding.inflate(inflater, container, false )
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Log.d(TAG, "***TabsFragment onViewCreated:  ")
        initViews()
        initPageAdapter()

//        this.parentFragmentManager.setFragmentResultListener(Constants.FROM_BASE_DIALOG, this
//        ) { requestKey, result ->
//            Log.d(TAG, "***TabsFragment setFragmentResultListener")
//            if(requestKey == Constants.FROM_BASE_DIALOG){
//                val tabPosition = result.getInt(Constants.TAB_POSITION, 0)
//                Log.d(TAG, "***TabsFragment setFragmentResultListener tabPosition = $tabPosition")
//                baseViewModel.saveTabPosition(tabPosition)
//                //initPageAdapter() //перезагружаем чтобы обновить
//                adapter.notifyDataSetChanged() //просто обновляем данные
//                viewPager.currentItem  =  tabPosition
//            }
//        }
        //устанавливаем текущую вкладку - берём из преференсис     0- вкладка секундомера
        viewPager.currentItem  =  0
        Log.d(TAG, "***TabsFragment setFragmentResultListener tabPosition = ${viewPager.currentItem}")
    }

    override fun onPause() {
        super.onPause()
        //baseViewModel.saveTabPosition(viewPager.currentItem)
    }

    private fun initViews() {
        viewPager = binding.viewPagerMytracks
        tabLayout = binding.tabLayoutMytracks
    }

    private fun initPageAdapter() {
        adapter =  ViewPageAdapter(requireActivity(), childFragmentManager, viewPager)
        viewPager.adapter = adapter
        tabLayout.setupWithViewPager(viewPager)
        //устанавливаем цвет текста черный а при выделении - синий
        tabLayout.setTabTextColors(Color.BLUE, Color.WHITE)
        tabLayout.setSelectedTabIndicatorColor(Color.WHITE)

    }

}