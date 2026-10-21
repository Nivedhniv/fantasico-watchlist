package com.example.dowatch

import android.os.Bundle
import android.widget.TextView
import android.content.Intent
import android.widget.ImageButton
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.view.View
import android.util.Log
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.Toast
import java.util.Calendar
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import kotlinx.coroutines.launch
import android.view.ViewGroup
import android.widget.PopupWindow
import android.view.MotionEvent
import androidx.core.view.isEmpty
import java.io.File
import android.net.Uri

class MainActivity : AppCompatActivity()
{
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        updateGreeting()
        val btn=findViewById<ImageButton>(R.id.searchpage)
        val accountButton =
            findViewById<com.google.android.material.imageview.ShapeableImageView>(
                R.id.account
            )

        btn.setOnClickListener {
            val intent= Intent(this, Search::class.java)
            startActivity(intent)
        }
        accountButton.setOnClickListener {
            val intent= Intent(this, Account::class.java)
            startActivity(intent)
        }
    }
    override fun onResume() {
        super.onResume()
        loadShows()
        updateGreeting()
        updateProfileImage()
    }
    private fun updateProfileImage() {
        val accountButton =
            findViewById<com.google.android.material.imageview.ShapeableImageView>(
                R.id.account
            )

        val imageFile = File(filesDir, "profile_picture.jpg")

        if (imageFile.exists()) {
            accountButton.setImageDrawable(null)
            accountButton.setImageURI(Uri.fromFile(imageFile))
        } else {
            accountButton.setImageResource(
                R.drawable.round_account_circle_24
            )
        }
    }
    fun updateGreeting() {
        val greet = findViewById<TextView>(R.id.greeting)

        val prefs = getSharedPreferences("DowatchPrefs", MODE_PRIVATE)
        val nickname = prefs.getString("nickname", null)

        val greeting = when (Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) {
            in 5..11 -> "Good morning"
            in 12..16 -> "Good afternoon"
            in 17..20 -> "Good evening"
            else -> "Good night"
        }

        if (nickname == null) {
            greet.text = greeting
        } else {
            greet.text = "$greeting, $nickname"
        }
    }
    private fun loadShows() {
        lifecycleScope.launch {
            val loading=findViewById<FrameLayout>(R.id.loading)
            val content=findViewById<ScrollView>(R.id.content)

            val current=findViewById<LinearLayout>(R.id.current)
            val upcoming=findViewById<LinearLayout>(R.id.upcoming)
            val planning=findViewById<LinearLayout>(R.id.planwatch)
            val completed=findViewById<LinearLayout>(R.id.completed)
            val dropped=findViewById<LinearLayout>(R.id.dropped)

            val app = application as Application
            val shows = app.database.showDao().getAllShows()

            current.removeAllViews()
            upcoming.removeAllViews()
            planning.removeAllViews()
            completed.removeAllViews()
            dropped.removeAllViews()

            shows.forEach { show ->

                val card = layoutInflater.inflate(
                    R.layout.moviecard,
                    null
                )
                card.setOnTouchListener { view, event ->
                    when (event.action) {
                        MotionEvent.ACTION_DOWN -> {
                            view.animate()
                                .scaleX(0.96f)
                                .scaleY(0.96f)
                                .setDuration(150)
                                .start()
                        }
                        MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                            view.animate()
                                .scaleX(1f)
                                .scaleY(1f)
                                .setDuration(150)
                                .start()
                        }
                    }
                    false
                }
                val image = card.findViewById<ImageView>(R.id.img)
                val title = card.findViewById<TextView>(R.id.title)
                val year = card.findViewById<TextView>(R.id.yearposter)
                val rating=card.findViewById<TextView>(R.id.usrrating)
                val ratingbtn=card.findViewById<com.google.android.material.card.MaterialCardView>(R.id.ratingbutton)

                if (show.status == "Upcoming" || show.status == "Plan To Watch") {
                    ratingbtn.visibility = View.GONE
                } else {
                    ratingbtn.visibility = View.VISIBLE
                    rating.text = ((show.myrating ?: 0f) * 2).toString()
                }
                title.text = show.title
                year.text = show.year ?: ""
                rating.text = ((show.myrating ?: 0f) * 2).toString()

                Glide.with(this@MainActivity)
                    .load(show.posterurl)
                    .into(image)

                card.setOnClickListener {
                    val intent = Intent(this@MainActivity, usr_details::class.java)
                    intent.putExtra("SHOW_ID", show.id)
                    startActivity(intent)
                }
                card.setOnLongClickListener {
                    val showid=show.id.toString()
                    popup(card,showid)
                    true
                }
                when (show.status) {
                    "Currently Watching" -> current.addView(card)
                    "Upcoming" -> upcoming.addView(card)
                    "Plan To Watch" -> planning.addView(card)
                    "Completed" -> completed.addView(card)
                    "Dropped" -> dropped.addView(card)
                }
            }

            val curtitle=findViewById<TextView>(R.id.one)
            val upcomtitle=findViewById<TextView>(R.id.two)
            val plantitle=findViewById<TextView>(R.id.three)
            val comptitle=findViewById<TextView>(R.id.four)
            val droptitle=findViewById<TextView>(R.id.five)

            curtitle.visibility =
                if (current.isEmpty()) View.GONE else View.VISIBLE

            upcomtitle.visibility =
                if (upcoming.isEmpty()) View.GONE else View.VISIBLE

            plantitle.visibility =
                if (planning.isEmpty()) View.GONE else View.VISIBLE

            comptitle.visibility =
                if (completed.isEmpty()) View.GONE else View.VISIBLE

            droptitle.visibility =
                if (dropped.isEmpty()) View.GONE else View.VISIBLE

            current.visibility =
                if (current.isEmpty()) View.GONE else View.VISIBLE

            upcoming.visibility =
                if (upcoming.isEmpty()) View.GONE else View.VISIBLE

            planning.visibility =
                if (planning.isEmpty()) View.GONE else View.VISIBLE

            completed.visibility =
                if (completed.isEmpty()) View.GONE else View.VISIBLE

            dropped.visibility =
                if (dropped.isEmpty()) View.GONE else View.VISIBLE

            android.util.Log.d(
                "Dowatch",
                "Visibility: current=${current.visibility}, " +
                        "upcoming=${upcoming.visibility}, " +
                        "planning=${planning.visibility}, " +
                        "completed=${completed.visibility}, " +
                        "dropped=${dropped.visibility}"
            )

            val count = app.database.showDao().getShowCount()
            val welcome=findViewById<LinearLayout>(R.id.welcome)
            val toppanel=findViewById<LinearLayout>(R.id.toppanel)
            if(count==0){
                loading.visibility=View.GONE
                welcome.visibility= View.VISIBLE
                content.visibility=View.GONE
                val name=findViewById<EditText>(R.id.name)
                val nameok=findViewById<Button>(R.id.nameok)
                val msg=findViewById<TextView>(R.id.msg)
                val chip=findViewById<ImageView>(R.id.chip)

                val prefs = getSharedPreferences("DowatchPrefs", MODE_PRIVATE)
                val nickname = prefs.getString("nickname", null)

                if(nickname==null){
                    val message = "Chip chip !! hello Welcome User What should we call you??"
                    msg.text = ""

                    message.forEachIndexed { index, _ ->
                        msg.postDelayed({
                            msg.text = message.substring(0, index + 1)
                        }, index * 40L)
                    }
                    name.visibility=View.VISIBLE
                    nameok.visibility=View.VISIBLE

                    nameok.setOnClickListener {
                        val namestr=name.text.toString().trim()
                        if(namestr.isBlank()){
                            Toast.makeText(this@MainActivity,"Enter Nickname",Toast.LENGTH_SHORT).show()
                            return@setOnClickListener
                        }
                        else{
                            prefs.edit()
                                .putString("nickname", namestr)
                                .apply()
                            val imm = getSystemService(INPUT_METHOD_SERVICE)
                                    as android.view.inputmethod.InputMethodManager

                            imm.hideSoftInputFromWindow(name.windowToken, 0)
                            name.clearFocus()
                            val message = "Hey $namestr Search for your favorite shows and to add into your watch list"

                            msg.text = ""

                            message.forEachIndexed { index, _ ->
                                msg.postDelayed({
                                    msg.text = message.substring(0, index + 1)
                                }, index * 40L)
                            }
                            toppanel.visibility = View.INVISIBLE

                            toppanel.post {
                                toppanel.translationY = -toppanel.height.toFloat()

                                toppanel.visibility = View.VISIBLE

                                toppanel.animate()
                                    .translationY(0f)
                                    .setDuration(300)
                                    .start()
                            }

                            updateGreeting()
                            chip.setImageResource(R.drawable.happy)
                            nameok.visibility=View.GONE
                            name.visibility=View.GONE
                        }
                    }
                }
                else{
                    toppanel.visibility=View.VISIBLE
                    chip.setImageResource(R.drawable.happy)
                    val message="Hey $nickname Search for your favorite shows and to add into your watch list"
                    msg.text = ""
                    message.forEachIndexed { index, _ ->
                        msg.postDelayed({
                            msg.text = message.substring(0, index + 1)
                        }, index * 40L)
                    }

                    nameok.visibility=View.GONE
                    name.visibility=View.GONE
                }
            }
            else{
                toppanel.visibility= View.VISIBLE
                content.visibility=View.VISIBLE
                loading.visibility=View.GONE
                welcome.visibility= View.GONE
            }
        }
    }
    private fun popup(anchor: View, id: String) {
        val popupView = layoutInflater.inflate(
            R.layout.buttongroup,
            null
        )

        val popupWindow = PopupWindow(
            popupView,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            true
        )
        val info=popupView.findViewById<LinearLayout>(R.id.info)
        val intid=id.toInt()
        info.setOnClickListener {
            popupWindow.dismiss()
            val intent = Intent(this@MainActivity, usr_details::class.java)
            intent.putExtra("SHOW_ID", intid)
            startActivity(intent)
        }
        val delete=popupView.findViewById<LinearLayout>(R.id.delete)
        delete.setOnClickListener {
            popupWindow.dismiss()
            lifecycleScope.launch {
                val app = application as Application
                app.database.showDao().delete(intid)
                Toast.makeText(this@MainActivity,"Deleted Show",Toast.LENGTH_SHORT).show()
                loadShows()
            }
        }
        popupWindow.elevation = 12f
        popupWindow.showAsDropDown(anchor,230,-250)
    }
}