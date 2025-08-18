package com.example.muzpleer.ui.player

import android.content.ContentUris
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.core.net.toUri
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.muzpleer.R
import com.example.muzpleer.databinding.FragmentPlayerBinding
import com.example.muzpleer.model.Song
import com.example.muzpleer.ui.local.viewmodel.SharedViewModel
import com.google.android.material.snackbar.Snackbar
import org.koin.androidx.viewmodel.ext.android.activityViewModel

class PlayerFragment : Fragment() {
    private var _binding: FragmentPlayerBinding? = null
    private val binding get() = _binding!!
    private val viewModel: SharedViewModel by activityViewModel()

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

        setupControls()
        observeViewModel()
        // Скрыть плеер при открытии фрагмента
        hideActivityPlayer()
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

        binding.rewindBackButton.setOnClickListener {
            viewModel.seekRelative(-5000) // -5 секунд
        }
        binding.rewindForwardButton.setOnClickListener {
            viewModel.seekRelative(15000) // +15 секунд
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

    private fun observeViewModel() {

        viewModel.songAndPlaylist.observe(viewLifecycleOwner) { songAndPlaylist ->
            Log.d(TAG, "*** PlayerFragment onViewCreated currentSong.observe: " +
                    " currentSong = ${songAndPlaylist.song} currentPlayList.size = ${songAndPlaylist.playlist.size}")

            val currentSong = viewModel.getCurrentSong()
            //находим индекс трека в плейлисте
            val indexOfTrack =
                songAndPlaylist.playlist.indexOfFirst {song->
                    song.mediaUri == currentSong?.mediaUri
                }
            Log.d(TAG, "*** PlayerFragment onViewCreated indexOfTrack = $indexOfTrack " +
                    "songAndPlaylist.playlist.size = ${songAndPlaylist.playlist.size}")

            viewModel.setPlaylistForHandler(songAndPlaylist.playlist, indexOfTrack)
        }

        viewModel.currentSong.observe(viewLifecycleOwner) { currentSong ->

            currentSong?. let{
                binding.tvTitle.text = it.title
                binding.tvArtist.text = it.artist
                // Загружаем обложку, если есть
                val albumArtUri = ContentUris.withAppendedId(
                    "content://media/external/audio/albumart".toUri(),
                    currentSong.albumId)


                Glide.with(requireContext())
                    .load( albumArtUri)
                    .placeholder(R.drawable.muz_player3)
                    .error(R.drawable.muz_player3)
                    .into(binding.artworkImageView)
            }
        }

        viewModel.isPlaying.observe(viewLifecycleOwner) { isPlaying ->
            binding.playPauseButton.setImageResource(
                if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play
            )
        }

        viewModel.currentPosition.observe(viewLifecycleOwner) { position ->
            binding.tvCurrentTime.text = formatTime(position)
            binding.seekBar.progress = position.toInt()
        }

        viewModel.duration.observe(viewLifecycleOwner) { duration ->
            binding.tvTotalTime.text = formatTime(duration)
            binding.seekBar.max = duration.toInt()
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { message ->
            message?.let {
                Snackbar.make(binding.root, it, Snackbar.LENGTH_LONG).show()
                viewModel.clearError()
            }
        }
    }

    private fun formatTime(millis: Long): String {
        val seconds = (millis / 1000) % 60
        val minutes = (millis / (1000 * 60)) % 60
        return String.format("%02d:%02d", minutes, seconds)
    }

    override fun onDestroyView() {
        // Показать плеер при закрытии фрагмента
        showActivityPlayer()
        _binding = null
        super.onDestroyView()
    }

    companion object {
        const val TAG = "33333"
        private const val ARG_TRACK = "track"
        private const val ARG_PLAYLIST = "playlist"

        fun newInstance(track: Song, playlist: List<Song>): PlayerFragment {
            return PlayerFragment().apply {
                arguments = bundleOf(
                    ARG_TRACK to track,
                    ARG_PLAYLIST to ArrayList(playlist))
            }
        }
    }

    private fun hideActivityPlayer() {
        viewModel.setPlayerVisibility(false)
    }

    private fun showActivityPlayer() {
        viewModel.setPlayerVisibility(true)
    }
}

