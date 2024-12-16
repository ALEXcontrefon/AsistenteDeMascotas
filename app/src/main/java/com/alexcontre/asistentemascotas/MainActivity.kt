package com.alexcontre.asistentemascotas

import android.app.DatePickerDialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.auth.FirebaseAuth
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlinx.coroutines.*

class MainActivity : AppCompatActivity() {

    private lateinit var petListContainer: LinearLayout
    private lateinit var reminderListContainer: LinearLayout
    private val petList = mutableListOf<Pet>()
    private val reminderList = mutableListOf<Reminder>()

    private lateinit var petRecyclerView: RecyclerView
    private lateinit var reminderRecyclerView: RecyclerView

    val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        petListContainer = findViewById(R.id.petListContainer)
        reminderListContainer = findViewById(R.id.reminderListContainer)

        petRecyclerView = findViewById(R.id.petRecyclerView)
        reminderRecyclerView = findViewById(R.id.reminderRecyclerView)

        petRecyclerView.layoutManager = LinearLayoutManager(this)
        reminderRecyclerView.layoutManager = LinearLayoutManager(this)

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
        createNotificationChannel()
        updatePetList()
        updateReminderList()
        createNotificationChannel()
    }

    private fun updatePetList() {
        val progressBar: ProgressBar = findViewById(R.id.loadingProgressBar)
        progressBar.visibility = View.VISIBLE
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        petList.clear()
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
                    petRecyclerView.adapter = MyAdapter(petList)
                    progressBar.visibility = View.GONE

                }.addOnFailureListener { e ->
                    Toast.makeText(this, "Error al cargar las mascotas: ${e.message}", Toast.LENGTH_SHORT).show()
                    progressBar.visibility = View.GONE
                }
        } else {
            Toast.makeText(this, "Ningún usuario ha iniciado sesión", Toast.LENGTH_SHORT).show()
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
            Toast.makeText(this, "No hay mascotas para editar", Toast.LENGTH_SHORT).show()
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
                                                Toast.makeText(this, "La mascota se actualizó correctamente", Toast.LENGTH_SHORT).show()
                                                updatePetList()
                                            }
                                            .addOnFailureListener { e ->
                                                Toast.makeText(this, "No se pudo actualizar la mascota: ${e.message}", Toast.LENGTH_SHORT).show()
                                            }
                                    }
                                }
                        } else {
                            Toast.makeText(this, "Por favor, rellene todos los campos", Toast.LENGTH_SHORT).show()
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
            Toast.makeText(this, "No hay mascotas para eliminar", Toast.LENGTH_SHORT).show()
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
                                    Toast.makeText(this, "Mascota eliminada correctamente", Toast.LENGTH_SHORT).show()
                                    updatePetList()
                                }
                                .addOnFailureListener { e ->
                                    Toast.makeText(this, "No se pudo eliminar la mascota: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                        }
                    }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun updateReminderList() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        reminderList.clear()

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

                        val petId = document.getString("petId") ?: "No pet"
                        val petName = "Loading..."

                        val reminder = Reminder(
                            title = document.getString("title") ?: "No Title",
                            description = document.getString("description") ?: "No Description",
                            date = formattedDate,
                            petId = petId
                        )

                        db.collection("pets")
                            .document(petId)
                            .get()
                            .addOnSuccessListener { petDoc ->
                                reminder.petName = petDoc.getString("name") ?: "Unknown Pet"
                                reminderRecyclerView.adapter?.notifyDataSetChanged()
                            }
                        reminderList.add(reminder)
                    }
                    reminderRecyclerView.adapter = MyAdapter(reminderList)
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error al cargar los recordatorios: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(this, "Ningun usuario ha iniciado sesión", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showAddReminderDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_reminder, null)
        val titleInput = dialogView.findViewById<EditText>(R.id.reminderTitleInput)
        val descriptionInput = dialogView.findViewById<EditText>(R.id.reminderDescriptionInput)
        val dateInput = dialogView.findViewById<EditText>(R.id.reminderDateInput)
        val timeInput = dialogView.findViewById<EditText>(R.id.reminderTimeInput)
        val petSpinner = dialogView.findViewById<Spinner>(R.id.petSpinner)
        val petNames = mutableListOf<String>()
        val petIds = mutableListOf<String>()

        db.collection("pets")
            .whereEqualTo("userId", FirebaseAuth.getInstance().currentUser?.uid)
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    val petName = document.getString("name") ?: "Unknown Pet"
                    val petId = document.id
                    petNames.add(petName)
                    petIds.add(petId)
                }

                val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, petNames)
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                petSpinner.adapter = adapter
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error al cargar las mascotas: ${e.message}", Toast.LENGTH_SHORT).show()
            }

        val dialog = AlertDialog.Builder(this)
            .setTitle("Add Reminder")
            .setView(dialogView)
            .setPositiveButton("Add", null)
            .setNegativeButton("Cancel", null)
            .create()

        dialog.setOnShowListener {
            val addButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            addButton.setOnClickListener {
                val title = titleInput.text.toString().trim()
                val description = descriptionInput.text.toString().trim()
                val dateInputText = dateInput.text.toString().trim()
                val timeInputText = timeInput.text.toString().trim()
                val selectedPetPosition = petSpinner.selectedItemPosition
                val selectedPetId = petIds.getOrNull(selectedPetPosition)

                if (title.isNotEmpty() && description.isNotEmpty() && dateInputText.isNotEmpty() && timeInputText.isNotEmpty()) {
                    val userEmail = FirebaseAuth.getInstance().currentUser?.email
                    val userId = FirebaseAuth.getInstance().currentUser?.uid

                    val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                    dateFormat.isLenient = false

                    try {
                        val reminderDateTime = dateFormat.parse("$dateInputText $timeInputText")
                        if (reminderDateTime != null) {
                            if (reminderDateTime.time < System.currentTimeMillis()) {
                                Toast.makeText(this, "No se pudo guardar el recordatorio. La fecha y la hora ya pasaron.", Toast.LENGTH_SHORT).show()
                                return@setOnClickListener
                            }

                            val reminderData = hashMapOf(
                                "title" to title,
                                "description" to description,
                                "date" to Timestamp(reminderDateTime),
                                "userId" to userId,
                                "userEmail" to userEmail,
                                "petId" to selectedPetId
                            )

                            db.collection("reminders")
                                .add(reminderData)
                                .addOnSuccessListener { documentReference ->
                                    Toast.makeText(this, "Recordatorio agregado exitosamente", Toast.LENGTH_SHORT).show()

                                    updateReminderList()
                                    sendNotification(title, description, reminderDateTime.time)

                                    dialog.dismiss()
                                }
                                .addOnFailureListener { e ->
                                    Toast.makeText(this, "Error al agregar recordatorio: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                        } else {
                            Toast.makeText(this, "Fecha y hora no válidas. Utilice el formato dd/MM/yyyy HH:mm.", Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: ParseException) {
                        Toast.makeText(this, "Fecha y hora no válidas. Por favor use el formato dd/MM/yyyy HH:mm.", Toast.LENGTH_SHORT).show()
                        e.printStackTrace()
                    }
                } else {
                    Toast.makeText(this, "Por favor complete todos los campos", Toast.LENGTH_SHORT).show()
                }
            }
        }

        dialog.show()
    }

    private fun showEditReminderDialog() {
        if (reminderList.isEmpty()) {
            Toast.makeText(this, "No hay recordatorios para editar", Toast.LENGTH_SHORT).show()
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
                val petSpinner = dialogView.findViewById<Spinner>(R.id.petSpinner)

                titleInput.setText(selectedReminder.title)
                descriptionInput.setText(selectedReminder.description)

                val petAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, petList.map { it.name })
                petAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                petSpinner.adapter = petAdapter

                db.collection("reminders")
                    .whereEqualTo("title", selectedReminder.title)
                    .whereEqualTo("userId", FirebaseAuth.getInstance().currentUser?.uid)
                    .get()
                    .addOnSuccessListener { result ->
                        if (result.isEmpty) {
                            Toast.makeText(this, "No se encontró ningún recordatorio para editar", Toast.LENGTH_SHORT).show()
                            return@addOnSuccessListener
                        }
                        val document = result.documents[0]
                        val dateTimestamp = document.getTimestamp("date")
                        val dateString = dateTimestamp?.toDate()?.let {
                            SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(it)
                        } ?: ""
                        val timeString = dateTimestamp?.toDate()?.let {
                            SimpleDateFormat("HH:mm", Locale.getDefault()).format(it)
                        } ?: ""
                        dateInput.setText(dateString)
                        timeInput.setText(timeString)
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Error al recuperar los datos del recordatorio: ${e.message}", Toast.LENGTH_SHORT).show()
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
                            val dateTimeString = "$newDate $newTime"
                            val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                            val updatedDate = dateFormat.parse(dateTimeString)

                            if (updatedDate != null && updatedDate.time < System.currentTimeMillis()) {
                                Toast.makeText(this, "No se pudo guardar el recordatorio. La fecha y la hora ya pasaron.", Toast.LENGTH_SHORT).show()
                                return@setPositiveButton
                            }

                            db.collection("reminders")
                                .whereEqualTo("title", selectedReminder.title)
                                .whereEqualTo("userId", FirebaseAuth.getInstance().currentUser?.uid)
                                .get()
                                .addOnSuccessListener { result ->
                                    for (document in result) {
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
                                                Toast.makeText(this, "Recordatorio actualizado exitosamente", Toast.LENGTH_SHORT).show()

                                                val reminderId = document.id
                                                cancelNotification(reminderId.hashCode())
                                                if (updatedDate != null && updatedDate.time > System.currentTimeMillis()) {
                                                    sendNotification(title, description, updatedDate.time)
                                                } else {
                                                    Toast.makeText(this, "La hora de notificación debe ser en el futuro.", Toast.LENGTH_SHORT).show()
                                                }

                                                updateReminderList()
                                            }
                                            .addOnFailureListener { e ->
                                                Toast.makeText(this, "Error al cargar recordatorio: ${e.message}", Toast.LENGTH_SHORT).show()
                                            }
                                    }
                                }
                                .addOnFailureListener { e ->
                                    Toast.makeText(this, "Error al encontrar el recordatorio para editar: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                        } else {
                            Toast.makeText(this, "Por favor complete todos los campos", Toast.LENGTH_SHORT).show()
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
            Toast.makeText(this, "No hay recordatorios para eliminar", Toast.LENGTH_SHORT).show()
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
                                        "Recordatorio eliminado exitosamente",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    updateReminderList()
                                }
                                .addOnFailureListener { e ->
                                    Toast.makeText(
                                        this,
                                        "Error al eliminar el recordatorio: ${e.message}",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                        }
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(
                            this,
                            "Error al encontrar el recordatorio para eliminar: ${e.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = "reminder_channel"
            val channelName = "Reminder Notifications"
            val channelDescription = "Notifications for pet care reminders"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(channelId, channelName, importance)
            channel.description = channelDescription

            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun sendNotification(title: String, content: String, notificationTimeInMillis: Long) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "reminder_channel"
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE)
        } else {
            PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT)
        }

        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.patita)
            .setContentTitle(title)
            .setContentText(content)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()
        if (notificationTimeInMillis > System.currentTimeMillis()) {
            val delayMillis = notificationTimeInMillis - System.currentTimeMillis()
            val handler = Handler(Looper.getMainLooper())
            handler.postDelayed({
                notificationManager.notify(1, notification)
            }, delayMillis)
        } else {
            Toast.makeText(this, "La fecha y hora deben ser futuras", Toast.LENGTH_SHORT).show()
        }
    }

    private fun cancelNotification(notificationId: Int) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(notificationId)
    }

    data class Pet(var name: String, var breed: String, var age: Int, var type: String, var sex: String)
    data class Reminder(var title: String, var description: String, var date: String, val petId: String? = null, var petName: String? = null)
}