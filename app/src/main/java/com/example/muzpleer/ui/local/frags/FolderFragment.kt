package com.example.muzpleer.ui.local.frags

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager.widget.ViewPager
import com.example.muzpleer.R
import com.example.muzpleer.databinding.FragmentFoldersBinding
import com.example.muzpleer.model.Artist
import com.example.muzpleer.model.Folder
import com.example.muzpleer.model.Song
import com.example.muzpleer.ui.local.adapters.FoldersAdapter
import com.example.muzpleer.ui.local.viewmodel.SharedViewModel
import org.koin.androidx.viewmodel.ext.android.activityViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import kotlin.getValue

class FolderFragment : Fragment(){
    private var _binding: FragmentFoldersBinding? = null
    private val binding get() = _binding!!
    private val viewModel: SharedViewModel by activityViewModel()
    private lateinit var adapter: FoldersAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFoldersBinding.inflate(inflater, container, false)
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = FoldersAdapter { folder ->
            viewModel.setPlaylist(folder.songs) //устанавливаем список песен как плейлист
            // Навигация через Bundle
            val bundle = Bundle().apply {
                putString("folderPath", folder.path)
            }
            findNavController().navigate( R.id.alltracksFragment, bundle)
                //AlltracksFragment.newInstance( folderTracks).arguments)
        }

        binding.foldersRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@FolderFragment.adapter
        }
        viewModel.folders.observe(viewLifecycleOwner) { folders ->
            Log.d(TAG, "3 FolderFragment onViewCreated musicList.observe: folders.size= ${folders.size} ")
            if (folders.isEmpty()) {
                binding.progressBarFolder.visibility = View.VISIBLE
                binding.imageHolder3Folder.visibility = View.VISIBLE
                Log.d(TAG, "4 FolderFragment onViewCreated musicList.observe: progressBar.visibility = View.VISIBLE ")
            }else{
                binding.progressBarFolder.visibility = View.GONE
                binding.imageHolder3Folder.visibility = View.GONE
            }

            val sortedFolders = getSortedData(folders)
            adapter.folders = sortedFolders  //передаём данные в адаптер
        }
    }

    private fun getSortedData( folders:List<Folder>):List<Folder>{
        return folders.sortedWith(compareBy(
            { folder -> when {
                folder.name.matches(Regex("^[а-яА-ЯёЁ].*")) -> 0
                folder.name.matches(Regex("^[a-zA-Z].*")) -> 1
                else -> 2}
            },
            { folder -> folder.name.lowercase() }
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

        fun newInstance(viewPager: ViewPager): FolderFragment {
            this.viewPager = viewPager
            return FolderFragment()
        }
    }
}