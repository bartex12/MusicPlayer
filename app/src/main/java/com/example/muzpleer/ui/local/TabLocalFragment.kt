package com.example.muzpleer.ui.local

import android.content.ContentUris
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.SearchView
import androidx.core.net.toUri
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import androidx.viewpager.widget.ViewPager
import com.bumptech.glide.Glide
import com.example.muzpleer.R
import com.example.muzpleer.databinding.FragmentTabslocalBinding
import com.example.muzpleer.model.Song
import com.example.muzpleer.ui.local.adapters.ViewPageAdapterLocal
import com.example.muzpleer.ui.local.frags.SongFragment
import com.example.muzpleer.ui.local.viewmodel.SharedViewModel
import com.example.muzpleer.ui.player.PlayerFragment
import com.google.android.material.tabs.TabLayout
import org.koin.androidx.viewmodel.ext.android.activityViewModel
import org.w3c.dom.Text
import java.util.Locale
import kotlin.getValue

class TabLocalFragment: Fragment()  {
    companion object{
        const val TAG = "33333"
    }
    private var _binding: FragmentTabslocalBinding? = null
    private val binding get() = _binding!!

    lateinit var viewPager: ViewPager
    private lateinit var tabLayout: TabLayout
    private lateinit var adapter: ViewPageAdapterLocal
    private val viewModel: SharedViewModel by activityViewModel()

    private lateinit var title: TextView
    private lateinit var artist: TextView
    private lateinit var artWork: ImageView
    private lateinit var previous: ImageView
    private lateinit var playPause: ImageView
    private lateinit var next: ImageView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTabslocalBinding.inflate(inflater, container, false )
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Log.d(TAG, "#TabLocalFragment onViewCreated:  ")
        initViews()
        initPageAdapter()

        //устанавливаем текущую вкладку - берём из преференсис
        viewPager.currentItem  =  viewModel.getTabsLocalPosition()

        //Log.d(TAG, "***TabsFragment setFragmentResultListener tabPosition = ${viewPager.currentItem}")
        viewModel.currentSong.observe(viewLifecycleOwner) {currentSong->
            currentSong?. let {
                title.text=currentSong.title
                artist.text=currentSong.artist
                // Загружаем обложку, если есть
                val albumArtUri=ContentUris.withAppendedId(
                    "content://media/external/audio/albumart".toUri(),
                    currentSong.albumId
                )
                Glide.with(requireContext())
                    .load(if (currentSong.isLocal) albumArtUri else it.cover)
                    .placeholder(R.drawable.muz_player3)
                    .error(R.drawable.muz_player3)
                    .into(artWork)
            }
        }

        viewModel.isPlaying.observe(viewLifecycleOwner) { isPlaying ->
            playPause.setImageResource(
                if (isPlaying) R.drawable.ic_pause_white else R.drawable.ic_play_white
            )
        }

        viewModel.songAndPlaylist.observe(viewLifecycleOwner) { songAndPlaylist ->
            //находим индекс трека в плейлисте
            val indexOfTrack = if(songAndPlaylist.song.isLocal){
                songAndPlaylist.playlist.indexOfFirst { it.mediaUri == songAndPlaylist.song.mediaUri }
            }else{
                songAndPlaylist.playlist.indexOfFirst { it.resourceId == songAndPlaylist.song.resourceId  }
            }
            Log.d(TAG, "###TabLocalFragment onViewCreated " +
                    "indexOfTrack = $indexOfTrack " +
                    "songAndPlaylist.playlist.size = ${songAndPlaylist.playlist.size}" +
                    " currentSong = ${songAndPlaylist.song} " +
                    "currentPlayList.size = ${songAndPlaylist.playlist.size}")

            viewModel.setPlaylistForHandler(songAndPlaylist.playlist, indexOfTrack)
        }
    }

    override fun onPause() {
        super.onPause()
        //запоминаем текущую вкладку
        viewModel.saveTabsLocalPosition(viewPager.currentItem)
    }

    private fun initViews() {
        viewPager = binding.viewPagerLocal
        tabLayout = binding.tabLayoutLocal
        title = binding.playerBottom.title
        artist = binding.playerBottom.artist
        artWork = binding.playerBottom.artwork
        previous = binding.playerBottom.previous
        playPause = binding.playerBottom.playPause
        next = binding.playerBottom.next

        previous.setOnClickListener { viewModel.playPrevious() }
        playPause.setOnClickListener { viewModel.togglePlayPause() }
        next.setOnClickListener { viewModel.playNext()  }
    }

    private fun initPageAdapter() {
        adapter =  ViewPageAdapterLocal(requireActivity(), childFragmentManager, viewPager)
        viewPager.adapter = adapter
        tabLayout.setupWithViewPager(viewPager)
        //устанавливаем цвет текста черный а при выделении - синий
        tabLayout.setTabTextColors(Color.GREEN, Color.WHITE)
        tabLayout.setSelectedTabIndicatorColor(Color.WHITE)
    }

}