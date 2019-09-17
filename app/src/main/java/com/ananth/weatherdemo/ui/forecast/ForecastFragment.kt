package com.ananth.weatherdemo.ui.forecast

import android.content.Context
import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.ananth.weatherdemo.DEFAULT_LOCATION_VALUE
import com.ananth.weatherdemo.LOCATION_KEY
import com.ananth.weatherdemo.LOCATION_SHARED_PREFERENCE
import com.ananth.weatherdemo.databinding.ForecastFragmentBinding

class ForecastFragment : Fragment() {

    companion object {
        fun newInstance() = ForecastFragment()
    }


    private val viewModel : ForecastViewModel by lazy {
        val activity = requireNotNull(this.activity) {
            "You can only access the viewModel after onActivityCreated()"
        }
        ViewModelProviders.of(this, ForecastViewModel.Factory(activity.application))
            .get(ForecastViewModel::class.java)
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ) : View {
        val binding = ForecastFragmentBinding.inflate(inflater)

        /*
         * Allows Data Binding to Observe LiveData with the lifecycle of this Fragment
         */
        binding.setLifecycleOwner(this)

        /*
         * Giving the binding access to the OverviewViewModel
         */
        binding.viewModel = viewModel

        if (checkLocationSaved()) {
            navigate()
        }

        /*
         * Observes this LiveData for value change and navigates to LocationFragment
         */
        viewModel.navigate.observe(this, Observer {
            if (it) {
                navigate()
            }
        })
        /*
         * Implementation of swipe to refresh functionality
         */
        binding.swipeRefreshLayout.setOnRefreshListener {
            val locationSharedPreference = context?.getSharedPreferences(
                LOCATION_SHARED_PREFERENCE, Context.MODE_PRIVATE)
            val location = locationSharedPreference?.getString(LOCATION_KEY, DEFAULT_LOCATION_VALUE)
            location?.let { viewModel.getWeather(it) }
            binding.swipeRefreshLayout.isRefreshing = false
        }
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

    /**
     * Navigates to the LocationFragment
     */
    private fun navigate() {
        this.findNavController()
            .navigate(ForecastFragmentDirections.actionForecastFragmentToLocationFragment())
        viewModel.doneNavigating()
    }
}
