package com.example.civichub

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.View
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.marginTop
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject

class IssueDetailsActivity : AppCompatActivity() {

    private lateinit var constraintLayout: ConstraintLayout
    private lateinit var title: TextView
    private lateinit var description: TextView
    private lateinit var image: ImageView
    private lateinit var statusMessage: TextView
    private lateinit var sharedPref : SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_issue_details)

        sharedPref = getSharedPreferences(getString(R.string.shared_preferences_file), Context.MODE_PRIVATE)
        constraintLayout = findViewById(R.id.constraintLayout)
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

                //add buttons

                if (jsonObjectResponse.getJSONObject("lastIssueState").getInt("type") == 0 &&
                        sharedPref.getInt(getString(R.string.logged_user_type), -1) == 3){

                    val approveButton = Button(this)
                    approveButton.text = getString(R.string.approve_issue)
                    approveButton.id = View.generateViewId()
                    constraintLayout.addView(approveButton)
                    val approveButtonLayoutParams = approveButton.layoutParams as ConstraintLayout.LayoutParams
                    approveButtonLayoutParams.topToBottom = statusMessage.id
                    approveButtonLayoutParams.startToStart = constraintLayout.id
                    approveButtonLayoutParams.marginStart = 48
                    approveButtonLayoutParams.topMargin = 20
                    approveButton.requestLayout()

                    val revokeButton = Button(this)
                    revokeButton.text = getString(R.string.revoke_issue)
                    constraintLayout.addView(revokeButton)
                    val revokeButtonLayoutParams = revokeButton.layoutParams as ConstraintLayout.LayoutParams
                    revokeButtonLayoutParams.topToBottom = approveButton.id
                    revokeButtonLayoutParams.startToStart = constraintLayout.id
                    revokeButtonLayoutParams.marginStart = 48
                    revokeButtonLayoutParams.topMargin = 20
                    revokeButton.requestLayout()

                    approveButton.setOnClickListener {
                        val urlApprove = "http://10.0.2.2:5000/api/issueState/firstApprovalGiven/$issueId"
                        val requestApprove = JsonObjectRequest(
                            Request.Method.POST, urlApprove, null,
                            {
                                recreate()
                            },
                            {
                                Toast.makeText(this, "Error approve", Toast.LENGTH_LONG).show()
                            })
                        queue.add(requestApprove)
                    }

                    revokeButton.setOnClickListener {
                        val urlRevoke = "http://10.0.2.2:5000/api/issueState/revoke/$issueId"
                        val requestRevoke = JsonObjectRequest(
                            Request.Method.POST, urlRevoke, null,
                            {
                                recreate()
                            },
                            {
                                Toast.makeText(this, "Error revoke", Toast.LENGTH_LONG).show()
                            })
                        queue.add(requestRevoke)
                    }
                }

            },
            { error ->
                Log.d("Issues", error.toString())
                error.printStackTrace()
            })


        // Add the request to the RequestQueue.
        queue.add(request)


    }
}