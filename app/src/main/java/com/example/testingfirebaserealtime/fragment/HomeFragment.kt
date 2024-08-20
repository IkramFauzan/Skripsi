package com.example.testingfirebaserealtime.fragment

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.testingfirebaserealtime.DataClass
import com.example.testingfirebaserealtime.Destination
import com.example.testingfirebaserealtime.EventAd
import com.example.testingfirebaserealtime.R
import com.example.testingfirebaserealtime.activity.*
import com.example.testingfirebaserealtime.adapter.DestinationAdapter
import com.example.testingfirebaserealtime.category.WisataBuatanActivity
import com.example.testingfirebaserealtime.category.SejarahActivity
import com.example.testingfirebaserealtime.databinding.FragmentHomeBinding
import com.example.testingfirebaserealtime.detail.DetailAllLocationAcitivity
import com.example.testingfirebaserealtime.penginapan.activity.HotelActivity
import com.example.testingfirebaserealtime.activity.KulinerActivity
import com.example.testingfirebaserealtime.activity.PrayerActivity
import com.example.testingfirebaserealtime.activity.WisataAlamActivity
import com.example.testingfirebaserealtime.adapter.EventAdapter
import com.example.testingfirebaserealtime.detail.DetailEventActivity
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var adapter: DestinationAdapter
    private lateinit var databaseReference: DatabaseReference
    private lateinit var databaseReferenceArticle: DatabaseReference
    private lateinit var database: FirebaseDatabase
    private lateinit var dataList: ArrayList<DataClass>

    private lateinit var eventAdapter: EventAdapter
    private val eventList = ArrayList<EventAd>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()

        database = FirebaseDatabase.getInstance()
        databaseReference = database.getReference("users")

        binding.penginapan.setOnClickListener {
            val intent = Intent(requireContext(), HotelActivity::class.java)
            startActivity(intent)
        }

        binding.kuliner.setOnClickListener {
            val intent = Intent(requireContext(), KulinerActivity::class.java)
            startActivity(intent)
        }

        binding.prayer.setOnClickListener {
            val intent = Intent(requireContext(), PrayerActivity::class.java)
            startActivity(intent)
        }

        binding.wisata.setOnClickListener {
            val intent = Intent(requireContext(), WisataAlamActivity::class.java)
            startActivity(intent)
        }

        binding.belanja.setOnClickListener {
            val intent = Intent(requireContext(), WisataBuatanActivity::class.java)
            startActivity(intent)
        }

        binding.sejarah.setOnClickListener {
            val intent = Intent(requireContext(), SejarahActivity::class.java)
            startActivity(intent)
        }

        if (!isInternetAvailable()) {
            showNoInternetAlert()
        }

        setupView()
        loadUserProfile()
        setupRecyclerView()
        setupViewPager()
        loadEvents()
    }

    private fun loadUserProfile() {
        val currentUser = auth.currentUser
        val currentUserId = auth.currentUser?.uid

        Glide.with(requireContext())
            .load(currentUser?.photoUrl)
            .circleCrop()
            .into(binding.imageProfile)

        if (currentUserId != null) {
            databaseReference.child(currentUserId)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        val displayName = dataSnapshot.child("username").getValue(String::class.java)
                        val profilePhotoUrl = dataSnapshot.child("profileImageUrl").getValue(String::class.java)

                        binding.textHiUser.text = "Hi, ${displayName ?: "User"}"

                        if (profilePhotoUrl != null) {
                            Glide.with(requireContext())
                                .load(profilePhotoUrl)
                                .circleCrop()
                                .placeholder(R.drawable.baseline_person_pin_24)
                                .error(R.drawable.baseline_person_pin_24)
                                .into(binding.imageProfile)
                        } else {
                            binding.imageProfile.setImageResource(R.drawable.baseline_person_pin_24)
                        }
                    }

                    override fun onCancelled(databaseError: DatabaseError) {
                        Log.e("Firebase", "Error getting data", databaseError.toException())
                    }
                })
        } else {
            binding.textHiUser.text = "Hi, User"
            binding.imageProfile.setImageResource(R.drawable.baseline_person_pin_24)
        }
    }

    private fun setupRecyclerView() {
        dataList = ArrayList()
        adapter = DestinationAdapter(emptyList())
        binding.popularDestination.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.popularDestination.adapter = adapter

        val ratingReference = FirebaseDatabase.getInstance().reference.child("rating")
        Log.d("Rating", "$ratingReference")

        // Fetch data from "rating" node
        ratingReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(ratingSnapshot: DataSnapshot) {
                val dataMap = mutableMapOf<String, DataClass>()
                val allReference = FirebaseDatabase.getInstance().reference.child("allDestination")

                allReference.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(destinationSnapshot: DataSnapshot) {
                        for (wisataRatingSnapshot in ratingSnapshot.children) {
                            val placeName = wisataRatingSnapshot.key ?: continue

                            var totalRating = 0
                            for (ratingValueSnapshot in wisataRatingSnapshot.children) {
                                val ratingData = ratingValueSnapshot.value as? Map<String, Any>
                                val ratingValue = (ratingData?.get("rating") as? Number)?.toInt() ?: 0
                                totalRating += ratingValue
                            }

                            // Check if the placeName exists in allDestination
                            for (destination in destinationSnapshot.children) {
                                val destinationData = destination.getValue(DataClass::class.java)
                                destinationData?.let {
                                    if (it.placeName.equals(placeName, ignoreCase = true)) {
                                        // Correctly initializing the DataClass
                                        val dataClass = DataClass(
                                            placeAddress = it.placeAddress,
                                            placeName = it.placeName,
                                            placePhotoUrl = it.placePhotoUrl,
                                            description = it.description,
                                            lat = it.lat,
                                            lon = it.lon,
                                            operational = it.operational,
                                            fasilitas = it.fasilitas,
                                            category = it.category,
                                            placeVideoUrl = it.placeVideoUrl,
                                            id = it.id,
                                            totalRating = totalRating
                                        )
                                        dataMap[placeName] = dataClass
                                    }
                                }
                            }
                        }

                        dataList.clear()
                        dataList.addAll(dataMap.values)
                        dataList.sortByDescending { it.totalRating } // Sort by total rating

                        adapter.setItemList(dataList)
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.e("SearchFragment", "Error fetching destinations: ${error.message}")
                    }
                })
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("SearchFragment", "Error fetching ratings: ${error.message}")
            }
        })

        adapter.setOnItemClickCallback(object : DestinationAdapter.OnItemClickCallback {
            override fun onItemClick(dataClass: DataClass) {
                val intent = Intent(requireContext(), DetailAllLocationAcitivity::class.java).apply {
                    putExtra(DetailAllLocationAcitivity.PLACE, dataClass.placeName)
                }
                startActivity(intent)
            }
        })
    }

    private fun setupViewPager() {
        eventAdapter = EventAdapter(eventList) { eventAd ->
            val context = requireContext()
            val intent = Intent(context, DetailEventActivity::class.java).apply {
                putExtra(DetailEventActivity.IMAGE, eventAd.imageUrl)
                putExtra(DetailEventActivity.NAME, eventAd.eventName)
                putExtra(DetailEventActivity.DATE, eventAd.eventSchedule)
                putExtra(DetailEventActivity.LOC, eventAd.eventAddress)
                putExtra(DetailEventActivity.DETAIL, eventAd.eventDescription)
            }
            context.startActivity(intent)
        }
        binding.viewPagerEventAd.adapter = eventAdapter
        TabLayoutMediator(binding.tabLayout, binding.viewPagerEventAd) { _, _ -> }.attach()

        val handler = Handler(Looper.getMainLooper())
        val update = Runnable {
            var currentItem = binding.viewPagerEventAd.currentItem
            if (currentItem == eventAdapter.itemCount - 1) currentItem = 0 else currentItem++
            binding.viewPagerEventAd.setCurrentItem(currentItem, true)
        }
        val timer = Timer()
        timer.schedule(object : TimerTask() {
            override fun run() {
                handler.post(update)
            }
        }, 3000, 3000)
    }

    private fun loadEvents() {
        databaseReference = FirebaseDatabase.getInstance().getReference("eventAds")

        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                eventList.clear()
                val currentTime = System.currentTimeMillis()
                val oneDayMillis = 24 * 60 * 60 * 1000

                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

                for (eventSnapshot in snapshot.children) {
                    val eventAd = eventSnapshot.getValue(EventAd::class.java)
                    eventAd?.let {
                        if (it.eventSchedule.isNotEmpty()) {
                            try {
                                val eventDate = dateFormat.parse(it.eventSchedule)
                                val eventTime = eventDate?.time ?: 0L
                                if (eventTime >= currentTime - oneDayMillis) {
                                    eventList.add(it)
                                } else {
                                    // Handle events that are not within the desired range
                                }
                            } catch (e: ParseException) {
                                Log.e("HomeFragment", "Failed to parse event date", e)
                            }
                        } else {
                            Log.e("HomeFragment", "Empty eventSchedule found")
                        }
                    }
                }
                eventAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireContext(), "Failed to load events", Toast.LENGTH_SHORT).show()
                Log.e("HomeFragment", "Failed to load events", error.toException())
            }
        })
    }
    private fun isInternetAvailable(): Boolean {
        val connectivityManager =
            requireContext().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetworkInfo = connectivityManager.activeNetworkInfo
        return activeNetworkInfo != null && activeNetworkInfo.isConnected
    }

    private fun showNoInternetAlert() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("No connection")
            .setMessage("Mohon aktifkan koneksi internet untuk melanjutkan.")
            .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
            .setCancelable(false)
            .show()
    }

    private fun setupView() {
        @Suppress("DEPRECATION")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            requireActivity().window.insetsController?.hide(WindowInsets.Type.statusBars())
        } else {
            requireActivity().window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }
        (requireActivity() as AppCompatActivity).supportActionBar?.hide()
    }
}