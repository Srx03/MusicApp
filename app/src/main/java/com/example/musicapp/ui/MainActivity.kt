package com.example.musicapp.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.media.session.PlaybackStateCompat
import androidx.activity.viewModels
import androidx.core.view.isVisible
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.example.musicapp.R

import com.example.musicapp.adapters.SwipeSongAdapter
import com.example.musicapp.data.entites.Song
import com.example.musicapp.databinding.ActivityMainBinding
import com.example.musicapp.exoplayer.isPlaying
import com.example.musicapp.exoplayer.toSong
import com.example.musicapp.other.Status.*
import com.example.musicapp.ui.viewmodel.MainViewModel
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_main.*
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

   private lateinit var binding: ActivityMainBinding

    private val mainViewModel: MainViewModel by viewModels()

    @Inject lateinit var swipeSongAdapter: SwipeSongAdapter

    @Inject lateinit var glide: RequestManager

    private var currentSongPlaying: Song? = null

    private var playbackStat:  PlaybackStateCompat? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        subscribeToObservers()

        binding.vpSong.adapter = swipeSongAdapter

        binding.vpSong.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback(){
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                if(playbackStat?.isPlaying == true){
                    mainViewModel.playOrToggleSong(swipeSongAdapter.songs[position])
                }else{
                    currentSongPlaying = swipeSongAdapter.songs[position]
                }
            }
        })

        binding.ivPlayPause.setOnClickListener {
            currentSongPlaying?.let {
                mainViewModel.playOrToggleSong(it, true)
            }
        }

        swipeSongAdapter.setOnItemClickListener {
            navHostFragment.findNavController().navigate(
                R.id.globalActionToSongFragment
            )
        }

        navHostFragment.findNavController().addOnDestinationChangedListener{_, destination, _ ->
        when(destination.id){
            R.id.songFragment -> hideBottomBar()
            R.id.homeFragment -> showBottomBar()
            else -> showBottomBar()
          }
        }
    }

    private fun hideBottomBar(){
        binding.ivCurSongImage.isVisible = false
        binding.vpSong.isVisible = false
        binding.ivPlayPause.isVisible = false
    }

    private fun showBottomBar(){
        binding.ivCurSongImage.isVisible = true
        binding.vpSong.isVisible = true
        binding.ivPlayPause.isVisible = true
    }

private fun switchViewPagerToCurrentSong(song: Song){
    val newItemIndex = swipeSongAdapter.songs.indexOf(song)
    if(newItemIndex != -1){
        binding.vpSong.currentItem = newItemIndex
        currentSongPlaying = song
    }
}

    private fun subscribeToObservers(){
        mainViewModel.mediaItems.observe(this){
            it?.let { result ->
                when(result.status){
                    SUCCESS -> {
                        result.data?.let { songs ->
                            swipeSongAdapter.songs = songs
                            if(songs.isNotEmpty()){
                                    Glide.with(applicationContext).load((currentSongPlaying?: songs[0]).imageUrl).into(binding.ivCurSongImage)
                            }
                            switchViewPagerToCurrentSong(currentSongPlaying ?: return@observe)
                        }
                    }
                    ERROR -> Unit
                    LOADING -> Unit

                }
            }
        }
        mainViewModel.currentingPlayingSong.observe(this){
            if(it == null) return@observe

            currentSongPlaying = it.toSong()
            Glide.with(applicationContext).load(currentSongPlaying?.imageUrl).into(binding.ivCurSongImage)
            switchViewPagerToCurrentSong(currentSongPlaying ?: return@observe)
        }
        mainViewModel.playbackState.observe(this){
            playbackStat = it

            binding.ivPlayPause.setImageResource(
                if(playbackStat?.isPlaying == true) R.drawable.ic_pause else R.drawable.ic_play
            )
        }

        mainViewModel.isConnected.observe(this){
            it?.getContentIfNotHandled()?.let { result ->
                when(result.status){
                    ERROR -> Snackbar.make(rootLayout, result.message?: "An unknown error occured", Snackbar.LENGTH_LONG).show()
                else -> Unit
                }
            }
        }

        mainViewModel.networkError.observe(this){
            it?.getContentIfNotHandled()?.let { result ->
                when(result.status){
                    ERROR -> Snackbar.make(rootLayout, result.message?: "An unknown error occured", Snackbar.LENGTH_LONG).show()
                    else -> Unit
                }
            }
        }
    }
}