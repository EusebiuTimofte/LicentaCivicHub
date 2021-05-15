package com.example.civichub

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.Intent.EXTRA_ALLOW_MULTIPLE
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.location.Geocoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.lang.Exception
import java.util.*


class NewIssueFormActivity : AppCompatActivity() {

    private lateinit var addImagineButton: Button
    private lateinit var imagePicked: ImageView
    private lateinit var base64Codes: MutableList<String>
    private lateinit var addIssueButton: Button
    private lateinit var titleValue:TextView
    private lateinit var descriptionValue: TextView
    private lateinit var addressValue: TextView
    val REQUEST_IMAGE_GET = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_issue_form)
        addImagineButton = findViewById(R.id.pickImagesButton)

        addImagineButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                type = "image/*"
                putExtra(EXTRA_ALLOW_MULTIPLE, true)
            }
            if (intent.resolveActivity(packageManager) != null) {
                startActivityForResult(intent, REQUEST_IMAGE_GET)
            }
        }

        addressValue = findViewById(R.id.addressTextViewValue)
        val geocoder: Geocoder = Geocoder(this, Locale.getDefault())
        try{
            val addresses = geocoder.getFromLocation(intent.getDoubleExtra("Latitude", -100.0), intent.getDoubleExtra("Longitude", -100.0), 1)
            addressValue.text = addresses.get(0).getAddressLine(0)

        }catch (e: Exception){
            addressValue.text = getString(R.string.error_fetching_address)
        }
        imagePicked = findViewById(R.id.imageView)
        base64Codes = mutableListOf()
        addIssueButton = findViewById(R.id.addIssueButton)
        titleValue = findViewById(R.id.titleValue)
        descriptionValue = findViewById(R.id.descriptionEditText)
        addIssueButton.setOnClickListener {
            val sharedPref = getSharedPreferences(getString(R.string.shared_preferences_file), Context.MODE_PRIVATE)
            val queue = Volley.newRequestQueue(this.applicationContext)
            val url = "http://10.0.2.2:5000/api/issue"

            val jsonBody = JSONObject()
            jsonBody.put("title", titleValue.text)
            jsonBody.put("description", descriptionValue.text)
            jsonBody.put("userId", sharedPref.getString(getString(R.string.logged_user_id), ""))
            jsonBody.put("latitude", intent.getDoubleExtra("Latitude", -100.0))
            jsonBody.put("longitude", intent.getDoubleExtra("Longitude", -100.0))
            jsonBody.put("photos", JSONArray(base64Codes))

            Log.d("jsonBody", jsonBody.toString(2))
            // Request a string response from the provided URL.
            val request = JsonObjectRequest(
                Request.Method.POST, url,jsonBody,
                { response ->
                    Log.d("asd", "asd")
                    val intentMap = Intent(this, CustomMapActivity::class.java)
                    intentMap.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intentMap)
                },
                { error ->
                    error.printStackTrace()
                    Toast.makeText(this, "Error", Toast.LENGTH_LONG).show()
                    val intentMap = Intent(this, CustomMapActivity::class.java)
                    intentMap.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intentMap)
                })


            // Add the request to the RequestQueue.
            queue.add(request)
        }


    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_IMAGE_GET && resultCode == Activity.RESULT_OK && data!=null) {

            for (i in 0 until data.clipData!!.itemCount){
                val fullPhotoUri: Uri = data.clipData!!.getItemAt(i).uri
                val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    ImageDecoder.decodeBitmap(ImageDecoder.createSource(contentResolver, fullPhotoUri))
                } else {
                    MediaStore.Images.Media.getBitmap(contentResolver, fullPhotoUri)
                }
                imagePicked.setImageBitmap(bitmap)
                val byteArrayOutputStream = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
                val byteArray: ByteArray = byteArrayOutputStream.toByteArray()
                base64Codes.add(Base64.encodeToString(byteArray, Base64.DEFAULT))
            }

        }
    }

    //fac image pickerul
    //fac post la add issue


}