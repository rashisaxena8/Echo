package com.google.echo

import android.os.Parcel
import android.os.Parcelable

class Songs(var songID: Long,var songTitle: String, var artist: String, var songData: String, var dateAdded:Long) : Parcelable{
    constructor(parcel: Parcel) : this(
        parcel.readLong(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readLong()
    ) {
    }

    override fun writeToParcel(p0: Parcel?, p1: Int) {
        p0!!.writeLong(songID)
        p0!!.writeString(songTitle)
        p0!!.writeString(artist)
        p0!!.writeString(songData)
        p0!!.writeLong(dateAdded)
    }

    override fun describeContents(): Int {
        return 0
    }
object Statified{
    var nameComparator: Comparator<Songs> = Comparator<Songs>{songs1, songs2 ->
        val songOne = songs1.songTitle.toUpperCase()
        val songTwo = songs2.songTitle.toUpperCase()
        songOne.compareTo(songTwo)
    }
    var dateComparator: Comparator<Songs> = Comparator<Songs>{songs1, songs2 ->
        val songOne = songs1.dateAdded.toDouble()
        val songTwo = songs2.dateAdded.toDouble()
        songTwo.compareTo(songOne)
    }
}

    companion object CREATOR : Parcelable.Creator<Songs> {
        override fun createFromParcel(parcel: Parcel): Songs {
            return Songs(parcel)
        }

        override fun newArray(size: Int): Array<Songs?> {
            return arrayOfNulls(size)
        }
    }

}