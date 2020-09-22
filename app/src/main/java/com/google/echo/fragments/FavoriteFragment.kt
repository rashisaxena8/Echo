package com.google.echo.fragments
import android.app.Activity
import android.content.Context
import android.media.MediaPlayer
import android.os.Bundle
import android.provider.MediaStore
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.RelativeLayout
import android.widget.TextView
import com.google.echo.R
import com.google.echo.Songs
import com.google.echo.adapters.FavoriteAdapter
import com.google.echo.databases.EchoDatabase
import kotlinx.android.synthetic.main.fragment_song_playing.*


/**
 * A simple [Fragment] subclass.
 *
 */
class FavoriteFragment : Fragment() {

    var myActivit: Activity? = null

    var noFavorites: TextView? = null
    var nowPlayingBotomBar: RelativeLayout? = null
    var playPauseButton: ImageButton? = null
    var songTitle: TextView? = null
    var recyclerView: RecyclerView? = null
    var trackPosition: Int = 0
    var favoriteContent: EchoDatabase? = null
    var refreshList: ArrayList<Songs>? = null
    var getListFromDatabase: ArrayList<Songs>? = null
    object Statified{
        var mediaPlayer: MediaPlayer? = null

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view =  inflater!!.inflate(R.layout.fragment_favorite, container, false)
        activity?.title = "Favorite"
        noFavorites =view?.findViewById(R.id.noFavorites)
        nowPlayingBotomBar = view?.findViewById(R.id.hiddenBarFavScreen)
        songTitle =view?.findViewById(R.id.songTitleFavScreen)
        playPauseButton= view?.findViewById(R.id.playPauseButton)
        recyclerView = view?.findViewById(R.id.favoriteRecycler)
        return  view
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        myActivit = context as Activity
    }

    override fun onAttach(activity: Activity?) {
        super.onAttach(activity)
        myActivit = activity
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        favoriteContent = EchoDatabase(myActivit)
        dispaly_favorites_by_searching()
        bottomBarSetup()
        getListFromDatabase = getSongsFromPhone()
        if(getListFromDatabase == null){
            recyclerView?.visibility=View.INVISIBLE
            noFavorites?.visibility=View.VISIBLE
        }else{
            var favoriteAdapter = FavoriteAdapter(getListFromDatabase as ArrayList<Songs>, myActivit as Context)
            val mLayoutManager = LinearLayoutManager(activity)
            recyclerView?.layoutManager = mLayoutManager
            recyclerView?.itemAnimator = DefaultItemAnimator()
            recyclerView?.adapter = favoriteAdapter
            recyclerView?.setHasFixedSize(true)
        }

    }

    override fun onResume() {
        super.onResume()
    }

    override fun onPrepareOptionsMenu(menu: Menu?) {
        super.onPrepareOptionsMenu(menu)

    }

    fun getSongsFromPhone():ArrayList<Songs>{

        var arrayList= ArrayList<Songs>()
        var contentResolver= myActivit?.contentResolver
        var songUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        var songCursor = contentResolver?.query(songUri, null, null, null, null )
        if(songCursor != null && songCursor.moveToFirst()){

            val songId = songCursor.getColumnIndex(MediaStore.Audio.Media._ID)
            val songTitle = songCursor.getColumnIndex(MediaStore.Audio.Media.TITLE)
            val songArtist = songCursor.getColumnIndex(MediaStore.Audio.Media.ARTIST)
            val songData = songCursor.getColumnIndex(MediaStore.Audio.Media.DATA)
            val dateIndex = songCursor.getColumnIndex(MediaStore.Audio.Media.DATE_ADDED)
            while(songCursor.moveToNext()){
                var currentId = songCursor.getLong(songId)
                var currentTitle = songCursor.getString(songTitle)
                var currentArtist = songCursor.getString(songArtist)
                var currentData = songCursor.getString(songData)
                var currentdate = songCursor.getLong(dateIndex)
                arrayList.add(Songs(currentId, currentTitle, currentArtist, currentData, currentdate))

            }

        }
        return arrayList
    }

