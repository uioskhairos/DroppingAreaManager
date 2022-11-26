package com.droppingareamanager.app.user

import android.os.Bundle
import android.view.*
import android.widget.AdapterView
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintSet.GONE
import androidx.constraintlayout.widget.ConstraintSet.VISIBLE
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.droppingareamanager.app.databinding.FragmentDroppedItemsBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import java.util.*
import kotlin.collections.ArrayList
import com.droppingareamanager.app.R
import com.droppingareamanager.app.adapters.ItemsModelAdapter
import com.droppingareamanager.app.models.ItemModel

class DroppedItemsFragment : Fragment(R.layout.fragment_dropped_items) {
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var itemsArrayList: ArrayList<ItemModel>
    private lateinit var filtereditemAdminArrayList: java.util.ArrayList<ItemModel>
    private lateinit var itemRecyclerView: RecyclerView
    private lateinit var newItemAdminRecyclerView: RecyclerView
    private lateinit var tvLoadingData: TextView
    private lateinit var binding: FragmentDroppedItemsBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding = FragmentDroppedItemsBinding.bind(view)

        itemRecyclerView = requireView().findViewById(R.id.itemsListRecyclerview)
        newItemAdminRecyclerView = requireView().findViewById(R.id.itemsListRecyclerview)
        tvLoadingData = requireView().findViewById(R.id.tvLoadingData)
        itemRecyclerView.layoutManager = LinearLayoutManager(this.context)
        itemRecyclerView.setHasFixedSize(true)

        itemsArrayList= arrayListOf<ItemModel>()
        filtereditemAdminArrayList= arrayListOf<ItemModel>()

        auth = Firebase.auth
        //get user account type
        val uId = auth.currentUser?.uid.toString()

        database = FirebaseDatabase.getInstance().getReference("Items")

        database.orderByChild("sellerUid").equalTo(uId).addValueEventListener(object :ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                itemsArrayList.clear()
                filtereditemAdminArrayList.clear()
                if (snapshot.exists()){
                    for (i in snapshot.children){
                        val user = i.getValue(ItemModel::class.java)
                        itemsArrayList.add(user!!)
                    }
                    filtereditemAdminArrayList.addAll(itemsArrayList)
                    val mAdapter = ItemsModelAdapter(filtereditemAdminArrayList)
                    itemRecyclerView.adapter = mAdapter
                }
                else{
                    itemRecyclerView.visibility = GONE
                    binding.tvEmpty.visibility = VISIBLE
                    Toast.makeText(this@DroppedItemsFragment.context,"You don't have any item dropped.", Toast.LENGTH_LONG).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })

        val spinner = binding.spinnerSortItems
        spinner.onItemSelectedListener = object :
            AdapterView.OnItemSelectedListener{
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                if (itemsArrayList.isNotEmpty()){
                    if (spinner.selectedItem == "All"){
                        filtereditemAdminArrayList.clear()
                        filtereditemAdminArrayList.addAll(itemsArrayList)
                        newItemAdminRecyclerView.adapter!!.notifyDataSetChanged()
                    }
                    if (spinner.selectedItem == "Dropped"){
                        val status = "dropped"
                        filterItems(status)
                    }
                    if (spinner.selectedItem == "Pending"){
                        val status = ""
                        filterItems(status)
                    }
                    if (spinner.selectedItem == "Claimed"){
                        val status = "claimed"
                        filterItems(status)
                    }
                    if (spinner.selectedItem == "Pulled out"){
                        val status = "pulled out"
                        filterItems(status)
                    }
                    if (spinner.selectedItem == "Declined"){
                        val status = "declined"
                        filterItems(status)
                    }
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
//                binding.selectSort.text = "Please select an option"
            }

        }
    }

    private fun filterItems(status: String) {
        filtereditemAdminArrayList.clear()
        itemsArrayList.forEach{
            if(it.status == status){
                filtereditemAdminArrayList.add(it)
            }
        }
        newItemAdminRecyclerView.adapter!!.notifyDataSetChanged()

    }


    private fun getItemData() {
//        userRecyclerView.visibility = view.GONE

    }
}