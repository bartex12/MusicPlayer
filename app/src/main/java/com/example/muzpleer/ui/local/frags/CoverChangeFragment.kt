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
import com.example.muzpleer.util.Constants.CHANGE_COVER
import com.example.muzpleer.util.Constants.CHANGE_COVER_ALBUM_SONG
import com.example.muzpleer.util.Constants.CHANGE_COVER_ARTIST_SONG
import com.example.muzpleer.util.Constants.CHANGE_COVER_FAVORITES
import com.example.muzpleer.util.Constants.CHANGE_COVER_FOLDER_SONG
import com.example.muzpleer.util.Constants.CHANGE_COVER_SONG

class CoverChangeFragment : Fragment() {
    private var _binding: FragmentCoverChangeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: SharedViewModel by activityViewModels()
    private val pickImage =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            Log.d(TAG, "!!!CoverChangeFragment registerForActivityResult uri = $it ")
            viewModel.updateCoverImage(it)
        }
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

        //обеспечивает установку обложки при открытии CoverChangeFragment и замене обложки через pickImage
        viewModel.coverImageUri.observe(viewLifecycleOwner) { uri ->
            Log.d(TAG, "4*** CoverChangeFragment coverImageUri.observe: uri = $uri ")
            uri?.let {
                Glide.with(binding.root.context)
                    .load(it)
                    .placeholder(R.drawable.muz_player3)
                    .error(R.drawable.muz_player3)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(binding.coverImageView)
            }
        }

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
                Log.d(TAG, "1*** CoverChangeFragment saveButton: curUri = $curUri")
                if (arguments?.getInt(CHANGE_COVER) != null){
                    val fromAdapter = requireArguments().getInt(CHANGE_COVER)
                    Log.d(TAG, "11*** CoverChangeFragment saveButton: fromAdapter = $fromAdapter")
                    when(fromAdapter){
                        //обновляем обложку и записываем в базу
                        CHANGE_COVER_SONG -> { viewModel.updateCoverImageAndSave(curUri)}
                        CHANGE_COVER_ALBUM_SONG -> {viewModel.updateCoverImageAndSaveAlbumSong(curUri) }
                        CHANGE_COVER_ARTIST_SONG -> {viewModel.updateCoverImageAndSaveAlbumSong(curUri) }
                        CHANGE_COVER_FOLDER_SONG-> {viewModel.updateCoverImageAndSaveAlbumSong(curUri)}  // todo
                        CHANGE_COVER_FAVORITES -> {viewModel.updateCoverImageAndSaveFavorites(curUri)}
                        else-> {viewModel.updateCoverImageAndSave(curUri)}
                    }
                }
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