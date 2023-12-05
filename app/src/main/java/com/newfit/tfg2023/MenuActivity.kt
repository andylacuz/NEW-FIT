package com.newfit.tfg2023

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.*

class MenuActivity : AppCompatActivity() {
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu)

        // Verificar y actualizar suscripciones al iniciar la actividad
        verificarYActualizarSuscripciones()

        // Inicializar la instancia de FirebaseAuth
        firebaseAuth = Firebase.auth

        // Inicializar botones
        val mlezioni : Button = findViewById(R.id.mlezioni)
        val mabbon : Button = findViewById(R.id.mabbon)
        val mprofilo : Button = findViewById(R.id.mprofilo)
        val mexit : Button = findViewById(R.id.mexit)

        // Configurar acciones de clic para los botones
        mlezioni.setOnClickListener {
            // Iniciar la actividad de lecciones en caso de tener una suscripción activa
            val userId = FirebaseAuth.getInstance().currentUser?.uid

            if (userId != null) {
                val databaseReference = FirebaseDatabase.getInstance().getReference("Suscripciones").child(userId)

                databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        if (dataSnapshot.exists()) {
                            // El nodo "Suscripciones" contiene al menos un valor
                            val intent = Intent(this@MenuActivity, LezioniActivity::class.java)
                            startActivity(intent)
                        } else {
                            // El nodo "Suscripciones" no contiene ningún valor
                            val intent = Intent(this@MenuActivity, AbbonamentiActivity::class.java)
                            startActivity(intent)

                            Toast.makeText(baseContext, "Non hai nessun abbonamento attivo", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onCancelled(databaseError: DatabaseError) {
                        // Manejar errores si es necesario
                    }
                })
            }
        }

        mabbon.setOnClickListener {
            // Iniciar la actividad de abonos
            val a = Intent(this, AbbonamentiActivity::class.java)
            startActivity(a)
        }

        mprofilo.setOnClickListener {
            // Iniciar la actividad de perfil
            val b = Intent(this, ProfiloActivity::class.java)
            startActivity(b)
        }

        mexit.setOnClickListener {
            // Cerrar sesión y volver a la actividad principal
            signOut()
        }
    }

    // Función para cerrar sesión
    private fun signOut() {
        firebaseAuth.signOut()
        val b = Intent(this, MainActivity::class.java)
        startActivity(b)
        finish()
    }

    // Función para verificar y actualizar suscripciones expiradas
    private fun verificarYActualizarSuscripciones() {
        // Obtener el ID de usuario actual
        val userId = FirebaseAuth.getInstance().currentUser?.uid

        // Verificar si el ID de usuario no es nulo
        if (userId != null) {
            // Referencia a la ubicación de las inscripciones del usuario en la base de datos
            val inscripcionesRef = FirebaseDatabase.getInstance().getReference("Inscripciones").child(userId)

            // Agregar un listener para obtener datos una vez en la ubicación de inscripciones
            inscripcionesRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    // Iterar a través de las inscripciones del usuario
                    for (snapshot in dataSnapshot.children) {
                        // Obtener la fecha de finalización de la inscripción
                        val fechaFin = snapshot.child("fechaFin").getValue(String::class.java)

                        // Verificar si la fecha de finalización no es nula y ha expirado
                        if (fechaFin != null && haExpirado(fechaFin)) {
                            // Referencia a la ubicación de suscripciones y fecha de finalización específica
                            val suscripcionesRef = FirebaseDatabase.getInstance().getReference("Suscripciones").child(userId)
                            val fechaF = FirebaseDatabase.getInstance().getReference("Inscripciones").child(userId).child(snapshot.key!!).child("fechaFin")

                            // Eliminar la suscripción y la fecha de finalización en caso de expiración
                            suscripcionesRef.ref.removeValue()
                            fechaF.ref.removeValue()
                        }
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    // Manejar el error de manera adecuada
                }
            })
        }
    }

    // Función para verificar si una fecha ha expirado
    private fun haExpirado(fechaFin: String): Boolean {
        // Formato de fecha
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

        // Parsear la fecha de finalización y la fecha actual
        val fechaFinDate = dateFormat.parse(fechaFin)
        val fechaActual = obtenerFechaActual()
        val fechaActualDate = dateFormat.parse(fechaActual)

        // Verificar si la fecha actual es posterior a la fecha de finalización
        return fechaActualDate!!.after(fechaFinDate)
    }

    // Función para obtener la fecha actual en formato de cadena
    private fun obtenerFechaActual(): String {
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        return dateFormat.format(Date())
    }
}
