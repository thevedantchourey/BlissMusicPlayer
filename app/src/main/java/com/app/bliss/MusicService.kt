package com.app.bliss

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.graphics.BitmapFactory
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Binder
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.support.v4.media.session.MediaSessionCompat
import android.text.TextUtils
import androidx.core.app.NotificationCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions



class MusicService: Service(), AudioManager.OnAudioFocusChangeListener {

    private var myBinder = MyBinder()
    var mediaPlayer:MediaPlayer? = null
    private lateinit var mediaSession: MediaSessionCompat
    private lateinit var runnable: Runnable
//    lateinit var audioManager: AudioManager

    override fun onBind(intent: Intent?): IBinder{
        mediaSession = MediaSessionCompat(baseContext,"Music")
        return myBinder

    }
    inner class MyBinder: Binder(){
        fun currentService(): MusicService{
            return this@MusicService
        }
    }


    @SuppressLint("RemoteViewLayout", "UnspecifiedImmutableFlag")
    fun showNotification(play:Int, title:String) {

        val intent = Intent(baseContext, MainActivity::class.java)
        val contentIntent = PendingIntent.getActivity(this,0,intent, PendingIntent.FLAG_IMMUTABLE)

        val prevIntent = Intent(baseContext,NotificationBroadcast::class.java).setAction(ApplicationClass.PREVIOUS)
        val prevPendingIntent = PendingIntent.getBroadcast(baseContext,0,prevIntent,PendingIntent.FLAG_MUTABLE)


        val playIntent = Intent(baseContext,NotificationBroadcast::class.java).setAction(ApplicationClass.PLAY)
        val playPendingIntent = PendingIntent.getBroadcast(baseContext,0,playIntent,PendingIntent.FLAG_MUTABLE)


        val nextIntent = Intent(baseContext,NotificationBroadcast::class.java).setAction(ApplicationClass.NEXT)
        val nextPendingIntent = PendingIntent.getBroadcast(baseContext,0,nextIntent,PendingIntent.FLAG_MUTABLE)


        val exitIntent = Intent(baseContext,NotificationBroadcast::class.java).setAction(ApplicationClass.EXIT)
        val exitPendingIntent = PendingIntent.getBroadcast(baseContext,0,exitIntent,PendingIntent.FLAG_MUTABLE)


        val imgArt = getImage(PlayerActivity.musicListPA[PlayerActivity.songPosition].path!!)
        val img  = if(imgArt != null){
            BitmapFactory.decodeByteArray(imgArt, 0,imgArt.size)
        }else{
            BitmapFactory.decodeResource(
                resources,
                R.drawable.headphoneguy_made_by_vedant_r_j_chourey
            )
        }


//        val cover = binding.albumCover
//        val img = Glide.with(this).load( PlayerActivity.musicListPA[PlayerActivity.songPosition].albumCover)
//            .apply(
//                RequestOptions()
//                    .placeholder(R.drawable.headphoneguy_made_by_vedant_r_j_chourey)
//                    .centerCrop()).into(cover)



        val notification = NotificationCompat.Builder(baseContext, ApplicationClass.CHANNEL_ID)
            .setContentIntent(contentIntent)
            .setContentTitle(PlayerActivity.musicListPA[PlayerActivity.songPosition].title)
            .setContentText(PlayerActivity.musicListPA[PlayerActivity.songPosition].artist)
            .setStyle(androidx.media.app.NotificationCompat.MediaStyle().setMediaSession(mediaSession.sessionToken))
            .setCategory("Audio")
            .setLargeIcon(BitmapFactory.decodeResource(resources, R.drawable.headphoneguy_made_by_vedant_r_j_chourey2))
            .setSmallIcon(R.drawable.music_note)
            .setLargeIcon(img)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .addAction(R.drawable.baseline_arrow_back_ios_24,"prev",prevPendingIntent)
            .addAction(play,title,playPendingIntent)
            .addAction(R.drawable.baseline_arrow_forward_ios_24,"next",nextPendingIntent)
            .addAction(0,"exit", exitPendingIntent)
            .setOnlyAlertOnce(true)
            .build()

        startForeground(10,notification)

//        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
//            if (PlayerActivity.isPlaying){
//                PlayerActivity.isPlaying = false
//                mediaSession.setMetadata(
//                    MediaMetadataCompat.Builder()
//                        .putLong(
//                            MediaMetadataCompat.METADATA_KEY_DURATION,
//                            mediaPlayer!!.duration.toLong()
//                        )
//                        .build()
//                )
//                mediaSession.setPlaybackState(
//                    PlaybackStateCompat
//                        .Builder()
//                        .setState(
//                            PlaybackStateCompat.STATE_PLAYING,
//                            mediaPlayer!!.currentPosition.toLong(),
//                            0F
//                        )
//                        .setActions(PlaybackStateCompat.ACTION_SEEK_TO)
//                        .build()
//                )
//            }else{
//                PlayerActivity.isPlaying = true
//                mediaSession.setMetadata(
//                    MediaMetadataCompat.
//                    Builder()
//                        .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, mediaPlayer!!.duration.toLong())
//                        .build()
//                )
//                mediaSession.setPlaybackState(PlaybackStateCompat
//                    .Builder()
//                    .setState(PlaybackStateCompat.STATE_PLAYING, mediaPlayer!!.currentPosition.toLong(), 1F)
//                    .setActions(PlaybackStateCompat.ACTION_SEEK_TO)
//                    .build()
//                )
//            }
//        }

    }



    fun layout(){
        val songName = PlayerActivity.binding.songName
        val artist = PlayerActivity.binding.Artist
        val cover = PlayerActivity.binding.albumArt
        songName.text= PlayerActivity.musicListPA[PlayerActivity.songPosition].title
        songName.ellipsize = TextUtils.TruncateAt.MARQUEE
        songName.isSingleLine = true
        songName.isSelected = true
        artist.text =  PlayerActivity.musicListPA[PlayerActivity.songPosition].artist
        Glide.with(this).load( PlayerActivity.musicListPA[PlayerActivity.songPosition].albumCover)
            .apply(
                RequestOptions()
                    .placeholder(R.drawable.headphoneguy_made_by_vedant_r_j_chourey)
                    .centerCrop()).into(cover)
    }


    fun seekBarSetup(){
        runnable = Runnable {
            PlayerActivity.binding.recover.text = formatDuration(mediaPlayer!!.currentPosition.toLong())
            PlayerActivity.binding.seeking.progress = mediaPlayer!!.currentPosition
            Handler(Looper.getMainLooper()).postDelayed(runnable,200)
        }
        Handler(Looper.getMainLooper()).postDelayed(runnable,0)
    }

    override fun onAudioFocusChange(focusChange: Int) {
        if (focusChange<=0){
            PlayerActivity.binding.play.setImageResource(R.drawable.play)
            NowPlaying.binding.play.setImageResource(R.drawable.play)
            PlayerActivity.isPlaying = false
            mediaPlayer!!.pause()
        }else{

            PlayerActivity.binding.play.setImageResource(R.drawable.pause)
            NowPlaying.binding.play.setImageResource(R.drawable.pause)
            PlayerActivity.isPlaying = true
            mediaPlayer!!.start()
        }
    }


}