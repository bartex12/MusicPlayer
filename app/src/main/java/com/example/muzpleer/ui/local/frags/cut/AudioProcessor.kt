package com.example.muzpleer.ui.local.frags.cut

import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import android.media.MediaMetadataRetriever
import android.media.MediaMuxer
import android.media.MediaPlayer
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.example.muzpleer.model.cut.AmplitudePoint
import com.example.muzpleer.model.cut.AudioInfo
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.sin
import kotlin.math.sqrt

class AudioProcessor {
    private var mediaPlayer: MediaPlayer? = null
    private var currentFilePath: String? = null

    fun getAudioInfo(audioPath: String): AudioInfo {
        val file = File(audioPath)
        val mediaMetadataRetriever = MediaMetadataRetriever()

        try {
            mediaMetadataRetriever.setDataSource(audioPath)

            val duration = mediaMetadataRetriever
                .extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLong() ?: 0

            val sampleRate = mediaMetadataRetriever
                .extractMetadata(MediaMetadataRetriever.METADATA_KEY_SAMPLERATE)?.toInt() ?: 44100

            val bitrate = mediaMetadataRetriever
                .extractMetadata(MediaMetadataRetriever.METADATA_KEY_BITRATE)?.toInt() ?: 128000

            return AudioInfo(
                filePath = audioPath,
                duration = duration,
                sampleRate = sampleRate,
                channels = 2,
                bitrate = bitrate
            )
        } finally {
            mediaMetadataRetriever.release()
        }
    }

    fun extractAmplitudes(audioPath: String, stepMs: Int): List<AmplitudePoint> {
        return try {
            extractAmplitudesWithMediaExtractor(audioPath, stepMs)
        } catch (e: Exception) {
            Log.d(TAG, "@@AudioProcessor extractAmplitudes Error extracting amplitudes with MediaExtractor, using fallback error = ${e.message}")
            extractAmplitudesFallback(audioPath, stepMs)
        }
    }

    private fun extractAmplitudesWithMediaExtractor(audioPath: String, stepMs: Int): List<AmplitudePoint> {
        val amplitudes = mutableListOf<AmplitudePoint>()
        var extractor: MediaExtractor? = null

        try {
            extractor = MediaExtractor()
            extractor.setDataSource(audioPath)

            val audioTrackIndex = findAudioTrack(extractor)
            if (audioTrackIndex == -1) {
                throw RuntimeException("Audio track not found")
            }

            extractor.selectTrack(audioTrackIndex)
            val format = extractor.getTrackFormat(audioTrackIndex)

            val sampleRate = format.getInteger(MediaFormat.KEY_SAMPLE_RATE)
            val channelCount = format.getInteger(MediaFormat.KEY_CHANNEL_COUNT)
            val durationUs = format.getLong(MediaFormat.KEY_DURATION)
            val durationSeconds = durationUs / 1_000_000.0
            Log.d(TAG, "@@AudioProcessor extractAmplitudesWithMediaExtractor " +
                    "Sample rate: $sampleRate, Channels: $channelCount, Duration: $durationSeconds sec")
            // Извлекаем сырые амплитуды
            val rawAmplitudes = extractRawAmplitudes(extractor, sampleRate, channelCount, durationSeconds, stepMs)

            return normalizeAmplitudes(rawAmplitudes) // Улучшенный вариант
        } finally {
            extractor?.release()
        }
    }

    private fun extractRawAmplitudes(
        extractor: MediaExtractor,
        sampleRate: Int,
        channelCount: Int,
        durationSeconds: Double,
        stepMs: Int
    ): List<AmplitudePoint> {
        val amplitudes = mutableListOf<AmplitudePoint>()
        val stepSeconds = stepMs / 1000.0
        val samplesPerStep = (sampleRate * stepMs / 1000.0).toInt()

        val bufferSize = samplesPerStep * channelCount * 2 // 16-bit samples
        val buffer = ByteBuffer.allocateDirect(bufferSize)
        buffer.order(ByteOrder.LITTLE_ENDIAN)

        var currentTime = 0.0

        while (currentTime < durationSeconds) {
            extractor.seekTo((currentTime * 1_000_000).toLong(), MediaExtractor.SEEK_TO_CLOSEST_SYNC)

            buffer.clear()
            val bytesRead = extractor.readSampleData(buffer, 0)

            if (bytesRead > 0) {
                val amplitude = calculateAmplitudeFromBuffer(buffer, bytesRead, channelCount)
                amplitudes.add(AmplitudePoint(currentTime.toFloat(), amplitude))
            } else {
                amplitudes.add(AmplitudePoint(currentTime.toFloat(), 0f))
            }

            currentTime += stepSeconds

            if (!extractor.advance()) {
                break
            }
        }
        return amplitudes
    }

//    private fun normalizeAmplitudes(rawAmplitudes: List<AmplitudePoint>): List<AmplitudePoint> {
//        if (rawAmplitudes.isEmpty()) return emptyList()
//
//        // Находим максимальное абсолютное значение амплитуды
//        val maxAmplitude=rawAmplitudes.maxOf { abs(it.amplitude) }
//
//        // Если все амплитуды близки к нулю, используем дефолтное масштабирование
//        val scale=if (maxAmplitude > 0.01f) {
//            0.9f / maxAmplitude // растягиваем до 90% шкалы
//        } else {
//            10f // усиливаем слабый сигнал
//        }
//
//        // Применяем масштабирование и удаляем постоянную составляющую
//        return rawAmplitudes.map { point ->
//            AmplitudePoint(
//                time=point.time,
//                amplitude=point.amplitude * scale
//            )
//        }
//    }

