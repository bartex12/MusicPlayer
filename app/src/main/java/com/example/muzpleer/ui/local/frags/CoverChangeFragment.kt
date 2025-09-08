package com.example.muzpleer.ui.local.frags

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.activity.result.contract.ActivityResultContracts
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.transition.ChangeBounds
import androidx.transition.ChangeImageTransform
import androidx.transition.TransitionManager
import androidx.transition.TransitionSet
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
    private var isExpanded = false

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
                viewModel.updateCoverImageAndSave(curUri)
            }
            findNavController().navigateUp()
        }

        binding.coverImageView.setOnClickListener {
            isExpanded = !isExpanded
            //
            TransitionManager.beginDelayedTransition(
                binding.coverContainer, TransitionSet()
                    .addTransition(ChangeBounds())
                    .addTransition(ChangeImageTransform())
            )
            val params = binding.coverImageView.layoutParams as ConstraintLayout.LayoutParams
            if (isExpanded) {
                // Оставляем существующие constraints, но меняем размеры
                params.topMargin = 0
                params.width = ConstraintLayout.LayoutParams.MATCH_PARENT
                params.height = ConstraintLayout.LayoutParams.MATCH_PARENT
                params.dimensionRatio = ""
                binding.coverImageView.scaleType = ImageView.ScaleType.FIT_CENTER
            } else {
                // Возвращаем оригинальные размеры
                params.topMargin = 16
                params.width = 0
                params.height = 0
                params.dimensionRatio = "1:1"
                params.matchConstraintPercentWidth = 0.6f
                binding.coverImageView.scaleType = ImageView.ScaleType.FIT_CENTER
            }

            binding.coverImageView.layoutParams = params
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