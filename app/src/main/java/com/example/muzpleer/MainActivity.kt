package com.example.muzpleer

import android.Manifest
import android.content.ContentUris
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.core.view.GravityCompat
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.Lifecycle
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.bumptech.glide.Glide
import com.example.muzpleer.databinding.ActivityMainBinding
import com.example.muzpleer.model.Song
import com.example.muzpleer.ui.local.helper.IPreferenceHelper
import com.example.muzpleer.ui.local.helper.PreferenceHelperImpl
import com.example.muzpleer.ui.local.viewmodel.SharedViewModel
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private var doubleBackToExitPressedOnce = false
    private lateinit var navController:NavController
    private val viewModel: SharedViewModel by viewModel()

    private lateinit var playerLayout: ConstraintLayout
    private lateinit var title: TextView
    private lateinit var artist: TextView
    private lateinit var artWork: ImageView
    private lateinit var previous: ImageView
    private lateinit var playPause: ImageView
    private lateinit var next: ImageView

    private lateinit var appPreferences: IPreferenceHelper
    private var currentSong: Song? = null

    private val storagePermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            scanForMusic()
        } else {
            handlePermissionDenied()
        }
    }

    private val manageStorageLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (hasStoragePermission()) {
            scanForMusic()
        } else {
            handlePermissionDenied()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        navController = findNavController(R.id.nav_host_fragment_content_main)

        appPreferences =PreferenceHelperImpl(this.application)
        // Восстанавливаем последнюю песню
        val savedSongId = appPreferences.getCurrentSongId()
        if (savedSongId != -1L) {
            viewModel.setCurrentSongById(savedSongId)
        }
       //получаем разрешения
        checkPermissions()
    }

    private fun scanForMusic() {

        initViews()

        setSupportActionBar(binding.appBarMain.toolbar)
        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.tabLocalFragment,  R.id.settingsFragment), drawerLayout
        )

        //navController.navigate(R.id.localFragment)
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        viewModel.isPlaying.observe(this) { isPlaying ->
            playPause.setImageResource(
                if (isPlaying) R.drawable.ic_pause_white else R.drawable.ic_play_white
            )
        }

        viewModel.songAndPlaylist.observe(this) { songAndPlaylist ->
            //находим индекс трека в плейлисте
            val indexOfTrack = if(songAndPlaylist.song.isLocal){
                songAndPlaylist.playlist.indexOfFirst { it.mediaUri == songAndPlaylist.song.mediaUri }
            }else{
                songAndPlaylist.playlist.indexOfFirst { it.resourceId == songAndPlaylist.song.resourceId  }
            }
            Log.d(TAG, "###MainActivity scanForMusic " +
                    "indexOfTrack = $indexOfTrack " +
                    "songAndPlaylist.playlist.size = ${songAndPlaylist.playlist.size}" +
                    " currentSong title= ${songAndPlaylist.song.title} ")

            Log.d(TAG, "###MainActivity scanForMusic songAndPlaylist.playlist = " +
                    "${songAndPlaylist.playlist.map { it.title }}")

            viewModel.setPlaylistForHandler(songAndPlaylist.playlist, indexOfTrack)
        }

        viewModel.currentSong.observe(this) {songCurrent->
            currentSong = songCurrent
            songCurrent?. let {
                title.text=songCurrent.title
                artist.text=songCurrent.artist
                // Загружаем обложку, если есть
                val albumArtUri=ContentUris.withAppendedId(
                    "content://media/external/audio/albumart".toUri(),
                    songCurrent.albumId
                )
                Glide.with(this)
                    .load(if (songCurrent.isLocal) albumArtUri else it.cover)
                    .placeholder(R.drawable.muz_player3)
                    .error(R.drawable.muz_player3)
                    .into(artWork)
            }
        }
           //управление видимостью нижнего плеера из фрагмента:
            viewModel.playerVisibility.observe(this) { isVisible ->
                playerLayout.visibility = if (isVisible) View.VISIBLE else View.GONE
            }

       //initMenu() нельзя - иначе двоится меню тулбара
    }

    private fun initViews() {
        playerLayout=binding.appBarMain.contentMain.playerBottom

        title=binding.appBarMain.contentMain.title
        artist=binding.appBarMain.contentMain.artist
        artWork=binding.appBarMain.contentMain.artwork
        previous=binding.appBarMain.contentMain.previous
        playPause=binding.appBarMain.contentMain.playPause
        next=binding.appBarMain.contentMain.next

        previous.setOnClickListener { viewModel.playPrevious() }
        playPause.setOnClickListener { viewModel.togglePlayPause() }
        next.setOnClickListener { viewModel.playNext() }

        artWork.setOnClickListener { navController.navigate(R.id.playerFragment) }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    override fun onDestroy() {
        super.onDestroy()
        // Сохраняем текущую песню при закрытии
        currentSong?.let { appPreferences.saveCurrentSongId(it.id)  }
    }

    private fun checkPermissions() {
        when {
            hasStoragePermission() -> {
                scanForMusic()
            }
            shouldShowRequestPermissionRationale(getRequiredPermission()) -> {
                showPermissionRationale()
            }
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && !Environment.isExternalStorageManager() -> {
                requestManageExternalStorage()
            }
            else -> {
                requestStoragePermission()
            }
        }
    }

    private fun hasStoragePermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Environment.isExternalStorageManager() ||
                    ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.READ_MEDIA_AUDIO
                    ) == PackageManager.PERMISSION_GRANTED
        } else {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun getRequiredPermission(): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_AUDIO
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }
    }

    private fun requestStoragePermission() {
        storagePermissionLauncher.launch(getRequiredPermission())
    }

    private fun requestManageExternalStorage() {
        try {
            val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
            intent.data = "package:$packageName".toUri()
            manageStorageLauncher.launch(intent)
        } catch (e: Exception) {
            val intent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
            manageStorageLauncher.launch(intent)
        }
    }

    private fun showPermissionRationale() {
        AlertDialog.Builder(this)
            .setTitle("Требуется разрешение")
            .setMessage("Для поиска музыкальных треков приложению нужен доступ к вашим аудиофайлам")
            .setPositiveButton("Разрешить") { _, _ ->
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    requestManageExternalStorage()
                } else {
                    requestStoragePermission()
                }
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    private fun handlePermissionDenied() {
        if (!shouldShowRequestPermissionRationale(getRequiredPermission())) {
            showOpenSettingsDialog()
        } else {
            Toast.makeText(
                this,
                "Разрешение отклонено. Функционал ограничен",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun showOpenSettingsDialog() {
        AlertDialog.Builder(this)
            .setTitle("Требуется разрешение")
            .setMessage("Вы запретили доступ к медиафайлам. Хотите открыть настройки и предоставить разрешение?")
            .setPositiveButton("Настройки") { _, _ ->
                openAppSettings()
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    private fun openAppSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        intent.data = Uri.fromParts("package", packageName, null)
        startActivity(intent)
    }

    //при нажатии на кнопку Назад если фрагмент реализует BackButtonListener, вызываем метод backPressed
    //при этом если мы в homeFragment   - выходим из приложения по двойному щелчку,
    // а если в другом экране - делаем то, что там прописано
    override fun onBackPressed() {
        //если мы в homeFragment, то при нажатии Назад показываем Snackbar и при повторном
        //нажати в течении 2 секунд закрываем приложение
        Log.d(TAG,"MainActivity onBackPressed  Destination = ${navController.currentDestination?.label}")
        if( navController.currentDestination?.id  == R.id.tabLocalFragment){
            Log.d(TAG, "MainActivity onBackPressed  это TabLocalFragment")
            //если флаг = true - а это при двойном щелчке - закрываем программу
            if (doubleBackToExitPressedOnce) {
                super.onBackPressed()
                return
            }
            doubleBackToExitPressedOnce = true //выставляем флаг = true
            //закрываем шторку, если была открыта
            if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
                binding.drawerLayout.closeDrawer(GravityCompat.START)
            }
            //показываем Snackbar: Для выхода нажмите  НАЗАД  ещё раз
            Snackbar.make(
                findViewById(android.R.id.content), this.getString(R.string.forExit),
                Snackbar.LENGTH_SHORT
            ).show()
            //запускаем поток, в котором через 2 секунды меняем флаг
            Handler(Looper.getMainLooper())
                .postDelayed({ doubleBackToExitPressedOnce = false }, 2000)
        }else{
            Log.d(TAG, "MainActivity onBackPressed  это НЕ TabLocalFragment ")
            super.onBackPressed()
        }
    }

    companion object{
        const val TAG = "33333"
    }
}
