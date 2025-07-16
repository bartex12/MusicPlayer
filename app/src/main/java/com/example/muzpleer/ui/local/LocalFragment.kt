package com.example.muzpleer.ui.local

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.muzpleer.R
import androidx.fragment.app.Fragment
import org.koin.androidx.viewmodel.ext.android.viewModel
import androidx.navigation.fragment.findNavController
import com.example.muzpleer.databinding.FragmentLocalBinding
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.launch
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.muzpleer.SharedViewModel
import kotlin.getValue

class LocalFragment : Fragment() {
    private var _binding: FragmentLocalBinding? = null
    private val binding get() = _binding!!
    private val viewModel: LocalMusicViewModel by viewModel()
    private val sharedViewModel: SharedViewModel by activityViewModels()
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
            // Обработка клика по треку
            findNavController().navigate(
                R.id.action_localFragment_to_playerFragment,
                bundleOf("mediaItem" to track)
            )
        }

        binding.localRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@LocalFragment.adapter
        }

        binding.telefonMusicButton.setOnClickListener {
            findNavController().navigate(
                R.id.action_localFragment_to_tracksFragment)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.musicList.collect { tracks ->
                    if (tracks.isEmpty()) {
                        binding.tvEmpty.visibility = View.VISIBLE
                    }
                    adapter.data = tracks  //передаём данные в адаптер
                    sharedViewModel.setPlaylist(tracks)  //передаём плейлист в sharedViewModel
                }
            }
        }
        viewModel.loadLocalMusic()
    }
    companion object {
        private const val PERMISSION_REQUEST_CODE = 1001
        const val TAG = "33333"
    }
}