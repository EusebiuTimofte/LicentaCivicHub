package com.example.civichub.data

import android.content.ComponentCallbacks
import android.content.Context
import android.util.Log
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.civichub.data.model.LoggedInUser
import java.io.IOException

/**
 * Class that handles authentication w/ login credentials and retrieves user information.
 */
class LoginDataSource {

    fun login(username: String, password: String, context:Context, callback: (result: Result<LoggedInUser>) -> Unit) {
        try {
            // TODO: handle loggedInUser authentication
            // Instantiate the RequestQueue.
            val queue = Volley.newRequestQueue(context)
            val url = "http://10.0.2.2:5000/api/login/"

            // Request a string response from the provided URL.
            val stringRequest = StringRequest(
                Request.Method.GET, url,
                { response ->
                    // Display the first 500 characters of the response string.
                    Log.d("Login", "Response is: ${response.substring(0, 500)}")
                    Log.d("asd","e totto bene")
                    val fakeUser = LoggedInUser(java.util.UUID.randomUUID().toString(), "Jane Doe")
                    callback(Result.Success(fakeUser))

                },
                { error -> Log.d("Login",error.toString())
                    error.printStackTrace()})


            // Add the request to the RequestQueue.
            queue.add(stringRequest)

        } catch (e: Throwable) {
            callback(Result.Error(IOException("Error logging in", e)))
        }
    }

    fun logout() {
        // TODO: revoke authentication
    }
}