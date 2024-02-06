package com.app.bliss

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.media.MediaPlayer
import android.media.audiofx.AudioEffect
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import android.text.TextUtils
import android.widget.SeekBar
import android.widget.Toast
import com.app.bliss.databinding.ActivityPlayerBinding
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions

class PlayerActivity : AppCompatActivity(), ServiceConnection, MediaPlayer.OnCompletionListener {


    companion object{

        lateinit var musicListPA : ArrayList<SongData>
        var songPosition: Int = 0
        var isPlaying: Boolean = false
        var isFavourite: Boolean = false
        var fIndex: Int = -1
        var musicService: MusicService? = null
        var liked = MainActivity.favList
        var repeat : Boolean = false
        @SuppressLint("StaticFieldLeak")
        lateinit var binding: ActivityPlayerBinding
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.customTheme)
        binding = ActivityPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initialized()


        binding.play.setOnClickListener { if (isPlaying){ pause() } else{ play() } }
        binding.liked.setOnClickListener {
                if (isFavourite){
                    isFavourite = false
                    binding.liked.setImageResource(R.drawable.like)
                    binding.liked.setBackgroundResource(R.drawable.btn_shape)
                    liked.removeAt(fIndex)
                } else{
                    isFavourite = true
                    binding.liked.setImageResource(R.drawable.liked)
                    binding.liked.setBackgroundResource(R.drawable.btn_shape4)
                    MainActivity.favList.add(musicListPA[songPosition])
                }
        }
        binding.backward.setOnClickListener{ backWard() }
        binding.forward.setOnClickListener{  forWard() }
        binding.previous.setOnClickListener { finish() }
        binding.repeat.setOnClickListener {
            if (!repeat){
                repeat = true
                binding.repeat.setBackgroundResource(R.drawable.btn_shape)
                binding.repeat.setImageResource(R.drawable.skipbackward)
            }else{
                repeat = false
                binding.repeat.setBackgroundResource(R.drawable.btn_shape2)
                binding.repeat.setImageResource(R.drawable.repeatlock)
            }
        }

        binding.equalizer.setOnClickListener {
            try{
                val eqIntent = Intent(AudioEffect.ACTION_DISPLAY_AUDIO_EFFECT_CONTROL_PANEL)
                eqIntent.putExtra(
                    AudioEffect.EXTRA_AUDIO_SESSION,
                    musicService!!.mediaPlayer!!.audioSessionId
                )
                eqIntent.putExtra(AudioEffect.EXTRA_PACKAGE_NAME, baseContext.packageName)
                startActivityForResult(eqIntent, 10)
            }catch (e:Exception){ Toast.makeText(this, " Equalizer Feature Not Supported!", Toast.LENGTH_LONG).show() }
        }


        binding.cover.setOnLongClickListener{
            val share = Intent()
            share.action = Intent.ACTION_SEND
            share.type = "audio/*"
            share.putExtra(Intent.EXTRA_STREAM, Uri.parse(musicListPA[songPosition].path!!))
            startActivity(Intent.createChooser(share, "Share your Fav Music with Your Fav Ones!"))
            true
        }


