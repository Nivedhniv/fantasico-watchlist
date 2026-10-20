
package com.example.dowatch

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.io.File
import java.io.FileOutputStream

class Account : AppCompatActivity() {

    private lateinit var editCard: LinearLayout

    private lateinit var overlay: View
    private lateinit var profileImage: ImageView
    private lateinit var selectedImage: ImageView
    private lateinit var tapText: TextView
    private lateinit var nameText: TextView
    private lateinit var usernameInput: EditText

    private var selectedImageUri: Uri? = null

    private val imagePicker =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            if (uri != null) {
                selectedImageUri = uri
                selectedImage.setImageURI(uri)
                tapText.visibility = View.VISIBLE
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_account)
        val profileImageCard =
            findViewById<com.google.android.material.card.MaterialCardView>(
                R.id.profileImageCard
            )

        profileImageCard.setOnClickListener {
            imagePicker.launch("image/*")
        }
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

        val prefs = getSharedPreferences("DowatchPrefs", MODE_PRIVATE)

        val terms = findViewById<LinearLayout>(R.id.terms)
        val help = findViewById<LinearLayout>(R.id.help)
        val backbtn = findViewById<ImageButton>(R.id.Back)
        overlay = findViewById(R.id.overlay)
        editCard = findViewById(R.id.editcard)
        val editButton = findViewById<LinearLayout>(R.id.editbtn)
        profileImage = findViewById(R.id.imageView2)
        selectedImage = findViewById(R.id.imgselected)
        tapText = findViewById(R.id.taptext)
        nameText = findViewById(R.id.name)
        usernameInput = findViewById(R.id.usrname)

        val cancelButton = findViewById<Button>(R.id.cancelReview)
        val updateButton = findViewById<Button>(R.id.updateReview)

        // Load saved username
        val nickname = prefs.getString("nickname", "") ?: ""
        nameText.text = nickname
        usernameInput.setText(nickname)

        // Load saved profile picture
        loadProfileImage()

        terms.setOnClickListener {
            startActivity(
                Intent(this, Terms::class.java)
                    .putExtra(Terms.EXTRA_PAGE, Terms.PAGE_LEGAL)
            )
        }

        help.setOnClickListener {
            startActivity(
                Intent(this, Terms::class.java)
                    .putExtra(Terms.EXTRA_PAGE, Terms.PAGE_HELP)
            )
        }

        backbtn.setOnClickListener {
            finish()
        }

        // Open editor
        editButton.setOnClickListener {
            usernameInput.setText(
                prefs.getString("nickname", "") ?: ""
            )

            selectedImageUri = null
            loadSelectedImage()
            editCard.visibility = View.VISIBLE
            overlay.visibility=View.VISIBLE
        }

        // Pick a profile picture
        selectedImage.setOnClickListener {
            imagePicker.launch("image/*")
        }

        // Cancel editing
        cancelButton.setOnClickListener {
            selectedImageUri = null
            editCard.visibility = View.GONE
            overlay.visibility=View.GONE
        }

        // Save profile
        updateButton.setOnClickListener {
            val newName = usernameInput.text.toString().trim()

            if (newName.isBlank()) {
                usernameInput.error = "Enter a username"
                return@setOnClickListener
            }

            // Save image first, if a new one was selected
            if (selectedImageUri != null) {
                val saved = saveProfileImage(selectedImageUri!!)

                if (!saved) {
                    Toast.makeText(
                        this,
                        "Could not save profile image",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@setOnClickListener
                }
            }

            prefs.edit()
                .putString("nickname", newName)
                .apply()

            nameText.text = newName

            val file = File(filesDir, "profile_picture.jpg")
            if (file.exists()) {
                profileImage.setImageURI(null)
                profileImage.setImageURI(Uri.fromFile(file))
            }

            editCard.visibility = View.GONE
            overlay.visibility=View.GONE

            Toast.makeText(
                this,
                "Profile updated",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun saveProfileImage(uri: Uri): Boolean {
        return try {
            val destination = File(filesDir, "profile_picture.jpg")

            contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(destination).use { output ->
                    input.copyTo(output)
                }
            } ?: return false

            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    private fun loadProfileImage() {
        val file = File(filesDir, "profile_picture.jpg")

        if (file.exists()) {
            profileImage.setImageURI(Uri.fromFile(file))
        } else {
            profileImage.setImageResource(R.drawable.round_account_circle_24)
        }
    }

    private fun loadSelectedImage() {
        val file = File(filesDir, "profile_picture.jpg")

        if (file.exists()) {
            selectedImage.setImageURI(Uri.fromFile(file))
        } else {
            selectedImage.setImageDrawable(null)
        }

        tapText.visibility = View.VISIBLE
    }
}
