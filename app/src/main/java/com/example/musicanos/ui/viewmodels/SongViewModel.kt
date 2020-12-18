package com.example.musicanos.ui.viewmodels

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.musicanos.exoplayer.MusicService
import com.example.musicanos.exoplayer.MusicServiceConnection
import com.example.musicanos.exoplayer.currentPlaybackPosition
import com.example.musicanos.others.Constants.UPDATE_PLAYER_POSITION_INTERVAL
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SongViewModel @ViewModelInject constructor(
    musicServiceConnection: MusicServiceConnection
) : ViewModel(){
    private val playbackState = musicServiceConnection.playbackState

    private val _curSongDuration = MutableLiveData<Long>()

    val curSongDuration: LiveData<Long> = _curSongDuration

    private val _curPlayerPosition = MutableLiveData<Long>()

    val curPlayerPosition: LiveData<Long> = _curPlayerPosition

    init {

        updateCurrentPlayerPosition()
    }


    private fun updateCurrentPlayerPosition() {
        viewModelScope.launch {
            while (true) {
                val pos = playbackState.value?.currentPlaybackPosition
                if(curPlayerPosition.value != pos) {
                    _curPlayerPosition.postValue(pos)
                    _curPlayerPosition.postValue(MusicService.currentSongDuration)
                }
                delay(UPDATE_PLAYER_POSITION_INTERVAL)
            }
        }
    }
}