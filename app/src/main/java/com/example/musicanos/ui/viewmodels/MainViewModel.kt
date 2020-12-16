package com.example.musicanos.ui.viewmodels

import android.os.Build
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat.METADATA_KEY_MEDIA_ID
import androidx.annotation.RequiresApi
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.musicanos.data.entities.Song
import com.example.musicanos.exoplayer.MusicServiceConnection
import com.example.musicanos.exoplayer.isPlayEnabled
import com.example.musicanos.exoplayer.isPlaying
import com.example.musicanos.exoplayer.isPrepared
import com.example.musicanos.others.Constants.MEDIA_ROOT_ID
import com.example.musicanos.others.Resource


@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
class MainViewModel @ViewModelInject constructor(
    private val musicServiceConnection: MusicServiceConnection
): ViewModel() {
    private val _mediaItems = MutableLiveData<Resource<List<Song>>>()
    val mediaItems : LiveData<Resource<List<Song>>> = _mediaItems

    val isConnected = musicServiceConnection.isConnected
    val networkError = musicServiceConnection.networkError
    val currentPlayingSong = musicServiceConnection.currentPlayingSong
    val playbackState = musicServiceConnection.playbackState

    init {
        _mediaItems.postValue(Resource.loading(null))
        musicServiceConnection.subscribe(MEDIA_ROOT_ID, object : MediaBrowserCompat.SubscriptionCallback(){
            override fun onChildrenLoaded(parentId: String, children: MutableList<MediaBrowserCompat.MediaItem>) {
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

    fun skipToNextSong() {
        musicServiceConnection.transportControls.skipToNext()
    }
    fun skipToPreviousSong() {
        musicServiceConnection.transportControls.skipToPrevious()
    }
    fun seekTo(pos: Long) {
        musicServiceConnection.transportControls.seekTo(pos)
    }

    fun playOrToggleSong(mediaItem: Song, toggle: Boolean = false) {

        val isPrepared = playbackState.value?.isPrepared ?: false
        if (isPrepared && mediaItem.mediaId == currentPlayingSong.value?.getString(METADATA_KEY_MEDIA_ID)) {
            playbackState.value?.let { playbackState ->
                when {
                    playbackState.isPlaying -> if (toggle) musicServiceConnection.transportControls.pause()
                    playbackState.isPlayEnabled -> if (toggle) musicServiceConnection.transportControls.play()
                    else -> Unit
                }
            }
        } else {
            musicServiceConnection.transportControls.playFromMediaId(mediaItem.mediaId, null)
        }

    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onCleared() {
        super.onCleared()
        musicServiceConnection.unsubscribe(MEDIA_ROOT_ID, object  : MediaBrowserCompat.SubscriptionCallback(){})
    }
}