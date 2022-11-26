package com.droppingareamanager.app.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.droppingareamanager.app.R
import com.droppingareamanager.app.models.UserModel

class DroppersModelAdapter(private val userList: ArrayList<UserModel>): RecyclerView.Adapter <DroppersModelAdapter.MyViewHolder>(){

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(
            R.layout.droppers_list_admin,
        parent, false)
        return MyViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val currentUser = userList[position]

        holder.userFullName.text = currentUser.userFullName
        holder.userShopName.text = currentUser.userShopName
        holder.userEmail.text = currentUser.userEmail
        if (currentUser.balance==""){
            holder.balance.text = "₱0.00"
        }
        else{
            val finalOutputBalance = "%,d".format(currentUser.balance.toString().toInt())
            holder.balance.text = "₱$finalOutputBalance.00"
        }
    }

    override fun getItemCount(): Int {
        return userList.size
    }

    inner class  MyViewHolder (itemView : View): RecyclerView.ViewHolder(itemView){
        val userFullName : TextView = itemView.findViewById(R.id.fullName_home)
        val userShopName : TextView = itemView.findViewById(R.id.shopName_home)
        val userEmail : TextView = itemView.findViewById(R.id.email)
        val balance : TextView = itemView.findViewById(R.id.balance)
    }
}