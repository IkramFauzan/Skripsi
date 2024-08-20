package com.example.testingfirebaserealtime.activity

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.testingfirebaserealtime.R
import com.example.testingfirebaserealtime.adapter.KelurahanCuacaAdapter
import com.example.testingfirebaserealtime.data.KelurahanCuaca
import com.example.testingfirebaserealtime.data.ApiService
import com.example.testingfirebaserealtime.data.WeatherResponse
import com.example.testingfirebaserealtime.databinding.ActivityCuacaBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class CuacaActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCuacaBinding
    private val kelurahanCuacaList = mutableListOf<KelurahanCuaca>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityCuacaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Setup RecyclerView
        binding.recyclerviewKelurahanCuaca.layoutManager = LinearLayoutManager(this)
        val adapter = KelurahanCuacaAdapter(kelurahanCuacaList)
        binding.recyclerviewKelurahanCuaca.adapter = adapter

        // Daftar kelurahan yang akan ditampilkan cuacanya
        val kelurahanList = listOf(
            "Sungguminasa",
            "Pallangga",
            "Bontomarannu",
            "Tompobulu",
            "Tinggimoncong",
            "Mawang",
            "Bontomarannu",
            "Paccinongan")

        for (kelurahan in kelurahanList) {
            getWeatherData(kelurahan, adapter)
        }
    }

    private fun getWeatherData(kelurahan: String, adapter: KelurahanCuacaAdapter) {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.openweathermap.org/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val service = retrofit.create(ApiService::class.java)
        val call = service.getCurrentWeather(kelurahan + ",ID", "67405cf0cf785a7e70a3f5d332005592")

        call.enqueue(object : Callback<WeatherResponse> {
            override fun onResponse(
                call: Call<WeatherResponse>,
                response: Response<WeatherResponse>
            ) {
                if (response.isSuccessful) {
                    val weatherData = response.body()
                    Log.d("Weather", "$weatherData")
                    val iconResId = when (weatherData?.weather?.get(0)?.main) {
                        "Clear" -> R.drawable.sun
                        "Clouds" -> R.drawable.cloudy
                        "Rain" -> R.drawable.rainy
                        else -> R.drawable.sun // Gambar default jika tidak ada kecocokan
                    }
                    val kelurahanCuaca = KelurahanCuaca(
                        kelurahan = kelurahan,
                        suhu = weatherData?.main?.temp ?: 0f,
                        deskripsi = weatherData?.weather?.get(0)?.description ?: "No description",
                        iconResId = iconResId
                    )
                    kelurahanCuacaList.add(kelurahanCuaca)
                    adapter.notifyDataSetChanged()
                }
            }

            override fun onFailure(call: Call<WeatherResponse>, t: Throwable) {
                // Handle failure
            }
        })
    }

}
