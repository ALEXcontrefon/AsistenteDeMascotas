package com.alexcontre.asistentemascotas

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class RegisterActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val firstNameField: EditText = findViewById(R.id.firstNameField)
        val lastNameField: EditText = findViewById(R.id.lastNameField)
        val phoneField: EditText = findViewById(R.id.phoneField)
        val birthDateField: EditText = findViewById(R.id.birthDateField)
        val emailField: EditText = findViewById(R.id.emailField)
        val passwordField: EditText = findViewById(R.id.passwordField)
        val registerButton: Button = findViewById(R.id.registerButton)
        val backToLoginButton: Button = findViewById(R.id.backToLoginButton)

        registerButton.setOnClickListener {
            val firstName = firstNameField.text.toString().trim()
            val lastName = lastNameField.text.toString().trim()
            val phone = phoneField.text.toString().trim()
            val birthDate = birthDateField.text.toString().trim()
            val email = emailField.text.toString().trim()
            val password = passwordField.text.toString().trim()

            if (firstName.isEmpty() || lastName.isEmpty() || phone.isEmpty() || birthDate.isEmpty() ||
                email.isEmpty() || password.isEmpty()
            ) {
                Toast.makeText(this, "Por favor, completa todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val userId = auth.currentUser?.uid ?: ""
                        saveUserToFirestore(userId, firstName, lastName, phone, birthDate, email)
                    } else {
                        Toast.makeText(
                            this,
                            "Error al registrar: ${task.exception?.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
        }

        backToLoginButton.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun saveUserToFirestore(
        userId: String,
        firstName: String,
        lastName: String,
        phone: String,
        birthDate: String,
        email: String
    ) {
        val user = hashMapOf(
            "firstName" to firstName,
            "lastName" to lastName,
            "phone" to phone,
            "birthDate" to birthDate,
            "email" to email
        )

        db.collection("users").document(userId).set(user)
            .addOnSuccessListener {
                val intent = Intent(this, LoadingActivity::class.java)
                startActivity(intent)
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error al guardar usuario: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
