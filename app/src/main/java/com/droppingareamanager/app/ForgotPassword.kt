package com.droppingareamanager.app

import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.droppingareamanager.app.databinding.ActivityForgotPasswordBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase


class ForgotPassword : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var binding: ActivityForgotPasswordBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityForgotPasswordBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        auth = Firebase.auth
        val emailForgot = binding.forgotEmail.text
        binding.forgotPasswordBtn.setOnClickListener {
            auth.sendPasswordResetEmail(emailForgot.toString())
                .addOnCompleteListener {
                    if (it.isSuccessful) {
                        Log.d(TAG, "Email sent.")
                        Toast.makeText(this, "Password reset sent to your email", Toast.LENGTH_SHORT)
                            .show()
                    }
                    else{
                        Toast.makeText(this, "Failed to send password email reset", Toast.LENGTH_SHORT)
                            .show()
                    }
                }
                .addOnFailureListener {
                        err -> Toast.makeText(this, "Failed to send password email reset ${err.message}", Toast.LENGTH_SHORT).show()
                }
        }

        binding.backToLogin.setOnClickListener {
            val intent = Intent(this, Login::class.java)
            startActivity(intent)
        }
    }
}