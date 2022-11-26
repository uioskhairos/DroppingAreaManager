package com.droppingareamanager.app.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.droppingareamanager.app.R
import com.droppingareamanager.app.models.CashoutModel

class CashoutModelAdapter(private val cashoutList: ArrayList<CashoutModel>): RecyclerView.Adapter <CashoutModelAdapter.MyViewHolder>(){

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(
            R.layout.cashouts_list,
        parent, false)
        return MyViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val currentUser = cashoutList[position]

        holder.amount.text = currentUser.cashoutAmount as CharSequence?
        holder.metohod.text = currentUser.method
        holder.status.text = currentUser.status
    }

    override fun getItemCount(): Int {
        return cashoutList.size
    }

    inner class  MyViewHolder (itemView : View): RecyclerView.ViewHolder(itemView){
        val amount : TextView = itemView.findViewById(R.id.amountCashout)
        val metohod : TextView = itemView.findViewById(R.id.method)
        val status : TextView = itemView.findViewById(R.id.statusCashout)
    }
}