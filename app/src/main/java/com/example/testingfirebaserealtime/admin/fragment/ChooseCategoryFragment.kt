package com.example.testingfirebaserealtime.admin.fragment

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.testingfirebaserealtime.admin.manage.*
import com.example.testingfirebaserealtime.databinding.FragmentChooseCategoryBinding

class ChooseCategoryFragment : Fragment() {

    private var _binding: FragmentChooseCategoryBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentChooseCategoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.nature.setOnClickListener {
            val intent = Intent(requireContext(), ManagePenginapan::class.java)
            startActivity(intent)
        }

        binding.shopping.setOnClickListener {
            val intent = Intent(requireContext(), ManageWisataBuatan::class.java)
            startActivity(intent)
        }

        binding.history.setOnClickListener {
            val intent = Intent(requireContext(), ManageSejarah::class.java)
            startActivity(intent)
        }

//        binding.culture.setOnClickListener {
//            val intent = Intent(requireContext(), ManagePrayer::class.java)
//            startActivity(intent)
//        }

        binding.food.setOnClickListener {
            val intent = Intent(requireContext(), ManageKuliner::class.java)
            startActivity(intent)
        }

        binding.adventure.setOnClickListener {
            val intent = Intent(requireContext(), ManageWisataAlam::class.java)
            startActivity(intent)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}