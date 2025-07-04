package com.example.app_s10

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.RatingBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class AddGameActivity : AppCompatActivity() {

    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth

    // Views
    private lateinit var inputLayoutTitle: TextInputLayout
    private lateinit var inputLayoutGenre: TextInputLayout
    private lateinit var inputLayoutPlatform: TextInputLayout
    private lateinit var inputLayoutDescription: TextInputLayout
    private lateinit var inputLayoutYear: TextInputLayout
    private lateinit var etTitle: TextInputEditText
    private lateinit var etGenre: TextInputEditText
    private lateinit var etPlatform: TextInputEditText
    private lateinit var etDescription: TextInputEditText
    private lateinit var etYear: TextInputEditText
    private lateinit var ratingBar: RatingBar
    private lateinit var tvRatingLabel: TextView
    private lateinit var switchCompleted: SwitchMaterial
    private lateinit var btnSave: MaterialButton
    private lateinit var btnCancel: MaterialButton
    private lateinit var progressBar: ProgressBar

    companion object {
        private const val TAG = "AddGameActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_game)

        Log.d(TAG, "=== AddGameActivity INICIADO ===")

        // Inicializar Firebase
        initializeFirebase()

        // Verificar autenticación
        if (!checkAuthentication()) {
            return
        }

        // Inicializar interfaz
        initializeViews()
        setupListeners()

        // Debug opcional (comentar en producción)
        debugFirebaseConnection()

        Log.d(TAG, "✅ AddGameActivity configurado correctamente")
    }

    private fun initializeFirebase() {
        try {
            auth = FirebaseAuth.getInstance()
            database = FirebaseDatabase.getInstance().reference
            Log.d(TAG, "✅ Firebase inicializado")
            Log.d(TAG, "Database URL: ${database.toString()}")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error inicializando Firebase", e)
            Toast.makeText(this, "Error de configuración de Firebase", Toast.LENGTH_LONG).show()
        }
    }

    private fun checkAuthentication(): Boolean {
        val currentUser = auth.currentUser
        return if (currentUser == null) {
            Log.e(TAG, "❌ Usuario no autenticado")
            Toast.makeText(this, "Error: Debes iniciar sesión", Toast.LENGTH_LONG).show()
            finish()
            false
        } else {
            Log.d(TAG, "✅ Usuario autenticado: ${currentUser.uid}")
            Log.d(TAG, "Email: ${currentUser.email}")
            Log.d(TAG, "Anónimo: ${currentUser.isAnonymous}")
            true
        }
    }

    private fun initializeViews() {
        try {
            // Input layouts
            inputLayoutTitle = findViewById(R.id.input_layout_title)
            inputLayoutGenre = findViewById(R.id.input_layout_genre)
            inputLayoutPlatform = findViewById(R.id.input_layout_platform)
            inputLayoutDescription = findViewById(R.id.input_layout_description)
            inputLayoutYear = findViewById(R.id.input_layout_year)

            // Edit texts
            etTitle = findViewById(R.id.etGameTitle)
            etGenre = findViewById(R.id.etGameGenre)
            etPlatform = findViewById(R.id.etGamePlatform)
            etDescription = findViewById(R.id.etGameDescription)
            etYear = findViewById(R.id.etGameYear)

            // Other controls
            ratingBar = findViewById(R.id.ratingBar)
            tvRatingLabel = findViewById(R.id.tvRatingLabel)
            switchCompleted = findViewById(R.id.switchCompleted)

            // Buttons
            btnSave = findViewById(R.id.btnSaveGame)
            btnCancel = findViewById(R.id.btnCancel)

            // Progress
            progressBar = findViewById(R.id.progressBar)

            Log.d(TAG, "✅ Views inicializadas correctamente")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error inicializando views", e)
            Toast.makeText(this, "Error en la interfaz: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun setupListeners() {
        // Rating bar listener
        ratingBar.setOnRatingBarChangeListener { _, rating, _ ->
            tvRatingLabel.text = "Rating: ${String.format("%.1f", rating)}/5 ⭐"
            Log.d(TAG, "Rating actualizado: $rating")
        }

        // Save button
        btnSave.setOnClickListener {
            Log.d(TAG, "🎮 Botón GUARDAR presionado")
            saveGame()
        }

        // Cancel button
        btnCancel.setOnClickListener {
            Log.d(TAG, "❌ Botón CANCELAR presionado")
            finish()
        }

        Log.d(TAG, "✅ Listeners configurados")
    }

    private fun saveGame() {
        Log.d(TAG, "=== INICIANDO PROCESO DE GUARDADO ===")

        // Obtener datos del formulario
        val gameData = collectFormData()

        // Validar datos
        if (!validateGameData(gameData)) {
            Log.w(TAG, "❌ Validación falló")
            return
        }

        // Mostrar loading
        showLoading(true)

        // Procesar guardado
        processGameSave(gameData)
    }

    private fun collectFormData(): Map<String, Any> {
        val title = etTitle.text.toString().trim()
        val genre = etGenre.text.toString().trim()
        val platform = etPlatform.text.toString().trim()
        val description = etDescription.text.toString().trim()
        val yearString = etYear.text.toString().trim()
        val rating = ratingBar.rating
        val completed = switchCompleted.isChecked

        val data = mapOf(
            "title" to title,
            "genre" to genre,
            "platform" to platform,
            "description" to description,
            "yearString" to yearString,
            "rating" to rating,
            "completed" to completed
        )

        Log.d(TAG, "Datos recolectados:")
        Log.d(TAG, "  - Título: '$title'")
        Log.d(TAG, "  - Género: '$genre'")
        Log.d(TAG, "  - Plataforma: '$platform'")
        Log.d(TAG, "  - Año: '$yearString'")
        Log.d(TAG, "  - Rating: $rating")
        Log.d(TAG, "  - Completado: $completed")

        return data
    }

    private fun validateGameData(data: Map<String, Any>): Boolean {
        clearErrors()
        var isValid = true

        val title = data["title"] as String
        val genre = data["genre"] as String
        val platform = data["platform"] as String
        val yearString = data["yearString"] as String

        // Validar título
        when {
            title.isEmpty() -> {
                inputLayoutTitle.error = "El título es obligatorio"
                isValid = false
                Log.w(TAG, "❌ Título vacío")
            }
            title.length < 2 -> {
                inputLayoutTitle.error = "El título debe tener al menos 2 caracteres"
                isValid = false
                Log.w(TAG, "❌ Título muy corto: ${title.length} caracteres")
            }
            title.length > 100 -> {
                inputLayoutTitle.error = "El título es muy largo (máx. 100 caracteres)"
                isValid = false
                Log.w(TAG, "❌ Título muy largo: ${title.length} caracteres")
            }
        }

        // Validar género
        when {
            genre.isEmpty() -> {
                inputLayoutGenre.error = "El género es obligatorio"
                isValid = false
                Log.w(TAG, "❌ Género vacío")
            }
            genre.length > 50 -> {
                inputLayoutGenre.error = "El género es muy largo (máx. 50 caracteres)"
                isValid = false
                Log.w(TAG, "❌ Género muy largo: ${genre.length} caracteres")
            }
        }

        // Validar plataforma
        when {
            platform.isEmpty() -> {
                inputLayoutPlatform.error = "La plataforma es obligatoria"
                isValid = false
                Log.w(TAG, "❌ Plataforma vacía")
            }
            platform.length > 50 -> {
                inputLayoutPlatform.error = "La plataforma es muy larga (máx. 50 caracteres)"
                isValid = false
                Log.w(TAG, "❌ Plataforma muy larga: ${platform.length} caracteres")
            }
        }

        // Validar año
        if (yearString.isNotEmpty()) {
            val year = yearString.toIntOrNull()
            when {
                year == null -> {
                    inputLayoutYear.error = "El año debe ser un número válido"
                    isValid = false
                    Log.w(TAG, "❌ Año inválido: '$yearString'")
                }
                year < 1970 -> {
                    inputLayoutYear.error = "El año debe ser posterior a 1970"
                    isValid = false
                    Log.w(TAG, "❌ Año muy antiguo: $year")
                }
                year > 2030 -> {
                    inputLayoutYear.error = "El año debe ser anterior a 2030"
                    isValid = false
                    Log.w(TAG, "❌ Año muy futuro: $year")
                }
            }
        }

        // Validar descripción (opcional)
        val description = data["description"] as String
        if (description.length > 500) {
            inputLayoutDescription.error = "La descripción es muy larga (máx. 500 caracteres)"
            isValid = false
            Log.w(TAG, "❌ Descripción muy larga: ${description.length} caracteres")
        }

        Log.d(TAG, "Validación completa. Resultado: $isValid")
        return isValid
    }

    private fun processGameSave(formData: Map<String, Any>) {
        try {
            // Generar IDs
            val userId = auth.currentUser?.uid ?: throw Exception("No se pudo obtener el ID del usuario")
            val gameId = database.child("games").child(userId).push().key
                ?: throw Exception("No se pudo generar el ID del juego")

            Log.d(TAG, "IDs generados:")
            Log.d(TAG, "  - Usuario: $userId")
            Log.d(TAG, "  - Juego: $gameId")

            // Crear objeto Game
            val game = createGameObject(formData, gameId, userId)
            Log.d(TAG, "Objeto Game creado: $game")

            // Guardar en Firebase
            saveToFirebase(userId, gameId, game)

        } catch (e: Exception) {
            Log.e(TAG, "❌ Error en processGameSave", e)
            showLoading(false)
            showError("Error inesperado: ${e.message}")
        }
    }

    private fun createGameObject(formData: Map<String, Any>, gameId: String, userId: String): Game {
        val yearString = formData["yearString"] as String
        val year = if (yearString.isNotEmpty()) yearString.toIntOrNull() ?: 0 else 0

        return Game(
            id = gameId,
            title = formData["title"] as String,
            genre = formData["genre"] as String,
            platform = formData["platform"] as String,
            rating = formData["rating"] as Float,
            description = formData["description"] as String,
            releaseYear = year,
            completed = formData["completed"] as Boolean,
            userId = userId,
            createdAt = System.currentTimeMillis()
        )
    }

    private fun saveToFirebase(userId: String, gameId: String, game: Game) {
        val path = "games/$userId/$gameId"
        Log.d(TAG, "Guardando en Firebase - Ruta: $path")

        database.child("games").child(userId).child(gameId).setValue(game)
            .addOnSuccessListener {
                Log.d(TAG, "🎉 ¡ÉXITO! Juego guardado correctamente")
                showLoading(false)
                showSuccess("🎮 ¡Juego '${game.title}' guardado exitosamente!")

                // Volver a la pantalla anterior después de un breve delay
                android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                    finish()
                }, 1500)
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "❌ Error al guardar en Firebase", exception)
                showLoading(false)

                val errorMessage = when {
                    exception.message?.contains("Permission denied") == true ->
                        "Error de permisos. Verifica la configuración de Firebase."
                    exception.message?.contains("network") == true ->
                        "Error de conexión. Verifica tu internet."
                    else -> "Error al guardar: ${exception.message}"
                }

                showError(errorMessage)
            }
            .addOnCompleteListener { task ->
                Log.d(TAG, "Operación de guardado completada. Éxito: ${task.isSuccessful}")
                if (!task.isSuccessful) {
                    Log.e(TAG, "Detalles del error:", task.exception)
                }
            }

        // Timeout de seguridad (15 segundos)
        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
            if (progressBar.visibility == View.VISIBLE) {
                Log.w(TAG, "⏰ Timeout - La operación está tardando demasiado")
                showLoading(false)
                showError("⏰ La operación está tardando mucho. Verifica tu conexión e inténtalo de nuevo.")
            }
        }, 15000)
    }

    private fun clearErrors() {
        inputLayoutTitle.error = null
        inputLayoutGenre.error = null
        inputLayoutPlatform.error = null
        inputLayoutDescription.error = null
        inputLayoutYear.error = null
    }

    private fun showLoading(show: Boolean) {
        Log.d(TAG, "Mostrar loading: $show")

        progressBar.visibility = if (show) View.VISIBLE else View.GONE
        btnSave.isEnabled = !show
        btnCancel.isEnabled = !show

        // Cambiar texto y apariencia del botón
        if (show) {
            btnSave.text = "GUARDANDO..."
            btnSave.alpha = 0.6f
        } else {
            btnSave.text = "💾 GUARDAR JUEGO"
            btnSave.alpha = 1.0f
        }

        // Deshabilitar campos durante guardado
        etTitle.isEnabled = !show
        etGenre.isEnabled = !show
        etPlatform.isEnabled = !show
        etDescription.isEnabled = !show
        etYear.isEnabled = !show
        ratingBar.isEnabled = !show
        switchCompleted.isEnabled = !show
    }

    private fun showSuccess(message: String) {
        Log.d(TAG, "✅ Éxito: $message")
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    private fun showError(message: String) {
        Log.e(TAG, "❌ Error: $message")
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    // Función de debug (comentar en producción)
    private fun debugFirebaseConnection() {
        Log.d(TAG, "=== DEBUG: Testing Firebase Connection ===")

        // Test de conectividad simple
        val testKey = "connection_test_${System.currentTimeMillis()}"
        database.child("debug").child(testKey).setValue("test_value")
            .addOnSuccessListener {
                Log.d(TAG, "✅ Debug: Conexión a Firebase EXITOSA")
            }
            .addOnFailureListener { error ->
                Log.e(TAG, "❌ Debug: Error de conexión Firebase", error)
            }
    }
}