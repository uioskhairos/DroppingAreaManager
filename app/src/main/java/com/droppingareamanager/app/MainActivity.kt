package com.droppingareamanager.app

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import com.droppingareamanager.app.admin.AdminDashboard
import com.droppingareamanager.app.databinding.ActivityMainBinding
import com.droppingareamanager.app.user.DashboardActivity
import com.google.android.gms.ads.*
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging

class MainActivity : AppCompatActivity() {

    lateinit var mAdView : AdView
    private var mInterstitialAd: InterstitialAd? = null
    private lateinit var binding: ActivityMainBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var dbUser: DatabaseReference
    private var  appUpdate: AppUpdateManager? = null
    private val requestcode = 100

    override fun onStart() {
        super.onStart()
        auth = Firebase.auth

        if (FirebaseAuth.getInstance().currentUser !=null){
            retrieveAndStoreToken()
            var accountType: String?
            val userId = auth.currentUser?.uid.toString()
            dbUser = FirebaseDatabase.getInstance().getReference("User")
            dbUser.child(userId).get().addOnSuccessListener {
                accountType = it.child("accountType").value.toString()
                if (accountType == "Dropping Area"){
                    startActivity(Intent(this, AdminDashboard::class.java))
                    Toast.makeText(baseContext, "Logged in as $accountType.",
                        Toast.LENGTH_SHORT).show()
                }
                if (accountType == "Dropper"){
                    startActivity(Intent(this, DashboardActivity::class.java))
                    Toast.makeText(baseContext, "Logged in as $accountType.",
                        Toast.LENGTH_SHORT).show()
                }
            }.addOnFailureListener{
                Log.e("firebase", "Error getting data", it)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        loadInterAd()
        loadBannerAd()

//        val interAdBtn : Button = binding.buttonAd

//        interAdBtn.setOnClickListener {
//            showInterAd()
//        }

        appUpdate = AppUpdateManagerFactory.create(this)
        checkUpdate()

        val login: TextView = findViewById(R.id.button_login1)
        login.setOnClickListener {
            val intent = Intent(this, Login::class.java)
            startActivity(intent)
        }

        val register: TextView = findViewById(R.id.button_register1)

        register.setOnClickListener {
            val intent = Intent(this, Register::class.java)
            startActivity(intent)
        }

    }

    private fun showInterAd() {
        if (mInterstitialAd!=null){
            mInterstitialAd?.fullScreenContentCallback = object : FullScreenContentCallback(){
                override fun onAdClicked() {
                    super.onAdClicked()
                }

                override fun onAdDismissedFullScreenContent() {
                    super.onAdDismissedFullScreenContent()
                    val intent = Intent(this@MainActivity, MainActivity::class.java)
                    startActivity(intent)
                }

                override fun onAdFailedToShowFullScreenContent(p0: AdError) {
                    super.onAdFailedToShowFullScreenContent(p0)
                }

                override fun onAdImpression() {
                    super.onAdImpression()
                }

                override fun onAdShowedFullScreenContent() {
                    super.onAdShowedFullScreenContent()
                }

            }
            mInterstitialAd?.show(this)
        }else{
            val intent = Intent(this@MainActivity, MainActivity::class.java)
            startActivity(intent)
        }
    }

    private fun loadBannerAd() {
        MobileAds.initialize(this) {}

        mAdView = findViewById(R.id.adView)
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

    private fun loadInterAd() {
        var adRequest = AdRequest.Builder().build()

        InterstitialAd.load(this,"ca-app-pub-3940256099942544/1033173712", adRequest, object : InterstitialAdLoadCallback() {
            override fun onAdFailedToLoad(adError: LoadAdError) {
                mInterstitialAd = null
            }

            override fun onAdLoaded(interstitialAd: InterstitialAd) {
                mInterstitialAd = interstitialAd
            }
        })
    }

    private fun checkUpdate(){
        appUpdate?.appUpdateInfo?.addOnSuccessListener {
            if (it.updateAvailability()== UpdateAvailability.UPDATE_AVAILABLE
                && it.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)){
                appUpdate?.startUpdateFlowForResult(it, AppUpdateType.IMMEDIATE,this,requestcode)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        inProgressUpdate()
    }

    private fun inProgressUpdate(){
        appUpdate?.appUpdateInfo?.addOnSuccessListener {
            if (it.updateAvailability()==UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS){
                appUpdate?.startUpdateFlowForResult(it, AppUpdateType.IMMEDIATE,this,requestcode)
            }
        }
    }

    private fun retrieveAndStoreToken(){
        auth = Firebase.auth
        val userId = auth.currentUser!!.uid
        FirebaseMessaging.getInstance().token
            .addOnCompleteListener {
                if (it.isSuccessful){
                    val token = it.result
                    FirebaseDatabase.getInstance()
                        .getReference("Tokens")
                        .child(userId)
                        .setValue(token)
                }
            }
    }
}