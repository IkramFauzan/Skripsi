package com.example.testingfirebaserealtime.admin.manage

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.testingfirebaserealtime.DataClass
import com.example.testingfirebaserealtime.databinding.ActivityManagePrayerBinding
import com.example.testingfirebaserealtime.eksperimen.ListWisataAdapter
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase

class ManagePrayer : AppCompatActivity(), ListWisataAdapter.OnDeleteClickListener {

    private lateinit var binding: ActivityManagePrayerBinding
    private lateinit var database: FirebaseDatabase
    private lateinit var myRef: DatabaseReference
    private lateinit var adapter: ListWisataAdapter
    private val tempatList = mutableListOf<DataClass>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityManagePrayerBinding.inflate(layoutInflater)

        database = FirebaseDatabase.getInstance()
        myRef = FirebaseDatabase.getInstance().reference
        val prayerRef = myRef.child("tempat").child("prayer")

        adapter = ListWisataAdapter( this, tempatList)
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter

        prayerRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val list = mutableListOf<DataClass>()
                if (dataSnapshot.exists()) {
                    for (snapshot in dataSnapshot.children) {
                        val tempatWisata = snapshot.getValue(DataClass::class.java)
                        tempatWisata?.let { list.add(it) }
                    }
                    adapter.setItemList(list)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                print("Kenapa salah woi")
                Log.w("prayerActivity", "Failed to read value.", error.toException())
            }
        })

        setContentView(binding.root)
    }

    override fun onDeleteClicked(tempat: DataClass) {
        deletePlaceByName(tempat.placeName)
    }

    private fun deletePlaceByName(placeName: String) {
        val ref = FirebaseDatabase.getInstance().reference
        val auth = Firebase.auth
        val prayerDatabase = ref.child("tempat").child("prayer")
        val allPlaceDatabase = ref.child("allDestination")
        val ratingDatabase = ref.child("rating")
        val usersFavoritesDatabase = ref.child("favorites")

        Log.d("DeletePlace", "Place Name: $placeName")

        Toast.makeText(this, "Starting deletion for $placeName", Toast.LENGTH_SHORT).show()

        // Function to delete place by name from a given database reference
        fun deletePlaceFromDatabase(database: DatabaseReference, placeNameKey: String = "placeName", onComplete: (Boolean) -> Unit) {
            database.get().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    var placeIdToDelete: String? = null
                    for (snapshot in task.result.children) {
                        val currentPlaceName = snapshot.child(placeNameKey).getValue(String::class.java)
                        Log.d("DeletePlace", "Checking place: $currentPlaceName in ${database.key}")
                        if (currentPlaceName == placeName) {
                            placeIdToDelete = snapshot.key
                            Log.d("DeletePlace", "Found place to delete with ID: $placeIdToDelete in ${database.key}")
                            break
                        }
                    }

                    if (placeIdToDelete != null) {
                        database.child(placeIdToDelete).removeValue().addOnCompleteListener { deleteTask ->
                            onComplete(deleteTask.isSuccessful)
                            if (deleteTask.isSuccessful) {
                                Log.d("DeletePlace", "Place deleted successfully from ${database.key}")
                            } else {
                                Log.e("DeletePlace", "Failed to delete place from ${database.key}")
                            }
                        }.addOnFailureListener { exception ->
                            Log.e("DeletePlace", "Error: ${exception.message}")
                            Toast.makeText(this, "Error: ${exception.message}", Toast.LENGTH_SHORT).show()
                            onComplete(false)
                        }
                    } else {
                        Log.w("DeletePlace", "Place not found in ${database.key}")
                        Toast.makeText(this, "Tempat wisata tidak ditemukan", Toast.LENGTH_SHORT).show()
                        onComplete(false)
                    }
                } else {
                    Log.e("DeletePlace", "Failed to get data from ${database.key}")
                    Toast.makeText(this, "Gagal mengambil data", Toast.LENGTH_SHORT).show()
                    onComplete(false)
                }
            }.addOnFailureListener { exception ->
                Log.e("DeletePlace", "Error: ${exception.message}")
                Toast.makeText(this, "Error: ${exception.message}", Toast.LENGTH_SHORT).show()
                onComplete(false)
            }
        }

        // Function to delete place from all users' favorites
        fun deletePlaceFromFavorites(placeName: String, onComplete: (Boolean) -> Unit) {
            usersFavoritesDatabase.get().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val updates = mutableMapOf<String, Any?>()
                    for (userSnapshot in task.result.children) {
                        for (placeSnapshot in userSnapshot.children) {
                            val currentPlaceName = placeSnapshot.child("placeName").getValue(String::class.java)
                            if (currentPlaceName == placeName) {
                                updates["favorites/${userSnapshot.key}/${placeSnapshot.key}"] = null
                            }
                        }
                    }
                    if (updates.isNotEmpty()) {
                        ref.updateChildren(updates).addOnCompleteListener { updateTask ->
                            onComplete(updateTask.isSuccessful)
                            if (updateTask.isSuccessful) {
                                Log.d("DeletePlace", "Place deleted from all users' favorites successfully")
                            } else {
                                Log.e("DeletePlace", "Failed to delete place from all users' favorites")
                            }
                        }.addOnFailureListener { exception ->
                            Log.e("DeletePlace", "Error: ${exception.message}")
                            Toast.makeText(this, "Error: ${exception.message}", Toast.LENGTH_SHORT).show()
                            onComplete(false)
                        }
                    } else {
                        Log.w("DeletePlace", "Place not found in any user's favorites")
                        onComplete(true)
                    }
                } else {
                    Log.e("DeletePlace", "Failed to get data from users' favorites")
                    Toast.makeText(this, "Gagal mengambil data dari favorites pengguna", Toast.LENGTH_SHORT).show()
                    onComplete(false)
                }
            }.addOnFailureListener { exception ->
                Log.e("DeletePlace", "Error: ${exception.message}")
                Toast.makeText(this, "Error: ${exception.message}", Toast.LENGTH_SHORT).show()
                onComplete(false)
            }
        }

        // Function to delete rating by place name
        fun deleteRatingByPlaceName(ratingDatabase: DatabaseReference, onComplete: (Boolean) -> Unit) {
            ratingDatabase.get().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    var ratingIdToDelete: String? = null
                    var parentKeyToDelete: String? = null
                    for (parentSnapshot in task.result.children) {
                        for (snapshot in parentSnapshot.children) {
                            val currentPlaceName = parentSnapshot.key
                            Log.d("DeletePlace", "Checking rating place: $currentPlaceName")
                            if (currentPlaceName == placeName) {
                                ratingIdToDelete = snapshot.key
                                parentKeyToDelete = parentSnapshot.key
                                Log.d("DeletePlace", "Found rating to delete with ID: $ratingIdToDelete in $currentPlaceName")
                                break
                            }
                        }
                        if (ratingIdToDelete != null) {
                            break
                        }
                    }

                    if (ratingIdToDelete != null && parentKeyToDelete != null) {
                        ratingDatabase.child(parentKeyToDelete).child(ratingIdToDelete).removeValue().addOnCompleteListener { deleteTask ->
                            onComplete(deleteTask.isSuccessful)
                            if (deleteTask.isSuccessful) {
                                Log.d("DeletePlace", "Rating deleted successfully from ${ratingDatabase.key}")
                            } else {
                                Log.e("DeletePlace", "Failed to delete rating from ${ratingDatabase.key}")
                            }
                        }.addOnFailureListener { exception ->
                            Log.e("DeletePlace", "Error: ${exception.message}")
                            Toast.makeText(this, "Error: ${exception.message}", Toast.LENGTH_SHORT).show()
                            onComplete(false)
                        }
                    } else {
                        Log.w("DeletePlace", "Rating place not found")
                        Toast.makeText(this, "Tempat wisata tidak ditemukan dalam rating", Toast.LENGTH_SHORT).show()
                        onComplete(false)
                    }
                } else {
                    Log.e("DeletePlace", "Failed to get data from ${ratingDatabase.key}")
                    Toast.makeText(this, "Gagal mengambil data", Toast.LENGTH_SHORT).show()
                    onComplete(false)
                }
            }.addOnFailureListener { exception ->
                Log.e("DeletePlace", "Error: ${exception.message}")
                Toast.makeText(this, "Error: ${exception.message}", Toast.LENGTH_SHORT).show()
                onComplete(false)
            }
        }

        // Delete from prayer
        deletePlaceFromDatabase(prayerDatabase) { prayerSuccess ->
            if (prayerSuccess) {
                Toast.makeText(this, "Deleted from prayer", Toast.LENGTH_SHORT).show()
                // Delete from allPlace
                deletePlaceFromDatabase(allPlaceDatabase) { allPlaceSuccess ->
                    if (allPlaceSuccess) {
                        Toast.makeText(this, "Deleted from allPlace", Toast.LENGTH_SHORT).show()
                        // Delete from rating
                        deleteRatingByPlaceName(ratingDatabase) { ratingPlaceSuccess ->
                            if (ratingPlaceSuccess) {
                                Toast.makeText(this, "Deleted from rating", Toast.LENGTH_SHORT).show()
                                // Delete from all users' favorites
                                deletePlaceFromFavorites(placeName) { favoritesSuccess ->
                                    if (favoritesSuccess) {
                                        Toast.makeText(this, "Data wisata berhasil dihapus dari semua pengguna", Toast.LENGTH_SHORT).show()
                                        finish()
                                    } else {
                                        Log.e("DeletePlace", "Failed to delete place from all users' favorites")
                                        Toast.makeText(this, "Gagal menghapus data wisata dari semua pengguna", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            } else {
                                Log.e("DeletePlace", "Failed to delete place from rating")
                                Toast.makeText(this, "Gagal menghapus data rating", Toast.LENGTH_SHORT).show()
                            }
                        }
                    } else {
                        Log.e("DeletePlace", "Failed to delete place from allPlace")
                        Toast.makeText(this, "Gagal menghapus data wisata dari semua tempat", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Log.e("DeletePlace", "Failed to delete place from prayer")
                Toast.makeText(this, "Gagal menghapus data wisata dari prayer", Toast.LENGTH_SHORT).show()
            }
        }
    }
}