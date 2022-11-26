package com.droppingareamanager.app.admin

import android.app.DatePickerDialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.SearchView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.droppingareamanager.app.Login
import com.droppingareamanager.app.R
import com.droppingareamanager.app.adapters.ItemAdminModelAdapter
import com.droppingareamanager.app.databinding.ActivityApproveItemsAdminBinding
import com.droppingareamanager.app.models.AdminItemModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.*

class ApproveItemsAdminActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var itemAdminArrayList: ArrayList<AdminItemModel>
    private lateinit var allItemArrayList: ArrayList<AdminItemModel>
    private lateinit var filtereditemAdminArrayList: ArrayList<AdminItemModel>
    private lateinit var itemAdminRecyclerView: RecyclerView
    private lateinit var newItemAdminRecyclerView: RecyclerView
    private lateinit var binding: ActivityApproveItemsAdminBinding
    private lateinit var searchViewItemAdmin: SearchView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityApproveItemsAdminBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        auth = Firebase.auth

        itemAdminRecyclerView = findViewById(R.id.itemsListAdmin)
        newItemAdminRecyclerView = findViewById(R.id.itemsListAdmin)
        searchViewItemAdmin = findViewById(R.id.search_itemAdmin)
        itemAdminRecyclerView.layoutManager = LinearLayoutManager(this)
        itemAdminRecyclerView.setHasFixedSize(true)

        allItemArrayList= arrayListOf<AdminItemModel>()
        itemAdminArrayList= arrayListOf<AdminItemModel>()
        filtereditemAdminArrayList= arrayListOf<AdminItemModel>()


        binding.backBtn.setOnClickListener{
            val intent = Intent(this, AdminDashboard::class.java)
            startActivity(intent)
            finish()
        }
        binding.logoutBtn.setOnClickListener{
            val intent = Intent(this, Login::class.java)
            startActivity(intent)
            finish()
            auth.signOut()
        }

        binding.addItemFABBtnAdmin.setOnClickListener{
            val intent = Intent(this, AddItemsDialogAdminActivity::class.java)
            startActivity(intent)
            finish()
        }
        getUserDatas()
        binding.buttonStatusPending.setOnClickListener{
            val status = ""
            binding.tvLoadingData.visibility = View.VISIBLE
            getUserData(status)
        }
        binding.buttonStatusDropped.setOnClickListener{
            val status = "dropped"
            binding.tvLoadingData.visibility = View.VISIBLE
            getUserData(status)
        }

        searchViewItemAdmin.clearFocus()
        searchViewItemAdmin.setOnQueryTextListener(object : SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean {
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {

                filtereditemAdminArrayList.clear()
                val searchText = newText!!.lowercase(Locale.getDefault())
                if (searchText.isNotEmpty()){
                    itemAdminArrayList.forEach{
                        if(it.buyerFullName?.lowercase(Locale.getDefault())!!.contains(searchText)||
                            it.sellerFullName?.lowercase(Locale.getDefault())!!.contains(searchText)||
                            it.sellerShopName?.lowercase(Locale.getDefault())!!.contains(searchText)){
                            filtereditemAdminArrayList.add(it)
                        }
                    }

                    newItemAdminRecyclerView.adapter!!.notifyDataSetChanged()
                }
                else{
                    filtereditemAdminArrayList.clear()
                    filtereditemAdminArrayList.addAll(itemAdminArrayList)
                    newItemAdminRecyclerView.adapter!!.notifyDataSetChanged()
                }
                return false
            }

        })

        val myCalendar = Calendar.getInstance()
        val datePicker = DatePickerDialog.OnDateSetListener { views, year, month, dayOfMonth ->
            myCalendar.set(Calendar.YEAR, year)
            myCalendar.set(Calendar.MONTH, month)
            myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            updateable(myCalendar)
        }
        val datePicker2 = DatePickerDialog.OnDateSetListener { views, year, month, dayOfMonth ->
            myCalendar.set(Calendar.YEAR, year)
            myCalendar.set(Calendar.MONTH, month)
            myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            updateable2(myCalendar)
        }
        binding.button.setOnClickListener {
            DatePickerDialog(this, datePicker, myCalendar.get(Calendar.YEAR),
                myCalendar.get(Calendar.MONTH), myCalendar.get(Calendar.DAY_OF_MONTH)).show()
        }
        binding.button2.setOnClickListener {
            DatePickerDialog(this, datePicker2, myCalendar.get(Calendar.YEAR),
                myCalendar.get(Calendar.MONTH), myCalendar.get(Calendar.DAY_OF_MONTH)).show()
        }

        binding.sortByDateButton.setOnClickListener {
            val spinner = binding.spinnerSortDate
            val startDate = binding.startDate.text.toString()
            val endDate = binding.endDate.text.toString()
            if (binding.startDate.text.isNullOrEmpty()){
                binding.startDate.error = "Start Date is required."
                binding.startDate.requestFocus()
                return@setOnClickListener
            }
            if (binding.endDate.text.isNullOrEmpty()){
                binding.endDate.error = "End Date is required."
                binding.endDate.requestFocus()
                return@setOnClickListener
            }
            binding.startDate.setError(null,null)
            binding.endDate.setError(null,null)
            if (spinner.selectedItem == "Select"){
                val errorText = spinner.selectedView as TextView
                errorText.error = "anything here, just to add the icon"
                errorText.setTextColor(Color.RED) //just to highlight that this is an error
                errorText.text = "Please select Sort Type" //changes the selected item text to this
            }
            if (spinner.selectedItem == "Date Claimed"){
                filtereditemAdminArrayList.clear()
                allItemArrayList.forEach{
                    if (it.dateClaimed!! in startDate..endDate &&it.dateClaimed!=null &&it.dateClaimed!=""){
                        filtereditemAdminArrayList.add(it)
                    }
                }
                itemAdminArrayList.clear()
                itemAdminArrayList.addAll(filtereditemAdminArrayList)
                newItemAdminRecyclerView.adapter!!.notifyDataSetChanged()
            }
            if (spinner.selectedItem == "Date Dropped"){
                filtereditemAdminArrayList.clear()
                allItemArrayList.forEach{
                    if (it.dateDropped!! >= startDate){
                        if (it.dateDropped!! <= endDate){
                            if (it.dateDropped!=null){
                                if (it.dateDropped!=""){
                                    filtereditemAdminArrayList.add(it)
                                }
                            }
                        }
                    }
                }
                itemAdminArrayList.clear()
                itemAdminArrayList.addAll(filtereditemAdminArrayList)
                newItemAdminRecyclerView.adapter!!.notifyDataSetChanged()
            }
        }
    }




    private fun getUserData(status: String) {
    filtereditemAdminArrayList.clear()
        allItemArrayList.forEach{
            if(it.status == status){
                filtereditemAdminArrayList.add(it)
            }
        }
        itemAdminArrayList.clear()
        itemAdminArrayList.addAll(filtereditemAdminArrayList)
        binding.tvLoadingData.visibility = View.GONE
        binding.tvEmpty.visibility = View.GONE
        newItemAdminRecyclerView.adapter!!.notifyDataSetChanged()
    }
    private fun getUserDatas() {
        auth = Firebase.auth
        database = Firebase.database.reference
        //get user account type
        val uId = auth.currentUser?.uid.toString()
        database = FirebaseDatabase.getInstance().getReference("Items")
        database.addValueEventListener(object :ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                itemAdminArrayList.clear()
                filtereditemAdminArrayList.clear()
                if (snapshot.exists()){
                    binding.tvLoadingData.visibility = View.GONE
                    for (i in snapshot.children){
                        val user = i.getValue(AdminItemModel::class.java)
                        user?.id = i.key
                        itemAdminArrayList.add(user!!)
                        allItemArrayList.add(user)
                    }

                    filtereditemAdminArrayList.addAll(itemAdminArrayList)
                    val mAdapter = ItemAdminModelAdapter(filtereditemAdminArrayList, this@ApproveItemsAdminActivity)
                    itemAdminRecyclerView.adapter = mAdapter
                }
                else{
                    binding.tvLoadingData.visibility = View.GONE
                    binding.tvEmpty.visibility = View.VISIBLE
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }
    private fun updateable(myCalendar: Calendar) {
        val myFormat = "yyyy-MM-dd"
        val sdf = SimpleDateFormat(myFormat)
        binding.startDate.setText(sdf.format(myCalendar.time))

    }
    private fun updateable2(myCalendar: Calendar) {
        val myFormat = "yyyy-MM-dd"
        val sdf = SimpleDateFormat(myFormat)
        binding.endDate.setText(sdf.format(myCalendar.time))

    }
}