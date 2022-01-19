package ie.wit.medicineapp.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import ie.wit.medicineapp.databinding.CardSideEffectBinding

interface SideEffectListener {
    fun onSideEffectBtnClick(sideEffect: String?)
}

class SideEffectAdapter constructor(private var sideEffects: MutableList<String?>, private val listener: SideEffectListener) :
    RecyclerView.Adapter<SideEffectAdapter.MainHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MainHolder {
        val binding = CardSideEffectBinding
            .inflate(LayoutInflater.from(parent.context), parent, false)

        return MainHolder(binding)
    }

    override fun onBindViewHolder(holder: MainHolder, position: Int) {
        var sideEffect = sideEffects[holder.adapterPosition]
        holder.bind(sideEffect, listener)
    }

    override fun getItemCount(): Int = sideEffects.size

    fun removeAt(position: Int) {
        sideEffects.removeAt(position)
        notifyItemRemoved(position)
    }

    class MainHolder(private val binding: CardSideEffectBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(sideEffect: String?, listener: SideEffectListener) {
            binding.SideEffectText.text = sideEffect
            binding.root.tag = sideEffect
            binding.btnDeleteSideEffect.setOnClickListener{listener.onSideEffectBtnClick(sideEffect)}
            binding.executePendingBindings()
        }
    }
}