    fun bottomBarSetup(){

        try{
            bottomBarClickhandler()
            songTitle?.setText(SongPlayingFragment.Statified.currentSongHelper?.songTitle)
            SongPlayingFragment.Statified.mediaPlayer?.setOnCompletionListener ({
                songTitle?.setText(SongPlayingFragment.Statified.currentSongHelper?.songTitle)
                SongPlayingFragment.Staticated.onSongComlete()
            })
            if(SongPlayingFragment.Statified.mediaPlayer?.isPlaying as Boolean){
                nowPlayingBotomBar?.visibility = View.VISIBLE

            }else{
                nowPlayingBotomBar?.visibility = View.INVISIBLE
            }

        }catch (e:Exception){
            e.printStackTrace()

        }

    }

    fun bottomBarClickhandler(){

        nowPlayingBotomBar?.setOnClickListener({
            Statified.mediaPlayer = SongPlayingFragment.Statified.mediaPlayer
            val songPlayingFragment= SongPlayingFragment()
            val args = Bundle()
            args.putString("songArtist", SongPlayingFragment.Statified.currentSongHelper?.songArtist)
            args.putString("path", SongPlayingFragment.Statified.currentSongHelper?.songPath)
            args.putString("songTitle", SongPlayingFragment.Statified.currentSongHelper?.songTitle)
            args.putInt("SongId", SongPlayingFragment.Statified.currentSongHelper?.songId?.toInt() as Int)
            args.putInt("songPosition", SongPlayingFragment.Statified.currentSongHelper?.currentPosition?.toInt() as Int )
            args.putParcelableArrayList("songData", SongPlayingFragment.Statified.fetchSongs)
            args.putString("FavBottombar", "success")
            songPlayingFragment.arguments = args
            fragmentManager?.beginTransaction()
                ?.replace(R.id.details_fragment, songPlayingFragment)
                ?.addToBackStack("SongPlayingFragment")
                ?.commit()
        })

        playPauseButton?.setOnClickListener({

            if (SongPlayingFragment.Statified.mediaPlayer?.isPlaying as Boolean){

                SongPlayingFragment.Statified.mediaPlayer?.pause()
                trackPosition = SongPlayingFragment.Statified.mediaPlayer?.getCurrentPosition() as Int
                playPauseButton?.setBackgroundResource(R.drawable.play_icon)
            }else{
                SongPlayingFragment.Statified.mediaPlayer?.seekTo(trackPosition)
                SongPlayingFragment.Statified.mediaPlayer?.start()
                playPauseButton?.setBackgroundResource(R.drawable.pause_icon)
            }
        })

    }

    fun dispaly_favorites_by_searching() {
        if (favoriteContent?.checkSize() as Int > 0) {
            noFavorites?.visibility = View.INVISIBLE
            refreshList = ArrayList<Songs>()

            getListFromDatabase = (favoriteContent as EchoDatabase)?.queryDBList()
            var fetchListFromDevice = getSongsFromPhone()
            if(fetchListFromDevice != null){
                for(i in 0..fetchListFromDevice?.size - 1){
                    for(j in  0..getListFromDatabase?.size as Int - 1)
                        if((getListFromDatabase?.get(j)?.songID)=== (fetchListFromDevice?.get(i)?.songID)){
                            (refreshList as ArrayList<Songs>)?.add((getListFromDatabase as ArrayList<Songs>)[j])
                        }
                }
            }else {

            }
            if(refreshList==null){
                recyclerView?.visibility = View.INVISIBLE
                noFavorites?.visibility = View.VISIBLE
            }else{
                val favoriteAdapter = FavoriteAdapter(refreshList as ArrayList<Songs>, myActivit as Context)
                val mLayoutManager = LinearLayoutManager(activity)
                recyclerView?.layoutManager = mLayoutManager
                recyclerView?.itemAnimator = DefaultItemAnimator()
                recyclerView?.adapter= favoriteAdapter
                recyclerView?.setHasFixedSize(true)
            }

        }else{
            recyclerView?.visibility = View.INVISIBLE
            noFavorites?.visibility = View.VISIBLE
        }

    }


}
