package com.droppingareamanager.app.admin

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.droppingareamanager.app.Login
import com.droppingareamanager.app.databinding.ActivityAdminDashboardBinding
import com.droppingareamanager.app.models.UserModel
import com.droppingareamanager.app.user.DashboardActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import java.text.DecimalFormat

class AdminDashboard : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var dbUser: DatabaseReference
    private lateinit var dbItem: DatabaseReference
    private lateinit var dbCashout: DatabaseReference
    private lateinit var binding: ActivityAdminDashboardBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminDashboardBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        auth = Firebase.auth
        //get user account type
        val userId = auth.currentUser?.uid.toString()
        dbUser = FirebaseDatabase.getInstance().getReference("User")
        dbItem = FirebaseDatabase.getInstance().getReference("Items")
        dbCashout = FirebaseDatabase.getInstance().getReference("Cashouts")

        var accountType: String?
        dbUser.child(userId).get().addOnSuccessListener {
            accountType = it.child("accountType").value.toString()
            if (accountType == "Dropper"){
                startActivity(Intent(this, DashboardActivity::class.java))
            }
        }.addOnFailureListener{
            Log.e("firebase", "Error getting data", it)
        }

        dbUser.child(userId).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()){
                    val user = snapshot.getValue(UserModel::class.java)
                    binding.userData = user
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
        //get total items pick up
        dbItem.addValueEventListener(object :
            ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()){
                    var sumPickedUp = 0
                    var sumAmount=0
                    for (i in snapshot.children){
                        val status = i.child("status").value.toString()
                        val hf = i.child("itemHandlingFee").value.toString()
                        val amount = i.child("itemAmount").value.toString()
                        if(status == "claimed"){
                            sumPickedUp += hf.toInt()
                            sumAmount += amount.toInt()
                        }
                    }
                    //total picked up and itemAmount
                    val totalSumPickedup= sumPickedUp*0.65
                    val totalPickedUp = totalSumPickedup+sumAmount
                    dbCashout.addValueEventListener(object :
                        ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            var sumCashout = 0
                            for (i in snapshot.children){
                                val status = i.child("status").value.toString()
                                val amount = i.child("cashoutAmount").value.toString()
                                if (status == "pending" || status == "completed"){
                                    sumCashout += amount.toInt()
                                }
                            }
                            val totalCashout = sumCashout
                            dbUser.child(userId).child("cashout").setValue(totalCashout.toString())
                            val total = totalPickedUp.toInt() - totalCashout
                            dbItem.orderByChild("sellerRefUid").equalTo(userId).addValueEventListener(object :
                                ValueEventListener {
                                override fun onDataChange(snapshot: DataSnapshot) {
                                    if (snapshot.exists()){
                                        var sumRefCom = 0
                                        for (i in snapshot.children){
                                            val status = i.child("status").value.toString()
                                            val hf = i.child("itemHandlingFee").value.toString()
                                            if (status == "claimed"){
                                                sumRefCom += hf.toInt()
                                            }
                                        }
                                        val totalRefCom = sumRefCom*0.15555
                                        val totalBalance = Integer.sum(total, totalRefCom.toInt())
                                        //add comma
                                        val inputValue = totalBalance.toString()
                                        val number = java.lang.Double.valueOf(inputValue)
                                        val dec = DecimalFormat("#,###,###")
                                        val finalOutputBalance = dec.format(number)
                                        binding.balance.text = "₱$finalOutputBalance.00"
                                        dbUser.child(userId).child("balance").setValue(totalBalance.toString())
                                    }
                                    else{
                                        val totalRefCom = 0
                                        val totalBalance = Integer.sum(total, totalRefCom)
                                        binding.balance.text = "₱$totalBalance.00"
                                        dbUser.child(userId).child("balance").setValue(totalBalance.toString())
                                    }
                                }

                                override fun onCancelled(error: DatabaseError) {
                                    binding.balance.text = "Loading..."
                                }

                            })
                        }

                        override fun onCancelled(error: DatabaseError) {
                            binding.balance.text = "Loading..."
                        }

                    })
                }else{

                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })

        binding.dropItemDashboard.setOnClickListener{
            // move to manage items
            val intent = Intent(this, ApproveItemsAdminActivity::class.java)
            startActivity(intent)
        }
        binding.cashoutDashboard.setOnClickListener{
            val intent = Intent(this, AdminCashout::class.java)
            startActivity(intent)
        }
        binding.searchDropper.setOnClickListener{
            val intent = Intent(this, DroppersAdmin::class.java)
            startActivity(intent)
        }
        binding.logoutBtn.setOnClickListener{
            val intent = Intent(this, Login::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
            auth.signOut()
        }
        binding.manageDashboard.setOnClickListener{
            val intent = Intent(this, SalesActivity::class.java)
            intent.putExtra("balance", binding.balance.text.toString())
            startActivity(intent)
        }
    }
}