        binding.seeking.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
               if(fromUser) musicService!!.mediaPlayer!!.seekTo(progress)
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) = Unit
            override fun onStopTrackingTouch(seekBar: SeekBar?) = Unit

        })

    }


    private fun initialized(){
        songPosition = intent.getIntExtra("index",0)
        when(intent.getStringExtra("class")){

            "MusicPlayerAdapter" ->{
                if(MainActivity.isClicked){
                    val intent = Intent(this, MusicService::class.java)
                    bindService(intent,this, BIND_AUTO_CREATE)
                    startService(intent)
                    musicListPA = ArrayList()
                    musicListPA.addAll(MainActivity.favList)
                    layout()
                }else{
                    val intent = Intent(this, MusicService::class.java)
                    bindService(intent,this, BIND_AUTO_CREATE)
                    startService(intent)
                    musicListPA = ArrayList()
                    musicListPA.addAll(MainActivity.audioList)
                    layout()
                }
            }
            "NowPlaying"->{
                binding.recover.text = formatDuration(musicService!!.mediaPlayer!!.currentPosition.toLong())
                binding.covered.text = formatDuration(musicService!!.mediaPlayer!!.duration.toLong())
                binding.seeking.max = musicService!!.mediaPlayer!!.duration
                musicService!!.mediaPlayer!!.setOnCompletionListener(this)
                if (isPlaying) {
                    binding.play.setImageResource(R.drawable.pause)
                } else {
                    binding.play.setImageResource(R.drawable.play)
                }
                layout()
            }
            "shuffle"->{
                if(MainActivity.isClicked){
                    val intent = Intent(this, MusicService::class.java)
                    bindService(intent, this, BIND_AUTO_CREATE)
                    startService(intent)
                    musicListPA = ArrayList()
                    musicListPA.addAll(MainActivity.favList)
                    musicListPA.shuffle()
                    layout()
                }
            }
        }
    }


    private fun layout(){
        fIndex = favouriteCheck(musicListPA[songPosition].id)
        val songName = binding.songName
        val artist = binding.Artist
        val cover = binding.albumArt
        songName.text = musicListPA[songPosition].title
        songName.ellipsize = TextUtils.TruncateAt.MARQUEE
        songName.isSingleLine = true
        songName.isSelected = true
        artist.text = musicListPA[songPosition].artist
        Glide.with(this).load(musicListPA[songPosition].albumCover).centerCrop()
            .apply(
                RequestOptions()
                    .placeholder(R.drawable.headphoneguy_made_by_vedant_r_j_chourey)
            ).into(cover)
        if (repeat) {
            binding.repeat.setBackgroundResource(R.drawable.btn_shape)
            binding.repeat.setImageResource(R.drawable.skipbackward)
        }
        if (isFavourite) {
            binding.liked.setImageResource(R.drawable.liked)
            binding.liked.setBackgroundResource(R.drawable.btn_shape4)
        } else {
            binding.liked.setImageResource(R.drawable.like)
            binding.liked.setBackgroundResource(R.drawable.btn_shape)
        }
    }


    private fun play(){
        binding.play.setImageResource(R.drawable.pause)
        musicService!!.showNotification(R.drawable.baseline_pause_circle_outline_24,"pause")
        isPlaying = true
        musicService!!.mediaPlayer!!.start()
    }

    private fun pause(){
        binding.play.setImageResource(R.drawable.play)
        musicService!!.showNotification(R.drawable.baseline_play_circle_outline_24,"play")
        isPlaying = false
        musicService!!.mediaPlayer!!.pause()
    }


