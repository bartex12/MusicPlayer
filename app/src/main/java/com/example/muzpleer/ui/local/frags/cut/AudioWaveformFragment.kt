package com.example.muzpleer.ui.local.frags.cut

import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.muzpleer.R
import com.example.muzpleer.databinding.FragmentAudioWaveformBinding
import com.example.muzpleer.model.cut.AmplitudePoint
import com.example.muzpleer.model.cut.AudioInfo
import com.example.muzpleer.model.cut.ProcessingState
import com.example.muzpleer.ui.local.helper.TimeAxisFormatter
import com.example.muzpleer.ui.local.viewmodel.SharedViewModel
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.highlight.Highlight
import org.koin.androidx.viewmodel.ext.android.activityViewModel
import java.io.File

class  AudioWaveformFragment : Fragment() {
    private lateinit var binding: FragmentAudioWaveformBinding
    private val viewModel: SharedViewModel by activityViewModel()

    private var audioFilePath: String? = null
    private var isPlaying = false
    private var audioDuration = 0L
    private var selectionStartTime = 0f
    private var selectionEndTime = 0f

    private var isPlayingSelection = false // флаг воспроизведения выделенного отрезка
    private var selectionDuration = 0f // длительность выделения в секундах
    private var isPlaybackStarted = false // новый флаг

    private var playbackUpdateHandler = Handler(Looper.getMainLooper())
    private var playbackUpdateRunnable: Runnable? = null

    companion object {
        private const val TAG = "33333"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAudioWaveformBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        audioFilePath  = arguments?.getString("audioPath")
        if (audioFilePath.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "Аудиофайл не найден", Toast.LENGTH_SHORT).show()
            findNavController().navigateUp()
            return
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupChart()
        setupControls()
        setupObservers()
        loadAudioData()
        resetSelection()  // Инициализируем выделение

        // Устанавливаем callback для начала воспроизведения
        viewModel.setPlaybackStartedCallback {
            Log.d(TAG, "%% Playback started callback called")
            requireActivity().runOnUiThread {
                isPlaybackStarted = true
                Log.d(TAG, "%% Playback confirmed started, starting progress updates")
                startPlaybackProgressUpdates()
            }
        }

        // Устанавливаем callback для завершения воспроизведения
        viewModel.setPlaybackCompletionCallback {
            Log.d(TAG, "%% Playback completion callback called")
            requireActivity().runOnUiThread {
                isPlaying = false
                isPlayingSelection = false
                isPlaybackStarted = false
                binding.btnPlayPause.setImageResource(R.drawable.ic_play_arrow)
                stopPlaybackProgressUpdates()

                // Сбрасываем прогресс в конец
                binding.seekBarPlayback.progress = 1000
                Log.d(TAG, "%% Playback completed, UI updated")
            }
        }
    }

    private fun resetSelection() {
        selectionStartTime = 0f
        // selectionEndTime установится после загрузки аудио в updateAudioInfo()
    }


    private fun setupChart() {
        with(binding.lineChart) {
            description.isEnabled = false
            setTouchEnabled(true)
            isDragEnabled = true
            setScaleEnabled(true)
            setPinchZoom(true)
            setDrawBorders(true)
            setBorderColor(Color.GRAY)

            // Настройка осей
            xAxis.position = XAxis.XAxisPosition.BOTTOM
            xAxis.granularity = 0.1f
            xAxis.valueFormatter = TimeAxisFormatter()
            xAxis.textColor = Color.BLACK
            xAxis.setDrawGridLines(true)
            xAxis.gridColor = Color.LTGRAY

            // Настройка оси Y - от -1 до 1 для симметричного отображения
            axisLeft.axisMinimum = -1f
            axisLeft.axisMaximum = 1f
            axisLeft.granularity = 0.2f
            axisLeft.setDrawGridLines(true)
            axisLeft.gridColor = Color.LTGRAY
            axisLeft.textColor = Color.BLACK

            axisRight.isEnabled = false

            legend.isEnabled = false
        }
    }

    private fun setupControls() {
        // Кнопка воспроизведения/паузы
        binding.btnPlayPause.setOnClickListener {
            togglePlayback()
        }

        // Настройка двух SeekBar
        setupSeekBars()

        binding.btnTrim.setOnClickListener {
            trimAudio()
        }
    }

