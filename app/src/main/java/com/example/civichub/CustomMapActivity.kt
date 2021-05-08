package com.example.civichub

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import org.json.JSONArray
import org.json.JSONObject

class CustomMapActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnInfoWindowClickListener {

    private lateinit var mMap: GoogleMap
    private lateinit var jsonArray: JSONArray

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_custom_map)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.customMap) as SupportMapFragment
        mapFragment.getMapAsync(this)

    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        Log.d("mmap", "CustomMap")
        mMap = googleMap

        val queue = Volley.newRequestQueue(this.applicationContext)
        val url = "http://10.0.2.2:5000/api/issue/all"

        // Request a string response from the provided URL.
        val request = StringRequest(
            Request.Method.GET, url,
            { response ->
                // Display the first 500 characters of the response string.
                jsonArray = JSONArray(response)
                Log.d("json array", jsonArray.toString(2))
                for (i in 0 until jsonArray.length()) {
                    val jsonObject = jsonArray.getJSONObject(i)
                    val latitude = jsonObject.optString("latitude").toDouble()
                    val longitude = jsonObject.optString("longitude").toDouble()
                    val id = jsonObject.optString("id") as String
                    var marker = mMap.addMarker(MarkerOptions().position(LatLng(latitude, longitude)).title(jsonObject.getString("title")).snippet(jsonObject.getString("description")))
                    marker.tag = jsonObject
                }


            },
            { error ->
                Log.d("Issues", error.toString())
                error.printStackTrace()
            })


        // Add the request to the RequestQueue.
        queue.add(request)

        mMap.setOnInfoWindowClickListener(this)
        val sharedPref = getSharedPreferences(getString(R.string.shared_preferences_file), Context.MODE_PRIVATE)
        //get map target and zoom from preferences and move camera
        val latitude = sharedPref.getFloat(getString(R.string.camera_position_latitude),
            (-100.0).toFloat()
        ).toDouble()
        val longitude = sharedPref.getFloat(getString(R.string.camera_position_longitude),
            (-100.0).toFloat()
        ).toDouble()
        val zoom = sharedPref.getFloat(getString(R.string.camera_position_zoom), (-100.0).toFloat())
        if (! (latitude == -100.0 || longitude == -100.0 || zoom == (-100.0).toFloat())){
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(latitude, longitude), zoom))
        }
    }

    override fun onInfoWindowClick(marker: Marker) {

        val markerTag = marker.tag as JSONObject

        val intent = Intent(this.applicationContext, IssueDetailsActivity::class.java).apply {
            putExtra("issueId", markerTag.getString("id"))
        }
        startActivity(intent)
    }

    override fun onPause() {
        super.onPause()

        //save map target and zoom in shared preferences
        val sharedPref = getSharedPreferences(getString(R.string.shared_preferences_file), Context.MODE_PRIVATE)
        with(sharedPref.edit()){
            putFloat(getString(R.string.camera_position_latitude),
                mMap.cameraPosition.target.latitude.toFloat()
            )
            putFloat(getString(R.string.camera_position_longitude),
                mMap.cameraPosition.target.longitude.toFloat()
            )
            putFloat(getString(R.string.camera_position_zoom), mMap.cameraPosition.zoom)
            apply()
        }
    }
}