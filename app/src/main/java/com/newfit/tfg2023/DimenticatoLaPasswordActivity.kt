package com.newfit.tfg2023

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class DimenticatoLaPasswordActivity : AppCompatActivity() {

    // Declaración de la instancia de FirebaseAuth para la autenticación
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dimenticato_la_password)

        // Obtención de referencias a los elementos de la interfaz de usuario
        val veremail: TextView = findViewById(R.id.veremail)
        val dimpassword: Button = findViewById(R.id.dimpassword)

        // Configuración del listener para el botón de cambio de contraseña
        dimpassword.setOnClickListener {
            cambiarPassword(veremail.text.toString())
        }

        // Inicialización de la instancia de FirebaseAuth
        firebaseAuth = Firebase.auth
    }

    // Función para cambiar la contraseña asociada a una dirección de correo electrónico
    private fun cambiarPassword(email: String) {
        // Enviar una solicitud para restablecer la contraseña asociada al correo electrónico proporcionado
        firebaseAuth.sendPasswordResetEmail(email).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                // Éxito al enviar el correo electrónico de restablecimiento de contraseña
                Toast.makeText(baseContext, "INVIATO CORRETTAMENTE", Toast.LENGTH_SHORT).show()

                // Redirigir a la actividad principal (página de inicio de sesión)
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)

                // Finalizar la actividad actual
                finish()
            } else {
                // Error al enviar el correo electrónico de restablecimiento de contraseña
                Toast.makeText(baseContext, "ERRORE", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
