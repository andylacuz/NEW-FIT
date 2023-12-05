package com.newfit.tfg2023

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase

class RegisterActivity : AppCompatActivity() {

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var dbRef: DatabaseReference

    private lateinit var redtnome: TextView
    private lateinit var redtemail: TextView
    private lateinit var redtpassword: TextView
    private lateinit var redtpassword1: TextView
    private lateinit var bregistrati: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        // Inicialización de vistas
        redtnome = findViewById(R.id.redtnome)
        redtemail = findViewById(R.id.redtemail)
        redtpassword = findViewById(R.id.redtpassword)
        redtpassword1 = findViewById(R.id.redtpassword1)
        bregistrati = findViewById(R.id.bregistrati)

        // Configuración del listener para el botón de registro
        bregistrati.setOnClickListener {

            // Obtener valores de los campos
            val pass = redtpassword.text.toString()
            val pass1 = redtpassword1.text.toString()

            // Validar que las contraseñas coincidan
            if (pass == pass1) {
                // Validar que los campos no estén vacíos
                if (redtnome.text.isNotEmpty() && redtemail.text.isNotEmpty() && pass.isNotEmpty()) {
                    // Realizar el proceso de registro
                    registrati(redtemail.text.toString(), pass, redtnome.text.toString())
                } else {
                    Toast.makeText(baseContext, "ERRORE: INFORMAZIONI MANCANTI", Toast.LENGTH_SHORT).show()
                }
            } else {
                // Mostrar mensaje si las contraseñas no coinciden
                Toast.makeText(baseContext, "ERROR: LE PASSWORD NON SONO UGUALI", Toast.LENGTH_SHORT).show()
                redtpassword.requestFocus()
            }
        }

        // Inicialización de Firebase
        dbRef = FirebaseDatabase.getInstance().getReference("Clienti")
        firebaseAuth = Firebase.auth

        // Configuración del enlace para ir al inicio de sesión
        val tvGoLogin = findViewById<TextView>(R.id.tv_go_to_login)
        tvGoLogin.setOnClickListener {
            goToLogin()
        }
    }

    // Método para ir a la pantalla de inicio de sesión
    private fun goToLogin() {
        val i = Intent(this, MainActivity::class.java)
        startActivity(i)
        finish()
    }

    // Método para realizar el registro en Firebase
    private fun registrati(email: String, password: String, nome: String) {
        firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {
                // Obtener el ID del usuario registrado
                val user = firebaseAuth.currentUser
                val clientId = user?.uid

                if (clientId != null) {
                    // Crear un objeto cliente y almacenarlo en la base de datos
                    val clienti = DatiClienti(clientId, nome, email)
                    dbRef.child(clientId).setValue(clienti)
                }

                // Enviar correo de verificación y mostrar mensaje
                verificazione()
                Toast.makeText(baseContext, "Account creato correttamente, Riceverá un e-mail di verificazione", Toast.LENGTH_SHORT).show()

                // Ir a la pantalla de inicio después del registro exitoso
                val i = Intent(this, MainActivity::class.java)
                startActivity(i)
                finish()
            } else {
                // Mostrar mensaje de error en caso de fallo en el registro
                Toast.makeText(baseContext, "ERRORE", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Método para enviar correo de verificación
    private fun verificazione() {
        val user = firebaseAuth.currentUser!!
        user.sendEmailVerification().addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {
                Toast.makeText(baseContext, "E-mail di verifica inviata", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(baseContext, "ERRORE nell'invio dell'e-mail di verifica", Toast.LENGTH_SHORT).show()
            }
        }
    }
}