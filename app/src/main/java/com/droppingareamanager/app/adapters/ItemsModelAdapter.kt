package com.droppingareamanager.app.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintSet.GONE
import androidx.constraintlayout.widget.ConstraintSet.VISIBLE
import androidx.recyclerview.widget.RecyclerView
import com.droppingareamanager.app.R
import com.droppingareamanager.app.models.ItemModel

class ItemsModelAdapter(private val itemList: ArrayList<ItemModel>): RecyclerView.Adapter <ItemsModelAdapter.MyViewHolder>(){

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(
            R.layout.items_list,
            parent, false)
        return MyViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val currentUser = itemList[position]

        holder.buyerFullName.text = currentUser.buyerFullName
        holder.itemAmount.text = currentUser.itemAmount.toString()
        holder.itemHandlingFee.text = currentUser.itemHandlingFee.toString()
        holder.status.text = currentUser.status
        if (currentUser.status.isNullOrEmpty()){
            holder.status.text = "pending"
        }
        if (currentUser.itemHandlingFee.toString().isEmpty()){
            holder.itemHandlingFee.text = "For Approval"
        }
        if (currentUser.status == "dropped"){
            holder.claimedOn.text = "Dropped on:"
            holder.date.text = currentUser.dateDropped
            holder.cashedOutPic.visibility = GONE
        }
        if (currentUser.status == "claimed"){
            holder.claimedOn.text = "Claimed on:"
            holder.date.text = currentUser.dateClaimed
            if (currentUser.cashOutStatus == "Cashed Out"){
                holder.cashedOutPic.visibility = VISIBLE
            }else{
                holder.cashedOutPic.visibility = GONE
            }
        }
        if (currentUser.status.isNullOrEmpty()){
            holder.claimedOn.visibility = GONE
            holder.date.visibility = GONE
            holder.cashedOutPic.visibility = GONE
        }
        if (currentUser.status == "pulled out"){
            holder.claimedOn.visibility = GONE
            holder.date.visibility = GONE
            holder.cashedOutPic.visibility = GONE
        }
        if (currentUser.status == "declined"){
            holder.claimedOn.visibility = GONE
            holder.date.visibility = GONE
            holder.cashedOutPic.visibility = GONE
        }
    }

    override fun getItemCount(): Int {
        return itemList.size
    }

    inner class  MyViewHolder (itemView : View): RecyclerView.ViewHolder(itemView){
        val buyerFullName : TextView = itemView.findViewById(R.id.buyerFullName)
        val itemAmount : TextView = itemView.findViewById(R.id.amount)
        val itemHandlingFee : TextView = itemView.findViewById(R.id.handlingFee)
        val status : TextView = itemView.findViewById(R.id.status)
        val claimedOn : TextView = itemView.findViewById(R.id.claimedOn)
        val cashedOutPic : ImageView = itemView.findViewById(R.id.claimedPic)
        val date : TextView = itemView.findViewById(R.id.date)
    }
}