package com.example.civichub

import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject

class IssueDetailsActivity : AppCompatActivity() {

    private lateinit var title: TextView
    private lateinit var description: TextView
    private lateinit var image: ImageView
    private lateinit var statusMessage: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_issue_details)

        title = findViewById(R.id.title)
        description = findViewById(R.id.description)
        image = findViewById(R.id.descriptionImage)
        statusMessage = findViewById(R.id.statusMessage)

        val issueId = intent.getStringExtra("issueId")
        var jsonObjectResponse: JSONObject

        val queue = Volley.newRequestQueue(this.applicationContext)
        val url = "http://10.0.2.2:5000/api/issue/getIssueWithStatesDetails/$issueId"

        // Request a string response from the provided URL.
        val request = JsonObjectRequest(
            Request.Method.GET, url, null,
            { response ->
                // Display the first 500 characters of the response string.
                jsonObjectResponse = response
                title.text = jsonObjectResponse.getString("title")
                description.text = jsonObjectResponse.getString("description")
                val photosArray = jsonObjectResponse.getJSONArray("photos")
                if (photosArray.length() >= 1){
                    val imageBytes = Base64.decode(photosArray[0] as String, 0)
                    val imageBitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                    image.setImageBitmap(imageBitmap)
                }
                statusMessage.text = jsonObjectResponse.getJSONObject("lastIssueState").getString("message")

            },
            { error ->
                Log.d("Issues", error.toString())
                error.printStackTrace()
            })


        // Add the request to the RequestQueue.
        queue.add(request)

    }
}