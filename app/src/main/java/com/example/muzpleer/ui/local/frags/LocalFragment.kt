package com.example.muzpleer.ui.local.frags

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager.widget.ViewPager
import com.example.muzpleer.R
import com.example.muzpleer.databinding.FragmentLocalBinding
import com.example.muzpleer.model.Song
import com.example.muzpleer.ui.local.adapters.MusicAdapter
import com.example.muzpleer.ui.player.PlayerFragment
import org.koin.androidx.viewmodel.ext.android.viewModel

class LocalFragment : Fragment() {
    private var _binding: FragmentLocalBinding? = null
    private val binding get() = _binding!!
    private val viewModel: LocalViewModel by viewModel()
    private lateinit var adapter: MusicAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLocalBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = MusicAdapter { track ->
            val playlist = viewModel.musicList.value?:listOf()
            // Обработка клика по треку
            findNavController().navigate(
                R.id.action_tabsLocalFragment_to_playerFragment,
                PlayerFragment.Companion.newInstance(track, playlist).arguments
            )
        }

        binding.localRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@LocalFragment.adapter
        }

        viewModel.musicList.observe(viewLifecycleOwner) { tracks ->
            Log.d(TAG, "3 LocalFragment onViewCreated musicList.observe: tracks.size= ${tracks.size} ")
            if (tracks.isEmpty()) {
                binding.progressBar.visibility = View.VISIBLE
                binding.imageHolder3.visibility = View.VISIBLE
                Log.d(TAG, "4 LocalFragment onViewCreated musicList.observe: progressBar.visibility = View.VISIBLE ")
            }else{
                binding.progressBar.visibility = View.GONE
                binding.imageHolder3.visibility = View.GONE
            }
            val sortedData =  getSortedData(tracks)
            adapter.data = sortedData  //передаём данные в адаптер
        }
    }

    private fun getSortedData(tracks:List<Song>):List<Song>{
        return tracks.sortedWith(compareBy(
            { track -> when {
                track.title.matches(Regex("^[а-яА-ЯёЁ].*")) -> 0
                track.title.matches(Regex("^[a-zA-Z].*")) -> 1
                else -> 2}
            },
            { track -> track.title.lowercase() }
        )
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "33333"
        private lateinit var viewPager: ViewPager

        fun newInstance(viewPager: ViewPager): LocalFragment {
            this.viewPager = viewPager
            return LocalFragment()
        }
    }
}