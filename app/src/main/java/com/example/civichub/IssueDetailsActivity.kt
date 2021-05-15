package com.example.civichub

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.ContactsContract
import android.provider.MediaStore
import android.text.InputType
import android.util.Base64
import android.util.Log
import android.view.View
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.marginTop
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.civichub.data.model.LoggedInUser
import org.json.JSONObject
import java.io.ByteArrayOutputStream

class IssueDetailsActivity : AppCompatActivity() {

    private lateinit var constraintLayout: ConstraintLayout
    private lateinit var title: TextView
    private lateinit var description: TextView
    private lateinit var image: ImageView
    private lateinit var statusMessage: TextView
    private lateinit var sharedPref : SharedPreferences
    private lateinit var issueId : String
    private lateinit var jsonObjectResponse: JSONObject
    private lateinit var queue : RequestQueue
    private lateinit var solutionMessage: TextView
    private lateinit var solutionImage: ImageView
    private lateinit var revokedSolutionJustification: TextView
    private lateinit var addImplementationDetailsButton: Button
    private lateinit var revokedImplementationJustification: TextView
    val SUBMIT_SOLUTION = 1
    val SUBMIT_IMPLEMENTATION = 2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_issue_details)

        sharedPref = getSharedPreferences(getString(R.string.shared_preferences_file), Context.MODE_PRIVATE)
        constraintLayout = findViewById(R.id.constraintLayout)
        title = findViewById(R.id.title)
        description = findViewById(R.id.description)
        image = findViewById(R.id.descriptionImage)
        statusMessage = findViewById(R.id.statusMessage)


        issueId = intent.getStringExtra("issueId")!!

        queue = Volley.newRequestQueue(this.applicationContext)
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

                //add justification in case needed
                if (jsonObjectResponse.getJSONObject("lastIssueState").getInt("type") == 1 &&
                    !jsonObjectResponse.getJSONObject("lastIssueState").isNull("customMessage")){
                        val justificationString = jsonObjectResponse.getJSONObject("lastIssueState").getString("customMessage")
                    revokedSolutionJustification = TextView(this)
                    revokedSolutionJustification.text = justificationString
                    revokedSolutionJustification.id = View.generateViewId()
                    constraintLayout.addView(revokedSolutionJustification)
                    val revokedSolutionJustificationLayoutParams = revokedSolutionJustification.layoutParams as ConstraintLayout.LayoutParams
                    revokedSolutionJustificationLayoutParams.topToBottom = statusMessage.id
                    revokedSolutionJustificationLayoutParams.startToStart = constraintLayout.id
                    revokedSolutionJustificationLayoutParams.leftMargin = 48
                    revokedSolutionJustificationLayoutParams.topMargin = 20
                    revokedSolutionJustification.requestLayout()

                }

                addApproveRevokeButtons()

                //add solution button
                if (jsonObjectResponse.getJSONObject("lastIssueState").getInt("type") == 1 &&
                    sharedPref.getInt(getString(R.string.logged_user_type), -1) == 2) {
                    val addSolutionButton = Button(this)
                    addSolutionButton.text = getString(R.string.submit_solution)
                    addSolutionButton.id = View.generateViewId()
                    constraintLayout.addView(addSolutionButton)
                    val addSolutionButtonLayoutParams = addSolutionButton.layoutParams as ConstraintLayout.LayoutParams
                    if (this::revokedSolutionJustification.isInitialized){
                        addSolutionButtonLayoutParams.topToBottom = revokedSolutionJustification.id
                    }else{
                        addSolutionButtonLayoutParams.topToBottom = statusMessage.id
                    }

                    addSolutionButtonLayoutParams.startToStart = constraintLayout.id
                    addSolutionButtonLayoutParams.leftMargin = 48
                    addSolutionButtonLayoutParams.topMargin = 20
                    addSolutionButton.requestLayout()
                    addSolutionButton.setOnClickListener {
                        val intentSubmit = Intent(this, SubmitSolutionActivity::class.java).apply {
                            putExtra("issueId", issueId)
                        }
                        startActivityForResult(intentSubmit, SUBMIT_SOLUTION)
                    }

                }

                //show solution message and photos
                if (jsonObjectResponse.getJSONObject("lastIssueState").getInt("type") in 2..5){
                    solutionMessage = TextView(this)
                    solutionMessage.text = jsonObjectResponse.getString("solutionMessage")
                    solutionMessage.id = View.generateViewId()
                    constraintLayout.addView(solutionMessage)
                    val solutionMessageLayoutParams = solutionMessage.layoutParams as ConstraintLayout.LayoutParams
                    solutionMessageLayoutParams.topToBottom = statusMessage.id
                    solutionMessageLayoutParams.startToStart = constraintLayout.id
                    solutionMessageLayoutParams.leftMargin = 48
                    solutionMessageLayoutParams.topMargin = 20
                    solutionMessage.requestLayout()

                    solutionImage = ImageView(this)
                    solutionImage.id = View.generateViewId()
                    val solutionPhotosArray = jsonObjectResponse.getJSONArray("solutionPhotos")
                    if (solutionPhotosArray.length() >= 1){
                        val imageBytes = Base64.decode(solutionPhotosArray[0] as String, 0)
                        val imageBitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                        solutionImage.setImageBitmap(imageBitmap)
                    }
                    constraintLayout.addView(solutionImage)
                    val solutionImageLayoutParams = solutionImage.layoutParams as ConstraintLayout.LayoutParams
                    solutionImageLayoutParams.topToBottom = solutionMessage.id
                    solutionImageLayoutParams.startToStart = constraintLayout.id
                    solutionImageLayoutParams.leftMargin = 48
                    solutionImageLayoutParams.topMargin = 20
                    solutionImage.requestLayout()



                }

                //add approve solution button

                addApproveRevokeSolutionButtons()

                //add implementation details button
                if (jsonObjectResponse.getJSONObject("lastIssueState").getInt("type") == 3 &&
                    sharedPref.getInt(getString(R.string.logged_user_type), -1) == 2) {
                    addImplementationDetailsButton = Button(this)
                    addImplementationDetailsButton.text = getString(R.string.submit_implementation)
                    addImplementationDetailsButton.id = View.generateViewId()
                    constraintLayout.addView(addImplementationDetailsButton)
                    val addImplementationDetailsButtonLayoutParams = addImplementationDetailsButton.layoutParams as ConstraintLayout.LayoutParams
                    if (this::revokedImplementationJustification.isInitialized){
                        addImplementationDetailsButtonLayoutParams.topToBottom = revokedSolutionJustification.id
                    }else{
                        addImplementationDetailsButtonLayoutParams.topToBottom = solutionImage.id
                    }

                    addImplementationDetailsButtonLayoutParams.startToStart = constraintLayout.id
                    addImplementationDetailsButtonLayoutParams.leftMargin = 48
                    addImplementationDetailsButtonLayoutParams.topMargin = 20
                    addImplementationDetailsButton.requestLayout()
                    addImplementationDetailsButton.setOnClickListener {
                        val intentSubmit = Intent(this, SubmitImplementationFormActivity::class.java).apply {
                            putExtra("issueId", issueId)
                        }
                        startActivityForResult(intentSubmit, SUBMIT_IMPLEMENTATION)
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

    fun addApproveRevokeButtons(){
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
            approveButtonLayoutParams.leftMargin = 48
            approveButtonLayoutParams.topMargin = 20
            approveButton.requestLayout()

            val revokeButton = Button(this)
            revokeButton.text = getString(R.string.revoke_issue)
            constraintLayout.addView(revokeButton)
            val revokeButtonLayoutParams = revokeButton.layoutParams as ConstraintLayout.LayoutParams
            revokeButtonLayoutParams.topToBottom = approveButton.id
            revokeButtonLayoutParams.startToStart = constraintLayout.id
            revokeButtonLayoutParams.leftMargin = 48
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
    }


    fun addApproveRevokeSolutionButtons(){
        //add buttons

        if (jsonObjectResponse.getJSONObject("lastIssueState").getInt("type") == 2 &&
            sharedPref.getInt(getString(R.string.logged_user_type), -1) == 3){

            val approveButton = Button(this)
            approveButton.text = getString(R.string.approve_solution)
            approveButton.id = View.generateViewId()
            constraintLayout.addView(approveButton)
            val approveButtonLayoutParams = approveButton.layoutParams as ConstraintLayout.LayoutParams
            approveButtonLayoutParams.topToBottom = solutionImage.id
            approveButtonLayoutParams.startToStart = constraintLayout.id
            approveButtonLayoutParams.leftMargin = 48
            approveButtonLayoutParams.topMargin = 20
            approveButton.requestLayout()

            val revokeButton = Button(this)
            revokeButton.text = getString(R.string.revoke_solution)
            constraintLayout.addView(revokeButton)
            val revokeButtonLayoutParams = revokeButton.layoutParams as ConstraintLayout.LayoutParams
            revokeButtonLayoutParams.topToBottom = approveButton.id
            revokeButtonLayoutParams.startToStart = constraintLayout.id
            revokeButtonLayoutParams.leftMargin = 48
            revokeButtonLayoutParams.topMargin = 20
            revokeButton.requestLayout()

            approveButton.setOnClickListener {
                val urlApprove = "http://10.0.2.2:5000/api/issueState/approveSolution/$issueId"
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
                showdialog {
                    result ->
                    if (result != null){
                        val urlRevoke = "http://10.0.2.2:5000/api/issueState/wrongSolution/$issueId/$result"
                        val requestRevoke = JsonObjectRequest(
                            Request.Method.POST, urlRevoke, null,
                            {
                                recreate()
                            },
                            {
                                Toast.makeText(this, "Error revoke solution", Toast.LENGTH_LONG).show()
                            })
                        queue.add(requestRevoke)
                    }
                }


            }
        }
    }

    fun showdialog(callback: (result: String?) -> Unit){
        val builder: AlertDialog.Builder = android.app.AlertDialog.Builder(this)
        builder.setTitle("Title")

// Set up the input
        val input = EditText(this)
// Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        input.setHint("Enter Text")
        input.inputType = InputType.TYPE_CLASS_TEXT
        builder.setView(input)

// Set up the buttons
        builder.setPositiveButton("OK") { dialog, which ->
            // Here you get get input text from the Edittext
            callback(input.text.toString())
        }
        builder.setNegativeButton("Cancel") { dialog, which ->
            dialog.cancel()
            callback(null)
        }

        builder.show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == SUBMIT_SOLUTION) {
            recreate()
        }

        if (requestCode == SUBMIT_IMPLEMENTATION) {
            recreate()
        }
    }


}