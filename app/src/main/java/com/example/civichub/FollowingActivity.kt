package com.example.civichub

import android.content.Context
import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONArray
import org.json.JSONObject

class FollowingActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdapter: RecyclerView.Adapter<*>
    private lateinit var viewManager: LinearLayoutManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_following)


        supportActionBar?.setDisplayHomeAsUpEnabled(true)


        val queue = Volley.newRequestQueue(this.applicationContext)

        //set follow button on click listeners
        val sharedPref = getSharedPreferences(getString(R.string.shared_preferences_file), Context.MODE_PRIVATE)
        val userId = sharedPref.getString(getString(R.string.logged_user_id), "")
        val followUrl = "http://10.0.2.2:5000/api/Follow/getAllByUser/$userId"
        val followRequest = StringRequest(
            Request.Method.GET, followUrl,
            {
                // Display the first 500 characters of the response string.
                val followArray = JSONArray(it)
                viewManager = LinearLayoutManager(this.applicationContext)
                val aux = mutableListOf<JSONObject>()
                for (i in 0 until followArray.length()) {
                    aux.add(followArray.getJSONObject(i))
                }

                viewAdapter = FollowingAdapter(aux.toTypedArray(), this.applicationContext)

                recyclerView = findViewById<RecyclerView>(R.id.followingRecyclerView).apply {
                    setHasFixedSize(true)
                    layoutManager = viewManager
                    adapter = viewAdapter
                    addItemDecoration(ItemDecorationFollowing(2))
                }

            },
            { error ->

            })
        queue.add(followRequest)
    }

//    override fun onOptionsItemSelected(item: MenuItem): Boolean {
//        Log.d("back button", item.itemId.toString())
//        return when (item.itemId) {
//            R.id.home -> {
//                this.finish()
//                true
//            }
//            else -> super.onOptionsItemSelected(item)
//        }
//    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}