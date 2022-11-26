package com.droppingareamanager.app.user

import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.droppingareamanager.app.Login
import com.droppingareamanager.app.MainActivity
import com.droppingareamanager.app.databinding.ActivitySettingsBinding
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import com.droppingareamanager.app.R
import kotlinx.android.synthetic.main.user_settings_dialog.view.*

class Settings : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var binding: ActivitySettingsBinding
    private lateinit var dbItemRef: DatabaseReference
    private lateinit var dbUserRef: DatabaseReference

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        // Initialize Firebase Auth
        auth = Firebase.auth
        dbItemRef = FirebaseDatabase.getInstance().getReference("Items")
        dbUserRef = FirebaseDatabase.getInstance().getReference("User")

        val uId = auth.currentUser?.uid.toString()

        val balance =intent.getStringExtra("balance")
        val sumSales = intent.getStringExtra("sumSales")
        val sumRebates = intent.getStringExtra("sumRebates")
        val sumCashouts = intent.getStringExtra("sumCashouts")
        val sumRefCom = intent.getStringExtra("sumRefCom")
        binding.fullNameSettings.text = intent.getStringExtra("fullName")
        binding.shopNameSettings.text = intent.getStringExtra("shopName")
        binding.emailSettings.text = intent.getStringExtra("email")
        binding.sumSales.text = sumSales
        binding.sumRebates.text = sumRebates
        binding.sumCashouts.text = sumCashouts
        binding.sumRefCom.text = sumRefCom
        binding.balanceSettings.text = balance

        binding.changePass.setOnClickListener Foo@{
            val mDialogView = LayoutInflater.from(this).inflate(R.layout.user_settings_dialog,null)
            val mBuilder = AlertDialog.Builder(this)
                .setView(mDialogView)
                .setTitle("Update Password")
            val mAlertDialog = mBuilder.show()
            //update button
            mDialogView.updateBtn.setOnClickListener{
                val user = auth.currentUser
                val email = user?.email
                val oldPassword = mDialogView.dialogOldPassword.text.toString()
                val newPassword = mDialogView.dialogNewPassword.text.toString()
                val cNewPassword = mDialogView.dialogCNewPassword.text.toString()
                if (oldPassword.isEmpty()){
                    mDialogView.dialogOldPassword.error = "Password is required"
                    mDialogView.dialogOldPassword.requestFocus()
                    return@setOnClickListener
                }
                if (newPassword.isEmpty()){
                    mDialogView.dialogNewPassword.error = "New Password is required"
                    mDialogView.dialogNewPassword.requestFocus()
                    return@setOnClickListener
                }
                if (cNewPassword.isEmpty()){
                    mDialogView.dialogCNewPassword.error = "Please re-enter new password"
                    mDialogView.dialogCNewPassword.requestFocus()
                    return@setOnClickListener
                }
                    if(newPassword==cNewPassword){
                        // Get auth credentials from the user for re-authentication. The example below shows
                    // email and password credentials but there are multiple possible providers,
                    // such as GoogleAuthProvider or FacebookAuthProvider.
                        val credential =
                            email?.let { it1 -> EmailAuthProvider.getCredential(it1, oldPassword) };

                    // Prompt the user to re-provide their sign-in credentials
                        if (credential != null) {
                            user.reauthenticate(credential).addOnCompleteListener { it1 ->
                                if (it1.isSuccessful) {
                                    user.updatePassword(newPassword).addOnCompleteListener{
                                        if (it.isSuccessful) {
                                            mAlertDialog.dismiss()
                                            Log.d(TAG, "Password updated")
                                            Toast.makeText(this, "Password updated successfully", Toast.LENGTH_SHORT)
                                                .show()
                                        } else {
                                            mAlertDialog.dismiss()
                                            Log.d(TAG, "Error password not updated")
                                            Toast.makeText(baseContext, "Error password not updated.",
                                                Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                } else {
                                    mAlertDialog.dismiss()
                                    Log.d(TAG, "Error auth failed")
                                    Toast.makeText(baseContext, "Incorrect Password",
                                        Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    }else
                    {
                        mDialogView.dialogNewPassword.error = "Password does not match"
                        mDialogView.dialogNewPassword.requestFocus()
                        return@setOnClickListener
                    }

            }
            mDialogView.cancelBtnDialog.setOnClickListener{
                mAlertDialog.dismiss()
            }
        }

        binding.backBtn.setOnClickListener{
            val intent = Intent(this, MainActivity::class.java)
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

    }
}