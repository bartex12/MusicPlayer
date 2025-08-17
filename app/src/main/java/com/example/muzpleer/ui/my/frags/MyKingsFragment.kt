package com.example.muzpleer.ui.my.frags

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
import com.example.muzpleer.MainActivity
import com.example.muzpleer.R
import com.example.muzpleer.databinding.FragmentTracksBinding
import com.example.muzpleer.model.Song
import com.example.muzpleer.model.SongAndPlaylist
import com.example.muzpleer.ui.local.frags.SongFragment
import com.example.muzpleer.ui.local.viewmodel.SharedViewModel
import com.example.muzpleer.ui.my.adapters.MyTracksAdapter
import com.example.muzpleer.ui.my.viewmodel.MyViewModel
import org.koin.androidx.viewmodel.ext.android.activityViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import kotlin.getValue

class MyKingsFragment : Fragment() {

    private var _binding:FragmentTracksBinding? = null
    private val binding get() = _binding!!

    lateinit var adapter: MyTracksAdapter
    internal lateinit var recyclerView : RecyclerView
    lateinit var navController: NavController
    val baseViewModel: MyViewModel by  viewModel()
    private val sharedViewModel: SharedViewModel by activityViewModel()
    private var playlist: List<Song> = listOf()

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

        adapter = MyTracksAdapter(sharedViewModel,{ myTrack ->
            playlist = baseViewModel.getDataKing()
            sharedViewModel.setSongAndPlaylist(
                SongAndPlaylist(
                    song = myTrack,
                    playlist = playlist)
            )
           },
            { myTrack ->
                playlist = baseViewModel.getDataKing()
                sharedViewModel.setSongAndPlaylist(
                    SongAndPlaylist(
                        song = myTrack,
                        playlist = playlist)
                )
            navController.navigate(
                R.id.action_tabsLocalFragment_to_playerFragment)
        })
        recyclerView.layoutManager = LinearLayoutManager(activity)
        recyclerView.adapter = adapter

        //объявляем о регистрации контекстного меню
        registerForContextMenu(recyclerView)

        baseViewModel.dataKing.observe(viewLifecycleOwner) { list ->
            Log.d(TAG, "***MykingsFragment onViewCreated: data size = ${list.size}" )
            adapter.data = list  //обновление данных списка адаптера вкладки
        }

        //восстанавливаем позицию списка после поворота или возвращения на экран
        recyclerView.layoutManager?.scrollToPosition(sharedViewModel.getPositionMyKing())
    }

    //запоминаем  позицию списка, на которой сделан клик - на случай поворота экрана
    override fun onPause() {
        super.onPause()
        //определяем первую видимую позицию
        val manager = recyclerView.layoutManager as LinearLayoutManager
        val firstPosition = manager.findFirstVisibleItemPosition()
        sharedViewModel.savePositionMyKing(firstPosition)
        Log.d(TAG, "MyKingsFragment onPause firstPosition = $firstPosition")
    }

    companion object {
        private lateinit var viewPager: ViewPager
        const val TAG = "33333"

        fun newInstance(viewPager: ViewPager): MyKingsFragment {
            this.viewPager = viewPager
            return MyKingsFragment()
        }
    }
}

