package com.droppingareamanager.app.admin

import android.content.Intent
import android.os.Bundle
import android.widget.SearchView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintSet.GONE
import androidx.constraintlayout.widget.ConstraintSet.VISIBLE
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.droppingareamanager.app.Login
import com.droppingareamanager.app.R
import com.droppingareamanager.app.adapters.DroppersModelAdapter
import com.droppingareamanager.app.databinding.ActivityDroppersAdminBinding
import com.droppingareamanager.app.models.UserModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import java.util.*

class DroppersAdmin : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var dbItems: DatabaseReference
    private lateinit var dbUserRef: DatabaseReference
    private lateinit var dbCashout: DatabaseReference
    private lateinit var droppersAdminArrayList: ArrayList<UserModel>
    private lateinit var filteredDroppersAdminArrayList: ArrayList<UserModel>
    private lateinit var droppersAdminRecyclerView: RecyclerView
    private lateinit var newDropppersAdminRecyclerView: RecyclerView
    private lateinit var binding: ActivityDroppersAdminBinding
    private lateinit var searchViewDroppersAdmin: SearchView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDroppersAdminBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        auth = Firebase.auth

        droppersAdminRecyclerView = findViewById(R.id.droppersListAdmin)
        newDropppersAdminRecyclerView = findViewById(R.id.droppersListAdmin)
        searchViewDroppersAdmin = findViewById(R.id.search_droppersAdmin)
        droppersAdminRecyclerView.layoutManager = LinearLayoutManager(this)
        droppersAdminRecyclerView.setHasFixedSize(true)

        droppersAdminArrayList= arrayListOf<UserModel>()
        filteredDroppersAdminArrayList= arrayListOf<UserModel>()

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

        binding.tvLoadingData.visibility = VISIBLE

        auth = Firebase.auth
        dbUserRef = FirebaseDatabase.getInstance().getReference("User")
        dbItems = FirebaseDatabase.getInstance().getReference("Items")
        dbCashout = FirebaseDatabase.getInstance().getReference("Cashouts")
//        val dbStatus = database.orderByChild("status").equalTo("pending")
        dbUserRef.addValueEventListener(object :ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                droppersAdminArrayList.clear()
                filteredDroppersAdminArrayList.clear()
                if (snapshot.exists()){
                    binding.tvLoadingData.visibility = GONE
                    for (i in snapshot.children){
                        if (i.key!= auth.currentUser?.uid){
                        val dropper = i.getValue(UserModel::class.java)
                        dropper?.id = i.key
                        auth = Firebase.auth
                        //get user account type
                        val userId = i.key.toString()
                        //get total items pick up
                        dbItems.orderByChild("sellerUid").equalTo(userId).addValueEventListener(object :
                            ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                if (snapshot.exists()){
                                    var sumPickedUp = 0f
                                    var sumAmount=0f
                                    for (item in snapshot.children){
                                        val status = item.child("status").value.toString()
                                        val hf = item.child("itemHandlingFee").value.toString()
                                        val amount = item.child("itemAmount").value.toString()
                                        val dateS = item.child("dateClaimed").value.toString()
                                        if(status == "claimed" && item.child("dateClaimed").value!=null){
                                            val promo = "2022-08-01"
                                            if (dateS.isNotEmpty()){
                                                if (dateS < promo){
                                                    sumPickedUp += hf.toFloat()*0.5f
                                                }
                                                if(dateS >= promo){
                                                    sumPickedUp += hf.toFloat() * 0.15f
                                                }
                                            }else{
                                                sumPickedUp += hf.toFloat()*0.5f
                                            }

                                            sumAmount += amount.toFloat()
                                        }
                                    }
                                    //total picked up and itemAmount
//                    Toast.makeText(this@HomeFragment.context,"$sumPickedUp", Toast.LENGTH_LONG).show()
                                    val totalPickedUp = sumPickedUp+sumAmount
                                    dbCashout.orderByChild("uid").equalTo(userId).addValueEventListener(object :
                                        ValueEventListener {
                                        override fun onDataChange(snapshot: DataSnapshot) {
                                            var sumCashout = 0
                                            for (cashOut in snapshot.children){
                                                val status = cashOut.child("status").value.toString()
                                                val amount = cashOut.child("cashoutAmount").value.toString()
                                                if (status == "pending" || status == "completed"){
                                                    sumCashout += amount.toInt()
                                                }
                                            }
                                            val totalCashout = sumCashout
                                            dbUserRef.child(userId).child("cashout").setValue(totalCashout.toString())
                                            val total = totalPickedUp.toInt() - totalCashout
                                            dbItems.orderByChild("sellerRefUid").equalTo(userId).addValueEventListener(object :
                                                ValueEventListener {
                                                override fun onDataChange(snapshot: DataSnapshot) {
                                                    if (snapshot.exists()){
                                                        var sumRefCom = 0
                                                        for (items in snapshot.children){
                                                            val status = items.child("status").value.toString()
                                                            val hf = items.child("itemHandlingFee").value.toString()
                                                            if (status == "claimed"){
                                                                sumRefCom += hf.toInt()
                                                            }
                                                        }
                                                        val totalRefCom = sumRefCom*0.15
                                                        val totalBalance = Integer.sum(total, totalRefCom.toInt())
                                                        dbUserRef.child(userId).child("balance").setValue(totalBalance.toString())

                                                    }
                                                    else{
                                                        val totalRefCom = 0
                                                        val totalBalance = Integer.sum(total, totalRefCom)
                                                        dbUserRef.child(userId).child("balance").setValue(totalBalance.toString())

                                                    }
                                                }

                                                override fun onCancelled(error: DatabaseError) {
                                                    TODO("Not yet implemented")
                                                }

                                            })
                                        }

                                        override fun onCancelled(error: DatabaseError) {
                                            TODO("Not yet implemented")
                                        }

                                    })
                                }
                                else{
                                    dbUserRef.child(userId).child("balance").setValue("0")
                                }

                            }

                            override fun onCancelled(error: DatabaseError) {
                                TODO("Not yet implemented")
                            }

                        })
                        droppersAdminArrayList.add(dropper!!)
                    }
                    }

                    filteredDroppersAdminArrayList.addAll(droppersAdminArrayList)
                    val mAdapter = DroppersModelAdapter(filteredDroppersAdminArrayList)
                    droppersAdminRecyclerView.adapter = mAdapter
                }
                else{
                    binding.tvLoadingData.visibility = GONE
                    binding.tvEmpty.visibility = VISIBLE
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })

        searchViewDroppersAdmin.clearFocus()
        searchViewDroppersAdmin.setOnQueryTextListener(object : SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean {
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {

                filteredDroppersAdminArrayList.clear()
                val searchText = newText!!.lowercase(Locale.getDefault())
                if (searchText.isNotEmpty()){
                    droppersAdminArrayList.forEach{
                        if(it.userFullName?.lowercase(Locale.getDefault())!!.contains(searchText)
                            ||it.userEmail?.lowercase(Locale.getDefault())!!.contains(searchText)
                            ||it.userShopName?.lowercase(Locale.getDefault())!!.contains(searchText)){
                            filteredDroppersAdminArrayList.add(it)
                        }
                    }
                    newDropppersAdminRecyclerView.adapter!!.notifyDataSetChanged()
                }
                else{
                    filteredDroppersAdminArrayList.clear()
                    filteredDroppersAdminArrayList.addAll(droppersAdminArrayList)
                    newDropppersAdminRecyclerView.adapter!!.notifyDataSetChanged()
                }
                return false
            }

        })
    }
}