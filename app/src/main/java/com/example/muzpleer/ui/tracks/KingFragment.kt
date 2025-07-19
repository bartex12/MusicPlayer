package com.example.muzpleer.ui.tracks

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.muzpleer.R
import com.example.muzpleer.databinding.FragmentKingsBinding
import com.example.muzpleer.databinding.FragmentTracksBinding
import com.example.muzpleer.model.MusicTrack
import com.example.muzpleer.ui.player.PlayerFragment
import org.koin.androidx.viewmodel.ext.android.viewModel
import kotlin.getValue

class KingsFragment : Fragment() {
    private var _binding: FragmentKingsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: KingsViewModel by viewModel()
    private lateinit var adapter: TracksAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentKingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = TracksAdapter { track ->
            val playlist:List<MusicTrack> = viewModel.tracks.value?:listOf()
            // Navigate to player
            findNavController().navigate(
                R.id.action_kingsFragment_to_playerFragment,
                PlayerFragment.newInstance(track, playlist).arguments)
        }

        binding.kingsRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@KingsFragment.adapter
        }

        viewModel.tracks.observe(viewLifecycleOwner) { tracks ->
            adapter.data = tracks //передаём данные в адаптер
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}