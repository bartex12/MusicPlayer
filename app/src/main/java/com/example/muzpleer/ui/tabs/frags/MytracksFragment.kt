package com.example.muzpleer.ui.tabs.frags

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import com.example.muzpleer.R
import com.example.muzpleer.databinding.FragmentTracksBinding
import com.example.muzpleer.model.Song
import com.example.muzpleer.model.SongAndPlaylist
import com.example.muzpleer.ui.local.viewmodel.SharedViewModel
import com.example.muzpleer.ui.tabs.adapters.RecyclerViewTabAdapter
import com.example.muzpleer.ui.tabs.base.MyViewModel
import com.example.muzpleer.util.Constants
import org.koin.androidx.viewmodel.ext.android.activityViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import kotlin.getValue

class MytracksFragment(): Fragment() {

    private var _binding:FragmentTracksBinding? = null
    private val binding get() = _binding!!

    lateinit var adapter: RecyclerViewTabAdapter
    internal lateinit var recyclerView : RecyclerView
    lateinit var navController: NavController
    val baseViewModel: MyViewModel by  viewModel()
    private val sharedViewModel: SharedViewModel by activityViewModel()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTracksBinding.inflate(inflater, container, false)
        return binding.root //fragment_tracks
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = binding.tracksRecyclerView
        navController = findNavController()

        adapter = RecyclerViewTabAdapter({ myTrack ->

            val playlist: List<Song> = baseViewModel.getDataMy()
            sharedViewModel.setSongAndPlaylist(
                SongAndPlaylist(
                    song = myTrack,
                    playlist = playlist)
            )

            // Navigate to player
            navController.navigate(
                R.id.action_tabsFragment_to_playerFragment)
                //PlayerFragment.newInstance(myTrack, playlist).arguments)

        }, { myTrack ->
            //todo двойной клик
//            nameOfFile = nameItem
//            Log.d(TAG,"// onLongClick nameItem = $nameItem")
        })
        recyclerView.layoutManager = LinearLayoutManager(activity)
        recyclerView.adapter = adapter

        //объявляем о регистрации контекстного меню
        registerForContextMenu(recyclerView)

        baseViewModel.dataMy.observe(viewLifecycleOwner) { list ->
            Log.d(TAG, "***MytracksFragment onViewCreated: data size = ${list.size}" )
            adapter.data = list  //обновление данных списка адаптера вкладки RecyclerViewTabAdapter
        }
    }
    companion object {
        const val TAG = "33333"
        private lateinit var viewPager: ViewPager

        fun newInstance(viewPager: ViewPager): MytracksFragment {
            this.viewPager = viewPager
            return MytracksFragment()
        }
    }
}




//    override fun onCreateContextMenu(
//        menu: ContextMenu,
//        v: View,
//        menuInfo: ContextMenu.ContextMenuInfo?
//    ) {
//        super.onCreateContextMenu(menu, v, menuInfo)
////
////        super.onCreateContextMenu(menu, v, menuInfo)
////        menu.add(0, Constants.MOVE_SHOW_GRAF_SEC, 5, getString(R.string.action_showGraf))
////        menu.add(0, Constants.DELETE_ACTION_SEC, 10, getString(R.string.action_delete))
////        menu.add(0, Constants.CHANGE_ACTION_SEC, 20, getString(R.string.action_change))
////        menu.add(0, Constants.MOVE_TEMP_ACTION_SEC, 30, getString(R.string.action_move_temp))
////        menu.add(0, Constants.MOVE_LIKE_ACTION_SEC, 40, getString(R.string.action_move_like))
////        menu.add(0, Constants.CANCEL_ACTION_SEC, 50, getString(R.string.action_cancel))
////
////        if (getFileName() == Constants.FILENAME_TEST) {
////            menu.findItem(Constants.DELETE_ACTION_SEC).isEnabled = false
////            menu.findItem(Constants.CHANGE_ACTION_SEC).isEnabled = false
////            menu.findItem(Constants.MOVE_TEMP_ACTION_SEC).isEnabled = false
////            menu.findItem(Constants.MOVE_LIKE_ACTION_SEC).isEnabled = false
////            //выводим сообщение Системный файл. Действия ограничены.
////            Toast.makeText(activity, resources.getString(R.string.system_file_limited),
////                Toast.LENGTH_SHORT).show()
////        }
//    }
//
//    override fun onContextItemSelected(item: MenuItem): Boolean {
//        handleMenuItemClick(item)
//        return super.onContextItemSelected(item)
//    }
//
//    private fun handleMenuItemClick(item: MenuItem) {
//
////        when (item.itemId) {
////            Constants.MOVE_SHOW_GRAF_SEC -> {
////                val bundle = bundleOf(Constants.NAME_OF_FILE to getFileName())
////                navController.navigate(R.id.action_nav_rascladki_to_nav_grafic, bundle)
////            }
////            Constants.DELETE_ACTION_SEC -> {
////                showDeleteDialog()
////            }
////            Constants.CHANGE_ACTION_SEC -> {
////                val bundle = bundleOf(Constants.NAME_OF_FILE to getFileName(),
////                    Constants.TAB_POSITION to 0)
////                navController.navigate(R.id.baseDialog, bundle)
////            }
////            Constants.MOVE_TEMP_ACTION_SEC -> {
////                baseViewModel.moveFromTo(getFileName(), Constants.TYPE_TEMPOLEADER, object :AfterMove {
////                    override fun afterMove() {
////                        viewPager.currentItem = 1
////                        viewPager.adapter?.notifyDataSetChanged()//для обновления соседних вкладок
////                    }
////                })
////            }
////            Constants.MOVE_LIKE_ACTION_SEC -> {
////                baseViewModel.moveFromTo(getFileName(),Constants.TYPE_LIKE, object :AfterMove {
////                    override fun afterMove() {
////                        viewPager.currentItem = 2
////                        viewPager.adapter?.notifyDataSetChanged()//для обновления соседних вкладок
////                    }
////                })
////            }
////            Constants.CANCEL_ACTION_SEC -> {
////                return
////            }
////        }
//    }
//
//
//}