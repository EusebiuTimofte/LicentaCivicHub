package com.example.civichub

import android.app.Activity
import android.content.Intent
import android.content.Intent.EXTRA_ALLOW_MULTIPLE
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import androidx.core.content.ContentProviderCompat.requireContext

class NewIssueFormActivity : AppCompatActivity() {

    private lateinit var addImagineButton: Button
    val REQUEST_IMAGE_GET = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_issue_form)
        addImagineButton = findViewById(R.id.pickImagesButton)

        addImagineButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                type = "image/*"
//                putExtra(EXTRA_ALLOW_MULTIPLE, true)
            }
            if (intent.resolveActivity(packageManager) != null) {
                startActivityForResult(intent, REQUEST_IMAGE_GET)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_IMAGE_GET && resultCode == Activity.RESULT_OK && data!=null) {
            val thumbnail: Bitmap = data.getParcelableExtra("data")
            val fullPhotoUri: Uri? = data.data
            // Do work with photo saved at fullPhotoUri
            if (fullPhotoUri != null){
                val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    ImageDecoder.decodeBitmap(ImageDecoder.createSource(contentResolver, fullPhotoUri))
                } else {
                    MediaStore.Images.Media.getBitmap(contentResolver, fullPhotoUri)
                }
            }

        }
    }

    //fac image pickerul
    //fac post la add issue


}