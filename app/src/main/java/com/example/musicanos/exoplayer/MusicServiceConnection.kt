package com.example.musicanos.exoplayer

import android.content.ComponentName
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.musicanos.others.Constants.NETWORK_ERROR
import com.example.musicanos.others.Event
import com.example.musicanos.others.Resource

class MusicServiceConnection(
    private val context: Context
) {
    private val _isConnected = MutableLiveData<Event<Resource<Boolean>>>()
    val isConnected: LiveData<Event<Resource<Boolean>>> = _isConnected

    private val _networkError = MutableLiveData<Event<Resource<Boolean>>>()
    val networkError: LiveData<Event<Resource<Boolean>>> = _networkError

    private val _playbackState = MutableLiveData<PlaybackStateCompat>()
    val playbackState: LiveData<PlaybackStateCompat?> = _playbackState


    private val _currentPlayingSong = MutableLiveData<MediaMetadataCompat?>()
    val currentPlayingSong: LiveData<MediaMetadataCompat?> = _currentPlayingSong

    lateinit var mediaController: MediaControllerCompat

    val transportControls: MediaControllerCompat.TransportControls
        get() = mediaController.transportControls

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private val mediaBrowserConnectionCallback = MediaBrowserConnectionCallback(context)


    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private val mediaBrowser = MediaBrowserCompat(
        context,
        ComponentName(
            context,
            MusicService::class.java,
        ),
        mediaBrowserConnectionCallback,
        null
    ).apply { connect() }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    fun subscribe(parentId: String, callback: MediaBrowserCompat.SubscriptionCallback){
        mediaBrowser.subscribe(parentId, callback)
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    fun unsubscribe(parentId: String, callback: MediaBrowserCompat.SubscriptionCallback){
        mediaBrowser.unsubscribe(parentId, callback)
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private inner class MediaBrowserConnectionCallback(
        private val context: Context) : MediaBrowserCompat.ConnectionCallback(){
        override fun onConnected() {
            mediaController = MediaControllerCompat(context, mediaBrowser.sessionToken).apply {
                registerCallback(MediaControllerCallback())
            }
            _isConnected.postValue(Event(Resource.success(true)))
        }

        override fun onConnectionSuspended() {
            _isConnected.postValue(Event(Resource.error(
                "Connection was suspended", false
            )))
        }

        override fun onConnectionFailed() {
            _isConnected.postValue(Event(Resource.error(
                "Connection was failed", false
            )))
        }

    }

    private inner class MediaControllerCallback : MediaControllerCompat.Callback() {
        override fun onPlaybackStateChanged(state: PlaybackStateCompat?) {
            _playbackState.postValue(state)
        }

        override fun onMetadataChanged(metadata: MediaMetadataCompat?) {
            _currentPlayingSong.postValue(metadata)

        }

        override fun onSessionEvent(event: String?, extras: Bundle?) {
            super.onSessionEvent(event, extras)
            when(event) {
                NETWORK_ERROR -> _networkError.postValue(
                    Event(
                        Resource.error(
                            "Couldn't Connect to server",
                            data = null
                        )
                    )
                )
            }
        }

        @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
        override fun onSessionDestroyed() {
            mediaBrowserConnectionCallback.onConnectionSuspended()
        }
    }
}