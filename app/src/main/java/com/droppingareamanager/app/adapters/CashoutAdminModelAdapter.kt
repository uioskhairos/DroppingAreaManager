package com.droppingareamanager.app.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintSet.GONE
import androidx.constraintlayout.widget.ConstraintSet.VISIBLE
import androidx.recyclerview.widget.RecyclerView
import com.droppingareamanager.app.R
import com.droppingareamanager.app.admin.UpdateCashoutAdminActivity
import com.droppingareamanager.app.models.AdminCashoutModel
import com.google.firebase.database.*


class CashoutAdminModelAdapter(private val cashoutList: ArrayList<AdminCashoutModel>, val context: Context): RecyclerView.Adapter <CashoutAdminModelAdapter.MyViewHolder>(){
    private lateinit var dbCashout: DatabaseReference

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(
            com.droppingareamanager.app.R.layout.cashouts_list_admin,
            parent, false)
        return MyViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val currentUser = cashoutList[position]
        dbCashout = FirebaseDatabase.getInstance().getReference("Cashouts")
        val id = currentUser.id.toString()
        holder.shopName.text = currentUser.shopName
        holder.method.text = currentUser.method
        holder.cashoutAmount.text = currentUser.cashoutAmount as CharSequence?
        holder.status.text = currentUser.status
        holder.desc.text = currentUser.desc
        holder.time.text = currentUser.time as CharSequence?

        if (currentUser.status == "pending"){
            holder.approve.visibility = VISIBLE
            holder.decline.visibility = VISIBLE
        }
        if (currentUser.status == "completed"){
            holder.approve.visibility = GONE
            holder.decline.visibility = GONE
        }
        if (currentUser.status == "declined"){
            holder.approve.visibility = GONE
            holder.decline.visibility = GONE
        }

        holder.approve.setOnClickListener{
            val intent = Intent(context, UpdateCashoutAdminActivity::class.java)
            intent.putExtra("itemId", id)
            intent.putExtra("shopName", currentUser.shopName)
            intent.putExtra("amount", currentUser.cashoutAmount as CharSequence?)
            intent.putExtra("action", "approve")
            context.startActivity(intent)
        }
        holder.decline.setOnClickListener {
            val intent = Intent(context, UpdateCashoutAdminActivity::class.java)
            intent.putExtra("itemId", id)
            intent.putExtra("shopName", currentUser.shopName)
            intent.putExtra("amount", currentUser.cashoutAmount as CharSequence?)
            intent.putExtra("action", "decline")
            context.startActivity(intent)
        }
        holder.delete.setOnClickListener{
            val intent = Intent(context, UpdateCashoutAdminActivity::class.java)
            intent.putExtra("itemId", id)
            intent.putExtra("shopName", currentUser.shopName)
            intent.putExtra("amount", currentUser.cashoutAmount as CharSequence?)
            intent.putExtra("action", "delete")
            context.startActivity(intent)
        }
    }


    override fun getItemCount(): Int {
        return cashoutList.size
    }

    inner class  MyViewHolder (itemView : View): RecyclerView.ViewHolder(itemView){
        val shopName : TextView = itemView.findViewById(R.id.shopNameAdminCashout)
        val method : TextView = itemView.findViewById(R.id.method_cashout)
        val cashoutAmount : TextView = itemView.findViewById(R.id.cashoutAmountAdmin)
        val status : TextView = itemView.findViewById(R.id.cashoutStatusAdmin)
        val desc : TextView = itemView.findViewById(R.id.cashoutDescAdmin)
        val time : TextView = itemView.findViewById(R.id.cashoutTimeAdmin)
        val approve : TextView = itemView.findViewById(R.id.approve_btn_cashout)
        val decline : TextView = itemView.findViewById(R.id.decline_btn_cashout)
        val delete : ImageView = itemView.findViewById(R.id.deleteCashout)
    }
}