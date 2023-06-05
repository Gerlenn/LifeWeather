package com.lifeweather

import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.lifeweather.data.WeatherModel
import com.lifeweather.screens.DialogSearch
import com.lifeweather.screens.MainCard
import com.lifeweather.screens.TabLayout
import com.lifeweather.ui.theme.LifeWeatherTheme
import org.json.JSONObject

const val API_KEY = "7ce38c55cc96499b87d185506231905"
const val ERR_MSG = "Error get weather"

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LifeWeatherTheme {
                val daysList = remember {
                    mutableStateOf(listOf<WeatherModel>())
                }
                val dialogState = remember {
                    mutableStateOf(false)
                }
                val currentDay = remember {
                    mutableStateOf(WeatherModel(
                        "",
                        "",
                        "0",
                        "",
                        "",
                        "0",
                        "0",
                        ""
                    )
                    )
                }
                if (dialogState.value) {
                    DialogSearch(dialogState, onSubmit = {
                        getData(it, this, daysList, currentDay)
                    })
                }
                getData("London", this, daysList, currentDay)
                Image(
                    painter = painterResource(id = R.drawable.weather_back),
                    contentDescription = "background",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.FillBounds
                )
                Column {
                    MainCard(currentDay, onClickSync = {
                        getData("London", this@MainActivity, daysList, currentDay)
                    },
                        onClickSearch = {
                            dialogState.value = true
                        }
                    )
                    TabLayout(daysList, currentDay)
                }
            }
        }
    }
}

private fun getData(
    city: String, context: Context,
    daysList: MutableState<List<WeatherModel>>,
    currentDay: MutableState<WeatherModel>,
) {
    val url = "https://api.weatherapi.com/v1/forecast.json?key=$API_KEY" +
            "&q=$city" +
            "&days=" +
            "10" +
            "&aqi=no&alerts=no"
    val queue = Volley.newRequestQueue(context)
    val request = StringRequest(
        Request.Method.GET,
        url,
        { response ->
            val list = getWeatherByDays(response)
            currentDay.value = list[0]
            daysList.value = list
        },
        {
            Toast.makeText(context, ERR_MSG, Toast.LENGTH_SHORT).show()
        }
    )
    queue.add(request)
}

private fun getWeatherByDays(response: String): List<WeatherModel> {
    if (response.isEmpty()) return listOf()
    val list = arrayListOf<WeatherModel>()
    val mainObject = JSONObject(response)
    val city = mainObject.getJSONObject("location").getString("name")
    val days = mainObject.getJSONObject("forecast").getJSONArray("forecastday")

    for (i in 0 until days.length()) {
        val item = days[i] as JSONObject
        list.add(
            WeatherModel(
                city,
                item.getString("date"),
                "",
                item.getJSONObject("day")
                    .getJSONObject("condition")
                    .getString("text"),
                item.getJSONObject("day")
                    .getJSONObject("condition")
                    .getString("icon"),
                item.getJSONObject("day")
                    .getString("maxtemp_c").toFloat().toInt().toString(),
                item.getJSONObject("day")
                    .getString("mintemp_c").toFloat().toInt().toString(),
                item.getJSONArray("hour").toString()
            )
        )
    }
    list[0] = list[0].copy(
        time = mainObject.getJSONObject("current").getString("last_updated"),
        currentTemp = mainObject.getJSONObject("current").getString("temp_c")
    )
    return list
}