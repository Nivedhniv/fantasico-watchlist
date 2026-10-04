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
        val acc=findViewById<ImageButton>(R.id.account)
        btn.visibility=View.GONE
        acc.visibility=View.GONE
        btn.setOnClickListener {
            val intent= Intent(this, Search::class.java)
            startActivity(intent)
        }
        acc.setOnClickListener {
            val intent= Intent(this, Account::class.java)
            startActivity(intent)
        }



    }
    override fun onResume() {
        super.onResume()
        loadShows()
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

            val app = application as Application
            val shows = app.database.showDao().getAllShows()

            current.removeAllViews()
            upcoming.removeAllViews()
            planning.removeAllViews()
            completed.removeAllViews()

            shows.forEach { show ->

                val card = layoutInflater.inflate(
                    R.layout.moviecard,
                    null
                )

                val image = card.findViewById<ImageView>(R.id.img)
                val title = card.findViewById<TextView>(R.id.title)
                val year = card.findViewById<TextView>(R.id.yearposter)

                title.text = show.title
                year.text = show.year ?: ""

                Glide.with(this@MainActivity)
                    .load(show.posterurl)
                    .into(image)

                card.setOnClickListener {
                    val intent = Intent(this@MainActivity, usr_details::class.java)
                    intent.putExtra("SHOW_ID", show.id)
                    startActivity(intent)
                }

                when (show.status) {
                    "Watching" -> current.addView(card)
                    "Upcoming" -> upcoming.addView(card)
                    "Plan to watch" -> planning.addView(card)
                    "Completed" -> completed.addView(card)
                }

            }
            val count = app.database.showDao().getShowCount()
            val welcome=findViewById<LinearLayout>(R.id.welcome)
            if(count==0){
                loading.visibility=View.GONE
                welcome.visibility= View.VISIBLE
                content.visibility=View.GONE
                val btn=findViewById<ImageButton>(R.id.searchpage)
                val acc=findViewById<ImageButton>(R.id.account)

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
                            btn.visibility=View.VISIBLE
                            acc.visibility=View.VISIBLE
                            prefs.edit()
                                .putString("nickname", namestr)
                                .apply()

                            val message = "Hey $namestr Search for your favorite shows and to add into your watch list"

                            msg.text = ""

                            message.forEachIndexed { index, _ ->
                                msg.postDelayed({
                                    msg.text = message.substring(0, index + 1)
                                }, index * 40L)
                            }

                            updateGreeting()
                            chip.setImageResource(R.drawable.happy)
                            nameok.visibility=View.GONE
                            name.visibility=View.GONE
                        }
                    }
                }
                else{
                    btn.visibility=View.VISIBLE
                    acc.visibility=View.VISIBLE
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
                val btn=findViewById<ImageButton>(R.id.searchpage)
                val acc=findViewById<ImageButton>(R.id.account)
                btn.visibility=View.VISIBLE
                acc.visibility=View.VISIBLE
                content.visibility=View.VISIBLE
                loading.visibility=View.GONE
                welcome.visibility= View.GONE
            }
        }
    }
}