package com.example.adivinarnumero

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ScoreAdapter(private val items: List<Pair<String,Int>>) :
    RecyclerView.Adapter<ScoreAdapter.ViewHolder>() {

    inner class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        val tvName: TextView = v.findViewById(R.id.tvName)
        val tvScore: TextView = v.findViewById(R.id.tvScore)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        LayoutInflater.from(parent.context)
            .inflate(R.layout.item_score, parent, false)
            .let { ViewHolder(it) }

    override fun onBindViewHolder(holder: ViewHolder, pos: Int) {
        val (name,score) = items[pos]
        holder.tvName.text = name
        holder.tvScore.text = score.toString()
    }

    override fun getItemCount() = items.size
}
