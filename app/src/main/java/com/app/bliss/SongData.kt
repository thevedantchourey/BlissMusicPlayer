package com.app.bliss


import android.media.MediaMetadataRetriever
import java.io.File
import java.util.concurrent.TimeUnit

data class SongData (
    val title: String?,
    val artist: String?,
    val albumCover: String?,
    val path: String?,
    val duration: Long = 0,
    val id: String
)



fun formatDuration(duration:Long):String{
    val minutes = TimeUnit.MINUTES.convert(duration, TimeUnit.MILLISECONDS)
    val seconds = (TimeUnit.SECONDS.convert(duration,TimeUnit.MILLISECONDS) - minutes*TimeUnit.SECONDS.convert(1,TimeUnit.MINUTES))
    return String.format("%02d:%02d",minutes,seconds)
}


fun getImage(path: String): ByteArray? {
    val retriever = MediaMetadataRetriever()
    retriever.setDataSource(path)
    return retriever.embeddedPicture
}


fun songPst(increment: Boolean) {
    if(!PlayerActivity.repeat){
        if (increment) {
            if (PlayerActivity.musicListPA.size - 1 == PlayerActivity.songPosition) {
                PlayerActivity.songPosition = 0
            } else {
                ++PlayerActivity.songPosition
            }
        } else {
            if (0 == PlayerActivity.songPosition) {
                PlayerActivity.songPosition = PlayerActivity.musicListPA.size - 1
            } else {
                --PlayerActivity.songPosition
            }
        }
    }
}


fun favouriteCheck(id: String): Int{
    PlayerActivity.isFavourite = false
    MainActivity.favList.forEachIndexed{ index, songData ->
        if (id == songData.id){
            PlayerActivity.isFavourite = true
            return index
        }
    }
    return -1
}


fun checkPlayList(playlist: ArrayList<SongData>):ArrayList<SongData>{

    playlist.forEachIndexed{index, songData ->
        val file = File(songData.path!!)
        if (!file.exists()){
            playlist.removeAt(index)
        }
    }

    return playlist
}