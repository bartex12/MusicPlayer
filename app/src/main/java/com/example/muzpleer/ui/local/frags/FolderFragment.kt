package com.example.muzpleer.ui.local.frags

import android.content.Context
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager.widget.ViewPager
import com.example.muzpleer.databinding.FragmentFoldersBinding
import com.example.muzpleer.model.AudioFolder
import com.example.muzpleer.model.MusicTrack
import com.example.muzpleer.ui.local.adapters.FoldersAdapter
import org.koin.androidx.viewmodel.ext.android.viewModel

class FolderFragment : Fragment(){
    private var _binding: FragmentFoldersBinding? = null
    private val binding get() = _binding!!
    private val viewModel: FolderViewModel by viewModel()
    private lateinit var adapter: FoldersAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFoldersBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = FoldersAdapter { folder ->
            //todo сделать переход
//            val playlist = track.tracks
//            // Обработка клика по треку
//            findNavController().navigate(
//                R.id.action_tabsLocalFragment_to_playerFragment,
//                PlayerFragment.newInstance(track, playlist).arguments
//            )
        }

        binding.foldersRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@FolderFragment.adapter
        }
        viewModel.musicList.observe(viewLifecycleOwner) { tracks ->
            Log.d(TAG, "3 LocalFragment onViewCreated musicList.collect: tracks.size= ${tracks.size} ")
            if (tracks.isEmpty()) {
                binding.progressBarFolder.visibility = View.VISIBLE
                binding.imageHolder3Folder.visibility = View.VISIBLE
                Log.d(TAG, "4 LocalFragment onViewCreated musicList.collect: progressBar.visibility = View.VISIBLE ")
            }else{
                binding.progressBarFolder.visibility = View.GONE
                binding.imageHolder3Folder.visibility = View.GONE
            }
            val musicFolders : List<AudioFolder> = scanAudioFolders(requireContext())
            adapter.folders = musicFolders  //передаём данные в адаптер
        }
        viewModel.loadLocalMusic()
    }

    fun scanAudioFolders(context: Context): List<AudioFolder> {
        val foldersMap = mutableMapOf<String, MutableList<MusicTrack>>()
        val collection = MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)

        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.DURATION,
        )

        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0"

        context.contentResolver.query(
            collection,
            projection,
            selection,
            null,
            null
        )?.use { cursor ->
            val dataColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
            val durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)

            while (cursor.moveToNext()) {
                val path = cursor.getString(dataColumn)
                val folderPath = path.substringBeforeLast("/")
                val duration = cursor.getLong(durationColumn)

                // Пропускаем файлы без пути
                if (folderPath.isNotEmpty()) {
                    foldersMap.getOrPut(folderPath) { mutableListOf() }.add(
                        MusicTrack(
                            id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)),
                            title = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)) ?: "Unknown",
                            artist = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)) ?: "Unknown",
                            artworkUri = getAlbumArtUri(cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID))),
                            mediaUri = path,
                            duration = duration
                        )
                    )
                }
            }
        }

        return foldersMap.map { (path, tracks) ->
            AudioFolder(
                path = path,
                name = getDisplayName(path),
                tracks = tracks
            )
        }.sortedBy { it.name.lowercase() }
    }

    private fun getAlbumArtUri(albumId: Long): String? {
        val contentUri = MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI
        val projection = arrayOf(MediaStore.Audio.Albums.ALBUM_ART)
        val selection = "${MediaStore.Audio.Albums._ID} = ?"
        val selectionArgs = arrayOf(albumId.toString())

        context?.contentResolver?.query(
            contentUri,
            projection,
            selection,
            selectionArgs,
            null
        )?.use { cursor ->
            if (cursor.moveToFirst()) {
                return cursor.getString(0)
            }
        }
        return null
    }

    fun getDisplayName(path: String): String {
        return when {
            path.contains("WhatsApp/Media/WhatsApp Audio") -> "WhatsApp Audio"
            path.contains("Telegram/Telegram Audio") -> "Telegram Audio"
            path.contains("Download") -> "Downloads"
            else -> path.substringAfterLast("/")
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "33333"
        private lateinit var viewPager: ViewPager

        fun newInstance(viewPager: ViewPager): FolderFragment {
            this.viewPager = viewPager
            return FolderFragment()
        }
    }
}