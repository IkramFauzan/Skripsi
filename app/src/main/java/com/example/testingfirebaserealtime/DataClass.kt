package com.example.testingfirebaserealtime

import android.os.Parcelable
import com.google.firebase.database.IgnoreExtraProperties
import kotlinx.parcelize.Parcelize
import java.io.Serializable

data class DataClass(
    var placeAddress: String = "",
    var placeName: String = "",
    var placePhotoUrl: String = "",
    var description: String = "",
    var lat: Double = 0.0,
    var lon: Double = 0.0,
    var operational: String = "",
    var fasilitas: String = "",
    var category: String = "",
    var placeVideoUrl: String = "",
    var id: Int = 0,
    var totalRating: Int = 0
) : Serializable {
    // Default no-arg constructor
    constructor() : this("", "", "", "", 0.0, 0.0, "", "", "", "",0)
}

data class User(
    val username: String? = "",
    val email: String? = "",
    val password: String? = "",
    val role: String = ""
)

@IgnoreExtraProperties
data class UlasanCoba(
    val uid: String? ,
    val name: String? = null,
    val email: String? ,
    val profile: String? = null,
    val komentar: String? = null,
    val rating: Float? = null,
    val day: String? = null,
    val timestamp: Long? = null
)

data class Visit(val latitude: Double, val longitude: Double, val locationName: String, val timestamp: String)

data class Favorite(
    val placeName: String = "",
    val placeAddress: String = "",
    val placePhotoUrl: String = "",
    val username: String = ""
)

data class Report(
    val reason: String = "",
    val placeName: String = "",
    val timestamp: String = ""
)

@Parcelize
data class EventAd(
    val imageUrl: String = "",
    val eventName: String = "",
    val eventSchedule: String = "",
    val eventDescription: String = "",
    val eventAddress: String = ""
) : Parcelable

data class Article(
    val id: String? = null,
    val title: String,
    val address: String,
    val content: String,
    val date: String,
    val imageUrl: String,
    val createdDate: String
)

data class Destination(
    var placeName: String = "",
    var placeAddress: String? = null,
    var placePhotoUrl: String? = null,
    var description: String? = null,
    var lat: Double? = null,
    var lon: Double? = null,
    var operational: String? = null,
    var fasilitas: String? = null,
    var category: String? = null,
    var placeVideoUrl: String? = null,
    var id: String? = null
)









