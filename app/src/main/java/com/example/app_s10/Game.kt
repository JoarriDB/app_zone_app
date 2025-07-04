package com.example.app_s10

/**
 * Modelo de datos para representar un juego en la base de datos
 * Compatible con Firebase Realtime Database
 */
data class Game(
    val id: String = "",
    val title: String = "",
    val genre: String = "",
    val platform: String = "",
    val rating: Float = 0f,
    val description: String = "",
    val releaseYear: Int = 0,
    val completed: Boolean = false,
    val userId: String = "",
    val createdAt: Long = System.currentTimeMillis()
) {
    // Constructor sin parámetros requerido por Firebase
    constructor() : this("", "", "", "", 0f, "", 0, false, "", 0L)

    // Método para convertir a Map (útil para Firebase)
    fun toMap(): Map<String, Any> {
        return mapOf(
            "id" to id,
            "title" to title,
            "genre" to genre,
            "platform" to platform,
            "rating" to rating,
            "description" to description,
            "releaseYear" to releaseYear,
            "completed" to completed,
            "userId" to userId,
            "createdAt" to createdAt
        )
    }
}