package com.newfit.tfg2023

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*


class AbbonamentiActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_abbonamenti)

         // Referencias a los botones en el diseño
        val btnLezione = findViewById<Button>(R.id.btnLezione)
        val btnSettimanale = findViewById<Button>(R.id.btnSettimanale)
        val btnMensile = findViewById<Button>(R.id.btnMensile)
        val btnAnnuale = findViewById<Button>(R.id.btnAnnuale)

        // Asigna funciones a los clics de los botones
        btnLezione.setOnClickListener { mostrarDialogoLezione("LEZIONE", "Una lezione a sua scelta", "10€") }
        btnSettimanale.setOnClickListener { mostrarCalendarioSettimanale("SETTIMANALE", "Due lezioni a sua scelta entro una settimana reale(L-V)", "18€") }
        btnMensile.setOnClickListener { mostrarCalendarioMensile("MENSILE", "Un Mese completo dove potrá asistere a due lezioni per settimana entro il mese che é attivo l'abbonamento", "60€") }
        btnAnnuale.setOnClickListener { mostrarCalendarioAnnuale("ANNUALE", "Un anno dove potrá asistere a due lezioni a settimana", "600€") }

    }
    // Muestra el diálogo de abbonamento para el tipo "LEZIONE"
    private fun mostrarDialogoLezione(tipo: String, descripcion: String, precio: String) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_abbonamento, null)
        val tvAbbonamentoInfo = dialogView.findViewById<TextView>(R.id.tvAbbonamentoInfo)
        val btnPagar = dialogView.findViewById<Button>(R.id.btnPagar)
        // Configura la información del abbonamento en el diálogo
        tvAbbonamentoInfo.text = "Include: $descripcion\nPrecio: $precio"

        val builder = AlertDialog.Builder(this)
        builder.setView(dialogView)

        val dialog = builder.create()
        // Maneja el clic del botón de pago
        btnPagar.setOnClickListener {
            val userId = FirebaseAuth.getInstance().currentUser?.uid

            if (userId != null) {
                val suscripcionesRef = FirebaseDatabase.getInstance().getReference("Suscripciones").child(userId)

                suscripcionesRef.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {

                        if (dataSnapshot.exists()) {
                            // El usuario ya tiene un abbonamento, muestra un mensaje
                            Toast.makeText(baseContext, "Hai gía un Abbonamento", Toast.LENGTH_SHORT).show()

                        } else {
                            // Abre la actividad WebView para realizar el pago
                            abrirWebViewConEnlaceDePago(tipo,precio)
                        }
                    }

                    override fun onCancelled(databaseError: DatabaseError) {
                    // Manejo de errores, si es necesario
                    }
                })
            }
            // Cierra el diálogo después del clic
            dialog.dismiss()

        }
        // Muestra el diálogo
        dialog.show()

    }
    // Función similar a mostrarDialogoLezione para el tipo "SETTIMANALE"
    private fun mostrarCalendarioSettimanale(tipo: String, descripcion: String, precio: String) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_abbonamento, null)
        val tvAbbonamentoInfo = dialogView.findViewById<TextView>(R.id.tvAbbonamentoInfo)
        val btnPagar = dialogView.findViewById<Button>(R.id.btnPagar)

        tvAbbonamentoInfo.text = "Include: $descripcion\nPrezzo: $precio"

        val builder = AlertDialog.Builder(this)
        builder.setView(dialogView)

        val dialog = builder.create()
        dialog.show()

        btnPagar.setOnClickListener {
            val userId = FirebaseAuth.getInstance().currentUser?.uid

            if (userId != null) {
                val suscripcionesRef = FirebaseDatabase.getInstance().getReference("Suscripciones").child(userId)

                suscripcionesRef.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {

                        if (dataSnapshot.exists() ) {

                            Toast.makeText(baseContext, "Hai gía un Abbonamento", Toast.LENGTH_SHORT).show()


                        } else {

                            abrirWebViewConEnlaceDePago(tipo,precio)
                        }
                    }

                    override fun onCancelled(databaseError: DatabaseError) {

                    }
                })
            }

            dialog.dismiss()

        }

    }
    // Función similar a mostrarDialogoLezione para el tipo "MENSILE"
    private fun mostrarCalendarioMensile(tipo: String, descripcion: String, precio: String) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_abbonamento, null)
        val tvAbbonamentoInfo = dialogView.findViewById<TextView>(R.id.tvAbbonamentoInfo)
        val btnPagar = dialogView.findViewById<Button>(R.id.btnPagar)

        tvAbbonamentoInfo.text = "Include: $descripcion\nPrecio: $precio"

        val builder = AlertDialog.Builder(this)
        builder.setView(dialogView)

        val dialog = builder.create()
        dialog.show()

        btnPagar.setOnClickListener {
            val userId = FirebaseAuth.getInstance().currentUser?.uid

            if (userId != null) {
                val suscripcionesRef = FirebaseDatabase.getInstance().getReference("Suscripciones").child(userId)

                suscripcionesRef.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {

                        if (dataSnapshot.exists()) {

                            Toast.makeText(baseContext, "Hai gía un Abbonamento", Toast.LENGTH_SHORT).show()

                        } else {

                            abrirWebViewConEnlaceDePago(tipo,precio)
                        }
                    }

                    override fun onCancelled(databaseError: DatabaseError) {

                    }
                })
            }

            dialog.dismiss()
        }

    }
    // Función similar a mostrarDialogoLezione para el tipo "ANNUALE"
    private fun mostrarCalendarioAnnuale(tipo: String, descripcion: String, precio: String) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_abbonamento, null)
        val tvAbbonamentoInfo = dialogView.findViewById<TextView>(R.id.tvAbbonamentoInfo)
        val btnPagar = dialogView.findViewById<Button>(R.id.btnPagar)

        tvAbbonamentoInfo.text = "Include: $descripcion\nPrecio: $precio"

        val builder = AlertDialog.Builder(this)
        builder.setView(dialogView)

        val dialog = builder.create()
        dialog.show()

        btnPagar.setOnClickListener {
            val userId = FirebaseAuth.getInstance().currentUser?.uid

            if (userId != null) {
                val suscripcionesRef = FirebaseDatabase.getInstance().getReference("Suscripciones").child(userId)

                suscripcionesRef.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {

                        if (dataSnapshot.exists()) {

                            Toast.makeText(baseContext, "Hai gía un Abbonamento", Toast.LENGTH_SHORT).show()

                        } else {

                            abrirWebViewConEnlaceDePago(tipo,precio)
                        }
                    }

                    override fun onCancelled(databaseError: DatabaseError) {

                    }
                })
            }

            dialog.dismiss()

        }

    }
    // Abre la actividad WebView para realizar el pago
    private fun abrirWebViewConEnlaceDePago(tipo:String,precio:String) {

        val enlacePago = obtenerEnlacePago(tipo)

        if (enlacePago.isNotEmpty()) {
            val intent = Intent(this, WebViewActivity::class.java)
            intent.putExtra("enlacePago", enlacePago)
            intent.putExtra("tipoSuscripcion", tipo)
            intent.putExtra("precioSuscripcion", precio)
            startActivity(intent)
        } else {
            // Manejo de caso no previsto o error al obtener el enlace de pago
            Toast.makeText(baseContext, "Errore,riprova piú tardi", Toast.LENGTH_SHORT).show()
        }
    }
    // Obtiene el enlace de pago según el tipo de suscripción
    private fun obtenerEnlacePago(tipo: String): String {
        // Define enlaces de pago para diferentes tipos de suscripción
        val enlacePagoMensile = "https://buy.stripe.com/test_aEU8wW88X8R9bGo9AF"
        val enlacePagoLezione = "https://buy.stripe.com/test_00g28yah57N511K7sy"
        val enlacePagoSettimanale = "https://buy.stripe.com/test_eVadRg88XaZh9ygfZ5"
        val enlacePagoAnnuale = "https://buy.stripe.com/test_6oE7sS4WLd7p25OeUY"

        // Devuelve el enlace correspondiente al tipo de suscripción
        return when (tipo) {
            "MENSILE" -> enlacePagoMensile
            "LEZIONE" -> enlacePagoLezione
            "SETTIMANALE" -> enlacePagoSettimanale
            "ANNUALE" -> enlacePagoAnnuale
            else -> ""
        }
    }
}