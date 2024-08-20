package com.example.testingfirebaserealtime.detail

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.testingfirebaserealtime.KyuuAdapter
import com.example.testingfirebaserealtime.R
import com.example.testingfirebaserealtime.Report
import com.example.testingfirebaserealtime.UlasanCoba
import com.example.testingfirebaserealtime.auth.SignActivity
import com.example.testingfirebaserealtime.databinding.ActivityDetailKulinerBinding
import com.example.testingfirebaserealtime.eksperimen.MediaPlayerActivity
import com.example.testingfirebaserealtime.penginapan.activity.DetailHotelActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.*

class DetailKulinerActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var binding: ActivityDetailKulinerBinding
    private lateinit var mMap: GoogleMap
    private lateinit var ref: DatabaseReference
    private lateinit var kyuuAdapter: KyuuAdapter
    private lateinit var auth: FirebaseAuth

    private lateinit var db: FirebaseDatabase

    private var isFavorite = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityDetailKulinerBinding.inflate(layoutInflater)

        val address = intent.getStringExtra(ADDRESS)
        val place = intent.getStringExtra(PLACE)
        val description = intent.getStringExtra(DESC)
        val photo = intent.getStringExtra(PHOTO)
        val opera = intent.getStringExtra(OPERASIONAL)
        val fasilitas = intent.getStringExtra(FASILITAS)
        val mediaType = intent.getStringExtra(VIDEO)

        binding.apply {
            detailAlamatTextView.text = address
            detailDeskripsiTextView.text = description
            detailTempatTextView.text = place
            jamOperasional.text = opera
            listFasilitas.text = fasilitas
        }

        binding.progressBar.visibility = View.VISIBLE

        Handler(Looper.getMainLooper()).postDelayed({
            binding.progressBar.visibility = View.GONE
        }, 1000)

        if (description != null && description.length > 50) {
            binding.tampilkanLebihBanyakButton.visibility = View.VISIBLE
            var isExpanded = false
            binding.tampilkanLebihBanyakButton.setOnClickListener {
                if (!isExpanded) {
                    binding.detailDeskripsiTextView.maxLines = Integer.MAX_VALUE
                    binding.tampilkanLebihBanyakButton.text = "Tampilkan Lebih Sedikit"
                } else {
                    binding.detailDeskripsiTextView.maxLines = 2
                    binding.tampilkanLebihBanyakButton.text = "Tampilkan Lebih Banyak"
                }
                isExpanded = !isExpanded
            }
        }


        Glide.with(this)
            .load(photo)
            .into(binding.detailGambarImageView)

        if (description != null && description.length > 50) {
            binding.tampilkanLebihBanyakButton.visibility = View.VISIBLE
            // Set up onClickListener untuk tombol "Tampilkan Lebih Banyak" dan "Tampilkan Lebih Sedikit"
            var isExpanded = false // Untuk melacak apakah deskripsi saat ini ditampilkan penuh atau sebagian
            binding.tampilkanLebihBanyakButton.setOnClickListener {
                if (!isExpanded) {
                    // Jika deskripsi saat ini ditampilkan sebagian, tampilkan seluruhnya
                    binding.detailDeskripsiTextView.maxLines = Integer.MAX_VALUE
                    binding.tampilkanLebihBanyakButton.text = "Tampilkan Lebih Sedikit"
                } else {
                    // Jika deskripsi saat ini ditampilkan penuh, tampilkan hanya sebagian
                    binding.detailDeskripsiTextView.maxLines = 2 // Misalnya, hanya menampilkan 2 baris deskripsi
                    binding.tampilkanLebihBanyakButton.text = "Tampilkan Lebih Banyak"
                }
                isExpanded = !isExpanded // Toggle status isExpanded
            }
        }

        if (opera != null) {
            binding.showMore.visibility = View.VISIBLE
            var isExpanded = false
            binding.showMore.setOnClickListener {
                if (!isExpanded) {
                    binding.jamOperasional.maxLines = Integer.MAX_VALUE
                    binding.showMore.text = "Tampilkan Lebih Sedikit"
                } else {
                    binding.jamOperasional.maxLines = 2
                    binding.showMore.text = "Tampilkan Lebih Banyak"
                }
                isExpanded = !isExpanded
            }
        }

        if (fasilitas != null) {
            binding.showMore2.visibility = View.VISIBLE
            var isExpanded = false
            binding.showMore2.setOnClickListener {
                if (!isExpanded) {
                    binding.listFasilitas.maxLines = Integer.MAX_VALUE
                    binding.showMore2.text = "Tampilkan Lebih Sedikit"
                } else {
                    binding.listFasilitas.maxLines = 2
                    binding.showMore2.text = "Tampilkan Lebih Banyak"
                }
                isExpanded = !isExpanded
            }
        }

        // Ensure RatingBar is set to 0 by default if no rating is selected
        binding.rating.rating = 0f

        binding.rating.setOnRatingBarChangeListener { ratingBar, fl, b ->
            when (ratingBar.rating.toInt()) {
                1 -> binding.textView.text = "Very Bad"
                2 -> binding.textView.text = "Bad"
                3 -> binding.textView.text = "Good"
                4 -> binding.textView.text = "Great"
                5 -> binding.textView.text = "Awesome"
                else -> binding.textView.text = ""
            }
        }

        binding.textView.text = "Please select a rating"

        ref = FirebaseDatabase.getInstance().reference
        auth = Firebase.auth
        val firebaseUser = auth.currentUser

        //aksi untuk menyimpan data ke firebase
        binding.btn.setOnClickListener {
            val message = binding.rating.rating
            val review = binding.editText.text.toString()

            if (message == 0f && review.isEmpty()) {
                Toast.makeText(this@DetailKulinerActivity, "Rating dan komentar harus diisi", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (message == 0f) {
                Toast.makeText(this@DetailKulinerActivity, "Anda harus memilih rating sebelum memberi komentar", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (review.isEmpty()) {
                Toast.makeText(this@DetailKulinerActivity, "Anda harus mengisi field sebelum memberi komentar", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (firebaseUser != null) {
                val usersRef = ref.child("users").child(firebaseUser.uid)
                usersRef.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            val uid = snapshot.key ?: ""
                            val username = snapshot.child("username").value.toString()
                            val profile = snapshot.child("profileImageUrl").value.toString()
                            val email = snapshot.child("email").value.toString() // Menambahkan email
                            val currentDate = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(Date())

                            val ulasan = UlasanCoba(
                                uid,
                                username,
                                email,
                                profile,
                                review,
                                message,
                                currentDate
                            )

                            val ratingRef = ref.child("rating")
                                .child(intent.getStringExtra(PLACE).toString()).push() //ganti ke place id untuk menjadikan id tempat sebagai key
                            ratingRef.setValue(ulasan)
                                .addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        Toast.makeText(this@DetailKulinerActivity, "Rating added successfully!", Toast.LENGTH_SHORT).show()
                                    } else {
                                        Toast.makeText(this@DetailKulinerActivity, "Failed to add rating!", Toast.LENGTH_SHORT).show()
                                    }
                                }
                        } else {
                            Toast.makeText(this@DetailKulinerActivity, "User data not found!", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Toast.makeText(this@DetailKulinerActivity, "Failed to fetch user data: ${error.message}", Toast.LENGTH_SHORT).show()
                    }
                })
            } else {
                val alertBuilder = AlertDialog.Builder(this)
                alertBuilder.setTitle("Peringatan")
                alertBuilder.setMessage("Anda harus login untuk memberi ulasan")
                alertBuilder.setPositiveButton("OK") {dialog, which ->
                    val intent = Intent(this@DetailKulinerActivity, SignActivity::class.java)
                    startActivity(intent)
                }
                alertBuilder.setNegativeButton("Batal") {dialog, which ->

                }
                alertBuilder.show()
            }
            binding.editText.setText("")
        }

        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        kyuuAdapter = KyuuAdapter(this)
        binding.recyclerView.adapter = kyuuAdapter

        //Menampilkan Ulasan dari realtime database
        val wisataRef = ref.child("rating").child(intent.getStringExtra(DetailHotelActivity.PLACE).toString())
        wisataRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val ratingList = mutableListOf<Float>()
                    val komentarList = mutableListOf<String>()
                    val dayList = mutableListOf<String>()
                    val usernameList = mutableListOf<String>()
                    val profilList = mutableListOf<String>()

                    for (ratingSnapshot in snapshot.children) {
                        val rating = ratingSnapshot.child("rating").getValue(Float::class.java)
                        val komentar = ratingSnapshot.child("komentar").getValue(String::class.java)
                        val day = ratingSnapshot.child("day").getValue(String::class.java)
                        val username = ratingSnapshot.child("name").getValue(String::class.java)
                        val profile = ratingSnapshot.child("profile").getValue(String::class.java)
                        rating?.let { ratingList.add(it) }
                        komentar?.let { komentarList.add(it) }
                        day?.let { dayList.add(it) }
                        username?.let { usernameList.add(it) }
                        profile?.let { profilList.add(it) }
                    }
                    kyuuAdapter.addRating(ratingList, komentarList, usernameList, dayList, profilList)
                } else {
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        binding.btnFavorite.setOnClickListener {
            if (isFavorite) {
                addFavoritePlace()
            } else {
                addFavoritePlace()

            }
        }

        if (firebaseUser != null && place != null && address != null && photo != null) {
            val userRef = ref.child("users").child(firebaseUser.uid)
            userRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    //val name =snapshot.child("id").getValue(String::class.java)
                    val name = snapshot.key

                    if (snapshot.exists()) {
                        val favoriteRef = ref.child("favorites").child(name!!)
                        favoriteRef.orderByChild("placeName").equalTo(place).addListenerForSingleValueEvent(object :
                            ValueEventListener {
                            override fun onDataChange(dataSnapshot: DataSnapshot) {
                                if (dataSnapshot.exists()) {
                                    // Jika tempat sudah menjadi favorit, atur FAB menjadi filled
                                    isFavorite = true
                                    setupFavoriteButtonAppearance(isFavorite)
                                    binding.btnFavorite.setImageResource(R.drawable.baseline_favorite_white_24)
                                }
//
                            }

                            override fun onCancelled(error: DatabaseError) {
                                Toast.makeText(this@DetailKulinerActivity, "Gagal memeriksa tempat favorit: ${error.message}", Toast.LENGTH_SHORT).show()
                            }
                        })
                    } else {
                        Toast.makeText(this@DetailKulinerActivity, "Informasi pengguna tidak ditemukan", Toast.LENGTH_SHORT).show()
                    }

                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@DetailKulinerActivity, "Gagal mengambil informasi pengguna: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            })
        } else {
            Toast.makeText(this@DetailKulinerActivity, "Informasi tempat tidak lengkap", Toast.LENGTH_SHORT).show()
        }

        db = FirebaseDatabase.getInstance()

        binding.reportButton.setOnClickListener {
            if (place != null) {
                if (photo != null) {
                    showReportDialog(place)
                }
            } else {
                showLoginWarningDialog()
            }
        }

        binding.fabVideo.setOnClickListener {
            val intent = Intent(this, MediaPlayerActivity::class.java)
            intent.putExtra(MediaPlayerActivity.VIDEO, mediaType)
            startActivity(intent)
        }

        setContentView(binding.root)

        setupView()
    }

    private fun showReportDialog(placeName: String) {
        val alertDialog = AlertDialog.Builder(this)
        alertDialog.setTitle("Laporkan Tempat Kuliner")

        val input = EditText(this)
        input.hint = "Alasan pelaporan"
        alertDialog.setView(input)

        alertDialog.setPositiveButton("Laporkan") { dialog, which ->
            val reportReason = input.text.toString().trim()
            if (reportReason.isNotEmpty()) {
                val timestamp = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(Date())
                sendReportToFirebase(placeName, reportReason, timestamp)
            } else {
                Toast.makeText(this, "Alasan pelaporan tidak boleh kosong", Toast.LENGTH_SHORT).show()
            }
        }

        alertDialog.setNegativeButton("Batal") { dialog, which ->
            dialog.dismiss()
        }

        alertDialog.show()
    }


    private fun sendReportToFirebase(placeName: String, reportReason: String, timestamp: String) {
        val reportRef = FirebaseDatabase.getInstance().getReference("reports").push()
        val report = Report(
            reason = reportReason,
            placeName = placeName,
            timestamp = timestamp
        )
        reportRef.setValue(report)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Laporan berhasil dikirim", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Gagal mengirim laporan", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun showLoginWarningDialog() {
        val alertDialog = AlertDialog.Builder(this)
        alertDialog.setTitle("Peringatan")
        alertDialog.setMessage("Anda harus login untuk melaporkan tempat kuliner")
        alertDialog.setPositiveButton("Login") { _, _ ->
            val intent = Intent(this, SignActivity::class.java)
            startActivity(intent)
        }
        alertDialog.setNegativeButton("Batal") { dialog, _ ->
            dialog.dismiss()
        }
        alertDialog.show()
    }

    private fun deletePlaceByName(placeName: String) {
        ref = FirebaseDatabase.getInstance().reference
        auth = Firebase.auth
        val firebaseUser = auth.currentUser
        val usersRef = firebaseUser?.let { ref.child("favorites").child(it.uid) }
        val kulinerDatabase = ref.child("tempat").child("kuliner")
        val allPlaceDatabase = ref.child("allDestination")
        val ratingDatabase = ref.child("rating")

        Log.d("DeletePlace", "Place Name: $placeName")
        Log.d("DeletePlace", "Firebase User ID: ${firebaseUser?.uid}")

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

        // Delete from kuliner
        deletePlaceFromDatabase(kulinerDatabase) { kulinerSuccess ->
            if (kulinerSuccess) {
                Toast.makeText(this, "Deleted from kuliner", Toast.LENGTH_SHORT).show()
                // Delete from allPlace
                deletePlaceFromDatabase(allPlaceDatabase) { allPlaceSuccess ->
                    if (allPlaceSuccess) {
                        Toast.makeText(this, "Deleted from allPlace", Toast.LENGTH_SHORT).show()
                        // Delete from rating
                        deleteRatingByPlaceName(ratingDatabase) { ratingPlaceSuccess ->
                            if (ratingPlaceSuccess) {
                                Toast.makeText(this, "Deleted from rating", Toast.LENGTH_SHORT).show()
                                // Delete from favorites
                                if (usersRef != null) {
                                    deletePlaceFromDatabase(usersRef) { userPlaceSuccess ->
                                        if (userPlaceSuccess) {
                                            Toast.makeText(this, "Data wisata berhasil dihapus", Toast.LENGTH_SHORT).show()
                                            finish()
                                        } else {
                                            Log.e("DeletePlace", "Failed to delete place from favorites")
                                            Toast.makeText(this, "Gagal menghapus data wisata dari favorites", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                } else {
                                    Log.e("DeletePlace", "Users reference is null")
                                    Toast.makeText(this, "Gagal menghapus data wisata", Toast.LENGTH_SHORT).show()
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
                Log.e("DeletePlace", "Failed to delete place from kuliner")
                Toast.makeText(this, "Gagal menghapus data wisata dari kuliner", Toast.LENGTH_SHORT).show()
            }
        }
    }
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.uiSettings.isZoomControlsEnabled = true
        mMap.uiSettings.isIndoorLevelPickerEnabled = true
        mMap.uiSettings.isCompassEnabled = true
        mMap.uiSettings.isMapToolbarEnabled = true

        val address = intent.getStringExtra(DetailHotelActivity.ADDRESS)
        val place = intent.getStringExtra(DetailHotelActivity.PLACE)
        val lat = intent.getDoubleExtra(DetailHotelActivity.LATITUDE, 0.0)
        val lon = intent.getDoubleExtra(DetailHotelActivity.LONGITUDE, 0.0)

        val hotelLocation = LatLng(lat, lon)
        mMap.addMarker(MarkerOptions().position(hotelLocation).title(place).snippet(address))
        mMap.moveCamera(CameraUpdateFactory.newLatLng(hotelLocation))
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(hotelLocation, 14f))
    }

    private fun setupView() {
        @Suppress("DEPRECATION")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.hide(WindowInsets.Type.statusBars())
        } else {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }
        supportActionBar?.hide()
    }

    private fun addFavoritePlace() {
        val currentUser = auth.currentUser
        val placeName = intent.getStringExtra(DetailKulinerActivity.PLACE)
        val username = currentUser?.displayName // Mengambil nama pengguna
        val placeAddress = intent.getStringExtra(DetailKulinerActivity.ADDRESS)
        val placePhotoUrl = intent.getStringExtra(DetailKulinerActivity.PHOTO)

        val uiD = currentUser?.uid
        val userRef = uiD?.let { ref.child("users").child(it) }
        if (currentUser != null && placeName != null && placeAddress != null && placePhotoUrl != null) {
            userRef?.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    //val name = dataSnapshot.child("id").getValue(String::class.java)
                    val name = dataSnapshot.key
                    val usernameRef = ref.child("favorites")
                        .child(name ?: "") // Gunakan nama pengguna sebagai kunci
                    //usernameRef.addListenerForSingleValueEvent(object : ValueEventListener {

                    usernameRef.addListenerForSingleValueEvent(
                        object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                if (snapshot.exists()) {

                                    val placeExists = snapshot.children.any {
                                        it.child("placeName")
                                            .getValue(String::class.java) == placeName
                                    }

                                    if (placeExists) {
                                        // Jika tempat sudah ada dalam favorit, hapus dari daftar favorit
                                        val placeSnapshot = snapshot.children.first {
                                            it.child("placeName")
                                                .getValue(String::class.java) == placeName
                                        }
                                        placeSnapshot.ref.removeValue()
                                            .addOnCompleteListener { task ->
                                                if (task.isSuccessful) {
                                                    isFavorite = false
                                                    setupFavoriteButtonAppearance(isFavorite)
                                                    Toast.makeText(
                                                        this@DetailKulinerActivity,
                                                        "Tempat dihapus dari favorit",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                    // Mengubah tampilan FAB menjadi tidak diisi
                                                    binding.btnFavorite.setImageResource(R.drawable.baseline_favorite_border_24)
                                                } else {
                                                    Toast.makeText(
                                                        this@DetailKulinerActivity,
                                                        "Gagal menghapus tempat dari favorit",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                }
                                            }
                                    } else {
                                        // Jika tempat belum ada dalam favorit, tambahkan ke daftar favorit
                                        val newFavoriteRef = usernameRef.push()
                                        val favoritePlaceMap = hashMapOf(
                                            "placeName" to placeName,
                                            "placeAddress" to placeAddress,
                                            "placePhotoUrl" to placePhotoUrl
                                        )

                                        newFavoriteRef.setValue(favoritePlaceMap)
                                            .addOnCompleteListener { task ->
                                                if (task.isSuccessful) {
                                                    isFavorite = true
                                                    setupFavoriteButtonAppearance(isFavorite)
                                                    Toast.makeText(
                                                        this@DetailKulinerActivity,
                                                        "Tempat ditambahkan ke favorit",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                    // Mengubah tampilan FAB menjadi diisi
                                                    binding.btnFavorite.setImageResource(R.drawable.baseline_favorite_white_24)
                                                } else {
                                                    isFavorite = false
                                                    setupFavoriteButtonAppearance(isFavorite)
                                                    Toast.makeText(
                                                        this@DetailKulinerActivity,
                                                        "Gagal menambahkan tempat ke favorit",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                }
                                            }
                                    }
                                } else {
                                    // Jika tidak ada data favorit untuk pengguna saat ini, buat node baru
                                    val newFavoriteRef = usernameRef.push()
                                    val favoritePlaceMap = hashMapOf(
                                        "placeName" to placeName,
                                        "placeAddress" to placeAddress,
                                        "placePhotoUrl" to placePhotoUrl
                                    )

                                    newFavoriteRef.setValue(favoritePlaceMap)
                                        .addOnCompleteListener { task ->
                                            if (task.isSuccessful) {
                                                isFavorite = true
                                                setupFavoriteButtonAppearance(isFavorite)
                                                Toast.makeText(
                                                    this@DetailKulinerActivity,
                                                    "Tempat ditambahkan ke favorit",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                                // Mengubah tampilan FAB menjadi diisi
                                                binding.btnFavorite.setImageResource(R.drawable.baseline_favorite_white_24)
                                            } else {
                                                isFavorite = false
                                                setupFavoriteButtonAppearance(isFavorite)
                                                Toast.makeText(
                                                    this@DetailKulinerActivity,
                                                    "Gagal menambahkan tempat ke favorit",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                        }
                                }
                            }

                            override fun onCancelled(error: DatabaseError) {
                                Toast.makeText(
                                    this@DetailKulinerActivity,
                                    "Gagal memeriksa tempat favorit: ${error.message}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        })

                }

                override fun onCancelled(databaseError: DatabaseError) {
                    // Tangani kesalahan jika terjadi
                }
            })
        }

    }


    private fun setupFavoriteButtonAppearance(isFav: Boolean) {
        // Mengatur tampilan awal Float Action Button berdasarkan status favorit
        if (isFav) {
            binding.btnFavorite.setImageResource(R.drawable.baseline_favorite_white_24)
        } else {
            binding.btnFavorite.setImageResource(R.drawable.baseline_favorite_border_24)
        }
    }

    companion object {
        const val PLACE = "place"
        const val ADDRESS = "address"
        const val DESC = "desc"
        const val PHOTO = "photo"
        const val LATITUDE = "latitude"
        const val LONGITUDE = "longitude"
        const val OPERASIONAL = "operasional"
        const val FASILITAS = "fasilitas"
        const val PLACE_ID = "placeId"
        const val VIDEO = "video"
    }

}