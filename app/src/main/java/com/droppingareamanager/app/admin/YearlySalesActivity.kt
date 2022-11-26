package com.droppingareamanager.app.admin

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ProgressBar
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.droppingareamanager.app.Login
import com.droppingareamanager.app.adapters.SalesAdapter
import com.droppingareamanager.app.databinding.ActivityYearlySalesBinding
import com.droppingareamanager.app.models.SalesModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.Year
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.collections.ArrayList

class YearlySalesActivity : AppCompatActivity() {
    private lateinit var binding: ActivityYearlySalesBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var dbItems: DatabaseReference
    private lateinit var dbUserRef: DatabaseReference
    private lateinit var itemRecyclerView: RecyclerView
    private lateinit var newItemRecyclerView: RecyclerView
    private lateinit var tvLoadingData: ProgressBar
    private var itemsArrayList= ArrayList<SalesModel>()
    private var filteredItemArrayList= ArrayList<SalesModel>()

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityYearlySalesBinding.inflate(layoutInflater)
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

        updateRecyclerView()

        val myCalendar = Calendar.getInstance()
        val datePicker = DatePickerDialog.OnDateSetListener { views, year, month, dayOfMonth ->
            myCalendar.set(Calendar.YEAR, year)
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

    @RequiresApi(Build.VERSION_CODES.O)
    private fun updateRecyclerView() {

        filteredItemArrayList.clear()
        itemsArrayList.clear()

        val formatter = DateTimeFormatter.ofPattern("yyyy")
        val formatterDay = DateTimeFormatter.ofPattern("yyyy-MM-dd")

        var startDate = Year.parse(binding.tvDatePicker.text, formatter)
        val initialStartDate = Year.parse(binding.tvDatePicker.text, formatter)
        val endDate = Year.parse(binding.tvDatePicker2.text, formatter)
        var count = 1
        val args = intent.getBundleExtra("BUNDLE")
        val `object` = args!!.getSerializable("ARRAYLIST") as ArrayList<SalesModel>
        while (startDate<=endDate){
            val items = SalesModel()
            items.date = startDate.toString()
            for (item in `object`){
                var counts =0
                //iterate all items
                var sumTotal = 0
                while (counts <= `object`.size-1) {
                    val compDate = LocalDate.parse(`object`[counts].date, formatterDay)
                    if (startDate == Year.from(compDate)){
                        //total all
                        sumTotal += `object`[counts].total?.toInt()!!
                    }
                    counts += 1
                }
                items.total = "₱ $sumTotal.00"
            }

            itemsArrayList.add(items)
            startDate = initialStartDate.plusYears(count.toLong())
            count += 1
        }
        val mAdapter = SalesAdapter(itemsArrayList)
        itemRecyclerView.adapter = mAdapter
    }

    private fun updateable(myCalendar: Calendar) {
        val myFormat = "yyyy"
        val sdf = SimpleDateFormat(myFormat)
        binding.tvDatePicker.setText(sdf.format(myCalendar.time))

    }
    private fun updateable2(myCalendar: Calendar) {
        val myFormat = "yyyy"
        val sdf = SimpleDateFormat(myFormat)
        binding.tvDatePicker2.setText(sdf.format(myCalendar.time))

    }





















//        val barChart=binding.salesChart
//        val labels = arrayListOf(
//            "Jan", "Feb", "Mar",
//            "Abr", "May", "Jun",
//            "Jul", "Ago", "Set",
//            "Oct", "Nov", "Dec",
//            "Jan", "Feb", "Mar",
//            "Abr", "May", "Jun",
//            "Jul", "Ago", "Set",
//            "Oct", "Nov", "Dec"
//        )
//
//        barChart.xAxis.valueFormatter = IndexAxisValueFormatter(labels)
//        barChart.xAxis.position = XAxis.XAxisPosition.BOTTOM
//
//        barChart.setDrawGridBackground(false)
//        barChart.axisLeft.isEnabled = false
//        barChart.axisRight.isEnabled = false
//        barChart.description.isEnabled = false
//
//        val entries = arrayListOf(
//            BarEntry(0f, 10f),
//            BarEntry(1f, 20f),
//            BarEntry(2f, 30f),
//            BarEntry(3f, 40f),
//            BarEntry(4f, 50f),
//            BarEntry(5f, 60f),
//            BarEntry(6f, 70f),
//            BarEntry(7f, 60f),
//            BarEntry(8f, 50f),
//            BarEntry(9f, 40f),
//            BarEntry(10f, 30f),
//            BarEntry(11f, 20f),
//            BarEntry(12f, 10f),
//            BarEntry(13f, 20f),
//            BarEntry(14f, 30f),
//            BarEntry(15f, 40f),
//            BarEntry(16f, 50f),
//            BarEntry(17f, 60f),
//            BarEntry(18f, 70f),
//            BarEntry(19f, 60f),
//            BarEntry(20f, 50f),
//            BarEntry(21f, 40f),
//            BarEntry(22f, 30f),
//            BarEntry(23f, 20f)
//        )
//        val set = BarDataSet(entries, "BarDataSet")
//        set.valueTextSize = 12f
//
//        barChart.data = BarData(set)
//        barChart.invalidate()
}