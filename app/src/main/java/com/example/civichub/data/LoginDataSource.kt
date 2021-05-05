package com.example.civichub.data

import android.content.Context
import android.util.Log
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.civichub.data.model.LoggedInUser
import org.json.JSONObject
import java.io.IOException
import java.util.*


/**
 * Class that handles authentication w/ login credentials and retrieves user information.
 */
class LoginDataSource {

    fun login(
        username: String,
        password: String,
        context: Context,
        callback: (result: Result<LoggedInUser>) -> Unit
    ) {
        try {
            // TODO: url corect, modific clasa LoggedInUser cum trebuie
            // TODO: handle loggedInUser authentication
            // Instantiate the RequestQueue.
            val queue = Volley.newRequestQueue(context)
            val url = "http://10.0.2.2:5000/api/auth/login"
            val jsonBody = JSONObject()
            jsonBody.put("mail", username)
            jsonBody.put("password", password)

            // Request a string response from the provided URL.
            val request = JsonObjectRequest(
                Request.Method.POST, url, jsonBody,
                { response ->
                    // Display the first 500 characters of the response string.
                    Log.d("Login", response.toString(2))
                    val fakeUser = LoggedInUser(username, "Jane Doe", mail = response["mail"] as String, token = response["token"] as String)
                    callback(Result.Success(fakeUser))

                },
                { error ->
                    Log.d("Login", error.toString())
                    error.printStackTrace()
                })


            // Add the request to the RequestQueue.
            queue.add(request)

        } catch (e: Throwable) {
            callback(Result.Error(IOException("Error logging in", e)))
        }
    }

    fun logout() {
        // TODO: revoke authentication
    }
}