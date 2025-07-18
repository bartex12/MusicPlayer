package com.example.muzpleer.ui.player

import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.example.muzpleer.SharedViewModel
import com.example.muzpleer.R
import com.example.muzpleer.databinding.FragmentPlayerBinding
import com.example.muzpleer.model.MusicTrack
import com.example.muzpleer.util.ProgressState
import com.example.muzpleer.util.formatDuration
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import kotlin.getValue

class PlayerFragment : Fragment() {
    private var _binding: FragmentPlayerBinding? = null
    private val binding get() = _binding!!
    private val viewModel: PlayerViewModel by viewModel()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPlayerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        arguments?.let { bundle ->
            val track = bundle.getParcelable<MusicTrack>(ARG_TRACK) ?: return@let
            val playlist = bundle.getParcelableArrayList<MusicTrack>(ARG_PLAYLIST) ?: listOf(track)

            viewModel.setPlaylist(playlist, playlist.indexOfFirst { it.mediaUri == track.mediaUri  })
        }
        setupControls()
        observeViewModel()
    }

    private fun observeViewModel() {

        viewModel.currentTrack.observe(viewLifecycleOwner) { track ->
            track?.let {
                binding.titleTextView.text = it.title
                binding.artistTextView.text = it.artist

                Glide.with(requireContext())
                    .load(it.artworkUri)
                    .placeholder(R.drawable.placeholder2)
                    .into(binding.artworkImageView)
            }
        }

        viewModel.isPlaying.observe(viewLifecycleOwner) { isPlaying ->
            binding.playPauseButton.setImageResource(
                if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play
            )
        }

        viewModel.currentPosition.observe(viewLifecycleOwner) { position ->
            binding.currentTimeTextView.text = formatTime(position)
            binding.seekBar.progress = position.toInt()
        }

        viewModel.duration.observe(viewLifecycleOwner) { duration ->
            binding.durationTextView.text = formatTime(duration)
            binding.seekBar.max = duration.toInt()
        }
    }


    private fun setupControls() {
        binding.playPauseButton.setOnClickListener {
            viewModel.togglePlayPause()
        }

        binding.previousButton.setOnClickListener {
            // Переход к предыдущему треку
            viewModel.playPrevious()
        }

        binding.nextButton.setOnClickListener {
            // Переход к следующему треку
            viewModel.playNext()
        }

        binding.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    viewModel.seekTo(progress.toLong())
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })
    }

//    private fun setupPlayer() {
//        // Получаем список треков из предыдущего фрагмент
//        val initialIndex = playlist.indexOfFirst { it.mediaUri == currentTrack.mediaUri }
//
//        viewModel.setPlaylist(playlist, initialIndex)
//    }

    private fun formatTime(millis: Long): String {
        val seconds = (millis / 1000) % 60
        val minutes = (millis / (1000 * 60)) % 60
        return String.format("%02d:%02d", minutes, seconds)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onDestroy() {
        super.onDestroy()
        // Освобождаем ресурсы плеера
        viewModel.releasePlayer()
    }

    companion object {
        const val TAG = "33333"
        private const val ARG_TRACK = "track"
        private const val ARG_PLAYLIST = "playlist"

        fun newInstance(track: MusicTrack, playlist: List<MusicTrack>): PlayerFragment {
            return PlayerFragment().apply {
                arguments = bundleOf(
                    ARG_TRACK to track,
                    ARG_PLAYLIST to ArrayList(playlist))
            }
        }
    }
}

