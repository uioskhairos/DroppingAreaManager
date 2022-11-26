package com.droppingareamanager.app

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase

class Logout : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_logout)
        auth = Firebase.auth
        val userId = auth.currentUser!!.uid
        FirebaseDatabase.getInstance()
            .getReference("Tokens")
            .child(userId)
            .removeValue()
        auth.signOut()
        val intent = Intent(this, Login::class.java)
        //to prevent user from returning to Register Activity
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        finish() // to close register activity
    }
}