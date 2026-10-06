package com.example.dowatch
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.content.Intent
import android.widget.Button
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.content.Context
import android.net.NetworkCapabilities
import android.net.ConnectivityManager
import android.view.View
import android.widget.LinearLayout

class Splash : AppCompatActivity() {

    private lateinit var error: LinearLayout
    private lateinit var splash: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContentView(R.layout.activity_splash)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(
                systemBars.left,
                systemBars.top,
                systemBars.right,
                systemBars.bottom
            )
            insets
        }

        error = findViewById(R.id.error)
        splash = findViewById(R.id.splash)

        checkConnection()

        val retry = findViewById<Button>(R.id.retry)
        val nsettings = findViewById<Button>(R.id.nsettings)

        retry.setOnClickListener {
            checkConnection()
        }

        nsettings.setOnClickListener {
            startActivity(
                Intent(Settings.ACTION_WIRELESS_SETTINGS)
            )
        }
    }

    private fun checkConnection() {
        if (hasInternet()) {
            error.visibility = View.GONE
            splash.visibility = View.VISIBLE

            Handler(Looper.getMainLooper()).postDelayed({

                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()

            }, 1000L)

        } else {

            error.visibility = View.VISIBLE
            splash.visibility = View.GONE
        }
    }

    override fun onResume() {
        super.onResume()

        if (::error.isInitialized) {
            checkConnection()
        }
    }

    private fun hasInternet(): Boolean {

        val cm = getSystemService(Context.CONNECTIVITY_SERVICE)
                as ConnectivityManager

        val network = cm.activeNetwork ?: return false

        val capabilities = cm.getNetworkCapabilities(network)
            ?: return false

        return capabilities.hasCapability(
            NetworkCapabilities.NET_CAPABILITY_INTERNET
        ) &&
                capabilities.hasCapability(
                    NetworkCapabilities.NET_CAPABILITY_VALIDATED
                )
    }
}