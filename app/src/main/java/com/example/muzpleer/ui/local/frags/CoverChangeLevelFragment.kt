package com.example.muzpleer.ui.local.frags

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
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
import com.example.muzpleer.databinding.FragmentCoverChangeLevelBinding
import com.example.muzpleer.ui.local.viewmodel.SharedViewModel

class CoverChangeLevelFragment: Fragment() {
    private var _binding: FragmentCoverChangeLevelBinding? = null
    private val binding get() = _binding!!
    private var isExpanded = false
    private val viewModel: SharedViewModel by activityViewModels()

    private var levelId: Long = -1
    private var levelType: LevelType = LevelType.PLAYLIST

    enum class LevelType {
        PLAYLIST, ALBUM, ARTIST, FOLDER
    }

    private val pickImage =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let {
                Log.d(TAG, "1---CoverChangeLevelFragment registerForActivityResult uri = $it ")
                viewModel.updateCoverImageLevel(it)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Получаем аргументы
        levelId = arguments?.getLong("levelId") ?: -1
        levelType = arguments?.getSerializable("levelType") as? LevelType ?: LevelType.PLAYLIST
        Log.d(TAG, "2---CoverChangeLevelFragment onCreate levelId = $levelId levelType = $levelType")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCoverChangeLevelBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //обеспечивает установку обложки при открытии CoverChangeFragment и замене обложки через pickImage
        viewModel.coverImageUriLevel.observe(viewLifecycleOwner) { uri ->
            Log.d(TAG, "3--- CoverChangeLevelFragment coverImageUriLevel.observe: uri = $uri ")
            Glide.with(binding.root.context)
                .load(uri)
                .placeholder(R.drawable.muz_player3)
                .error(R.drawable.muz_player2)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(binding.coverImageViewLevel)
        }

        binding.usePhonePhotosLevel.setOnClickListener {
            pickImage.launch("image/*")
        }

        binding.searchOnlineLevel.setOnClickListener {
            openFlickrSearch()
        }

        binding.restoreDefaultLevel.setOnClickListener {
            val uri:Uri = "android.resource://${context?.packageName}/${R.drawable.muz_player5}".toUri()
           when(levelType){
                LevelType.PLAYLIST ->{ viewModel.updateCoverImageLevelAndSave(uri, levelId)}
                LevelType.ALBUM ->{viewModel.updateCoverImageAlbumAndSave(uri, levelId)}
                LevelType.ARTIST ->{viewModel.updateCoverImageArtistsAndSave(uri, levelId)}
                LevelType.FOLDER ->{viewModel.updateCoverImageFolderAndSave(uri, levelId)}
            }
        }

        binding.saveButtonLevel.setOnClickListener {
            viewModel.getCoverImageUriLevel()?. let{curUri->
                Log.d(TAG, "4--- CoverChangeLevelFragment saveButtonLevel: curUri = $curUri")
                when (levelType) {
                    LevelType.PLAYLIST -> viewModel.updateCoverImageLevelAndSave(curUri, levelId) //+
                    LevelType.ALBUM -> { viewModel.updateCoverImageAlbumAndSave(curUri, levelId) }
                    LevelType.ARTIST -> { viewModel.updateCoverImageArtistsAndSave(curUri, levelId) } //+
                    LevelType.FOLDER -> { viewModel.updateCoverImageFolderAndSave(curUri, levelId) } //+
                }
            }
            findNavController().navigateUp()
        }

        binding.coverImageViewLevel.setOnClickListener {
            isExpanded = !isExpanded
            //
            TransitionManager.beginDelayedTransition(
                binding.coverContainer, TransitionSet()
                    .addTransition(ChangeBounds())
                    .addTransition(ChangeImageTransform())
            )
            val params = binding.coverImageViewLevel.layoutParams as ConstraintLayout.LayoutParams
            if (isExpanded) {
                // Оставляем существующие constraints, но меняем размеры
                params.topMargin = 0
                params.width = ConstraintLayout.LayoutParams.MATCH_PARENT
                params.height = ConstraintLayout.LayoutParams.MATCH_PARENT
                params.dimensionRatio = ""
                binding.coverImageViewLevel.scaleType = ImageView.ScaleType.FIT_CENTER
            } else {
                // Возвращаем оригинальные размеры
                params.topMargin = 16
                params.width = 0
                params.height = 0
                params.dimensionRatio = "1:1"
                params.matchConstraintPercentWidth = 0.6f
                binding.coverImageViewLevel.scaleType = ImageView.ScaleType.FIT_CENTER
            }
            binding.coverImageViewLevel.layoutParams = params
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