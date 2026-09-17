package com.example.dowatch

import android.os.Bundle
import android.widget.TextView
import android.content.Intent
import android.widget.ImageButton
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.bottomsheet.BottomSheetDialog
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.FrameLayout
import android.widget.ImageView
import android.app.DatePickerDialog
import android.widget.EditText
import java.util.Calendar
import android.widget.LinearLayout
import  com.google.android.material.bottomsheet.BottomSheetBehavior
import androidx.activity.result.contract.ActivityResultContracts


class MainActivity : AppCompatActivity()
{
    private lateinit var img: ImageView
    private lateinit var text: TextView

    private val filePicker = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            img.setImageURI(uri)
            img.visibility = View.VISIBLE
            text.visibility=View.GONE
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val btn=findViewById<ImageButton>(R.id.searchpage)
        val acc=findViewById<ImageButton>(R.id.account)
        val ios=findViewById<TextView>(R.id.greeting)

        btn.setOnClickListener {
            val intent= Intent(this, Search::class.java)
            startActivity(intent)
        }
        acc.setOnClickListener {
            val intent= Intent(this, Account::class.java)
            startActivity(intent)
        }
        ios.setOnClickListener {
            val intent= Intent(this, Iosdemo::class.java)
            startActivity(intent)
        }
        val addshow=findViewById<ImageButton>(R.id.addbtn)

        addshow.setOnClickListener {
            val sheetview=layoutInflater.inflate(R.layout.add_show,null)
            val dialog= BottomSheetDialog(this)
            dialog.setContentView((sheetview))
            dialog.show()
            img = sheetview.findViewById(R.id.addimg)
            text = sheetview.findViewById(R.id.selectImageText)
            val rating=sheetview.findViewById<LinearLayout>(R.id.ratingSection)
            rating.visibility=View.GONE
            //date area
            val date=sheetview.findViewById<EditText>(R.id.date)
            val datearea=sheetview.findViewById<LinearLayout>(R.id.datesection)
            datearea.visibility=View.GONE

            //review
            val reviewarea=sheetview.findViewById<LinearLayout>(R.id.reviewsection)
            reviewarea.visibility=View.GONE

            date.setOnClickListener {

                val calendar = Calendar.getInstance()

                val datePicker = DatePickerDialog(
                    this,
                    { _, year, month, day ->
                        date.setText("$day/${month + 1}/$year")
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
                )

                datePicker.show()
            }

            val bottomSheet = dialog.findViewById<View>(
                com.google.android.material.R.id.design_bottom_sheet
            )
            bottomSheet?.let {
                val behavior = BottomSheetBehavior.from(it)
                behavior.state = BottomSheetBehavior.STATE_EXPANDED
                behavior.isDraggable = false
            }
            val imagePicker = sheetview.findViewById<FrameLayout>(R.id.imagePicker)
            imagePicker.setOnClickListener {
                    filePicker.launch("image/*")
                }
            val status=arrayOf("Watching","completed","plan to watch","Upcoming")
            val dropdown = sheetview.findViewById<AutoCompleteTextView>(R.id.status)

            val adapter= ArrayAdapter(this,android.R.layout.simple_dropdown_item_1line,status)
            dropdown.setAdapter(adapter)

            dropdown.setOnItemClickListener { parent, _, position, _ ->

                when (parent.getItemAtPosition(position).toString()) {

                    "Watching" -> {
                        rating.visibility=View.VISIBLE
                        datearea.visibility=View.GONE
                        reviewarea.visibility=View.VISIBLE
                    }

                    "completed" -> {
                        rating.visibility=View.VISIBLE
                        datearea.visibility=View.GONE
                        reviewarea.visibility=View.VISIBLE
                    }

                    "plan to watch" -> {
                        rating.visibility=View.GONE
                        datearea.visibility=View.GONE
                        reviewarea.visibility= View.GONE
                    }

                    "Upcoming" ->{
                        rating.visibility=View.GONE
                        datearea.visibility=View.VISIBLE
                        reviewarea.visibility= View.GONE
                    }
                }
            val closebtn=sheetview.findViewById<ImageButton>(R.id.closeBtn)
            closebtn.setOnClickListener {
                dialog.dismiss()
            }
        }
        }
    }
}