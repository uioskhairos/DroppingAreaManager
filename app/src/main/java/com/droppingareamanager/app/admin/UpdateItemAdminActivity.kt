package com.droppingareamanager.app.admin

import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View.GONE
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import com.droppingareamanager.app.R
import com.droppingareamanager.app.databinding.ActivityUpdateItemAdminActivityBinding
import com.droppingareamanager.app.models.NotificationModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class UpdateItemAdminActivity : AppCompatActivity() {
    private lateinit var binding: ActivityUpdateItemAdminActivityBinding
    private lateinit var dbItem: DatabaseReference
    private lateinit var auth: FirebaseAuth

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUpdateItemAdminActivityBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        dbItem = FirebaseDatabase.getInstance().getReference("Items")
        val intent = intent
        val itemId =intent.getStringExtra("itemId").toString()
        val buyerFullName =intent.getStringExtra("buyerFullName")
        val sellerShopName =intent.getStringExtra("sellerShopName")
        val itemAmount =intent.getStringExtra("itemAmount").toString()
        val action =intent.getStringExtra("action")
        val sellerId =intent.getStringExtra("sellerId").toString()
        val status =intent.getStringExtra("status").toString()
        binding.buyerNameApprove.text = buyerFullName
        binding.sellerNameApprove.text = sellerShopName
        binding.tvAmount.text = "₱$itemAmount.00"


        if (action=="approve"){
            if (status == ""){
                binding.updateQuestion.text = "Drop Item"
                binding.editAmount.visibility = GONE
                binding.editHandlingFee.visibility = GONE
            }
            if (status == "dropped"){
                binding.editAmount.visibility = GONE
                binding.approveHandlingFee.visibility = GONE
                binding.updateQuestion.text = "Claim Item"
                binding.editHandlingFee.visibility = GONE
            }
        }
        if (action=="decline"){
            if (status == ""){
                binding.editAmount.visibility = GONE
                binding.approveHandlingFee.visibility = GONE
                binding.updateQuestion.text = "Decline Item"
                binding.editHandlingFee.visibility = GONE
            }
            if (status == "dropped"){
                binding.editAmount.visibility = GONE
                binding.approveHandlingFee.visibility = GONE
                binding.updateQuestion.text = "Pullout Item"
                binding.editHandlingFee.visibility = GONE
            }

        }
        if (action=="edit"){
            binding.approveHandlingFee.visibility = GONE
            binding.updateQuestion.text = "Edit Item"
        }
        if (action=="delete"){
            binding.editAmount.visibility = GONE
            binding.approveHandlingFee.visibility = GONE
            binding.updateQuestion.text = "Delete Item"
            binding.editHandlingFee.visibility = GONE
        }

//        val hf = binding.approveHandlingFee.text.toString().trim()

        binding.confirmBtnUpdate.setOnClickListener{
            // get date dropped
            //time
            val current = LocalDate.now()
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
            val time = current.format(formatter).toString().trim()
            val mDialogView = LayoutInflater.from(this).inflate(R.layout.fragment_loading_dialog,null)
            val mBuilder = AlertDialog.Builder(this)
                .setView(mDialogView)
            val mAlertDialog = mBuilder.show()
            if (action=="approve" && status == "") {
                if (binding.approveHandlingFee.text!!.isNotEmpty()){
                    dbItem.child(itemId).child("itemHandlingFee")
                        .setValue(binding.approveHandlingFee.text.toString().trim())
                        .addOnCompleteListener {
                            dbItem.child(itemId).child("status").setValue("dropped")
                                .addOnCompleteListener {
                                    dbItem.child(itemId).child("dateDropped").setValue(time)
                                    val intentCancel =
                                        Intent(this, ApproveItemsAdminActivity::class.java)
                                    //to prevent user from returning to Register Activity
                                    intent.flags =
                                        Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                                    startActivity(intentCancel)
                                    mAlertDialog.dismiss()
                                    finish()
                                    Toast.makeText(
                                        this,
                                        "Item dropped successfully",
                                        Toast.LENGTH_SHORT
                                    )
                                        .show()
                                }.addOnFailureListener { err ->
                                mAlertDialog.dismiss()
                                Toast.makeText(
                                    this,
                                    "Update Error ${err.message}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                        .addOnFailureListener { err ->
                            mAlertDialog.dismiss()
                            Toast.makeText(this, "Update Error ${err.message}", Toast.LENGTH_SHORT)
                                .show()
                        }
                }
                else{
                    mAlertDialog.dismiss()
                    binding.approveHandlingFee.error = "Handling Fee is required."
                    binding.approveHandlingFee.requestFocus()
                    return@setOnClickListener
                }

            }
            if (action=="edit"){
                if (binding.editAmount.text.toString()!=""){
                    dbItem.child(itemId).child("itemAmount").setValue(binding.editAmount.text.toString().trim()).addOnCompleteListener {
                        val intentCancel = Intent(this, ApproveItemsAdminActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                        startActivity(intentCancel)
                        mAlertDialog.dismiss()
                        finish()
                        Toast.makeText(this, "Item amount changed successfully", Toast.LENGTH_SHORT)
                            .show()
                    }
                        .addOnFailureListener {
                                err ->
                            val intentCancel = Intent(this, ApproveItemsAdminActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                            startActivity(intentCancel)
                            mAlertDialog.dismiss()
                            Toast.makeText(this, "Update Error ${err.message}", Toast.LENGTH_SHORT).show()
                        }
                }
                if (binding.editHf.text.toString()!=""){
                    dbItem.child(itemId).child("itemHandlingFee").setValue(binding.editHf.text.toString().trim()).addOnCompleteListener {
                        val intentCancel = Intent(this, ApproveItemsAdminActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                        startActivity(intentCancel)
                        mAlertDialog.dismiss()
                        finish()
                        Toast.makeText(this, "Handling Fee changed successfully", Toast.LENGTH_SHORT)
                            .show()
                    }
                        .addOnFailureListener {
                                err ->
                            Toast.makeText(this, "Update Error ${err.message}", Toast.LENGTH_SHORT).show()
                        }
                }
                mAlertDialog.dismiss()
            }
            if (action=="decline" && status == ""){
                dbItem.child(itemId).child("status").setValue("declined").addOnCompleteListener {
                    val intentCancel = Intent(this, ApproveItemsAdminActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(intentCancel)
                    mAlertDialog.dismiss()
                    finish()
                    Toast.makeText(this, "Item declined successfully", Toast.LENGTH_SHORT)
                        .show()
                }.addOnFailureListener {
                        err -> mAlertDialog.dismiss()
                    Toast.makeText(this, "Update Error ${err.message}", Toast.LENGTH_SHORT).show()
                }
            }
            if (action=="approve" && status == "dropped"){
                dbItem.child(itemId).child("status").setValue("claimed").addOnCompleteListener {
                    dbItem.child(itemId).child("dateClaimed").setValue(time)
                    val intentCancel = Intent(this, ApproveItemsAdminActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(intentCancel)
                    mAlertDialog.dismiss()
//                    createNotificationChannel()
                    val name = buyerFullName.toString()
                    auth = Firebase.auth
                    //get user account type
                    val userId = auth.currentUser?.uid.toString()
//                    if (sellerId == userId){
//
//                    }
//                    sendNotification(name)
                    sendFirebaseNotification("Item Claimed", "$name's item was claimed.", sellerId)
                    finish()
                    Toast.makeText(this, "Item claimed successfully", Toast.LENGTH_SHORT)
                        .show()
                }.addOnFailureListener {
                        err -> mAlertDialog.dismiss()
                    Toast.makeText(this, "Update Error ${err.message}", Toast.LENGTH_SHORT).show()
                }
            }
            if (action=="decline" && status =="dropped"){
                dbItem.child(itemId).child("status").setValue("pulled out").addOnCompleteListener {
                    val intentCancel = Intent(this, ApproveItemsAdminActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(intentCancel)
                    mAlertDialog.dismiss()
                    finish()
                    Toast.makeText(this, "Item pulled out successfully", Toast.LENGTH_SHORT)
                        .show()
                }.addOnFailureListener {
                        err -> mAlertDialog.dismiss()
                    Toast.makeText(this, "Update Error ${err.message}", Toast.LENGTH_SHORT).show()
                }
            }
            if (action=="delete"){
                dbItem.child(itemId).removeValue().addOnCompleteListener {
                    val intentCancel = Intent(this, ApproveItemsAdminActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(intentCancel)
                    mAlertDialog.dismiss()
                    finish()
                    Toast.makeText(this, "Item deleted successfully", Toast.LENGTH_SHORT)
                        .show()
                }.addOnFailureListener {
                        err -> mAlertDialog.dismiss()
                    Toast.makeText(this, "Update Error ${err.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
        binding.cancelBtnUpdate.setOnClickListener{
            val intentCancel = Intent(this, ApproveItemsAdminActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intentCancel)
            finish()
        }


    }

    private fun sendFirebaseNotification(title: String, text: String, receiverId: String) {
        val notification = NotificationModel(text,title,receiverId)

        FirebaseDatabase
            .getInstance()
            .getReference("Notification")
            .push()
            .setValue(notification).addOnCompleteListener {
                if (it.isSuccessful){
                    Toast.makeText(this, "Notification sent successfully", Toast.LENGTH_SHORT)
                        .show()
                }
            }
    }

}