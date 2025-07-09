package com.example.muzpleer.ui.tracks

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.muzpleer.R
import com.example.muzpleer.databinding.FragmentTracksBinding
import org.koin.androidx.viewmodel.ext.android.viewModel

class TracksFragment : Fragment() {
    private var _binding: FragmentTracksBinding? = null
    private val binding get() = _binding!!
    private val viewModel: TracksViewModel by viewModel()
    private lateinit var adapter: TracksAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentTracksBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = TracksAdapter { mediaItem ->
            // Navigate to player
            findNavController().navigate(
                R.id.action_tracksFragment_to_playerFragment,
                bundleOf("mediaItem" to mediaItem)
            )
        }

        binding.tracksRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@TracksFragment.adapter
        }

        viewModel.loadData()

        viewModel.tracks.observe(viewLifecycleOwner) { tracks ->
            adapter.data = tracks
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
