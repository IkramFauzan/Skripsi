package com.example.testingfirebaserealtime.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.testingfirebaserealtime.data.KelurahanCuaca
import com.example.testingfirebaserealtime.databinding.ItemKelurahanBinding

class KelurahanCuacaAdapter(private val kelurahanCuacaList: List<KelurahanCuaca>) :
    RecyclerView.Adapter<KelurahanCuacaAdapter.KelurahanCuacaViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): KelurahanCuacaViewHolder {
        val binding = ItemKelurahanBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return KelurahanCuacaViewHolder(binding)
    }

    override fun onBindViewHolder(holder: KelurahanCuacaViewHolder, position: Int) {
        val kelurahanCuaca = kelurahanCuacaList[position]
        holder.bind(kelurahanCuaca)
    }

    override fun getItemCount(): Int = kelurahanCuacaList.size

    class KelurahanCuacaViewHolder(private val binding: ItemKelurahanBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(kelurahanCuaca: KelurahanCuaca) {
            binding.textViewKelurahan.text = kelurahanCuaca.kelurahan
            binding.textViewSuhu.text = "${kelurahanCuaca.suhu}°C"
            binding.textViewDeskripsi.text = kelurahanCuaca.deskripsi
            binding.imageViewIcon.setImageResource(kelurahanCuaca.iconResId)
        }
    }
}
