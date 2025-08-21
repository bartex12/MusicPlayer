package com.example.muzpleer.ui.local.frags

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.muzpleer.R
import com.example.muzpleer.databinding.FragmentCoverChangeBinding
import com.example.muzpleer.ui.local.viewmodel.SharedViewModel

class CoverChangeFragment : Fragment() {
    private var _binding: FragmentCoverChangeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: SharedViewModel by activityViewModels()
    private val pickImage =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { viewModel.updateCoverImage(it) }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCoverChangeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //обеспечивает установку обложки при открытии CoverChangeFragment
        viewModel.coverImageUri.observe(viewLifecycleOwner) { uri ->
            viewModel.getSelectedSong()?.artUri = uri.toString()
            Log.d(TAG, "!!!CoverChangeFragment coverImageUri.observe: uri = $uri ")
            uri?.let {
                Glide.with(binding.root.context)
                    .load(it)
                    .placeholder(R.drawable.muz_player3)
                    .error(R.drawable.muz_player3)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(binding.coverImageView)
            }
        }

//        viewModel.selectedSong.observe(viewLifecycleOwner) { selectedSong ->
//            Log.d(TAG, "!!!CoverChangeFragment selectedSong.observe: selectedSong = $selectedSong ")
//            selectedSong?. let{
//                Glide.with(binding.root.context)
//                    .load(it.artUri)
//                    .placeholder(R.drawable.muz_player3)
//                    .error(R.drawable.muz_player3)
//                    .diskCacheStrategy(DiskCacheStrategy.ALL)
//                    .into(binding.coverImageView)
//            }
//        }

        binding.usePhonePhotos.setOnClickListener {
            pickImage.launch("image/*")
        }

        binding.searchOnline.setOnClickListener {
            openFlickrSearch()
        }

        binding.restoreDefault.setOnClickListener {
            viewModel.restoreDefaultCover()
        }

        binding.saveButton.setOnClickListener {
            viewModel.getCoverImageUri()?. let{curUri->
                viewModel.getSelectedSong()?. let{curSelectedSong->
                    curSelectedSong.artUri = curUri.toString()
                    Log.d(TAG, "CoverChangeFragment saveButton:" +
                            "  curSelectedSong.artUri = ${curSelectedSong.artUri}" +
                            "  currentSelectedSong = ${curSelectedSong.title}")
                }
                viewModel.updateCoverImageAndSave(curUri)
            }
            findNavController().navigateUp()
        }
    }

    private fun openFlickrSearch() {
        val intent = Intent(Intent.ACTION_VIEW, "https://www.flickr.com/search/".toUri())
        startActivity(intent)
    }
    companion object{
        const val  TAG = "33333"
    }
}