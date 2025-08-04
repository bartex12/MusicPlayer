package com.example.muzpleer.ui.tabs.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.muzpleer.R
import com.example.muzpleer.databinding.FragmentTracksBinding
import com.example.muzpleer.model.Song
import com.example.muzpleer.ui.player.PlayerFragment
import com.example.muzpleer.ui.tabs.adapters.RecyclerViewTabAdapter
import org.koin.androidx.viewmodel.ext.android.viewModel

open class BaseFragment:Fragment() {

    companion object{const val TAG = "33333"}

    private var _binding:FragmentTracksBinding? = null
    private val binding get() = _binding!!

    lateinit var adapter: RecyclerViewTabAdapter
    internal lateinit var recyclerView : RecyclerView
    lateinit var navController: NavController
    val baseViewModel: BaseViewModel by  viewModel()

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

            val playlist: List<Song> = baseViewModel.data.value ?: listOf()
            // Navigate to player
            navController.navigate(
                R.id.action_tabsFragment_to_playerFragment,
                PlayerFragment.newInstance(myTrack, playlist).arguments
            )
        }, { myTrack ->
            //todo двойной клик
//            nameOfFile = nameItem
//            Log.d(TAG,"// onLongClick nameItem = $nameItem")
        })
        recyclerView.layoutManager = LinearLayoutManager(activity)
        recyclerView.adapter = adapter

        //объявляем о регистрации контекстного меню
        registerForContextMenu(recyclerView)
    }
}