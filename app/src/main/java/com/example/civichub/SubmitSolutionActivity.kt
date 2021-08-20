package com.example.civichub

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream

class SubmitSolutionActivity : AppCompatActivity() {

    private lateinit var descriptionInput: TextView
    private lateinit var addImagesButton: Button
    private lateinit var imageView: ImageView
    private lateinit var submitButton: Button
    private lateinit var base64Codes: MutableList<String>
    private lateinit var issueId: String
    private lateinit var userGuid: String

    val REQUEST_IMAGE_GET = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_submit_solution)

        val sharedPref = getSharedPreferences(getString(R.string.shared_preferences_file), Context.MODE_PRIVATE)
        userGuid = sharedPref.getString(getString(R.string.logged_user_id), "")!!

        descriptionInput = findViewById(R.id.descriptionEditText)
        addImagesButton = findViewById(R.id.addImagesButton)
        imageView = findViewById(R.id.imageView)
        submitButton = findViewById(R.id.submitSolutionButton)
        base64Codes = mutableListOf()

        addImagesButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                type = "image/*"
            }
            if (intent.resolveActivity(packageManager) != null) {
                startActivityForResult(intent, REQUEST_IMAGE_GET)
            }
        }

        issueId = intent.getStringExtra("issueId")!!

        submitButton.setOnClickListener {
            val queue = Volley.newRequestQueue(this.applicationContext)
            val url = "http://10.0.2.2:5000/api/issueState/solutionGiven"

            val jsonBody = JSONObject()
            jsonBody.put("issueId", issueId)
            jsonBody.put("messageFromAuthorities", descriptionInput.text)
            jsonBody.put("photos", JSONArray(base64Codes))
            jsonBody.put("userId", userGuid)

            //Log.d("jsonBody", jsonBody.toString(2))
            // Request a string response from the provided URL.
            val request = JsonObjectRequest(
                Request.Method.POST, url,jsonBody,
                { response ->
                    Log.d("asd", "asd")
//                    val intentMap = Intent(this, CustomMapActivity::class.java)
//                    intentMap.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
//                    startActivity(intentMap)
                    setResult(Activity.RESULT_OK)
                    finish()
                },
                { error ->
                    error.printStackTrace()
                    Toast.makeText(this, "Error", Toast.LENGTH_LONG).show()
//                    val intentMap = Intent(this, CustomMapActivity::class.java)
//                    intentMap.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
//                    startActivity(intentMap)
                    setResult(Activity.RESULT_CANCELED)
                    finish()
                })


            // Add the request to the RequestQueue.
            queue.add(request)
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_IMAGE_GET && resultCode == Activity.RESULT_OK && data != null) {

            for (i in 0 until data.clipData!!.itemCount) {
                val fullPhotoUri: Uri = data.clipData!!.getItemAt(i).uri
                val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    ImageDecoder.decodeBitmap(
                        ImageDecoder.createSource(
                            contentResolver,
                            fullPhotoUri
                        )
                    )
                } else {
                    MediaStore.Images.Media.getBitmap(contentResolver, fullPhotoUri)
                }
                imageView.setImageBitmap(bitmap)
                val byteArrayOutputStream = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
                val byteArray: ByteArray = byteArrayOutputStream.toByteArray()
                base64Codes.add(Base64.encodeToString(byteArray, Base64.DEFAULT))
            }
        }
    }
}