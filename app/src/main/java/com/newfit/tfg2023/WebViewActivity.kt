package com.newfit.tfg2023

import android.annotation.SuppressLint
import android.os.Bundle
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.*

class WebViewActivity : AppCompatActivity() {
    private lateinit var webView: WebView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_web_view)

        // Inicializa el WebView desde el diseño
        webView = findViewById(R.id.webView)

        // Obtiene datos de la intención
        val enlacePago = intent.getStringExtra("enlacePago")
        val tipoSuscripcion = intent.getStringExtra("tipoSuscripcion")
        val precio = intent.getStringExtra("precioSuscripcion")

        // Verifica si los datos necesarios están presentes antes de cargar el WebView
        if (enlacePago != null && tipoSuscripcion != null && precio != null) {
            cargarWebView(enlacePago, tipoSuscripcion)
        } else {
            // Finaliza la actividad si los datos no están completos
            finish()
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun cargarWebView(enlacePago: String, tipo: String) {
        // Configura opciones del WebView y carga la URL del enlace de pago
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        webView.settings.javaScriptEnabled = true

        webView.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                // Puede manejar el progreso de carga aquí si es necesario
            }
        }

        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                // Permite que las URL se carguen en el WebView y no en el navegador externo
                return false
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                // Se ejecuta cuando la página ha terminado de cargarse

                // Verifica si el pago se ha confirmado según la URL
                val pagoConfirmado = verificarPagoConfirmado(url)
                if (pagoConfirmado) {
                    // Si el pago está confirmado, registra la suscripción en la base de datos
                    if (userId != null) {
                        registrarSuscripcionEnBD(userId, tipo)
                    }
                } else {
                    // Manejo de errores si fuese necesario

                }
            }
        }

        // Carga la URL del enlace de pago en el WebView
        webView.loadUrl(enlacePago)
    }

    private fun verificarPagoConfirmado(url: String?): Boolean {
        // Verifica si la URL contiene la confirmación de pago
        return url?.contains("https://andylacuz.github.io/Confirmation/") == true
    }

    private fun registrarSuscripcionEnBD(userId: String, tipo: String) {
        // Registra la suscripción en la base de datos Firebase

        // Obtiene la fecha actual para el pago
        val fechaPago = obtenerFechaActual()


        // Referencia a la ubicación de la suscripción en la base de datos
        val suscripcionesActualRef = FirebaseDatabase.getInstance().getReference("Suscripciones").child(userId).child(tipo)

        // Establece el valor de la suscripción como verdadero
        suscripcionesActualRef.setValue(true)



        // Según el tipo de suscripción, inscribe al usuario en la clase correspondiente
        when (tipo) {
            "LEZIONE" -> {
                inscribirseAClase(userId, fechaPago, 30, "Lezione", "10€")
            }
            "SETTIMANALE" -> {
                inscribirseAClase(userId, fechaPago, 7, "Settimanale", "18€")
            }
            "MENSILE" -> {
                inscribirseAClase(userId, fechaPago, 30, "Mensile", "60€")
            }
            "ANNUALE" -> {
                inscribirseAClase(userId, fechaPago, 365, "Annuale", "600€")
            }
            else -> {
                // Manejo para otros casos si es necesario
            }
        }
    }

    private fun inscribirseAClase(userId: String, fechaInicio: String, duracionDias: Int, nome: String, prezzo: String) {
        // Inscribir al usuario en una clase y registrar la información en la base de datos

        // Referencia a la ubicación de las clases inscritas en la base de datos
        val clasesInscritasRef = FirebaseDatabase.getInstance().getReference("Inscripciones").child(userId)

        // Genera un ID único para la inscripción
        val inscripcionId = clasesInscritasRef.push().key

        // Calcula la fecha de finalización de la clase
        val fechaFin = calcularFechaFin(fechaInicio, duracionDias)

        val suscripcionesActualRef = FirebaseDatabase.getInstance().getReference("Suscripciones").child(userId)
        suscripcionesActualRef.child("FechaFin").setValue(fechaFin)
        // Crea un objeto ClaseInscripcion con la información de la clase
        val claseInscripcion = ClaseInscripcion(nome, fechaInicio, fechaFin, prezzo)

        // Guarda la información de la clase inscrita en la base de datos
        if (inscripcionId != null) {
            clasesInscritasRef.child(inscripcionId).setValue(claseInscripcion)
        }
    }

    private fun calcularFechaFin(fechaInicio: String, duracionDias: Int): String {
        // Calcula la fecha de finalización sumando la duración en días a la fecha de inicio

        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val fechaInicioDate = dateFormat.parse(fechaInicio)

        val calendar = Calendar.getInstance()
        calendar.time = fechaInicioDate!!
        calendar.add(Calendar.DAY_OF_MONTH, duracionDias)

        return dateFormat.format(calendar.time)
    }

    private fun obtenerFechaActual(): String {
        // Obtiene la fecha actual en formato dd/MM/yyyy

        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        return dateFormat.format(Date())
    }
}
