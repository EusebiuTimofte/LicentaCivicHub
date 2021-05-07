package com.example.civichub

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.ContactsContract
import android.util.Log
import android.view.MenuItem
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.NavUtils
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.civichub.ui.login.LoginActivity
import org.json.JSONException
import org.json.JSONObject
import java.nio.charset.Charset

class RegisterActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        val registerButton = findViewById<Button>(R.id.registerButton)
        val emailInput = findViewById<TextView>(R.id.editTextTextEmailAddress)
        val passwordInput1 = findViewById<TextView>(R.id.editTextTextPassword)
        val passwordInput2 = findViewById<TextView>(R.id.editTextTextPassword2)

        registerButton.setOnClickListener {
            it.isEnabled = false
            // Instantiate the RequestQueue.
            val queue = Volley.newRequestQueue(this)
            val url = "http://10.0.2.2:5000/api/auth/register"
            var jsonBody = JSONObject()
            jsonBody.put("mail", emailInput.text)
            jsonBody.put("password", passwordInput1.text)
            val jsonText = jsonBody.toString()

            // Request a json response from the provided URL.
            val jsonObjectRequest = JsonObjectRequest(
                Request.Method.POST, url, jsonBody,
                { response ->
                    Toast.makeText(this, "registered", Toast.LENGTH_SHORT).show()
                    Log.d("register", response.toString())
                    val intent = Intent(this, LoginActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                },
                { error ->
                    Log.d("error register", error.toString())
                    Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show()

               }
            )

            // Add the request to the RequestQueue.
            queue.add(jsonObjectRequest)
        }
    }
}