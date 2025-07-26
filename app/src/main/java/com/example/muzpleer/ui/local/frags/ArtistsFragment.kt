package com.example.muzpleer.ui.local.frags

import android.content.ContentUris
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager.widget.ViewPager
import com.example.muzpleer.databinding.FragmentSingersBinding
import com.example.muzpleer.model.MusicAlbum
import com.example.muzpleer.model.MusicArtist
import com.example.muzpleer.model.MusicTrack
import com.example.muzpleer.ui.local.adapters.ArtistsAdapter
import org.koin.androidx.viewmodel.ext.android.viewModel
import kotlin.collections.plus
import kotlin.getValue

class ArtistsFragment:Fragment() {
    private var _binding: FragmentSingersBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ArtistsViewModel by viewModel()
    private lateinit var adapter: ArtistsAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSingersBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = ArtistsAdapter { track ->
            //todo сделать переход
//            val playlist = viewModel.musicList.value
//            // Обработка клика по треку
//            findNavController().navigate(
//                R.id.action_tabsLocalFragment_to_playerFragment,
//                PlayerFragment.Companion.newInstance(track, playlist).arguments
//            )
        }

        binding.singersRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
//            this.addItemDecoration(decor = StickyHeaderDecoration())
//            this.setFastScrollEnabled(true)
            adapter = this@ArtistsFragment.adapter
        }

        viewModel.musicList.observe(viewLifecycleOwner) { tracks ->
            Log.d(TAG, "3 ArtistsFragment onViewCreated musicList.observe  tracks.size= ${tracks.size} ")
            if (tracks.isEmpty()) {
                binding.progressBarSingers.visibility = View.VISIBLE
                binding.imageHolder3Singers.visibility = View.VISIBLE
                Log.d(TAG, "4 ArtistsFragment onViewCreated musicList.observe: progressBar.visibility = View.VISIBLE ")
            }else{
                binding.progressBarSingers.visibility = View.GONE
                binding.imageHolder3Singers.visibility = View.GONE
            }

            val artists: List<MusicArtist> = scanArtistsWithTracks(tracks)
            adapter.data = artists  //передаём данные в адаптер
        }
    }

    private fun scanArtistsWithTracks(tracks:List<MusicTrack>): List<MusicArtist> {
        // Теперь ключ - имя артиста
        val artistMap = mutableMapOf<String, MusicArtist>()
        // Группируем треки по артистам
        tracks.forEach { track ->
            val normalizedName  = track.artist?.trim()?.lowercase()
            if (artistMap.containsKey(normalizedName)) {
                // Добавляем трек к существующему альбому
                val existingArtist  = artistMap[normalizedName]!!
//                Log.d(TAG, "*1*AlbumFragment scanAlbumsApi29Plus " +
//                        "existingAlbum.tracks.size  = ${existingAlbum.tracks.size}  ")
                artistMap[normalizedName.toString()] = existingArtist.copy(
                    tracks = existingArtist.tracks + track)
            } else {
                val albumArtUri = ContentUris.withAppendedId(
                    "content://media/external/audio/albumart".toUri(), track.albumId )
                // Создаем новый альбом
                artistMap[normalizedName.toString()] = MusicArtist(
                    name = track.artist,
                    artworkUri = albumArtUri,
                    tracks = listOf(track)
                )
//                Log.d(TAG, "*2*AlbumFragment scanAlbumsApi29Plus " +
//                        "новый альбом = ${ albumsMap[normalizedTitle]} " +
//                        "Всего альбомов  = ${albumsMap.size}  ")
            }
        }
        return artistMap.values.sortedWith(compareBy(
            { artist -> when {
                artist.name.matches(Regex("^[а-яА-ЯёЁ].*")) -> 0
                artist.name.matches(Regex("^[a-zA-Z].*")) -> 1
                else -> 2}
            },
            { artist -> artist.name.lowercase() }
        )
        )
    }

    private fun getArtistsWithTracks(tracks: List<MusicTrack> ): List<MusicArtist> {

        val artistsMap = mutableMapOf<String, MutableList<MusicTrack>>()

        // Группируем треки по исполнителям
        tracks.forEach { track ->
            val artistName = track.artist?.takeIf { it.isNotBlank() } ?: "<unknown>"
            artistsMap.getOrPut(artistName) { mutableListOf() }.add(track)
        }
        // Преобразуем в список MusicArtist
        return artistsMap.map { (name, tracksForArtist) ->
            MusicArtist(
                name = name,
                tracks = tracksForArtist.sortedBy { it.title },
                artworkUri = ContentUris
                    .withAppendedId( "content://media/external/audio/albumart".toUri(),
                        tracksForArtist.firstOrNull()?.albumId ?:-1 )  //неправильно todo
            )
        }.sortedWith(artistComparator) // Применяем кастомную сортировку
    }

    private val artistComparator = compareBy<MusicArtist> { artist ->
        when {
            artist.name == "<unknown>" -> 2 // Unknown в конец
            artist.name.matches(Regex("^[а-яА-ЯёЁ].*")) -> 0 // Русские сначала
            artist.name.matches(Regex("^[a-zA-Z].*")) -> 1 // Латинские после русских
            else -> 2 // Все остальное (цифры, символы) в конец
        }
    }.thenBy { it.name.lowercase() } // Сортировка внутри групп

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "33333"
        private lateinit var viewPager: ViewPager

        fun newInstance(viewPager: ViewPager): ArtistsFragment {
            this.viewPager = viewPager
            return ArtistsFragment()
        }
    }
//    private fun getAlbumIdForCover(tracksForArtist: List<MusicTrack>): Long{
//        tracksForArtist.forEach {musicTrack->
//            val uri = ContentUris.withAppendedId(
//                "content://media/external/audio/albumart".toUri(),
//                musicTrack.albumId)
//            try {
//                requireContext().contentResolver.openInputStream(uri)?.use { stream ->
//                    if (BitmapFactory.decodeStream(stream).width>0){
//                        return musicTrack.albumId
//                    }
//                    Log.d(TAG, "Обложка найдена:")
//                } ?: Log.d(TAG, "Обложка не найдена")
//            } catch (e: Exception) {
//                Log.d(TAG, "Ошибка: ${e.message}")
//            }
//        }
//        return -1
//    }

}