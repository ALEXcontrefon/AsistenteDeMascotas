package com.alexcontre.asistentemascotas

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class LoadingActivity : AppCompatActivity() {

    private lateinit var welcomeMessage: TextView
    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_loading)

        welcomeMessage = findViewById(R.id.welcomeMessage)
        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        val userEmail = auth.currentUser?.email

        if (userEmail != null) {
            fetchUserData(userEmail)
        } else {
            navigateToMain()
        }
    }

    private fun fetchUserData(email: String) {
        firestore.collection("users")
            .whereEqualTo("email", email)
            .get()
            .addOnSuccessListener { documents ->
                if (documents != null && !documents.isEmpty) {
                    val user = documents.documents.first()
                    val firstName = user.getString("firstName") ?: "User"
                    val lastName = user.getString("lastName") ?: ""
                    updateWelcomeMessage("$firstName $lastName")
                } else {
                    updateWelcomeMessage("User")
                }
            }
            .addOnFailureListener {
                updateWelcomeMessage("User")
            }
            .addOnCompleteListener {
                Handler().postDelayed({
                    navigateToMain()
                }, 3000)
            }
    }

    private fun updateWelcomeMessage(name: String) {
        welcomeMessage.text = "Welcome, $name!"
    }

    private fun navigateToMain() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}
