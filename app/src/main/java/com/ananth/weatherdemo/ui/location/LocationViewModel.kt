package com.ananth.weatherdemo.ui.location

import android.app.Application
import android.content.Context
import androidx.databinding.ObservableField
import androidx.lifecycle.*
import com.ananth.weatherdemo.LOCATION_KEY
import com.ananth.weatherdemo.LOCATION_SHARED_PREFERENCE

class LocationViewModel(application: Application) : AndroidViewModel(application)  {

    /*
     * This will serve as two way binding, this will have the value that the user is
     * typing in the EditText
     */
    val location = ObservableField<String>()
    var context : Context = application

    /*
     * The Fragment observes this to navigate to the ForecastFragment
     */
    private val _navigate = MutableLiveData<Boolean>()
    val navigate : LiveData<Boolean>
        get() = _navigate

    /**
     * Saves the user entered location in SharedPreferences, in case the entered value isn't
     * blank or null. In that case, this also triggers navigation to ForecastFragment.
     * This is callback called when user clicks DONE button
     */
    fun onDoneClick() {
        if (!location.get().isNullOrBlank()) {
            val locationSharedPreference = context.getSharedPreferences(
                LOCATION_SHARED_PREFERENCE, Context.MODE_PRIVATE)
            locationSharedPreference.edit().putString(LOCATION_KEY, location.get()?.trim()).apply()
            navigate()
        }
    }

    /**
     * Triggers navigation to ForecastFragment. Callback method, called when the user clicks
     * CANCEL button.
     */
    fun onCancelClick() {
        navigate()
    }

    /**
     * Sets the value of mutable live data to true. This changes the value of the live data which
     * is observed in Fragment and does navigation if value is set to true.
     */
    private fun navigate() {
        _navigate.value = true
    }

    /**
     * Sets mutable live data to false to avoid false positives.
     */
    fun doneNavigating() {
        _navigate.value = false
    }


    /**
     * Factory for constructing LocationViewModel with parameter. Boilerplate code.
     */
    class Factory(val app : Application) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass : Class<T>): T {
            if (modelClass.isAssignableFrom(LocationViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return LocationViewModel(app) as T
            }
            throw IllegalArgumentException("Unable to construct viewmodel")
        }
    }
}
