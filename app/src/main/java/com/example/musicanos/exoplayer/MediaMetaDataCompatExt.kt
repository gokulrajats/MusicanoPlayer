package com.example.musicanos.exoplayer

import android.support.v4.media.MediaMetadataCompat
import com.example.musicanos.data.entities.Song

fun MediaMetadataCompat.toSong(): Song {
    return description.let {
        Song(
            it.mediaId.toString() ?: "",
            it.title.toString(),
            it.subtitle.toString(),
            it.mediaUri.toString(),
            it.iconUri.toString()
        )
    }
}