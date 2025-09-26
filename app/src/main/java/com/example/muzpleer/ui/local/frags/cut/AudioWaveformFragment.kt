package com.example.muzpleer.ui.local.frags.cut

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.Toast
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

            // Настройка оси Y
            axisLeft.setDrawGridLines(false)
            axisLeft.textColor = Color.BLACK
            axisLeft.axisMinimum = -1f
            axisLeft.axisMaximum = 1f
            axisLeft.granularity = 0.2f

            axisRight.isEnabled = false

            legend.isEnabled = false
        }
    }

    private fun setupControls() {
        // Кнопка воспроизведения/паузы
        binding.btnPlayPause.setOnClickListener {
            togglePlayback()
        }
        // Кнопки выделения
        binding.btnSelectStart.setOnClickListener {
            setSelectionPoint(true)
        }

        binding.btnSelectEnd.setOnClickListener {
            setSelectionPoint(false)
        }

        // Кнопки действий
        binding.btnPlaySelection.setOnClickListener {
            playSelection()
        }

        binding.btnTrim.setOnClickListener {
            trimAudio()
        }

        binding.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    val time = progress.toFloat() / 100f * audioDuration
                    binding.tvCurrentTime.text = formatTime(time.toLong())
                    seekToPosition(time)
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
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
        val dataSet =LineDataSet(entries, "Амплитуда")
        dataSet.color = Color.BLUE
        dataSet.setDrawCircles(false)
        dataSet.lineWidth = 1.5f
        dataSet.setDrawValues(false)

        // Область под кривой
        dataSet.setDrawFilled(true)
        dataSet.fillColor = Color.argb(50, 0, 0, 255)
        dataSet.fillAlpha = 100

        val lineData =LineData(dataSet)
        binding.lineChart.data = lineData
        binding.lineChart.invalidate()

        // Устанавливаем максимальное время для SeekBar
        binding.seekBar.max = 1000
        binding.tvTotalTime.text = formatTime(audioDuration)
    }

    private fun updateAudioInfo(audioInfo: AudioInfo) {
        val file = File(audioInfo.filePath)
        val fileSize = String.format("%.2f MB", file.length().toDouble() / 1024 / 1024)

        binding.tvFileInfo.text = "Длительность: ${formatTime(audioInfo.duration)} | " +
                "Размер: $fileSize | " +
                "Частота: ${audioInfo.sampleRate} Hz"
    }
    private fun togglePlayback() {
        isPlaying = !isPlaying

        if (isPlaying) {
            binding.btnPlayPause.setImageResource(R.drawable.ic_pause2)
            viewModel.startPlayback(selectionStart, selectionEnd)
        } else {
            binding.btnPlayPause.setImageResource(R.drawable.ic_play)
            viewModel.pausePlayback()
        }
    }
    private fun setSelectionPoint(isStart: Boolean) {
        val currentTime = binding.seekBar.progress.toFloat() / 1000f * audioDuration

        if (isStart) {
            selectionStart = currentTime
            binding.tvSelectionStart.text = "Начало: ${formatTime(currentTime.toLong())}"
        } else {
            selectionEnd = currentTime
            binding.tvSelectionEnd.text = "Конец: ${formatTime(currentTime.toLong())}"
        }

        updateSelectionInfo()
        highlightSelectionOnChart()
    }

    private fun playSelection() {
        if (selectionStart >= selectionEnd) {
            Toast.makeText(requireContext(), "Некорректный интервал", Toast.LENGTH_SHORT).show()
            return
        }

        viewModel.playSelection(selectionStart, selectionEnd)
        binding.btnPlayPause.setImageResource(R.drawable.ic_pause2)
        isPlaying = true
    }

    private fun trimAudio() {
        if (selectionStart >= selectionEnd) {
            Toast.makeText(requireContext(), "Выделите корректный интервал", Toast.LENGTH_SHORT).show()
            return
        }

        val outputPath = "${requireContext().externalCacheDir?.absolutePath}/trimmed_${System.currentTimeMillis()}.mp3"
        viewModel.trimAudio(audioFilePath!!, outputPath, selectionStart.toLong(), selectionEnd.toLong())
    }

    private fun seekToPosition(time: Float) {
        viewModel.seekTo(time)
        updateCurrentTime(time)
    }

    private fun updateCurrentTime(time: Float) {
        binding.tvCurrentTime.text = formatTime(time.toLong())
        val progress = (time / audioDuration * 1000).toInt()
        binding.seekBar.progress = progress
    }

    private fun updateSelectionInfo() {
        val duration = (selectionEnd - selectionStart).toLong()
        binding.tvSelectionDuration.text = "Длительность: ${formatTime(duration)}"

        // Показываем/скрываем панель выделения
        val hasSelection = selectionStart > 0 || selectionEnd > 0
        binding.btnPlaySelection.isEnabled = hasSelection && selectionStart < selectionEnd
        binding.btnTrim.isEnabled = hasSelection && selectionStart < selectionEnd
    }

    private fun highlightSelectionOnChart() {
        // Добавляем маркеры выделения на график
        val chart = binding.lineChart
        // Создаем выделения для начальной и конечной точек
        val highlightStart = Highlight(selectionStart, 0f, 0) // xValue, yValue, dataSetIndex
        val highlightEnd = Highlight(selectionEnd, 0f, 0)

        // Устанавливаем выделения
        chart.highlightValues(arrayOf(highlightStart, highlightEnd))

        // Обновляем график
        chart.invalidate()
    }

    private fun formatTime(milliseconds: Long): String {
        val seconds = milliseconds / 1000
        val minutes = seconds / 60
        val remainingSeconds = seconds % 60
        return String.format("%d:%02d", minutes, remainingSeconds)
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.release()
    }
}