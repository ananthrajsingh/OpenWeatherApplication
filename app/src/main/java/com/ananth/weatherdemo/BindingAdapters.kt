package com.ananth.weatherdemo

import android.widget.ImageView
import androidx.core.net.toUri
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions

/*
 * Ideally should be in a Constants.kt file.
 */
const val LOCATION_SHARED_PREFERENCE = "location-shared-preference"
const val LOCATION_KEY = "location"
const val DEFAULT_LOCATION_VALUE = "default-location"
const val WEATHER_REFRESH_INTERVAL: Long = 1000 * 60 * 5 // Five Minutes

/**
 * Created by ananthrajsingh on 2019-09-17
 * This will help get the image from the server on the based on the the icon code that we
 * receive. The ImageViews have the tag app:iconCode which listen to the icon code received
 * from the server. This binding method takes that code and sets corresponding image to that
 * ImageView.
 */
@BindingAdapter("iconCode")
fun bindImage(imgView : ImageView, iconCode : String?) {
    iconCode?.let {
        val imgUrl = "http://openweathermap.org/img/w/$iconCode.png"
        val imgUri = imgUrl.toUri().buildUpon().build()
        Glide.with(imgView.context)
            .load(imgUri)
            .apply(
                RequestOptions()
                    .placeholder(R.drawable.loading_animation)
                    .error(R.drawable.ic_broken_image))
            .into(imgView)
    }
}