package com.app.bliss

import android.annotation.SuppressLint
import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.text.TextUtils
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.app.bliss.databinding.FragmentNowPlayingBinding
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions


class NowPlaying : Fragment() {

    companion object {
        @SuppressLint("StaticFieldLeak")
        lateinit var binding: FragmentNowPlayingBinding
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_now_playing, container, false)
        binding = FragmentNowPlayingBinding.bind(view)
        binding.root.visibility = View.INVISIBLE

        return view
    }

    override fun onResume() {
        super.onResume()
            if (PlayerActivity.musicService != null) {
                binding.root.visibility = View.VISIBLE
                val songName2 = binding.songName2
                val artist = binding.artist
                val cover = binding.albumCover
                songName2.text = PlayerActivity.musicListPA[PlayerActivity.songPosition].title
                songName2.ellipsize = TextUtils.TruncateAt.MARQUEE
                songName2.isSingleLine = true
                songName2.isSelected = true
                artist.text = PlayerActivity.musicListPA[PlayerActivity.songPosition].artist
                binding.duration.text = formatDuration(PlayerActivity.musicListPA[PlayerActivity.songPosition].duration)
                Glide.with(this)
                    .load(PlayerActivity.musicListPA[PlayerActivity.songPosition].albumCover)
                    .apply(
                        RequestOptions()
                            .placeholder(R.drawable.headphoneguy_made_by_vedant_r_j_chourey)
                            .centerInside()
                    ).into(cover)
                binding.root.setOnClickListener {
                    val intent = Intent(requireContext(), PlayerActivity::class.java)
                    intent.putExtra("index", PlayerActivity.songPosition)
                    intent.putExtra("class", "NowPlaying")
                    requireContext().startActivity(intent)
                }
                if (PlayerActivity.isPlaying) {
                    binding.play.setImageResource(R.drawable.pause)
                } else {
                    binding.play.setImageResource(R.drawable.play)
                }
                binding.play.setOnClickListener {
                    if (PlayerActivity.isPlaying) { pause() } else { play() }
                }
                binding.backward.setOnClickListener { preNextSong(false) }
                binding.forward.setOnClickListener { preNextSong(true) }

            }
    }



    private fun play(){
        PlayerActivity.binding.play.setImageResource(R.drawable.pause)
        PlayerActivity.musicService!!.showNotification(R.drawable.pause,"pause")
        PlayerActivity.isPlaying = true
        PlayerActivity.musicService!!.mediaPlayer!!.start()
    }

    private fun pause(){
        PlayerActivity.binding.play.setImageResource(R.drawable.play)
        PlayerActivity.musicService!!.showNotification(R.drawable.play,"play")
        PlayerActivity.isPlaying = false
        PlayerActivity.musicService!!.mediaPlayer!!.pause()
    }


    private fun preNextSong(increment: Boolean) {

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

    }
}