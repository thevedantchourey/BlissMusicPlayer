package com.app.bliss

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.app.bliss.databinding.ListStyle2Binding
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions

class MusicPlayerAdapter(audios: ArrayList<SongData>, private val  context: Context) : RecyclerView.Adapter<PlayerViewHolder>() {

    private var audio: ArrayList<SongData> = ArrayList()
    init {
        audio = audios
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlayerViewHolder {
        val view = ListStyle2Binding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PlayerViewHolder(view)
    }

    override fun onBindViewHolder(holder: PlayerViewHolder, position: Int) {
        val audioModel : SongData = audio[position]
        holder.title.text = audioModel.title
        holder.duration.text = formatDuration(audio[position].duration)
        Glide.with(context).load(audioModel.albumCover)
            .apply(RequestOptions().placeholder(R.drawable.bliss_icon).centerCrop()).into(holder.cover)
        holder.root.setOnClickListener{
            val intent = Intent(context,PlayerActivity::class.java)
            intent.putExtra("index",position)
            intent.putExtra("class","MusicPlayerAdapter")
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return audio.size
    }


}
class PlayerViewHolder(binding: ListStyle2Binding): RecyclerView.ViewHolder(binding.root) {
    val title = binding.songName
    val cover = binding.albumCover
    val duration = binding.duration
    val root = binding.root
}

