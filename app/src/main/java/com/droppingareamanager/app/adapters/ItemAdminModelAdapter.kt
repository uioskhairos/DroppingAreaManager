package com.droppingareamanager.app.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.droppingareamanager.app.R
import com.droppingareamanager.app.admin.UpdateItemAdminActivity
import com.droppingareamanager.app.models.AdminItemModel
import com.google.firebase.database.*


class ItemAdminModelAdapter(private val itemList: ArrayList<AdminItemModel>, val context: Context): RecyclerView.Adapter <ItemAdminModelAdapter.MyViewHolder>(){
    private lateinit var dbItem: DatabaseReference

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.items_list_admin,
            parent, false)
        return MyViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val currentUser = itemList[position]
        dbItem = FirebaseDatabase.getInstance().getReference("Items")
        val id = currentUser.id.toString()
        holder.buyerFullName.text = currentUser.buyerFullName
        holder.sellerId.text = currentUser.sellerUid
        holder.sellerFullName.text = currentUser.sellerFullName
        holder.sellerShopName.text = currentUser.sellerShopName
        holder.itemAmount.text = currentUser.itemAmount as CharSequence?
        holder.itemHandlingFee.text = currentUser.itemHandlingFee as CharSequence?
        holder.status.text = currentUser.status
        if (currentUser.status==""){
            holder.approve.text = "APPROVE"
            holder.decline.text = "DECLINE"
            holder.approve.visibility = VISIBLE
            holder.decline.visibility = VISIBLE
            holder.claimed.visibility = GONE
            holder.pulledOut.visibility = GONE
            holder.claimedOrDropped.visibility = GONE
            holder.claimedOrDroppedDate.visibility = GONE
        }
        if (currentUser.status=="dropped"){
            holder.approve.text = "CLAIM"
            holder.decline.text = "PULL OUT"
            holder.approve.visibility = VISIBLE
            holder.decline.visibility = VISIBLE
            holder.claimed.visibility = GONE
            holder.pulledOut.visibility = GONE
            holder.claimedOrDropped.text = "Dropped On:"
            holder.claimedOrDroppedDate.text = currentUser.dateDropped
        }
        if (currentUser.status=="claimed"){
            holder.approve.visibility = GONE
            holder.decline.visibility = GONE
            holder.claimed.visibility = VISIBLE
            holder.pulledOut.visibility = GONE
            holder.claimedOrDropped.text = "Claimed On:"
            holder.claimedOrDroppedDate.text = currentUser.dateClaimed

        }
        if (currentUser.status=="pulled out"){
            holder.approve.visibility = GONE
            holder.decline.visibility = GONE
            holder.pulledOut.visibility = VISIBLE
            holder.claimed.visibility = GONE
            holder.claimedOrDropped.visibility = GONE
            holder.claimedOrDroppedDate.visibility = GONE

        }
        holder.approve.setOnClickListener{
            val intent = Intent(context, UpdateItemAdminActivity::class.java)
            intent.putExtra("itemId", id)
            intent.putExtra("sellerId", currentUser.sellerUid)
            intent.putExtra("buyerFullName", currentUser.buyerFullName)
            intent.putExtra("sellerShopName", currentUser.sellerShopName)
            intent.putExtra("itemAmount", currentUser.itemAmount as CharSequence?)
            intent.putExtra("action", "approve")
            intent.putExtra("status", currentUser.status)
            context.startActivity(intent)
        }
        holder.decline.setOnClickListener{
            val intent = Intent(context, UpdateItemAdminActivity::class.java)
            intent.putExtra("itemId", id)
            intent.putExtra("sellerId", currentUser.sellerUid)
            intent.putExtra("buyerFullName", currentUser.buyerFullName)
            intent.putExtra("sellerShopName", currentUser.sellerShopName)
            intent.putExtra("itemAmount", currentUser.itemAmount as CharSequence?)
            intent.putExtra("action", "decline")
            intent.putExtra("status", currentUser.status)
            context.startActivity(intent)
        }
        holder.edit.setOnClickListener{
            val intent = Intent(context, UpdateItemAdminActivity::class.java)
            intent.putExtra("itemId", id)
            intent.putExtra("sellerId", currentUser.sellerUid)
            intent.putExtra("buyerFullName", currentUser.buyerFullName)
            intent.putExtra("sellerShopName", currentUser.sellerShopName)
            intent.putExtra("itemAmount", currentUser.itemAmount as CharSequence?)
            intent.putExtra("itemHandlingFee", currentUser.itemHandlingFee as CharSequence?)
            intent.putExtra("action", "edit")
            intent.putExtra("status", currentUser.status)
            context.startActivity(intent)
        }
        holder.delete.setOnClickListener{
            val intent = Intent(context, UpdateItemAdminActivity::class.java)
            intent.putExtra("itemId", id)
            intent.putExtra("sellerId", currentUser.sellerUid)
            intent.putExtra("buyerFullName", currentUser.buyerFullName)
            intent.putExtra("sellerShopName", currentUser.sellerShopName)
            intent.putExtra("itemAmount", currentUser.itemAmount as CharSequence?)
            intent.putExtra("action", "delete")
            intent.putExtra("status", currentUser.status)
            context.startActivity(intent)
        }
    }


    override fun getItemCount(): Int {
        return itemList.size
    }

    inner class  MyViewHolder (itemView : View): RecyclerView.ViewHolder(itemView){
        val buyerFullName : TextView = itemView.findViewById(R.id.buyerFullNameAdmin)
        val sellerId : TextView = itemView.findViewById(R.id.sellerIdAdmin)
        val sellerFullName : TextView = itemView.findViewById(R.id.sellerFullNameAdmin)
        val sellerShopName : TextView = itemView.findViewById(R.id.sellerShopNameAdmin)
        val itemAmount : TextView = itemView.findViewById(R.id.itemAmountAdmin)
        val itemHandlingFee : TextView = itemView.findViewById(R.id.itemHandlingFeeAdmin)
        val status : TextView = itemView.findViewById(R.id.itemStatusAdmin)
        val approve : TextView = itemView.findViewById(R.id.approve_btn_item)
        val decline : TextView = itemView.findViewById(R.id.decline_btn_item)
        val claimed : ImageView = itemView.findViewById(R.id.claimedPic)
        val pulledOut : ImageView = itemView.findViewById(R.id.pulledOutPic)
        val edit : TextView = itemView.findViewById(R.id.editButton)
        val delete : ImageView = itemView.findViewById(R.id.deleteItem)
        val claimedOrDropped : TextView = itemView.findViewById(R.id.claimedOrDropped)
        val claimedOrDroppedDate : TextView = itemView.findViewById(R.id.claimedOrDroppedDate)
    }
}