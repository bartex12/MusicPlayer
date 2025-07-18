package com.example.muzpleer.ui.local

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.muzpleer.R
import com.example.muzpleer.databinding.FragmentLocalBinding
import com.example.muzpleer.ui.player.PlayerFragment
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class LocalFragment : Fragment() {
    private var _binding: FragmentLocalBinding? = null
    private val binding get() = _binding!!
    private val viewModel: LocalMusicViewModel by viewModel()
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
            val playlist = viewModel.musicList.value
            // Обработка клика по треку
            findNavController().navigate(
                R.id.action_localFragment_to_playerFragment,
                PlayerFragment.newInstance(track, playlist).arguments
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

            viewModel.progress.observe(viewLifecycleOwner) {isShow ->
                if(isShow){
                    Log.d(TAG, " 1 LocalFragment onViewCreated observe: progress= $isShow  ")
                    binding.progressBar.visibility = View.VISIBLE
                }else{
//                    Log.d(TAG, " 2 LocalFragment onViewCreated observe: progress= $isShow   ")
//                    binding.progressBar.visibility = View.GONE
                }
            }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.musicList.collect { tracks ->
                        Log.d(TAG, "3 LocalFragment onViewCreated musicList.collect: tracks.size= ${tracks.size} ")
                        if (tracks.isEmpty()) {
                            binding.progressBar.visibility = View.VISIBLE
                            binding.imageHolder3.visibility = View.VISIBLE
                            Log.d(TAG, "4 LocalFragment onViewCreated musicList.collect: progressBar.visibility = View.VISIBLE ")
                        }else{
                            binding.progressBar.visibility = View.GONE
                            binding.imageHolder3.visibility = View.GONE
                        }
                        adapter.data = tracks  //передаём данные в адаптер
                        viewModel.setProgress(false)
                    }
                }
            }
        }
        viewModel.loadLocalMusic()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "33333"
    }
}