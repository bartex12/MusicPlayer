package com.example.muzpleer.ui.player

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.bumptech.glide.Glide
import com.example.muzpleer.R
import com.example.muzpleer.databinding.FragmentPlayerBinding
import com.example.muzpleer.model.MediaItem
import com.example.muzpleer.util.PlaybackProgress
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class PlayerFragment : Fragment() {
    private var _binding: FragmentPlayerBinding? = null
    private val binding get() = _binding!!
    private val viewModel: PlayerViewModel by viewModel()
    private var isSeeking = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentPlayerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
        handleArguments()
        observeViewModel()
    }

    private fun setupUI() {
        with(binding) {
            playPauseButton.setOnClickListener {
                //Log.d(TAG, "PlayerFragment setupUI: playPauseButton = Click ")
                viewModel.togglePlayPause()
            }
            previousButton.setOnClickListener { viewModel.skipToPrevious() }
            nextButton.setOnClickListener { viewModel.skipToNext() }

            seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                    if (fromUser) {
                        currentTimeTextView.text = formatDuration(progress.toLong())
                    }
                }

                override fun onStartTrackingTouch(seekBar: SeekBar) {
                    isSeeking = true
                }

                override fun onStopTrackingTouch(seekBar: SeekBar) {
                    viewModel.seekTo(seekBar.progress.toLong())
                    isSeeking = false
                }
            })
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                   // Закончилась ли инициализация плеера
                    viewModel.isInitialized.collect { isReady ->
                        binding.playPauseButton.isEnabled = isReady
                        //if (!isReady) //showLoadingIndicator()
                    }
                }

                // Observe current media item
                launch {
                    viewModel.currentMediaItem.collect { mediaItem ->
                        mediaItem?.let { updateMediaInfo(it) }
                    }
                }

                // Observe playback state
                launch {
                    viewModel.playbackState.collect { state ->
                        Log.d(TAG, "PlayerFragment observeViewModel: state = $state ")
                        updatePlaybackStateUI(state)
                    }
                }

                // Observe progress
                launch {
                    viewModel.progress.collect { progress ->
                        if (!isSeeking) {
                            updateProgress(progress)
                        }
                    }
                }

                // Observe errors
                launch {
                    viewModel.errorMessage.collect { error ->
                        error?.let { showError(it) }
                    }
                }
            }
        }
    }

    private fun handleArguments() {
        arguments?.getParcelable<MediaItem>("mediaItem")?.let {
            viewModel.setMediaItem(it)
            Log.d(TAG, "PlayerFragment handleArguments: mediaItem = ${it.title} ")
        } ?: run {
            showError("No media item provided")
        }
    }

    private fun updateMediaInfo(mediaItem: MediaItem) {
        with(binding) {
            titleTextView.text = mediaItem.title
            artistTextView.text = mediaItem.artist
            // Здесь можно загрузить обложку, если она есть
            //Glide.with(artworkImageView).load(mediaItem.artworkUri).into(artworkImageView)
        Glide.with(binding.artworkImageView)
            .load(mediaItem.cover)
            .into(binding.artworkImageView)
        }
    }

    private fun updatePlaybackStateUI(state: PlaybackState) {
        with(binding) {
            //Log.d(TAG, "PlayerFragment updatePlaybackStateUI: state = ${state.toString()} ")
            playPauseButton.setImageResource(
                when (state) {
                    PlaybackState.PLAYING -> R.drawable.ic_pause
                    else -> R.drawable.ic_play
                }
            )
        }
    }

    private fun updateProgress(progress: PlaybackProgress) {
        with(binding) {
            seekBar.max = progress.duration.toInt()
            seekBar.progress = progress.currentPosition.toInt()
            currentTimeTextView.text = formatDuration(progress.currentPosition)
            durationTextView.text = formatDuration(progress.duration)
        }
    }

    private fun showError(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    private fun formatDuration(millis: Long): String {
        // Format to mm:ss
        return ""
    }

    companion object {
        const val TAG = "33333"
    }
}
