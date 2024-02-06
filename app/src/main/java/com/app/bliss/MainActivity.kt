package com.app.bliss


import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.app.bliss.databinding.ActivityMainBinding
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import java.io.File
import kotlin.system.exitProcess



class MainActivity : AppCompatActivity() {
    private val binding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }
    private var mInterstitialAd: InterstitialAd? = null
    private var tAG = "MainActivity"
    private var ad = 0
    private lateinit var adapter: MusicPlayerAdapter
    private var reqCode = 10

    companion object{
        var audioList : ArrayList<SongData> = arrayListOf()
        var favList : ArrayList<SongData> = arrayListOf()
        var isClicked: Boolean = false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        if(requestRuntimePermission()) {

            MobileAds.initialize(this) {
                loadAd()
            }
            initialized()

            favList = ArrayList()
            val liked = getSharedPreferences("LIKED", MODE_PRIVATE)
            val jsonString = liked.getString("LikedSongs", null)
            val typeToken = object : TypeToken<ArrayList<SongData>>(){}.type
            if(jsonString != null){
                val data : ArrayList<SongData> = GsonBuilder().create().fromJson(jsonString, typeToken)
                favList.addAll(data)
            }


            try{
                binding.fav.setOnClickListener {
                    if (isClicked) {
                        isClicked = false
                        initialized()
                    } else {
                        isClicked = true
                        favList = checkPlayList(favList)
                        val rv = binding.songView
                        rv.layoutManager = GridLayoutManager(this, 2, LinearLayoutManager.VERTICAL, false)
                        val adapter2 = MusicPlayerAdapter(favList, this)
                        adapter2.notifyItemChanged(favList.size)
                        adapter2.notifyItemRangeChanged(PlayerActivity.fIndex, favList.size)
                        rv.adapter = adapter2
                        if (favList.size == 0) {
                            binding.msg.isVisible = true
                            binding.head.isVisible = false
                        }
                        if (favList.size > 1) {
                            binding.shuffle.visibility = View.VISIBLE
                            binding.removeAll.visibility = View.VISIBLE
                            binding.shuffle.setOnClickListener {
                                val intent2 = Intent(this, PlayerActivity::class.java)
                                intent2.putExtra("index", 0)
                                intent2.putExtra("class", "shuffle")
                                startActivity(intent)
                            }
                            binding.removeAll.setOnClickListener {
                                val builder = AlertDialog.Builder(this)

                                builder.setTitle("Warning")
                                builder.setMessage("All liked songs will be removed on next start!")
                                builder.setIcon(android.R.drawable.ic_dialog_alert)

                                builder.setPositiveButton("OK") { _, _ ->
                                    favList.removeAll(favList.toSet())
                                }

                                builder.setNegativeButton("Cancel") { dialog, _ ->
                                    dialog.dismiss()
                                }

                                val dialog = builder.create()
                                dialog.show()
                            }

                        }
                        ad++
                        if (ad > 3) {
                            adsInitialization()
                            ad = 1
                        }
                    }
                }
            }catch (e:Exception){ return }
        }
    }


    private fun initialized(){
        val rv = binding.songView
        audioList = getAllAudio()
        audioList = checkPlayList(audioList)
        rv.layoutManager = GridLayoutManager(this, 2, LinearLayoutManager.VERTICAL, false)
        adapter = MusicPlayerAdapter(audioList, this)
        rv.adapter = adapter
        adapter.notifyDataSetChanged()
        adapter.notifyItemChanged(audioList.size)
        binding.shuffle.visibility = View.INVISIBLE
        binding.removeAll.visibility = View.INVISIBLE
        binding.msg.isVisible = false
        binding.head.isVisible = true

    }

    private fun getAllAudio():ArrayList<SongData>{

        val tempList = ArrayList<SongData>()

        val mediaTitle =  MediaStore.Audio.Media.TITLE
        val mediaArtist =  MediaStore.Audio.Media.ARTIST
        val mediaData =  MediaStore.Audio.Media.DATA
        val mediaID =  MediaStore.Audio.Media._ID
        val mediaDuration = MediaStore.Audio.Media.DURATION
        val mediaCover = MediaStore.Audio.Media.ALBUM_ID

        val audioUri= MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(
            mediaTitle,
            mediaArtist,
            mediaData,
            mediaDuration,
            mediaCover,
            mediaID
        )

        val cursor = contentResolver.query(audioUri,projection,null,null,null)
        if(cursor!=null){
            if (cursor.moveToFirst()){
                do {
                    val titleC = cursor.getString(cursor.getColumnIndexOrThrow(mediaTitle))
                    val artistC = cursor.getString(cursor.getColumnIndexOrThrow(mediaArtist))
                    val pathC = cursor.getString(cursor.getColumnIndexOrThrow(mediaData))
                    val durationC = cursor.getLong(cursor.getColumnIndexOrThrow(mediaDuration))
                    val coverC = cursor.getLong(cursor.getColumnIndexOrThrow(mediaCover)).toString()
                    val idC = cursor.getString(cursor.getColumnIndexOrThrow(mediaID))
                    val uri = Uri.parse("content://media/external/audio/albumart")
                    val artUri = Uri.withAppendedPath(uri, coverC).toString()
                    val audioModel = SongData(titleC,artistC,artUri,pathC,durationC,idC)
                    val file = audioModel.path?.let { File(it) }
                    if(file!!.exists()){
                        tempList.add(audioModel)
                    }
                    audioList.add(audioModel)
                }while (cursor.moveToNext())
                cursor.close()
            }

        }

        return tempList
    }



    fun loadAd(){
        val adRequest = AdRequest.Builder().build()

        InterstitialAd.load(this, "ca-app-pub-2360133179842292/5881422571", adRequest, object : InterstitialAdLoadCallback() {
            override fun onAdFailedToLoad(adError: LoadAdError) {
                adError.toString().let { Log.d(tAG, it) }
                mInterstitialAd = null
                loadAd()
            }

            override fun onAdLoaded(interstitialAd: InterstitialAd) {
                Log.d(tAG, "Ad was loaded.")
                mInterstitialAd = interstitialAd
            }
        })
    }

    private fun adsInitialization(){
        if (mInterstitialAd != null) {
            mInterstitialAd?.show(this)

            mInterstitialAd?.fullScreenContentCallback = object: FullScreenContentCallback() {
                override fun onAdClicked() {
                    // Called when a click is recorded for an ad.
                    Log.d(tAG, "Ad was clicked.")
                }

                override fun onAdDismissedFullScreenContent() {
                    // Called when ad is dismissed.
                    Log.d(tAG, "Ad dismissed fullscreen content.")
                    mInterstitialAd = null
                    loadAd()
                }

                override fun onAdFailedToShowFullScreenContent(p0: AdError) {
                    // Called when ad fails to show.
                    Log.e(tAG, "Ad failed to show fullscreen content.")
                    mInterstitialAd = null
                    loadAd()
                }

                override fun onAdImpression() {
                    // Called when an impression is recorded for an ad.
                    Log.d(tAG, "Ad recorded an impression.")
                }

                override fun onAdShowedFullScreenContent() {
                    loadAd()
                    Log.d(tAG, "Ad showed fullscreen content.")
                }
            }
        } else {
            Log.d("TAG", "The interstitial ad wasn't ready yet.")
        }

    }



    @SuppressLint("InlinedApi")
    private fun requestRuntimePermission():Boolean{
        if(ActivityCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    android.Manifest.permission.READ_EXTERNAL_STORAGE)){
                ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE,android.Manifest.permission.READ_MEDIA_AUDIO,android.Manifest.permission.POST_NOTIFICATIONS), reqCode)
            } else{
                ActivityCompat.requestPermissions(this,  arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE,android.Manifest.permission.READ_MEDIA_AUDIO,android.Manifest.permission.POST_NOTIFICATIONS), reqCode)
            }
        }
    return true
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode == reqCode){
            if(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show()
                initialized()
            }
            else{
                requestRuntimePermission()
            }
        }
    }


    override fun onResume() {
        super.onResume()

        val liked = getSharedPreferences("LIKED", MODE_PRIVATE).edit()
        val jsonLiked = GsonBuilder().create().toJson(favList)
        liked.putString("LikedSongs", jsonLiked)
        liked.apply()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (!PlayerActivity.isPlaying or PlayerActivity.isPlaying  && PlayerActivity.musicService != null){
            PlayerActivity.musicService!!.stopSelf()
            PlayerActivity.musicService!!.mediaPlayer!!.release()
            PlayerActivity.musicService = null
            exitProcess(1)
        }
    }
}