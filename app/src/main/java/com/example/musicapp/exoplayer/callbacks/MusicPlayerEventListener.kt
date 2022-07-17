package com.example.musicapp.exoplayer.callbacks

import android.widget.Toast
import com.example.musicapp.exoplayer.MusicServices
import com.google.android.exoplayer2.PlaybackException
import com.google.android.exoplayer2.Player

class MusicPlayerEventListener(private val musicServices: MusicServices): Player.Listener {


    override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
        when(playbackState){
            Player.STATE_READY ->{
                musicServices.stopForeground(false)
            }

            Player.STATE_BUFFERING -> {
                TODO()
            }
            Player.STATE_ENDED -> {
                TODO()
            }
            Player.STATE_IDLE -> {
                TODO()
            }
        }
    }

    override fun onPlayerError(error: PlaybackException) {
        super.onPlayerError(error)
        Toast.makeText(musicServices, "An unknown error!", Toast.LENGTH_LONG).show()
    }
}