    private fun setupSeekBars() {
        binding.seekBarPlayback.max = 1000
        binding.seekBarPlayback.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                if (fromUser && audioDuration > 0) {
                    if (isPlayingSelection && selectionDuration > 0) {
                        // Перемотка в пределах выделенного отрезка
                        val timeInSelection = progress * selectionDuration / 1000f
                        val absoluteTime = selectionStartTime + timeInSelection
                        seekToPosition(absoluteTime)
                    } else {
                        // Перемотка по всему треку
                        val time = progress * audioDuration / 1000f
                        seekToPosition(time)
                    }
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
                // Пауза при начале перемотки
                if (isPlaying) {
                    pausePlayback()
                }
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                // Автоматически не возобновляем воспроизведение после перемотки
                // Пользователь сам решит когда нажать play
            }
        })

        setupSelectionSeekBars()
    }

    private fun seekToPosition(time: Float) {
        viewModel.seekTo(time)
        updateCurrentTime(time)
    }

    private fun updateCurrentTime(timeInSeconds: Float) {
        if (isPlayingSelection && selectionDuration > 0) {
            // Обновляем прогресс относительно выделенного отрезка
            val progressInSelection = ((timeInSeconds - selectionStartTime) * 1000 / selectionDuration).toInt()
            binding.seekBarPlayback.progress = progressInSelection.coerceIn(0, 1000)
        } else {
            // Обновляем прогресс относительно всего трека
            val progress = (timeInSeconds * 1000 * 1000 / audioDuration).toInt()
            binding.seekBarPlayback.progress = progress
        }
    }

    //обработчик изменения выделения
    private fun setupSelectionSeekBars() {
        binding.seekBarStart.max = 1000
        binding.seekBarEnd.max = 1000

        val seekBarListener = object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                if (fromUser && audioDuration > 0) {
                    // ПРАВИЛЬНОЕ преобразование: progress -> время в секундах
                    val timeInSeconds = (progress * audioDuration / 1000f) / 1000f
                    Log.d(TAG, "%%  AudioWaveformFragment setupSelectionSeekBars " +
                            "SeekBar progress: $progress, audioDuration: $audioDuration, timeInSeconds: $timeInSeconds")
                    when (seekBar.id) {
                        R.id.seekBarStart -> {
                            selectionStartTime = timeInSeconds
                            binding.tvCurrentTimeStart.text = formatTime((timeInSeconds  * 1000).toLong())
                            if (selectionStartTime > selectionEndTime) {
                                selectionStartTime = selectionEndTime
                                binding.seekBarStart.progress = (selectionStartTime * 1000 * 1000 / audioDuration).toInt()
                            }
                        }
                        R.id.seekBarEnd -> {
                            selectionEndTime = timeInSeconds
                            binding.tvCurrentTimeEnd.text = formatTime((timeInSeconds  * 1000).toLong())
                            if (selectionEndTime < selectionStartTime) {
                                selectionEndTime = selectionStartTime
                                binding.seekBarEnd.progress = (selectionEndTime * 1000 * 1000 / audioDuration).toInt()
                            }
                        }
                    }

                    // При изменении выделения сбрасываем флаг воспроизведения выделенного отрезка
                    if (isPlayingSelection) {
                        Log.d(TAG, "%% Selection changed during playback, stopping")
                        isPlayingSelection = false
                        if (isPlaying) {
                            pausePlayback()
                        }
                    }

                    updateSelectionInfo()
                    highlightSelectionOnChart()
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        }

        binding.seekBarStart.setOnSeekBarChangeListener(seekBarListener)
        binding.seekBarEnd.setOnSeekBarChangeListener(seekBarListener)
    }

    private fun setupObservers() {
        viewModel.amplitudes.observe(viewLifecycleOwner) { amplitudes ->
            plotAmplitudes(amplitudes)
            binding.progressBar.visibility = View.GONE
        }

        viewModel.audioInfo.observe(viewLifecycleOwner) { audioInfo ->
            audioInfo?.let {
                audioDuration = it.duration
                updateAudioInfo(it)
            }
        }

        viewModel.processingState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is ProcessingState.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.btnTrim.isEnabled = false
                }
                is ProcessingState.Success -> {
                    binding.progressBar.visibility = View.GONE
                    binding.btnTrim.isEnabled = true
                    Toast.makeText(requireContext(), "Аудио успешно обработано", Toast.LENGTH_SHORT).show()
                }
                is ProcessingState.Error -> {
                    binding.progressBar.visibility = View.GONE
                    binding.btnTrim.isEnabled = true
                    Toast.makeText(requireContext(), "Ошибка: ${state.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun loadAudioData() {
        audioFilePath?.let { path ->
            binding.progressBar.visibility = View.VISIBLE
            viewModel.loadAudioData(path)

            Log.d(TAG, "%% AudioWaveformFragment loadAudioData: audioFilePath = $path ")
            // Устанавливаем название файла
            val fileName = File(path).name
            binding.tvFileName.text = fileName
        }
    }

    private fun plotAmplitudes(amplitudes: List<AmplitudePoint>) {
        if (amplitudes.isEmpty()) return

        val entries = amplitudes.map { Entry(it.time, it.amplitude) }

        val dataSet = LineDataSet(entries, "Амплитуда")
        dataSet.color = Color.BLUE
        dataSet.setDrawCircles(false)
        dataSet.lineWidth = 1.5f
        dataSet.setDrawValues(false)
        dataSet.setDrawFilled(true)
        dataSet.fillColor = Color.BLUE
        dataSet.fillAlpha = 50

        val lineData = LineData(dataSet)
        binding.lineChart.data = lineData

        binding.lineChart.axisLeft.axisMinimum = -1f
        binding.lineChart.axisLeft.axisMaximum = 1f

        binding.lineChart.invalidate()

        // После построения графика обновляем выделение
        highlightSelectionOnChart()
    }

    // Обновляем метод при загрузке данных аудио
    private fun updateAudioInfo(audioInfo: AudioInfo) {
        audioDuration = audioInfo.duration
        val file = File(audioInfo.filePath)
        val fileSize = String.format("%.2f MB", file.length().toDouble() / 1024 / 1024)

        binding.tvFileInfo.text = "Длительность: ${formatTime(audioDuration)} | Размер: $fileSize"
        binding.tvTotalTime.text = formatTime(audioDuration)

        // Устанавливаем конечное время
        selectionEndTime = audioDuration / 1000f // переводим в секунды
        binding.seekBarEnd.progress = 1000
        binding.tvCurrentTimeEnd.text = formatTime(audioDuration)
        binding.tvCurrentTimeStart.text = formatTime(0)

        updateSelectionInfo()
        highlightSelectionOnChart() // Обновляем график после загрузки данных
    }

    private fun togglePlayback() {
        Log.d(TAG, "%% togglePlayback called, isPlaying: $isPlaying")

        if (isPlaying) {
            // Останавливаем воспроизведение
            pausePlayback()
        } else {
            // Начинаем воспроизведение
            startPlayback()
        }
    }

    private fun updateTimeDisplayForSelection() {
        // Для выделенного отрезка показываем его длительность
        val selectionDurationMs = (selectionDuration * 1000).toLong()
        binding.tvTotalTime.text = formatTime(selectionDurationMs)
    }

    private fun updateTimeDisplayForFullTrack() {
        // Для всего трека показываем полную длительность
        binding.tvTotalTime.text = formatTime(audioDuration)
    }

    private fun startPlayback() {
        Log.d(TAG, "1%% startPlayback called isPlaying = $isPlaying")

        isPlaying = true
        isPlaybackStarted = false // сбрасываем флаг начала воспроизведения
        binding.btnPlayPause.setImageResource(R.drawable.ic_pause)
        Log.d(TAG, "2%% startPlayback called isPlaying = $isPlaying")

        // Определяем что воспроизводить
        if (selectionEndTime > selectionStartTime) {
            // Воспроизводим выделенный отрезок
            isPlayingSelection = true
            selectionDuration = selectionEndTime - selectionStartTime
            Log.d(TAG, "3%% Playing selection: $selectionStartTime - $selectionEndTime c, duration: $selectionDuration c")

            viewModel.startPlayback(selectionStartTime, selectionEndTime)
            // Обновляем отображение времени для выделенного отрезка
            updateTimeDisplayForSelection()
        } else {
            // Воспроизводим весь трек
            isPlayingSelection = false
            Log.d(TAG, "%% Playing full track")
            viewModel.startPlayback(0f, audioDuration / 1000f)
            // Обновляем отображение времени для всего трека
            updateTimeDisplayForFullTrack()
        }

        // Сбрасываем прогресс на начало
        binding.seekBarPlayback.progress = 0

        // НЕ запускаем обновление прогресса здесь - дождемся callback
        Log.d(TAG, "%% Waiting for playback to actually start...")
    }

    private fun pausePlayback() {
        Log.d(TAG, "%% pausePlayback called")

        isPlaying = false
        isPlaybackStarted = false
        binding.btnPlayPause.setImageResource(R.drawable.ic_play_arrow)
        viewModel.pausePlayback()
        stopPlaybackProgressUpdates()
    }

    private fun trimAudio() {
        if (selectionStartTime >= selectionEndTime) {
            Toast.makeText(requireContext(), "Выделите корректный интервал", Toast.LENGTH_SHORT).show()
            return
        }

        val outputPath = "${requireContext().externalCacheDir?.absolutePath}/trimmed_${System.currentTimeMillis()}.mp3"
        viewModel.trimAudio(audioFilePath!!, outputPath, selectionStartTime.toDouble(), selectionEndTime.toDouble())
    }

    private fun updateSelectionInfo() {
        val durationMs = ((selectionEndTime - selectionStartTime) * 1000).toLong()
        selectionDuration = selectionEndTime - selectionStartTime

        binding.tvSelectionDuration.text = "Длительность выделения: ${formatTime(durationMs)}"

        // Если выделение валидно, обновляем отображение времени для seekBar
        if (selectionEndTime > selectionStartTime) {
            binding.tvTotalTime.text = formatTime(durationMs)
        } else {
            binding.tvTotalTime.text = formatTime(audioDuration)
        }

        val hasValidSelection = selectionEndTime > selectionStartTime
        binding.btnTrim.isEnabled = hasValidSelection

        // Визуальная индикация
        if (hasValidSelection) {
            binding.tvSelectionDuration.setBackgroundColor(
                ContextCompat.getColor(requireContext(), R.color.selection_background)
            )
            binding.tvSelectionDuration.setTextColor(
                ContextCompat.getColor(requireContext(), R.color.colorPrimaryDark)
            )
        } else {
            binding.tvSelectionDuration.setBackgroundColor(Color.TRANSPARENT)
            binding.tvSelectionDuration.setTextColor(
                ContextCompat.getColor(requireContext(), android.R.color.darker_gray)
            )
        }
    }

    private fun highlightSelectionOnChart() {
        val chart = binding.lineChart
        Log.d(TAG, "%% AudioWaveformFragment highlightSelectionOnChart Setting highlights:" +
                " start=$selectionStartTime, end=$selectionEndTime")

        // ИСПРАВЛЕНИЕ: используем правильные переменные
        val highlightStart = Highlight(selectionStartTime, 0f, 0) // xValue, yValue, dataSetIndex
        val highlightEnd = Highlight(selectionEndTime, 0f, 0)

        // Устанавливаем оба маркера
        chart.highlightValues(arrayOf(highlightStart, highlightEnd))

        // Добавляем визуальное выделение области
        addSelectionArea(selectionStartTime, selectionEndTime)

        chart.invalidate()

        Log.d(TAG, "%%  AudioWaveformFragment highlightSelectionOnChart " +
                "Highlight selection: $selectionStartTime - $selectionEndTime")
    }

    private fun addSelectionArea(startTime: Float, endTime: Float) {
        val chart = binding.lineChart
        val data = chart.data ?: return

        // Удаляем старую область выделения если есть
        removeSelectionArea()

        if (endTime > startTime) {
            // Создаем точки для области выделения
            val areaEntries = listOf(
                Entry(startTime, -1f),
                Entry(startTime, 1f),
                Entry(endTime, 1f),
                Entry(endTime, -1f),
                Entry(startTime, -1f)
            )

            val areaDataSet = LineDataSet(areaEntries, "Selection Area")
            areaDataSet.color = Color.TRANSPARENT
            areaDataSet.setDrawCircles(false)
            areaDataSet.setDrawValues(false)
            areaDataSet.setDrawFilled(true)
            areaDataSet.fillColor = Color.argb(50, 255, 0, 0)
            areaDataSet.fillAlpha = 80

            data.addDataSet(areaDataSet)
        }
    }

    private fun removeSelectionArea() {
        val chart = binding.lineChart
        val data = chart.data ?: return

        val selectionDataSet = data.getDataSetByLabel("Selection Area", false)
        if (selectionDataSet != null) {
            data.removeDataSet(selectionDataSet)
        }
    }


//    private fun formatTime(milliseconds: Long): String {
//        val totalSeconds = milliseconds / 1000
//        val minutes = totalSeconds / 60
//        val seconds = totalSeconds % 60
//        val ms = (milliseconds % 1000) / 10 // две цифры миллисекунд
//
//        return String.format("%d:%02d.%02d", minutes, seconds, ms)
//    }

    // Исправляем форматирование времени
    private fun formatTime(milliseconds: Long): String {
        Log.d(TAG, "5%%  AudioWaveformFragment formatTime called with: $milliseconds ms")
        val totalSeconds = milliseconds / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return String.format("%d:%02d", minutes, seconds)
    }

    override fun onDestroy() {
        super.onDestroy()
        stopPlaybackProgressUpdates()
        viewModel.release()
    }

    private fun startPlaybackProgressUpdates() {
        stopPlaybackProgressUpdates()

        playbackUpdateRunnable = object : Runnable {
            override fun run() {
                try {
                    if (isPlaying && isPlaybackStarted && viewModel.isPlaying()) {
                        updatePlaybackProgress()
                        playbackUpdateHandler.postDelayed(this, 100)
                    } else if (isPlaying && !isPlaybackStarted) {
                        // Ждем начала воспроизведения
                        Log.d(TAG, "%% Still waiting for playback to start...")
                        playbackUpdateHandler.postDelayed(this, 50)
                    } else if (!viewModel.isPlaying() && isPlaying) {
                        // Воспроизведение остановилось
                        Log.d(TAG, "%% Playback stopped unexpectedly")
                        requireActivity().runOnUiThread {
                            pausePlayback()
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error in playback update", e)
                    requireActivity().runOnUiThread {
                        pausePlayback()
                    }
                }
            }
        }
        playbackUpdateHandler.post(playbackUpdateRunnable!!)
        Log.d(TAG, "%% Playback progress updates started")
    }

    private fun stopPlaybackProgressUpdates() {
        playbackUpdateRunnable?.let {
            playbackUpdateHandler.removeCallbacks(it)
            playbackUpdateRunnable = null
        }
    }

    private fun updatePlaybackProgress() {
        try {
            val currentPosition = viewModel.getCurrentPosition()
            Log.d(TAG, "%% updatePlaybackProgress - currentPosition: $currentPosition, isPlayingSelection: $isPlayingSelection")

            if (isPlayingSelection && selectionDuration > 0) {
                // Прогресс в пределах выделенного отрезка
                val timeInSelection = (currentPosition - selectionStartTime).coerceIn(0f, selectionDuration)
                val progressInSelection = (timeInSelection * 1000 / selectionDuration).toInt()
                val clampedProgress = progressInSelection.coerceIn(0, 1000)

                Log.d(TAG, "%% Selection progress - timeInSelection: $timeInSelection, progress: $clampedProgress")

                binding.seekBarPlayback.progress = clampedProgress

                // Если дошли до конца выделенного отрезка
                if (timeInSelection >= selectionDuration) {
                    Log.d(TAG, "%% Reached end of selection, stopping")
                    requireActivity().runOnUiThread {
                        pausePlayback()
                    }
                }

            } else {
                // Прогресс по всему треку
                val progress = (currentPosition * 1000 * 1000 / audioDuration).toInt()
                val clampedProgress = progress.coerceIn(0, 1000)

                Log.d(TAG, "%% Full track progress: $clampedProgress")

                binding.seekBarPlayback.progress = clampedProgress

                // Если дошли до конца трека
                if (currentPosition >= audioDuration / 1000f) {
                    Log.d(TAG, "%% Reached end of track, stopping")
                    requireActivity().runOnUiThread {
                        pausePlayback()
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error updating playback progress", e)
            requireActivity().runOnUiThread {
                pausePlayback()
            }
        }
    }
}
