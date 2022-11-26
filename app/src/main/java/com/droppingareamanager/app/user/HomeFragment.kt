package com.droppingareamanager.app.user

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.Toast
import androidx.navigation.Navigation
import com.droppingareamanager.app.R
import com.droppingareamanager.app.admin.AdminDashboard
import com.droppingareamanager.app.databinding.FragmentHomeBinding
import com.droppingareamanager.app.models.RegisterModel
import com.google.android.gms.ads.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import java.lang.Integer.sum


class HomeFragment : Fragment(R.layout.fragment_home) {
    private lateinit var auth: FirebaseAuth
    private lateinit var dbUser: DatabaseReference
    private lateinit var dbItem: DatabaseReference
    private lateinit var dbCashout: DatabaseReference
    private lateinit var binding: FragmentHomeBinding
    private lateinit var mAdView : AdView

    override fun onStart() {
        super.onStart()
        auth = Firebase.auth
        val userId = auth.currentUser?.uid.toString()

        var accountType: String?
        dbUser = FirebaseDatabase.getInstance().getReference("User")
        dbUser.child(userId).get().addOnSuccessListener {
            accountType = it.child("accountType").value.toString()
            if (accountType == "Dropping Area"){
                startActivity(Intent(this.context, AdminDashboard::class.java))
            }
        }.addOnFailureListener{
            Log.e("firebase", "Error getting data", it)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding = FragmentHomeBinding.bind(view)

        getUserData()
        loadBannerAd()

        binding.dropItemDashboard.setOnClickListener {
            val nextAction = HomeFragmentDirections.actionHomeFragmentToItemsFragment()
            Navigation.findNavController(it).navigate(nextAction)
        }
        binding.searchDropper.setOnClickListener {
            val nextAction = HomeFragmentDirections.actionHomeFragmentToDroppersFragment()
            Navigation.findNavController(it).navigate(nextAction)
        }
        binding.cashoutDashboard.setOnClickListener {
            val nextAction = HomeFragmentDirections.actionHomeFragmentToCashoutReqFragment()
            Navigation.findNavController(it).navigate(nextAction)
        }
//        binding.subscriptionDashboard.setOnClickListener {
//            val nextAction = HomeFragmentDirections.actionHomeFragmentToSubscriptionFragment()
//            Navigation.findNavController(it).navigate(nextAction)
//        }
        binding.searchItemDashboard.setOnClickListener {
            val nextAction = HomeFragmentDirections.actionHomeFragmentToDroppedItemsFragment()
            Navigation.findNavController(it).navigate(nextAction)
        }
//        binding.referralsDashboard.setOnClickListener{
//            val nextAction = HomeFragmentDirections.actionHomeFragmentToReferralFragment()
//            Navigation.findNavController(it).navigate(nextAction)
//        }
    }

    private fun loadBannerAd() {
        this.context?.let { MobileAds.initialize(it) {} }

        mAdView = binding.adView
        val adRequest = AdRequest.Builder().build()
        mAdView.loadAd(adRequest)

        mAdView.adListener = object: AdListener() {
            override fun onAdClicked() {
                // Code to be executed when the user clicks on an ad.
            }

            override fun onAdClosed() {
                // Code to be executed when the user is about to return
                // to the app after tapping on an ad.
            }

            override fun onAdFailedToLoad(adError : LoadAdError) {
                // Code to be executed when an ad request fails.
            }

            override fun onAdImpression() {
                // Code to be executed when an impression is recorded
                // for an ad.
            }

            override fun onAdLoaded() {
                // Code to be executed when an ad finishes loading.
            }

            override fun onAdOpened() {
                // Code to be executed when an ad opens an overlay that
                // covers the screen.
            }
        }
    }

    private fun getUserData() {
        auth = Firebase.auth
        //get user account type
        val userId = auth.currentUser?.uid.toString()
        dbUser = FirebaseDatabase.getInstance().getReference("User")
        dbItem = FirebaseDatabase.getInstance().getReference("Items")
        dbCashout = FirebaseDatabase.getInstance().getReference("Cashouts")
        dbUser.child(userId).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()){
                    val user = snapshot.getValue(RegisterModel::class.java)
                    binding.userData = user
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
        //get total items pick up
        dbItem.orderByChild("sellerUid").equalTo(userId).addValueEventListener(object :
            ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()){
                    var sumPickedUp = 0f
                    var sumAmount=0f
                    for (i in snapshot.children){
                        val status = i.child("status").value.toString()
                        val hf = i.child("itemHandlingFee").value.toString()
                        val amount = i.child("itemAmount").value.toString()
                        val dateS = i.child("dateClaimed").value.toString()
                        if(status == "claimed" && i.child("dateClaimed").value!=null){
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
                            for (item in snapshot.children){
                                val status = item.child("status").value.toString()
                                val amount = item.child("cashoutAmount").value.toString()
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
                                        for (items in snapshot.children){
                                            val status = items.child("status").value.toString()
                                            val hf = items.child("itemHandlingFee").value.toString()
                                            if (status == "claimed"){
                                                sumRefCom += hf.toInt()
                                            }
                                        }
                                        val totalRefCom = sumRefCom*0.15
                                        val totalBalance = sum(total, totalRefCom.toInt())
                                        //add comma
                                        val finalOutputBalance = "%,d".format(totalBalance)
                                        binding.balance.text = "₱$finalOutputBalance.00"
                                        dbUser.child(userId).child("balance").setValue(totalBalance.toString())
                                        val resultSumRebates = "%,d".format(sumPickedUp.toInt())
                                        val totalSumAmount = "%,d".format(sumAmount.toInt())
                                        binding.manageDashboard.setOnClickListener {
                                            val intent = Intent(this@HomeFragment.context, Settings::class.java)
                                            intent.putExtra("balance", binding.balance.text.toString())
                                            intent.putExtra("sumSales", "₱$totalSumAmount.00")
                                            intent.putExtra("sumRebates", "₱$resultSumRebates.00")
                                            intent.putExtra("sumCashouts", "₱$sumCashout.00")
                                            intent.putExtra("sumRefCom", "₱$totalRefCom.00")
                                            intent.putExtra("fullName", "${binding.userData?.userFullName}")
                                            intent.putExtra("shopName", "${binding.userData?.userShopName}")
                                            intent.putExtra("email", "${binding.userData?.userEmail}")
                                            startActivity(intent)
                                        }
                                        binding.arrowDashboard.setOnClickListener {
                                            // move to manage items
                                            val intent = Intent(this@HomeFragment.context, Settings::class.java)
                                            intent.putExtra("balance", binding.balance.text.toString())
                                            intent.putExtra("sumSales", "₱$totalSumAmount.00")
                                            intent.putExtra("sumRebates", "₱$resultSumRebates.00")
                                            intent.putExtra("sumCashouts", "₱$sumCashout.00")
                                            intent.putExtra("sumRefCom", "₱$totalRefCom.00")
                                            intent.putExtra("fullName", "${binding.userData?.userFullName}")
                                            intent.putExtra("shopName", "${binding.userData?.userShopName}")
                                            intent.putExtra("email", "${binding.userData?.userEmail}")
                                            startActivity(intent)
                                        }
                                        binding.settingsDashboard.setOnClickListener {
                                            val intent = Intent(this@HomeFragment.context, Settings::class.java)
                                            intent.putExtra("balance", binding.balance.text.toString())
                                            intent.putExtra("sumSales", "₱$totalSumAmount.00")
                                            intent.putExtra("sumRebates", "₱$resultSumRebates.00")
                                            intent.putExtra("sumCashouts", "₱$sumCashout.00")
                                            intent.putExtra("sumRefCom", "₱$totalRefCom.00")
                                            intent.putExtra("fullName", "${binding.userData?.userFullName}")
                                            intent.putExtra("shopName", "${binding.userData?.userShopName}")
                                            intent.putExtra("email", "${binding.userData?.userEmail}")
                                            startActivity(intent)
                                        }
                                    }
                                    else{
                                        val totalRefCom = 0
                                        val totalBalance = sum(total, totalRefCom)
                                        val finalOutputBalance = "%,d".format(totalBalance)
                                        binding.balance.text = "₱$finalOutputBalance.00"
                                        dbUser.child(userId).child("balance").setValue(totalBalance.toString())
                                        val resultSumRebates = "%,d".format(sumPickedUp.toInt())
                                        val totalSumAmount = "%,d".format(sumAmount.toInt())

                                        binding.manageDashboard.setOnClickListener {
                                            val intent = Intent(this@HomeFragment.context, Settings::class.java)
                                            intent.putExtra("balance", binding.balance.text)
                                            intent.putExtra("sumSales", "₱$totalSumAmount.00")
                                            intent.putExtra("sumRebates", "₱$resultSumRebates.00")
                                            intent.putExtra("sumCashouts", "₱$sumCashout.00")
                                            intent.putExtra("sumRefCom", "₱$totalRefCom.00")
                                            intent.putExtra("fullName", "${binding.userData?.userFullName}")
                                            intent.putExtra("shopName", "${binding.userData?.userShopName}")
                                            intent.putExtra("email", "${binding.userData?.userEmail}")
                                            startActivity(intent)
                                        }
                                        binding.arrowDashboard.setOnClickListener {
                                            // move to manage items
                                            val intent = Intent(this@HomeFragment.context, Settings::class.java)
                                            intent.putExtra("balance", binding.balance.text)
                                            intent.putExtra("sumSales", "₱$totalSumAmount.00")
                                            intent.putExtra("sumRebates", "₱$resultSumRebates.00")
                                            intent.putExtra("sumCashouts", "₱$sumCashout.00")
                                            intent.putExtra("sumRefCom", "₱$totalRefCom.00")
                                            intent.putExtra("fullName", "${binding.userData?.userFullName}")
                                            intent.putExtra("shopName", "${binding.userData?.userShopName}")
                                            intent.putExtra("email", "${binding.userData?.userEmail}")
                                            startActivity(intent)
                                        }
                                        binding.settingsDashboard.setOnClickListener {
                                            val intent = Intent(this@HomeFragment.context, Settings::class.java)
                                            intent.putExtra("balance", binding.balance.text)
                                            intent.putExtra("sumSales", "₱$totalSumAmount.00")
                                            intent.putExtra("sumRebates", "₱$resultSumRebates.00")
                                            intent.putExtra("sumCashouts", "₱$sumCashout.00")
                                            intent.putExtra("sumRefCom", "₱$totalRefCom.00")
                                            intent.putExtra("fullName", "${binding.userData?.userFullName}")
                                            intent.putExtra("shopName", "${binding.userData?.userShopName}")
                                            intent.putExtra("email", "${binding.userData?.userEmail}")
                                            startActivity(intent)
                                        }
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
                }else
                {
                    binding.balance.text = "₱0.00"
                    binding.manageDashboard.setOnClickListener {
                        val intent = Intent(this@HomeFragment.context, Settings::class.java)
                        intent.putExtra("balance", binding.balance.text.toString())
                        intent.putExtra("sumSales", "₱0.00")
                        intent.putExtra("sumRebates", "₱0.00")
                        intent.putExtra("sumCashouts", "₱0.00")
                        intent.putExtra("sumRefCom", "₱0.00")
                        intent.putExtra("fullName", "${binding.userData?.userFullName}")
                        intent.putExtra("shopName", "${binding.userData?.userShopName}")
                        intent.putExtra("email", "${binding.userData?.userEmail}")
                        startActivity(intent)
                    }
                    binding.arrowDashboard.setOnClickListener {
                        // move to manage items
                        val intent = Intent(this@HomeFragment.context, Settings::class.java)
                        intent.putExtra("balance", binding.balance.text.toString())
                        intent.putExtra("sumSales", "₱0.00")
                        intent.putExtra("sumRebates", "₱0.00")
                        intent.putExtra("sumCashouts", "₱0.00")
                        intent.putExtra("sumRefCom", "₱0.00")
                        intent.putExtra("fullName", "${binding.userData?.userFullName}")
                        intent.putExtra("shopName", "${binding.userData?.userShopName}")
                        intent.putExtra("email", "${binding.userData?.userEmail}")
                        startActivity(intent)
                    }
                    binding.settingsDashboard.setOnClickListener {
                        val intent = Intent(this@HomeFragment.context, Settings::class.java)
                        intent.putExtra("balance", binding.balance.text.toString())
                        intent.putExtra("sumSales", "₱0.00")
                        intent.putExtra("sumRebates", "₱0.00")
                        intent.putExtra("sumCashouts", "₱0.00")
                        intent.putExtra("sumRefCom", "₱0.00")
                        intent.putExtra("fullName", "${binding.userData?.userFullName}")
                        intent.putExtra("shopName", "${binding.userData?.userShopName}")
                        intent.putExtra("email", "${binding.userData?.userEmail}")
                        startActivity(intent)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }
}