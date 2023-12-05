package com.newfit.tfg2023

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class MainActivity : AppCompatActivity() {

    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        // Agregamos un retraso de 2000 milisegundos (2 segundos) para mostrar el splash screen
        Thread.sleep(2000)
        // Establecemos el tema de la aplicación
        setTheme(R.style.Theme_TFG2023)
        super.onCreate(savedInstanceState)
        // Establecemos el diseño de la actividad principal
        setContentView(R.layout.activity_main)

        // Inicializamos las referencias a los elementos de la interfaz de usuario
        val baccedi: Button = findViewById(R.id.baccedi)
        val edtemail: TextView = findViewById(R.id.edtemail)
        val edtpassword: TextView = findViewById(R.id.edtpassword)
        val dim: TextView = findViewById(R.id.dim)
        // Inicializamos la instancia de FirebaseAuth
        firebaseAuth = Firebase.auth

        // Borramos el texto del campo de contraseña al iniciar la actividad
        edtpassword.text = null

        // Configuramos el evento click para el botón de acceso
        baccedi.setOnClickListener()
        {
            // Llamamos a la función signIn con el correo electrónico y la contraseña proporcionados
            signIn(edtemail.text.toString(), edtpassword.text.toString())
        }

        // Configuramos el evento click para el enlace de olvido de contraseña
        dim.setOnClickListener()
        {
            val i = Intent(this, DimenticatoLaPasswordActivity::class.java)
            startActivity(i)
        }

        // Configuramos el evento click para el enlace de registro
        val tvGoRegister = findViewById<TextView>(R.id.tv_go_to_register)
        tvGoRegister.setOnClickListener{
            goToRegister()
        }
    }

    // Función para navegar a la actividad de registro
    private fun goToRegister() {
        val i = Intent(this, RegisterActivity::class.java)
        startActivity(i)
    }

    // Función para realizar el inicio de sesión
    private fun signIn(email: String, password: String) {
        // Verificamos si el correo electrónico o la contraseña están en blanco
        if (email.isBlank() || password.isBlank()) {
            // Mostramos un mensaje de error y salimos de la función
            Toast.makeText(baseContext, "Inserisci email e password", Toast.LENGTH_SHORT).show()
            return
        }

        // Intentamos iniciar sesión con el correo electrónico y la contraseña proporcionados
        firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {
                // Si la autenticación es exitosa, obtenemos el usuario actual
                val user = firebaseAuth.currentUser
                // Verificamos si el correo electrónico está verificado
                val verifica = user?.isEmailVerified
                if (verifica == true) {
                    // Mostramos un mensaje de éxito y navegamos al menú principal
                    Toast.makeText(baseContext, "Autenticazione confermata", Toast.LENGTH_SHORT).show()
                    val a = Intent(this, MenuActivity::class.java)
                    startActivity(a)
                    finish()
                }
            } else {
                // Si hay un error en la autenticación, mostramos un mensaje de error
                Toast.makeText(baseContext, "Errore d'accesso", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
