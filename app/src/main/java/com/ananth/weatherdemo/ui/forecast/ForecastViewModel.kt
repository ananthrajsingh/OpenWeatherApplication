package com.ananth.weatherdemo.ui.forecast

import android.app.Application
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.lifecycle.*
import com.ananth.weatherdemo.DEFAULT_LOCATION_VALUE
import com.ananth.weatherdemo.LOCATION_KEY
import com.ananth.weatherdemo.LOCATION_SHARED_PREFERENCE
import com.ananth.weatherdemo.WEATHER_REFRESH_INTERVAL
import com.ananth.weatherdemo.network.WeatherProperty
import com.ananth.weatherdemo.network.getWeatherApiService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.lang.Exception
import java.util.*
import kotlin.collections.ArrayList

class ForecastViewModel(application: Application) : AndroidViewModel(application) {

    private val weekdayNames =
        arrayOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")
    /*
     * ---------------------------------------------------------------------------------------------
     * Internally, we use a MutableLiveData, because we will be updating them with new values.
     * The external LiveData interface to the property is immutable, so only this class can modify.
     * ---------------------------------------------------------------------------------------------
     */

    /* Holds today's weather */
    private val _currentDayWeather = MutableLiveData<WeatherProperty>()
    val currentDayWeather : LiveData<WeatherProperty>
        get() = _currentDayWeather

    /* Holds next five days' weather */
    private val _fiveDayForecast = MutableLiveData<List<WeatherProperty>>()
    val fiveDayForecast : LiveData<List<WeatherProperty>>
        get() = _fiveDayForecast

    /* Holds name of the weekday names starting from tomorrow */
    private val _upcomingDays = MutableLiveData<List<String>>()
    val upcomingDays : LiveData<List<String>>
        get() = _upcomingDays

    /* The city of which the weather needs to be shown */
    private val _currentCity = MutableLiveData<String>()
    val currentCity : LiveData<String>
        get() = _currentCity

    private val _navigate = MutableLiveData<Boolean>()
    val navigate : LiveData<Boolean>
        get() = _navigate

    private val mainHandler = Handler(Looper.getMainLooper())

    private lateinit var weatherRunnable: Runnable

    /* Create a Coroutine scope using a job to be able to cancel when needed */
    private var viewModelJob = Job()

    /* the Coroutine runs using the Main (UI) dispatcher */
    private val coroutineScope = CoroutineScope(viewModelJob + Dispatchers.Main)


    init {
        weatherRunnable = Runnable {
            val locationSharedPreference = application.getSharedPreferences(
                LOCATION_SHARED_PREFERENCE, Context.MODE_PRIVATE)
            _currentCity.value = locationSharedPreference
                .getString(LOCATION_KEY, DEFAULT_LOCATION_VALUE)
            currentCity.value?.let { getWeather(it) }
            mainHandler.postDelayed(weatherRunnable, WEATHER_REFRESH_INTERVAL)
        }
        mainHandler.post(weatherRunnable)
    }

