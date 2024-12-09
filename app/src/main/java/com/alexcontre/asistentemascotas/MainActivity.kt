package com.alexcontre.asistentemascotas

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.auth.FirebaseAuth
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var petListContainer: LinearLayout
    private lateinit var reminderListContainer: LinearLayout
    private val petList = mutableListOf<Pet>()
    private val reminderList = mutableListOf<Reminder>()

    val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        petListContainer = findViewById(R.id.petListContainer)
        reminderListContainer = findViewById(R.id.reminderListContainer)

        val petEditButton: Button = findViewById(R.id.editPetButton)
        petEditButton.setOnClickListener {
            val popupMenu = PopupMenu(this, petEditButton)
            menuInflater.inflate(R.menu.pet_settings_menu, popupMenu.menu)

            popupMenu.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.addPet -> {
                        showAddPetDialog()
                        true
                    }
                    R.id.editPet -> {
                        showEditPetDialog()
                        true
                    }
                    R.id.deletePet -> {
                        showDeletePetDialog()
                        true
                    }
                    else -> false
                }
            }
            popupMenu.show()
        }

        val backToLoginButton: Button = findViewById(R.id.backToLoginButton)
        backToLoginButton.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }

        val reminderEditButton: Button = findViewById(R.id.editReminderButton)
        reminderEditButton.setOnClickListener {
            val popupMenu = PopupMenu(this, reminderEditButton)
            menuInflater.inflate(R.menu.reminder_settings_menu, popupMenu.menu)

            popupMenu.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.addReminder -> {
                        showAddReminderDialog()
                        true
                    }
                    R.id.editReminder -> {
                        showEditReminderDialog()
                        true
                    }
                    R.id.deleteReminder -> {
                        showDeleteReminderDialog()
                        true
                    }
                    else -> false
                }
            }
            popupMenu.show()
        }

        updatePetList()
        updateReminderList()
    }

    private fun updatePetList() {
        val progressBar: ProgressBar = findViewById(R.id.loadingProgressBar)
        progressBar.visibility = View.VISIBLE

        petListContainer.removeAllViews()
        val titleTextView = TextView(this)
        titleTextView.text = "Pet List"
        titleTextView.textSize = 20f
        titleTextView.setTypeface(null, android.graphics.Typeface.BOLD)
        petListContainer.addView(titleTextView)

        val userId = FirebaseAuth.getInstance().currentUser?.uid



        if (userId != null) {
            db.collection("pets")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener { result ->
                    petList.clear()
                    for (document in result) {
                        val pet = Pet(
                            name = document.getString("name") ?: "",
                            breed = document.getString("breed") ?: "",
                            age = document.getLong("age")?.toInt() ?: 0,
                            type = document.getString("type") ?: "",
                            sex = document.getString("sex") ?: "Desconocido"
                        )
                        petList.add(pet)
                    }

                    petList.forEach { pet ->
                        val petDetails = """
                        Name: ${pet.name}
                        Breed: ${pet.breed}
                        Age: ${pet.age}
                        Type: ${pet.type}
                        Sex: ${pet.sex}
                    """.trimIndent()
                        val textView = TextView(this)
                        textView.text = petDetails
                        textView.textSize = 18f
                        petListContainer.addView(textView)
                    }
                    progressBar.visibility = View.GONE
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error al cargar las mascotas: ${e.message}", Toast.LENGTH_SHORT).show()
                    progressBar.visibility = View.GONE
                }
        } else {
            Toast.makeText(this, "No user logged in", Toast.LENGTH_SHORT).show()
            progressBar.visibility = View.GONE
        }
    }



    private fun showAddPetDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_pet, null)
        val nameInput = dialogView.findViewById<EditText>(R.id.petNameInput)
        val breedInput = dialogView.findViewById<EditText>(R.id.petBreedInput)
        val ageInput = dialogView.findViewById<EditText>(R.id.petAgeInput)
        val petTypeInput = dialogView.findViewById<Spinner>(R.id.petTypeInput)
        val petSexInput = dialogView.findViewById<Spinner>(R.id.petSexInput)

        val petTypes = resources.getStringArray(R.array.pet_types)
        val petSexes = resources.getStringArray(R.array.pet_sex)

        petTypeInput.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, petTypes).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        petSexInput.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, petSexes).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }

        AlertDialog.Builder(this)
            .setTitle("Add Pet")
            .setView(dialogView)
            .setPositiveButton("Add") { dialogInterface, _ ->
                val name = nameInput.text.toString().trim()
                val breed = breedInput.text.toString().trim()
                val age = ageInput.text.toString().trim().toIntOrNull() ?: 0
                val petType = petTypeInput.selectedItem.toString()
                val petSex = petSexInput.selectedItem.toString()
                val userEmail = FirebaseAuth.getInstance().currentUser?.email

                if (name.isNotEmpty() && breed.isNotEmpty()) {
                    val petData = hashMapOf(
                        "name" to name,
                        "breed" to breed,
                        "age" to age,
                        "type" to petType,
                        "sex" to petSex,
                        "userId" to FirebaseAuth.getInstance().currentUser?.uid,
                        "userEmail" to userEmail
                    )

                    db.collection("pets")
                        .add(petData)
                        .addOnSuccessListener {
                            Toast.makeText(this, "Mascota registrada con éxito", Toast.LENGTH_SHORT).show()
                            updatePetList()
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(this, "Error al registrar mascota: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    Toast.makeText(this, "Por favor, completa todos los campos", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }



    private fun showEditPetDialog() {
        if (petList.isEmpty()) {
            Toast.makeText(this, "No pets to edit", Toast.LENGTH_SHORT).show()
            return
        }

        val petNames = petList.map { it.name }.toTypedArray()
        var selectedPetIndex = 0

        AlertDialog.Builder(this)
            .setTitle("Select Pet to Edit")
            .setSingleChoiceItems(petNames, 0) { _, which ->
                selectedPetIndex = which
            }
            .setPositiveButton("Next") { _, _ ->
                val selectedPet = petList[selectedPetIndex]

                val dialogView = layoutInflater.inflate(R.layout.dialog_add_pet, null)
                val nameInput = dialogView.findViewById<EditText>(R.id.petNameInput)
                val breedInput = dialogView.findViewById<EditText>(R.id.petBreedInput)
                val ageInput = dialogView.findViewById<EditText>(R.id.petAgeInput)
                val petTypeInput = dialogView.findViewById<Spinner>(R.id.petTypeInput)
                val petSexInput = dialogView.findViewById<Spinner>(R.id.petSexInput)

                nameInput.setText(selectedPet.name)
                breedInput.setText(selectedPet.breed)
                ageInput.setText(selectedPet.age.toString())

                val petTypes = resources.getStringArray(R.array.pet_types)
                val typeAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, petTypes)
                typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                petTypeInput.adapter = typeAdapter
                petTypeInput.setSelection(petTypes.indexOf(selectedPet.type))

                val petSexes = resources.getStringArray(R.array.pet_sex)
                val sexAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, petSexes)
                sexAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                petSexInput.adapter = sexAdapter
                petSexInput.setSelection(petSexes.indexOf(selectedPet.sex))

                AlertDialog.Builder(this)
                    .setTitle("Edit Pet")
                    .setView(dialogView)
                    .setPositiveButton("Save") { _, _ ->
                        val name = nameInput.text.toString().trim()
                        val breed = breedInput.text.toString().trim()
                        val age = ageInput.text.toString().trim().toIntOrNull() ?: 0
                        val petType = petTypeInput.selectedItem.toString()
                        val petSex = petSexInput.selectedItem.toString()

                        if (name.isNotEmpty() && breed.isNotEmpty()) {
                            db.collection("pets")
                                .whereEqualTo("name", selectedPet.name)
                                .whereEqualTo("userId", FirebaseAuth.getInstance().currentUser?.uid)
                                .get()
                                .addOnSuccessListener { documents ->
                                    for (doc in documents) {
                                        db.collection("pets").document(doc.id)
                                            .update(
                                                "name", name,
                                                "breed", breed,
                                                "age", age,
                                                "type", petType,
                                                "sex", petSex
                                            )
                                            .addOnSuccessListener {
                                                Toast.makeText(this, "Pet updated successfully", Toast.LENGTH_SHORT).show()
                                                updatePetList()
                                            }
                                            .addOnFailureListener { e ->
                                                Toast.makeText(this, "Failed to update pet: ${e.message}", Toast.LENGTH_SHORT).show()
                                            }
                                    }
                                }
                        } else {
                            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                        }
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }



    private fun showDeletePetDialog() {
        if (petList.isEmpty()) {
            Toast.makeText(this, "No pets to delete", Toast.LENGTH_SHORT).show()
            return
        }

        val petNames = petList.map { it.name }.toTypedArray()
        var selectedPetIndex = 0

        AlertDialog.Builder(this)
            .setTitle("Select Pet to Delete")
            .setSingleChoiceItems(petNames, 0) { _, which ->
                selectedPetIndex = which
            }
            .setPositiveButton("Delete") { _, _ ->
                val selectedPet = petList[selectedPetIndex]

                db.collection("pets")
                    .whereEqualTo("name", selectedPet.name)
                    .whereEqualTo("userId", FirebaseAuth.getInstance().currentUser?.uid)
                    .get()
                    .addOnSuccessListener { documents ->
                        for (doc in documents) {
                            db.collection("pets").document(doc.id)
                                .delete()
                                .addOnSuccessListener {
                                    Toast.makeText(this, "Pet deleted successfully", Toast.LENGTH_SHORT).show()
                                    updatePetList()
                                }
                                .addOnFailureListener { e ->
                                    Toast.makeText(this, "Failed to delete pet: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                        }
                    }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun updateReminderList() {
        reminderListContainer.removeAllViews()

        val titleTextView = TextView(this)
        titleTextView.text = "Reminder List"
        titleTextView.textSize = 20f
        titleTextView.setTypeface(null, android.graphics.Typeface.BOLD)
        reminderListContainer.addView(titleTextView)

        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            db.collection("reminders")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener { result ->
                    reminderList.clear()

                    for (document in result) {
                        val timestamp = document.getTimestamp("date")
                        val formattedDate = timestamp?.toDate()?.let {
                            SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(it)
                        } ?: "No date"

                        val reminder = Reminder(
                            title = document.getString("title") ?: "No Title",
                            description = document.getString("description") ?: "No Description",
                            date = formattedDate
                        )
                        reminderList.add(reminder)
                    }

                    for (reminder in reminderList) {
                        val textView = TextView(this)
                        textView.text = "Title: ${reminder.title}\nDescription: ${reminder.description}\nDate: ${reminder.date}"
                        textView.textSize = 18f
                        textView.setPadding(10, 10, 10, 10)
                        reminderListContainer.addView(textView)
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error al cargar los recordatorios: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(this, "No user logged in", Toast.LENGTH_SHORT).show()
        }
    }



    private fun showAddReminderDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_reminder, null)
        val titleInput = dialogView.findViewById<EditText>(R.id.reminderTitleInput)
        val descriptionInput = dialogView.findViewById<EditText>(R.id.reminderDescriptionInput)
        val dateInput = dialogView.findViewById<EditText>(R.id.reminderDateInput)
        val timeInput = dialogView.findViewById<EditText>(R.id.reminderTimeInput)

        AlertDialog.Builder(this)
            .setTitle("Add Reminder")
            .setView(dialogView)
            .setPositiveButton("Add") { _, _ ->
                val title = titleInput.text.toString().trim()
                val description = descriptionInput.text.toString().trim()
                val dateInputText = dateInput.text.toString().trim()
                val timeInputText = timeInput.text.toString().trim()

                if (title.isNotEmpty() && description.isNotEmpty() && dateInputText.isNotEmpty() && timeInputText.isNotEmpty()) {
                    val userEmail = FirebaseAuth.getInstance().currentUser?.email
                    val userId = FirebaseAuth.getInstance().currentUser?.uid

                    val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                    dateFormat.isLenient = false

                    try {
                        val reminderDateTime = dateFormat.parse("$dateInputText $timeInputText")

                        if (reminderDateTime != null) {
                            val reminderData = hashMapOf(
                                "title" to title,
                                "description" to description,
                                "date" to Timestamp(reminderDateTime),
                                "userId" to userId,
                                "userEmail" to userEmail
                            )

                            db.collection("reminders")
                                .add(reminderData)
                                .addOnSuccessListener {
                                    Toast.makeText(this, "Recordatorio agregado con éxito", Toast.LENGTH_SHORT).show()
                                    updateReminderList()
                                }
                                .addOnFailureListener { e ->
                                    Toast.makeText(this, "Error al agregar recordatorio: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                        } else {
                            Toast.makeText(this, "Fecha y hora inválidas. Usa el formato dd/MM/yyyy HH:mm.", Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: ParseException) {
                        Toast.makeText(this, "Fecha y hora inválidas. Usa el formato dd/MM/yyyy HH:mm.", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "Por favor, completa todos los campos", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }


    private fun showEditReminderDialog() {
        if (reminderList.isEmpty()) {
            Toast.makeText(this, "No reminders to edit", Toast.LENGTH_SHORT).show()
            return
        }

        val reminderTitles = reminderList.map { it.title }.toTypedArray()
        var selectedReminderIndex = 0

        AlertDialog.Builder(this)
            .setTitle("Select Reminder to Edit")
            .setSingleChoiceItems(reminderTitles, 0) { _, which ->
                selectedReminderIndex = which
            }
            .setPositiveButton("Next") { _, _ ->
                val selectedReminder = reminderList[selectedReminderIndex]
                val dialogView = layoutInflater.inflate(R.layout.dialog_add_reminder, null)
                val titleInput = dialogView.findViewById<EditText>(R.id.reminderTitleInput)
                val descriptionInput = dialogView.findViewById<EditText>(R.id.reminderDescriptionInput)
                val dateInput = dialogView.findViewById<EditText>(R.id.reminderDateInput)
                val timeInput = dialogView.findViewById<EditText>(R.id.reminderTimeInput)

                titleInput.setText(selectedReminder.title)
                descriptionInput.setText(selectedReminder.description)

                db.collection("reminders")
                    .whereEqualTo("title", selectedReminder.title)
                    .whereEqualTo("userId", FirebaseAuth.getInstance().currentUser?.uid)
                    .get()
                    .addOnSuccessListener { result ->
                        if (result.isEmpty) {
                            Toast.makeText(this, "No reminder found to edit", Toast.LENGTH_SHORT).show()
                            return@addOnSuccessListener
                        }
                        val document = result.documents[0]
                        val dateTimestamp = document.getTimestamp("date")
                        val dateString = if (dateTimestamp != null) {
                            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                            sdf.format(dateTimestamp.toDate())
                        } else {
                            document.getString("date") ?: ""
                        }
                        val timeString = if (dateTimestamp != null) {
                            val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
                            sdf.format(dateTimestamp.toDate())
                        } else {
                            document.getString("time") ?: ""
                        }
                        dateInput.setText(dateString)
                        timeInput.setText(timeString)
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Error fetching reminder data: ${e.message}", Toast.LENGTH_SHORT).show()
                    }

                AlertDialog.Builder(this)
                    .setTitle("Edit Reminder")
                    .setView(dialogView)
                    .setPositiveButton("Save") { _, _ ->
                        val title = titleInput.text.toString().trim()
                        val description = descriptionInput.text.toString().trim()
                        val newDate = dateInput.text.toString().trim()
                        val newTime = timeInput.text.toString().trim()

                        if (title.isNotEmpty() && description.isNotEmpty() && newDate.isNotEmpty() && newTime.isNotEmpty()) {
                            db.collection("reminders")
                                .whereEqualTo("title", selectedReminder.title)
                                .whereEqualTo("userId", FirebaseAuth.getInstance().currentUser?.uid)
                                .get()
                                .addOnSuccessListener { result ->
                                    for (document in result) {
                                        val dateTimeString = "$newDate $newTime"
                                        val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                                        val updatedDate = dateFormat.parse(dateTimeString)

                                        db.collection("reminders")
                                            .document(document.id)
                                            .update(
                                                mapOf(
                                                    "title" to title,
                                                    "description" to description,
                                                    "date" to Timestamp(updatedDate)
                                                )
                                            )
                                            .addOnSuccessListener {
                                                Toast.makeText(this, "Reminder updated successfully", Toast.LENGTH_SHORT).show()
                                                updateReminderList()
                                            }
                                            .addOnFailureListener { e ->
                                                Toast.makeText(this, "Error updating reminder: ${e.message}", Toast.LENGTH_SHORT).show()
                                            }
                                    }
                                }
                                .addOnFailureListener { e ->
                                    Toast.makeText(this, "Error finding reminder to edit: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                        } else {
                            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                        }
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }





    private fun showDeleteReminderDialog() {
        if (reminderList.isEmpty()) {
            Toast.makeText(this, "No reminders to delete", Toast.LENGTH_SHORT).show()
            return
        }

        val reminderTitles = reminderList.map { it.title }.toTypedArray()
        var selectedReminderIndex = 0

        AlertDialog.Builder(this)
            .setTitle("Select Reminder to Delete")
            .setSingleChoiceItems(reminderTitles, 0) { _, which ->
                selectedReminderIndex = which
            }
            .setPositiveButton("Delete") { _, _ ->
                val selectedReminder = reminderList[selectedReminderIndex]
                db.collection("reminders")
                    .whereEqualTo("title", selectedReminder.title)
                    .whereEqualTo("userId", FirebaseAuth.getInstance().currentUser?.uid)
                    .get()
                    .addOnSuccessListener { result ->
                        for (document in result) {
                            db.collection("reminders")
                                .document(document.id)
                                .delete()
                                .addOnSuccessListener {
                                    Toast.makeText(
                                        this,
                                        "Reminder deleted successfully",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    updateReminderList()
                                }
                                .addOnFailureListener { e ->
                                    Toast.makeText(
                                        this,
                                        "Error deleting reminder: ${e.message}",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                        }
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(
                            this,
                            "Error finding reminder to delete: ${e.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    data class Pet(var name: String, var breed: String, var age: Int, var type: String, var sex: String)
    data class Reminder(var title: String, var description: String, var date: String)
}