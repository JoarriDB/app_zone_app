package com.example.app_s10

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class GamesListActivity : AppCompatActivity() {

    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private lateinit var gameAdapter: GameAdapter

    // Views
    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var layoutEmptyState: LinearLayout
    private lateinit var fabAddGame: FloatingActionButton
    private lateinit var tvTotalGames: TextView
    private lateinit var tvCompletedGames: TextView
    private lateinit var tvAverageRating: TextView
    private lateinit var chipAll: Chip
    private lateinit var chipCompleted: Chip
    private lateinit var chipPending: Chip

    // Data
    private var allGames = mutableListOf<Game>()
    private var filteredGames = mutableListOf<Game>()

    // Filter states
    private enum class FilterType { ALL, COMPLETED, PENDING }
    private var currentFilter = FilterType.ALL

    companion object {
        private const val TAG = "GamesListActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_games_list)

        // Inicializar Firebase
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference

        // Verificar autenticación
        if (auth.currentUser == null) {
            finish()
            return
        }

        initializeViews()
        setupRecyclerView()
        setupListeners()
        loadGames()

        Log.d(TAG, "GamesListActivity iniciado")
    }

    private fun initializeViews() {
        recyclerView = findViewById(R.id.recyclerViewGames)
        progressBar = findViewById(R.id.progressBar)
        layoutEmptyState = findViewById(R.id.layoutEmptyState)
        fabAddGame = findViewById(R.id.fabAddGame)
        tvTotalGames = findViewById(R.id.tvTotalGames)
        tvCompletedGames = findViewById(R.id.tvCompletedGames)
        tvAverageRating = findViewById(R.id.tvAverageRating)
        chipAll = findViewById(R.id.chipAll)
        chipCompleted = findViewById(R.id.chipCompleted)
        chipPending = findViewById(R.id.chipPending)
    }

    private fun setupRecyclerView() {
        gameAdapter = GameAdapter(filteredGames) { game ->
            onGameClick(game)
        }

        recyclerView.apply {
            layoutManager = LinearLayoutManager(this@GamesListActivity)
            adapter = gameAdapter
            setHasFixedSize(true)
        }
    }

    private fun setupListeners() {
        // FAB para agregar juego
        fabAddGame.setOnClickListener {
            startActivity(Intent(this, AddGameActivity::class.java))
        }

        // Filtros
        chipAll.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                currentFilter = FilterType.ALL
                applyFilter()
            }
        }

        chipCompleted.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                currentFilter = FilterType.COMPLETED
                applyFilter()
            }
        }

        chipPending.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                currentFilter = FilterType.PENDING
                applyFilter()
            }
        }
    }

    private fun loadGames() {
        val userId = auth.currentUser?.uid ?: return

        showLoading(true)

        database.child("games").child(userId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    allGames.clear()

                    for (gameSnapshot in snapshot.children) {
                        val game = gameSnapshot.getValue(Game::class.java)
                        game?.let { allGames.add(it) }
                    }

                    // Ordenar por fecha de creación (más recientes primero)
                    allGames.sortByDescending { it.createdAt }

                    applyFilter()
                    updateStatistics()
                    showLoading(false)

                    Log.d(TAG, "Juegos cargados: ${allGames.size}")
                }

                override fun onCancelled(error: DatabaseError) {
                    showLoading(false)
                    showError("Error al cargar juegos: ${error.message}")
                    Log.e(TAG, "Error al cargar juegos", error.toException())
                }
            })
    }

    private fun applyFilter() {
        filteredGames.clear()

        when (currentFilter) {
            FilterType.ALL -> filteredGames.addAll(allGames)
            FilterType.COMPLETED -> filteredGames.addAll(allGames.filter { it.completed })
            FilterType.PENDING -> filteredGames.addAll(allGames.filter { !it.completed })
        }

        gameAdapter.updateGames(filteredGames)
        updateEmptyState()
    }

    private fun updateStatistics() {
        val totalGames = allGames.size
        val completedGames = allGames.count { it.completed }
        val averageRating = if (allGames.isNotEmpty()) {
            allGames.map { it.rating }.average()
        } else 0.0

        tvTotalGames.text = totalGames.toString()
        tvCompletedGames.text = completedGames.toString()
        tvAverageRating.text = String.format("%.1f", averageRating)
    }

    private fun updateEmptyState() {
        val isEmpty = filteredGames.isEmpty()
        layoutEmptyState.visibility = if (isEmpty) View.VISIBLE else View.GONE
        recyclerView.visibility = if (isEmpty) View.GONE else View.VISIBLE
    }

    private fun onGameClick(game: Game) {
        // Mostrar detalles del juego o permitir edición
        showGameDetails(game)
    }

    private fun showGameDetails(game: Game) {
        val completedText = if (game.completed) "✅ Completado" else "⏳ Pendiente"
        val ratingText = "⭐ ${game.rating}/5"

        val message = """
            🎮 ${game.title}
            
            📂 Género: ${game.genre}
            📱 Plataforma: ${game.platform}
            📅 Año: ${if (game.releaseYear > 0) game.releaseYear else "N/A"}
            $ratingText
            $completedText
            
            ${if (game.description.isNotEmpty()) "📝 ${game.description}" else ""}
        """.trimIndent()

        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Detalles del Juego")
            .setMessage(message)
            .setPositiveButton("OK", null)
            .setNeutralButton("Eliminar") { _, _ ->
                confirmDeleteGame(game)
            }
            .show()
    }

    private fun confirmDeleteGame(game: Game) {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Eliminar Juego")
            .setMessage("¿Estás seguro de que quieres eliminar '${game.title}'?")
            .setPositiveButton("Eliminar") { _, _ ->
                deleteGame(game)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun deleteGame(game: Game) {
        val userId = auth.currentUser?.uid ?: return

        database.child("games").child(userId).child(game.id).removeValue()
            .addOnSuccessListener {
                Toast.makeText(this, "Juego eliminado: ${game.title}", Toast.LENGTH_SHORT).show()
                Log.d(TAG, "Juego eliminado: ${game.title}")
            }
            .addOnFailureListener { exception ->
                showError("Error al eliminar juego: ${exception.message}")
                Log.e(TAG, "Error al eliminar juego", exception)
            }
    }

    private fun showLoading(show: Boolean) {
        progressBar.visibility = if (show) View.VISIBLE else View.GONE
    }

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    override fun onResume() {
        super.onResume()
        // Recargar datos cuando se regrese de AddGameActivity
        // Los datos se actualizan automáticamente gracias al ValueEventListener
    }
}