    /**
     * Gets the current day's weather and upcoming days' weather using the WeatherApiService.
     * Handles error returned by the request. Evaluates upcoming days' names.
     */
    fun getWeather(location : String) {
        if (location == DEFAULT_LOCATION_VALUE) return
        coroutineScope.launch {
            /* API Key is exposed, which is a bad practice, I am aware, and makes me uneasy. */
            val getCurrentWeatherPropertyDeferred = getWeatherApiService(getApplication())
                .getCurrentDayWeather(location, "metric",
                    "6754ebe8a500871444c118be542b6fdc")
            try {
                val currentWeatherProperty = getCurrentWeatherPropertyDeferred.await()
                _currentDayWeather.value = currentWeatherProperty
            } catch (e : HttpException) {
                e.printStackTrace()
                when {
                    e.code() == 404 -> Toast.makeText(getApplication(),
                        "Please Check Location Name", Toast.LENGTH_LONG).show()
                    e.code() == 504 -> Toast.makeText(getApplication(),
                        "Please Check Network Connectivity", Toast.LENGTH_LONG).show()
                    else -> Toast.makeText(getApplication(),
                        "Something Is Not Right", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(getApplication(), "Something Is Not Right", Toast.LENGTH_LONG)
                    .show()
            }

            val getForecast = getWeatherApiService(getApplication()).getForecast(location,
                "metric", "6754ebe8a500871444c118be542b6fdc")
            try {
                val allForecast = getForecast.await()
                _fiveDayForecast.value = getFiveDayWeather(allForecast.list)
            } catch (e : HttpException) {
                e.printStackTrace()
            }
            _upcomingDays.value = getUpcomingDays()
        }
    }

    /**
     * Filters and give one forecast for each day.
     * @param allForecastList List returned by the server which has multiple [WeatherProperty] for
     * each day, since there is a [WeatherProperty] for every 3 hours.
     * @return List of [WeatherProperty] which has only 5 elements, one (first) for each upcoming
     * day.
     */
    private fun getFiveDayWeather(allForecastList : List<WeatherProperty>) : List<WeatherProperty> {
        var todaysDate: Int = getDayOfMonth(System.currentTimeMillis())
        val fiveDayWeather: ArrayList<WeatherProperty> = ArrayList()
        for (currentWeatherProperty: WeatherProperty in allForecastList) {
            if (getDayOfMonth(currentWeatherProperty.utcTime * 1000) == todaysDate + 1) {
                fiveDayWeather.add(currentWeatherProperty)
                todaysDate++
            }
        }
        /*
         * If we get forecast between 12 am and 3 am, we get only next 4 days, for obvious reasons.
         * Thus, adding today's forecast in the beginning.
         * This situation needs to be handles more meticulously. In current state, it is only a
         * hack and gives wrong results during 12 am and 3 am. Wrong? How? Because for first element
         * it should show today's weekday name, but will show tomorrow's and so on.
         */
        if (fiveDayWeather.size == 4) {
            fiveDayWeather.reverse()
            fiveDayWeather.add(allForecastList[0])
            fiveDayWeather.reverse()
        }
        return fiveDayWeather
    }

    /**
     * Gets the weekday names of upcoming days.
     * @return The list which contains next coming day at index one and in increasing order.
     */
    private fun getUpcomingDays() : List<String> {
        var dayOfWeek = getDayOfWeek(System.currentTimeMillis())
        var index = 0
        val upcomingWeekdays: ArrayList<String> = ArrayList()
        while (index < 5) {
            upcomingWeekdays.add(weekdayNames[dayOfWeek - 1])
            dayOfWeek++
            if (dayOfWeek == 8) dayOfWeek = 1
            index++
        }
        return upcomingWeekdays
    }

    private fun getDayOfMonth(timestamp: Long): Int {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timestamp
        return calendar.get(Calendar.DAY_OF_MONTH)
    }

    private fun getDayOfWeek(timestamp: Long) : Int {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timestamp
        return calendar.get(Calendar.DAY_OF_WEEK)
    }

    /**
     * Triggers navigation to the LocationFragment. It is a callback method called when the forward
     * arrow ImageView is clicked.
     */
    fun onLocationEditRequest() {
        _navigate.value = true
    }

    /**
     * Resets the value of navigation LiveData to avoid false positives. Called by the Fragment
     * class.
     */
    fun doneNavigating() {
        _navigate.value = false
    }

    /**
     * When the [ViewModel] is finished, we cancel our coroutine [viewModelJob], which tells the
     * Retrofit service to stop. We also remove the callbacks to stop refreshing weather.
     */
    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
        mainHandler.removeCallbacks(weatherRunnable)
    }

    /**
     * Factory for constructing ForecastViewModel with parameter
     */
    class Factory(val app: Application) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ForecastViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return ForecastViewModel(app) as T
            }
            throw IllegalArgumentException("Unable to construct viewmodel")
        }
    }
}
