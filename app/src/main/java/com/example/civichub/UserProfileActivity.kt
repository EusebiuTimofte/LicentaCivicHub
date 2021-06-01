package com.example.civichub

import android.content.Context
import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.civichub.ui.login.LoginActivity
import kotlinx.android.synthetic.main.activity_new_issue_form.*
import org.json.JSONObject
import kotlin.properties.Delegates

class UserProfileActivity : AppCompatActivity() {

    private lateinit var mailLabel: TextView
    private lateinit var rankLabel: TextView
    private lateinit var pointsLabel: TextView
    private lateinit var progressBarEl: ProgressBar
    private lateinit var badgeImage: ImageView
//    private lateinit var logoutButton: Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_profile)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val sharedPref =
            getSharedPreferences(getString(R.string.shared_preferences_file), Context.MODE_PRIVATE)
        val userMail = sharedPref.getString(getString(R.string.logged_user_mail), "")

        mailLabel = findViewById(R.id.mailTextView)
        rankLabel = findViewById(R.id.rankTextView)
        pointsLabel = findViewById(R.id.pointsTextView)
        progressBarEl = findViewById(R.id.progressBar)
        badgeImage = findViewById(R.id.badgeImageView)
//        logoutButton = findViewById(R.id.logoutButton)

        mailLabel.text = userMail

        val queue = Volley.newRequestQueue(this.applicationContext)
        //set rank label and icon
        val userId = sharedPref.getString(getString(R.string.logged_user_id), "")
        val getBadgeUrl = "http://10.0.2.2:5000/api/gamification/GetBadgeNumber/$userId"
        val getBadgeRequest = JsonObjectRequest(
            Request.Method.GET, getBadgeUrl, null,
            {
                Log.d("points_limit", it.toString(2))
                rankLabel.text = getString(R.string.rank).format(it.getInt("badge"))
                setBadge(it.getInt("badge"))
                //setez textul la label ca fiind points / limit
                //setez la progress bar points/limit progresu
                pointsLabel.text = getString(R.string.progress)
                    .format(it.getInt("points"), it.getInt("limit"))
                progressBarEl.progress = (((it.getInt("points").toDouble()) / (it.getInt("limit")
                    .toDouble())) * 100).toInt()
            },
            { error ->

            })
        queue.add(getBadgeRequest)

//        logoutButton.setOnClickListener {
//            with(sharedPref.edit()) {
//                putString(getString(R.string.logged_user_mail), "")
//                apply()
//            }
//            val loginIntent = Intent(this.applicationContext, LoginActivity::class.java)
//            loginIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
//            startActivity(loginIntent)
//        }
    }

    fun setBadge(badgeNr: Int) {
        when (badgeNr) {
            1 -> badgeImage.setImageResource(R.drawable.badge1)
            2 -> badgeImage.setImageResource(R.drawable.badge2)
            3 -> badgeImage.setImageResource(R.drawable.badge3)
            4 -> badgeImage.setImageResource(R.drawable.badge4)
            5 -> badgeImage.setImageResource(R.drawable.badge5)
            6 -> badgeImage.setImageResource(R.drawable.badge6)
            7 -> badgeImage.setImageResource(R.drawable.badge7)
            8 -> badgeImage.setImageResource(R.drawable.badge8)
            9 -> badgeImage.setImageResource(R.drawable.badge8)
        }


    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}