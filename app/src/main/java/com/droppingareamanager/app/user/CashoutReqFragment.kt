package com.droppingareamanager.app.user

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.constraintlayout.widget.ConstraintSet.GONE
import androidx.constraintlayout.widget.ConstraintSet.VISIBLE
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import com.droppingareamanager.app.R
import com.droppingareamanager.app.databinding.FragmentCashoutReqBinding
import com.droppingareamanager.app.models.CashoutModelAdd
import com.droppingareamanager.app.models.CashoutUserModel
import com.droppingareamanager.app.models.NotificationModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class CashoutReqFragment : Fragment(R.layout.fragment_cashout_req) {

    private lateinit var auth: FirebaseAuth
    private lateinit var time: Any
    private lateinit var time2: Any
    private lateinit var addCashoutButton: Button

    private lateinit var dbCashoutRef: DatabaseReference
    private lateinit var dbUserRef: DatabaseReference
    private lateinit var dbItems: DatabaseReference
    private lateinit var cashoutType: Spinner
    private lateinit var binding: FragmentCashoutReqBinding

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding =FragmentCashoutReqBinding.bind(view)
        // Initialize Firebase Auth
        auth = Firebase.auth
        dbCashoutRef = FirebaseDatabase.getInstance().getReference("Cashouts")
        dbUserRef = FirebaseDatabase.getInstance().getReference("User")
        dbItems = FirebaseDatabase.getInstance().getReference("Items")

        cashoutType = requireView().findViewById(R.id.spinner_cashout)
        addCashoutButton =requireView().findViewById(R.id.button_addCashout)

        val uId = auth.currentUser?.uid.toString()

        //time
        val versionAPI = Build.VERSION.SDK_INT
//        val versionRelease = Build.VERSION.RELEASE
        time = 0
        time2 = 0
        if (versionAPI >= 26){
            val current = LocalDateTime.now()
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")
            val formatter2 = DateTimeFormatter.ofPattern("yyyy-MM-dd")
            time = current.format(formatter)
            time2 = current.format(formatter2)
        }
        cashoutType.onItemSelectedListener = object :
            AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                if (cashoutType.selectedItem == "Cash") {
                    binding.gcashInput.visibility = View.GONE
                }
                if (cashoutType.selectedItem == "Select Payment Method") {
                    binding.gcashInput.visibility = View.GONE
                }
                if (cashoutType.selectedItem == "Gcash") {
                    binding.gcashInput.visibility = View.VISIBLE
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                TODO("Not yet implemented")
            }
        }


        getUserData()

        addCashoutButton.setOnClickListener{
            if(binding.userDataCashout?.balance?.toInt()!=0){
                requestCashout()
            }
            else{
                Toast.makeText(this@CashoutReqFragment.requireContext(), "You do not have enough balance", Toast.LENGTH_SHORT)
                    .show()
                return@setOnClickListener
            }
        }
        //update items to cashout
        dbItems.orderByChild("sellerUid").equalTo(uId).addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()){
                    for (i in snapshot.children){
                        if (i.child("status").value=="claimed" && i.child("cashOutStatus").value!="Cashed Out"){
                            dbItems.child(i.key.toString()).child("cashOutStatus").setValue("Cashed Out")
                            dbItems.child(i.key.toString()).child("cashOutDate").setValue(time2.toString())
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })

    }

    private fun getUserData() {
        val uId = auth.currentUser?.uid.toString()
        dbUserRef.child(uId).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()){
                    val userCashout = snapshot.getValue(CashoutUserModel::class.java)
                    binding.userDataCashout = userCashout
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }

    private fun requestCashout() {
        val uId = auth.currentUser?.uid.toString()
        val shopName = binding.userDataCashout?.userShopName
        val itemAmount = binding.userDataCashout?.balance
        binding.editTextAmountCashout.text = "₱$itemAmount.00"
        if (cashoutType.selectedItem.toString()=="Select Payment Method") {
            val errorText = cashoutType.selectedView as TextView
            errorText.error = "anything here, just to add the icon"
            errorText.setTextColor(Color.RED) //just to highlight that this is an error
            errorText.text = "Cashout Method is required"
            return
        }
        if (cashoutType.selectedItem.toString() == "Gcash"){
            if (binding.gcashNum.text.toString() == ""){
                binding.gcashNum.error = "GCASH NUMBER is required."
                binding.gcashNum.requestFocus()
                return
            }
            if (binding.gcashNum.text.toString().length != 11){
                binding.gcashNum.error = "GCASH NUMBER must be 11 digits."
                binding.gcashNum.requestFocus()
                return
            }
        }
            val method = cashoutType.selectedItem.toString()
            val status = "pending"
            val time = time.toString()

            val push = dbCashoutRef.push().key!!
            val item = CashoutModelAdd(uId, shopName, method, itemAmount, status,binding.gcashNum.text.toString(), time)
            LoadingDialogFragment().show(childFragmentManager, "LOADING")
            dbCashoutRef.child(push).setValue(item)
                .addOnCompleteListener {
                    childFragmentManager.findFragmentByTag("LOADING")?.let {
                        (it as DialogFragment).dismiss()
                    }
//                    LoadingDialogFragment().dismiss()
                    SuccessCashoutDialogFragment().show(childFragmentManager, "")
                    sendFirebaseNotification("Cashout Request", "$shopName requested a cashout.", "6SpqMFJ91hei0pjTklWLKL32f7E2")
                }
                .addOnFailureListener {
                        err -> Toast.makeText(this@CashoutReqFragment.requireContext(), "Data Insertion Error ${err.message}", Toast.LENGTH_SHORT).show()
//                    LoadingDialogFragment().dismiss()
                    childFragmentManager.findFragmentByTag("LOADING")?.let {
                        (it as DialogFragment).dismiss()
                    }
                }
    }

    private fun sendFirebaseNotification(title: String, text: String, receiverId: String) {
        val notification = NotificationModel(text,title,receiverId)

        FirebaseDatabase
            .getInstance()
            .getReference("Notification")
            .push()
            .setValue(notification).addOnCompleteListener {
                if (it.isSuccessful){
                    Toast.makeText(this@CashoutReqFragment.context, "Notification sent successfully", Toast.LENGTH_SHORT)
                        .show()
                }
            }
    }
}