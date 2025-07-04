package com.example.app_s10

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RatingBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*

class GameAdapter(
    private var games: List<Game>,
    private val onGameClick: (Game) -> Unit = {}
) : RecyclerView.Adapter<GameAdapter.GameViewHolder>() {

    private val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale("es", "ES"))

    class GameViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTitle: TextView = itemView.findViewById(R.id.tvGameTitle)
        val tvGenre: TextView = itemView.findViewById(R.id.tvGameGenre)
        val tvPlatform: TextView = itemView.findViewById(R.id.tvGamePlatform)
        val tvYear: TextView = itemView.findViewById(R.id.tvGameYear)
        val tvDescription: TextView = itemView.findViewById(R.id.tvGameDescription)
        val tvCompleted: TextView = itemView.findViewById(R.id.tvCompleted)
        val tvDate: TextView = itemView.findViewById(R.id.tvGameDate)
        val ratingBar: RatingBar = itemView.findViewById(R.id.ratingBarItem)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GameViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_game, parent, false)
        return GameViewHolder(view)
    }

    override fun onBindViewHolder(holder: GameViewHolder, position: Int) {
        val game = games[position]

        // Información básica
        holder.tvTitle.text = game.title
        holder.tvGenre.text = game.genre
        holder.tvPlatform.text = game.platform
        holder.tvYear.text = if (game.releaseYear > 0) game.releaseYear.toString() else "N/A"

        // Rating
        holder.ratingBar.rating = game.rating

        // Descripción (mostrar solo si no está vacía)
        if (game.description.isNotEmpty()) {
            holder.tvDescription.text = game.description
            holder.tvDescription.visibility = View.VISIBLE
        } else {
            holder.tvDescription.visibility = View.GONE
        }

        // Estado de completado
        if (game.completed) {
            holder.tvCompleted.visibility = View.VISIBLE
        } else {
            holder.tvCompleted.visibility = View.GONE
        }

        // Fecha de creación
        val date = Date(game.createdAt)
        holder.tvDate.text = "Agregado el ${dateFormat.format(date)}"

        // Click listener
        holder.itemView.setOnClickListener {
            onGameClick(game)
        }
    }

    override fun getItemCount() = games.size

    fun updateGames(newGames: List<Game>) {
        games = newGames
        notifyDataSetChanged()
    }

    fun addGame(game: Game) {
        val mutableGames = games.toMutableList()
        mutableGames.add(0, game) // Agregar al inicio
        games = mutableGames
        notifyItemInserted(0)
    }

    fun removeGame(game: Game) {
        val mutableGames = games.toMutableList()
        val index = mutableGames.indexOfFirst { it.id == game.id }
        if (index != -1) {
            mutableGames.removeAt(index)
            games = mutableGames
            notifyItemRemoved(index)
        }
    }

    fun getGameCount(): Int = games.size

    fun getCompletedGamesCount(): Int = games.count { it.completed }

    fun getAverageRating(): Float {
        return if (games.isEmpty()) 0f
        else games.map { it.rating }.average().toFloat()
    }
}