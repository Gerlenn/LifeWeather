package com.lifeweather.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.pagerTabIndicatorOffset
import com.google.accompanist.pager.rememberPagerState
import com.lifeweather.R
import com.lifeweather.data.WeatherModel
import com.lifeweather.ui.theme.BlueLight
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

@Composable
fun MainCard(currentDay: MutableState<WeatherModel>, onClickSync: () -> Unit, onClickSearch: () -> Unit) {
    Column(
        modifier = Modifier.padding(8.dp),
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            backgroundColor = BlueLight,
            elevation = 0.dp,
            shape = RoundedCornerShape(10.dp)) {
            Column(modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally

            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(
                        modifier = Modifier.padding(top = 15.dp, start = 10.dp),
                        text = currentDay.value.time,
                        style = TextStyle(fontSize = 15.sp),
                        color = Color.White
                    )
                    AsyncImage(
                        model = "https:" + currentDay.value.icon,
                        contentDescription = "status weather",
                        modifier = Modifier
                            .size(50.dp)
                            .padding(top = 10.dp, end = 6.dp))
                }
                Text(
                    text = currentDay.value.city,
                    style = TextStyle(fontSize = 24.sp),
                    color = Color.White)
                Text(
                    text = if (currentDay.value.currentTemp.isNotEmpty())
                        "${currentDay.value.currentTemp.toFloat().toInt()}°C"
                    else
                        "${currentDay.value.maxTemp}°C/${currentDay.value.minTemp}"
                        ,
                    style = TextStyle(fontSize = 60.sp),
                    color = Color.White)
                Text(
                    text = currentDay.value.condition,
                    style = TextStyle(fontSize = 16.sp),
                    color = Color.White)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween) {
                    IconButton(
                        onClick = {
                            onClickSearch.invoke()
                        }) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_search),
                            contentDescription = "search",
                            tint = Color.White)
                    }
                    Text(
                        modifier = Modifier,
                        text = "${currentDay.value.maxTemp.toFloat().toInt()}°C/" +
                                "${currentDay.value.minTemp.toFloat().toInt()}°C",
                        style = TextStyle(fontSize = 16.sp),
                        color = Color.White)
                    IconButton(
                        onClick = {
                            onClickSync.invoke()
                        }) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_sync),
                            contentDescription = "synchronized",
                            tint = Color.White
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalPagerApi::class)
@Composable
fun TabLayout(daysList: MutableState<List<WeatherModel>>, currentDay: MutableState<WeatherModel>) {
    val tabList = listOf("Hours", "Days")
    val pagerState = rememberPagerState()
    val tabIndex = pagerState.currentPage
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .padding(
                start = 8.dp,
                end = 8.dp
            )
            .clip(RoundedCornerShape(10.dp)),

        ) {
        TabRow(
            selectedTabIndex = tabIndex,
            indicator = { position ->
                TabRowDefaults.Indicator(Modifier.pagerTabIndicatorOffset(pagerState, position))
            },
            backgroundColor = BlueLight,
            contentColor = Color.White
        ) {
            tabList.forEachIndexed { index, text ->
                Tab(
                    selected = false,
                    onClick = {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(index)
                        }
                    }, text = {
                        Text(
                            text = text,
                            style = TextStyle(fontSize = 18.sp)
                        )
                    })
            }
        }
        HorizontalPager(
            count = tabList.size,
            state = pagerState,
            modifier = Modifier.weight(1.0f)
        ) { index ->
            val list = when(index){
                0 -> getWeatherByHours(currentDay.value.hours)
                1 -> daysList.value
                else -> daysList.value
            }
            MainList(list, currentDay)
        }
    }
}

private fun getWeatherByHours(hours: String): List<WeatherModel> {
    if (hours.isEmpty()) return listOf()
    val hoursArray = JSONArray(hours)
    val list = ArrayList<WeatherModel>()
    for (i in 0 until hoursArray.length()) {
        val item = hoursArray[i] as JSONObject
        list.add(
            WeatherModel(
                "",
                item.getString("time"),
                item.getString("temp_c").toFloat().toInt().toString() + "°C",
                item.getJSONObject("condition").getString("text"),
                item.getJSONObject("condition").getString("icon"),
                "",
                "",
                ""
            )
        )
    }
    return list
}

