package com.example.muzpleer.ui.local

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
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
//                    if (tracks.isEmpty()) {
//                        binding.tvEmpty.visibility = View.VISIBLE
//                    }
                    adapter.data = tracks  //передаём данные в адаптер
                    sharedViewModel.setPlaylist(tracks)  //передаём плейлист в sharedViewModel
                }
            }
        }
        checkPermissions()
    }

    private fun checkPermissions() {
        Log.d(TAG, "LocalFragment checkPermissions:  ")
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_AUDIO
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }
        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                permission
            ) == PackageManager.PERMISSION_GRANTED -> {
                // Разрешение уже дано - сканируем музыку
                //scanForMusic() //todo
                //viewModel.setPermissionGranted(true)
                viewModel.loadLocalMusic()
            }
            shouldShowRequestPermissionRationale(permission) -> {
                // Показываем объяснение, зачем нужно разрешение
                showPermissionRationale()
            }
            else -> {
                // Запрашиваем разрешение впервые
                ActivityCompat.requestPermissions(
                    requireActivity(),
                    arrayOf(permission),
                    PERMISSION_REQUEST_CODE
                )
            }
        }
    }

    private fun showPermissionRationale() {
        AlertDialog.Builder(requireContext())
            .setTitle("Нужен доступ к медиафайлам")
            .setMessage("Для поиска музыкальных треков приложению нужен доступ к вашим аудиофайлам")
            .setPositiveButton("Разрешить") { _, _ ->
                val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    Manifest.permission.READ_MEDIA_AUDIO
                } else {
                    Manifest.permission.READ_EXTERNAL_STORAGE
                }
                ActivityCompat.requestPermissions(
                    requireActivity(),
                    arrayOf(permission),
                    PERMISSION_REQUEST_CODE
                )
            }
            .setNegativeButton("Отмена", null)
            .show()
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Разрешение получено - сканируем музыку
                    //scanForMusic() //todo
                    viewModel.setPermissionGranted(true)
                } else {
                    // Пользователь отказал
                    if (!shouldShowRequestPermissionRationale(permissions[0])) {
                        // Пользователь выбрал "Больше не спрашивать"
                        showOpenSettingsDialog()
                    } else {
                        Toast.makeText(
                            requireContext(),
                            getString(R.string.not_permission),
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
        }
    }

    private fun showOpenSettingsDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Требуется разрешение")
            .setMessage("Вы запретили доступ к медиафайлам. Хотите открыть настройки и предоставить разрешение?")
            .setPositiveButton("Настройки") { _, _ ->
                ///открываем настройки приложения
                //openAppSettings() //todo
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

//    private fun openAppSettings() {
//        val intent = Intent(requireActivity().Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
//        val uri = Uri.fromParts("package", packageName, null)
//        intent.data = uri
//        startActivity(intent)
//    }

    companion object {
        private const val PERMISSION_REQUEST_CODE = 1001
        const val TAG = "33333"
    }
}