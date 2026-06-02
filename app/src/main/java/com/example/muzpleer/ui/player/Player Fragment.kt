package com.example.muzpleer.ui.player

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.SeekBar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.net.toUri
import androidx.core.os.bundleOf
import androidx.documentfile.provider.DocumentFile
import androidx.fragment.app.Fragment
import androidx.transition.ChangeBounds
import androidx.transition.ChangeImageTransform
import androidx.transition.TransitionManager
import androidx.transition.TransitionSet
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.example.muzpleer.R
import com.example.muzpleer.databinding.FragmentPlayerBinding
import com.example.muzpleer.model.Song
import com.example.muzpleer.ui.local.viewmodel.SharedViewModel
import com.google.android.material.snackbar.Snackbar
import org.koin.androidx.viewmodel.ext.android.activityViewModel
import java.io.File
import java.io.FileOutputStream

class PlayerFragment : Fragment() {
    private var _binding: FragmentPlayerBinding? = null
    private val binding get() = _binding!!
    private val viewModel: SharedViewModel by activityViewModel()
    private var isExpanded = false

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
       // viewModel.getCurrentSong()?. let{viewModel.setCurrentSong(it)}
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

        binding.artworkImageView.setOnClickListener {
            isExpanded = !isExpanded
            //
            TransitionManager.beginDelayedTransition(
                binding.playerContainer, TransitionSet()
                    .addTransition(ChangeBounds())
                    .addTransition(ChangeImageTransform())
            )
            val params = binding.artworkImageView.layoutParams as ConstraintLayout.LayoutParams
            if (isExpanded) {
                // Оставляем существующие constraints, но меняем размеры
                params.topMargin = 0
                params.width = ConstraintLayout.LayoutParams.MATCH_PARENT
                params.height = ConstraintLayout.LayoutParams.MATCH_PARENT
                params.dimensionRatio = ""
                binding.artworkImageView.scaleType = ImageView.ScaleType.FIT_CENTER
            } else {
                // Возвращаем оригинальные размеры
                params.topMargin = 16
                params.width = 0
                params.height = 0
                params.dimensionRatio = "1:1"
                params.matchConstraintPercentWidth = 0.6f
                binding.artworkImageView.scaleType = ImageView.ScaleType.FIT_CENTER
            }

            binding.artworkImageView.layoutParams = params
        }
    }

    private fun observeViewModel() {

        viewModel.songAndPlaylist.observe(viewLifecycleOwner) { songAndPlaylist ->
            Log.d(TAG, "1*** PlayerFragment onViewCreated songAndPlaylist.observe: " +
                    " currentSong title = ${songAndPlaylist.song.title} " +
                    " currentSong.artUri = ${songAndPlaylist.song.artUri}" +
                    " playlist.size = ${songAndPlaylist.playlist.size}")
            val currentSong = viewModel.getCurrentSong()
            Log.d(TAG, "2*** PlayerFragment onViewCreated songAndPlaylist.observe: " +
                    " currentSong title = ${currentSong?.title}  " +
                    " currentSong.artUri = ${currentSong?.artUri}")

            binding.tvTitle.text = currentSong?.title
            binding.tvArtist.text =currentSong?.artist

            //находим индекс трека в плейлисте
            val indexOfTrack =
                songAndPlaylist.playlist.indexOfFirst {song->
                    song.mediaUri == currentSong?.mediaUri
                }
            //перед передачей в Handler меняем в передаваемом плейлисте artUri для текущего трека
            songAndPlaylist.playlist[indexOfTrack].artUri =currentSong?.artUri
            val trackArtUri =  songAndPlaylist.playlist[indexOfTrack].artUri
            Log.d(TAG, "3*** PlayerFragment onViewCreated indexOfTrack = $indexOfTrack " +
                    "songAndPlaylist.playlist.size = ${songAndPlaylist.playlist.size} artUri = $trackArtUri")

            //не работает - требует права доступа
            trackArtUri?.let{artUri->

                // 🔍 ДИАГНОСТИКА - проверяем URI перед загрузкой
                checkUriPermission(artUri.toUri())

                // Загружаем изображение
                try {
                    showImageWithGlide(binding.root.context, artUri.toUri(), binding.artworkImageView)
                } catch (e: SecurityException) {
                    Log.e(TAG, "Security exception when loading: ${e.message}")
                    binding.artworkImageView.setImageResource(R.drawable.muz_player2)
                }
            }
            viewModel.setPlaylistForHandler(songAndPlaylist.playlist, indexOfTrack)
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

    private fun checkUriPermission(uri: Uri) {
        try {
            Log.d(TAG, "=== Checking URI permission ===")
            Log.d(TAG, "URI: $uri  Scheme: ${uri.scheme}  Authority: ${uri.authority}")

            // Проверяем persisted permissions
            val perms = requireContext().contentResolver.persistedUriPermissions
            Log.d(TAG, "Persisted permissions count: ${perms.size}")
            perms.forEach { perm ->
                Log.d(TAG, "  Permission: ${perm.uri}, read: ${perm.isReadPermission}, write: ${perm.isWritePermission}")
                if (perm.uri.toString() == uri.toString()) {
                    Log.d(TAG, "  ✅ Found matching permission!")
                }
            }

            // Проверяем, можем ли открыть InputStream
            try {
                val inputStream = requireContext().contentResolver.openInputStream(uri)
                if (inputStream != null) {
                    Log.d(TAG, "✅ Can open InputStream successfully!")
                    inputStream.close()
                } else {
                    Log.d(TAG, "❌ Cannot open InputStream (null)")
                }
            } catch (e: SecurityException) {
                Log.e(TAG, "❌ SecurityException: ${e.message}")
                throw e
            }

            // Проверяем, существует ли файл
            try {
                val file = File(uri.path ?: "")
                Log.d(TAG, "File exists: ${file.exists()}, can read: ${file.canRead()}")
            } catch (e: Exception) {
                Log.d(TAG, "Cannot check file: ${e.message}")
            }

        } catch (e: Exception) {
            Log.e(TAG, "❌ Error in diagnostic: ${e.message}")
        }
    }

    fun showImageWithGlide(context: Context, artUri: Uri, imageView: ImageView){
        // Загрузка обложки
            Glide.with(context)
                .load(artUri)
                .placeholder(R.drawable.muz_player3)
                .error(R.drawable.muz_player2)
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                .addListener(object : RequestListener<Drawable> {
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: com.bumptech.glide.request.target.Target<Drawable?>,
                        isFirstResource: Boolean
                    ): Boolean {
                        Log.e(TAG, " ❌ Glide load failed for URI: $artUri", e)
                        return false
                    }

                    override fun onResourceReady(
                        resource: Drawable,
                        model: Any,
                        target: com.bumptech.glide.request.target.Target<Drawable?>?,
                        dataSource: DataSource,
                        isFirstResource: Boolean
                    ): Boolean {
                        Log.d(TAG, "✅Glide load success for URI: $artUri")
                        Log.d(TAG, "✅ DataSource: $dataSource") // 👈 Важно! Покажет откуда загружено
                        // Если загружено не из кэша, сохраняем локальную копию
//                        if (dataSource != DataSource.DATA_DISK_CACHE &&
//                            dataSource != DataSource.RESOURCE_DISK_CACHE &&
//                            dataSource != DataSource.MEMORY_CACHE) {
//
//                            // Конвертируем Drawable в Bitmap
//                            val bitmap = (resource as BitmapDrawable).bitmap
//                            val songId = viewModel.getCurrentSong()?.id ?: return false
//
//                            // Сохраняем локальную копию и обновляем URI в базе
//                            val localPath = saveArtworkToCache(artUri, bitmap, songId)
//                            viewModel.updateSongArtUri(songId, "file://$localPath")
//                        }
                        return false
                    }
                })
                .into(imageView)
    }

    private fun saveArtworkToCache(uri: Uri, bitmap: Bitmap, songId: Long): String {
        val cacheDir = File(requireContext().cacheDir, "album_art")
        if (!cacheDir.exists()) cacheDir.mkdirs()

        val cacheFile = File(cacheDir, "song_$songId.jpg")
        FileOutputStream(cacheFile).use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
        }

        return cacheFile.absolutePath
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