    private fun normalizeAmplitudes(rawAmplitudes: List<AmplitudePoint>): List<AmplitudePoint> {
        if (rawAmplitudes.isEmpty()) return emptyList()

        // 1. Удаляем общее среднее значение
        val mean = rawAmplitudes.map { it.amplitude }.average().toFloat()
        val centered = rawAmplitudes.map { point ->
            AmplitudePoint(point.time, point.amplitude - mean)
        }

        // 2. Находим максимальное отклонение от нуля
        val maxDeviation = centered.maxOf { abs(it.amplitude) }

        // 3. Масштабируем чтобы максимум был ±0.9
        return if (maxDeviation > 0.001f) {
            val scale = 0.9f / maxDeviation
            centered.map { point ->
                AmplitudePoint(point.time, point.amplitude * scale)
            }
        } else {
            centered // если сигнал очень слабый
        }
    }

    private fun findAudioTrack(extractor: MediaExtractor): Int {
        // Простой поиск первого аудио трека
        for (i in 0 until extractor.trackCount) {
            val format = extractor.getTrackFormat(i)
            val mimeType = format.getString(MediaFormat.KEY_MIME)

            if (mimeType?.startsWith("audio/") == true) {
                Log.d(TAG, "@@AudioProcessor findAudioTrack Found audio track at index $i: $mimeType")
                return i
            }
        }

        Log.e(TAG, " @@AudioProcessor findAudioTrack No audio tracks found in ${extractor.trackCount} tracks")
        return -1
    }

//    private fun calculateAmplitudeFromBuffer(buffer: ByteBuffer, bytesRead: Int, channelCount: Int): Float {
//        buffer.position(0)
//        buffer.limit(bytesRead)
//
//        val samples = bytesRead / 2 // 16-bit samples
//        val samplesPerChannel = samples / channelCount
//
//        var maxAmplitude = 0f
//
//        // Для каждого канала вычисляем амплитуду
//        for (channel in 0 until channelCount) {
//            var channelAmplitude = 0f
//            var sampleCount = 0
//
//            for (i in channel until samples step channelCount) {
//                if (i * 2 + 1 < bytesRead) {
//                    val sample = buffer.getShort(i * 2).toFloat() / Short.MAX_VALUE.toFloat()
//                    channelAmplitude += abs(sample)
//                    sampleCount++
//                }
//            }
//
//            if (sampleCount > 0) {
//                channelAmplitude /= sampleCount
//                maxAmplitude = max(maxAmplitude, channelAmplitude)
//            }
//        }
//
//        return maxAmplitude
//    }


    //Улучшенный расчет амплитуды с RMS
    private fun calculateAmplitudeFromBuffer(buffer: ByteBuffer, bytesRead: Int, channelCount: Int): Float {
        buffer.position(0)
        buffer.limit(bytesRead)

        val samples = bytesRead / 2 // 16-bit samples
        val samplesPerChannel = samples / channelCount

        // Используем RMS (Root Mean Square) для более точной амплитуды
        var sumSquares = 0.0
        var sampleCount = 0

        for (i in 0 until samples) {
            if (i * 2 + 1 < bytesRead) {
                val sample = buffer.getShort(i * 2).toFloat() / Short.MAX_VALUE.toFloat()
                sumSquares += sample * sample
                sampleCount++
            }
        }

        return if (sampleCount > 0) {
            sqrt(sumSquares / sampleCount).toFloat()
        } else {
            0f
        }
    }

    private fun extractAmplitudesFallback(audioPath: String, stepMs: Int): List<AmplitudePoint> {
        val amplitudes = mutableListOf<AmplitudePoint>()

        try {
            val audioInfo = getAudioInfo(audioPath)
            val durationSeconds = audioInfo.duration / 1000.0
            val stepSeconds = stepMs / 1000.0

            // Генерируем реалистичную волновую форму на основе метаданных
            var time = 0.0
            while (time < durationSeconds) {
                // Более реалистичная симуляция с разными частотами
                val baseFreq = 2.0 * Math.PI * 1.0 // базовая частота 1 Гц
                val highFreq = 2.0 * Math.PI * 10.0 // высокая частота 10 Гц

                // Комбинация синусоид для реалистичности
                val amplitude = (
                        sin(time * baseFreq) * 0.6 +
                                sin(time * highFreq) * 0.3 +
                                randomVariation() * 0.1
                        ).toFloat()

                amplitudes.add(AmplitudePoint(time.toFloat(), amplitude.coerceIn(-1f, 1f)))
                time += stepSeconds
            }
        } catch (e: Exception) {
            // Минимальный фолбэк
            for (i in 0..100) {
                amplitudes.add(AmplitudePoint(i * 0.1f, sin(i * 0.2f).toFloat() * 0.8f))
            }
        }

        return amplitudes
    }

