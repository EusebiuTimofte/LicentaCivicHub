package com.example.civichub

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.BitmapFactory
import android.os.Bundle
import android.text.InputType
import android.util.Base64
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject
import kotlin.properties.Delegates


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
    private lateinit var implementationMessage: TextView
    private lateinit var implementationImage: ImageView
    private lateinit var followButton: ImageButton
    private var followJsonObject: JSONObject? = null
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var userGuid:String
    private val SUBMIT_SOLUTION = 1
    private val SUBMIT_IMPLEMENTATION = 2
    private var scale by Delegates.notNull<Float>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_issue_details)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        scale = applicationContext.resources.displayMetrics.density

        sharedPref = getSharedPreferences(getString(R.string.shared_preferences_file), Context.MODE_PRIVATE)
        userGuid = sharedPref.getString(getString(R.string.logged_user_id), "")!!

        constraintLayout = findViewById(R.id.constraintLayout)
        title = findViewById(R.id.title)
        description = findViewById(R.id.description)
        image = findViewById(R.id.descriptionImage)
        statusMessage = findViewById(R.id.statusMessage)
        followButton = findViewById(R.id.followButton)
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayoutIssueDetails)

        swipeRefreshLayout.setOnRefreshListener {
            recreate()
            swipeRefreshLayout.isRefreshing = false
        }


        issueId = intent.getStringExtra("issueId")!!

        queue = Volley.newRequestQueue(this.applicationContext)
        val url = "http://10.0.2.2:5000/api/issue/getIssueWithStatesDetails/$issueId"

        // Request a string response from the provided URL.
        val request = JsonObjectRequest(
            Request.Method.GET, url, null,
            { response ->
                // Display the first 500 characters of the response string.
                jsonObjectResponse = response
                title.text = getString(R.string.issue_description_title).format(jsonObjectResponse.getString("title"))
                description.text = getString(R.string.issue_description_description).format(jsonObjectResponse.getString("description"))
                val photosArray = jsonObjectResponse.getJSONArray("photos")
                if (photosArray.length() >= 1){
                    val imageBytes = Base64.decode(photosArray[0] as String, 0)
                    val imageBitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                    image.setImageBitmap(imageBitmap)
                }
                statusMessage.text = getString(R.string.issue_description_status).format(jsonObjectResponse.getJSONObject("lastIssueState").getString("message"))

                //add justification in case needed
                if (jsonObjectResponse.getJSONObject("lastIssueState").getInt("type") == 1 &&
                    !jsonObjectResponse.getJSONObject("lastIssueState").isNull("customMessage")){
                        val justificationString = jsonObjectResponse.getJSONObject("lastIssueState").getString("customMessage")
                    revokedSolutionJustification = findViewById(R.id.revokedSolutionJustification)
                    revokedSolutionJustification.text = getString(R.string.issue_description_justification).format(justificationString)
                    revokedSolutionJustification.visibility = View.VISIBLE
//                    revokedSolutionJustification.id = View.generateViewId()
//                    constraintLayout.addView(revokedSolutionJustification)
//                    val revokedSolutionJustificationLayoutParams = revokedSolutionJustification.layoutParams as ConstraintLayout.LayoutParams
//                    revokedSolutionJustificationLayoutParams.topToBottom = statusMessage.id
//                    revokedSolutionJustificationLayoutParams.startToStart = constraintLayout.id
//                    revokedSolutionJustificationLayoutParams.leftMargin = 48
//                    revokedSolutionJustificationLayoutParams.topMargin = 20
//                    revokedSolutionJustification.requestLayout()
//
//                    val constraintSet = ConstraintSet()
//                    constraintSet.clone(constraintLayout)
//
//                    constraintSet.connect(
//                        revokedSolutionJustification.id,
//                        ConstraintSet.LEFT,
//                        statusMessage.id,
//                        ConstraintSet.LEFT,
//                        0
//                    )
//                    constraintSet.applyTo(constraintLayout)
                }

                addApproveRevokeButtons()

                //add solution button
                if (jsonObjectResponse.getJSONObject("lastIssueState").getInt("type") == 1 &&
                    sharedPref.getInt(getString(R.string.logged_user_type), -1) == 2) {
                    val addSolutionButton = findViewById<Button>(R.id.addSolutionButton)
                    addSolutionButton.text = getString(R.string.submit_solution)
                    addSolutionButton.visibility = View.VISIBLE
//                    addSolutionButton.id = View.generateViewId()
//                    constraintLayout.addView(addSolutionButton)
//                    val addSolutionButtonLayoutParams = addSolutionButton.layoutParams as ConstraintLayout.LayoutParams
//                    if (this::revokedSolutionJustification.isInitialized){
//                        addSolutionButtonLayoutParams.topToBottom = revokedSolutionJustification.id
//                    }else{
//                        addSolutionButtonLayoutParams.topToBottom = statusMessage.id
//                    }
//
//                    addSolutionButtonLayoutParams.startToStart = constraintLayout.id
//                    addSolutionButtonLayoutParams.leftMargin = 48
//                    addSolutionButtonLayoutParams.topMargin = 20
//                    addSolutionButton.requestLayout()
                    addSolutionButton.setOnClickListener {
                        val intentSubmit = Intent(this, SubmitSolutionActivity::class.java).apply {
                            putExtra("issueId", issueId)
                        }
                        startActivityForResult(intentSubmit, SUBMIT_SOLUTION)
                    }

                }

                //show solution message and photos
                if (jsonObjectResponse.getJSONObject("lastIssueState").getInt("type") in 2..5){
                    solutionMessage = findViewById(R.id.solutionMessage)
                    solutionMessage.text = getString(R.string.issue_description_solution_proposed).format(jsonObjectResponse.getString("solutionMessage"))
                    solutionMessage.visibility = View.VISIBLE
//                    solutionMessage.id = View.generateViewId()
//                    constraintLayout.addView(solutionMessage)
//                    val solutionMessageLayoutParams = solutionMessage.layoutParams as ConstraintLayout.LayoutParams
//                    solutionMessageLayoutParams.topToBottom = statusMessage.id
////                    solutionMessageLayoutParams.startToStart = statusMessage.id
////                    solutionMessageLayoutParams.marginStart = (20 * scale).toInt()
////                    solutionMessageLayoutParams.leftMargin = (20 * scale).toInt()
////                    solutionMessageLayoutParams.setMargins(20,20,10,0)
//                    solutionMessageLayoutParams.topMargin = 50
////                    val solutionMessageMarginParams = solutionMessage.layoutParams as ViewGroup.MarginLayoutParams
////                    solutionMessageMarginParams.setMargins(20,20,10,0)
//                    solutionMessage.requestLayout()
//
//                    val constraintSet = ConstraintSet()
//                    constraintSet.clone(constraintLayout)
//
//                    constraintSet.connect(
//                        solutionMessage.id,
//                        ConstraintSet.LEFT,
//                        statusMessage.id,
//                        ConstraintSet.LEFT,
//                        (20 * scale).toInt()
//                    )
//                    constraintSet.connect(
//                        solutionMessage.id,
//                        ConstraintSet.END,
//                        constraintLayout.id,
//                        ConstraintSet.END,
//                        0
//                    )
//                    constraintSet.applyTo(constraintLayout)

                    solutionImage = findViewById(R.id.solutionImage)
//                    solutionImage.id = View.generateViewId()
                    val solutionPhotosArray = jsonObjectResponse.getJSONArray("solutionPhotos")
                    if (solutionPhotosArray.length() >= 1){
                        val imageBytes = Base64.decode(solutionPhotosArray[0] as String, 0)
                        val imageBitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                        solutionImage.setImageBitmap(imageBitmap)
                    }
                    solutionImage.visibility = View.VISIBLE
//                    constraintLayout.addView(solutionImage)
//                    val solutionImageLayoutParams = solutionImage.layoutParams as ConstraintLayout.LayoutParams
//                    solutionImageLayoutParams.topToBottom = solutionMessage.id
////                    solutionImageLayoutParams.startToStart = constraintLayout.id
////                    solutionImageLayoutParams.leftMargin = 48
//                    solutionImageLayoutParams.topMargin = 20
//                    solutionImage.requestLayout()
//
//
//                    val constraintSetImage = ConstraintSet()
//                    constraintSetImage.clone(constraintLayout)
//                    constraintSetImage.connect(
//                        solutionImage.id,
//                        ConstraintSet.LEFT,
//                        solutionMessage.id,
//                        ConstraintSet.LEFT,
//                        0
//                    )
//                    constraintSetImage.applyTo(constraintLayout)



                }

                //add approve solution button



                addApproveRevokeSolutionButtons()


                //add revoked implementation justification in case needed
                if (jsonObjectResponse.getJSONObject("lastIssueState").getInt("type") == 3 &&
                    !jsonObjectResponse.getJSONObject("lastIssueState").isNull("customMessage")){
                    val justificationString = jsonObjectResponse.getJSONObject("lastIssueState").getString("customMessage")
                    Log.d("initialisation", this::revokedImplementationJustification.isInitialized.toString())
                    revokedImplementationJustification = findViewById(R.id.revokedImplementationJustification)
                    Log.d("initialisation after", this::revokedImplementationJustification.isInitialized.toString())
                    revokedImplementationJustification.text = getString(R.string.issue_description_justification_implementation).format(justificationString)
                    revokedImplementationJustification.visibility = View.VISIBLE
//                    revokedImplementationJustification.id = View.generateViewId()
//                    constraintLayout.addView(revokedImplementationJustification)
//                    val revokedImplementationJustificationLayoutParams = revokedImplementationJustification.layoutParams as ConstraintLayout.LayoutParams
//                    revokedImplementationJustificationLayoutParams.topToBottom = solutionImage.id
////                    revokedImplementationJustificationLayoutParams.startToStart = constraintLayout.id
////                    revokedImplementationJustificationLayoutParams.leftMargin = 48
//                    revokedImplementationJustificationLayoutParams.topMargin = 20
//                    revokedImplementationJustification.requestLayout()
//
//                    val constraintSet = ConstraintSet()
//                    constraintSet.clone(constraintLayout)
//
//                    constraintSet.connect(
//                        revokedImplementationJustification.id,
//                        ConstraintSet.LEFT,
//                        statusMessage.id,
//                        ConstraintSet.LEFT,
//                        0
//                    )
//                    constraintSet.applyTo(constraintLayout)

                }

                //add implementation details button
                if (jsonObjectResponse.getJSONObject("lastIssueState").getInt("type") == 3 &&
                    sharedPref.getInt(getString(R.string.logged_user_type), -1) == 2) {
                    addImplementationDetailsButton = findViewById(R.id.addImplementationDetails)
                    addImplementationDetailsButton.text = getString(R.string.submit_implementation)
                    addImplementationDetailsButton.visibility = View.VISIBLE
//                    addImplementationDetailsButton.id = View.generateViewId()
//                    constraintLayout.addView(addImplementationDetailsButton)
//                    val addImplementationDetailsButtonLayoutParams = addImplementationDetailsButton.layoutParams as ConstraintLayout.LayoutParams
//                    if (this::revokedImplementationJustification.isInitialized){
//                        addImplementationDetailsButtonLayoutParams.topToBottom = revokedImplementationJustification.id
//                    }else{
//                        addImplementationDetailsButtonLayoutParams.topToBottom = solutionImage.id
//                    }
//
//                    addImplementationDetailsButtonLayoutParams.startToStart = constraintLayout.id
//                    addImplementationDetailsButtonLayoutParams.leftMargin = 48
//                    addImplementationDetailsButtonLayoutParams.topMargin = 20
//                    addImplementationDetailsButton.requestLayout()
                    addImplementationDetailsButton.setOnClickListener {
                        val intentSubmit = Intent(this, SubmitImplementationFormActivity::class.java).apply {
                            putExtra("issueId", issueId)
                        }
                        startActivityForResult(intentSubmit, SUBMIT_IMPLEMENTATION)
                    }

                }

                //show implementation message and photo
                if (jsonObjectResponse.getJSONObject("lastIssueState").getInt("type") in 4..5){
                    implementationMessage = findViewById(R.id.implementationMessage)
                    implementationMessage.text = getString(R.string.issue_description_implementation_details).format(jsonObjectResponse.getString("implementationMessage"))
                    implementationMessage.visibility = View.VISIBLE
//                    implementationMessage.id = View.generateViewId()
//                    constraintLayout.addView(implementationMessage)
//                    val implementationMessageLayoutParams = implementationMessage.layoutParams as ConstraintLayout.LayoutParams
//                    implementationMessageLayoutParams.topToBottom = solutionImage.id
////                    implementationMessageLayoutParams.startToStart = constraintLayout.id
////                    implementationMessageLayoutParams.leftMargin = 48
//                    implementationMessageLayoutParams.topMargin = 20
//                    implementationMessage.requestLayout()
//
//                    val constraintSet = ConstraintSet()
//                    constraintSet.clone(constraintLayout)
//
//                    constraintSet.connect(
//                        implementationMessage.id,
//                        ConstraintSet.LEFT,
//                        statusMessage.id,
//                        ConstraintSet.LEFT,
//                        0
//                    )
//                    constraintSet.applyTo(constraintLayout)

                    implementationImage = findViewById(R.id.implementationImage)
//                    implementationImage.id = View.generateViewId()
                    val solutionPhotosArray = jsonObjectResponse.getJSONArray("implementationPhotos")
                    if (solutionPhotosArray.length() >= 1){
                        val imageBytes = Base64.decode(solutionPhotosArray[0] as String, 0)
                        val imageBitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                        implementationImage.setImageBitmap(imageBitmap)
                    }
                    implementationImage.visibility = View.VISIBLE
//                    constraintLayout.addView(implementationImage)
//                    val implementationImageLayoutParams = implementationImage.layoutParams as ConstraintLayout.LayoutParams
//                    implementationImageLayoutParams.topToBottom = implementationMessage.id
////                    implementationImageLayoutParams.startToStart = constraintLayout.id
////                    implementationImageLayoutParams.leftMargin = 48
//                    implementationImageLayoutParams.topMargin = 20
//                    implementationImage.requestLayout()
//
//                    val constraintSetImage = ConstraintSet()
//                    constraintSetImage.clone(constraintLayout)
//
//                    constraintSetImage.connect(
//                        implementationImage.id,
//                        ConstraintSet.LEFT,
//                        implementationMessage.id,
//                        ConstraintSet.LEFT,
//                        0
//                    )
//                    constraintSet.applyTo(constraintLayout)


                }

                //show add revoke implementation buttons
                addApproveRevokeImplementationButtons()


                //set follow button on click listeners
                val userId = sharedPref.getString(getString(R.string.logged_user_id), "")
                val followUrl = "http://10.0.2.2:5000/api/Follow/getAllByIssueAndUser/$issueId/$userId"
                val followRequest = StringRequest(Request.Method.GET, followUrl,
                    {
                        // Display the first 500 characters of the response string.
//                        followButton.setBackgroundColor(Color.GREEN)
                        followButton.setImageResource(android.R.drawable.btn_star_big_on)
                        followJsonObject = JSONObject(it)
                    },
                    {

//                        if (error.message!! == "Follow not found"){
//                            followButton.setBackgroundColor(Color.RED)
                        followButton.setImageResource(android.R.drawable.btn_star_big_off)
                            followJsonObject = null
//                        }else{
//                            Toast.makeText(this.applicationContext, "Error checking following", Toast.LENGTH_LONG).show()
//                            followJsonObject = null
//                        }
                    })
                queue.add(followRequest)

                val addFollowRequestUrl = "http://10.0.2.2:5000/api/Follow"
                val addFollowRequestBody = JSONObject()
                addFollowRequestBody.put("userId", userId)
                addFollowRequestBody.put("issueId", issueId)
                val addFollowRequest = JsonObjectRequest(Request.Method.POST, addFollowRequestUrl, addFollowRequestBody,
                    {
                        queue.add(followRequest)
                    },
                    {
                        Toast.makeText(this.applicationContext, "Add follow error", Toast.LENGTH_LONG).show()
                        Log.d("Add follow", it.message!!)
                    })

                val deleteFollowRequestUrl = "http://10.0.2.2:5000/api/Follow/byUserAndIssue/$userId/$issueId"
                val deleteFollowRequest = StringRequest(Request.Method.DELETE, deleteFollowRequestUrl,
                    {
                        queue.add(followRequest)
                    },
                    {
                        Toast.makeText(this.applicationContext, "Error unfollowing", Toast.LENGTH_LONG).show()
                    })

                followButton.setOnClickListener {

                    if (followJsonObject == null){
                        queue.add(addFollowRequest)
                    }else{
                        queue.add(deleteFollowRequest)
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


    private fun addApproveRevokeButtons(){
        //add buttons

        if (jsonObjectResponse.getJSONObject("lastIssueState").getInt("type") == 0 &&
            sharedPref.getInt(getString(R.string.logged_user_type), -1) == 3){

            val approveButton = findViewById<Button>(R.id.approveIssueButton)
            approveButton.text = getString(R.string.approve_issue)
            approveButton.visibility = View.VISIBLE
//            approveButton.id = View.generateViewId()
//            constraintLayout.addView(approveButton)
//            val approveButtonLayoutParams = approveButton.layoutParams as ConstraintLayout.LayoutParams
//            approveButtonLayoutParams.topToBottom = statusMessage.id
//            approveButtonLayoutParams.startToStart = constraintLayout.id
//            approveButtonLayoutParams.leftMargin = 48
//            approveButtonLayoutParams.topMargin = 20
//            approveButton.requestLayout()

            val revokeButton = findViewById<Button>(R.id.revokeIssueButton)
            revokeButton.text = getString(R.string.revoke_issue)
            revokeButton.visibility = View.VISIBLE
//            constraintLayout.addView(revokeButton)
//            val revokeButtonLayoutParams = revokeButton.layoutParams as ConstraintLayout.LayoutParams
//            revokeButtonLayoutParams.topToBottom = approveButton.id
//            revokeButtonLayoutParams.startToStart = constraintLayout.id
//            revokeButtonLayoutParams.leftMargin = 48
//            revokeButtonLayoutParams.topMargin = 20
//            revokeButton.requestLayout()

            approveButton.setOnClickListener {
                val urlApprove = "http://10.0.2.2:5000/api/issueState/firstApprovalGiven/$issueId/$userGuid"
                val requestApprove = JsonObjectRequest(
                    Request.Method.POST, urlApprove, null,
                    {
                        Toast.makeText(this, "Reclamatia a fost aprobata",Toast.LENGTH_SHORT).show()
                        recreate()
                    },
                    {
                        Toast.makeText(this, "Error approve", Toast.LENGTH_LONG).show()
                    })
                queue.add(requestApprove)
            }

            revokeButton.setOnClickListener {
                val urlRevoke = "http://10.0.2.2:5000/api/issueState/revoke/$issueId/$userGuid"
                val requestRevoke = JsonObjectRequest(
                    Request.Method.POST, urlRevoke, null,
                    {
                        Toast.makeText(this, "Reclamatia a fost respinsa",Toast.LENGTH_SHORT).show()
                        recreate()
                    },
                    {
                        Toast.makeText(this, "Error revoke", Toast.LENGTH_LONG).show()
                    })
                queue.add(requestRevoke)
            }
        }
    }


    private fun addApproveRevokeSolutionButtons(){
        //add buttons

        if (jsonObjectResponse.getJSONObject("lastIssueState").getInt("type") == 2 &&
            sharedPref.getInt(getString(R.string.logged_user_type), -1) == 3){

            val approveButton = findViewById<Button>(R.id.approveSolutionButton)
            approveButton.text = getString(R.string.approve_solution)
            approveButton.visibility = View.VISIBLE
//            approveButton.id = View.generateViewId()
//            constraintLayout.addView(approveButton)
//            val approveButtonLayoutParams = approveButton.layoutParams as ConstraintLayout.LayoutParams
//            approveButtonLayoutParams.topToBottom = solutionImage.id
//            approveButtonLayoutParams.startToStart = constraintLayout.id
//            approveButtonLayoutParams.leftMargin = 48
//            approveButtonLayoutParams.topMargin = 20
//            approveButton.requestLayout()

            val revokeButton = findViewById<Button>(R.id.revokeSolutionButton)
            revokeButton.text = getString(R.string.revoke_solution)
            revokeButton.visibility = View.VISIBLE
//            constraintLayout.addView(revokeButton)
//            val revokeButtonLayoutParams = revokeButton.layoutParams as ConstraintLayout.LayoutParams
//            revokeButtonLayoutParams.topToBottom = approveButton.id
//            revokeButtonLayoutParams.startToStart = constraintLayout.id
//            revokeButtonLayoutParams.leftMargin = 48
//            revokeButtonLayoutParams.topMargin = 20
//            revokeButton.requestLayout()

            approveButton.setOnClickListener {
                val urlApprove = "http://10.0.2.2:5000/api/issueState/approveSolution/$issueId/$userGuid"
                val requestApprove = JsonObjectRequest(
                    Request.Method.POST, urlApprove, null,
                    {
                        Toast.makeText(this, "Solutia a fost aprobata",Toast.LENGTH_SHORT).show()
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
                        val urlRevoke = "http://10.0.2.2:5000/api/issueState/wrongSolution/$issueId/$result/$userGuid"
                        val requestRevoke = JsonObjectRequest(
                            Request.Method.POST, urlRevoke, null,
                            {
                                Toast.makeText(this, "Solutia a fost respinsa",Toast.LENGTH_SHORT).show()
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

    private fun showdialog(callback: (result: String?) -> Unit){
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        builder.setTitle("Justificare")

// Set up the input
        val input = EditText(this)
// Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        input.hint = "Introduceti motivul"
        input.inputType = InputType.TYPE_CLASS_TEXT
        builder.setView(input)

// Set up the buttons
        builder.setPositiveButton("OK") { _, _ ->
            // Here you get get input text from the Edittext
            callback(input.text.toString())
        }
        builder.setNegativeButton("Renunta") { dialog, _ ->
            dialog.cancel()
            callback(null)
        }

        builder.show()
    }

    private fun addApproveRevokeImplementationButtons(){
        //add buttons

        if (jsonObjectResponse.getJSONObject("lastIssueState").getInt("type") == 4 &&
            sharedPref.getInt(getString(R.string.logged_user_type), -1) == 3){

            val approveButton = findViewById<Button>(R.id.approveImplementationButton)
            approveButton.text = getString(R.string.approve_implementation)
            approveButton.visibility = View.VISIBLE
//            approveButton.id = View.generateViewId()
//            constraintLayout.addView(approveButton)
//            val approveButtonLayoutParams = approveButton.layoutParams as ConstraintLayout.LayoutParams
//            approveButtonLayoutParams.topToBottom = implementationImage.id
//            approveButtonLayoutParams.startToStart = constraintLayout.id
//            approveButtonLayoutParams.leftMargin = 48
//            approveButtonLayoutParams.topMargin = 20
//            approveButton.requestLayout()

            val revokeButton = findViewById<Button>(R.id.revokeImplementationButton)
            revokeButton.text = getString(R.string.revoke_implementation)
            revokeButton.visibility = View.VISIBLE
//            constraintLayout.addView(revokeButton)
//            val revokeButtonLayoutParams = revokeButton.layoutParams as ConstraintLayout.LayoutParams
//            revokeButtonLayoutParams.topToBottom = approveButton.id
//            revokeButtonLayoutParams.startToStart = constraintLayout.id
//            revokeButtonLayoutParams.leftMargin = 48
//            revokeButtonLayoutParams.topMargin = 20
//            revokeButton.requestLayout()

            approveButton.setOnClickListener {
                val urlApprove = "http://10.0.2.2:5000/api/issueState/approveImplementation/$issueId/$userGuid"
                val requestApprove = JsonObjectRequest(
                    Request.Method.POST, urlApprove, null,
                    {
                        Toast.makeText(this, "Modul de implementare a fost aprobat",Toast.LENGTH_SHORT).show()
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
                        val urlRevoke = "http://10.0.2.2:5000/api/issueState/wrongImplementation/$issueId/$result/$userGuid"
                        val requestRevoke = JsonObjectRequest(
                            Request.Method.POST, urlRevoke, null,
                            {
                                Toast.makeText(this, "Modul de implementare a fost respins",Toast.LENGTH_SHORT).show()
                                recreate()
                            },
                            {
                                Toast.makeText(this, "Error revoke implementation", Toast.LENGTH_LONG).show()
                            })
                        queue.add(requestRevoke)
                    }
                }


            }
        }
    }

//    fun showdialog(callback: (result: String?) -> Unit){
//        val builder: AlertDialog.Builder = android.app.AlertDialog.Builder(this)
//        builder.setTitle("Title")
//
//// Set up the input
//        val input = EditText(this)
//// Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
//        input.setHint("Enter Text")
//        input.inputType = InputType.TYPE_CLASS_TEXT
//        builder.setView(input)
//
//// Set up the buttons
//        builder.setPositiveButton("OK") { dialog, which ->
//            // Here you get get input text from the Edittext
//            callback(input.text.toString())
//        }
//        builder.setNegativeButton("Cancel") { dialog, which ->
//            dialog.cancel()
//            callback(null)
//        }
//
//        builder.show()
//    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == SUBMIT_SOLUTION) {
            if (resultCode == Activity.RESULT_OK){
                Toast.makeText(this, "Solutia a fost adaugata", Toast.LENGTH_SHORT).show()
            }
            recreate()
        }

        if (requestCode == SUBMIT_IMPLEMENTATION) {
            if (resultCode == Activity.RESULT_OK){
                Toast.makeText(this, "Implementarea a fost adaugata", Toast.LENGTH_SHORT).show()
            }
            recreate()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

}