package com.example.musicanos.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import com.bumptech.glide.RequestManager
import com.example.musicanos.R
import com.example.musicanos.adapters.SwipeSongAdapter
import com.example.musicanos.data.entities.Song
import com.example.musicanos.exoplayer.toSong
import com.example.musicanos.others.Status
import com.example.musicanos.ui.viewmodels.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_main.*
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val mainViewModel: MainViewModel by viewModels()

    @Inject
    lateinit var swipeSongAdapter: SwipeSongAdapter

    private var currentPlayingSong: Song? = null

    @Inject
    lateinit var glide: RequestManager
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        subscribeToObservers()

        vpSong.adapter = swipeSongAdapter
    }

    private fun subscribeToObservers() {
        mainViewModel.mediaItems.observe(this){
            it?.let {
                when(it.status) {
                    Status.SUCCESS -> {
                        it.data.let { song ->
                            if (song != null) {
                                swipeSongAdapter.songs = song
                                if (song.isNotEmpty()){
                                    glide.load((currentPlayingSong?:song[0]).imageUrl).into(ivCurSongImage)
                                }
                                switchViewPagerToCurrentSong(currentPlayingSong?: return@observe)
                            }
                        }
                    }
                    Status.ERROR -> Unit
                    Status.LOADING -> Unit
                }
            }
        }
        mainViewModel.currentPlayingSong.observe(this){
            if (it == null) return@observe
            currentPlayingSong = it.toSong()
            glide.load(currentPlayingSong?.imageUrl).into(ivCurSongImage)
            switchViewPagerToCurrentSong(currentPlayingSong?: return@observe)
        }
    }

    private fun switchViewPagerToCurrentSong(song: Song) {
        val newItemIndex = swipeSongAdapter.songs.indexOf(song)
        if(newItemIndex != -1) {
            vpSong.currentItem = newItemIndex
            currentPlayingSong = song
        }
    }
}