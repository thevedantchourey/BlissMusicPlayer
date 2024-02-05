package com.app.bliss

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import kotlin.system.exitProcess

class NotificationBroadcast: BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        when(intent?.action){
            ApplicationClass.PREVIOUS -> preNextSong(increment = false)
            ApplicationClass.PLAY -> if(PlayerActivity.isPlaying){ pause() } else{ play() }
            ApplicationClass.NEXT ->  preNextSong(increment = true)
            ApplicationClass.EXIT -> {
                PlayerActivity.musicService!!.stopSelf()
                PlayerActivity.musicService!!.mediaPlayer!!.release()
                PlayerActivity.musicService = null
                exitProcess(1)
            }
        }
    }


    private fun play(){
        PlayerActivity.isPlaying = true
        PlayerActivity.musicService!!.mediaPlayer!!.start()
        PlayerActivity.musicService!!.showNotification(R.drawable.baseline_pause_circle_outline_24,"pause")
    }

    private fun pause(){
        PlayerActivity.isPlaying = false
        PlayerActivity.musicService!!.mediaPlayer!!.pause()
        PlayerActivity.musicService!!.showNotification(R.drawable.baseline_play_circle_outline_24,"play")
    }

    private fun preNextSong(increment: Boolean){
        songPst(increment = increment)
        PlayerActivity.musicService!!.layout()
        pause()
        PlayerActivity.musicService!!.mediaPlayer!!.reset()
        PlayerActivity.musicService!!.mediaPlayer = MediaPlayer()
        PlayerActivity.musicService!!.mediaPlayer!!.setDataSource(PlayerActivity.musicListPA[PlayerActivity.songPosition].path)
        PlayerActivity.musicService!!.mediaPlayer!!.prepare()
        PlayerActivity.binding.recover.text = formatDuration(PlayerActivity.musicService!!.mediaPlayer!!.currentPosition.toLong())
        PlayerActivity.binding.covered.text = formatDuration(PlayerActivity.musicService!!.mediaPlayer!!.duration.toLong())
        PlayerActivity.binding.seeking.progress = 0
        PlayerActivity.binding.seeking.max = PlayerActivity.musicService!!.mediaPlayer!!.duration
        play()
        PlayerActivity.fIndex = favouriteCheck(PlayerActivity.musicListPA[PlayerActivity.songPosition].id)
        if(PlayerActivity.isFavourite){
            PlayerActivity.binding.liked.setImageResource(R.drawable.liked)
            PlayerActivity.binding.liked.setBackgroundResource(R.drawable.btn_shape4)
        }else{
            PlayerActivity.binding.liked.setImageResource(R.drawable.like)
            PlayerActivity.binding.liked.setBackgroundResource(R.drawable.btn_shape)
        }

    }



}