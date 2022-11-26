package com.droppingareamanager.app.admin

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.droppingareamanager.app.databinding.ActivityUpdateCashoutAdminBinding
import com.google.firebase.database.*

class UpdateCashoutAdminActivity : AppCompatActivity() {
    private lateinit var binding: ActivityUpdateCashoutAdminBinding
    private lateinit var dbCashout: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUpdateCashoutAdminBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        dbCashout = FirebaseDatabase.getInstance().getReference("Cashouts")
        val intent = intent
        val itemId =intent.getStringExtra("itemId").toString()
        val shopName =intent.getStringExtra("shopName")
        val amount =intent.getStringExtra("amount")
        val action =intent.getStringExtra("action")
        binding.userNameApprove.text = shopName
        binding.cashoutAmountApprove.text = "₱$amount.00"

//        binding.approveHf.visibility.GONE
        if (action=="approve"){
            binding.updateQuestion.text = "Approve Cashout?"
        }
        if (action=="decline"){
            binding.updateQuestion.text = "Decline Cashout?"
        }
        if (action=="delete"){
            binding.updateQuestion.text = "Delete Cashout?"
        }

        binding.confirmBtnUpdate.setOnClickListener{
            if (action=="approve"){
                dbCashout.child(itemId).child("status").setValue("completed").addOnCompleteListener {
                    val intentCancel = Intent(this, AdminCashout::class.java)
                    //to prevent user from returning to Register Activity
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(intentCancel)
                    finish()
                    Toast.makeText(this, "Cashout approved successfully", Toast.LENGTH_SHORT)
                        .show()
                }.addOnFailureListener {
                        err -> Toast.makeText(this, "Update Error ${err.message}", Toast.LENGTH_SHORT).show()
                }
            }
            if (action=="decline"){
                dbCashout.child(itemId).child("status").setValue("declined").addOnCompleteListener {
                    val intentCancel = Intent(this, AdminCashout::class.java)
                    //to prevent user from returning to Register Activity
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(intentCancel)
                    finish()
                    Toast.makeText(this, "Cashout declined", Toast.LENGTH_SHORT)
                        .show()
                }.addOnFailureListener {
                        err -> Toast.makeText(this, "Update Error ${err.message}", Toast.LENGTH_SHORT).show()
                }
            }
            if (action=="delete"){
                dbCashout.child(itemId).removeValue().addOnCompleteListener {
                    val intentCancel = Intent(this, AdminCashout::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(intentCancel)
                    finish()
                    Toast.makeText(this, "Cashout deleted successfully", Toast.LENGTH_SHORT)
                        .show()
                }.addOnFailureListener {
                        err -> Toast.makeText(this, "Update Error ${err.message}", Toast.LENGTH_SHORT).show()
                }

                //delete all pending
//                dbCashout.orderByChild("status").equalTo("pending").addValueEventListener(object :
//                    ValueEventListener {
//                    override fun onDataChange(snapshot: DataSnapshot) {
//                        if (snapshot.exists()){
//                            for (i in snapshot.children){
//                                val key = i.key.toString()
//                                dbCashout.child(key).removeValue()
//                                val intentCancel = Intent(this@UpdateCashoutAdminActivity, AdminCashout::class.java)
//                            }
//                        }
//                        else{
//                            Toast.makeText(this@UpdateCashoutAdminActivity,"You don't have any cashout transaction.", Toast.LENGTH_LONG).show()
//                        }
//                    }
//
//                    override fun onCancelled(error: DatabaseError) {
//                        TODO("Not yet implemented")
//                    }
//
//                })
            }
        }
        binding.cancelBtnUpdate.setOnClickListener{
            val intentCancel = Intent(this, AdminCashout::class.java)
            startActivity(intentCancel)
            finish()
        }


    }
}