package com.example.muzpleer.ui.local.frags

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.muzpleer.databinding.FragmentEditSongBinding
import com.example.muzpleer.model.Song
import com.example.muzpleer.ui.local.viewmodel.SharedViewModel

class EditSongFragment : Fragment() {
    private var _binding: FragmentEditSongBinding? = null
    private val binding get() = _binding!!
    private val viewModel: SharedViewModel by activityViewModels()
    private var currentSong: Song? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditSongBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Получаем текущую песню
        currentSong = viewModel.getSelectedSong()
        currentSong?.let { song ->
            // Заполняем поля данными песни
            binding.etTitle.setText(song.title)
            binding.etArtist.setText(song.artist)
            binding.etAuthor.setText(song.author ?: "")
            binding.etAlbum.setText(song.albumName ?: "")
            binding.etGenre.setText(song.genre ?: "")
            binding.etYear.setText(song.year?.toString() ?: "")
        }

        binding.btnCancel.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.btnSave.setOnClickListener {
            saveSongInfo()
        }
    }

    private fun saveSongInfo() {
        currentSong?.let { song ->
            val title = binding.etTitle.text.toString().trim()
            val artist = binding.etArtist.text.toString().trim()
            val author = binding.etAuthor.text.toString().trim().takeIf { it.isNotEmpty() }
            val album = binding.etAlbum.text.toString().trim().takeIf { it.isNotEmpty() }
            val genre = binding.etGenre.text.toString().trim().takeIf { it.isNotEmpty() }
            val year = binding.etYear.text.toString().toIntOrNull()

            if (title.isEmpty() || artist.isEmpty()) {
                Toast.makeText(requireContext(), "Название и исполнитель обязательны", Toast.LENGTH_SHORT).show()
                return
            }

            viewModel.updateSongInfo(
                songId = song.id,
                title = title,
                artist = artist,
                album = album,
                author = author,
                genre = genre,
                year = year
            )

            findNavController().navigateUp()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}