package com.example.testingfirebaserealtime.detail

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.testingfirebaserealtime.*
import com.example.testingfirebaserealtime.R
import com.example.testingfirebaserealtime.admin.UpdateDataActivity
import com.example.testingfirebaserealtime.auth.SignActivity
import com.example.testingfirebaserealtime.databinding.ActivityDetailAllLocationAcitivityBinding
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

@Suppress("DEPRECATION")
class DetailAllLocationAcitivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var binding: ActivityDetailAllLocationAcitivityBinding
    private lateinit var mMap: GoogleMap
    private lateinit var ref: DatabaseReference
    private lateinit var kyuuAdapter: KyuuAdapter
    private val usersMap: MutableMap<String, User> = mutableMapOf()

    private lateinit var auth: FirebaseAuth

    private lateinit var db: FirebaseDatabase

    private var isFavorite = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityDetailAllLocationAcitivityBinding.inflate(layoutInflater)

        val dataClass = intent.getSerializableExtra(DATA) as DataClass

        val opera = dataClass.operational
        val fasilitas = dataClass.fasilitas
        val description = dataClass.description

        val place = dataClass.placeName
        val address = dataClass.placeAddress
        val photo = dataClass.placePhotoUrl
        val category = dataClass.category

        binding.apply {
            detailAlamatTextView.text = dataClass.placeAddress
            detailDeskripsiTextView.text = dataClass.description
            detailTempatTextView.text = dataClass.placeName
            jamOperasional.text = dataClass.operational
            listFasilitas.text = dataClass.fasilitas
        }

        when (category) {
            "wisataBuatan" -> binding.time.text = "Jam Operasional"
            "wisataAlam" -> binding.time.text = "Jam Operasional"
            "wisataSejarah" -> binding.time.text = "Jam Operasional"
            "kuliner" -> binding.time.text = "Jam Operasional"
            "penginapan" -> binding.time.text = "Harga Penginapan"
            else -> binding.time.text = "Informasi Tidak Tersedia" // default text jika kategori tidak sesuai
        }

        Glide.with(this)
            .load(dataClass.placePhotoUrl)
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

        binding.btn.setOnClickListener {
            val message = binding.rating.rating
            val review = binding.editText.text.toString()

            if (message == 0f && review.isEmpty()) {
                Toast.makeText(this@DetailAllLocationAcitivity, "Rating dan komentar harus diisi", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (message == 0f) {
                Toast.makeText(this@DetailAllLocationAcitivity, "Anda harus memilih rating sebelum memberi komentar", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (review.isEmpty()) {
                Toast.makeText(this@DetailAllLocationAcitivity, "Anda harus mengisi field sebelum memberi komentar", Toast.LENGTH_SHORT).show()
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
                            val email = snapshot.child("email").value.toString()
                            val currentDate = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(
                                Date()
                            )

                            val ulasan = UlasanCoba(
                                uid,
                                username,
                                email,
                                profile,
                                review,
                                message,
                                currentDate
                            )

                            // Menambahkan komentar ke tempat wisata tertentu
                            val ratingRef = ref.child("rating").child(intent.getStringExtra(DetailPrayerActivity.PLACE).toString()).push()
                            ratingRef.setValue(ulasan)
                                .addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        Toast.makeText(this@DetailAllLocationAcitivity, "Rating added successfully!", Toast.LENGTH_SHORT).show()
                                    } else {
                                        Toast.makeText(this@DetailAllLocationAcitivity, "Failed to add rating!", Toast.LENGTH_SHORT).show()
                                    }
                                }
                        } else {
                            Toast.makeText(this@DetailAllLocationAcitivity, "User data not found!", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Toast.makeText(this@DetailAllLocationAcitivity, "Failed to fetch user data: ${error.message}", Toast.LENGTH_SHORT).show()
                    }
                })
            } else {
                val alertBuilder = AlertDialog.Builder(this)
                alertBuilder.setTitle("Peringatan")
                alertBuilder.setMessage("Anda harus login untuk memberi ulasan")
                alertBuilder.setPositiveButton("OK") {dialog, which ->
                    val intent = Intent(this@DetailAllLocationAcitivity, SignActivity::class.java)
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

        // Mendapatkan data pengguna dari Firebase Database
        val usersData = FirebaseDatabase.getInstance().getReference("users")
        usersData.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                dataSnapshot.children.forEach { snapshot ->
                    val user = snapshot.getValue(User::class.java)
                    user?.let {
                        usersMap[it.username!!] = user
                    }
                }
                // Setelah mendapatkan data pengguna, beri tahu adapter
                kyuuAdapter.setUsersMap(usersMap)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle error
            }
        })

        //Menampilkan Ulasan dari realtime database
        val wisataRef = ref.child("rating").child(intent.getStringExtra(PLACE).toString())
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
                    // Handle jika snapshot kosong
                    // Misalnya, tampilkan pesan bahwa tidak ada data yang tersedia
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
                    val name = snapshot.key

                    if (snapshot.exists()) {
                        val favoriteRef = ref.child("favorites").child(name!!)
                        favoriteRef.orderByChild("placeName").equalTo(place).addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(dataSnapshot: DataSnapshot) {
                                if (dataSnapshot.exists()) {
                                    // Jika tempat sudah menjadi favorit, atur FAB menjadi filled
                                    isFavorite = true
                                    setupFavoriteButtonAppearance(isFavorite)
                                    binding.btnFavorite.setImageResource(R.drawable.baseline_favorite_white_24)
                                    //Toast.makeText(this@DetailKulinerActivity, "Tempat sudah ditambahkan ke favorit", Toast.LENGTH_SHORT).show()
                                }
//
                            }

                            override fun onCancelled(error: DatabaseError) {
                                Toast.makeText(this@DetailAllLocationAcitivity, "Gagal memeriksa tempat favorit: ${error.message}", Toast.LENGTH_SHORT).show()
                            }
                        })
                    } else {
                        Toast.makeText(this@DetailAllLocationAcitivity, "Informasi pengguna tidak ditemukan", Toast.LENGTH_SHORT).show()
                    }

                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@DetailAllLocationAcitivity, "Gagal mengambil informasi pengguna: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            })
        } else {
            Toast.makeText(this@DetailAllLocationAcitivity, "Informasi tempat tidak lengkap", Toast.LENGTH_SHORT).show()
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

        setContentView(binding.root)
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
                //val timestamp = System.currentTimeMillis().toString()
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
        alertDialog.setPositiveButton("Login") { dialog, which ->
            val intent = Intent(this, SignActivity::class.java)
            startActivity(intent)
        }
        alertDialog.setNegativeButton("Batal") { dialog, which ->
            dialog.dismiss()
        }
        alertDialog.show()
    }

    private fun deletePlaceByName(placeName: String) {
        val database = FirebaseDatabase.getInstance().reference.child("tempat").child("kuliner")

        database.get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                var placeIdToDelete: String? = null
                for (snapshot in task.result.children) {
                    val currentPlaceName = snapshot.child("placeName").getValue(String::class.java)
                    if (currentPlaceName == placeName) {
                        placeIdToDelete = snapshot.key
                        break
                    }
                }

                if (placeIdToDelete != null) {
                    database.child(placeIdToDelete).removeValue().addOnCompleteListener { deleteTask ->
                        if (deleteTask.isSuccessful) {
                            Toast.makeText(this, "Data wisata berhasil dihapus", Toast.LENGTH_SHORT).show()
                            finish()
                        } else {
                            Toast.makeText(this, "Gagal menghapus data wisata", Toast.LENGTH_SHORT).show()
                        }
                    }.addOnFailureListener { exception ->
                        Toast.makeText(this, "Error: ${exception.message}", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "Tempat wisata tidak ditemukan", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Gagal mengambil data", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener { exception ->
            Toast.makeText(this, "Error: ${exception.message}", Toast.LENGTH_SHORT).show()
        }
    }
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.uiSettings.isZoomControlsEnabled = true
        mMap.uiSettings.isIndoorLevelPickerEnabled = true
        mMap.uiSettings.isCompassEnabled = true
        mMap.uiSettings.isMapToolbarEnabled = true

        val dataClass = intent.getSerializableExtra(DATA) as DataClass

        val address = dataClass.placeAddress
        val place = dataClass.placeName
        val lat = dataClass.lat
        val lon = dataClass.lon

        val hotelLocation = LatLng(lat, lon)
        mMap.addMarker(MarkerOptions().position(hotelLocation).title(place).snippet(address))
        mMap.moveCamera(CameraUpdateFactory.newLatLng(hotelLocation))
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(hotelLocation, 14f))

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
                                                        this@DetailAllLocationAcitivity,
                                                        "Tempat dihapus dari favorit",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                    // Mengubah tampilan FAB menjadi tidak diisi
                                                    binding.btnFavorite.setImageResource(R.drawable.baseline_favorite_border_24)
                                                } else {
                                                    Toast.makeText(
                                                        this@DetailAllLocationAcitivity,
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
                                                        this@DetailAllLocationAcitivity,
                                                        "Tempat ditambahkan ke favorit",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                    // Mengubah tampilan FAB menjadi diisi
                                                    binding.btnFavorite.setImageResource(R.drawable.baseline_favorite_white_24)
                                                } else {
                                                    isFavorite = false
                                                    setupFavoriteButtonAppearance(isFavorite)
                                                    Toast.makeText(
                                                        this@DetailAllLocationAcitivity,
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
                                                    this@DetailAllLocationAcitivity,
                                                    "Tempat ditambahkan ke favorit",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                                // Mengubah tampilan FAB menjadi diisi
                                                binding.btnFavorite.setImageResource(R.drawable.baseline_favorite_white_24)
                                            } else {
                                                isFavorite = false
                                                setupFavoriteButtonAppearance(isFavorite)
                                                Toast.makeText(
                                                    this@DetailAllLocationAcitivity,
                                                    "Gagal menambahkan tempat ke favorit",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                        }
                                }
                            }

                            override fun onCancelled(error: DatabaseError) {
                                Toast.makeText(
                                    this@DetailAllLocationAcitivity,
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

    companion object {
        const val DATA = "data"
        const val PLACE = "place"
    }
}

