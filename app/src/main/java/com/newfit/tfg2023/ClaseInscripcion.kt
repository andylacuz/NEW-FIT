package com.newfit.tfg2023

data class ClaseInscripcion(
    val nome: String?, // Identificador único de la inscripción
    val fechaInicio: String? = null,
    val fechaFin: String? = null,
    val prezzo : String?
)