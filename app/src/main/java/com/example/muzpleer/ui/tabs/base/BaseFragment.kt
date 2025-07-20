package com.example.muzpleer.ui.tabs.base

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.muzpleer.databinding.FragmentTracksBinding

open class BaseFragment:Fragment() {

    companion object{const val TAG = "33333"}

    private var _binding:FragmentTracksBinding? = null
    private val binding get() = _binding!!

    lateinit var adapter: RecyclerViewTabAdapter
    private lateinit var recyclerView : RecyclerView
    lateinit var navController: NavController
    private var nameOfFile = ""
    val baseViewModel: BaseViewModel by  viewModels()

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

        initRecyclerTabAdapter()
        //объявляем о регистрации контекстного меню
        registerForContextMenu(recyclerView)
    }

    private fun initRecyclerTabAdapter(){
        adapter = RecyclerViewTabAdapter({myTrack->
//            Log.d(TAG,"// onClick nameOfFile = $fileName")
//            val bundle = bundleOf(Constants.NAME_OF_FILE to fileName,
//                Constants.FROM_ACTIVITY to  Constants.TAB_BAR_ACTIVITY)
//            navController.navigate(R.id.action_nav_rascladki_to_nav_tempoleader, bundle)
        },{myTrack->
//            nameOfFile = nameItem
//            Log.d(TAG,"// onLongClick nameItem = $nameItem")
        })
        recyclerView.layoutManager = LinearLayoutManager(activity)
        recyclerView.adapter = adapter
    }


}