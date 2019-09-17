package com.ananth.weatherdemo

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.ananth.weatherdemo.ui.main.ForecastFragment

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState : Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)
    }

}
