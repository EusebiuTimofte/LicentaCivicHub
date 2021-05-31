package com.example.civichub.ui.main

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.civichub.FollowingAdapter
import com.example.civichub.ItemDecorationFollowing
import com.example.civichub.R
import org.json.JSONArray
import org.json.JSONObject

class PageViewModel : ViewModel() {

    private val _index = MutableLiveData<Int>()
    val text: LiveData<String> = Transformations.map(_index) {
        "Hello world from section: $it"
    }

    val users: MutableLiveData<JSONArray> = MutableLiveData()

    fun setIndex(index: Int, context: Context) {
        _index.value = index
        var requestUrl = "http://10.0.2.2:5000/api/gamification/GetUsersTop/$index"
//        when (index) {
//            1 -> requestUrl = "http://10.0.2.2:5000/api/gamification/GetUsersTop"
//            2 -> requestUrl = "http://10.0.2.2:5000/api/gamification/GetUsersTop"
//            3 -> requestUrl = "http://10.0.2.2:5000/api/gamification/GetUsersTop"
//            else -> requestUrl = "http://10.0.2.2:5000/api/gamification/GetUsersTop"
//        }
        val queue = Volley.newRequestQueue(context)
        val topRequest = StringRequest(
            Request.Method.GET, requestUrl,
            {
                // Display the first 500 characters of the response string.
                users.value = JSONArray(it)
            },
            { error ->

            })
        queue.add(topRequest)
    }

}