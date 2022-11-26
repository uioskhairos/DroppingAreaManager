package com.droppingareamanager.app.user

import android.os.Bundle
import android.view.*
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintSet.GONE
import androidx.constraintlayout.widget.ConstraintSet.VISIBLE
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import kotlin.collections.ArrayList
import com.droppingareamanager.app.R
import com.droppingareamanager.app.adapters.CashoutModelAdapter
import com.droppingareamanager.app.databinding.FragmentCashoutBinding
import com.droppingareamanager.app.models.CashoutModel

class CashoutFragment : Fragment(R.layout.fragment_cashout) {
    private lateinit var auth: FirebaseAuth
    private lateinit var dbCashout: DatabaseReference
    private lateinit var cashoutArrayList: ArrayList<CashoutModel>
    private lateinit var cashoutRecyclerView: RecyclerView
    private lateinit var tvLoadingData: TextView
    private lateinit var binding: FragmentCashoutBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding = FragmentCashoutBinding.bind(view)

        cashoutRecyclerView = requireView().findViewById(R.id.cashoutsList)
        tvLoadingData = requireView().findViewById(R.id.tvLoadingData)
        cashoutRecyclerView.layoutManager = LinearLayoutManager(this.context)
        cashoutRecyclerView.setHasFixedSize(true)

        cashoutArrayList= arrayListOf<CashoutModel>()

        auth = Firebase.auth
        //get user account type
        val uId = auth.currentUser?.uid.toString()
        dbCashout = FirebaseDatabase.getInstance().getReference("Cashouts")
        dbCashout.orderByChild("uid").equalTo(uId).addValueEventListener(object :ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                cashoutArrayList.clear()
                if (snapshot.exists()){
                    for (i in snapshot.children){
                        val user = i.getValue(CashoutModel::class.java)
                        cashoutArrayList.add(user!!)
                    }
                    val mAdapter = CashoutModelAdapter(cashoutArrayList)
                    cashoutRecyclerView.adapter = mAdapter
                }
                else{
                    cashoutRecyclerView.visibility = GONE
                    binding.tvEmpty.visibility = VISIBLE
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }
}