package com.droppingareamanager.app.admin

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.SearchView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintSet.GONE
import androidx.constraintlayout.widget.ConstraintSet.VISIBLE
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.droppingareamanager.app.Login
import com.droppingareamanager.app.R
import com.droppingareamanager.app.adapters.CashoutAdminModelAdapter
import com.droppingareamanager.app.databinding.ActivityAdminCashoutBinding
import com.droppingareamanager.app.models.AdminCashoutModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.util.*

class AdminCashout : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var cashoutAdminArrayList: ArrayList<AdminCashoutModel>
    private lateinit var filteredCashoutAdminArrayList: ArrayList<AdminCashoutModel>
    private lateinit var cashoutAdminRecyclerView: RecyclerView
    private lateinit var newCashoutAdminRecyclerView: RecyclerView
    private lateinit var binding: ActivityAdminCashoutBinding
    private lateinit var searchViewCashoutAdmin: SearchView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminCashoutBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        auth = Firebase.auth

        cashoutAdminRecyclerView = findViewById(R.id.cashoutsListAdmin)
        newCashoutAdminRecyclerView = findViewById(R.id.cashoutsListAdmin)
        searchViewCashoutAdmin = findViewById(R.id.search_cashoutAdmin)
        cashoutAdminRecyclerView.layoutManager = LinearLayoutManager(this)
        cashoutAdminRecyclerView.setHasFixedSize(true)

        cashoutAdminArrayList= arrayListOf<AdminCashoutModel>()
        filteredCashoutAdminArrayList= arrayListOf<AdminCashoutModel>()

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

        binding.addCashoutFABBtnAdmin.setOnClickListener{
            val intent = Intent(this, AddCashoutsDialogAdminActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
        }
        binding.tvLoadingData.visibility = VISIBLE
        getUserData()

        searchViewCashoutAdmin.clearFocus()
        searchViewCashoutAdmin.setOnQueryTextListener(object : SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean {
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {

                filteredCashoutAdminArrayList.clear()
                if (cashoutAdminArrayList.isNotEmpty()) {
                    val searchText = newText!!.lowercase(Locale.getDefault())
                    if (searchText.isNotEmpty()) {
                        cashoutAdminArrayList.forEach {
                            if (it.shopName?.lowercase(Locale.getDefault())!!
                                    .contains(searchText)
                            ) {
                                filteredCashoutAdminArrayList.add(it)
                            }
                        }
                        newCashoutAdminRecyclerView.adapter!!.notifyDataSetChanged()
                    } else {
                        filteredCashoutAdminArrayList.clear()
                        filteredCashoutAdminArrayList.addAll(cashoutAdminArrayList)
                        newCashoutAdminRecyclerView.adapter!!.notifyDataSetChanged()
                    }
                }
                return false
            }

        })
        if (cashoutAdminArrayList.isNotEmpty()){
            val spinner = binding.spinnerSortItems
            spinner.onItemSelectedListener = object :
                AdapterView.OnItemSelectedListener{
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    if (spinner.selectedItem == "All"){
                        filteredCashoutAdminArrayList.clear()
                        filteredCashoutAdminArrayList.addAll(cashoutAdminArrayList)
                        newCashoutAdminRecyclerView.adapter!!.notifyDataSetChanged()
                        if (filteredCashoutAdminArrayList.isEmpty()){
                            cashoutAdminRecyclerView.visibility = GONE
                            binding.tvEmpty.visibility = VISIBLE
                        }
                    }
                    if (spinner.selectedItem == "Pending"){
                        val status = "pending"
                        filterItems(status)
                    }
                    if (spinner.selectedItem == "Completed"){
                        val status = "completed"
                        filterItems(status)
                    }
                    if (spinner.selectedItem == "Declined"){
                        val status = "declined"
                        filterItems(status)
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
//                binding.selectSort.text = "Please select an option"
                }

            }
        }
    }


    private fun getUserData() {
        auth = Firebase.auth
        database = Firebase.database.reference
        database = FirebaseDatabase.getInstance().getReference("Cashouts")
        database.addValueEventListener(object :ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                cashoutAdminArrayList.clear()
                filteredCashoutAdminArrayList.clear()
                if (snapshot.exists()){
                    binding.tvLoadingData.visibility = GONE
                    for (i in snapshot.children){
                        val cashout = i.getValue(AdminCashoutModel::class.java)
                        cashout?.id = i.key
                        cashoutAdminArrayList.add(cashout!!)
                    }

                    filteredCashoutAdminArrayList.addAll(cashoutAdminArrayList)
                    val mAdapter = CashoutAdminModelAdapter(filteredCashoutAdminArrayList, this@AdminCashout)
                    cashoutAdminRecyclerView.adapter = mAdapter
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
    }
    private fun filterItems(status: String) {
        filteredCashoutAdminArrayList.clear()
        cashoutAdminArrayList.forEach{
            if(it.status == status){
                filteredCashoutAdminArrayList.add(it)
            }
        }
        newCashoutAdminRecyclerView.adapter!!.notifyDataSetChanged()

    }
}