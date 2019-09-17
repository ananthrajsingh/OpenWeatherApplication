package com.ananth.weatherdemo.ui.location

import android.content.Context
import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.ananth.weatherdemo.DEFAULT_LOCATION_VALUE
import com.ananth.weatherdemo.LOCATION_KEY
import com.ananth.weatherdemo.LOCATION_SHARED_PREFERENCE
import com.ananth.weatherdemo.databinding.LocationFragmentBinding

class LocationFragment : Fragment() {

    companion object {
        fun newInstance() = LocationFragment()
    }

    private val viewModel : LocationViewModel by lazy {
        val activity = requireNotNull(this.activity) {
            "You can only access the viewModel after onActivityCreated()"
        }
        ViewModelProviders.of(this, LocationViewModel.Factory(activity.application))
            .get(LocationViewModel::class.java)
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ) : View? {

        val binding = LocationFragmentBinding.inflate(inflater)

        binding.lifecycleOwner = this

        binding.viewModel = viewModel

        /*
         * If we don't have any location saved, hide the CANCEL button
         */
        if (checkLocationSaved()) {
            binding.cancel.isVisible = false
        }
        /*
         * Navigate back to the location fragment if the observed value changes to true
         */
        viewModel.navigate.observe(this, Observer {
            if (it) {
                this.findNavController()
                    .navigate(LocationFragmentDirections.actionLocationFragmentToForecastFragment())
                viewModel.doneNavigating()
            }
        })
        return binding.root
    }

    /**
     * Checks whether there is any previous location saved to the shared preferences.
     * This method is intended to check if this is the first time the user is being shown
     * this fragment.
     */
    private fun checkLocationSaved() : Boolean {
        val locationSharedPreference = context?.getSharedPreferences(
            LOCATION_SHARED_PREFERENCE, Context.MODE_PRIVATE)
        val location = locationSharedPreference?.getString(LOCATION_KEY, DEFAULT_LOCATION_VALUE)
        return location == DEFAULT_LOCATION_VALUE
    }
}
