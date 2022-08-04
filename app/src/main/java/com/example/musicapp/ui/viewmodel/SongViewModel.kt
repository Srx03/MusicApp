package com.example.musicapp.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.musicapp.exoplayer.MusicServiceConnection
import com.example.musicapp.exoplayer.MusicServices
import com.example.musicapp.exoplayer.currentPlaybackPosition
import com.example.musicapp.other.Constatns.UPDATE_POSITION
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SongViewModel @Inject constructor(
     musicServiceConnection: MusicServiceConnection
) : ViewModel() {

    private val playbackState = musicServiceConnection.playbackState

    private val _currentSongDuration = MutableLiveData<Long>()
    val currentSongDuration: LiveData<Long> = _currentSongDuration

    private val _currentPlayerPosition = MutableLiveData<Long>()
    val currentPlayerPosition: LiveData<Long> = _currentPlayerPosition

    init {
        updateCurrentPlayerPosition()
    }

    private fun updateCurrentPlayerPosition() {
        viewModelScope.launch {
            while(true) {
                val pos = playbackState.value?.currentPlaybackPosition
                if(currentPlayerPosition.value != pos) {
                    _currentPlayerPosition.postValue(pos!!)
                    _currentSongDuration.postValue(MusicServices.curSongDuration)
                }
                delay(UPDATE_POSITION)
            }
        }
    }


}