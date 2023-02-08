package com.vrcareer.b4b.app.tasks

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI.setupActionBarWithNavController
import androidx.navigation.ui.navigateUp
import com.vrcareer.b4b.R
import com.vrcareer.b4b.databinding.FragmentTaskHomeBinding


class TaskHomeFragment : Fragment() {

    private lateinit var binding: FragmentTaskHomeBinding
    private lateinit var appBarConfiguration: AppBarConfiguration
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentTaskHomeBinding.inflate(inflater,container,false)

        return binding.root
    }


}