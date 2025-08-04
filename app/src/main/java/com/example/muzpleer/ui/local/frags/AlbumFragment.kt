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
import com.example.muzpleer.databinding.FragmentAlbumBinding
import com.example.muzpleer.model.Album
import com.example.muzpleer.model.Song
import com.example.muzpleer.ui.local.adapters.AlbumsAdapter
import com.example.muzpleer.ui.local.viewmodel.SharedViewModel
import org.koin.androidx.viewmodel.ext.android.activityViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import kotlin.getValue


class AlbumFragment: Fragment() {
    private var _binding: FragmentAlbumBinding? = null
    private val binding get() = _binding!!
    private val viewModel: SharedViewModel by activityViewModel()
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

            viewModel.setPlaylist(album.songs) //устанавливаем список песен как плейлист
            // Навигация через Bundle
            val bundle = Bundle().apply {
                putString("albumId", album.id)
            }
            findNavController().navigate( R.id.alltracksFragment, bundle)
                //AlltracksFragment.newInstance( albumTracks).arguments)
        }

        binding.albumRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@AlbumFragment.adapter
        }
        viewModel.albums.observe(viewLifecycleOwner) { musicAlbums ->
            Log.d(TAG, "3 AlbumFragment onViewCreated musicList.observe: musicAlbums.size= ${musicAlbums.size} ")
            if (musicAlbums.isEmpty()) {
                binding.progressBarAlbum.visibility = View.VISIBLE
                binding.imageHolder3Album.visibility = View.VISIBLE
                Log.d(TAG, "4 AlbumFragment onViewCreated musicList.observe: progressBar.visibility = View.VISIBLE ")
            }else{
                binding.progressBarAlbum.visibility = View.GONE
                binding.imageHolder3Album.visibility = View.GONE
            }
            adapter.albums = musicAlbums  //передаём данные в адаптер
        }
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