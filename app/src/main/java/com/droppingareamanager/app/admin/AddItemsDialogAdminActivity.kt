package com.droppingareamanager.app.admin

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.ArrayAdapter
import android.widget.SearchView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import com.droppingareamanager.app.databinding.FragmentAddItemsDialogBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.items_list.*
import kotlinx.android.synthetic.main.search_shopname_dialog.view.*
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import com.droppingareamanager.app.R
import com.droppingareamanager.app.models.ItemAdminModelAdd
import com.droppingareamanager.app.models.ItemModelAdd


class AddItemsDialogAdminActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var dbUserRef: DatabaseReference
    private lateinit var dbItemRef: DatabaseReference
    private lateinit var binding: FragmentAddItemsDialogBinding
    private var shopsArrayList= arrayListOf<String>()
    private var filteredShopsArrayList= arrayListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = FragmentAddItemsDialogBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        // Initialize Firebase Auth
        auth = Firebase.auth
        dbItemRef = FirebaseDatabase.getInstance().getReference("Items")
        dbUserRef = FirebaseDatabase.getInstance().getReference("User")

        binding.editTextShopNameAddItemDialog.doAfterTextChanged{
            val sellerShopName = binding.editTextShopNameAddItemDialog.text.toString().uppercase().trim()
            dbUserRef.orderByChild("userShopName").equalTo(sellerShopName).addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()){
                        for (i in snapshot.children){
                            binding.sellerUid.text = i.child("userId").value.toString()
                            binding.sellerFullName.text = i.child("userFullName").value.toString()
                            binding.sellerRefUid.text = i.child("userReferrerId").value.toString()
                        }
                    }
                    else{
                        if (sellerShopName!=""){
                            binding.editTextShopNameAddItemDialog.error = "Seller does not exist"
                            binding.editTextShopNameAddItemDialog.requestFocus()
                            return
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            })
        }

        shopsArrayList= arrayListOf()
        filteredShopsArrayList= arrayListOf()
        binding.editTextShopNameAddItemDialog.setOnClickListener {
            val mDialogView = LayoutInflater.from(this).inflate(R.layout.search_shopname_dialog,null)
            val mBuilder = AlertDialog.Builder(this)
                .setView(mDialogView)
            val mAlertDialog = mBuilder.show()
            dbUserRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    filteredShopsArrayList.clear()
                    shopsArrayList.clear()
                    for (i in snapshot.children){
                        val shops = i.child("userShopName").value
                        shopsArrayList.add(shops!! as String)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            })
            val shopsAdapter : ArrayAdapter<String> = ArrayAdapter(
                this,android.R.layout.simple_list_item_1,filteredShopsArrayList
            )
            mDialogView.shopsList.adapter = shopsAdapter
            mDialogView.searchShopName.clearFocus()
            mDialogView.searchShopName.setOnQueryTextListener(object : SearchView.OnQueryTextListener{
                override fun onQueryTextSubmit(query: String?): Boolean {
                    return true
                }
                override fun onQueryTextChange(newText: String?): Boolean {
                    filteredShopsArrayList.clear()
                    val searchText = newText!!.lowercase(Locale.getDefault())
                    if (searchText.isNotEmpty()){
                        shopsArrayList.forEach{
                            if(it.lowercase(Locale.getDefault()).contains(searchText)){
                                filteredShopsArrayList.add(it)
                            }
                        }
                        (mDialogView.shopsList.adapter as ArrayAdapter<*>).notifyDataSetChanged()
                        mDialogView.shopsList.isClickable = true
                        mDialogView.shopsList.setOnItemClickListener{ parent, view, position, id ->
                            binding.editTextShopNameAddItemDialog.text = filteredShopsArrayList[position]
                            mAlertDialog.dismiss()
                        }
                    }
                    else{
                        filteredShopsArrayList.clear()
                        filteredShopsArrayList.addAll(shopsArrayList)
                        (mDialogView.shopsList.adapter as ArrayAdapter<*>).notifyDataSetChanged()
                        mDialogView.shopsList.isClickable = true
                        mDialogView.shopsList.setOnItemClickListener{ parent, view, position, id ->
                            binding.editTextShopNameAddItemDialog.text = filteredShopsArrayList[position]
                            mAlertDialog.dismiss()
                        }
                    }
                    return false
                }

            })
            //update button
