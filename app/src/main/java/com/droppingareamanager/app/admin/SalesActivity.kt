package com.droppingareamanager.app.admin

import android.content.ContentValues
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.droppingareamanager.app.Login
import com.droppingareamanager.app.databinding.ActivitySalesBinding
import com.droppingareamanager.app.models.SalesModel
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.user_settings_dialog.view.*
import java.io.Serializable
import java.text.DecimalFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.Year
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.*
import com.droppingareamanager.app.R

class SalesActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySalesBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var dbItems: DatabaseReference
    private lateinit var dbUserRef: DatabaseReference
    private lateinit var dbCashout: DatabaseReference
    private val itemsArrayList= arrayListOf<Any>()
    private val newitemsArrayList= arrayListOf<Any>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySalesBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        auth = Firebase.auth
        dbItems = FirebaseDatabase.getInstance().getReference("Items")
        dbUserRef = FirebaseDatabase.getInstance().getReference("User")
        dbCashout = FirebaseDatabase.getInstance().getReference("Cashouts")
        val userId = auth.currentUser?.uid.toString()
        val balance =intent.getStringExtra("balance")
        binding.balanceSettings.text = balance

//        getUserData()

        binding.backBtn.setOnClickListener{
            val intent = Intent(this, AdminDashboard::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
        }
        binding.logoutBtn.setOnClickListener{
            val intent = Intent(this, Login::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
            auth.signOut()
        }
        dbItems.orderByChild("status").equalTo("claimed").addValueEventListener(object :ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()){
                    var countItems = 0
                    var sumSales = 0
                    for (i in snapshot.children){
                        countItems += 1
                        sumSales += i.child("itemHandlingFee").value.toString().toInt()
                    }
                    val inputValue = sumSales.toString()
                    val number = java.lang.Double.valueOf(inputValue)
                    val dec = DecimalFormat("#,###,###")
                    val finalOutput = dec.format(number)
                    binding.sumSales.text = "₱$finalOutput.00"
                    binding.sumItems.text = countItems.toString()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
        dbUserRef.addValueEventListener(object :ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()){
                    var countUsers = 0
                    for (i in snapshot.children){
                        countUsers += 1
                    }
                    val totalUsers = countUsers - 1
                    binding.sumItems.text = totalUsers.toString()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
        dbCashout.orderByChild("status").equalTo("completed").addValueEventListener(object :ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()){
                    var sumCashout = 0
                    for (i in snapshot.children){
                        sumCashout += i.child("cashoutAmount").value.toString().toInt()
                    }
                    val inputValue = sumCashout.toString()
                    val number = java.lang.Double.valueOf(inputValue)
                    val dec = DecimalFormat("#,###,###")
                    val finalOutput = dec.format(number)
                    binding.sumCashouts.text = "₱$finalOutput.00"
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })

        dbUserRef.orderByChild("userId").equalTo(userId).addValueEventListener(object :ValueEventListener{
            @RequiresApi(Build.VERSION_CODES.O)
            override fun onDataChange(snapshot: DataSnapshot) {
                itemsArrayList.clear()
                if (snapshot.exists()){
                    for (i in snapshot.children){
                        binding.fullNameSettings.text = i.child("userFullName").value.toString()
                        binding.shopNameSettings.text = i.child("userShopName").value.toString()
                        binding.emailSettings.text = i.child("userEmail").value.toString()
                        val getStartDate = i.child("time").value as CharSequence?

                        //time
                        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
                        val launchDate = LocalDateTime.now()
                        val inputFormatter = DateTimeFormatter.ISO_DATE_TIME
                        val myDate = LocalDate.parse(launchDate.toString(), inputFormatter)

                        val outputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.US)
                        val final = outputFormatter.format(myDate)

                        var startDate = LocalDate.parse(getStartDate, formatter)
                        val initialStartDate = LocalDate.parse(getStartDate, formatter)
                        val endDate = LocalDate.parse(final, formatter)
                        var count = 1

                        while (startDate<=endDate){
                            val sales = SalesModel()
                            sales.date = startDate.toString()
                            // compute total
                            dbItems.orderByChild("dateClaimed").equalTo(startDate.toString()).addValueEventListener(object :ValueEventListener{
                                override fun onDataChange(snapshot: DataSnapshot) {
                                    if (snapshot.exists()){
                                        var total = 0
                                        for (i2 in snapshot.children){
                                            if (i2.child("status").value == "claimed"){
                                                total += i2.child("itemHandlingFee").value.toString().toInt()
                                            }
                                        }
                                        //get total
                                        val sumTotal = total*0.35
                                        sales.total = "$total"
                                    }else{
                                        sales.total = "0"
                                    }
                                }

                                override fun onCancelled(error: DatabaseError) {
                                    TODO("Not yet implemented")
                                }

                            })
                            itemsArrayList.add(sales)
                            newitemsArrayList.addAll(itemsArrayList)
                            startDate = initialStartDate.plusDays(count.toLong())
                            count += 1
                        }
                        binding.buttonViewDailySales.setOnClickListener {
                            val intent = Intent(this@SalesActivity, DailySalesActivity::class.java)
//                            intent.putExtra("sales", itemsArrayList)
                            intent.putExtra("startDate", initialStartDate.toString())
                            intent.putExtra("endDate", endDate.toString())

                            val `object` = itemsArrayList
                            val args = Bundle()
                            args.putSerializable("ARRAYLIST", `object` as Serializable)
                            intent.putExtra("BUNDLE", args)
                            startActivity(intent)
                        }
                        binding.buttonViewMonthlySales.setOnClickListener {
                            val intent = Intent(this@SalesActivity, MonthlySalesActivity::class.java)
//                            intent.putExtra("sales", itemsArrayList)
                            intent.putExtra("startDate", YearMonth.from(initialStartDate).toString())
                            intent.putExtra("endDate", YearMonth.from(endDate).toString())

                            val `object` = itemsArrayList
                            val args = Bundle()
                            args.putSerializable("ARRAYLIST", `object` as Serializable)
                            intent.putExtra("BUNDLE", args)
                            startActivity(intent)
                        }
                        binding.buttonViewYearlySales.setOnClickListener {
                            val intent = Intent(this@SalesActivity, YearlySalesActivity::class.java)
//                            intent.putExtra("sales", itemsArrayList)
                            intent.putExtra("startDate", Year.from(initialStartDate).toString())
                            intent.putExtra("endDate", Year.from(endDate).toString())

                            val `object` = itemsArrayList
                            val args = Bundle()
                            args.putSerializable("ARRAYLIST", `object` as Serializable)
                            intent.putExtra("BUNDLE", args)
                            startActivity(intent)
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })

        binding.changePass.setOnClickListener Listener1@{
            val mDialogView = LayoutInflater.from(this).inflate(R.layout.user_settings_dialog,null)
            val mBuilder = AlertDialog.Builder(this)
                .setView(mDialogView)
                .setTitle("Update Password")
            val mAlertDialog = mBuilder.show()
            //update button
            mDialogView.updateBtn.setOnClickListener{
                val user = auth.currentUser
                val email = user?.email
                val oldPassword = mDialogView.dialogOldPassword.text.toString()
                val newPassword = mDialogView.dialogNewPassword.text.toString()
                val cNewPassword = mDialogView.dialogCNewPassword.text.toString()
                if (oldPassword.isEmpty()){
                    mDialogView.dialogOldPassword.error = "Password is required"
                    mDialogView.dialogOldPassword.requestFocus()
                    return@setOnClickListener
                }
                if (newPassword.isEmpty()){
                    mDialogView.dialogNewPassword.error = "New Password is required"
                    mDialogView.dialogNewPassword.requestFocus()
                    return@setOnClickListener
                }
                if (cNewPassword.isEmpty()){
                    mDialogView.dialogCNewPassword.error = "Please re-enter new password"
                    mDialogView.dialogCNewPassword.requestFocus()
                    return@setOnClickListener
                }
                if(newPassword==cNewPassword){
                    // Get auth credentials from the user for re-authentication. The example below shows
                    // email and password credentials but there are multiple possible providers,
                    // such as GoogleAuthProvider or FacebookAuthProvider.
                    val credential =
                        email?.let { it1 -> EmailAuthProvider.getCredential(it1, oldPassword) };

                    // Prompt the user to re-provide their sign-in credentials
                    if (credential != null) {
                        user.reauthenticate(credential).addOnCompleteListener { it1 ->
                            if (it1.isSuccessful) {
                                user.updatePassword(newPassword).addOnCompleteListener{
                                    if (it.isSuccessful) {
                                        mAlertDialog.dismiss()
                                        Log.d(ContentValues.TAG, "Password updated")
                                        Toast.makeText(this, "Password updated successfully", Toast.LENGTH_SHORT)
                                            .show()
                                    } else {
                                        mAlertDialog.dismiss()
                                        Log.d(ContentValues.TAG, "Error password not updated")
                                        Toast.makeText(baseContext, "Error password not updated.",
                                            Toast.LENGTH_SHORT).show()
                                    }
                                }
                            } else {
                                mAlertDialog.dismiss()
                                Log.d(ContentValues.TAG, "Error auth failed")
                                Toast.makeText(baseContext, "Incorrect Password",
                                    Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }else
                {
                    mDialogView.dialogNewPassword.error = "Password does not match"
                    mDialogView.dialogNewPassword.requestFocus()
                    return@setOnClickListener
                }

            }
            mDialogView.cancelBtnDialog.setOnClickListener{
                mAlertDialog.dismiss()
            }
        }
    }

}
