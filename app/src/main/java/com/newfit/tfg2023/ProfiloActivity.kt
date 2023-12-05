package com.newfit.tfg2023

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.io.File
import java.io.FileOutputStream

// Declaración de variables
private lateinit var profileImage: ImageView
lateinit var userName: TextView
private lateinit var editNameButton: Button
private lateinit var userEmail: TextView
private lateinit var viewClassesButton: Button
private lateinit var deleteAccountButton: Button
private lateinit var restoreDefaultButton: Button
private val GALLERY_REQUEST_CODE = 1001

class ProfiloActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profilo)

        //Cargamos la imagen de perfil al inicio de la actividad
        loadProfileImage()
        // Inicialización de las vistas
        profileImage = findViewById(R.id.profileImage)
        userName = findViewById(R.id.userName)
        editNameButton = findViewById(R.id.editNameButton)
        userEmail = findViewById(R.id.userEmail)
        viewClassesButton = findViewById(R.id.viewClassesButton)
        deleteAccountButton = findViewById(R.id.deleteAccountButton)
        restoreDefaultButton = findViewById(R.id.restoreDefaultButton)

        // Obtener el ID del usuario actual
        val userId = FirebaseAuth.getInstance().currentUser?.uid

        if (userId != null) {
            // Acceder a la base de datos para obtener el nombre y el email del usuario
            val databaseReference = FirebaseDatabase.getInstance().getReference("Clienti").child(userId)
            databaseReference.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    // Obtener el nombre y el email del usuario desde la base de datos
                    val nombre = dataSnapshot.child("nome").value.toString()
                    val email = dataSnapshot.child("email").value.toString()

                    // Establecer el nombre y el email en los TextView
                    userName.text = nombre
                    userEmail.text = email
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    // Manejar errores si es necesario
                }
            })
        }

        // Configurar el botón para editar el nombre
        editNameButton.setOnClickListener {
            showEditNameDialog()
        }

        // Configurar el botón para restaurar la imagen predeterminada
        restoreDefaultButton.setOnClickListener {
            restoreDefaultImage()
        }

        // Configurar el clic en la imagen de perfil para seleccionar una imagen de la galería
        profileImage.setOnClickListener {
            val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(galleryIntent, GALLERY_REQUEST_CODE)
        }

        // Configurar el botón para eliminar la cuenta
        deleteAccountButton.setOnClickListener {
            showDeleteAccountConfirmationDialog()
        }

        // Configurar el botón para ver las clases
        viewClassesButton.setOnClickListener {
            // Obtener el ID del usuario actual
            val userId = FirebaseAuth.getInstance().currentUser?.uid

            if (userId != null) {
                val databaseReference = FirebaseDatabase.getInstance().getReference("Inscripciones").child(userId)

                databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        val classList = mutableListOf<String>()

                        for (classSnapshot in dataSnapshot.children) {
                            val fechaInicio = classSnapshot.child("fechaInicio").getValue(String::class.java)
                            val nome = classSnapshot.child("nome").getValue(String::class.java)
                            val prezzo = classSnapshot.child("prezzo").getValue(String::class.java)

                            if (fechaInicio != null && nome != null && prezzo != null) {
                                classList.add("Data: $fechaInicio\nNome: $nome\nPrezzo: $prezzo")
                            }
                        }

                        // Verifica si hay datos antes de crear el diálogo
                        if (classList.isNotEmpty()) {
                            val adapter = ArrayAdapter(this@ProfiloActivity, android.R.layout.simple_list_item_1, classList)
                            val listView = ListView(this@ProfiloActivity)
                            listView.adapter = adapter

                            val builder = AlertDialog.Builder(this@ProfiloActivity)
                            builder.setTitle("Abbonamenti")
                            builder.setView(listView)
                            builder.show()
                        } else {
                            // Muestra un mensaje o toma alguna acción si no hay datos
                            Toast.makeText(this@ProfiloActivity, "No hay datos disponibles", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onCancelled(databaseError: DatabaseError) {
                        // Manejo de errores si es necesario
                        Log.e("Firebase", "Error en la consulta a Firebase: ${databaseError.message}")
                    }
                })
            }
        }
    }

    // Función para cargar la imagen de perfil desde Firebase o localmente
    private fun loadProfileImage() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            val databaseReference = FirebaseDatabase.getInstance().reference
            databaseReference.child("Clienti").child(userId).child("imagineProfilo")
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        // Si el usuario tiene una imagen de perfil, cárgala desde Firebase
                        val profileImageUri = dataSnapshot.getValue(String::class.java)

                        if (profileImageUri != null) {
                            // Carga la imagen de perfil en el ImageView

                            // Si no tiene una imagen de perfil en la base de datos, intenta cargarla desde almacenamiento local
                            val localProfileImage = loadLocalProfileImage()
                            if (localProfileImage != null) {
                                // Carga la imagen de perfil almacenada localmente
                                profileImage.setImageBitmap(localProfileImage)
                            } else {
                                // Si no tiene una imagen de perfil, muestra la imagen predeterminada
                                profileImage.setImageResource(R.drawable.perfil)
                            }
                        }
                    }

                    override fun onCancelled(databaseError: DatabaseError) {
                        // Manejo de errores si es necesario
                    }
                })
        } else {
            // Manejo de errores si el usuario no ha iniciado sesión
        }
    }

    // Función para manejar el resultado de seleccionar una imagen de la galería
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == GALLERY_REQUEST_CODE && resultCode == Activity.RESULT_OK && data != null) {
            val selectedImageUri = data.data

            val databaseReference = FirebaseDatabase.getInstance().reference

            // Obtiene el ID del usuario actual (supongamos que lo tienes)
            val userId = FirebaseAuth.getInstance().currentUser?.uid

            // Verifica que el usuario esté autenticado y tiene un ID válido
            if (userId != null) {
                // Guarda la URI de la imagen en la base de datos
                databaseReference.child("Clienti").child(userId).child("imagineProfilo").setValue(selectedImageUri.toString())

                profileImage.setImageURI(selectedImageUri)
                if (selectedImageUri != null) {
                    saveImageLocally(selectedImageUri)
                }
            }
        }
    }

    // Función para guardar la imagen seleccionada localmente
    private fun saveImageLocally(imageUri: Uri) {
        val fileName = "profile_image.jpg"
        val inputStream = contentResolver.openInputStream(imageUri)
        val outputStream = FileOutputStream(File(filesDir, fileName))
        inputStream?.use {
            val bitmap = BitmapFactory.decodeStream(it)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
        }
        outputStream.close()
    }

    // Función para restaurar la imagen predeterminada
    private fun restoreDefaultImage() {
        // Carga la imagen predeterminada en el ImageView
        profileImage.setImageResource(R.drawable.perfil)

        // Elimina la referencia a la imagen personalizada en la base de datos
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            val databaseReference = FirebaseDatabase.getInstance().reference
            databaseReference.child("Clienti").child(userId).child("imagineProfilo").removeValue()
        }
    }

    // Función para mostrar el diálogo de edición del nombre
    private fun showEditNameDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Editar Nombre")

        val input = EditText(this)
        input.setText(userName.text)
        builder.setView(input)

        builder.setPositiveButton("Guardar") { _, _ ->
            val newName = input.text.toString().trim()
            if (newName.isNotEmpty()) {
                // Actualiza el nombre en la base de datos
                updateName(newName)
            }
        }

        builder.setNegativeButton("Cancelar") { dialog, _ ->
            dialog.cancel()
        }

        val dialog = builder.create()
        dialog.show()
    }

    // Función para actualizar el nombre en la base de datos
    private fun updateName(newName: String) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid

        if (userId != null) {
            val databaseReference = FirebaseDatabase.getInstance().getReference("Clienti").child(userId)
            databaseReference.child("redtnome").setValue(newName).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    userName.text = newName // Actualiza el TextView con el nuevo nombre
                    Toast.makeText(this, "Aggiornato Correttamente", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Errore", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // Función para cargar la imagen de perfil almacenada localmente
    private fun loadLocalProfileImage(): Bitmap? {
        val fileName = "profile_image.jpg"
        val file = File(filesDir, fileName)
        return if (file.exists()) {
            BitmapFactory.decodeFile(file.absolutePath)
        } else {
            null
        }
    }

    // Función para mostrar el diálogo de confirmación de eliminación de cuenta
    private fun showDeleteAccountConfirmationDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Cancellare Account")
        builder.setMessage("Sei sicuro di voler eliminare il tuo account? Tutti i dati correlati saranno persi in modo permanente.")

        builder.setPositiveButton("Sí") { _, _ ->
            // El usuario ha confirmado que desea eliminar la cuenta
            deleteAccount()
        }

        builder.setNegativeButton("No") { dialog, _ ->
            dialog.cancel()
        }

        val dialog = builder.create()
        dialog.show()
    }

    // Función para eliminar la cuenta y los datos relacionados
    private fun deleteAccount() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid

        if (userId != null) {
            // Eliminar los datos relacionados con la cuenta del usuario en la base de datos
            val databaseReference = FirebaseDatabase.getInstance().getReference("Clienti").child(userId)
            databaseReference.removeValue()

            // Eliminar los registros de clases del usuario (ajusta la referencia según tu estructura de datos)
            val classReference = FirebaseDatabase.getInstance().getReference("Iscrizioni").child(userId)
            classReference.removeValue()

            // Eliminar la imagen de perfil almacenada localmente
            val fileName = "profile_image.jpg"
            val file = File(filesDir, fileName)
            if (file.exists()) {
                file.delete()
            }

            // Eliminar la cuenta del usuario en Firebase Authentication
            FirebaseAuth.getInstance().currentUser?.delete()
                ?.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        // La cuenta ha sido eliminada exitosamente, regresar a la pantalla de inicio de sesión, por ejemplo.
                        // Aquí puedes agregar la lógica para redirigir al usuario a la pantalla de inicio de sesión.
                        val intent = Intent(this, MainActivity::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                        startActivity(intent)
                        finish()
                    } else {
                        // Error al eliminar la cuenta, maneja la situación según tus necesidades.
                    }
                }
        }
    }
}