//            mDialogView.cancelBtnDialog.setOnClickListener{
//                mAlertDialog.dismiss()
//            }
        }

        binding.buttonCancelAddItem.setOnClickListener{
            val intent = Intent(this, ApproveItemsAdminActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
        }

        binding.buttonAddItemDialog.setOnClickListener{
            binding.buttonAddItemDialog.isEnabled = false
            binding.buttonAddItemDialog.text = "Adding..."
            val sellerShopName = binding.editTextShopNameAddItemDialog.text.toString().uppercase().trim()
            val buyerFullName = binding.editTextBuyerNameAddItemDialog.text.toString().trim()
            val itemAmount = binding.editTextAmountAddItemDialog.text.toString().trim()
            val itemHandlingFee = binding.editTextHfAddItemDialog.text.toString().trim()
            val status = "dropped".trim()

            // logged in user is the DA
            auth = Firebase.auth
            val droppingArea = auth.currentUser?.uid.toString().trim()

            //time
            val current = LocalDateTime.now()
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
            val time = current.format(formatter).toString().trim()

            if (sellerShopName.isEmpty()){
                binding.editTextShopNameAddItemDialog.error = "Shop Name is required"
                binding.editTextShopNameAddItemDialog.requestFocus()
                binding.buttonAddItemDialog.isEnabled = true
                binding.buttonAddItemDialog.text = "ADD ITEM"
                return@setOnClickListener
            }
            if (buyerFullName.isEmpty()){
                binding.editTextBuyerNameAddItemDialog.error = "Buyer Name is required"
                binding.editTextBuyerNameAddItemDialog.requestFocus()
                binding.buttonAddItemDialog.isEnabled = true
                binding.buttonAddItemDialog.text = "ADD ITEM"
                return@setOnClickListener
            }
            if (itemAmount.isEmpty()){
                binding.editTextAmountAddItemDialog.error = "Amount is required"
                binding.editTextAmountAddItemDialog.requestFocus()
                binding.buttonAddItemDialog.isEnabled = true
                binding.buttonAddItemDialog.text = "ADD ITEM"
                return@setOnClickListener
            }
            if (itemHandlingFee.isEmpty()){
                binding.editTextShopNameAddItemDialog.error = "Handling Fee is required"
                binding.editTextShopNameAddItemDialog.requestFocus()
                binding.buttonAddItemDialog.isEnabled = true
                binding.buttonAddItemDialog.text = "ADD ITEM"
                return@setOnClickListener
            }
            val push = dbItemRef.push().key!!
            val item = ItemAdminModelAdd(binding.sellerUid.text.toString(),binding.sellerFullName.text.toString(), sellerShopName,binding.sellerRefUid.text.toString(), buyerFullName, itemAmount,itemHandlingFee, status,"", time)
            dbItemRef.child(push).setValue(item)
                .addOnCompleteListener {
                    binding.editTextBuyerNameAddItemDialog.text?.clear()
                    binding.editTextAmountAddItemDialog.text?.clear()
                    binding.editTextHfAddItemDialog.text?.clear()
                    Toast.makeText(this@AddItemsDialogAdminActivity, "Item listed proceed to our Dropping area for dropping", Toast.LENGTH_LONG).show()
                    binding.buttonAddItemDialog.isEnabled = true
                    binding.buttonAddItemDialog.text = "ADD ITEM"
                }
                .addOnFailureListener {
                        err -> Toast.makeText(this@AddItemsDialogAdminActivity, "Data Insertion Error ${err.message}", Toast.LENGTH_SHORT).show()
                    binding.buttonAddItemDialog.isEnabled = true
                    binding.buttonAddItemDialog.text = "ADD ITEM"
                }

        }


    }

}