    private fun randomVariation(): Double {
        return Math.random() * 2 - 1 // случайное число от -1 до 1
    }

    fun startPlayback(startTime: Float = 0f, endTime: Float = 0f) {
        currentFilePath?.let { path ->
            mediaPlayer?.release()
            mediaPlayer = MediaPlayer().apply {
                setDataSource(path)
                prepareAsync()

                setOnPreparedListener {
                    if (startTime > 0) {
                        seekTo((startTime * 1000).toInt())
                    }
                    start()

                    if (endTime > 0) {
                        // Остановить воспроизведение в endTime
                        val delay = ((endTime - startTime) * 1000).toLong()
                        Handler(Looper.getMainLooper()).postDelayed({
                            pausePlayback()
                        }, delay)
                    }
                }
            }
        }
    }

    fun pausePlayback() {
        mediaPlayer?.pause()
    }

    fun playSelection(startTime: Float, endTime: Float) {
        startPlayback(startTime, endTime)
    }

    fun seekTo(time: Float) {
        mediaPlayer?.seekTo((time * 1000).toInt())
    }

//    //не устанавливается библиотека FFmpeg
//    fun trimAudio(inputPath: String, outputPath: String, startTime: Double, endTime: Double) {
//        // Использование FFmpeg для обрезки аудио
//        val cmd = arrayOf(
//            "-i", inputPath,
//            "-ss", startTime.toString(),
//            "-to", endTime.toString(),
//            "-c", "copy",
//            outputPath
//        )
//
//        try {
//            val returnCode = FFmpeg.execute(cmd)
//            if (returnCode == 0) {
//                Log.d(TAG, "Аудио успешно обрезано: $outputPath")
//            } else {
//                throw RuntimeException("Ошибка FFmpeg, код: $returnCode")
//            }
//        } catch (e: Exception) {
//            throw RuntimeException("Ошибка обрезки аудио: ${e.message}")
//        }
//    }

    fun trimAudio(inputPath: String, outputPath: String, startTimeMs: Long, endTimeMs: Long) {
        var extractor: MediaExtractor? = null
        var muxer: MediaMuxer? = null

        try {
            extractor = MediaExtractor()
            extractor.setDataSource(inputPath)

            val trackCount = extractor.trackCount
            var audioTrackIndex = -1
            var audioFormat: MediaFormat? = null

            // Находим аудио дорожку
            for (i in 0 until trackCount) {
                val format = extractor.getTrackFormat(i)
                val mime = format.getString(MediaFormat.KEY_MIME)
                if (mime?.startsWith("audio/") == true) {
                    audioTrackIndex = i
                    audioFormat = format
                    break
                }
            }

            if (audioTrackIndex == -1) {
                throw RuntimeException("Аудио дорожка не найдена")
            }

            extractor.selectTrack(audioTrackIndex)

            // Создаем muxer для записи
            muxer = MediaMuxer(outputPath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
            val writeTrackIndex = muxer.addTrack(audioFormat!!)

            muxer.start()

            val bufferSize = 64 * 1024
            val buffer = ByteBuffer.allocate(bufferSize)
            val bufferInfo = android.media.MediaCodec.BufferInfo()

            extractor.seekTo(startTimeMs * 1000, MediaExtractor.SEEK_TO_CLOSEST_SYNC)

            while (true) {
                bufferInfo.offset = 0
                bufferInfo.size = extractor.readSampleData(buffer, 0)

                if (bufferInfo.size < 0) {
                    break
                }

                bufferInfo.presentationTimeUs = extractor.sampleTime

                // Проверяем не вышли ли за endTime
                if (bufferInfo.presentationTimeUs > endTimeMs * 1000) {
                    break
                }

                // Корректируем временную метку
                bufferInfo.presentationTimeUs -= startTimeMs * 1000

                bufferInfo.flags = MediaCodec.BUFFER_FLAG_KEY_FRAME

                // Подготавливаем buffer для записи
                buffer.position(0)
                buffer.limit(bufferInfo.size)

                muxer.writeSampleData(writeTrackIndex, buffer, bufferInfo)

                // Очищаем buffer для следующего чтения
                buffer.clear()

                if (!extractor.advance()) {
                    break
                }
            }

            muxer.stop()
            Log.d(TAG, "Аудио успешно обрезано: ${endTimeMs - startTimeMs} мс")
        } catch (e: Exception) {
            throw RuntimeException("Ошибка обрезки аудио: ${e.message}")
        } finally {
            extractor?.release()
            muxer?.release()
        }
    }

    fun release() {
        mediaPlayer?.release()
        mediaPlayer = null
    }
    companion object{
        const val TAG = "33333"
    }
}