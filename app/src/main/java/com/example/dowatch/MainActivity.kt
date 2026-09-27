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
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import androidx.activity.result.contract.ActivityResultContracts
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


        val btn=findViewById<ImageButton>(R.id.searchpage)
        val acc=findViewById<ImageButton>(R.id.account)

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
    private fun loadShows() {
        lifecycleScope.launch {
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

                when (show.status) {
                    "Watching" -> current.addView(card)
                    "Upcoming" -> upcoming.addView(card)
                    "Plan to watch" -> planning.addView(card)
                    "Completed" -> completed.addView(card)
                }

            }
            val count = app.database.showDao().getShowCount()
            var content=findViewById<ScrollView>(R.id.contentPanel)
            var welcome=findViewById<LinearLayout>(R.id.welcome)
            if(count==0){
                welcome.visibility= View.VISIBLE
                content.visibility=View.GONE
            }
            else{
                content.visibility=View.VISIBLE
                welcome.visibility= View.GONE
            }
        }
    }
}