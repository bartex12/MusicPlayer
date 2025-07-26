package com.example.muzpleer.ui.local.frags

import android.content.ContentUris
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager.widget.ViewPager
import com.example.muzpleer.databinding.FragmentAlbumBinding
import com.example.muzpleer.model.MusicAlbum
import com.example.muzpleer.model.MusicTrack
import com.example.muzpleer.ui.local.adapters.AlbumsAdapter
import org.koin.androidx.viewmodel.ext.android.viewModel


class AlbumFragment: Fragment() {
    private var _binding: FragmentAlbumBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AlbumViewModel by viewModel()
    private lateinit var adapter: AlbumsAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAlbumBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = AlbumsAdapter { album ->
            val albumTracks:List<MusicTrack> = album.tracks

            //todo сделать переход
//            val playlist = track.tracks
//            // Обработка клика по треку
//            findNavController().navigate(
//                R.id.action_albumFragment_to_playerFragment,
//                PlayerFragment.newInstance(track, playlist).arguments
//            )
        }

        binding.albumRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@AlbumFragment.adapter
        }
        viewModel.musicList.observe(viewLifecycleOwner) { tracks ->
            Log.d(TAG, "3 AlbumFragment onViewCreated musicList.observe: tracks.size= ${tracks.size} ")
            if (tracks.isEmpty()) {
                binding.progressBarAlbum.visibility = View.VISIBLE
                binding.imageHolder3Album.visibility = View.VISIBLE
                Log.d(TAG, "4 AlbumFragment onViewCreated musicList.observe: progressBar.visibility = View.VISIBLE ")
            }else{
                binding.progressBarAlbum.visibility = View.GONE
                binding.imageHolder3Album.visibility = View.GONE
            }
            val musicAlbums: List<MusicAlbum> = scanAlbumsApi29Plus(tracks)
            adapter.albums = musicAlbums  //передаём данные в адаптер
        }
    }

    private fun scanAlbumsApi29Plus(tracks:List<MusicTrack>): List<MusicAlbum> {
        // Теперь ключ - название альбома
        val albumsMap = mutableMapOf<String, MusicAlbum>()
        // Группируем треки по альбомам
        tracks.forEach { track ->
            val normalizedTitle = track.album?.trim()?.lowercase()
            if (albumsMap.containsKey(normalizedTitle)) {
                // Добавляем трек к существующему альбому
                val existingAlbum = albumsMap[normalizedTitle]!!
//                Log.d(TAG, "*1*AlbumFragment scanAlbumsApi29Plus " +
//                        "existingAlbum.tracks.size  = ${existingAlbum.tracks.size}  ")
                albumsMap[normalizedTitle.toString()] = existingAlbum.copy(
                    tracks = existingAlbum.tracks + track)
            } else {
                val albumArtUri = ContentUris.withAppendedId(
                    "content://media/external/audio/albumart".toUri(), track.albumId )
                // Создаем новый альбом
                albumsMap[normalizedTitle.toString()] = MusicAlbum(
                    id = track.albumId,
                    title = track.album.toString(),
                    artist = track.artist,
                    artworkUri = albumArtUri,
                    tracks = listOf(track)
                )
//                Log.d(TAG, "*2*AlbumFragment scanAlbumsApi29Plus " +
//                        "новый альбом = ${ albumsMap[normalizedTitle]} " +
//                        "Всего альбомов  = ${albumsMap.size}  ")
            }
        }
        return albumsMap.values.sortedWith(compareBy(
            { album -> when {
                album.title.matches(Regex("^[а-яА-ЯёЁ].*")) -> 0
                album.title.matches(Regex("^[a-zA-Z].*")) -> 1
                else -> 2}
            },
            { album -> album.title.lowercase() }
        )
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "33333"
        private lateinit var viewPager: ViewPager

        fun newInstance(viewPager: ViewPager): AlbumFragment {
            this.viewPager = viewPager
            return AlbumFragment()
        }
    }
}