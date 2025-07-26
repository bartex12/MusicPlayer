package com.example.muzpleer.ui.local.frags

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager.widget.ViewPager
import com.example.muzpleer.R
import com.example.muzpleer.databinding.FragmentAlltracksBinding
import com.example.muzpleer.model.MusicTrack
import com.example.muzpleer.ui.local.adapters.MusicAdapter
import com.example.muzpleer.ui.player.PlayerFragment

class AlltracksFragment:Fragment() {
    private var _binding: FragmentAlltracksBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: MusicAdapter
    private lateinit var alltrackslist: List<MusicTrack>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            alltrackslist = it.getParcelableArrayList(ARG_ALLTRACKSLIST) ?: listOf()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAlltracksBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = MusicAdapter { track ->
            // Обработка клика по треку
            findNavController().navigate(
                R.id.action_alltracksFragment_to_playerFragment,
                PlayerFragment.Companion.newInstance(track, alltrackslist).arguments
            )
        }
        binding.alltracksRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@AlltracksFragment.adapter
        }
        Log.d(TAG, "3 AlltracksFragment onViewCreated  alltrackslist.size= ${alltrackslist.size} ")
        adapter.data = alltrackslist  //передаём данные в адаптер
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "33333"
        private lateinit var viewPager: ViewPager
        private const val ARG_ALLTRACKSLIST = "ARG_ALLTRACKSLIST"

        fun newInstance( alltrackslist: List<MusicTrack>): AlltracksFragment {
            return AlltracksFragment().apply {
                arguments = bundleOf(
                    ARG_ALLTRACKSLIST to ArrayList(alltrackslist))
            }
        }
    }
}