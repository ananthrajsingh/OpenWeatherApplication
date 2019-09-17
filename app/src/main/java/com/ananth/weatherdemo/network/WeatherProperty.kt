package com.ananth.weatherdemo.network

import com.squareup.moshi.Json

/**
 * Created by ananthrajsingh on 2019-09-17
 */

/**
 * The model class that we use throughout the application to store the information received
 * from the server.
 */
data class WeatherProperty(
    val weather: List<Weather>,
    val main: Main,
    @Json(name = "dt") val utcTime: Long
)
/*
 * The forecast endpoint does not return array, instead an object which contains "list" array.
 * Therefore, we need this approach.
 */
data class WeatherPropertyList(
    val list: List<WeatherProperty>
)
data class Weather(
    @Json(name = "main") val description: String,
    val icon: String
)

data class Main(
    @Json(name = "temp") val temperature: Double,
    @Json(name = "temp_min") val minTemperature: Double,
    @Json(name = "temp_max") val maxTemperature: Double
)