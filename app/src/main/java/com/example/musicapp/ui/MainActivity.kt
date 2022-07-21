package com.example.musicapp.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.example.musicapp.R
import com.example.musicapp.adapters.SwipeSongAdapter
import com.example.musicapp.data.entites.Song
import com.example.musicapp.databinding.ActivityMainBinding
import com.example.musicapp.databinding.ListItemBinding
import com.example.musicapp.databinding.SwipeItemBinding
import com.example.musicapp.exoplayer.toSong
import com.example.musicapp.other.Status
import com.example.musicapp.other.Status.*
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

   private lateinit var binding: ActivityMainBinding

    private val mainViewModel: MainViewModel by viewModels()

    @Inject lateinit var swipeSongAdapter: SwipeSongAdapter

    @Inject lateinit var glide: RequestManager

    private var currentSongPlaying: Song? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.vpSong.adapter = swipeSongAdapter

        subscribeToObservers()


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
    }

}