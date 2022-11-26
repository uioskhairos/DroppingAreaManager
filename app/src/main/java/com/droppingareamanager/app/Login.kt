package com.droppingareamanager.app

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.res.ResourcesCompat
import com.droppingareamanager.app.admin.AdminDashboard
import com.droppingareamanager.app.databinding.ActivityLoginBinding
import com.droppingareamanager.app.user.DashboardActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging

class Login : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var dbUser: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        auth = Firebase.auth

        val registertext: TextView = findViewById(R.id.textView_register_now)

        registertext.setOnClickListener {
            val intent = Intent(this, Register::class.java)
            startActivity(intent)
        }
        binding.forgotPassword.setOnClickListener{
            val intent = Intent(this, ForgotPassword::class.java)
            startActivity(intent)
        }

        val loginButton: Button = findViewById(R.id.button_login)

        loginButton.setOnClickListener{
            loginButton.isEnabled = false
//            loginButton.setTextColor(ContextCompat.getColor(textView.context, R.color.white))
            loginButton.text = "Loading..."
//            loginButton.setBackgroundColor(ContextCompat.getColor(loginButton.context, R.color.light_gray))
            ResourcesCompat.getDrawable(resources, R.drawable.disabled, null);
            performLogin()
        }
    }

    private fun performLogin(){
        //get input from user
        val email: EditText = findViewById(R.id.editText_email_login)
        val password: EditText = findViewById(R.id.editText_password_login)
        val loginButton: Button = findViewById(R.id.button_login)
//        accountType = findViewById(R.id.accountType)
        var accountType: String?

        //null check of inputs
        if (email.text.isEmpty() || password.text.isEmpty()){
            Toast.makeText(this, "Please fill all the required fields", Toast.LENGTH_SHORT)
                .show()
            loginButton.isEnabled = true
            loginButton.text = "Login"
            ResourcesCompat.getDrawable(resources, R.drawable.rounded, null)
            return
        }
        val emailInput = email.text.toString()
        val passwordInput = password.text.toString()

        auth.signInWithEmailAndPassword(emailInput, passwordInput)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    retrieveAndStoreToken()

                    auth = Firebase.auth
                    val userId = auth.currentUser?.uid.toString()
                    dbUser = FirebaseDatabase.getInstance().getReference("User")
                    dbUser.child(userId).get().addOnSuccessListener {
                        accountType = it.child("accountType").value.toString()
                        if (accountType == "Dropping Area"){
                            startActivity(Intent(this@Login, AdminDashboard::class.java))
                            Toast.makeText(baseContext, "Logged in as $accountType.",
                                Toast.LENGTH_SHORT).show()
                        }
                        if (accountType == "Dropper"){
                            startActivity(Intent(this@Login, DashboardActivity::class.java))
                            Toast.makeText(baseContext, "Logged in as $accountType.",
                                Toast.LENGTH_SHORT).show()
                        }
                    }.addOnFailureListener{
                        Log.e("firebase", "Error getting data", it)
                    }

                } else {
                    // If sign in fails, display a message to the user.
                    Toast.makeText(baseContext, "Authentication failed.",
                        Toast.LENGTH_SHORT).show()
                    loginButton.isEnabled = true
                    loginButton.text = "Login"
                    ResourcesCompat.getDrawable(resources, R.drawable.rounded, null)
                }
            }
            .addOnFailureListener {
                Toast.makeText(baseContext, "Authentication failed. ${it.localizedMessage}",
                    Toast.LENGTH_SHORT).show()
                loginButton.isEnabled = true
                loginButton.text = "Login"
                ResourcesCompat.getDrawable(resources, R.drawable.rounded, null)
            }

    }
    private fun retrieveAndStoreToken(){
        auth = Firebase.auth
        val userId = auth.currentUser!!.uid
        FirebaseMessaging.getInstance().token
            .addOnCompleteListener {
                if (it.isSuccessful){
                    val token = it.result
                    FirebaseDatabase.getInstance()
                        .getReference("Tokens")
                        .child(userId)
                        .setValue(token)
                }
            }
    }
}