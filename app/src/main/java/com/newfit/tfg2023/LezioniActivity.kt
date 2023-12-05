package com.newfit.tfg2023

import android.app.DatePickerDialog
import android.icu.text.SimpleDateFormat
import android.icu.util.Calendar
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.*
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import java.util.*
import android.widget.ArrayAdapter

// Declaración de variables globales
private lateinit var tvDate: TextView
private lateinit var bcerca: Button
private lateinit var dbRef: DatabaseReference
private lateinit var firebaseauth: FirebaseAuth
private lateinit var tvClassData: TextView
private lateinit var llClassesContainer: LinearLayout
private lateinit var selectedDate: Calendar


// Clase principal de la actividad Lezioni
class LezioniActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lezioni)

        // Inicialización de variables de la interfaz de usuario
        tvClassData = findViewById(R.id.tvClassData)
        tvDate = findViewById(R.id.tvSelectData)
        bcerca = findViewById(R.id.bcerca)
        llClassesContainer = findViewById(R.id.llClassesContainer)

        // Inicialización de Firebase
        firebaseauth = Firebase.auth
        dbRef = FirebaseDatabase.getInstance().getReference("Lezioni")



        // Acción del botón de búsqueda
        bcerca.setOnClickListener{
            showDatePicker()
        }
    }
    private fun showDatePicker(){
        val currentDate = Calendar.getInstance()
        val nextDay = Calendar.getInstance()
        nextDay.add(Calendar.DAY_OF_MONTH, 1)
        obtenerFechaFinSuscripcion { fechaFin ->
            if (fechaFin.isNotEmpty()) {
                val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                val maxDate = Calendar.getInstance()
                maxDate.time = dateFormat.parse(fechaFin)

                // Configura el DatePickerDialog con la fecha máxima
                val datePickerDialog = DatePickerDialog(
                    this,
                    { _, year: Int, monthOfYear: Int, dayOfMonth: Int ->
                        selectedDate = Calendar.getInstance()
                        selectedDate.set(year, monthOfYear, dayOfMonth)

                        // Verifica si la fecha seleccionada es sábado o domingo
                        if (selectedDate.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY ||
                            selectedDate.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY
                        ) {
                            Toast.makeText(baseContext, "Chiuso", Toast.LENGTH_SHORT).show()
                        } else {
                            // Formatea la fecha y realiza la consulta a Firebase
                            val formattedDate = dateFormat.format(selectedDate.time)
                            tvDate.text = "DATA:$formattedDate"
                            queryFirebase(formattedDate)
                        }
                    },
                    currentDate.get(Calendar.YEAR),
                    currentDate.get(Calendar.MONTH),
                    currentDate.get(Calendar.DAY_OF_MONTH)
                )

                datePickerDialog.datePicker.minDate = nextDay.timeInMillis
                datePickerDialog.datePicker.maxDate = maxDate.timeInMillis
                datePickerDialog.show()
            } else {
                // Manejar el caso en que no haya una fecha de finalización en la base de datos
                // Puedes mostrar un mensaje o manejarlo según tus necesidades
                Toast.makeText(baseContext, "ERRORE", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun obtenerFechaFinSuscripcion(callback: (String) -> Unit) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        val suscripcionesRef = FirebaseDatabase.getInstance().getReference("Suscripciones").child(userId!!)

        suscripcionesRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    val fechaFin = dataSnapshot.child("FechaFin").getValue(String::class.java) ?: ""
                    callback.invoke(fechaFin)
                } else {
                    callback.invoke("") // Si no hay datos, envía una cadena vacía
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Manejar errores si es necesario
                callback.invoke("") // En caso de error, envía una cadena vacía
            }
        })
    }



    // Método para realizar la consulta a Firebase
    private fun queryFirebase(selectedDate:String) {
        // Crear un cuadro de diálogo inferior para mostrar la información de la clase
        val bottomSheetDialog = BottomSheetDialog(this@LezioniActivity)
        val view = layoutInflater.inflate(R.layout.layout_clase_info, null)
        bottomSheetDialog.setContentView(view)
        val usersList: ArrayList<String> = ArrayList()
        val listViewUsers: ListView = view.findViewById(R.id.listViewUsers)
        val adapter = ArrayAdapter(this@LezioniActivity, android.R.layout.simple_list_item_1, usersList)

        listViewUsers.adapter = adapter

        // Obtener la fecha en formato Date
        val date = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(selectedDate)
        val calendar = Calendar.getInstance()
        calendar.time = date
        llClassesContainer.removeAllViews()

        // Obtener el día de la semana
        val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
        val daysOfWeek = arrayOf("Domenica", "Lunedi", "Martedi", "Mercoledi", "Giovedi", "Venerdi", "Sabato")
        val selectedDay = daysOfWeek[dayOfWeek - 1]

        // Consultar Firebase para obtener las clases del día seleccionado
        dbRef.orderByChild("giornosett").equalTo(selectedDay).addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val result = StringBuilder()

                // Iterar a través de las clases y agregar botones dinámicamente
                for (classSnapshot in dataSnapshot.children){
                    val className = classSnapshot.child("nome").getValue(String::class.java)?: "Nome non disponibile"
                    val classTime = classSnapshot.child("orario").getValue(String::class.java)?: "Orario non disponibile"
                    val classID = classSnapshot.key

                    // Agregar botones de clases dinámicamente
                    addClassButton(className, classTime, classSnapshot, usersList, adapter, bottomSheetDialog, classID)
                }

                tvClassData.text = result.toString()
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Manejar errores, si es necesario
            }
        })
    }

    // Método para agregar botones de clase dinámicamente
    private fun addClassButton(
        className: String,
        classTime: String,
        classSnapshot: DataSnapshot,
        usersList: ArrayList<String>,
        adapter: ArrayAdapter<String>,
        bottomSheetDialog: BottomSheetDialog,
        classId: String?) {

        // Crear un botón para cada clase
        val button = Button(this)
        button.text = "$className\norario: $classTime"
        button.setBackgroundColor(resources.getColor(R.color.yellow))

        // Obtener el ID del usuario actual
        val userId = FirebaseAuth.getInstance().currentUser?.uid

        // Verificar si el usuario está inscrito en la clase y cambiar el color del botón en consecuencia
        if (userId != null) {
            val inscripcionesRef = FirebaseDatabase.getInstance().getReference("Iscripzioni").child(userId)
            val date = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault()).format(selectedDate.time)
            val useRef = inscripcionesRef.child(date)
            val orario = classTime

            useRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        val asistenza = dataSnapshot.child("asistenza").getValue(Boolean::class.java)
                        val horario = dataSnapshot.child("orario").getValue(String::class.java)

                        if (asistenza == true && horario == orario) {
                            button.setBackgroundColor(resources.getColor(R.color.purple_700))
                            button.setTextColor(resources.getColor(R.color.white))
                        }
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    // Manejar errores, si es necesario
                }
            })
        }

        // Acción del botón al hacer clic
        button.setOnClickListener {
            // Mostrar información de la clase en un cuadro de diálogo inferior
            val view = layoutInflater.inflate(R.layout.layout_clase_info, null)
            bottomSheetDialog.setContentView(view)
            bottomSheetDialog.show()

            // Configurar la ListView para mostrar la lista de usuarios inscritos
            val listViewUsers: ListView = view.findViewById(R.id.listViewUsers)
            listViewUsers.adapter = adapter

            // Obtener y mostrar la lista de usuarios inscritos en la clase
            getEnrolledUsers(classSnapshot, usersList, adapter, listViewUsers)

            // Configurar botones de inscripción y desinscripción
            val btnEnroll = view.findViewById<Button>(R.id.btnEnroll)
            val btnUnenroll = view.findViewById<Button>(R.id.btnUnenroll)

            // Acción del botón de desinscripción
            btnUnenroll.setOnClickListener {
                // Lógica para desinscribirse de la clase

                // Obtener el ID del usuario actual
                val userId = FirebaseAuth.getInstance().currentUser?.uid


                if (userId != null) {
                    val suscripcionesRef = FirebaseDatabase.getInstance().getReference("Suscripciones").child(userId)

                    // Verificar si el nodo Suscripciones ya existe
                    suscripcionesRef.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                            if (dataSnapshot.exists()) {

                            } else {
                                // El nodo Suscripciones no existe, crearlo con Lezione=true porque al cancelar la inscripción a la clase,podamos inscribirnos a otra
                                suscripcionesRef.child("LEZIONE").setValue(true)

                            }
                        }

                        override fun onCancelled(databaseError: DatabaseError) {
                            // Manejo de errores, si es necesario
                        }
                    })
                }

                if (userId != null) {

                    // Referencia a la sección de inscripciones del usuario

                    val inscripcionesRef = FirebaseDatabase.getInstance().getReference("Iscripzioni").child(userId)
                    val date = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault()).format(selectedDate.time)
                    val useRef = inscripcionesRef.child(date)
                    val orario = classTime

                    useRef.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                            if (dataSnapshot.exists()) {
                                // Verificar si el usuario está inscrito en la clase
                                val asistenza = dataSnapshot.child("asistenza").getValue(Boolean::class.java)
                                val horario = dataSnapshot.child("orario").getValue(String::class.java)

                                if (asistenza == true && horario == orario) {
                                    // Eliminar la inscripción del usuario y actualizar la interfaz
                                    usersList.clear()
                                    adapter.notifyDataSetChanged()
                                    dataSnapshot.ref.removeValue()

                                    button.setBackgroundColor(resources.getColor(R.color.yellow))
                                    button.setTextColor(resources.getColor(R.color.black))

                                    Toast.makeText(baseContext, "Iscrizione annullata", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(baseContext, "Non sei iscritto a questa lezione", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }

                        override fun onCancelled(databaseError: DatabaseError) {
                            // Manejo de errores, si es necesario
                        }
                    })

                    // Eliminar la referencia del usuario en la lista de inscritos en la clase
                    val lezioniRef = FirebaseDatabase.getInstance().getReference("Lezioni").child(classId!!)
                    val userLezRef = lezioniRef.child("utenInscriti").child(userId)

                    userLezRef.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                            if (dataSnapshot.exists()) {
                                dataSnapshot.ref.removeValue()
                            }
                        }

                        override fun onCancelled(databaseError: DatabaseError) {
                            // Manejo de errores, si es necesario
                        }
                    })
                }
            }

            // Acción del botón de inscripción
            btnEnroll.setOnClickListener {
                // Lógica para inscribirse en la clase

                // Obtener el ID del usuario actual
                val userId = FirebaseAuth.getInstance().currentUser?.uid

                if (userId != null) {
                    // Referencia a la sección de inscripciones del usuario
                    val inscripcionesRef = FirebaseDatabase.getInstance().getReference("Iscripzioni").child(userId)
                    val date = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault()).format(selectedDate.time)
                    val useRef = inscripcionesRef.child(date)


                    useRef.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                            if  (dataSnapshot.exists() && dataSnapshot.child("asistenza").getValue(Boolean::class.java) == true ){
                                // El usuario ya está inscrito en otra clase en la misma fecha
                                Toast.makeText(baseContext, "Sei giá iscrito oggi", Toast.LENGTH_SHORT).show()
                            } else {
                                // El usuario no está inscrito en otra clase en la misma fecha


                                if (userId != null) {
                                    val inscripcionesRef = FirebaseDatabase.getInstance().getReference("Suscripciones").child(userId).child("LEZIONE")

                                    inscripcionesRef.addListenerForSingleValueEvent(object : ValueEventListener {
                                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                                            if (dataSnapshot.exists() && dataSnapshot.getValue(Boolean::class.java) == true) {
                                                // Usuario tiene la suscripción Lezione activa
                                                // Eliminar la suscripción Lezione activa
                                                dataSnapshot.ref.removeValue()
                                            } else {
                                                // Usuario no tiene la suscripción Lezione activa,pero los errores aquí se manejan en las demás actividades

                                            }
                                        }

                                        override fun onCancelled(databaseError: DatabaseError) {
                                            // Manejo de errores, si es necesario
                                        }
                                    })
                                }


                                if (userId != null) {
                                    // Referencia a la sección de inscripciones del usuario
                                    val inscripcionesRef = FirebaseDatabase.getInstance().getReference("Iscripzioni").child(userId)
                                    val date = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault()).format(selectedDate.time)
                                    val useRef = inscripcionesRef.child(date)
                                    val orario = classTime

                                    // Obtener el nombre del usuario desde Firebase
                                    val userRef = FirebaseDatabase.getInstance().getReference("Clienti").child(userId)
                                    userRef.addListenerForSingleValueEvent(object : ValueEventListener {
                                        override fun onDataChange(userSnapshot: DataSnapshot) {
                                            val userName = userSnapshot.child("nome").getValue(String::class.java)

                                            if (userName != null) {
                                                // Guardar la inscripción en la base de datos
                                                val inscripcion = IscripzioniData(className, date, orario, true)
                                                useRef.setValue(inscripcion)

                                                // Añadir el nombre del usuario a la lista de inscritos
                                                val enrolledUsersRef = classSnapshot.child("utenInscriti").ref
                                                enrolledUsersRef.child(userId).child("nombre").setValue(userName)

                                                Toast.makeText(baseContext, "Iscrito correttamente", Toast.LENGTH_SHORT).show()
                                                button.setBackgroundColor(resources.getColor(R.color.purple_700))
                                                button.setTextColor(resources.getColor(R.color.white))
                                            } else {
                                                Toast.makeText(baseContext, "Errore nel recupero del nome utente", Toast.LENGTH_SHORT).show()
                                            }
                                        }

                                        override fun onCancelled(databaseError: DatabaseError) {
                                            Log.e("Error", "Error al obtener datos del usuario: ${databaseError.message}")
                                        }
                                    })
                                }
                            }
                        }

                        override fun onCancelled(databaseError: DatabaseError) {
                            // Manejo de errores, si es necesario
                        }
                    })
                }
            }
        }

        // Añadir el botón a la interfaz de usuario
        llClassesContainer.addView(button)
    }

    // Método para obtener y mostrar la lista de usuarios inscritos en la clase
    private fun getEnrolledUsers(
        classSnapshot: DataSnapshot,
        usersList: ArrayList<String>,
        adapter: ArrayAdapter<String>,
        listViewUsers: ListView
    ) {
        val enrolledUsersRef = classSnapshot.child("utenInscriti").ref

        // Consultar Firebase para obtener la lista de usuarios inscritos
        enrolledUsersRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                usersList.clear()

                // Iterar a través de la lista de usuarios inscritos y añadirlos a la lista
                for (userSnapshot in snapshot.children) {
                    val userName = userSnapshot.child("nombre").getValue(String::class.java)
                    if (userName != null) {
                        usersList.add(userName)
                    }
                }

                // Actualizar el adaptador y mostrar la lista en la ListView
                adapter.notifyDataSetChanged()
                listViewUsers.adapter = adapter
            }

            override fun onCancelled(error: DatabaseError) {
                // Manejo de errores, si es necesario
            }
        })
    }
}
