package com.droppingareamanager.app.admin

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.droppingareamanager.app.Login
import com.droppingareamanager.app.adapters.SalesAdapter
import com.droppingareamanager.app.databinding.ActivityDailySalesBinding
import com.droppingareamanager.app.models.SalesModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_daily_sales.*
import kotlinx.android.synthetic.main.activity_sales.*
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.collections.ArrayList


class DailySalesActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDailySalesBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var dbItems: DatabaseReference
    private lateinit var dbUserRef: DatabaseReference
    private lateinit var itemRecyclerView: RecyclerView
    private lateinit var newItemRecyclerView: RecyclerView
    private lateinit var tvLoadingData: ProgressBar
    private var itemsArrayList= ArrayList<SalesModel>()
    private var filteredItemArrayList= ArrayList<SalesModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDailySalesBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        itemRecyclerView = binding.itemsListRecyclerview
        newItemRecyclerView = binding.itemsListRecyclerview
        tvLoadingData = binding.tvLoadingData
        itemRecyclerView.layoutManager = LinearLayoutManager(this)
        itemRecyclerView.setHasFixedSize(true)
        auth = Firebase.auth
        dbItems = FirebaseDatabase.getInstance().getReference("Items")
        dbUserRef = FirebaseDatabase.getInstance().getReference("User")
        val userId = auth.currentUser?.uid.toString()

        binding.tvDatePicker.setText(intent.getStringExtra("startDate"))
        binding.tvDatePicker2.setText(intent.getStringExtra("endDate"))

        val args = intent.getBundleExtra("BUNDLE")
        val `object` = args!!.getSerializable("ARRAYLIST")
        val mAdapter = SalesAdapter(`object` as ArrayList<SalesModel>)

        itemRecyclerView.adapter = mAdapter

        val myCalendar = Calendar.getInstance()
        val datePicker = DatePickerDialog.OnDateSetListener { views, year, month, dayOfMonth ->
            myCalendar.set(Calendar.YEAR, year)
            myCalendar.set(Calendar.MONTH, month)
            myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            updateable(myCalendar)
        }
        val datePicker2 = DatePickerDialog.OnDateSetListener { views, year, month, dayOfMonth ->
            myCalendar.set(Calendar.YEAR, year)
            myCalendar.set(Calendar.MONTH, month)
            myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            updateable2(myCalendar)
        }
        binding.btnDatePicker.setOnClickListener {
            DatePickerDialog(this, datePicker, myCalendar.get(Calendar.YEAR),
                myCalendar.get(Calendar.MONTH), myCalendar.get(Calendar.DAY_OF_MONTH)).show()
        }
        binding.btnDatePicker2.setOnClickListener {
            DatePickerDialog(this, datePicker2, myCalendar.get(Calendar.YEAR),
                myCalendar.get(Calendar.MONTH), myCalendar.get(Calendar.DAY_OF_MONTH)).show()
        }

        binding.buttonViewSales.setOnClickListener{
            if (binding.tvDatePicker.text.toString()!=""&&binding.tvDatePicker2.text.toString()!=""
                &&binding.tvDatePicker.text.toString().length==10&&binding.tvDatePicker2.text.toString().length==10
                &&binding.tvDatePicker.text.toString()<=binding.tvDatePicker2.text.toString()
            ){
                updateRecyclerView()
            }
        }
        binding.backBtn.setOnClickListener{
            val intent = Intent(this, SalesActivity::class.java)
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

    private fun updateRecyclerView() {

        filteredItemArrayList.clear()
        itemsArrayList.clear()

        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

        var startDate = LocalDate.parse(binding.tvDatePicker.text, formatter)
        val initialStartDate = LocalDate.parse(binding.tvDatePicker.text, formatter)
        val endDate = LocalDate.parse(binding.tvDatePicker2.text, formatter)
        var count = 1
        val args = intent.getBundleExtra("BUNDLE")
        val `object` = args!!.getSerializable("ARRAYLIST") as ArrayList<SalesModel>
        while (startDate<=endDate){
            val items = SalesModel()
            items.date = startDate.toString()
//            if (`object` != null) {
//                itemsArrayList.add(`object` as SalesModel)
//            }
//            `object`.forEach(){
//                items.total = it.toString()
//            }
            for (item in `object`){
                var counts =0
                while (counts <= `object`.size-1) {
                    if (startDate.toString() == `object`[counts].date){
                        items.total = "₱ ${`object`[counts].total}.00"
                    }
                    counts += 1
                }
            }

            itemsArrayList.add(items)
            startDate = initialStartDate.plusDays(count.toLong())
            count += 1
        }
        val mAdapter = SalesAdapter(itemsArrayList)
        itemRecyclerView.adapter = mAdapter
    }

    private fun updateable(myCalendar: Calendar) {
        val myFormat = "yyyy-MM-dd"
        val sdf = SimpleDateFormat(myFormat)
        binding.tvDatePicker.setText(sdf.format(myCalendar.time))

    }
    private fun updateable2(myCalendar: Calendar) {
        val myFormat = "yyyy-MM-dd"
        val sdf = SimpleDateFormat(myFormat)
        binding.tvDatePicker2.setText(sdf.format(myCalendar.time))

    }
}