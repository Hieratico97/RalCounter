package com.monsoonmage.ralcounter.ui.fliplog

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.monsoonmage.ralcounter.R
import com.monsoonmage.ralcounter.databinding.ItemFlipResultBinding
import com.monsoonmage.ralcounter.model.FlipResult
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class FlipLogAdapter : ListAdapter<FlipResult, FlipLogAdapter.FlipViewHolder>(DIFF) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        FlipViewHolder(ItemFlipResultBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: FlipViewHolder, position: Int) =
        holder.bind(getItem(position), position + 1)

    class FlipViewHolder(private val b: ItemFlipResultBinding) : RecyclerView.ViewHolder(b.root) {
        fun bind(flip: FlipResult, index: Int) {
            val ctx = b.root.context
            b.tvFlipIndex.text = "#$index"
            b.tvTurnNumber.text = "Turno ${flip.turnNumber} · Hechizo ${flip.spellsAtTime}"
            if (flip.isWin) {
                b.tvFlipIcon.text = "⚡"
                b.tvFlipResultText.text = "GANASTE"
                b.tvFlipResultText.setTextColor(ContextCompat.getColor(ctx, R.color.win_color))
            } else {
                b.tvFlipIcon.text = "💀"
                b.tvFlipResultText.text = "PERDISTE"
                b.tvFlipResultText.setTextColor(ContextCompat.getColor(ctx, R.color.lose_color))
            }
            val sdf = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
            b.tvTimestamp.text = sdf.format(Date(flip.timestamp))
        }
    }

    companion object {
        val DIFF = object : DiffUtil.ItemCallback<FlipResult>() {
            override fun areItemsTheSame(a: FlipResult, b: FlipResult) = a.timestamp == b.timestamp
            override fun areContentsTheSame(a: FlipResult, b: FlipResult) = a == b
        }
    }
}
