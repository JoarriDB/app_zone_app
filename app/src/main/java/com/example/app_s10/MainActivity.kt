package com.example.app_s10

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    // Views
    private lateinit var tvWelcome: TextView
    private lateinit var tvUserEmail: TextView
    private lateinit var btnLogout: MaterialButton
    private lateinit var cardStats: CardView
    private lateinit var cardAchievements: CardView
    private lateinit var cardProfile: CardView
    private lateinit var cardSettings: CardView

    // 🎮 NUEVOS: Cards para el sistema de juegos
    private lateinit var cardAddGame: CardView
    private lateinit var cardViewGames: CardView

    companion object {
        private const val TAG = "MainActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        // Inicializar Firebase Auth
        auth = FirebaseAuth.getInstance()

        // Verificar autenticación
        val currentUser = auth.currentUser
        if (currentUser == null) {
            // Usuario no autenticado, redirigir al login
            redirectToLogin()
            return
        }

        // Configurar UI
        setupUI()
        setupWindowInsets()

        // Cargar información del usuario
        loadUserInfo(currentUser)

        // Configurar listeners
        setupClickListeners()

        Log.d(TAG, "MainActivity iniciado para usuario: ${currentUser.email}")
    }

    private fun setupUI() {
        // Inicializar views existentes
        tvWelcome = findViewById(R.id.tv_welcome)
        tvUserEmail = findViewById(R.id.tv_user_email)
        btnLogout = findViewById(R.id.btn_logout)
        cardStats = findViewById(R.id.card_stats)
        cardAchievements = findViewById(R.id.card_achievements)
        cardProfile = findViewById(R.id.card_profile)
        cardSettings = findViewById(R.id.card_settings)

        // 🎮 NUEVOS: Inicializar cards de juegos
        cardAddGame = findViewById(R.id.card_add_game)
        cardViewGames = findViewById(R.id.card_view_games)
    }

    private fun setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun loadUserInfo(user: FirebaseUser) {
        // Personalizar saludo según el tipo de usuario
        val welcomeMessage = if (user.isAnonymous) {
            "¡Hola, Invitado!"
        } else {
            // Intentar obtener nombre del displayName o email
            val name = user.displayName?.takeIf { it.isNotEmpty() }
                ?: user.email?.substringBefore("@")?.replaceFirstChar { it.uppercase() }
                ?: "Gamer"
            "¡Hola, $name!"
        }

        tvWelcome.text = welcomeMessage

        // Mostrar email o indicar usuario anónimo
        tvUserEmail.text = if (user.isAnonymous) {
            "Usuario invitado"
        } else {
            user.email ?: "Sin email"
        }

        Log.d(TAG, "Usuario cargado - Anónimo: ${user.isAnonymous}, Email: ${user.email}, Nombre: ${user.displayName}")

        // Verificar estado de verificación de email
        if (!user.isAnonymous && user.email != null && !user.isEmailVerified) {
            showEmailVerificationDialog()
        }
    }

    private fun setupClickListeners() {
        // Botón logout
        btnLogout.setOnClickListener {
            showLogoutConfirmationDialog()
        }

        // 🎮 Funcionalidades de juegos
        setupGameFeatures()

        // Cards de navegación originales
        cardStats.setOnClickListener {
            showGameStatistics()
        }

        cardAchievements.setOnClickListener {
            showAchievementsInfo()
        }

        cardProfile.setOnClickListener {
            showProfileInfo()
        }

        cardSettings.setOnClickListener {
            showSettingsInfo()
        }
    }

    // 🎮 Configurar funcionalidades de juegos
    private fun setupGameFeatures() {
        // Configurar cards de juegos principales
        cardAddGame.setOnClickListener {
            val intent = Intent(this, AddGameActivity::class.java)
            startActivity(intent)
        }

        cardViewGames.setOnClickListener {
            val intent = Intent(this, GamesListActivity::class.java)
            startActivity(intent)
        }
    }

    // 🎮 Mostrar estadísticas básicas
    private fun showGameStatistics() {
        AlertDialog.Builder(this)
            .setTitle("📊 Estadísticas de Juegos")
            .setMessage("""
                🎮 Sistema de Registro de Juegos Configurado
                
                Funcionalidades disponibles:
                ✅ Agregar juegos a tu biblioteca
                ✅ Ver lista completa de juegos
                ✅ Calificar con sistema de estrellas
                ✅ Marcar como completado
                ✅ Filtrar por estado
                ✅ Sincronización en tiempo real
                ✅ Eliminar juegos
                
                ¡Comienza a construir tu biblioteca gaming!
            """.trimIndent())
            .setPositiveButton("🎮 Ver Mi Biblioteca") { _, _ ->
                startActivity(Intent(this, GamesListActivity::class.java))
            }
            .setNeutralButton("➕ Agregar Juego") { _, _ ->
                startActivity(Intent(this, AddGameActivity::class.java))
            }
            .setNegativeButton("OK", null)
            .show()
    }

    // 🏆 Información de logros
    private fun showAchievementsInfo() {
        AlertDialog.Builder(this)
            .setTitle("🏆 Sistema de Logros")
            .setMessage("""
                ¡Desbloquea logros gaming!
                
                🎮 Primeros Pasos:
                • Registra tu primer juego
                • Completa 5 juegos
                • Alcanza rating promedio de 4⭐
                
                🏆 Logros Avanzados:
                • Biblioteca de 50+ juegos
                • Master gamer (100+ completados)
                • Crítico experto (ratings detallados)
                
                💡 Tip: ¡Agrega más juegos para desbloquear achievements!
            """.trimIndent())
            .setPositiveButton("🎮 Ir a Mis Juegos") { _, _ ->
                startActivity(Intent(this, GamesListActivity::class.java))
            }
            .setNegativeButton("OK", null)
            .show()
    }

    // 👤 Información de perfil
    private fun showProfileInfo() {
        val user = auth.currentUser
        val userType = if (user?.isAnonymous == true) "Invitado" else "Registrado"
        val email = user?.email ?: "No disponible"
        val verified = if (user?.isEmailVerified == true) "✅ Verificado" else "⚠️ Sin verificar"

        AlertDialog.Builder(this)
            .setTitle("👤 Mi Perfil Gaming")
            .setMessage("""
                📊 Información del Usuario:
                
                👤 Tipo: $userType
                📧 Email: $email
                🔐 Estado: $verified
                
                🎮 Personalización:
                • Avatar gaming personalizado
                • Estadísticas detalladas
                • Historial de actividad
                • Preferencias de juegos
                
                ¡Tu progreso gaming en un solo lugar!
            """.trimIndent())
            .setPositiveButton("Ver Estadísticas") { _, _ ->
                showGameStatistics()
            }
            .setNegativeButton("OK", null)
            .show()
    }

    // ⚙️ Información de configuración
    private fun showSettingsInfo() {
        AlertDialog.Builder(this)
            .setTitle("⚙️ Configuración Gaming")
            .setMessage("""
                🎮 Personaliza tu experiencia:
                
                🎨 Apariencia:
                • Temas de color (Neón, Retro, Dark)
                • Modo nocturno automático
                • Tamaño de fuente
                
                📱 Funcionalidades:
                • Notificaciones de nuevos juegos
                • Backup automático
                • Sincronización en la nube
                
                🔐 Cuenta:
                • Gestión de sesión
                • Privacidad de datos
                • Exportar biblioteca
            """.trimIndent())
            .setPositiveButton("Gestionar Cuenta") { _, _ ->
                showAccountOptions()
            }
            .setNegativeButton("OK", null)
            .show()
    }

    // 🔐 Opciones de cuenta
    private fun showAccountOptions() {
        val options = arrayOf(
            "📧 Verificar Email",
            "🔒 Cambiar Contraseña",
            "📤 Exportar Datos",
            "🚪 Cerrar Sesión"
        )

        AlertDialog.Builder(this)
            .setTitle("🔐 Gestión de Cuenta")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> sendEmailVerification()
                    1 -> showChangePasswordInfo()
                    2 -> showExportDataInfo()
                    3 -> showLogoutConfirmationDialog()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    // 🔒 Info cambio de contraseña
    private fun showChangePasswordInfo() {
        AlertDialog.Builder(this)
            .setTitle("🔒 Cambiar Contraseña")
            .setMessage("Se enviará un enlace de recuperación a tu email para cambiar la contraseña.")
            .setPositiveButton("Enviar") { _, _ ->
                val email = auth.currentUser?.email
                if (email != null) {
                    auth.sendPasswordResetEmail(email)
                        .addOnSuccessListener {
                            Toast.makeText(this, "📧 Email de recuperación enviado", Toast.LENGTH_LONG).show()
                        }
                        .addOnFailureListener {
                            Toast.makeText(this, "Error al enviar email", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    Toast.makeText(this, "No hay email asociado a la cuenta", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    // 📤 Info exportar datos
    private fun showExportDataInfo() {
        AlertDialog.Builder(this)
            .setTitle("📤 Exportar Biblioteca")
            .setMessage("""
                🎮 Próximamente podrás exportar:
                
                📊 Tu biblioteca completa
                📈 Estadísticas de juego
                🏆 Logros desbloqueados
                📱 Configuraciones personalizadas
                
                Formatos disponibles: JSON, CSV, PDF
            """.trimIndent())
            .setPositiveButton("Ver Mis Datos") { _, _ ->
                startActivity(Intent(this, GamesListActivity::class.java))
            }
            .setNegativeButton("OK", null)
            .show()
    }

    private fun showLogoutConfirmationDialog() {
        AlertDialog.Builder(this)
            .setTitle("Cerrar Sesión")
            .setMessage("¿Estás seguro de que quieres cerrar sesión?")
            .setPositiveButton("Sí") { _, _ ->
                performLogout()
            }
            .setNegativeButton("Cancelar", null)
            .setIcon(android.R.drawable.ic_dialog_alert)
            .show()
    }

    private fun performLogout() {
        auth.signOut()
        Toast.makeText(this, getString(R.string.logout_success), Toast.LENGTH_SHORT).show()
        Log.d(TAG, "Usuario desconectado")
        redirectToLogin()
    }

    private fun redirectToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun showEmailVerificationDialog() {
        AlertDialog.Builder(this)
            .setTitle("Verificar Email")
            .setMessage(getString(R.string.auth_email_verification_required))
            .setPositiveButton("Enviar verificación") { _, _ ->
                sendEmailVerification()
            }
            .setNegativeButton("Más tarde", null)
            .setIcon(android.R.drawable.ic_dialog_info)
            .show()
    }

    private fun sendEmailVerification() {
        val user = auth.currentUser
        user?.sendEmailVerification()
            ?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, getString(R.string.auth_verification_email_sent), Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(this, "Error al enviar verificación", Toast.LENGTH_SHORT).show()
                }
            }
    }

    override fun onStart() {
        super.onStart()
        // Verificar autenticación cada vez que la actividad se vuelve visible
        val currentUser = auth.currentUser
        if (currentUser == null) {
            Log.d(TAG, "Usuario no autenticado en onStart, redirigiendo...")
            redirectToLogin()
        }
    }
}