package com.example.musicapp.ui

import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat.METADATA_KEY_MEDIA_ID
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.musicapp.data.entites.Song
import com.example.musicapp.exoplayer.MusicServiceConnection
import com.example.musicapp.exoplayer.isPlayEnabled
import com.example.musicapp.exoplayer.isPlaying
import com.example.musicapp.exoplayer.isPrepared
import com.example.musicapp.other.Constatns.ROOT_ID
import com.example.musicapp.other.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val musicServiceConnection: MusicServiceConnection
) : ViewModel() {

    private val _mediaItems = MutableLiveData<Resource<List<Song>>>()
     val mediaItems: LiveData<Resource<List<Song>>> = _mediaItems

    val isConnected = musicServiceConnection.isConnected
    val networkError = musicServiceConnection.networkError
    val currentingPlayingSong = musicServiceConnection.currentingPlayingSong
    val playbackState = musicServiceConnection.playbackState


    init {
        _mediaItems.postValue(Resource.loading(null))
        musicServiceConnection.subscribe(ROOT_ID, object: MediaBrowserCompat.SubscriptionCallback(){
            override fun onChildrenLoaded(
                parentId: String,
                children: MutableList<MediaBrowserCompat.MediaItem>
            ) {
                super.onChildrenLoaded(parentId, children)
                val items = children.map {
                    Song(
                        it.mediaId!!,
                        it.description.title.toString(),
                        it.description.subtitle.toString(),
                        it.description.mediaUri.toString(),
                        it.description.iconUri.toString()
                    )
                }
                _mediaItems.postValue(Resource.success(items))
            }
        })

    }

    fun skipToNextSong(){
        musicServiceConnection.transportControls.skipToNext()
    }

    fun skipToPreviousSong(){
        musicServiceConnection.transportControls.skipToPrevious()
    }

    fun seekTo(position: Long){
        musicServiceConnection.transportControls.seekTo(position)
    }

    fun playOrToggleSong(mediaItem: Song, toggle: Boolean = false){
        val isPrepared = playbackState.value?.isPrepared?: false
        if (isPrepared && mediaItem.mediaId == currentingPlayingSong.value?.getString(METADATA_KEY_MEDIA_ID)){
            playbackState.value?.let {
                when{
                    it.isPlaying -> if(toggle) musicServiceConnection.transportControls.pause()
                    it.isPlayEnabled -> musicServiceConnection.transportControls.play()
                    else -> Unit
                }
            }
        } else{
            musicServiceConnection.transportControls.playFromMediaId(mediaItem.mediaId, null)
        }
    }


    override fun onCleared() {
        super.onCleared()
        musicServiceConnection.unSubscribe(ROOT_ID,object : MediaBrowserCompat.SubscriptionCallback(){})
    }


}