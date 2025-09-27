package com.example.muzpleer.ui.local.frags.cut

import android.graphics.Color
import android.os.Bundle
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
    private var selectionStart = 0f
    private var selectionEnd = 0f
    private var selectionStartTime = 0f
    private var selectionEndTime = 0f
    private var isSelectingStart = true

    companion object {
        private const val TAG = "33333"

        fun newInstance(audioPath: String): AudioWaveformFragment {
            return AudioWaveformFragment().apply {
                arguments = Bundle().apply {
                    putString("audioPath", audioPath)
                }
            }
        }
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
            // Убираем фон
            //setDrawGridBackground(false)
        }
    }

    private fun setupControls() {
        // Кнопка воспроизведения/паузы
        binding.btnPlayPause.setOnClickListener {
            togglePlayback()
        }

        // Настройка двух SeekBar
        setupSeekBars()

        // Кнопки действий
        binding.btnPlaySelection.setOnClickListener {
            playSelection()
        }

        binding.btnTrim.setOnClickListener {
            trimAudio()
        }
    }

    private fun setupSeekBars() {
        // SeekBar для воспроизведения
        binding.seekBarPlayback.max = 1000
        binding.seekBarPlayback.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                if (fromUser && audioDuration > 0) {
                    val time = progress * audioDuration / 1000f
                    // Обновляем позицию воспроизведения
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })

        // SeekBar для выделения начала и конца
        setupSelectionSeekBars()
    }

    private fun setupSelectionSeekBars() {
        binding.seekBarStart.max = 1000
        binding.seekBarEnd.max = 1000

        val seekBarListener = object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                if (fromUser && audioDuration > 0) {
                    val time = progress * audioDuration / 1000f

                    when (seekBar.id) {
                        R.id.seekBarStart -> {
                            selectionStartTime = time
                            binding.tvCurrentTimeStart.text = formatTime(time.toLong())
                            if (selectionStartTime > selectionEndTime) {
                                selectionStartTime = selectionEndTime
                                binding.seekBarStart.progress = (selectionStartTime * 1000 / audioDuration).toInt()
                            }
                        }
                        R.id.seekBarEnd -> {
                            selectionEndTime = time
                            binding.tvCurrentTimeEnd.text = formatTime(time.toLong())
                            if (selectionEndTime < selectionStartTime) {
                                selectionEndTime = selectionStartTime
                                binding.seekBarEnd.progress = (selectionEndTime * 1000 / audioDuration).toInt()
                            }
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

        // Инициализируем начальные значения времени
        binding.tvCurrentTimeStart.text = formatTime(0)
        binding.tvCurrentTimeEnd.text = formatTime(audioDuration)
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

        // Основная линия амплитуд
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

        // Устанавливаем конечное время в конечный SeekBar
        binding.seekBarEnd.progress = 1000
        selectionEndTime = audioDuration / 1000f
        binding.tvCurrentTimeEnd.text = formatTime(audioDuration)

        updateSelectionInfo()
    }
    private fun togglePlayback() {
        isPlaying = !isPlaying

        if (isPlaying) {
            binding.btnPlayPause.setImageResource(R.drawable.ic_pause)
            // Воспроизводим выделенный отрезок или весь трек
            if (selectionEndTime > selectionStartTime) {
                viewModel.startPlayback(selectionStartTime, selectionEndTime)
            } else {
                viewModel.startPlayback(0f, audioDuration / 1000f)
            }
        } else {
            binding.btnPlayPause.setImageResource(R.drawable.ic_play_arrow)
            viewModel.pausePlayback()
        }
    }

    private fun playSelection() {
        if (selectionStartTime >= selectionEndTime) {
            Toast.makeText(requireContext(), "Некорректный интервал", Toast.LENGTH_SHORT).show()
            return
        }

        viewModel.playSelection(selectionStartTime, selectionEndTime)
        binding.btnPlayPause.setImageResource(R.drawable.ic_pause)
        isPlaying = true
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
        val duration = (selectionEndTime  - selectionStartTime).toLong()
        binding.tvSelectionDuration.text = "Длительность выделения: ${formatTime(duration)}"

        val hasValidSelection = selectionEndTime > selectionStartTime
        binding.btnPlaySelection.isEnabled = hasValidSelection
        binding.btnTrim.isEnabled = hasValidSelection

        // Визуальная индикация валидности выделения
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
        // Добавляем маркеры выделения на график
        val chart = binding.lineChart

        // Создаем выделения для начальной и конечной точек
        val highlightStart = Highlight(selectionStart, 0f, 0) // xValue, yValue, dataSetIndex
        val highlightEnd = Highlight(selectionEnd, 0f, 0)

        // Устанавливаем оба маркера
        chart.highlightValues(arrayOf(highlightStart, highlightEnd))

        // Добавляем визуальное выделение области
        addSelectionArea(selectionStartTime, selectionEndTime)

        // Обновляем график
        chart.invalidate()
    }

    private fun addSelectionArea(startTime: Float, endTime: Float) {
        val chart = binding.lineChart
        val data = chart.data ?: return

        // Удаляем старую область выделения если есть
        removeSelectionArea()

        // Создаем точки для области выделения
        val areaEntries = listOf(
            Entry(startTime, -1f),
            Entry(startTime, 1f),
            Entry(endTime, 1f),
            Entry(endTime, -1f),
            Entry(startTime, -1f) // замыкаем полигон
        )

        val areaDataSet = LineDataSet(areaEntries, "Selection Area")
        areaDataSet.color = Color.TRANSPARENT
        areaDataSet.setDrawCircles(false)
        areaDataSet.setDrawValues(false)
        areaDataSet.setDrawFilled(true)
        areaDataSet.fillColor = Color.argb(50, 255, 0, 0) // полупрозрачный красный
        areaDataSet.fillAlpha = 80

        data.addDataSet(areaDataSet)
    }

    private fun removeSelectionArea() {
        val chart = binding.lineChart
        val data = chart.data ?: return

        val selectionDataSet = data.getDataSetByLabel("Selection Area", false)
        if (selectionDataSet != null) {
            data.removeDataSet(selectionDataSet)
        }
    }

    private fun formatTime(milliseconds: Long): String {
        val totalSeconds = milliseconds / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        val ms = (milliseconds % 1000) / 10 // две цифры миллисекунд

        return String.format("%d:%02d.%02d", minutes, seconds, ms)
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.release()
    }
}