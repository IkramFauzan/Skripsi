package com.example.testingfirebaserealtime.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.example.testingfirebaserealtime.EventAd
import com.example.testingfirebaserealtime.R
import com.example.testingfirebaserealtime.databinding.FragmentEventDetailBinding

class EventDetailFragment : Fragment() {

    private var _binding: FragmentEventDetailBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout using view binding
        _binding = FragmentEventDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Retrieve the event data from arguments
        val eventAd = arguments?.getParcelable<EventAd>("eventAds")

        eventAd?.let {
            Glide.with(this)
                .load(it.imageUrl)
                .into(binding.imageEventDetail)

            binding.textEventName.text = it.eventName
            binding.textEventSchedule.text = it.eventSchedule
            binding.textEventDescription.text = it.eventDescription
            binding.textEventAddress.text = it.eventAddress
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(eventAd: EventAd) = EventDetailFragment().apply {
            arguments = Bundle().apply {
                putParcelable("eventAds", eventAd)
            }
        }
    }
}
