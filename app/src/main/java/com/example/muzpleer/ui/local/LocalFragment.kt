package com.example.muzpleer.ui.local

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import com.example.muzpleer.R
import androidx.fragment.app.Fragment
import org.koin.androidx.viewmodel.ext.android.viewModel
import androidx.navigation.fragment.findNavController
import com.example.muzpleer.databinding.FragmentLocalBinding
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.launch
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.muzpleer.SharedViewModel
import kotlin.getValue

class LocalFragment : Fragment() {


    private var _binding: FragmentLocalBinding? = null
    private val binding get() = _binding!!
    private val viewModel: LocalMusicViewModel by viewModel()
    private val sharedViewModel: SharedViewModel by activityViewModels()
    private lateinit var adapter: MusicAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLocalBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = MusicAdapter { track ->
            // Обработка клика по треку
            findNavController().navigate(
                R.id.action_localFragment_to_playerFragment,
                bundleOf("mediaItem" to track)
            )
        }

        binding.localRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@LocalFragment.adapter
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.musicList.collect { tracks ->
                    adapter.data = tracks  //передаём данные в адаптер
                    sharedViewModel.setPlaylist(tracks)  //передаём плейлист в sharedViewModel
                }
            }
        }
        checkPermissions()
    }

    private fun checkPermissions() {
        Log.d(TAG, "LocalFragment checkPermissions:  ")
        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED -> {
                Log.d(TAG, "LocalFragment checkPermissions: true ")
                viewModel.setPermissionGranted(true)
            }
            shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE) -> {
                showPermissionRationale()
            }
            else -> {
                Log.d(TAG, "LocalFragment checkPermissions:  requestPermissions ")
                //потом убрать //todo
                viewModel.setPermissionGranted(true)
                requestPermissions(
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    PERMISSION_REQUEST_CODE
                )
            }
        }
    }

    private fun showPermissionRationale() {
        AlertDialog.Builder(requireContext())
            .setTitle("Нужен доступ к хранилищу")
            .setMessage("Чтобы проигрывать музыку, приложению нужно читать файлы с вашего устройства.")
            .setPositiveButton("Хорошо") { _, _ ->
                // Повторный запрос разрешения после объяснения
                requestPermissions(
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    PERMISSION_REQUEST_CODE
                )
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                viewModel.setPermissionGranted(true)
            } else {
                // Показать сообщение, что без разрешения функция недоступна
            }
        }
    }

    companion object {
        private const val PERMISSION_REQUEST_CODE = 1001
        const val TAG = "33333"
    }
}