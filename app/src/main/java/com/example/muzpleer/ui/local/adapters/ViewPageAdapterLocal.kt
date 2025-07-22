package com.example.muzpleer.ui.local.adapters


import android.content.Context
import android.util.Log
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import com.example.muzpleer.R
import com.example.muzpleer.ui.local.frags.AlbumFragment
import com.example.muzpleer.ui.local.frags.LocalFragment
import com.example.muzpleer.ui.tabs.frags.MykingsFragment
import com.example.muzpleer.ui.tabs.frags.MytracksFragment


class ViewPageAdapterLocal(private val context: Context, fragmentManager : FragmentManager,
                      private val  viewPager: ViewPager)
    : FragmentPagerAdapter(fragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    companion object{
        const val TAG = "33333"
    }

    private val fragments = arrayOf(
        LocalFragment.newInstance(viewPager) ,
        AlbumFragment.newInstance(viewPager),
        MytracksFragment.newInstance(viewPager)
    )

    private val titles = arrayOf(
        context.getString(R.string.local),
        context.getString(R.string.albums),
        context.getString(R.string.mytracks)
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