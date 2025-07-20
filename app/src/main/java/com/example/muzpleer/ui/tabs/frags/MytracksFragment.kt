package com.example.muzpleer.ui.tabs.frags

import android.os.Bundle
import android.util.Log
import android.view.ContextMenu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.viewpager.widget.ViewPager
import com.example.muzpleer.ui.tabs.base.BaseFragment
import com.example.muzpleer.util.Constants

class MytracksFragment(): BaseFragment() {
    companion object {
        private lateinit var viewPager: ViewPager

        fun newInstance(viewPager: ViewPager): MytracksFragment {
            this.viewPager = viewPager
            return MytracksFragment()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        baseViewModel.getMyTracks().observe(viewLifecycleOwner) { list ->
            val data = list.filter { it.typeFromIfMy == Constants.MY_TRACK }
            Log.d(TAG, "***MytracksFragment onViewCreated: data = " +
                    " ${data.filter { it.typeFromIfMy ==Constants.MY_TRACK}.map { it.title }}")
            adapter.data = data  //обновление данных списка адаптера вкладки RecyclerViewTabAdapter
        }
    }

    override fun onCreateContextMenu(
        menu: ContextMenu,
        v: View,
        menuInfo: ContextMenu.ContextMenuInfo?
    ) {
        super.onCreateContextMenu(menu, v, menuInfo)
//
//        super.onCreateContextMenu(menu, v, menuInfo)
//        menu.add(0, Constants.MOVE_SHOW_GRAF_SEC, 5, getString(R.string.action_showGraf))
//        menu.add(0, Constants.DELETE_ACTION_SEC, 10, getString(R.string.action_delete))
//        menu.add(0, Constants.CHANGE_ACTION_SEC, 20, getString(R.string.action_change))
//        menu.add(0, Constants.MOVE_TEMP_ACTION_SEC, 30, getString(R.string.action_move_temp))
//        menu.add(0, Constants.MOVE_LIKE_ACTION_SEC, 40, getString(R.string.action_move_like))
//        menu.add(0, Constants.CANCEL_ACTION_SEC, 50, getString(R.string.action_cancel))
//
//        if (getFileName() == Constants.FILENAME_TEST) {
//            menu.findItem(Constants.DELETE_ACTION_SEC).isEnabled = false
//            menu.findItem(Constants.CHANGE_ACTION_SEC).isEnabled = false
//            menu.findItem(Constants.MOVE_TEMP_ACTION_SEC).isEnabled = false
//            menu.findItem(Constants.MOVE_LIKE_ACTION_SEC).isEnabled = false
//            //выводим сообщение Системный файл. Действия ограничены.
//            Toast.makeText(activity, resources.getString(R.string.system_file_limited),
//                Toast.LENGTH_SHORT).show()
//        }
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        handleMenuItemClick(item)
        return super.onContextItemSelected(item)
    }

    private fun handleMenuItemClick(item: MenuItem) {

//        when (item.itemId) {
//            Constants.MOVE_SHOW_GRAF_SEC -> {
//                val bundle = bundleOf(Constants.NAME_OF_FILE to getFileName())
//                navController.navigate(R.id.action_nav_rascladki_to_nav_grafic, bundle)
//            }
//            Constants.DELETE_ACTION_SEC -> {
//                showDeleteDialog()
//            }
//            Constants.CHANGE_ACTION_SEC -> {
//                val bundle = bundleOf(Constants.NAME_OF_FILE to getFileName(),
//                    Constants.TAB_POSITION to 0)
//                navController.navigate(R.id.baseDialog, bundle)
//            }
//            Constants.MOVE_TEMP_ACTION_SEC -> {
//                baseViewModel.moveFromTo(getFileName(), Constants.TYPE_TEMPOLEADER, object :AfterMove {
//                    override fun afterMove() {
//                        viewPager.currentItem = 1
//                        viewPager.adapter?.notifyDataSetChanged()//для обновления соседних вкладок
//                    }
//                })
//            }
//            Constants.MOVE_LIKE_ACTION_SEC -> {
//                baseViewModel.moveFromTo(getFileName(),Constants.TYPE_LIKE, object :AfterMove {
//                    override fun afterMove() {
//                        viewPager.currentItem = 2
//                        viewPager.adapter?.notifyDataSetChanged()//для обновления соседних вкладок
//                    }
//                })
//            }
//            Constants.CANCEL_ACTION_SEC -> {
//                return
//            }
//        }
    }


}