//    private fun preOrFor(increment: Boolean){
//        if(increment){
//            songPst(true)
//            layout()
//        }else{
//            songPst(false)
//            layout()
//        }
//   }



    private fun forWard(){
            if (isPlaying) {
                musicService!!.mediaPlayer!!.pause()
                musicService!!.mediaPlayer!!.reset()
                if (songPosition != musicListPA.size - 1) {
                    songPosition++
                } else {
                    songPosition = 0
                }
                musicService!!.mediaPlayer!!.setDataSource(musicListPA[songPosition].path)
                musicService!!.mediaPlayer!!.prepare()
                musicService!!.mediaPlayer!!.start()
                val playAndPause = binding.play
                playAndPause.setImageResource(R.drawable.pause)
                musicService!!.showNotification(R.drawable.baseline_pause_circle_outline_24, "pause")
                binding.recover.text =
                    formatDuration(musicService!!.mediaPlayer!!.currentPosition.toLong())
                binding.covered.text =
                    formatDuration(musicService!!.mediaPlayer!!.duration.toLong())
                binding.seeking.progress = 0
                binding.seeking.max = musicService!!.mediaPlayer!!.duration
                musicService!!.mediaPlayer!!.setOnCompletionListener(this)
                layout()
            } else {
                musicService!!.mediaPlayer!!.pause()
                musicService!!.mediaPlayer!!.reset()
                if (songPosition != musicListPA.size - 1) {
                    ++songPosition
                } else {
                    songPosition = 0
                }
                musicService!!.mediaPlayer!!.setDataSource(musicListPA[songPosition].path)
                musicService!!.mediaPlayer!!.prepare()
                musicService!!.mediaPlayer!!.start()
                val playAndPause = binding.play
                playAndPause.setImageResource(R.drawable.pause)
                musicService!!.showNotification(R.drawable.baseline_pause_circle_outline_24, "pause")
                binding.recover.text =
                    formatDuration(musicService!!.mediaPlayer!!.currentPosition.toLong())
                binding.covered.text =
                    formatDuration(musicService!!.mediaPlayer!!.duration.toLong())
                binding.seeking.progress = 0
                binding.seeking.max = musicService!!.mediaPlayer!!.duration
                musicService!!.mediaPlayer!!.setOnCompletionListener(this)
                layout()
            }
    }


    private fun backWard(){

            if (isPlaying) {
                musicService!!.mediaPlayer!!.pause()
                musicService!!.mediaPlayer!!.reset()
                if (songPosition != 0) {
                    --songPosition
                } else {
                    songPosition = musicListPA.size - 1
                }
                musicService!!.mediaPlayer!!.setDataSource(musicListPA[songPosition].path)
                musicService!!.mediaPlayer!!.prepare()
                musicService!!.mediaPlayer!!.start()
                val playAndPause = binding.play
                playAndPause.setImageResource(R.drawable.pause)
                musicService!!.showNotification(R.drawable.baseline_pause_circle_outline_24, "pause")
                binding.recover.text =
                    formatDuration(musicService!!.mediaPlayer!!.currentPosition.toLong())
                binding.covered.text =
                    formatDuration(musicService!!.mediaPlayer!!.duration.toLong())
                binding.seeking.progress = 0
                binding.seeking.max = musicService!!.mediaPlayer!!.duration
                musicService!!.mediaPlayer!!.setOnCompletionListener(this)
                layout()
            } else {
                musicService!!.mediaPlayer!!.pause()
                musicService!!.mediaPlayer!!.reset()
                if (songPosition != 0) {
                    --songPosition
                } else {
                    songPosition = musicListPA.size - 1
                }
                musicService!!.mediaPlayer!!.setDataSource(musicListPA[songPosition].path)
                musicService!!.mediaPlayer!!.prepare()
                musicService!!.mediaPlayer!!.start()
                val playAndPause = binding.play
                playAndPause.setImageResource(R.drawable.pause)
                musicService!!.showNotification(R.drawable.baseline_pause_circle_outline_24, "pause")
                binding.recover.text =
                    formatDuration(musicService!!.mediaPlayer!!.currentPosition.toLong())
                binding.covered.text =
                    formatDuration(musicService!!.mediaPlayer!!.duration.toLong())
                binding.seeking.progress = 0
                binding.seeking.max = musicService!!.mediaPlayer!!.duration
                musicService!!.mediaPlayer!!.setOnCompletionListener(this)
                layout()
            }
    }


    override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
        val binder = service as MusicService.MyBinder
        musicService = binder.currentService()
        musicService!!.seekBarSetup()
        createMedia()
    }

    override fun onServiceDisconnected(name: ComponentName?) {
        musicService = null
    }


    override fun onCompletion(mp: MediaPlayer?) {

        try {
                musicService!!.mediaPlayer!!.pause()
                musicService!!.mediaPlayer!!.reset()
                if (!repeat) {
                    if (songPosition != musicListPA.size - 1) {
                        ++songPosition
                    } else {
                        songPosition = 0
                    }
                }
                musicService!!.mediaPlayer!!.setDataSource(musicListPA[songPosition].path)
                musicService!!.mediaPlayer!!.prepare()
                musicService!!.mediaPlayer!!.start()
                val playAndPause = binding.play
                playAndPause.setImageResource(R.drawable.pause)
                musicService!!.showNotification(R.drawable.baseline_pause_circle_outline_24, "pause")
                binding.recover.text = formatDuration(musicService!!.mediaPlayer!!.currentPosition.toLong())
                binding.covered.text = formatDuration(musicService!!.mediaPlayer!!.duration.toLong())
                binding.seeking.progress = 0
                binding.seeking.max = musicService!!.mediaPlayer!!.duration
                layout()
        }catch (e:Exception){return}
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 10 || resultCode == RESULT_OK){
            binding.equalizer.setBackgroundResource(R.drawable.btn_shape4)
            return
        }
    }


    private fun createMedia(){
        try {
                if (musicService!!.mediaPlayer == null) {
                    musicService!!.mediaPlayer = MediaPlayer()
                    musicService!!.mediaPlayer!!.reset()
                    musicService!!.mediaPlayer!!.setDataSource(musicListPA[songPosition].path)
                    musicService!!.mediaPlayer!!.prepare()
                    musicService!!.mediaPlayer!!.start()
                    isPlaying = true
                    val playAndPause = binding.play
                    playAndPause.setImageResource(R.drawable.pause)
                    musicService!!.showNotification(R.drawable.baseline_pause_circle_outline_24, "pause")
                    binding.recover.text =
                        formatDuration(musicService!!.mediaPlayer!!.currentPosition.toLong())
                    binding.covered.text =
                        formatDuration(musicService!!.mediaPlayer!!.duration.toLong())
                    binding.seeking.progress = 0
                    binding.seeking.max = musicService!!.mediaPlayer!!.duration
                    musicService!!.mediaPlayer!!.setOnCompletionListener(this)
                }else{
                    musicService!!.mediaPlayer!!.reset()
                    musicService!!.mediaPlayer!!.setDataSource(musicListPA[songPosition].path)
                    musicService!!.mediaPlayer!!.prepare()
                    musicService!!.mediaPlayer!!.start()
                    val playAndPause = binding.play
                    playAndPause.setImageResource(R.drawable.pause)
                    musicService!!.showNotification(R.drawable.baseline_pause_circle_outline_24, "pause")
                    binding.recover.text = formatDuration(musicService!!.mediaPlayer!!.currentPosition.toLong())
                    binding.covered.text = formatDuration(musicService!!.mediaPlayer!!.duration.toLong())
                    binding.seeking.progress = 0
                    binding.seeking.max = musicService!!.mediaPlayer!!.duration
                    musicService!!.mediaPlayer!!.setOnCompletionListener(this)
                }
        }catch( e: Exception){ return }
    }


}