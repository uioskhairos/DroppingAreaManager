package com.droppingareamanager.app.admin

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import com.droppingareamanager.app.R
import com.droppingareamanager.app.databinding.ActivityAddcashoutdialogAdminBinding
import com.droppingareamanager.app.models.CashoutModelAdd
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_addcashoutdialog_admin.*
import kotlinx.android.synthetic.main.search_shopname_dialog.view.*
import kotlinx.android.synthetic.main.items_list.*
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*


class AddCashoutsDialogAdminActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var dbUserRef: DatabaseReference
    private lateinit var dbItemRef: DatabaseReference
    private lateinit var dbCashoutRef: DatabaseReference
    private lateinit var binding: ActivityAddcashoutdialogAdminBinding
    private var shopsArrayList= arrayListOf<String>()
    private var filteredShopsArrayList= arrayListOf<String>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddcashoutdialogAdminBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        // Initialize Firebase Auth
        auth = Firebase.auth
        dbItemRef = FirebaseDatabase.getInstance().getReference("Items")
        dbUserRef = FirebaseDatabase.getInstance().getReference("User")
        dbCashoutRef = FirebaseDatabase.getInstance().getReference("Cashouts")

        dbUserRef = FirebaseDatabase.getInstance().getReference("User")

        binding.shopNameAddCashoutDialog.doAfterTextChanged{
            val sellerShopName = binding.shopNameAddCashoutDialog.text.toString()
            dbUserRef.orderByChild("userShopName").equalTo(sellerShopName).addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()){
                        for (i in snapshot.children){
                            binding.sellerUid.text = i.child("userId").value.toString()
                        }
                    }
                    else{
                        if (sellerShopName!=""){
                            binding.shopNameAddCashoutDialog.error = "Seller does not exist"
                            binding.shopNameAddCashoutDialog.requestFocus()
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
        binding.shopNameAddCashoutDialog.setOnClickListener {
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
                            binding.shopNameAddCashoutDialog.text = filteredShopsArrayList[position]
                            mAlertDialog.dismiss()
                        }
                    }
                    else{
                        filteredShopsArrayList.clear()
                        filteredShopsArrayList.addAll(shopsArrayList)
                        (mDialogView.shopsList.adapter as ArrayAdapter<*>).notifyDataSetChanged()
                        mDialogView.shopsList.isClickable = true
                        mDialogView.shopsList.setOnItemClickListener{ parent, view, position, id ->
                            binding.shopNameAddCashoutDialog.text = filteredShopsArrayList[position]
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

        binding.buttonCancelAddCashout.setOnClickListener{
            val intent = Intent(this, AdminCashout::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
        }

        binding.buttonAddCashoutDialog.setOnClickListener{
            binding.buttonAddCashoutDialog.isEnabled = false
            binding.buttonAddCashoutDialog.text = "Adding..."
            val sellerShopName = binding.shopNameAddCashoutDialog.text.toString().uppercase().trim()
            val method = binding.editTextMethodAddCashoutDialog.selectedItem.toString().trim()
            val amount = binding.editTextAmountAddCashoutDialog.text.toString().trim()


            if (sellerShopName.isEmpty()){
                binding.shopNameAddCashoutDialog.error = "Shop Name is required"
                binding.shopNameAddCashoutDialog.requestFocus()
                binding.buttonAddCashoutDialog.isEnabled = true
                binding.buttonAddCashoutDialog.text = "ADD CASHOUT"
                return@setOnClickListener
            }
            if (method.isEmpty()){
                val errorText = binding.editTextMethodAddCashoutDialog.selectedView as TextView
                errorText.error = "anything here, just to add the icon"
                errorText.setTextColor(Color.RED) //just to highlight that this is an error
                errorText.text = "Payment Method is required"
                binding.buttonAddCashoutDialog.isEnabled = true
                binding.buttonAddCashoutDialog.text = "ADD CASHOUT"
                return@setOnClickListener
            }
            if (amount.isEmpty()){
                binding.editTextAmountAddCashoutDialog.error = "Amount is required"
                binding.editTextAmountAddCashoutDialog.requestFocus()
                binding.buttonAddCashoutDialog.isEnabled = true
                binding.buttonAddCashoutDialog.text = "ADD CASHOUT"
                return@setOnClickListener
            }


            addCashout()
        }
    }

    private fun addCashout() {
        //time
        val current = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")
        val time = current.format(formatter).toString().trim()
        val sellerShopName = binding.shopNameAddCashoutDialog.text.toString().uppercase().trim()
        val method = binding.editTextMethodAddCashoutDialog.selectedItem.toString().trim()
        val amount = binding.editTextAmountAddCashoutDialog.text.toString().trim()
        val desc = binding.editTextDescAddCashoutDialog.text.toString()
        val status = "pending".trim()
        val push = dbCashoutRef.push().key!!
        val uid = binding.sellerUid.text?.toString()
        val cashout = CashoutModelAdd( uid, sellerShopName,method, amount, status, desc, time)
        dbCashoutRef.child(push).setValue(cashout)
            .addOnCompleteListener {
                binding.editTextAmountAddCashoutDialog.text?.clear()
                binding.editTextDescAddCashoutDialog.text?.clear()
                Toast.makeText(this@AddCashoutsDialogAdminActivity, "Cashout listed successfully", Toast.LENGTH_SHORT).show()
//                                    SuccessDialogFragment().show(childFragmentManager, "")
                binding.buttonAddCashoutDialog.isEnabled = true
                binding.buttonAddCashoutDialog.text = "ADD CASHOUT"
            }
            .addOnFailureListener {
                    err -> Toast.makeText(this@AddCashoutsDialogAdminActivity, "Data Insertion Error ${err.message}", Toast.LENGTH_SHORT).show()
                binding.buttonAddCashoutDialog.isEnabled = true
                binding.buttonAddCashoutDialog.text = "ADD CASHOUT"
            }
    }

}
