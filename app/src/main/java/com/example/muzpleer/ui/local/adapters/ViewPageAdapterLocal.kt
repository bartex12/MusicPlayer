package com.example.muzpleer.ui.local.adapters


import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import com.example.muzpleer.R
import com.example.muzpleer.ui.my.frags.MyKingsFragment
import com.example.muzpleer.ui.my.frags.MyTracksFragment


class ViewPageAdapterLocal(private val context: Context, fragmentManager : FragmentManager,
                      private val  viewPager: ViewPager)
    : FragmentPagerAdapter(fragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    companion object{
        const val TAG = "33333"
    }

    private val fragments = arrayOf(
        MyTracksFragment.newInstance(viewPager),
        MyKingsFragment.newInstance(viewPager)
    )

    private val titles = arrayOf(
        context.getString(R.string.my_tracks),
        context.getString(R.string.little_king),
    )

    override fun getCount(): Int {
        return fragments.size
    }

    override fun getItem(position: Int): Fragment {
        return   fragments[position]
    }

    //ЭТО обязательно для обновления соседних вкладок при перемещении строки списка
    override fun getItemPosition(obj: Any): Int {
        // POSITION_NONE makes it possible to reload the PagerAdapter!
        return POSITION_NONE
    }

    override fun getPageTitle(position: Int): CharSequence {
        return titles[position]
    }
}