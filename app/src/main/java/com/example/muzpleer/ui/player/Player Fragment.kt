package com.example.muzpleer.ui.player

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.media3.exoplayer.ExoPlayer
import com.bumptech.glide.Glide
import com.example.muzpleer.R
import com.example.muzpleer.databinding.FragmentPlayerBinding
import com.example.muzpleer.model.MusicTrack
import com.google.android.material.snackbar.Snackbar
import org.koin.android.ext.android.inject

class PlayerFragment : Fragment() {
    private var _binding: FragmentPlayerBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: PlayerViewModel
    private val exoPlayer: ExoPlayer by inject()

    private lateinit var track: MusicTrack
    private lateinit var playlist: List<MusicTrack>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPlayerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            track = it.getParcelable("track") ?: throw IllegalStateException("Track argument is required")
            playlist = it.getParcelableArrayList("playlist") ?: listOf(track)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Инициализируем ViewModel с контекстом приложения
        viewModel = ViewModelProvider(
            this,
            PlayerViewModelFactory(requireActivity().application, exoPlayer)
        )[PlayerViewModel::class.java]

        viewModel.setPlaylist(playlist, playlist.indexOfFirst { it.mediaUri == track.mediaUri  })

        setupControls()
        observeViewModel()
    }

    override fun onResume() {
        super.onResume()
        // Восстанавливаем воспроизведение при возвращении на фрагмент
        viewModel.currentTrack.value?.let {
            viewModel.playTrack(it)
        }
    }

    override fun onPause() {
        super.onPause()
        // Приостанавливаем воспроизведение при уходе с фрагмента
        viewModel.togglePlayPause()
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

        viewModel.errorMessage.observe(viewLifecycleOwner) { message ->
            message?.let {
                Snackbar.make(binding.root, it, Snackbar.LENGTH_LONG).show()
                viewModel.clearError()
            }
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
        Log.d(TAG, "PlayerFragment onDestroy:  ")
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

