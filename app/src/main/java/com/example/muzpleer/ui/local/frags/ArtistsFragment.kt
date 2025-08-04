package com.example.muzpleer.ui.local.frags

import android.content.ContentUris
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager.widget.ViewPager
import com.example.muzpleer.R
import com.example.muzpleer.databinding.FragmentSingersBinding
import com.example.muzpleer.model.Album
import com.example.muzpleer.model.Artist
import com.example.muzpleer.model.Song
import com.example.muzpleer.ui.local.adapters.ArtistsAdapter
import com.example.muzpleer.ui.local.viewmodel.SharedViewModel
import org.koin.androidx.viewmodel.ext.android.activityViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import kotlin.collections.plus
import kotlin.getValue

class ArtistsFragment:Fragment() {
    private var _binding: FragmentSingersBinding? = null
    private val binding get() = _binding!!
    private val viewModel: SharedViewModel by activityViewModel()
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

        adapter = ArtistsAdapter { artist ->

            viewModel.setPlaylist(artist.songs) //устанавливаем список песен как плейлист
            // Навигация через Bundle
            val bundle = Bundle().apply {
                putString("artistId", artist.id)
            }
            findNavController().navigate( R.id.alltracksFragment,bundle)
               // AlltracksFragment.newInstance( artistTracks).arguments)
        }

        binding.singersRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@ArtistsFragment.adapter
        }

        viewModel.artists.observe(viewLifecycleOwner) { artists ->
            Log.d(TAG, "3 ArtistsFragment onViewCreated musicList.observe  artists.size= ${artists.size} ")
            if (artists.isEmpty()) {
                binding.progressBarSingers.visibility = View.VISIBLE
                binding.imageHolder3Singers.visibility = View.VISIBLE
                Log.d(TAG, "4 ArtistsFragment onViewCreated musicList.observe: progressBar.visibility = View.VISIBLE ")
            }else{
                binding.progressBarSingers.visibility = View.GONE
                binding.imageHolder3Singers.visibility = View.GONE
            }

            val sortedData = getSortedData(artists)
            adapter.data = sortedData  //передаём данные в адаптер
        }
    }
    private fun getSortedData(artists:List<Artist>):List<Artist>{
        return artists.sortedWith(compareBy(
            { artist -> when {
                artist.name.matches(Regex("^[а-яА-ЯёЁ].*")) -> 0
                artist.name.matches(Regex("^[a-zA-Z].*")) -> 1
                else -> 2}
            },
            { artist -> artist.name.lowercase() }
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

        fun newInstance(viewPager: ViewPager): ArtistsFragment {
            this.viewPager = viewPager
            return ArtistsFragment()
        }
    }

}