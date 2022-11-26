package com.droppingareamanager.app.user

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import com.droppingareamanager.app.databinding.FragmentItemsBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import com.droppingareamanager.app.R
import com.droppingareamanager.app.models.ItemModelAdd
import com.droppingareamanager.app.models.RegisterModel

class ItemsFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var buyerName: EditText
    private lateinit var amount: EditText
    private lateinit var addItemButton: Button
    private lateinit var binding: FragmentItemsBinding
    private lateinit var dbItemRef: DatabaseReference
    private lateinit var dbUserRef: DatabaseReference


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_items, container, false)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding = FragmentItemsBinding.bind(view)

        // Initialize Firebase Auth
        auth = Firebase.auth
        dbItemRef = FirebaseDatabase.getInstance().getReference("Items")
        dbUserRef = FirebaseDatabase.getInstance().getReference("User")

        getuserData()

        buyerName = requireView().findViewById(R.id.editText_buyerName_addItem)
        amount = requireView().findViewById(R.id.editText_amount_addItem)
        addItemButton = requireView().findViewById(R.id.button_addCashout)
        val uId = auth.currentUser?.uid.toString()
        dbUserRef
            .orderByChild("userId").equalTo(uId)
            .addValueEventListener(object :
                ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()){
                        for (i in snapshot.children){
                            binding.sellerRefUid.text = i.child("userReferrerId").value.toString()
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            })

        addItemButton.setOnClickListener{
            addItemButton.isEnabled = false
            addItemButton.text = "Adding..."
            addItem()
        }

    }

    private fun getuserData() {
        val uId = auth.currentUser?.uid.toString()
        dbUserRef.child(uId).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()){
                    val userdata = snapshot.getValue(RegisterModel::class.java)
                    binding.userData = userdata
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }

    private fun addItem(){

        if (buyerName.text.isEmpty()){
            buyerName.error = "Buyer Name is required"
            buyerName.requestFocus()
            addItemButton.isEnabled = true
            addItemButton.text = "ADD ITEM"
            return
        }
        if (amount.text.isEmpty()){
            amount.error = "Amount is required. Type 0 if paid"
            amount.requestFocus()
            addItemButton.isEnabled = true
            addItemButton.text = "ADD ITEM"
            return
        }

        val buyerFullName = buyerName.text.toString()
        val itemAmount = amount.text.toString()
        val sellerUid = auth.currentUser?.uid.toString()
        val sellerShopName = binding.userData?.userShopName
        val sellerFullName = binding.userData?.userFullName
        val sellerRefUid = binding.sellerRefUid.text.toString()
        val push = dbItemRef.push().key!!
        val item = ItemModelAdd(sellerUid,sellerFullName, sellerShopName, sellerRefUid, buyerFullName, itemAmount)
        LoadingDialogFragment().show(childFragmentManager, "LOADING")
        dbItemRef.child(push).setValue(item)
            .addOnCompleteListener {
                childFragmentManager.findFragmentByTag("LOADING")?.let {
                    (it as DialogFragment).dismiss()
                }
                buyerName.text.clear()
                amount.text.clear()
                Toast.makeText(this.context, "Item listed proceed to our Dropping area for dropping", Toast.LENGTH_LONG).show()
                SuccessDialogFragment().show(childFragmentManager, "")
                addItemButton.isEnabled = true
                addItemButton.text = "ADD ITEM"
            }
            .addOnFailureListener {
                    err -> Toast.makeText(this.context, "Data Insertion Error ${err.message}", Toast.LENGTH_SHORT).show()
                childFragmentManager.findFragmentByTag("LOADING")?.let {
                    (it as DialogFragment).dismiss()
                }
                addItemButton.isEnabled = true
                addItemButton.text = "ADD ITEM"
            }


    }
}