package com.example.civichub

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.core.widget.doOnTextChanged
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.civichub.ui.login.LoginActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import org.json.JSONObject
import java.util.regex.Pattern

class RegisterActivity : AppCompatActivity(), AdapterView.OnItemSelectedListener {


    private lateinit var spinner: Spinner
    private var userTypeSelected: Int = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        val registerButton = findViewById<Button>(R.id.registerButton)
        val emailTextInputLayout = findViewById<TextInputLayout>(R.id.EmailTextInputLayout)
        val emailTextInputEditText = findViewById<TextInputEditText>(R.id.EmailTextInputEditText)

        val passwordTextInputLayout = findViewById<TextInputLayout>(R.id.passwordTextInputLayout)
        val passwordTextInputEditText = findViewById<TextInputEditText>(R.id.passwordTextInputEditText)

        val repeatPasswordTextInputLayout = findViewById<TextInputLayout>(R.id.repeatPasswordTextInputLayout)
        val repeatPasswordTextInputEditText = findViewById<TextInputEditText>(R.id.repeatPasswordTextInputEditText)

        spinner = findViewById(R.id.spinner)

        registerButton.isEnabled = false

        emailTextInputEditText.doOnTextChanged { text, _, _, _ ->
            if (!Pattern.matches("[A-Za-z0-9_]{3,}@[A-za-z]{3,10}\\.[a-z]{3}", text ?: "")){
                emailTextInputLayout.error = applicationContext.resources.getString(R.string.invalid_mail)
                registerButton.isEnabled = false
            }else{
                emailTextInputLayout.error = null
                if (passwordTextInputLayout.error == null &&  repeatPasswordTextInputLayout.error == null
                    && (passwordTextInputEditText.text?:"").toString().length >= 5){
                    registerButton.isEnabled = true
                }
            }
        }

        passwordTextInputEditText.doOnTextChanged { text, _, _, _ ->
            if (!Pattern.matches(".{5,}", text ?: "")){
                passwordTextInputLayout.error = applicationContext.resources.getString(R.string.minimum_length_password_edit_text)
                registerButton.isEnabled = false
            }else{
                passwordTextInputLayout.error = null
                if (emailTextInputLayout.error == null &&  repeatPasswordTextInputLayout.error == null
                    && (passwordTextInputEditText.text?:"").toString() == (repeatPasswordTextInputEditText?:"z").toString() ){
                    registerButton.isEnabled = true
                }
            }
        }

        repeatPasswordTextInputEditText.doOnTextChanged { text, _, _, _ ->
            if ((text?:"").toString() != passwordTextInputEditText.text.toString()){
                repeatPasswordTextInputLayout.error = applicationContext.resources.getString(R.string.wrong_passwords)
                registerButton.isEnabled = false
            }else{
                repeatPasswordTextInputLayout.error = null
                if (passwordTextInputLayout.error == null &&  emailTextInputLayout.error == null
                    && (Pattern.matches(".{5,}", text ?: ""))){
                    registerButton.isEnabled = true
                }
            }
        }

        registerButton.setOnClickListener {
            it.isEnabled = false
            // Instantiate the RequestQueue.
            val queue = Volley.newRequestQueue(this)
            val url = "http://10.0.2.2:5000/api/auth/register"
            val jsonBody = JSONObject()
            jsonBody.put("mail", emailTextInputEditText.text)
            jsonBody.put("password", passwordTextInputEditText.text)
            jsonBody.put("tip", userTypeSelected)

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

        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter.createFromResource(
            this,
            R.array.planets_array,
            android.R.layout.simple_spinner_item
//            R.layout.custom_spinner_item
        ).also { adapter ->
            // Specify the layout to use when the list of choices appears
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
//            adapter.setDropDownViewResource(R.layout.custom_spinner_dropdown_item)
            // Apply the adapter to the spinner
            spinner.adapter = adapter
        }
        spinner.onItemSelectedListener = this
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        userTypeSelected = position + 1
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
        TODO("Not yet implemented")
    }
}