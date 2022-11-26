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
import com.droppingareamanager.app.databinding.ActivityMonthlySalesBinding
import com.droppingareamanager.app.models.SalesModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_daily_sales.*
import kotlinx.android.synthetic.main.activity_sales.*
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.collections.ArrayList


class MonthlySalesActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMonthlySalesBinding
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
        binding = ActivityMonthlySalesBinding.inflate(layoutInflater)
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

//        val args = intent.getBundleExtra("BUNDLE")
//        val `object` = args!!.getSerializable("ARRAYLIST")
//        val mAdapter = SalesAdapter(`object` as ArrayList<SalesModel>)
//
//        itemRecyclerView.adapter = mAdapter
        updateRecyclerView()

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
                &&binding.tvDatePicker.text.toString().length==7&&binding.tvDatePicker2.text.toString().length==7
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

        val formatter = DateTimeFormatter.ofPattern("yyyy-MM")
        val formatterDay = DateTimeFormatter.ofPattern("yyyy-MM-dd")

        var startDate = YearMonth.parse(binding.tvDatePicker.text, formatter)
        val initialStartDate = YearMonth.parse(binding.tvDatePicker.text, formatter)
        val endDate = YearMonth.parse(binding.tvDatePicker2.text, formatter)
        var count = 1
        val args = intent.getBundleExtra("BUNDLE")
        val `object` = args!!.getSerializable("ARRAYLIST") as ArrayList<SalesModel>
        while (startDate<=endDate){
            val items = SalesModel()
            val desiredFormat = DateTimeFormatter.ofPattern("MMM yyyy").format(startDate)
            items.date = desiredFormat.toString()
            for (item in `object`){
                var counts =0
                //iterate all items
                var sumTotal = 0
                while (counts <= `object`.size-1) {
                    val compDate = LocalDate.parse(`object`[counts].date, formatterDay)
                    if (startDate == YearMonth.from(compDate)){
                        //total all
                        sumTotal += `object`[counts].total?.toInt()!!
                    }
                    counts += 1
                }
                items.total = "₱ $sumTotal.00"
            }

            itemsArrayList.add(items)
            startDate = initialStartDate.plusMonths(count.toLong())
            count += 1
        }
        val mAdapter = SalesAdapter(itemsArrayList)
        itemRecyclerView.adapter = mAdapter
    }

    private fun updateable(myCalendar: Calendar) {
        val myFormat = "yyyy-MM"
        val sdf = SimpleDateFormat(myFormat)
        binding.tvDatePicker.setText(sdf.format(myCalendar.time))

    }
    private fun updateable2(myCalendar: Calendar) {
        val myFormat = "yyyy-MM"
        val sdf = SimpleDateFormat(myFormat)
        binding.tvDatePicker2.setText(sdf.format(myCalendar.time))

    }
}