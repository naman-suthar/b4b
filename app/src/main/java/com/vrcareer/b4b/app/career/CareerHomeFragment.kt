package com.vrcareer.b4b.app.career

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.vrcareer.b4b.databinding.FragmentCareerHomeBinding

class CareerHomeFragment : Fragment() {
    private lateinit var binding: FragmentCareerHomeBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentCareerHomeBinding.inflate(inflater,container,false)

        return binding.root
    }


}