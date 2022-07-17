package com.example.musicapp.exoplayer

import android.app.PendingIntent
import android.content.Intent
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import androidx.media.MediaBrowserServiceCompat
import com.example.musicapp.exoplayer.callbacks.MusicPlaybackPreparer
import com.example.musicapp.exoplayer.callbacks.MusicPlayerEventListener
import com.example.musicapp.exoplayer.callbacks.MusicPlayerNotificationListener
import com.example.musicapp.other.Constatns.ROOT_ID
import com.example.musicapp.other.Constatns.SERVICE_TAG
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector
import com.google.android.exoplayer2.ext.mediasession.TimelineQueueNavigator
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import javax.inject.Inject

@AndroidEntryPoint
class MusicServices : MediaBrowserServiceCompat() {



    @Inject
    lateinit var dataSourceFactory: DefaultDataSourceFactory

    @Inject
    lateinit var exoPlayer: ExoPlayer

    @Inject
    lateinit var firebaseMusicSource: FirebaseMusicSource

    private lateinit var musicNotificationManager: MusicNotificationManager

    private val serverJob = Job()
    private val serviceScope = CoroutineScope(Dispatchers.Main + serverJob)

    private lateinit var  mediaSession: MediaSessionCompat
    private lateinit var mediaSessionConnector: MediaSessionConnector

    var isForegroundService = false

    private lateinit var musicPlayEventListener: MusicPlayerEventListener

    companion object{
        var currentSongDuration = 0L
        private set
    }

    private var currentPlayingSong: MediaMetadataCompat? = null

    private var isPlayerInitialized = false

    override fun onCreate() {
        super.onCreate()

        serviceScope.launch {
            firebaseMusicSource.fetchMediaData()
        }

        val activityIntent = packageManager?.getLaunchIntentForPackage(packageName)?.let {
            PendingIntent.getActivity(this,0, it,0)
        }

        mediaSession = MediaSessionCompat(this, SERVICE_TAG).apply {
            setSessionActivity(activityIntent)
            isActive = true
        }

        sessionToken = mediaSession.sessionToken
        musicNotificationManager  = MusicNotificationManager(this,mediaSession.sessionToken,MusicPlayerNotificationListener(this)){
                currentSongDuration = exoPlayer.duration
        }

        val musicPlaybackPreparer = MusicPlaybackPreparer(firebaseMusicSource){
            currentPlayingSong = it
            preperePlayer(firebaseMusicSource.songs, it, true)
        }

        mediaSessionConnector = MediaSessionConnector(mediaSession)
        mediaSessionConnector.setPlaybackPreparer(musicPlaybackPreparer)
        mediaSessionConnector.setQueueNavigator(MusicQueueNaigator())
        mediaSessionConnector.setPlayer(exoPlayer)

        musicPlayEventListener = MusicPlayerEventListener(this)
        exoPlayer.addListener(musicPlayEventListener)
        musicNotificationManager.showNotification(exoPlayer)
    }

    private inner class MusicQueueNaigator: TimelineQueueNavigator(mediaSession){
        override fun getMediaDescription(player: Player, windowIndex: Int): MediaDescriptionCompat {
            return firebaseMusicSource.songs[windowIndex].description
        }
    }

    private fun preperePlayer(songs: List<MediaMetadataCompat>, itemPlay: MediaMetadataCompat?, playNow: Boolean){
        val curSongIndex = if(currentPlayingSong == null) 0 else songs.indexOf(itemPlay)
        exoPlayer.prepare(firebaseMusicSource.asMediaSource(dataSourceFactory))
        exoPlayer.seekTo(curSongIndex, 0L)
        exoPlayer.playWhenReady = playNow
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        exoPlayer.stop()
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()

        exoPlayer.removeListener(musicPlayEventListener)
        exoPlayer.release()
    }


    override fun onGetRoot(
        clientPackageName: String,
        clientUid: Int,
        rootHints: Bundle?
    ): BrowserRoot? {
        return  BrowserRoot(ROOT_ID, null)
    }

    override fun onLoadChildren(
        parentId: String,
        result: Result<MutableList<MediaBrowserCompat.MediaItem>>
    ) {
        when(parentId){
            ROOT_ID -> {
                val resultsSent = firebaseMusicSource.whenReady { isInitialized ->
                    if(isInitialized){
                        result.sendResult(firebaseMusicSource.asMediaItems())
                        if (!isPlayerInitialized && firebaseMusicSource.songs.isNotEmpty()){
                            preperePlayer(firebaseMusicSource.songs, firebaseMusicSource.songs[0], false)
                            isPlayerInitialized = true
                        }
                    } else{
                        result.sendResult(null)
                    }
                }
                if(!resultsSent){
                    result.detach()
                }
            }
        }

    }
}