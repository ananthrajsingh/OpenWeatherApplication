package com.ananth.weatherdemo.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo
import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Deferred
import okhttp3.Cache
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import okhttp3.OkHttpClient


/**
 * Created by ananthrajsingh on 2019-09-17
 */
private const val BASE_URL = "https://api.openweathermap.org/data/2.5/"
/**
 * Build the Moshi object that Retrofit will be using, making sure to add the Kotlin adapter for
 * full Kotlin compatibility.
 */
private val moshi = Moshi.Builder()
    .add(KotlinJsonAdapterFactory())
    .build()
/**
 * A public interface that exposes the [getCurrentDayWeather] and [getForecast] method
 */
interface WeatherApiService {
    @GET("weather")
    fun getCurrentDayWeather(@Query("q") city : String,
                             @Query("units") unit : String = "metric",
                             @Query("APPID") apiKey : String) : Deferred<WeatherProperty>

    @GET("forecast")
    fun getForecast(@Query("q") cityAndCountry : String,
                    @Query("units") unit : String = "metric",
                    @Query("APPID") apiKey : String) : Deferred<WeatherPropertyList>
}

/**
 * Provides us with the [WeatherApiService] over which we can do our desired operations.
 */
fun getWeatherApiService(context: Context): WeatherApiService {
    /*
     * Retrofit builder to build a retrofit object using a Moshi converter with the Moshi
     * object.
     */
    val retrofit =  Retrofit.Builder()
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .addCallAdapterFactory(CoroutineCallAdapterFactory())
        .client(getOkHttpClient(context))
        .baseUrl(BASE_URL)
        .build()
    return retrofit.create(WeatherApiService::class.java)
}

/**
 * Builds the http client with the capability to hold cache. This will help in showing user
 * some data even when there is not network connection.
 */
fun getOkHttpClient(context: Context): OkHttpClient {
    val cacheSize = (5 * 1024 * 1024).toLong()
    val cache = Cache(context.cacheDir, cacheSize)
    return OkHttpClient.Builder()
        .cache(cache)
        .addInterceptor { chain ->
            var request = chain.request()
            request = if (hasNetwork(context)!!)
                request.newBuilder().header("Cache-Control", "public, max-age=" + 5)
                    .build()
            else
                request.newBuilder().header(
                    "Cache-Control",
                    "public, only-if-cached, max-stale=" + 60 * 60 * 24 * 7)
                    .build()
            chain.proceed(request)
        }
        .build()
}

/**
 * Helps finding whether the phone has network connections
 */
fun hasNetwork(context: Context): Boolean? {
    var isConnected: Boolean? = false // Initial Value
    val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE)
            as ConnectivityManager
    val activeNetwork: NetworkInfo? = connectivityManager.activeNetworkInfo
    if (activeNetwork != null && activeNetwork.isConnected)
        isConnected = true
    return isConnected
}